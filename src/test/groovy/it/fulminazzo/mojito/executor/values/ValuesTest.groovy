package it.fulminazzo.mojito.executor.values

import it.fulminazzo.mojito.executor.ExecutorException
import it.fulminazzo.mojito.executor.values.objects.ObjectClassValue
import it.fulminazzo.mojito.executor.values.objects.ObjectValue
import it.fulminazzo.mojito.executor.values.primitivevalue.PrimitiveValue
import spock.lang.Specification

class ValuesTest extends Specification {
    private ClassValue<?> value

    void setup() {
        this.value = new Values.SingletonValue('TEST_VALUE')
    }

    def 'test null value should be compatible with class value #classValue'() {
        given:
        def value = Values.NULL_VALUE

        expect:
        value.isAssignableFrom(classValue)

        where:
        classValue << ObjectClassValue.values()
    }

    def 'test null value should not be compatible with class value #classValue'() {
        given:
        def value = Values.NULL_VALUE

        expect:
        !value.isAssignableFrom(classValue)

        where:
        classValue << PrimitiveClassValue.values()
    }

    def 'test SingletonValue compatibleWith #other should be #expected'() {
        when:
        def actual = this.value.compatibleWith(other)

        then:
        actual == expected

        where:
        other                                   | expected
        new Values.SingletonValue('TEST_VALUE') | true
        Values.NULL_VALUE                       | false
        null                                    | false
        PrimitiveValue.of(false)                | false
        ObjectValue.of(false)                   | false
    }

    def 'test SingletonValue toClass'() {
        when:
        this.value.toClass()

        then:
        def e = thrown(ExecutorException)
        e.message == ExecutorException.noClassValue(Values.SingletonValue).message
    }

    def 'test SingletonValue hashCode'() {
        given:
        def code = this.value.hashCode()
        int expected = Objects.hash(Values.SingletonValue, 'TEST_VALUE')

        expect:
        code == expected
    }

    def 'test SingletonValue equality'() {
        given:
        def second = new Values.SingletonValue('TEST_VALUE')

        expect:
        this.value == second
    }

    def 'test SingletonValue should not be equal to #other'() {
        expect:
        !this.value.equals(other)

        where:
        other << [PrimitiveClassValue.BOOLEAN, ObjectValue.of('Hello, world!'), null,
                  new Values.SingletonValue('MOCK'), 'TEST_VALUE']
    }

    def 'test SingletonValue toString'() {
        given:
        def string = this.value.toString()

        expect:
        string == 'TEST_VALUE'
    }

}
