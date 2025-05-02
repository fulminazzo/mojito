package it.fulminazzo.mojito.handler;

import it.fulminazzo.fulmicollection.structures.tuples.Tuple;
import it.fulminazzo.fulmicollection.utils.ReflectionUtils;
import it.fulminazzo.mojito.environment.MockEnvironment;
import it.fulminazzo.mojito.handler.elements.ClassElement;
import it.fulminazzo.mojito.handler.elements.Element;
import it.fulminazzo.mojito.handler.elements.ParameterElements;
import it.fulminazzo.mojito.handler.elements.variables.ElementLiteralVariableContainer;
import it.fulminazzo.mojito.parser.node.Node;
import it.fulminazzo.mojito.parser.node.container.CodeBlock;
import it.fulminazzo.mojito.parser.node.literals.Literal;
import it.fulminazzo.mojito.parser.node.statements.CaseStatement;
import it.fulminazzo.mojito.parser.node.statements.CatchStatement;
import it.fulminazzo.mojito.visitors.Visitor;
import it.fulminazzo.mojito.visitors.visitorobjects.variables.LiteralVariableContainer;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class Handler implements Visitor<ClassElement, Element, ParameterElements> {
    private final Object executingObject;
    private final MockEnvironment<Element> environment;

    public Handler(Object executingObject) {
        this.executingObject = executingObject;
        this.environment = new MockEnvironment<>();
    }

    public Element visitMockNode(String name, int version) {
        return Element.of(name + version);
    }

    @Override
    public @NotNull Element visitThrow(@NotNull Node expression) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Element visitBreak(@NotNull Node expression) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Element visitContinue(@NotNull Node expression) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Element visitTryStatement(@NotNull CodeBlock block, @NotNull List<CatchStatement> catchBlocks,
                                              @NotNull CodeBlock finallyBlock, @NotNull Node expression) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Element visitCatchStatement(@NotNull List<Literal> exceptions, @NotNull CodeBlock block,
                                                @NotNull Node expression) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Element visitSwitchStatement(@NotNull List<CaseStatement> cases, @NotNull CodeBlock defaultBlock,
                                                 @NotNull Node expression) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Element visitCaseStatement(@NotNull CodeBlock block, @NotNull Node expression) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Element visitForStatement(@NotNull Node assignment, @NotNull Node increment,
                                              @NotNull CodeBlock code, @NotNull Node expression) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Element visitEnhancedForStatement(@NotNull Node type, @NotNull Node variable,
                                                      @NotNull CodeBlock code, @NotNull Node expression) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Element visitDoStatement(@NotNull CodeBlock code, @NotNull Node expression) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Element visitWhileStatement(@NotNull CodeBlock code, @NotNull Node expression) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Element visitIfStatement(@NotNull CodeBlock then, @NotNull Node elseBranch, @NotNull Node expression) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Element convertVariable(@NotNull ClassElement variableType, @NotNull Element variable) {
        // Cast disabled for re-assignment tests
        return variable;
    }

    @Override
    public @NotNull Element visitDynamicArray(@NotNull List<Node> parameters, @NotNull Node type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Element visitStaticArray(int size, @NotNull Node type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Element visitArrayIndex(@NotNull Node array, @NotNull Node index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Element visitArrayLiteral(@NotNull Node type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull ParameterElements visitMethodInvocation(@NotNull List<Node> parameters) {
        return new ParameterElements(parameters.stream().map(n -> n.accept(this)).collect(Collectors.toList()));
    }

    @Override
    public @NotNull Element visitTernaryOperator(@NotNull Node expression, @NotNull Node first, @NotNull Node second) {
        return Element.of(new Object());
    }

    @Override
    public @NotNull Element visitNullLiteral() {
        return Element.of(null);
    }

    @Override
    public @NotNull Element visitThisLiteral() {
        return Element.of(this.executingObject);
    }

    @Override
    public @NotNull Element visitCharValueLiteral(@NotNull String rawValue) {
        return Element.of(rawValue.charAt(0));
    }

    @Override
    public @NotNull Element visitNumberValueLiteral(@NotNull String rawValue) {
        return Element.of(Integer.valueOf(rawValue));
    }

    @Override
    public @NotNull Element visitLongValueLiteral(@NotNull String rawValue) {
        return Element.of(Long.valueOf(rawValue));
    }

    @Override
    public @NotNull Element visitDoubleValueLiteral(@NotNull String rawValue) {
        return Element.of(Double.valueOf(rawValue));
    }

    @Override
    public @NotNull Element visitFloatValueLiteral(@NotNull String rawValue) {
        return Element.of(Float.valueOf(rawValue));
    }

    @Override
    public @NotNull Element visitBooleanValueLiteral(@NotNull String rawValue) {
        return Element.of(Boolean.valueOf(rawValue));
    }

    @Override
    public @NotNull Element visitStringValueLiteral(@NotNull String rawValue) {
        return Element.of(rawValue);
    }

    @Override
    public @NotNull LiteralVariableContainer<ClassElement, Element, ParameterElements> newLiteralObject(@NotNull String value) {
        return new ElementLiteralVariableContainer(this.environment, ClassElement.of(null), value, visitNullLiteral());
    }

    @Override
    public @NotNull Element visitEmptyLiteral() {
        return Element.EMPTY;
    }

    @Override
    public @NotNull Tuple<ClassElement, Element> getObjectFromLiteral(@NotNull String literal) {
        try {
            Tuple<ClassElement, Element> tuple = new Tuple<>();
            if (literal.endsWith(".class")) {
                ClassElement element = ClassElement.of(getClass(literal.substring(0, literal.length() - 6)));
                tuple.set(element.toClass(), element.toClass());
            } else {
                ClassElement element = ClassElement.of(getClass(literal));
                tuple.set(element, element);
            }
            return tuple;
        } catch (IllegalArgumentException e) {
            return Visitor.super.getObjectFromLiteral(literal);
        }
    }

    private @NotNull Class<?> getClass(@NotNull String literal) {
        try {
            return ReflectionUtils.getClass(literal);
        } catch (IllegalArgumentException e) {
            if (!literal.contains(".")) {
                for (String p : Arrays.asList(
                        String.class.getPackage().getName(),
                        HashMap.class.getPackage().getName(),
                        IOException.class.getPackage().getName()
                ))
                    try {
                        return ReflectionUtils.getClass(p + "." + literal);
                    } catch (IllegalArgumentException ignored) {
                    }
            }
            throw new IllegalArgumentException("Could not find class: " + literal);
        }
    }

    @Override
    public @NotNull RuntimeException exceptionWrapper(@NotNull Exception exception) {
        return new HandlerException(exception);
    }

    @Override
    public @NotNull RuntimeException cannotResolveSymbol(@NotNull String symbol) {
        return new HandlerException("Cannot resolve symbol '%s'", symbol);
    }

}
