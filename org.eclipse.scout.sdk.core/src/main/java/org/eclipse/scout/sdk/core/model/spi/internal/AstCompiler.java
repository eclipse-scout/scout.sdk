/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.model.spi.internal;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

/**
 *
 */
public class AstCompiler extends org.eclipse.jdt.internal.compiler.Compiler {
  private final Map<CompilationUnitDeclaration, ICompilationUnit> m_sources = new HashMap<>();

  @SuppressWarnings("resource")
  public AstCompiler(INameEnvironment nameEnv) {
    super(nameEnv, DefaultErrorHandlingPolicies.proceedWithAllProblems(), createDefaultOptions(), new P_EmptyCompilerRequestor(), new CollectingProblemFactory(), new P_EmptyPrintWriter(), null /*progress*/);
    lookupEnvironment.completeTypeBindings(); // must be called once so that the initial state is ready
  }

  @Override
  protected synchronized void addCompilationUnit(org.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceUnit, CompilationUnitDeclaration parsedUnit) {
    super.addCompilationUnit(sourceUnit, parsedUnit);
    m_sources.put(parsedUnit, sourceUnit);
  }

  public ICompilationUnit getSource(CompilationUnitDeclaration decl) {
    return m_sources.get(decl);
  }

  /**
   * @return null when no errors
   */
  public synchronized String getCompileErrors(CompilationUnitDeclaration unit) {
    process(unit, 0);

    CategorizedProblem[] errors = unit.compilationResult().getErrors();
    if (errors != null && errors.length > 0) {
      StringBuilder sb = new StringBuilder();
      for (CategorizedProblem p : errors) {
        sb.append(p.getMessage()).append('\n');
      }
      sb.setLength(sb.length() - 1);
      return sb.toString();
    }
    return null;
  }

  static CompilerOptions createDefaultOptions() {
    CompilerOptions opts = new CompilerOptions();
    opts.produceDebugAttributes = 0;
    opts.complianceLevel = ClassFileConstants.JDK1_8;
    opts.originalComplianceLevel = opts.complianceLevel;
    opts.sourceLevel = opts.complianceLevel;
    opts.originalSourceLevel = opts.complianceLevel;
    opts.targetJDK = opts.complianceLevel;
    opts.verbose = false; // enable for debug info
    opts.preserveAllLocalVariables = true;
    opts.parseLiteralExpressionsAsConstants = false;
    opts.reportUnusedParameterIncludeDocCommentReference = false;
    opts.reportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable = false;
    opts.suppressOptionalErrors = true;
    opts.performMethodsFullRecovery = false;
    opts.performStatementsRecovery = true;
    opts.generateClassFiles = false;
    opts.reportMissingOverrideAnnotationForInterfaceMethodImplementation = false;
    opts.ignoreSourceFolderWarningOption = true;
    opts.analyseResourceLeaks = false;
    opts.ignoreMethodBodies = true;
    opts.docCommentSupport = true; //we want javadoc start and end positions

    opts.isAnnotationBasedNullAnalysisEnabled = true;
    opts.storeAnnotations = true; // also parse annotations
    return opts;
  }

  private static final class CollectingProblemFactory extends DefaultProblemFactory {

    CollectingProblemFactory() {
    }

    @Override
    public CategorizedProblem createProblem(char[] originatingFileName, int problemId, String[] problemArguments, String[] messageArguments, int severity, int startPosition, int endPosition, int lineNumber, int columnNumber) {
      return createProblem(originatingFileName, problemId, problemArguments, 0, messageArguments, severity, startPosition, endPosition, lineNumber, columnNumber);
    }

    @Override
    public CategorizedProblem createProblem(char[] originatingFileName, int problemId, String[] problemArguments,
        int elaborationId, String[] messageArguments, int severity, int startPosition, int endPosition, int lineNumber, int columnNumber) {
      if ((severity & (ProblemSeverities.Error | ProblemSeverities.Fatal | ProblemSeverities.InternalError)) != 0) {
        StringBuilder msg = new StringBuilder();
        if (originatingFileName != null) {
          msg.append(originatingFileName).append(":");
        }
        if (lineNumber > 0) {
          msg.append(lineNumber);
        }
        if (msg.length() > 0) {
          msg.append(" ");
        }
        String txt = getLocalizedMessage(problemId, elaborationId, messageArguments);
        if (txt != null) {
          msg.append(txt);
        }
        return new DefaultProblem(originatingFileName, msg.toString(), problemId, problemArguments, severity, startPosition, endPosition, lineNumber, columnNumber);
      }
      return null;
    }

  }

  private static final class P_EmptyCompilerRequestor implements ICompilerRequestor {
    @Override
    public void acceptResult(CompilationResult result) {
    }
  }

  private static final class P_EmptyPrintWriter extends PrintWriter {
    @SuppressWarnings("resource")
    private P_EmptyPrintWriter() {
      super(new P_EmptyWriter());
    }
  }

  private static final class P_EmptyWriter extends Writer {

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void close() throws IOException {
    }
  }
}
