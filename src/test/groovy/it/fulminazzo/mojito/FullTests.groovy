package it.fulminazzo.mojito

import it.fulminazzo.mojito.typechecker.TypeCheckerException
import it.fulminazzo.mojito.typechecker.types.ClassType
import it.fulminazzo.mojito.typechecker.types.objects.ObjectClassType
import it.fulminazzo.mojito.typechecker.types.objects.ObjectType
import spock.lang.Specification

/**
 * This class will handle tests for Parser, Typechecker and Executor all together.
 */
class FullTests extends Specification {

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
