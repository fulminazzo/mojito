package it.fulminazzo.mojito;

import it.fulminazzo.fulmicollection.objects.Refl;
import it.fulminazzo.mojito.exceptions.FormatException;
import it.fulminazzo.mojito.executor.Executor;
import it.fulminazzo.mojito.executor.ExecutorException;
import it.fulminazzo.mojito.parser.JavaParser;
import it.fulminazzo.mojito.parser.ParserException;
import it.fulminazzo.mojito.parser.node.Node;
import it.fulminazzo.mojito.parser.node.NodeException;
import it.fulminazzo.mojito.parser.node.literals.Literal;
import it.fulminazzo.mojito.typechecker.TypeChecker;
import it.fulminazzo.mojito.typechecker.TypeCheckerException;
import it.fulminazzo.mojito.utils.TimeUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Supplier;

/**
 * Starting point of the program.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Mojito {
    private static final String[] HELP_OPTIONS = new String[]{"-h", "--help", "/?"};
    
    private static void info(final @NotNull Object message) {
        final String format = "[%s] - %s%n";
        final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        System.out.printf(format, dateFormat.format(new Date()), message);
    }

    /**
     * The main function of the program.
     *
     * @param args the command line arguments
     */
    @SuppressWarnings("unchecked")
    public static void main(final String @NotNull [] args) {
        try {
            if (args.length == 0) throw new ArgumentsException();
            String argument = args[0];

            for (String o : HELP_OPTIONS)
                if (argument.equalsIgnoreCase(o))
                    throw new ArgumentsException();

            Runner runner = newRunner();

            final Object code;
            final int start;
            if (argument.equalsIgnoreCase("--code")) {
                if (args.length == 1) throw new ArgumentsException();
                code = args[1];
                start = 2;
            } else {
                code = new File(argument);
                start = 1;
            }

            final Map<String, Object> variables;
            try {
                variables = executeTimed(
                        "Beginning reading of variables from command line...",
                        "Finished parsing of variables. (%time%)",
                        () -> parseVariables(args, start));
            } catch (RunnerException e) {
                info("An error occurred while parsing variables from command line.");
                info(e.getMessage());
                return;
            }

            try {
                executeTimed(
                        String.format("Starting program with %s environment variables.", variables.size()),
                        "Successfully terminated program execution. (%time%)",
                        () -> {
                            if (code instanceof File) runner.run((File) code, variables);
                            else runner.run((String) code, variables);
                            return null;
                        }
                );
            } catch (RunnerException | ParserException | TypeCheckerException | ExecutorException e) {
                info("An error occurred while running the program.");
                info(e.getMessage());
                return;
            }

            info("Program execution returned:");
            Optional<?> result = runner.latestResult();
            System.out.println(result.isPresent() ? result.get() : "Nothing was returned");
        } catch (ArgumentsException e) {
            System.out.println("Usage:");
            System.out.println("java -jar mojito.jar <filename> <var1:val1> <var2:val2>...");
            System.out.println("java -jar mojito.jar --code \"code to run\" <var1:val1> <var2:val2>...");
        }
    }

    /**
     * Executes the given function and computes the time that it took to run.
     *
     * @param <T>          the type returned by the function
     * @param startMessage the message to send before execution
     * @param endMessage   the message to send after execution
     * @param function     the function executed
     * @return the result of the function
     */
    static <T> T executeTimed(final @NotNull String startMessage, final @NotNull String endMessage, final @NotNull Supplier<T> function) {
        long start = System.currentTimeMillis();
        info(startMessage);
        T result = function.get();
        long end = System.currentTimeMillis();
        info(endMessage.replace("%time%", TimeUtils.formatTime(end - start)));
        return result;
    }

    /**
     * Uses {@link Runner} to read the given array expecting a <code>key:value</code>
     * pair for each element.
     *
     * @param arguments the array
     * @param start     the index to start from
     * @return a map with the parsed variables
     */
    static @NotNull Map<String, Object> parseVariables(final String @NotNull [] arguments, int start) {
        Map<String, Object> variables = new HashMap<>();

        for (int i = start; i < arguments.length; i++) {
            String argument = arguments[i];
            try {
                if (argument.contains(":")) {
                    String[] parts = argument.split(":");
                    String name = Literal.of(parts[0]).getLiteral();
                    String value = String.join(":", Arrays.copyOfRange(parts, 1, parts.length));

                    try {
                        variables.put(name, parseExpression(value));
                    } catch (ParserException e) {
                        throw new ArgumentsException("No value was returned from key");
                    }
                } else throw new ArgumentsException("Expected 'key:value' pair");
            } catch (ArgumentsException | NodeException e) {
                throw new RunnerException("Error for variable '%s' at index %s: %s", argument, i, e.getMessage());
            }
        }

        return variables;
    }

    /**
     * Reads a simple Java expression and converts it to a value.
     *
     * @param expression the expression
     * @return the object returned
     */
    static @Nullable Object parseExpression(final @NotNull String expression) {
        final Object executingObject = new Object();

        JavaParser parser = new JavaParser();
        parser.setInput(expression);
        Node parsed = new Refl<>(parser).callMethod("nextSpaceless").invokeMethod("parseExpression");

        TypeChecker checker = new TypeChecker(executingObject);
        parsed.accept(checker);

        Executor executor = new Executor(executingObject);
        return parsed.accept(executor).getValue();
    }

    /**
     * Creates a new {@link Runner} with {@link Object} as executor.
     *
     * @return the runner
     */
    public static @NotNull Runner newRunner() {
        return newRunner(new Object());
    }

    /**
     * Creates a new {@link Runner} with the given object as executor.
     *
     * @param executor the executing object
     * @return the runner
     */
    public static @NotNull Runner newRunner(final @NotNull Object executor) {
        return new MojitoRunner(executor);
    }

    /**
     * A helper exception for many functions of this class.
     */
    private static class ArgumentsException extends FormatException {

        /**
         * Instantiates a new Arguments exception.
         */
        public ArgumentsException() {
            super("");
        }

        /**
         * Instantiates a new Arguments exception.
         *
         * @param message the message
         * @param args    the arguments to add in the message format
         */
        public ArgumentsException(final @NotNull String message, final Object @NotNull ... args) {
            super(message, args);
        }

    }

}
