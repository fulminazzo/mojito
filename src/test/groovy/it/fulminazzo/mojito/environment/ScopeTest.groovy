package it.fulminazzo.mojito.environment

import it.fulminazzo.mojito.environment.scopetypes.ScopeType
import spock.lang.Specification

class ScopeTest extends Specification {
    private Scope<String> scope

    void setup() {
        this.scope = new Scope<>(ScopeType.CODE_BLOCK)
    }

    def 'test lookupInfo not declared'() {
        given:
        def varName = 'var'

        when:
        this.scope.lookupInfo(NamedEntity.of(varName))

        then:
        def e = thrown(ScopeException)
        e.message == ScopeException.noSuchVariable(NamedEntity.of(varName)).message
    }

    def 'test markConstant not declared'() {
        given:
        def varName = 'var'

        when:
        this.scope.markConstant(NamedEntity.of(varName))

        then:
        def e = thrown(ScopeException)
        e.message == ScopeException.noSuchVariable(NamedEntity.of(varName)).message
    }

    def 'declare should throw ScopeException on already declared variable'() {
        when:
        this.scope.declare(new WrapperInfo<>(String), NamedEntity.of('var'), '1')
        this.scope.declare(new WrapperInfo<>(String), NamedEntity.of('var'), '1')

        then:
        def e = thrown(ScopeException)
        e.message == ScopeException.alreadyDeclaredVariable(NamedEntity.of('var')).message
    }

    def 'update should throw ScopeException on not declared variable'() {
        when:
        this.scope.update(NamedEntity.of('var'), '1')

        then:
        def e = thrown(ScopeException)
        e.message == ScopeException.noSuchVariable(NamedEntity.of('var')).message
    }

    def 'update should throw ScopeException on invalid variable'() {
        when:
        this.scope.declare(new WrapperInfo<>(String), NamedEntity.of('var'), '1')
        this.scope.update(NamedEntity.of('var'), 10)

        then:
        def e = thrown(ScopeException)
        e.message == 'Cannot assign 10 to WrapperInfo(String)'
    }

    def 'test object data equality'() {
        given:
        def data = new Scope.ObjectData(new WrapperInfo<>(String), NamedEntity.of('var'))

        expect:
        data == new Scope.ObjectData(new WrapperInfo<>(String), NamedEntity.of('var'))
    }

    def 'test object data should not be equal to #other'() {
        given:
        def data = new Scope.ObjectData(new WrapperInfo<>(String), NamedEntity.of('var'))

        expect:
        data != other

        where:
        other << [
                new Scope.ObjectData(new WrapperInfo<>(Integer), NamedEntity.of('var')),
                new Scope.ObjectData(new WrapperInfo<>(String), NamedEntity.of('other')),
                new Scope.ObjectData(new WrapperInfo<>(Integer), NamedEntity.of('other')),
                new WrapperInfo<>(Integer),
        ]
    }

    def 'test object data toString'() {
        given:
        def data = new Scope.ObjectData(new WrapperInfo<>(String), NamedEntity.of('var'))

        expect:
        data.toString() == "${Scope.ObjectData.simpleName}(${new WrapperInfo<>(String)}, var)"
    }

}
