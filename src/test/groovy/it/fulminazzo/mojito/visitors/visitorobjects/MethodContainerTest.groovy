package it.fulminazzo.mojito.visitors.visitorobjects

import it.fulminazzo.mojito.handler.elements.ClassElement
import spock.lang.Specification

class MethodContainerTest extends Specification {
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
        def container = MethodContainer.of(
                GenericClass.getMethod('returnType'),
                this.type
        )

        expect:
        container.returnType == a.toJavaClass()
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

    }

}
