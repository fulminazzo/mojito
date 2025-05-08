package it.fulminazzo.mojito.visitors.visitorobjects

import it.fulminazzo.mojito.handler.elements.ClassElement
import spock.lang.Specification

class MethodContainerTest extends Specification {
    private ClassElement a
    private ClassElement b
    private ClassElement c
    private GenericsContainer<ClassElement> type

    void setup() {
        this.a = ClassElement.of(Double)
        this.b = ClassElement.of(Float)
        this.c = ClassElement.of(Boolean)
        this.type = new GenericsContainer<ClassElement>() {

            @Override
            Map<String, ClassElement> getGenericTypes() {
                return [
                        'A': this.a,
                        'B': this.b,
                        'C': this.c
                ]
            }

        }

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
