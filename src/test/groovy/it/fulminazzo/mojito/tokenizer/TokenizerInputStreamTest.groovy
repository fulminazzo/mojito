package it.fulminazzo.mojito.tokenizer

import spock.lang.Specification

class TokenizerInputStreamTest extends Specification {
    private MockInputStream actualStream
    private TokenizerInputStream stream

    void setup() {
        this.actualStream = new MockInputStream('hello')
        this.stream = new TokenizerInputStream(this.actualStream)
    }

    def 'test that tokenizer input stream works as a stream'() {
        when:
        def read = this.stream.read()

        then:
        read == 'h'.charAt(0)
    }

    def 'test that tokenizer input stream prioritizes push over stream'() {
        given:
        this.stream.push(data)

        when:
        def read = this.stream.read()

        then:
        read == expected

        where:
        data     || expected
        (byte) 1 || 1
        'world'  || 'w'.charAt(0)
    }

    def 'test that tokenizer input stream available returns correct value'() {
        when:
        def available = this.stream.available()

        then:
        available == this.actualStream.available()
    }

    def 'test that tokenizer input stream close actually closes the stream'() {
        given:
        this.actualStream.close()

        when:
        this.actualStream.read()

        then:
        thrown(IOException)
    }

    def 'test that tokenizer input stream mark actually marks'() {
        when:
        this.stream.mark(2)

        then:
        this.actualStream.mark == 2
    }

    def 'test that tokenizer input stream reset actually resets'() {
        given:
        this.actualStream.mark(6)

        when:
        this.stream.reset()

        then:
        this.actualStream.mark == 0
    }

    def 'test that tokenizer input stream markSupported returns correct value'() {
        expect:
        this.stream.markSupported()
    }

}
