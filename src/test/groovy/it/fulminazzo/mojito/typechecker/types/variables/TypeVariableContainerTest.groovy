package it.fulminazzo.mojito.typechecker.types.variables

import it.fulminazzo.fulmicollection.structures.tuples.Tuple
import it.fulminazzo.mojito.TestClass
import it.fulminazzo.mojito.typechecker.types.*
import it.fulminazzo.mojito.visitors.visitorobjects.MethodContainer
import org.jetbrains.annotations.NotNull
import spock.lang.Specification

import java.lang.reflect.Field
import java.lang.reflect.Modifier

class TypeVariableContainerTest extends Specification {

    static newContainer(def variable) {
        return new TypeVariableContainer() {

            @Override
            @NotNull Type getVariable() {
                return variable
            }

            @Override
            ClassType toClass() {
                return null
            }

        }

    }

    static containersGenerator() {
        return [
                { v -> newContainer(v) },
                { v -> new ArrayTypeVariableContainer(null, PrimitiveClassType.INT, '0', v) },
                { v -> new TypeFieldContainer(null, PrimitiveClassType.INT, 'publicField', v) },
                { v -> new TypeLiteralVariableContainer(null, PrimitiveClassType.INT, 'i', v) },
        ]
    }

    def 'test container.#tuple.value.name(#tuple.value.parameterTypes) should call variable.#tuple.value.name(#tuple.value.parameterTypes)'() {
        given:
        def containerGenerator = tuple.key
        def method = tuple.value
        def methodName = method.name

        and:
        def variable = Mock(Type)
        def container = containerGenerator(variable)

        and:
        def parameters = method.parameterTypes.collect {
            switch (it) {
                case Type[] -> new Type[]{PrimitiveType.INT}
                case ClassType -> PrimitiveClassType.INT
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
        tuple << TypeVariableContainer.methods
                .findAll { it.declaringClass == TypeVariableContainer }
                .findAll { !Modifier.isAbstract(it.modifiers) }
                .findAll { it.name != 'invokeMethod' }
                .collectMany { containersGenerator().collect { c -> new Tuple<>(c, it) } }
    }

    def 'test container invokeMethod should be called to internal variable'() {
        given:
        def variable = Mock(Type)
        def container = generateContainer(variable)

        and:
        def method = MethodContainer.of(TestClass.getMethod('publicMethod'))
        def types = new ParameterTypes([])

        when:
        container.invokeMethod(method, types)

        then:
        1 * variable.invokeMethod(method, types)

        where:
        generateContainer << containersGenerator()
    }

}
