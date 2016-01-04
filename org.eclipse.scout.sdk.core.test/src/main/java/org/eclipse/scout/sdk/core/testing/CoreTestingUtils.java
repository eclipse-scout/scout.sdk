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
package org.eclipse.scout.sdk.core.testing;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;
import java.util.regex.Pattern;

import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.scout.sdk.core.fixture.BaseClass;
import org.eclipse.scout.sdk.core.fixture.ChildClass;
import org.eclipse.scout.sdk.core.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.api.IFileLocator;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.internal.JavaEnvironmentImplementor;
import org.eclipse.scout.sdk.core.model.spi.ClasspathSpi;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.junit.Assert;

/**
 * helpers used for general core unit tests (not specific to scout generated code)
 */
public final class CoreTestingUtils {
  private static final Pattern WHITESPACE_PAT = Pattern.compile("\\s+");
  private static ICompilationUnit baseClassIcu;
  private static ICompilationUnit childClassIcu;

  private CoreTestingUtils() {
  }

  /**
   * @return a {@link IJavaEnvironment} containing the source folder <code>src/main/fixture</code> and using a
   *         {@link IFileLocator} with the test module itself as root
   */
  public static IJavaEnvironment createJavaEnvironment() {
    return createJavaEnvironment(null);
  }

  /**
   * @return a {@link IJavaEnvironment} containing the source folder <code>src/main/fixture</code> and the specified
   *         fileLocator
   */
  public static IJavaEnvironment createJavaEnvironment(IFileLocator fileLocator) {
    return new JavaEnvironmentBuilder()
        .withoutScoutSdk()
        .withSourceFolder("src/main/fixture")
        .withFileLocator(fileLocator)
        .build();
  }

  /**
   * @return a {@link IJavaEnvironment} containing the binary folder <code>target/classes</code>
   */
  public static IJavaEnvironment createJavaEnvironmentWithBinaries() {
    return new JavaEnvironmentBuilder()
        .withoutScoutSdk()
        .withoutAllSources()
        .withClassesFolder("target/classes")
        .build();
  }

  public static IType getBaseClassType() {
    ICompilationUnit icu = getChildClassIcu(); // do not get from getBaseClassIcu()
    return icu.types().first().superClass();
  }

  public static IType getChildClassType() {
    ICompilationUnit icu = getChildClassIcu();
    return icu.types().first();
  }

  public static String getCompileErrors(IJavaEnvironment env, String fqn) {
    if (env instanceof JavaEnvironmentImplementor) {
      return ((JavaEnvironmentImplementor) env).compileErrors(fqn);
    }
    throw new UnsupportedOperationException(IJavaEnvironment.class.getName() + " implementation '" + env.getClass().getName() + "' is not supported.");
  }

  public static IType assertNoCompileErrors(IJavaEnvironment env, String fqn, String source) {
    String pck = Signature.getQualifier(fqn);
    String simpleName = Signature.getSimpleName(fqn);
    return assertNoCompileErrors(env, pck, simpleName, source);
  }

  public static IType assertNoCompileErrors(IJavaEnvironment env, String qualifier, String simpleName, String source) {
    env.registerCompilationUnitOverride(qualifier, simpleName + SuffixConstants.SUFFIX_STRING_java, new StringBuilder(source));
    env.reload();
    IType t = env.findType(qualifier + '.' + simpleName);
    Assert.assertNull(getCompileErrors(env, t.name()));
    return t;
  }

  public static synchronized ICompilationUnit getChildClassIcu() {
    if (childClassIcu == null) {
      childClassIcu = createJavaEnvironment().findType(ChildClass.class.getName()).compilationUnit();
    }
    return childClassIcu;
  }

  public static synchronized ICompilationUnit getBaseClassIcu() {
    if (baseClassIcu == null) {
      baseClassIcu = createJavaEnvironment().findType(BaseClass.class.getName()).compilationUnit();
    }
    return baseClassIcu;
  }

  public static String removeWhitespace(String s) {
    if (s == null) {
      return null;
    }
    return WHITESPACE_PAT.matcher(s).replaceAll("");
  }

  public static String normalizeWhitespace(String s) {
    if (s == null) {
      return null;
    }
    return WHITESPACE_PAT.matcher(s).replaceAll(" ").trim();
  }

  public static IJavaEnvironment importJavaEnvironment(InputStream in) throws IOException {
    Properties p = new Properties();
    p.load(in);
    return importJavaEnvironment(p);
  }

  public static IJavaEnvironment importJavaEnvironment(Reader r) throws IOException {
    Properties p = new Properties();
    p.load(r);
    return importJavaEnvironment(p);
  }

  public static void exportJavaEnvironment(IJavaEnvironment env, Writer w) throws IOException {
    StringBuilder src = new StringBuilder();
    StringBuilder bin = new StringBuilder();
    for (ClasspathSpi cp : env.unwrap().getClasspath()) {
      (cp.isSource() ? src : bin).append("\n    " + cp.getPath() + ",");
    }
    Properties p = new Properties();
    p.setProperty("src", src.toString());
    p.setProperty("bin", bin.toString());
    p.store(w, "");
  }

  /**
   * @param p
   *
   *          <pre>
   *  allowErrors=true,
   * src=path1, path2, ...
   * bin=path1, path2, ...
   *          </pre>
   *
   * @return
   */
  public static IJavaEnvironment importJavaEnvironment(Properties p) {
    JavaEnvironmentBuilder builder = new JavaEnvironmentBuilder()
        .withRunningClasspath(false);
    for (String s : p.getProperty("src").split(",")) {
      s = s.trim();
      if (!s.isEmpty()) {
        builder.withAbsoluteSourcePath(s);
      }
    }
    for (String s : p.getProperty("bin").split(",")) {
      s = s.trim();
      if (!s.isEmpty()) {
        builder.withAbsoluteBinaryPath(s);
      }
    }
    return builder.build();
  }
}
