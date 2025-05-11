package it.fulminazzo.mojito.executor;

import it.fulminazzo.fulmicollection.structures.tuples.Tuple;
import it.fulminazzo.mojito.environment.Environment;
import it.fulminazzo.mojito.environment.scopetypes.ScopeType;
import it.fulminazzo.mojito.executor.values.ClassValue;
import it.fulminazzo.mojito.executor.values.*;
import it.fulminazzo.mojito.executor.values.arrays.ArrayClassValue;
import it.fulminazzo.mojito.executor.values.arrays.ArrayValue;
import it.fulminazzo.mojito.executor.values.objects.ObjectClassValue;
import it.fulminazzo.mojito.executor.values.objects.ObjectValue;
import it.fulminazzo.mojito.executor.values.primitivevalue.BooleanValue;
import it.fulminazzo.mojito.executor.values.primitivevalue.PrimitiveValue;
import it.fulminazzo.mojito.executor.values.variables.ArrayValueVariableContainer;
import it.fulminazzo.mojito.executor.values.variables.ValueLiteralVariableContainer;
import it.fulminazzo.mojito.parser.node.Node;
import it.fulminazzo.mojito.parser.node.container.CodeBlock;
import it.fulminazzo.mojito.parser.node.literals.Literal;
import it.fulminazzo.mojito.parser.node.statements.CaseStatement;
import it.fulminazzo.mojito.parser.node.statements.CatchStatement;
import it.fulminazzo.mojito.utils.MapUtils;
import it.fulminazzo.mojito.visitors.Visitor;
import it.fulminazzo.mojito.visitors.visitorobjects.variables.VariableContainer;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A {@link Visitor} that executes all the objects of the parsed code.
 */
@SuppressWarnings("unchecked")
@Getter
public class Executor implements Visitor<ClassValue<?>, Value<?>, ParameterValues> {
    private final @NotNull Object executingObject;
    private final @NotNull Environment<Value<?>> environment;

    /**
     * Instantiates a new Executor.
     *
     * @param executingObject the executing object
     */
    public Executor(final @NotNull Object executingObject) {
        this.executingObject = executingObject;
        this.environment = new Environment<>();
    }

    @Override
    public @NotNull Value<?> visitThrow(@NotNull Node expression) {
        Value<? extends Throwable> value = (Value<? extends Throwable>) expression.accept(this);
        throw new ExceptionWrapper(value);
    }

    @Override
    public @NotNull Value<?> visitBreak(@NotNull Node expression) {
        throw new BreakException();
    }

    @Override
    public @NotNull Value<?> visitContinue(@NotNull Node expression) {
        throw new ContinueException();
    }

    @Override
    public @NotNull Value<?> visitTryStatement(@NotNull CodeBlock block, @NotNull List<CatchStatement> catchBlocks,
                                               @NotNull CodeBlock finallyBlock, @NotNull Node assignments) {
        return visitScoped(ScopeType.TRY, () -> {
            assignments.accept(this);

            Map<ExceptionTuple, CodeBlock> exceptionsMap = new HashMap<>();
            for (CatchStatement catchStatement : catchBlocks) {
                TupleValue<List<ExceptionTuple>, CodeBlock> catchExceptions = (TupleValue<List<ExceptionTuple>, CodeBlock>)
                        catchStatement.accept(this);
                for (ExceptionTuple tuple : catchExceptions.getKey())
                    exceptionsMap.put(tuple, catchExceptions.getValue());
            }

            Value<?> returnedValue;
            try {
                returnedValue = block.accept(this);
            } catch (ExceptionWrapper e) {
                Value<? extends Throwable> exception = e.getActualException();
                Tuple<ExceptionTuple, CodeBlock> keyAndValue = MapUtils.getKeyAndValue(exceptionsMap, exception.toClass());
                returnedValue = visitScoped(ScopeType.CATCH, () -> {
                    ExceptionTuple key = keyAndValue.getKey();
                    this.environment.declare(key.getExceptionType(), key.getExceptionName().namedEntity(), exception);
                    return keyAndValue.getValue().accept(this);
                });
            } finally {
                Value<?> finalValue = finallyBlock.accept(this);
                if (!finalValue.is(Values.NO_VALUE)) returnedValue = finalValue;
            }
            return returnedValue;
        });
    }

    /**
     * Parses all the exceptions in a list of {@link ExceptionTuple}s
     * with the expression as associated name.
     * Then, returns a {@link TupleValue} containing the block as value.
     *
     * @param exceptions the exceptions
     * @param block      the block
     * @param expression the expression
     * @return the catch statement
     */
    @Override
    public @NotNull Value<?> visitCatchStatement(@NotNull List<Literal> exceptions, @NotNull CodeBlock block,
                                                 @NotNull Node expression) {
        List<ExceptionTuple> exceptionTuples = new ArrayList<>();
        for (Literal literal : exceptions)
            exceptionTuples.add(new ExceptionTuple(
                    literal.accept(this).checkClass(),
                    expression.accept(this).check(ValueLiteralVariableContainer.class)
            ));
        return new TupleValue<>(exceptionTuples, block);
    }

    @Override
    public @NotNull Value<?> visitSwitchStatement(@NotNull List<CaseStatement> cases, @NotNull CodeBlock defaultBlock,
                                                  @NotNull Node expression) {
        return visitScoped(ScopeType.SWITCH, () -> {
            Value<?> compared = expression.accept(this);
            boolean switched = false;
            for (CaseStatement caseStatement : cases) {
                TupleValue<Node, CodeBlock> cs = (TupleValue<Node, CodeBlock>) caseStatement.accept(this);
                Value<?> comparison = cs.getKey().accept(this);
                if (compared.equal(comparison).equals(BooleanValue.TRUE) || switched)
                    try {
                        switched = true;
                        Value<?> returned = visitScoped(ScopeType.CASE, () -> cs.getValue().accept(this));
                        if (!returned.is(Values.NO_VALUE)) return returned;
                    } catch (BreakException ignored) {
                        return Values.NO_VALUE;
                    }
            }

            return defaultBlock.accept(this);
        });
    }

    /**
     * Converts the case statement to a {@link TupleValue} containing
     * the expression as key and block as value.
     * This will be later used by {@link #visitSwitchStatement(List, CodeBlock, Node)}.
     *
     * @param block      the block
     * @param expression the expression
     * @return the case statement
     */
    @Override
    public @NotNull Value<?> visitCaseStatement(@NotNull CodeBlock block, @NotNull Node expression) {
        return new TupleValue<>(expression, block);
    }

    @Override
    public @NotNull Value<?> visitForStatement(@NotNull Node assignment, @NotNull Node increment,
                                               @NotNull CodeBlock code, @NotNull Node expression) {
        return visitScoped(ScopeType.FOR, () -> {
            for (assignment.accept(this); expression.accept(this).is(BooleanValue.TRUE); increment.accept(this)) {
                Optional<Value<?>> returnedValue = visitLoopCodeBlock(code);
                if (returnedValue.isPresent()) return returnedValue.get();
            }
            return Values.NO_VALUE;
        });
    }

    @Override
    public @NotNull Value<?> visitEnhancedForStatement(@NotNull Node type, @NotNull Node variable,
                                                       @NotNull CodeBlock code, @NotNull Node expression) {
        return visitScoped(ScopeType.FOR, () -> {
            ClassValue<?> variableType = type.accept(this).check(ClassValue.class);
            ValueLiteralVariableContainer<?> variableName = variable.accept(this).check(ValueLiteralVariableContainer.class);
            Value<?> iterable = expression.accept(this);

            final Iterator<?> iterator;
            if (iterable.is(ArrayValue.class))
                iterator = iterable.check(ArrayValue.class).getValues().stream().map(v -> ((Value<?>) v).getValue()).iterator();
            else iterator = ((Iterable<?>) iterable.getValue()).iterator();

            while (iterator.hasNext()) {
                Value<?> next = Value.of(iterator.next());
                try {
                    variableName.set(next);
                } catch (ExecutorException ignored) {
                    this.environment.declare(variableType, variableName.namedEntity(), next);
                }
                Optional<Value<?>> returnedValue = visitLoopCodeBlock(code);
                if (returnedValue.isPresent()) return returnedValue.get();
            }

            return Values.NO_VALUE;
        });
    }

    @Override
    public @NotNull Value<?> visitDoStatement(@NotNull CodeBlock code, @NotNull Node expression) {
        return visitScoped(ScopeType.DO, () -> {
            do {
                Optional<Value<?>> returnedValue = visitLoopCodeBlock(code);
                if (returnedValue.isPresent()) return returnedValue.get();
            } while (expression.accept(this).is(BooleanValue.TRUE));
            return Values.NO_VALUE;
        });
    }

    @Override
    public @NotNull Value<?> visitWhileStatement(@NotNull CodeBlock code, @NotNull Node expression) {
        return visitScoped(ScopeType.WHILE, () -> {
            while (expression.accept(this).is(BooleanValue.TRUE)) {
                Optional<Value<?>> returnedValue = visitLoopCodeBlock(code);
                if (returnedValue.isPresent()) return returnedValue.get();
            }
            return Values.NO_VALUE;
        });
    }

    @Override
    public @NotNull Value<?> visitIfStatement(@NotNull CodeBlock then, @NotNull Node elseBranch, @NotNull Node expression) {
        if (expression.accept(this).is(BooleanValue.TRUE)) return then.accept(this);
        else return elseBranch.accept(this);
    }

    @Override
    public @NotNull Value<?> convertVariable(@NotNull ClassValue<?> variableType, @NotNull Value<?> variable) {
        // Test for uninitialized
        if (variable.is(visitEmptyLiteral()))
            variable = variableType.isPrimitive() ? variableType.toObject() : visitNullLiteral();
        if (!variable.isPrimitive()) return variable;
        else if (variableType.is(PrimitiveClassValue.class)) return variableType.cast(variable);
        else if (!variableType.is(ObjectClassValue.OBJECT)) // Can only be ClassObjectValue at this point
            return variableType.cast(variable);
        return variable;
    }

    @Override
    public @NotNull Value<?> visitDynamicArray(@NotNull List<Node> parameters, @NotNull Node type) {
        ClassValue<Object> componentsType = type.accept(this).check(ArrayClassValue.class).getComponentsType();
        Collection<Value<Object>> components = new LinkedList<>();
        for (Node component : parameters) components.add((Value<Object>) component.accept(this));
        return ArrayValue.of(componentsType, components);
    }

    @Override
    public @NotNull Value<?> visitStaticArray(int size, @NotNull Node type) {
        ClassValue<?> componentsType = type.accept(this).check(ClassValue.class);
        return ArrayValue.of(componentsType, size);
    }

    @Override
    public @NotNull Value<?> visitArrayIndex(@NotNull Node array, @NotNull Node index) {
        VariableContainer<ClassValue<?>, Value<?>, ParameterValues, ?> container = array.accept(this).check(VariableContainer.class);
        ArrayValue<?> arrayValue = container.getVariable().check(ArrayValue.class);
        ClassValue<?> componentsType = arrayValue.getComponentsType();
        Integer value = (Integer) index.accept(this).getValue();
        return new ArrayValueVariableContainer<>(container, componentsType, value.toString(), componentsType.cast(arrayValue.get(value)));
    }

    @Override
    public @NotNull Value<?> visitArrayLiteral(@NotNull Node type) {
        ClassValue<?> classValue = type.accept(this).check(ClassValue.class);
        return ArrayClassValue.of(classValue);
    }

    @Override
    public @NotNull ParameterValues visitMethodInvocation(@NotNull List<Node> parameters) {
        List<Value<?>> parameterValues = new LinkedList<>();
        for (Node parameter : parameters)
            parameterValues.add(parameter.accept(this));
        return new ParameterValues(parameterValues);
    }

    @Override
    public @NotNull Value<?> visitTernaryOperator(@NotNull Node expression, @NotNull Node first, @NotNull Node second) {
        if (expression.accept(this).is(BooleanValue.TRUE)) return first.accept(this);
        else return second.accept(this);
    }

    @Override
    public @NotNull Value<?> visitNullLiteral() {
        return Values.NULL_VALUE;
    }

    @Override
    public @NotNull Value<?> visitThisLiteral() {
        return ObjectValue.of(getExecutingObject());
    }

    @Override
    public @NotNull Value<?> visitCharValueLiteral(@NotNull String rawValue) {
        return PrimitiveValue.of(rawValue.charAt(0));
    }

    @Override
    public @NotNull Value<?> visitNumberValueLiteral(@NotNull String rawValue) {
        return PrimitiveValue.of(Integer.parseInt(rawValue));
    }

    @Override
    public @NotNull Value<?> visitLongValueLiteral(@NotNull String rawValue) {
        return PrimitiveValue.of(Long.parseLong(rawValue));
    }

    @Override
    public @NotNull Value<?> visitDoubleValueLiteral(@NotNull String rawValue) {
        return PrimitiveValue.of(Double.valueOf(rawValue));
    }

    @Override
    public @NotNull Value<?> visitFloatValueLiteral(@NotNull String rawValue) {
        return PrimitiveValue.of(Float.parseFloat(rawValue));
    }

    @Override
    public @NotNull Value<?> visitBooleanValueLiteral(@NotNull String rawValue) {
        return PrimitiveValue.of(Boolean.parseBoolean(rawValue));
    }

    @Override
    public @NotNull Value<?> visitStringValueLiteral(@NotNull String rawValue) {
        return ObjectValue.of(rawValue);
    }

    @Override
    public @NotNull Value<?> visitGenericsLiteral(@NotNull List<Literal> types, @NotNull String value) {
        return visitLiteralImpl(value);
    }

    @Override
    public @NotNull Tuple<ClassValue<?>, Value<?>> getObjectFromLiteral(@NotNull String literal) {
        try {
            Tuple<ClassValue<?>, Value<?>> tuple = new Tuple<>();
            if (literal.endsWith(".class")) {
                ClassValue<?> type = ClassValue.of(literal.substring(0, literal.length() - 6));
                Value<?> actualValue = ObjectValue.of(type.getValue());
                tuple.set(actualValue.toClass(), actualValue);
            } else {
                ClassValue<?> type = ClassValue.of(literal);
                tuple.set(type, type);
            }
            return tuple;
        } catch (ValueException e) {
            return Visitor.super.getObjectFromLiteral(literal);
        }
    }

    @Override
    public @NotNull ValueLiteralVariableContainer<?> newLiteralObject(@NotNull String value) {
        return new ValueLiteralVariableContainer<>(this.environment, value);
    }

    @Override
    public @NotNull Value<?> visitEmptyLiteral() {
        return Values.NO_VALUE;
    }

    /**
     * Support method for many break and continue supported statements.
     *
     * @param code the code
     * @return null in case nothing was returned, {@link Values#NO_VALUE} in case a {@link #visitBreak(Node)} occurred, otherwise the actual returned type of the codeblock
     */
    @NotNull Optional<Value<?>> visitLoopCodeBlock(final @NotNull CodeBlock code) {
        try {
            Value<?> returnedValue = code.accept(this);
            // Return occurred
            if (!returnedValue.is(Values.NO_VALUE)) return Optional.of(returnedValue);
        } catch (BreakException ignored) {
            return Optional.of(Values.NO_VALUE);
        } catch (ContinueException ignored) {
        }
        return Optional.empty();
    }

    @Override
    public @NotNull RuntimeException exceptionWrapper(@NotNull Exception exception) {
        return ExecutorException.of(exception);
    }

    @Override
    public @NotNull RuntimeException cannotResolveSymbol(@NotNull String symbol) {
        return ExecutorException.cannotResolveSymbol(symbol);
    }

}
