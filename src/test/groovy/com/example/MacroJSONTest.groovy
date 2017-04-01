package com.example

import groovy.json.JsonOutput
import groovy.transform.CompileStatic
import groovy.transform.ToString
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.GStringExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression

@ToString
class MacroJSONTest extends GroovyTestCase {

    void "test that json works"() {
        assertScript '''
         def myRandomValue = UUID.randomUUID();
         def result = macroJSON {
            bla 123
            s "yo"
            person {
                name "John"
                age 26
            }
            rnd myRandomValue
        }
        assert result == """{"bla":123,"s":"yo","person":{"name":"John","age":26},"rnd":"${myRandomValue}"}"""
        '''
    }

    @CompileStatic
    void "test json AST"() {
        ClosureExpression cl = macro { { ->
            bla 123
            s "yo"
            person {
                name "John"
                age 26
            }
            rnd myRandomValue
        }}
        def result = SuperMacroMethods.macroJSON(null, cl)

        assert result instanceof GStringExpression

        assert result.getStrings().size() == 2
        assert result.getStrings()[0].text == '{"bla":123,"s":"yo","person":{"name":"John","age":26},"rnd":'
        assert result.getStrings()[1].text == '}'

        assert result.getValues().size() == 1
        def value = result.getValues()[0]
        assert value instanceof StaticMethodCallExpression

        assert value.getReceiver() == ClassHelper.makeWithoutCaching(JsonOutput)
        assert value.getMethodAsString() == "toJson"

        def arguments = value.getArguments()
        assert arguments instanceof VariableExpression
        assert arguments.getName() == "myRandomValue"
    }

}
