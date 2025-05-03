package it.fulminazzo.mojito.typechecker.types.objects


import spock.lang.Specification

class GenericsObjectClassTypeTest extends Specification {

    def 'test that initialization with correct parameters does not throw'() {
        given:
        def type = ObjectType.of(List)

        and:
        def genericTypes = [ObjectClassType.STRING]

        when:
        def classType = new GenericsObjectClassType(type, genericTypes)

        then:
        classType.genericTypes['E'] == genericTypes[0]
    }

}
