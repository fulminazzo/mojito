package it.fulminazzo.mojito.visitors.visitorobjects

import it.fulminazzo.mojito.handler.elements.ClassElement
import spock.lang.Specification

import java.util.function.Consumer
import java.util.function.Function

class ClassVisitorObjectTest extends Specification {

    def 'test isFunctionalInterface should return #expected for #clazz'() {
        when:
        def element = ClassElement.of(clazz)

        then:
        element.functionalInterface == expected

        where:
        clazz    || expected
        Runnable || true
        Consumer || true
        Function || true
        Object   || false
    }

    def 'test class visitor object should only be compatible with a visitor object instance'() {
        expect:
        !ClassElement.of(String).compatibleWith(null)
    }

}
