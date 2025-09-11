package it.fulminazzo.mojito.visitors.visitorobjects;

import it.fulminazzo.mojito.visitors.visitorobjects.executables.ExecutableContainer;

/**
 * This exception is thrown by {@link VisitorObject#invokeMethod(ExecutableContainer, ParameterVisitorObjects)}
 * in case the currently found method is not correct.
 */
public final class IncorrectMethodException extends VisitorObjectException {

    /**
     * Instantiates a new Incorrect method exception.
     */
    public IncorrectMethodException() {
        super("");
    }

}
