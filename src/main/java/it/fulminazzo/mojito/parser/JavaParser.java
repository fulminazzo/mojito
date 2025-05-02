package it.fulminazzo.mojito.parser;

import it.fulminazzo.fulmicollection.objects.Refl;
import it.fulminazzo.fulmicollection.utils.ReflectionUtils;
import it.fulminazzo.fulmicollection.utils.StringUtils;
import it.fulminazzo.mojito.parser.node.*;
import it.fulminazzo.mojito.parser.node.arrays.DynamicArray;
import it.fulminazzo.mojito.parser.node.arrays.StaticArray;
import it.fulminazzo.mojito.parser.node.container.CodeBlock;
import it.fulminazzo.mojito.parser.node.container.JavaProgram;
import it.fulminazzo.mojito.parser.node.literals.*;
import it.fulminazzo.mojito.parser.node.operators.binary.*;
import it.fulminazzo.mojito.parser.node.operators.unary.Decrement;
import it.fulminazzo.mojito.parser.node.operators.unary.Increment;
import it.fulminazzo.mojito.parser.node.operators.unary.Minus;
import it.fulminazzo.mojito.parser.node.operators.unary.Not;
import it.fulminazzo.mojito.parser.node.statements.*;
import it.fulminazzo.mojito.parser.node.values.*;
import it.fulminazzo.mojito.tokenizer.TokenType;
import it.fulminazzo.mojito.tokenizer.Tokenizer;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import static it.fulminazzo.mojito.tokenizer.TokenType.*;

/**
 * A parser to read Java code using {@link Tokenizer} and {@link TokenType}.
 */
@NoArgsConstructor
public class JavaParser extends Parser {

    /**
     * Instantiates a new Java parser.
     *
     * @param input the input
     */
    public JavaParser(@NotNull InputStream input) {
        super(input);
    }

    /**
     * JAVA_PROGRAM := SINGLE_STMT*
     *
     * @return the node
     */
    public @NotNull JavaProgram parseProgram() {
        final LinkedList<Statement> statements = new LinkedList<>();
        nextSpaceless();
        while (lastToken() != EOF) statements.add(parseSingleStatement());
        return new JavaProgram(statements);
    }

    /**
     * BLOCK := CODE_BLOCK | SINGLE_STMT
     *
     * @return the node
     */
    protected @NotNull CodeBlock parseBlock() {
        if (lastToken() == OPEN_BRACE) return parseCodeBlock();
        else return new CodeBlock(parseSingleStatement());
    }

    /**
     * CODE_BLOCK := \{ SINGLE_STMT* \}
     *
     * @return the node
     */
    protected @NotNull CodeBlock parseCodeBlock() {
        consume(OPEN_BRACE);
        final LinkedList<Statement> statements = new LinkedList<>();
        while (lastToken() != CLOSE_BRACE) statements.add(parseSingleStatement());
        consume(CLOSE_BRACE);
        return new CodeBlock(statements);
    }

    /**
     * SINGLE_STMT := STMT | ;
     *
     * @return the node
     */
    protected @NotNull Statement parseSingleStatement() {
        final Statement statement;
        // Read comments
        @NotNull Tokenizer tokenizer = this.getTokenizer();
        if (lastToken() == COMMENT_INLINE) {
            tokenizer.readUntilNextLine();
            return parseSingleStatement();
        } else if (lastToken() == COMMENT_BLOCK_START) {
            getTokenizer().readUntil(COMMENT_BLOCK_END);
            return parseSingleStatement();
        }

        if (lastToken() == SEMICOLON) {
            consume(SEMICOLON);
            statement = new Statement();
        } else statement = parseStatement();
        return statement;
    }

    /**
     * STMT := return EXPR; | throw EXPR; break; | continue; |
     *         TRY_STMT | SWITCH_STMT | FOR_STMT | DO_STMT | WHILE_STMT | IF_STMT
     *         ASSIGNMENT;
     *
     * @return the node
     */
    protected @NotNull Statement parseStatement() {
        final Node exp;
        switch (lastToken()) {
            case RETURN: {
                consume(RETURN);
                Statement returnNode = new Return(parseExpression());
                if (lastToken() == SEMICOLON) consume(SEMICOLON);
                return returnNode;
            }
            case THROW: {
                consume(THROW);
                Statement throwNode = new Throw(parseExpression());
                consume(SEMICOLON);
                return throwNode;
            }
            case BREAK: {
                consume(BREAK);
                consume(SEMICOLON);
                return new Break();
            }
            case CONTINUE: {
                consume(CONTINUE);
                consume(SEMICOLON);
                return new Continue();
            }
            case TRY:
                return parseTryStatement();
            case SWITCH:
                return parseSwitchStatement();
            case FOR:
                return parseForStatement();
            case DO:
                return parseDoStatement();
            case WHILE:
                return parseWhileStatement();
            case IF:
                return parseIfStatement();
            default: {
                exp = parseAssignment();
                consume(SEMICOLON);
            }
        }
        return new Statement(exp);
    }

    /**
     * TRY_STMT := try ( \( ASSIGNMENT_BLOCK \) )? CODE_BLOCK CATCH+ ( finally CODE_BLOCK )?
     *
     * @return the node
     */
    protected @NotNull TryStatement parseTryStatement() {
        consume(TRY);

        final AssignmentBlock assignmentBlock;
        if (lastToken() == OPEN_PAR) {
            consume(OPEN_PAR);
            assignmentBlock = parseAssignmentBlock();
            consume(CLOSE_PAR);
        } else assignmentBlock = new AssignmentBlock(new LinkedList<>());

        final CodeBlock block = parseCodeBlock();

        final List<CatchStatement> catchBlocks = new LinkedList<>();
        while (lastToken() == CATCH) catchBlocks.add(parseCatchStatement());

        CodeBlock finallyBlock = null;
        if (lastToken() == FINALLY) {
            consume(FINALLY);
            finallyBlock = parseCodeBlock();
        }

        // If no catch and no finally block was specified, throw error
        if (finallyBlock == null) {
            if (catchBlocks.isEmpty()) throw ParserException.invalidTryStatement(this);
            else finallyBlock = new CodeBlock();
        }

        return new TryStatement(assignmentBlock, block, catchBlocks, finallyBlock);
    }

    /**
     * ASSIGNMENT_BLOCK := (ARRAY_LITERAL LITERAL ( = EXPR? ); )+
     *
     * @return the node
     */
    protected @NotNull AssignmentBlock parseAssignmentBlock() {
        List<Assignment> assignments = new LinkedList<>();
        do {
            Node node = parseAssignment();
            if (node.is(Assignment.class)) assignments.add((Assignment) node);
            else throw ParserException.invalidNodeProvided(this, Assignment.class, node);
        } while (lastToken() == SEMICOLON && consume(SEMICOLON) == LITERAL);
        return new AssignmentBlock(assignments);
    }

    /**
     * CATCH := catch \( ( LITERAL \| )* LITERAL LITERAL \) CODE_BLOCK
     *
     * @return the node
     */
    protected @NotNull CatchStatement parseCatchStatement() {
        consume(CATCH);
        consume(OPEN_PAR);
        List<Literal> exceptions = new LinkedList<>();
        do exceptions.add(parseLiteral());
        while (lastToken() == BIT_OR && consume(BIT_OR) == LITERAL);
        Literal exceptionsName = parseLiteral();
        consume(CLOSE_PAR);
        CodeBlock block = parseCodeBlock();
        return new CatchStatement(exceptions, exceptionsName, block);
    }

    /**
     * SWITCH_STMT := switch \( EXPR \) \{ (CASE_BLOCK)* (DEFAULT_BLOCK)? \}
     *
     * @return the node
     */
    protected @NotNull Statement parseSwitchStatement() {
        consume(SWITCH);
        Node expression = parseExpression();
        List<CaseStatement> caseStatements = new LinkedList<>();
        CodeBlock defaultBlock = null;
        consume(OPEN_BRACE);
        while (lastToken() != CLOSE_BRACE) {
            if (lastToken() == CASE) {
                CaseStatement caseStatement = parseCaseBlock();
                if (caseStatements.stream().anyMatch(c -> c.getExpression().equals(caseStatement.getExpression())))
                    throw ParserException.caseBlockAlreadyDefined(this, caseStatement);
                else caseStatements.add(caseStatement);
            } else if (defaultBlock == null) defaultBlock = parseDefaultBlock();
            else throw ParserException.defaultBlockAlreadyDefined(this);
        }
        if (defaultBlock == null) defaultBlock = new CodeBlock();
        consume(CLOSE_BRACE);
        return new SwitchStatement(expression, caseStatements, defaultBlock);
    }

    /**
     * CASE_BLOCK := case EXPR: ( CODE_BLOCK | SINGLE_STMT* )
     *
     * @return the node
     */
    protected @NotNull CaseStatement parseCaseBlock() {
        consume(CASE);
        Node expression = parseExpression();
        consume(COLON);
        LinkedList<Statement> statements = new LinkedList<>();
        if (lastToken() == OPEN_BRACE)
            statements.addAll(parseCodeBlock().getStatements());
        else while (tokenIsValidForCaseDefaultBlock()) statements.add(parseSingleStatement());
        return new CaseStatement(expression, new CodeBlock(statements));
    }

    /**
     * DEFAULT_BLOCK := default: ( CODE_BLOCK | SINGLE_STMT* )
     *
     * @return the node
     */
    protected @NotNull CodeBlock parseDefaultBlock() {
        consume(DEFAULT);
        consume(COLON);
        LinkedList<Statement> statements = new LinkedList<>();
        if (lastToken() == OPEN_BRACE)
            statements.addAll(parseCodeBlock().getStatements());
        else while (tokenIsValidForCaseDefaultBlock()) statements.add(parseSingleStatement());
        return new CodeBlock(statements);
    }

    private boolean tokenIsValidForCaseDefaultBlock() {
        return lastToken() != CLOSE_BRACE && lastToken() != CASE && lastToken() != DEFAULT;
    }

    /**
     * FOR_STMT := for \( ASSIGNMENT?; EXPR?; EXPR? \) BLOCK | ENHANCED_FOR_STMT
     *
     * @return the node
     */
    protected @NotNull Statement parseForStatement() {
        consume(FOR);
        consume(OPEN_PAR);

        Node assignment;
        if (lastToken() != SEMICOLON) {
            assignment = parseAssignment();
            if (assignment.is(Assignment.class)) {
                Assignment ass = (Assignment) assignment;
                if (!ass.isInitialized() && lastToken() == COLON)
                    return parseEnhancedForStatement(ass);
            }
        } else assignment = new EmptyLiteral();
        consume(SEMICOLON);

        Node condition = lastToken() == SEMICOLON ? new EmptyLiteral() : parseExpression();
        consume(SEMICOLON);

        Node increment = lastToken() == CLOSE_PAR ? new EmptyLiteral() : parseExpression();
        consume(CLOSE_PAR);

        CodeBlock block = parseBlock();
        return new ForStatement(assignment, condition, increment, block);
    }

    /**
     * ENHANCED_FOR_STMT := for \( ARRAY_LITERAL LITERAL : EXPR \) BLOCK
     *
     * @return the node
     */
    private @NotNull EnhancedForStatement parseEnhancedForStatement(final @NotNull Assignment assignment) {
        consume(COLON);
        Node expression = parseExpression();
        consume(CLOSE_PAR);
        CodeBlock block = parseBlock();
        return new EnhancedForStatement(assignment.getType(), assignment.getName(), expression, block);
    }

    /**
     * DO_STMT := do BLOCK while PAR_EXPR
     *
     * @return the node
     */
    protected @NotNull DoStatement parseDoStatement() {
        consume(DO);
        CodeBlock codeBlock = parseBlock();
        consume(WHILE);
        Node expression = parseParenthesizedExpr();
        consume(SEMICOLON);
        return new DoStatement(expression, codeBlock);
    }

    /**
     * WHILE_STMT := while PAR_EXPR BLOCK
     *
     * @return the node
     */
    protected @NotNull WhileStatement parseWhileStatement() {
        consume(WHILE);
        Node expression = parseParenthesizedExpr();
        CodeBlock codeBlock = parseBlock();
        return new WhileStatement(expression, codeBlock);
    }

    /**
     * IF_STMT := if PAR_EXPR BLOCK ( else IF_STMT )* ( else BLOCK )?
     *
     * @return the node
     */
    protected @NotNull IfStatement parseIfStatement() {
        consume(IF);
        Node expression = parseParenthesizedExpr();
        CodeBlock codeBlock = parseBlock();
        if (lastToken() == ELSE) {
            consume(ELSE);
            if (lastToken() == IF)
                return new IfStatement(expression, codeBlock, parseIfStatement());
            else return new IfStatement(expression, codeBlock, parseBlock());
        }
        return new IfStatement(expression, codeBlock, new EmptyLiteral());
    }

    /**
     * ASSIGNMENT := ( final )? ARRAY_LITERAL LITERAL ( = EXPR? ) | LITERAL = EXPR | EXPR
     *
     * @return the node
     */
    protected @NotNull Node parseAssignment() {
        boolean finalVariable = false;
        if (lastToken() == FINAL) {
            consume(FINAL);
            finalVariable = true;
        }
        Node expression = parseExpression();
        if (expression.is(Literal.class)) {
            if (lastToken() == LITERAL || expression.is(ArrayLiteral.class)) {
                Literal name = parseLiteral();
                Node value = new EmptyLiteral();
                if (lastToken() == ASSIGN) {
                    consume(ASSIGN);
                    value = parseExpression();
                }
                Assignment assignment = new Assignment(expression, name, value);
                if (finalVariable) {
                    if (assignment.isInitialized()) return new FinalAssignment(assignment);
                    else throw ParserException.finalVariableNotInitialized(this, assignment);
                } else expression = assignment;
            } else {
                consume(ASSIGN);
                expression = new ReAssign(expression, parseExpression());
            }
        }
        if (finalVariable) throw ParserException.finalNotAllowed(this, expression);
        return expression;
    }

    /**
     * EXPR := NEW_OBJECT | INCREMENT | DECREMENT | AND
     *
     * @return the node
     */
    protected @NotNull Node parseExpression() {
        Node expression;
        switch (lastToken()) {
            case NEW: {
                expression = parseNewObject();
                break;
            }
            case ADD: {
                consume(ADD);
                expression = parseIncrement();
                break;
            }
            case SUBTRACT: {
                consume(SUBTRACT);
                expression = parseDecrement();
                break;
            }
            default: {
                expression = parseBinaryComparison();
            }
        }
        return expression;
    }

    /**
     * NEW_OBJECT := new LITERAL METHOD_INVOCATION |
     *               new ARRAY_LITERAL{ ( EXPR )? ( , EXPR )* \} |
     *               new LITERAL ( \[ NUMBER_VALUE \] )+
     *
     * @return the node
     */
    protected @NotNull Node parseNewObject() {
        match(NEW);
        next(); // Necessary space
        consume(SPACE);
        Node literal = parseLiteral();

        if (lastToken() == OPEN_BRACKET) {
            // array initialization
            while (lastToken() == OPEN_BRACKET) {
                consume(OPEN_BRACKET);
                if (lastToken() == CLOSE_BRACKET) {
                    consume(CLOSE_BRACKET);
                    literal = new ArrayLiteral(literal);
                } else break;
            }

            if (lastToken() == OPEN_BRACE) {
                // dynamic array initialization
                List<Node> parameters = new LinkedList<>();
                consume(OPEN_BRACE);
                while (lastToken() != CLOSE_BRACE) {
                    parameters.add(parseExpression());
                    if (lastToken() == COMMA) consume(COMMA);
                }
                consume(CLOSE_BRACE);
                return new DynamicArray(literal, parameters);
            } else {
                // static array initialization
                StaticArray array = null;
                if (literal.is(ArrayLiteral.class)) {
                    while (literal.is(ArrayLiteral.class)) {
                        literal = ((ArrayLiteral) literal).getType();
                        StaticArray innerArray = new StaticArray(literal,
                                createLiteral(NumberValueLiteral.class, "0"));
                        if (array == null) array = innerArray;
                        else array.updateComponentType(innerArray);
                    }
                }

                array = parseStaticArray(array, literal);
                consume(CLOSE_BRACKET);

                while (lastToken() == OPEN_BRACKET) {
                    consume(OPEN_BRACKET);
                    array = parseStaticArray(array, literal);
                    consume(CLOSE_BRACKET);
                }
                return array;
            }
        } else {
            // new object
            MethodInvocation invocation = parseMethodInvocation();
            return new NewObject(literal, invocation);
        }
    }

    /**
     * Support method for parsing {@link StaticArray} in {@link #parseNewObject()}.
     * Does NOT check for {@link TokenType#OPEN_BRACKET} or {@link TokenType#CLOSE_BRACKET}.
     *
     * @param array the previously parsed {@link StaticArray}
     * @param type  the type of the elements
     * @return the newly parsed {@link StaticArray}
     */
    protected @NotNull StaticArray parseStaticArray(@Nullable StaticArray array,
                                                    final @NotNull Node type) {
        NumberValueLiteral size;
        if (lastToken() != CLOSE_BRACKET) size = (NumberValueLiteral) parseTypeValue();
        else size = createLiteral(NumberValueLiteral.class, "0");
        if (array == null) array = new StaticArray(type, size);
        else array.updateComponentType(new StaticArray(type, size));
        return array;
    }

    /**
     * ARRAY_LITERAL := LITERAL ( \[\] )* | LITERAL ( \[ [0-9]+ \] )+
     *
     * @param expression the expression to start from
     * @return the node
     */
    protected @NotNull Node parseArrayLiteral(@NotNull Node expression) {
        if (lastToken() == OPEN_BRACKET) {
            consume(OPEN_BRACKET);
            if (lastToken() == NUMBER_VALUE) {
                expression = new ArrayIndex(expression, parseExpression());
                consume(CLOSE_BRACKET);
                while (lastToken() == OPEN_BRACKET) {
                    consume(OPEN_BRACKET);
                    expression = new ArrayIndex(expression, parseExpression());
                    consume(CLOSE_BRACKET);
                }
            } else {
                consume(CLOSE_BRACKET);
                expression = new ArrayLiteral(expression);
                while (lastToken() == OPEN_BRACKET) {
                    consume(OPEN_BRACKET);
                    consume(CLOSE_BRACKET);
                    expression = new ArrayLiteral(expression);
                }
            }
        }
        return expression;
    }

    /**
     * INCREMENT := ++ATOM
     *
     * @return the node
     */
    protected @NotNull Increment parseIncrement() {
        consume(ADD);
        return new Increment(parseAtom(), true);
    }

    /**
     * DECREMENT := --ATOM | MINUS
     *
     * @return the node
     */
    protected @NotNull Node parseDecrement() {
        if (lastToken() != SUBTRACT) return new Minus(parseExpression());
        consume(SUBTRACT);
        return new Decrement(parseAtom(), true);
    }

    /**
     * AND := OR (&amp;&amp; OR)*
     *
     * @return the node
     */
    protected @NotNull Node parseBinaryComparison() {
        return parseBinaryComparison(AND);
    }

    /**
     * AND := OR (&amp;&amp; OR)* <br>
     * OR := EQUAL (|| EQUAL)* <br>
     * EQUAL := NOT_EQUAL (== NOT_EQUAL)* <br>
     * NOT_EQUAL := LESS_THAN (!= LESS_THAN)* <br>
     * LESS_THAN := LESS_THAN_EQUAL (&lt; LESS_THAN_EQUAL)* <br>
     * LESS_THAN_EQUAL := GREATER_THAN (&lt;= GREATER_THAN)* <br>
     * GREATER_THAN := GREATER_THAN_EQUAL (&gt; GREATER_THAN_EQUAL)* <br>
     * GREATER_THAN_EQUAL := BIT_AND (&gt;= BIT_AND)*
     *
     * @param comparison the {@link TokenType} that corresponds to the comparison
     * @return the node
     */
    protected @NotNull Node parseBinaryComparison(final @NotNull TokenType comparison) {
        if (comparison.after(GREATER_THAN_EQUAL)) return parseBinaryOperation();
        else {
            final TokenType nextOperation = TokenType.values()[comparison.ordinal() + 1];
            Node node = parseBinaryComparison(nextOperation);
            while (lastToken() == comparison) {
                consume(comparison);
                Node tmp = parseBinaryComparison(nextOperation);
                node = generateBinaryOperationFromToken(comparison, node, tmp);
            }
            return node;
        }
    }

    /**
     * BIT_AND := BIT_OR ( (&amp; BIT_OR)* | (&amp;= BIT_OR) )
     *
     * @return the node
     */
    protected @NotNull Node parseBinaryOperation() {
        return parseBinaryOperation(BIT_AND);
    }

    /**
     * BIT_AND := BIT_OR ( (&amp; BIT_OR)* | (&amp;= BIT_OR) ) <br>
     * BIT_OR := BIT_XOR ( (| BIT_XOR)* | (|= BIT_XOR) ) <br>
     * BIT_XOR := LSHIFT ( (^ LSHIFT)* | (^= LSHIFT) ) <br>
     * LSHIFT := RSHIFT ( (&lt;&lt; RSHIFT)* | (&lt;&lt;= RSHIFT) ) <br>
     * RSHIFT := URSHIFT ( (&gt;&gt; URSHIFT)* | (&gt;&gt;= URSHIFT) ) <br>
     * URSHIFT := ADD ( (&gt;&gt;&gt; ADD)* | (&gt;&gt;&gt;= ADD) ) <br>
     * ADD := SUB ( (+ SUB)* | (+= SUB) | ++ ) <br>
     * SUB := MUL ( (- MUL)* | (-= MUL) | -- ) <br>
     * MUL := DIV ( (* DIV)* | (*= DIV) ) <br>
     * DIV := MOD ( (/ MOD)* | (/= MOD) ) <br>
     * MOD := UNARY_OPERATION ( (% UNARY_OPERATION)* | (%= UNARY_OPERATION) )
     *
     * @param operation the {@link TokenType} that corresponds to the operation
     * @return the node
     */
    protected @NotNull Node parseBinaryOperation(final @NotNull TokenType operation) {
        if (operation.after(MODULO)) return parseUnaryOperation();
        else {
            final TokenType nextOperation = TokenType.values()[operation.ordinal() + 1];
            Node node = parseBinaryOperation(nextOperation);
            while (lastToken() == operation) {
                consume(operation);
                TokenType lastToken = lastToken();
                if (operation == ADD && lastToken == ADD) {
                    consume(ADD);
                    return unwrapCast(node, n -> new Increment(n, false));
                } else if (operation == SUBTRACT && lastToken == SUBTRACT) {
                    consume(SUBTRACT);
                    return unwrapCast(node, n -> new Decrement(n, false));
                } else if (lastToken == ASSIGN) {
                    consume(ASSIGN);
                    Node nextOperationNode = parseBinaryOperation(nextOperation);
                    Node newNode = generateBinaryOperationFromToken(operation, node, nextOperationNode);
                    node = new ReAssign(node, newNode);
                } else {
                    Node nextOperationNode = parseBinaryOperation(nextOperation);
                    node = generateBinaryOperationFromToken(operation, node, nextOperationNode);
                }
            }
            return node;
        }
    }

    /**
     * Because of how the grammar works, expressions of the type:
     * <br>
     * <i>(double) i++</i>
     * <br>
     * will be parsed as:
     * <br>
     * <i>Increment(Cast(Double), i)</i>
     * <br>
     * while the Java specification asserts the opposite.
     * To comply with it, this method will unwrap a {@link Cast} node
     * and update its cast object to the resulting node of the conversion function.
     *
     * @param node               the node
     * @param conversionFunction conversion function
     * @return the re-wrapped node
     */
    @NotNull Node unwrapCast(final @NotNull Node node,
                             final @NotNull Function<Node, Node> conversionFunction) {
        if (node.is(Cast.class)) {
            Cast cast = (Cast) node;
            Node type = cast.getType();
            Node expression = cast.getExpression();
            return new Cast(type, unwrapCast(expression, conversionFunction));
        } else return conversionFunction.apply(node);
    }

    /**
     * Creates a new {@link BinaryOperation} object from the given {@link TokenType}.
     * <br>
     * Uses {@link #findBinaryOperationClass(String)} to search the most appropriate class.
     *
     * @param operation the token that identifies the binary operation
     * @param left      the left operand
     * @param right     the right operand
     * @return the node object
     */
    protected @NotNull Node generateBinaryOperationFromToken(final @NotNull TokenType operation,
                                                             final @NotNull Node left,
                                                             final @NotNull Node right) {
        Class<? extends BinaryOperation> clazz = findBinaryOperationClass(operation.name());
        return new Refl<>(clazz, left, right).getObject();
    }

    /**
     * Finds the most appropriate {@link BinaryOperation} class from the given string.
     * <br>
     * Throws {@link IllegalArgumentException} if not found.
     *
     * @param className the class name
     * @return the class
     */
    protected @NotNull Class<? extends BinaryOperation> findBinaryOperationClass(@NotNull String className) {
        if (className.equals(URSHIFT.name())) return URShift.class;
        else if (className.equals(RSHIFT.name())) return RShift.class;
        else if (className.equals(LSHIFT.name())) return LShift.class;
        else {
            className = StringUtils.capitalize(className).replace("_", "");
            return ReflectionUtils.getClass(BinaryOperation.class.getPackage().getName() + "." + className);
        }
    }

    /**
     * UNARY_OPERATION := CAST |  NOT | METHOD_CALL
     *
     * @return the node
     */
    protected @NotNull Node parseUnaryOperation() {
        switch (lastToken()) {
            case OPEN_PAR:
                return parseCast();
            case NOT:
                return parseNot();
            default:
                return parseMethodCall();
        }
    }

    /**
     * CAST := (PAR_EXPR)* (EXPR | PAR_EXPR)
     *
     * @return the node
     */
    protected @NotNull Node parseCast() {
        Node expression = parseParenthesizedExpr();
        if (lastToken().between(MODULO, SPACE) || lastToken() == OPEN_PAR)
            expression = new Cast(expression, parseUnaryOperation());
        else if (lastToken() == ADD) {
            consume(ADD);
            if (lastToken() == ADD) expression = new Cast(expression, parseIncrement());
            else return new Add(expression, parseExpression());
        } else if (lastToken() == SUBTRACT) {
            consume(SUBTRACT);
            if (lastToken() == SUBTRACT) expression = new Cast(expression, parseDecrement());
            else if (expression.is(Literal.class) &&
                    (lastToken().between(MODULO, SPACE) || lastToken() == OPEN_PAR))
                expression = new Cast(expression, new Minus(parseUnaryOperation()));
            else return new Subtract(expression, parseExpression());
        }
        return expression;
    }

    /**
     * PAR_EXPR := \( EXPR \)
     *
     * @return the node
     */
    protected @NotNull Node parseParenthesizedExpr() {
        consume(OPEN_PAR);
        Node expression = parseExpression();
        consume(CLOSE_PAR);
        return expression;
    }

    /**
     * MINUS := - EXPR
     *
     * @return the node
     */
    protected @NotNull Node parseMinus() {
        consume(SUBTRACT);
        return new Minus(parseExpression());
    }

    /**
     * NOT := ! EXPR
     *
     * @return the node
     */
    protected @NotNull Node parseNot() {
        consume(NOT);
        return new Not(parseExpression());
    }

    /**
     * METHOD_CALL := ATOM ( .LITERAL METHOD_INVOCATION? )*
     *
     * @return the node
     */
    protected @NotNull Node parseMethodCall() {
        Node node = parseAtom();
        if (node.is(Literal.class) && lastToken() == OPEN_PAR) {
            Literal literalNode = (Literal) node;
            String literal = literalNode.getLiteral();
            final @NotNull Node executor;
            final @NotNull String methodName;
            if (literal.contains(".")) {
                executor = getLiteralFromString(literal.substring(0, literal.lastIndexOf(".")));
                methodName = literal.substring(literal.lastIndexOf(".") + 1);
            } else {
                executor = new EmptyLiteral();
                methodName = literal;
            }
            node = new MethodCall(executor, methodName, parseMethodInvocation());
        }
        while (lastToken() == DOT) {
            Literal methodName = parseLiteralNoDot();
            if (lastToken() == OPEN_PAR)
                node = new MethodCall(node, methodName.getLiteral(), parseMethodInvocation());
            else node = new Field(node, methodName);
        }
        return node;
    }

    /**
     * METHOD_INVOCATION := \( (EXPR)? (, EXPR)* \)
     *
     * @return the node
     */
    protected @NotNull MethodInvocation parseMethodInvocation() {
        List<Node> parameters = new LinkedList<>();
        consume(OPEN_PAR);
        while (lastToken() != CLOSE_PAR) {
            parameters.add(parseExpression());
            if (lastToken() == COMMA) consume(COMMA);
        }
        consume(CLOSE_PAR);
        return new MethodInvocation(parameters);
    }

    /**
     * ATOM := NULL | THIS | ARRAY_LITERAL | TYPE_VALUE
     *
     * @return the node
     */
    protected @NotNull Node parseAtom() {
        switch (lastToken()) {
            case NULL:
                return parseNull();
            case THIS:
                return parseThis();
            case LITERAL:
                return parseArrayLiteral(parseLiteral());
            default:
                return parseTypeValue();
        }
    }

    /**
     * NULL := {@link TokenType#NULL}
     *
     * @return the node
     */
    protected @NotNull Node parseNull() {
        consume(NULL);
        return new NullLiteral();
    }

    /**
     * THIS := {@link TokenType#THIS}
     *
     * @return the node
     */
    protected @NotNull Node parseThis() {
        consume(THIS);
        return new ThisLiteral();
    }

    /**
     * LITERAL := {@link TokenType#LITERAL}
     *
     * @return the node
     */
    protected @NotNull Literal parseLiteral() {
        final String literal = getTokenizer().lastRead();
        Literal l = getLiteralFromString(literal);
        consume(LITERAL);
        return l;
    }

    /**
     * Converts the given string to a {@link Literal}.
     * Throws {@link ParserException#invalidValueProvided(Parser, String)} in case of error.
     *
     * @param literal the string
     * @return the literal
     */
    @NotNull Literal getLiteralFromString(final @NotNull String literal) {
        try {
            return Literal.of(literal);
        } catch (NodeException e) {
            throw ParserException.invalidValueProvided(this, literal);
        }
    }

    /**
     * Parses {@link #parseLiteral()} with no {@link TokenType#DOT}
     *
     * @return the node
     */
    protected @NotNull Literal parseLiteralNoDot() {
        getTokenizer().nextUntil(DOT);
        return parseLiteral();
    }

    /**
     * TYPE_VALUE := {@link TokenType#NUMBER_VALUE} | {@link TokenType#LONG_VALUE} |
     *               {@link TokenType#DOUBLE_VALUE} | {@link TokenType#FLOAT_VALUE} |
     *               {@link TokenType#BOOLEAN_VALUE} | {@link TokenType#CHAR_VALUE} |
     *               {@link TokenType#STRING_VALUE}
     *
     * @return the node
     */
    protected @NotNull ValueLiteral parseTypeValue() {
        final String read = getTokenizer().lastRead();
        final ValueLiteral literal;
        switch (lastToken()) {
            case NUMBER_VALUE: {
                literal = createLiteral(NumberValueLiteral.class, read);
                break;
            }
            case LONG_VALUE: {
                literal = createLiteral(LongValueLiteral.class, read);
                break;
            }
            case DOUBLE_VALUE: {
                literal = createLiteral(DoubleValueLiteral.class, read);
                break;
            }
            case FLOAT_VALUE: {
                literal = createLiteral(FloatValueLiteral.class, read);
                break;
            }
            case BOOLEAN_VALUE: {
                literal = createLiteral(BooleanValueLiteral.class, read);
                break;
            }
            case CHAR_VALUE: {
                literal = createLiteral(CharValueLiteral.class, read);
                break;
            }
            case STRING_VALUE: {
                literal = createLiteral(StringValueLiteral.class, read);
                break;
            }
            default:
                throw ParserException.unexpectedToken(this, lastToken());
        }
        nextSpaceless();
        return literal;
    }

    /**
     * Creates a new {@link ValueLiteral} from the given class and rawValue.
     * Throws {@link ParserException} in case a {@link NodeException} occurs.
     *
     * @param literalType the type of the literal
     * @param rawValue    the raw value
     * @param <L>         the type of the literal
     * @return the literal
     */
    protected <L extends ValueLiteral> @NotNull L createLiteral(final @NotNull Class<L> literalType,
                                                                final @NotNull String rawValue) {
        try {
            return new Refl<>(literalType, rawValue).getObject();
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            if (cause instanceof NodeException)
                throw ParserException.invalidValueProvided(this, rawValue);
            else throw e;
        }
    }

}
