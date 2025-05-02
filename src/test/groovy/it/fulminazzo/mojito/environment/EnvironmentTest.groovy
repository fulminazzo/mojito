package it.fulminazzo.mojito.environment

import it.fulminazzo.mojito.environment.scopetypes.ScopeType
import spock.lang.Specification

class EnvironmentTest extends Specification {
    private Environment<Integer> environment

    void setup() {
        this.environment = new Environment()
    }

    void cleanup() {
        // Exit all scopes
        while (!this.environment.isMainScope()) this.environment.exitScope()
    }

    def 'test isInTryScope of exception #exception should return true'() {
        given:
        this.environment.enterScope(ScopeType.tryScope([IllegalArgumentException, RuntimeException].stream()))

        expect:
        this.environment.isInTryScope(exception)

        where:
        exception << [IllegalArgumentException, RuntimeException, IllegalStateException]
    }

    def 'test isInTryScope of exception #exception should not return true'() {
        given:
        this.environment.enterScope(ScopeType.tryScope([IllegalArgumentException, IllegalStateException].stream()))
        this.environment.enterScope(ScopeType.tryScope([ScopeException, IOException].stream()))

        expect:
        !this.environment.isInTryScope(exception)

        where:
        exception << [
                RuntimeException, Exception, InstantiationException, ConcurrentModificationException,
                NullPointerException, Throwable,
        ]
    }

    def 'test main scope should not be anything else'() {
        when:
        this.environment.check(scopeType)

        then:
        def e = thrown(ScopeException)
        e.message == ScopeException.scopeTypeMismatch(scopeType).message

        where:
        scopeType << ScopeType.values().findAll { it != ScopeType.MAIN }
    }

    def 'test check of no types should throw different error'() {
        given:
        def arr = new ScopeType[0]

        and:
        def exceptionMessage = ScopeException.scopeTypeMismatch(arr).message

        when:
        this.environment.check()

        then:
        def e = thrown(ScopeException)
        e.message == exceptionMessage
    }

    def 'test exit of main scope should throw exception'() {
        when:
        this.environment.exitScope()

        then:
        thrown(IllegalStateException)
    }

    def 'test inner scope type should be able to see #scopeType scope type'() {
        given:
        this.environment.enterScope(ScopeType.CODE_BLOCK)
        ScopeType.values().each { this.environment.enterScope(it) }
        this.environment.enterScope(scopeType)
        this.environment.enterScope(ScopeType.CODE_BLOCK)
        this.environment.enterScope(ScopeType.CODE_BLOCK)
        this.environment.enterScope(ScopeType.CODE_BLOCK)

        when:
        this.environment.check(scopeType)

        then:
        notThrown(ScopeException)

        where:
        scopeType << ScopeType.values().findAll { it != ScopeType.MAIN }
    }

    def 'test scope type'() {
        expect:
        this.environment.scopeType() == ScopeType.MAIN
    }

    def 'test declare, isDeclared, lookup and update'() {
        given:
        def varName = 'var'

        when:
        this.environment.declare(new WrapperInfo<>(Integer), NamedEntity.of(varName), 1)
        def declared = this.environment.isDeclared(NamedEntity.of(varName))
        def first = this.environment.lookup(NamedEntity.of(varName))
        this.environment.update(NamedEntity.of(varName), 2)
        def second = this.environment.lookup(NamedEntity.of(varName))

        then:
        declared
        first == 1
        second == 2
    }

    def 'test lookupInfo'() {
        given:
        def expected = new WrapperInfo<>(Integer)
        def varName = 'var'

        when:
        this.environment.declare(expected, NamedEntity.of(varName), 1)
        def actual = this.environment.lookupInfo(NamedEntity.of(varName))

        then:
        actual == expected
    }

    def 'test lookupInfo not declared'() {
        given:
        def varName = 'var'

        when:
        this.environment.lookupInfo(NamedEntity.of(varName))

        then:
        def e = thrown(ScopeException)
        e.message == ScopeException.noSuchVariable(NamedEntity.of(varName)).message
    }

    def 'test markConstant'() {
        given:
        def expected = new WrapperInfo<>(Integer)
        def varName = 'var'

        when:
        this.environment.declare(expected, NamedEntity.of(varName), 1)
        this.environment.markConstant(NamedEntity.of(varName))

        and:
        def data = this.environment.lastScope().getKey(NamedEntity.of(varName))

        then:
        data.get().isConstant()
    }

    def 'test markConstant not declared'() {
        given:
        def varName = 'var'

        when:
        this.environment.markConstant(NamedEntity.of(varName))

        then:
        def e = thrown(ScopeException)
        e.message == ScopeException.noSuchVariable(NamedEntity.of(varName)).message
    }

    def 'test declare twice'() {
        given:
        def varName = 'var'

        when:
        this.environment.declare(new WrapperInfo<>(Integer), NamedEntity.of(varName), 1)
        this.environment.declare(new WrapperInfo<>(Integer), NamedEntity.of(varName), 2)

        then:
        def e = thrown(ScopeException)
        e.message == ScopeException.alreadyDeclaredVariable(NamedEntity.of(varName)).message
    }

    def 'test lookup without declare'() {
        given:
        def varName = 'var'

        when:
        this.environment.lookup(NamedEntity.of(varName))

        then:
        def e = thrown(ScopeException)
        e.message == ScopeException.noSuchVariable(NamedEntity.of(varName)).message
    }

    def 'test update without declare'() {
        given:
        def varName = 'var'

        when:
        this.environment.update(NamedEntity.of(varName), 1)

        then:
        def e = thrown(ScopeException)
        e.message == ScopeException.noSuchVariable(NamedEntity.of(varName)).message
    }

}
