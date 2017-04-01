package com.example;

import groovy.inspect.swingui.AstNodeToScriptVisitor;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.io.PrintWriter;

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class DebugASTTransformation implements ASTTransformation {
  @Override
  public void visit(ASTNode[] nodes, SourceUnit source) {
    System.out.println("=====After transformation====");
    System.out.println();
    PrintWriter writer = new PrintWriter(System.out);
    ((ModuleNode) nodes[0]).getStatementBlock().visit(new AstNodeToScriptVisitor(writer));
    writer.flush();
    System.out.println("=============================");
  }
}
