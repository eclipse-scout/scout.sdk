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
package org.eclipse.scout.sdk.core.parser;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;

import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/**
 *
 */
public class AstCompiler extends org.eclipse.jdt.internal.compiler.Compiler {

  @SuppressWarnings("resource")
  public AstCompiler(INameEnvironment env) {
    super(env, DefaultErrorHandlingPolicies.proceedWithAllProblems(), createDefaultOptions(), new P_EmptyCompilerRequestor(), new CollectProblemFactory(), new P_EmptyPrintWriter(), null /*progress*/);
    lookupEnvironment.completeTypeBindings(); // must be called once so that the initial state is ready
  }

  public synchronized CompilationUnitDeclaration createAst(ICompilationUnit icu) {
    beginToCompile(new ICompilationUnit[]{icu});
    CompilationUnitDeclaration unit = this.unitsToProcess[0];
    if (!options.ignoreMethodBodies) {
      process(unit, 0);
    }
    // do not call unit.cleanup() so that the scope back pointers remain valid for further lazy loading

    unit.compilationResult.tagAsAccepted();

    // do not reset() this compiler because the structure is still needed for further lazy loading
    return unit;
  }

  public void throwOnErrors() {
    List<CharSequence> errors = getProblemFactory().getErrors();
    if (!errors.isEmpty()) {
      StringBuilder sb = new StringBuilder("Errors found:\n");
      sb.append(errors.get(0));
      for (int i = 1; i < errors.size(); i++) {
        sb.append('\n').append(errors.get(i));
      }
      throw new RuntimeException(sb.toString());
    }
  }

  @Override
  public void reset() {
    super.reset();
    getProblemFactory().reset();
  }

  public CollectProblemFactory getProblemFactory() {
    return (CollectProblemFactory) problemReporter.problemFactory;
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

    opts.isAnnotationBasedNullAnalysisEnabled = true;
    opts.storeAnnotations = true; // also parse annotations
    return opts;
  }

  private static final class P_EmptyCompilerRequestor implements ICompilerRequestor {
    @Override
    public void acceptResult(CompilationResult result) {
    }
  }

  private static final class P_EmptyPrintWriter extends PrintWriter {
    @SuppressWarnings("resource")
    public P_EmptyPrintWriter() {
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
