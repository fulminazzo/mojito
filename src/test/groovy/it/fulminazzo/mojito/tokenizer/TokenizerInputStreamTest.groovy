package it.fulminazzo.mojito.tokenizer

import spock.lang.Specification

class TokenizerInputStreamTest extends Specification {
    private MockInputStream actualStream
    private TokenizerInputStream stream

    void setup() {
        this.actualStream = new MockInputStream('hello')
        this.stream = new TokenizerInputStream(this.actualStream)
    }

    def 'test that tokenizer input stream read single works'() {
        when:
        def read = this.stream.read()

        then:
        read == 'h'.charAt(0)
    }

    def 'test that tokenizer input stream read single buffer works'() {
        given:
        this.stream.push((byte) 1)

        when:
        def read = this.stream.read()

        then:
        read == 1
    }

    def 'test that tokenizer input stream read byte array works'() {
        given:
        def arr = new byte[3]

        when:
        def read = this.stream.read(arr)

        then:
        read == 3
        new String(arr) == 'hel'
    }

    def 'test that tokenizer input stream read byte array buffer works'() {
        given:
        def arr = new byte[5]

        and:
        this.stream.push('!?')

        when:
        def read = this.stream.read(arr)

        then:
        read == 5
        new String(arr) == '!?hel'
    }

    def 'test that tokenizer input stream read byte array bound works'() {
        given:
        def arr = 'lloXX'.bytes

        when:
        def read = this.stream.read(arr, 3, 2)

        then:
        read == 2
        new String(arr) == 'llohe'
    }

    def 'test that tokenizer input stream read byte array bound buffer works'() {
        given:
        def arr = 'WorldXX XXllo!'.bytes

        and:
        this.stream.push('?! ')

        when:
        def read = this.stream.read(arr, 5, length)

        then:
        read == length
        new String(arr) == expected

        where:
        length || expected
        5      || 'World?! hello!'
        3      || 'World?! XXllo!'
        1      || 'World?X XXllo!'
    }

    def 'test that tokenizer input stream read byte array bound throws exception for values: #offset, #length'() {
        given:
        def arr = new byte[1]

        when:
        this.stream.read(arr, offset, length)

        then:
        thrown(IndexOutOfBoundsException)

        where:
        offset | length
        -1     | 2
        2      | -1
        -1     | -1
        2      | 2
    }

    def 'test that tokenizer input stream skip works'() {
        when:
        def skipped = this.stream.skip(4)

        and:
        def read = this.stream.read()

        then:
        skipped == 4
        read == 'o'.charAt(0)
    }

    def 'test that tokenizer input stream skip buffer works'() {
        given:
        this.stream.push('?!')

        when:
        def skipped = this.stream.skip(bytes)

        and:
        def read = this.stream.read()

        then:
        skipped == bytes
        read == expected.charAt(0)

        where:
        bytes || expected
        6     || 'o'
        1     || '!'
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
