package it.fulminazzo.mojito

import it.fulminazzo.mojito.executor.values.Value
import it.fulminazzo.mojito.typechecker.TypeCheckerException
import it.fulminazzo.mojito.typechecker.types.ClassType
import it.fulminazzo.mojito.typechecker.types.objects.ObjectClassType
import it.fulminazzo.mojito.typechecker.types.objects.ObjectType
import spock.lang.Specification

/**
 * This class will handle tests for Parser, Typechecker and Executor all together.
 */
class FullTests extends Specification {

    def 'test that list of TestClass returns correct object'() {
        given:
        def code = 'List<it.fulminazzo.mojito.TestClass> list = new LinkedList<>();' +
                'list.add(new it.fulminazzo.mojito.TestClass());' +
                'list.add(new it.fulminazzo.mojito.TestClass(1, true));' +
                'return list.get(0).publicStaticMethod() + list.get(1).publicField;'

        and:
        Runner runner = Mojito.newRunner()

        when:
        runner.run(code)

        then:
        runner.latestResult()
                .map { (Value) it}
                .map { it.value }
                .get() == 2.0
    }

    def 'test that list of numbers can support both doubles and numbers'() {
        given:
        def code = 'List<Number> list = new ArrayList<>();' +
                'list.add(1);' +
                'list.add(2L);' +
                'list.add(3.0d);' +
                'list.add(4.0f);'

        when:
        Mojito.newRunner().run(code)

        then:
        noExceptionThrown()
    }

    def 'test invalid enhanced for statement throws correct exception'() {
        given:
        def code = 'List<String> list = new ArrayList<String>();' +
                'list.add("Hello");' +
                'list.add("World");' +
                'for (Integer i : list)' +
                'System.out.println(i);'

        when:
        Mojito.newRunner().run(code)

        then:
        def e = thrown(TypeCheckerException)
        e.message == TypeCheckerException.invalidType(
                ClassType.of(Iterable, [ObjectClassType.INTEGER]),
                ObjectType.of(ArrayList, [ObjectClassType.STRING])
        ).message
    }

}
