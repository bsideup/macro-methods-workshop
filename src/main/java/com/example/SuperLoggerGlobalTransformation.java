package com.example;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.MethodCallTransformation;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.classgen.asm.InvocationWriter;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.GroovyASTTransformation;

@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
public class SuperLoggerGlobalTransformation extends MethodCallTransformation {

  @Override
  protected GroovyCodeVisitor getTransformer(ASTNode[] nodes, SourceUnit sourceUnit) {
    return new ClassCodeExpressionTransformer() {

      @Override
      protected SourceUnit getSourceUnit() {
        return sourceUnit;
      }

      @Override
      public Expression transform(Expression exp) {
        Expression result = super.transform(exp);

        if (result instanceof MethodCallExpression) {
          MethodCallExpression call = (MethodCallExpression) result;

          if ("superlog".equals(call.getMethodAsString())) {
            TupleExpression args = InvocationWriter.makeArgumentList(call.getArguments());

            if (args.getExpressions().size() == 1) {
              return SuperMacroMethods.superlog(
                  sourceUnit,
                  call,
                  args.getExpression(0)
              );
            }
          }
        }

        return result;
      }
    };
  }
}
