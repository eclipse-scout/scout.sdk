/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.model.ecj;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies.proceedWithAllProblems;

import java.io.PrintWriter;
import java.io.Writer;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.ISourceType;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.scout.sdk.core.log.SdkLog;

public class EcjAstCompiler extends org.eclipse.jdt.internal.compiler.Compiler {

  private static final Level VERBOSE_LOG_LEVEL = Level.FINER;
  private final Map<CompilationUnitDeclaration, ICompilationUnit> m_sources = new HashMap<>();
  private final Object m_lock;

  protected EcjAstCompiler(INameEnvironment nameEnv, CompilerOptions opts, Object lock) {
    super(nameEnv, proceedWithAllProblems(), opts == null ? createDefaultOptions() : opts, new EmptyCompilerRequestor(), new CollectingProblemFactory(), new PrintWriter(new SdkLogWriter()), null);
    m_lock = lock;
    lookupEnvironment.completeTypeBindings(); // must be called once so that the initial state is ready
  }

  public static CompilerOptions createDefaultOptions() {
    var result = new CompilerOptions();
    result.produceDebugAttributes = 0;
    result.complianceLevel = ClassFileConstants.getLatestJDKLevel();
    result.originalComplianceLevel = result.complianceLevel;
    result.sourceLevel = result.complianceLevel;
    result.originalSourceLevel = result.complianceLevel;
    result.targetJDK = result.complianceLevel;
    result.verbose = SdkLog.isLevelEnabled(VERBOSE_LOG_LEVEL);
    result.preserveAllLocalVariables = true;
    result.parseLiteralExpressionsAsConstants = false;
    result.reportUnusedParameterIncludeDocCommentReference = false;
    result.reportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable = false;
    result.suppressOptionalErrors = true;
    result.performMethodsFullRecovery = false;
    result.performStatementsRecovery = true;
    result.generateClassFiles = false;
    result.reportMissingOverrideAnnotationForInterfaceMethodImplementation = false;
    result.ignoreSourceFolderWarningOption = true;
    result.analyseResourceLeaks = false;
    result.ignoreMethodBodies = true;
    result.docCommentSupport = true; //we want javadoc start and end positions
    result.isAnnotationBasedNullAnalysisEnabled = false; // do not enable (performance and NPE in JDT compiler)
    result.storeAnnotations = true; // also parse annotations
    return result;
  }

  @Override
  protected synchronized void addCompilationUnit(ICompilationUnit sourceUnit, CompilationUnitDeclaration parsedUnit) {
    super.addCompilationUnit(sourceUnit, parsedUnit);
    m_sources.put(parsedUnit, sourceUnit);
  }

  public synchronized ICompilationUnit getSource(CompilationUnitDeclaration decl) {
    return m_sources.get(decl);
  }

  /**
   * @return null when no errors
   */
  public List<String> getCompileErrors(CompilationUnitDeclaration unit) {
    synchronized (m_lock) {
      process(unit, 0);
    }

    var errors = unit.compilationResult().getErrors();
    if (errors == null || errors.length < 1) {
      return emptyList();
    }

    return Arrays.stream(errors)
        .map(CategorizedProblem::getMessage)
        .collect(toList());
  }

  @Override
  public void accept(IModule module, LookupEnvironment environment) {
    synchronized (m_lock) {
      super.accept(module, environment);
    }
  }

  @Override
  public void accept(IBinaryType binaryType, PackageBinding packageBinding, AccessRestriction accessRestriction) {
    synchronized (m_lock) {
      super.accept(binaryType, packageBinding, accessRestriction);
    }
  }

  @Override
  public void accept(ISourceType[] sourceTypes, PackageBinding packageBinding, AccessRestriction accessRestriction) {
    synchronized (m_lock) {
      super.accept(sourceTypes, packageBinding, accessRestriction);
    }
  }

  @Override
  public void accept(ICompilationUnit sourceUnit, AccessRestriction accessRestriction) {
    synchronized (m_lock) {
      super.accept(sourceUnit, accessRestriction);
    }
  }

  static final class CollectingProblemFactory extends DefaultProblemFactory {

    @Override
    public CategorizedProblem createProblem(char[] originatingFileName, int problemId, String[] problemArguments, String[] messageArguments, int severity, int startPosition, int endPosition,
        int lineNumber, int columnNumber) {
      return createProblem(originatingFileName, problemId, problemArguments, 0, messageArguments, severity, startPosition, endPosition, lineNumber, columnNumber);
    }

    @Override
    public CategorizedProblem createProblem(char[] originatingFileName, int problemId, String[] problemArguments,
        int elaborationId, String[] messageArguments, int severity, int startPosition, int endPosition, int lineNumber, int columnNumber) {
      if ((severity & (ProblemSeverities.Error | ProblemSeverities.Fatal | ProblemSeverities.InternalError)) != 0) {
        var txt = getLocalizedMessage(problemId, elaborationId, messageArguments);
        var msg = new StringBuilder(txt.length() + 128);
        if (originatingFileName != null) {
          msg.append(originatingFileName).append(':');
        }
        if (lineNumber > 0) {
          msg.append(lineNumber);
        }
        if (msg.length() > 0) {
          msg.append(' ');
        }
        msg.append(txt);
        return new DefaultProblem(originatingFileName, msg.toString(), problemId, problemArguments, severity, startPosition, endPosition, lineNumber, columnNumber);
      }
      return null;
    }
  }

  static final class EmptyCompilerRequestor implements ICompilerRequestor {
    @Override
    public void acceptResult(CompilationResult result) {
      // we are not interested in the results
    }
  }

  static final class SdkLogWriter extends Writer {

    @Override
    public void write(char[] buffer, int off, int len) {
      if (!SdkLog.isLevelEnabled(VERBOSE_LOG_LEVEL)) {
        return;
      }
      SdkLog.log(VERBOSE_LOG_LEVEL, CharBuffer.wrap(buffer, off, len));
    }

    @Override
    public void flush() {
      // nothing to flush
    }

    @Override
    public void close() {
      // nothing to close
    }
  }
}
