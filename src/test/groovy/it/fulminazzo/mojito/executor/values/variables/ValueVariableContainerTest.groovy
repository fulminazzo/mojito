package it.fulminazzo.mojito.executor.values.variables

import it.fulminazzo.fulmicollection.structures.tuples.Tuple
import it.fulminazzo.mojito.executor.values.ClassValue
import it.fulminazzo.mojito.executor.values.ParameterValues
import it.fulminazzo.mojito.TestClass
import it.fulminazzo.mojito.executor.values.Value
import it.fulminazzo.mojito.visitors.visitorobjects.executables.ExecutableContainer
import org.jetbrains.annotations.NotNull
import spock.lang.Specification

import java.lang.reflect.Field
import java.lang.reflect.Modifier

class ValueVariableContainerTest extends Specification {

    static newContainer(def variable) {
        return new ValueVariableContainer<?>() {

            @NotNull
            @Override
            Value<?> getVariable() {
                return variable
            }

            @Override
            ClassValue<?> toClass() {
                return null
            }

        }

    }

    static containersGenerator() {
        return [
                { v -> newContainer(v) },
                { v -> new ArrayValueVariableContainer<>(null, ClassValue.of(Integer), '0', v) },
                { v -> new ValueFieldContainer<>(null, ClassValue.of(Integer), 'publicField', v) },
                { v -> new ValueLiteralVariableContainer<>(null, ClassValue.of(Integer), 'i', v) },
        ]
    }

    def 'test container.#tuple.value.name(#tuple.value.parameterTypes) should call variable.#tuple.value.name(#tuple.value.parameterTypes)'() {
        given:
        def containerGenerator = tuple.key
        def method = tuple.value
        def methodName = method.name

        and:
        def variable = Mock(Value)
        def container = containerGenerator(variable)

        and:
        def parameters = method.parameterTypes.collect {
            switch (it) {
                case Field -> TestClass.getField('publicField')
                default -> throw new IllegalArgumentException(it.canonicalName)
            }
        }

        when:
        if (parameters.size() == 0) container."${methodName}"()
        else container."${methodName}"(*parameters)

        then:
        if (parameters.size() == 0) 1 * variable."${methodName}"()
        else 1 * variable."${methodName}"(*parameters)

        where:
        tuple << ValueVariableContainer.methods
                .findAll { it.declaringClass == ValueVariableContainer }
                .findAll { !Modifier.isAbstract(it.modifiers) }
                .findAll { it.name != 'invokeMethod' }
                .collectMany { containersGenerator().collect { c -> new Tuple<>(c, it) } }
    }

    def 'test container invokeMethod should be called to internal variable'() {
        given:
        def variable = Mock(Value)
        def container = generateContainer(variable)

        and:
        def method = ExecutableContainer.of(TestClass.getMethod('publicMethod'))
        def values = new ParameterValues([])

        when:
        container.invokeMethod(method, values)

        then:
        1 * variable.invokeMethod(method, values)

        where:
        generateContainer << containersGenerator()
    }

}
