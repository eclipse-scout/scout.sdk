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
    return new JavaEnvironmentBuilder()
        .withoutScoutSdk()
        .withSourceFolder("src/main/fixture")
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

  /**
   * @return the {@link IType} for the {@link BaseClass} fixture.
   */
  public static IType getBaseClassType() {
    ICompilationUnit icu = getChildClassIcu(); // do not get from getBaseClassIcu()
    return icu.types().first().superClass();
  }

  /**
   * @return the {@link IType} for the {@link ChildClass} fixture.
   */
  public static IType getChildClassType() {
    ICompilationUnit icu = getChildClassIcu();
    return icu.types().first();
  }

  /**
   * Asserts that a primary class with given fully qualified name and source compiles within the given
   * {@link IJavaEnvironment}.
   *
   * @param env
   *          The {@link IJavaEnvironment} to do the compilation.
   * @param fqn
   *          The fully qualified name of the primary class (e.g. org.eclipse.scout.sdk.test.MyClass).
   * @param source
   *          The source of the whole compilation unit of the given primary class name.
   * @return The {@link IType} representing the given type and source.
   * @throws AssertionError
   *           if the given type has compile errors within the given {@link IJavaEnvironment}.
   */
  public static IType assertNoCompileErrors(IJavaEnvironment env, String fqn, String source) {
    String pck = Signature.getQualifier(fqn);
    String simpleName = Signature.getSimpleName(fqn);
    return assertNoCompileErrors(env, pck, simpleName, source);
  }

  /**
   * Asserts that the primary class with given simple name, qualifier and source compiles within the given
   * {@link IJavaEnvironment}.
   *
   * @param env
   *          The {@link IJavaEnvironment} to do the compilation.
   * @param qualifier
   *          The qualifier of the class (e.g. org.eclipse.scout.sdk.test)
   * @param simpleName
   *          The simple name of the class (e.g. MyClass)
   * @param source
   *          The source of the whole compilation unit of the given primary class.
   * @return The {@link IType} representing the given type and source.
   * @throws AssertionError
   *           if the given type has compile errors within the given {@link IJavaEnvironment}.
   */
  public static IType assertNoCompileErrors(IJavaEnvironment env, String qualifier, String simpleName, String source) {
    boolean reloadRequired = env.registerCompilationUnitOverride(qualifier, simpleName + SuffixConstants.SUFFIX_STRING_java, new StringBuilder(source));
    if (reloadRequired) {
      env.reload();
    }
    String fqn = qualifier + '.' + simpleName;
    IType t = env.findType(fqn);
    Assert.assertNotNull("Generated type '" + fqn + "' could not be found.", t);
    Assert.assertNull(env.compileErrors(t.name()));
    return t;
  }

  /**
   * @return The {@link ICompilationUnit} of the {@link ChildClass} fixture.
   */
  public static synchronized ICompilationUnit getChildClassIcu() {
    if (childClassIcu == null) {
      childClassIcu = createJavaEnvironment().findType(ChildClass.class.getName()).compilationUnit();
    }
    return childClassIcu;
  }

  /**
   * @return The {@link ICompilationUnit} of the {@link BaseClass} fixture.
   */
  public static synchronized ICompilationUnit getBaseClassIcu() {
    if (baseClassIcu == null) {
      baseClassIcu = createJavaEnvironment().findType(BaseClass.class.getName()).compilationUnit();
    }
    return baseClassIcu;
  }

  /**
   * Removes all white spaces of the given {@link String}.
   *
   * @param s
   *          The string in which the white spaces should be removed.
   * @return The input {@link String} without any white spaces.
   */
  public static String removeWhitespace(String s) {
    if (s == null) {
      return null;
    }
    return WHITESPACE_PAT.matcher(s).replaceAll("");
  }

  /**
   * normalizes all white space characters to one space. This removes any tabs, new-lines, etc.
   *
   * @param s
   *          The input {@link String} for witch the white spaces should be normalized.
   * @return The input {@link String} with the white spaces normalized.
   */
  public static String normalizeWhitespace(String s) {
    if (s == null) {
      return null;
    }
    return WHITESPACE_PAT.matcher(s).replaceAll(" ").trim();
  }

  /**
   * Imports an {@link IJavaEnvironment} that has been previously exported using
   * {@link #exportJavaEnvironment(IJavaEnvironment, Writer)}.
   *
   * @param r
   *          The source to import
   * @return The imported {@link IJavaEnvironment}.
   * @throws IOException
   */
  public static IJavaEnvironment importJavaEnvironment(Reader r) throws IOException {
    Properties p = new Properties();
    p.load(r);
    return importJavaEnvironment(p);
  }

  /**
   * Exports the given {@link IJavaEnvironment} into the given {@link Writer}. It can be imported again using
   * {@link #importJavaEnvironment(Reader)}.
   *
   * @param env
   *          The {@link IJavaEnvironment} to export.
   * @param w
   *          The write to export it to.
   * @throws IOException
   */
  public static void exportJavaEnvironment(IJavaEnvironment env, Writer w) throws IOException {
    StringBuilder src = new StringBuilder();
    StringBuilder bin = new StringBuilder();
    for (ClasspathSpi cp : env.unwrap().getClasspath()) {
      (cp.getMode() == ClasspathSpi.MODE_SOURCE ? src : bin).append("\n    " + cp.getPath() + ",");
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
   * allowErrors=true,
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
