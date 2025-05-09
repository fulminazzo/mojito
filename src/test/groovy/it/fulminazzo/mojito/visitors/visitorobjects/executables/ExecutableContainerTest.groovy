package it.fulminazzo.mojito.visitors.visitorobjects.executables

import it.fulminazzo.mojito.handler.elements.ClassElement
import it.fulminazzo.mojito.visitors.visitorobjects.GenericsContainer
import spock.lang.Specification

import java.lang.reflect.Array

class ExecutableContainerTest extends Specification {
    private static final ClassElement a = ClassElement.of(Double)
    private static final ClassElement b = ClassElement.of(Float)
    private static final ClassElement c = ClassElement.of(Boolean)
    private GenericsContainer<ClassElement> type

    void setup() {
        this.type = new GenericsContainer<ClassElement>() {

            @Override
            Map<String, ClassElement> getGenericTypes() {
                return [
                        'A': a,
                        'B': b,
                        'C': c
                ]
            }

        }

    }

    def 'test that return type of parameterized method is correct'() {
        given:
        def container = ExecutableContainer.of(
                GenericClass.getMethod('returnType'),
                this.type
        )

        expect:
        container.returnType == a.toJavaClass()
    }

    def 'test that parameters of method parameters(#parameters) are #expected'() {
        given:
        def container = ExecutableContainer.of(
                GenericClass.getMethod('parameters', parameters.toArray(new Class[0])),
                this.type
        )

        expect:
        container.parameterTypes.toList() == expected

        where:
        parameters                  | expected
        [Object]                    | [b.toJavaClass()]
        [Object, String]            | [b.toJavaClass(), String]
        [Integer, Object, String]   | [Integer, b.toJavaClass(), String]
        [Integer, Object, Object[]] | [Integer, b.toJavaClass(), Array.newInstance(c.toJavaClass(), 0).getClass()]
    }

    def 'test that return type and parameters of parameterized method are correct'() {
        given:
        def container = ExecutableContainer.of(
                GenericClass.getMethod('full', Object, Object),
                this.type
        )

        expect:
        container.parameterTypes.toList() == [b.toJavaClass(), c.toJavaClass()]
        container.returnType == a.toJavaClass()
    }

    def 'test that method container lookup does not override methods generic types'() {
        given:
        def container = ExecutableContainer.of(
                GenericClass.getMethod('dubious', Object, Byte),
                this.type
        )

        expect:
        container.parameterTypes.toList() == [b.toJavaClass(), Byte]
        container.returnType == Character
    }

    @SuppressWarnings('unused')
    static class GenericClass<A, B, C> {

        A returnType() {
            return null
        }

        void parameters(B b) {

        }

        void parameters(B b, String s) {

        }

        void parameters(Integer i, B b, String s) {

        }

        void parameters(Integer i, B b, C... c) {

        }

        A full(B b, C c) {
            return null
        }

        <A extends Character, C extends Byte> A dubious(B b, C c) {
            return null
        }

    }

}
