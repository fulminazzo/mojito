package it.fulminazzo.mojito;

import it.fulminazzo.mojito.environment.NamedEntity;
import it.fulminazzo.mojito.environment.ScopeException;
import it.fulminazzo.mojito.executor.ExceptionWrapper;
import it.fulminazzo.mojito.executor.Executor;
import it.fulminazzo.mojito.executor.values.ClassValue;
import it.fulminazzo.mojito.executor.values.Value;
import it.fulminazzo.mojito.executor.values.Values;
import it.fulminazzo.mojito.parser.JavaParser;
import it.fulminazzo.mojito.parser.node.container.JavaProgram;
import it.fulminazzo.mojito.typechecker.TypeChecker;
import it.fulminazzo.mojito.typechecker.types.ClassType;
import it.fulminazzo.mojito.typechecker.types.Types;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

/**
 * An implementation of {@link Runner} for Mojito.
 */
final class MojitoRunner implements Runner {
    private final @NotNull Object executingObject;
    private @Nullable Object latestResult;

    /**
     * Instantiates a new Mojito runner.
     *
     * @param executingObject the executing object
     */
    public MojitoRunner(final @NotNull Object executingObject) {
        this.executingObject = executingObject;
        this.latestResult = Optional.empty();
    }

    @Override
    public @NotNull Optional<?> latestResult() {
        return Optional.ofNullable(this.latestResult);
    }

    @Override
    public @NotNull Optional<?> run(final @NotNull InputStream input, final @NotNull Map<String, Object> variables) {
        final JavaParser parser = new JavaParser();
        final TypeChecker typeChecker = new TypeChecker(this.executingObject);
        final Executor executor = new Executor(this.executingObject);

        for (final String k : variables.keySet())
            try {
                final Object v = variables.get(k);
                final Class<?> vClass = v == null ? null : v.getClass();

                NamedEntity name = NamedEntity.of(k);

                ClassType classType = vClass == null ? (ClassType) Types.NULL_TYPE : ClassType.of(vClass);
                typeChecker.getEnvironment().declare(classType, name, classType.toType());

                Value<?> value = Value.of(v);
                ClassValue<?> classValue = v == null ? (ClassValue<?>) Values.NULL_VALUE : value.toClass();
                executor.getEnvironment().declare(classValue, name, value);
            } catch (ScopeException ignored) {
                // Cannot happen
            }

        parser.setInput(input);
        JavaProgram parsed = parser.parseProgram();
        typeChecker.visitProgram(parsed);

        try {
            this.latestResult = executor.visitProgram(parsed).map(Value::getValue).orElse(null);
            return latestResult();
        } catch (ExceptionWrapper e) {
            throw RunnerException.of(e.getActualException().getValue());
        }
    }

}
