package it.fulminazzo.javaparser.executor.values.objects

import spock.lang.Specification

class StringObjectValueTest extends Specification {

    def 'test to class value'() {
        given:
        def string = new StringObjectValue('Hello, world!')

        when:
        def classValue = string.toClassValue()

        then:
        classValue == ObjectClassValue.STRING
    }

    def 'test add'() {
        given:
        def first = new StringObjectValue('Hello, ')
        def second = new StringObjectValue('world!')

        when:
        def result = first.add(second)

        then:
        result.getValue() == 'Hello, world!'
    }

}