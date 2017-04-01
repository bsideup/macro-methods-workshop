package com.example

import groovy.json.JsonOutput
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.classgen.asm.InvocationWriter
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.macro.runtime.Macro
import org.codehaus.groovy.macro.runtime.MacroContext

import static org.codehaus.groovy.ast.tools.GeneralUtils.callX
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX
import static org.codehaus.groovy.ast.tools.GeneralUtils.convertASTToSource

class SuperMacroMethods {

    static ClassNode JSON_OUTPUT_CLASS_NODE = ClassHelper.makeWithoutCaching(JsonOutput);

    static void supermacrolog(Object o, Object arg) {
        throw new IllegalStateException("Do not call me at runtime!");
    }

    @Macro
    static Expression nameof(MacroContext ctx, VariableExpression variable) {
        return constX(variable.getName());
    }

    @Macro
    static Expression supermacrolog(MacroContext ctx, Expression argument) {
        return superlog(ctx.sourceUnit, ctx.call, argument)
    }

    static Expression superlog(SourceUnit sourceUnit, MethodCallExpression call, Expression argument) {
        String prefix = sourceUnit.getName();
        prefix += " [" + call.getLineNumber() + ":" + call.getColumnNumber() + "]";
        try {
            prefix += " " + convertASTToSource(sourceUnit.getSource(), argument);
        } catch (Exception e) {
            throw new RuntimeException("YOLO", e);
        }
        prefix += " = ";

        return (Expression) macro {
            println($v { constX(prefix) } + $v { argument })
        }
    }

    @Macro
    static Expression macroJSON(MacroContext ctx, ClosureExpression cl) {
        List<String> buffer = [];
        List<String> strings = [];
        List<Expression> values = [];
        closureToJSON(cl, buffer, strings, values)

        if (!buffer.empty) {
            strings << buffer.join("")
        }
        return new GStringExpression(
                "<GENERATED JSON GSTRING>",
                strings.collect { constX(it as String) },
                values
        )
    }

    /**
     * Please, don't do it like this. It's just a quick example :)
     */
    private static void closureToJSON(ClosureExpression cl, List<String> stringsBuffer, List<String> strings, List<Expression> values) {
        assert cl.code instanceof BlockStatement
        def code = cl.code as BlockStatement

        stringsBuffer << '{'

        code.statements.eachWithIndex { statement, i ->
            assert statement instanceof ExpressionStatement
            def expression = (statement as ExpressionStatement).expression

            assert expression instanceof MethodCallExpression
            def call = expression as MethodCallExpression

            def args = InvocationWriter.makeArgumentList(call.getArguments())

            assert args.expressions.size() == 1

            def firstArg = args.getExpression(0)

            def result = """"${call.methodAsString}":""";
            stringsBuffer << result

            switch (firstArg) {
                case ConstantExpression:
                    stringsBuffer << JsonOutput.toJson(firstArg.value)
                    break;
                case ClosureExpression:
                    closureToJSON(firstArg, stringsBuffer, strings, values)
                    break;
                default:
                    strings << stringsBuffer.join("")
                    stringsBuffer.clear()
                    values << callX(JSON_OUTPUT_CLASS_NODE, "toJson", firstArg)
                    break;
            }

            if (i != code.statements.size() - 1) {
                stringsBuffer << ","
            }
        }

        stringsBuffer << '}'
    }
}
