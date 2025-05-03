package it.fulminazzo.mojito.utils

import spock.lang.Specification

class StringUtilsTest extends Specification {

    def 'test that quoteSplitter method works'() {
        given:
        def string = 'Hello(Friend, World, Mom), how, (are you, you doing)'

        when:
        def split = StringUtils.quoteSplitter(string, ', *', '\\(', '\\)')

        then:
        split.length == 3
        split[0] == 'Hello(Friend, World, Mom)'
        split[1] == 'how'
        split[2] == '(are you, you doing)'
    }

}
