package it.fulminazzo.mojito.tokenizer

import spock.lang.Specification

class TokenizerInputStreamTest extends Specification {

    def 'test that tokenizer input stream works as a stream'() {
        given:
        def stream = new TokenizerInputStream(new ByteArrayInputStream('hello'.bytes))

        when:
        def read = stream.read()

        then:
        read == 'h'.charAt(0)
    }

    def 'test that tokenizer input stream prioritizes push over stream'() {
        given:
        def stream = new TokenizerInputStream(new ByteArrayInputStream('hello'.bytes))

        when:
        stream.push(data)
        def read = stream.read()

        then:
        read == expected

        where:
        data     || expected
        (byte) 1 || 1
        'world'  || 'w'.charAt(0)
    }

}
