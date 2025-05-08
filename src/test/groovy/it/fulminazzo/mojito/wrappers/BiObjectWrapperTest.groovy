package it.fulminazzo.mojito.wrappers

import spock.lang.Specification

class BiObjectWrapperTest extends Specification {
    private String name
    private int age
    private BiObjectWrapper wrapper

    void setup() {
        this.name = 'Alex'
        this.age = 22
        this.wrapper = new MockWrapper(this.name, this.age)
    }

    def 'test hashCode'() {
        given:
        int code = Objects.hash(MockWrapper, this.name, this.age)

        expect:
        this.wrapper.hashCode() == code
    }

    def 'test equality'() {
        expect:
        this.wrapper == new MockWrapper(this.name, this.age)
    }

    def 'test inequality with #object'() {
        expect:
        !this.wrapper.equals(object)

        where:
        object << [
                null,
                new BiObjectWrapper<>(2.0, 10),
                new MockWrapper('Steve', 22),
                new MockWrapper('Alex', 23),
                new MockWrapper('Steve', 21),
        ]
    }

    def 'test toString'() {
        expect:
        this.wrapper.toString() == "${MockWrapper.simpleName}(${this.name}, ${this.age})"
    }

    static class MockWrapper extends BiObjectWrapper<String, Integer> {

        MockWrapper(String first, Integer second) {
            super(first, second)
        }

    }

}
