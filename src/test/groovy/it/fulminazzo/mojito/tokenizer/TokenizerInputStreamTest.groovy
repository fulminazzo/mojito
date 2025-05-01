package it.fulminazzo.mojito.tokenizer

import spock.lang.Specification

class TokenizerInputStreamTest extends Specification {
    private TokenizerInputStream stream

    void setup() {
        this.stream = new TokenizerInputStream(new ByteArrayInputStream('hello'.bytes))
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

}
