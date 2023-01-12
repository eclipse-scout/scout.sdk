/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.testing;

import java.nio.file.Path;
import java.util.regex.Pattern;

import org.eclipse.scout.sdk.core.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.model.CompilationUnitInfo;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;
import org.opentest4j.AssertionFailedError;

/**
 * Testing helpers
 */
public final class CoreTestingUtils {
  private static final Pattern WHITESPACE_PAT = Pattern.compile("[\\s\0\1\2\3\4\5\6\7\u0008]+");

  private CoreTestingUtils() {
  }

  /**
   * Registers the source of the specified {@link ICompilationUnitGenerator} within the specified
   * {@link IJavaEnvironment} as override.
   *
   * @param env
   *          The {@link IJavaEnvironment} in which the compilation unit should be registered as override (see
   *          {@link IJavaEnvironment#registerCompilationUnitOverride(CharSequence, String, String)}.
   * @param generator
   *          The {@link ICompilationUnitGenerator} that builds the source.
   * @return The {@link IType} that corresponds to the main type of the specified {@link ICompilationUnitGenerator}.
   */
  public static IType registerCompilationUnit(IJavaEnvironment env, ICompilationUnitGenerator<?> generator) {
    var src = Ensure.notNull(generator).toJavaSource(Ensure.notNull(env));
    return registerCompilationUnit(env, src, generator.packageName().orElse(null), generator.elementName().orElseThrow());
  }

  /**
   * Registers a compilation unit override with the specified configuration.
   *
   * @param env
   *          The {@link IJavaEnvironment} in which the override should be registered.
   * @param source
   *          The source of the compilation unit.
   * @param qualifier
   *          The package qualifier of the compilation unit or {@code null} for the default package.
   * @param simpleName
   *          The simple name of the compilation unit (this is the Java file name without file extension. Corresponds to
   *          the name of the main type).
   * @return The {@link IType} that corresponds to the main type of the specified {@link ICompilationUnitGenerator}.
   */
  public static IType registerCompilationUnit(IJavaEnvironment env, CharSequence source, String qualifier, String simpleName) {
    return registerCompilationUnit(env, source, qualifier, simpleName, null);
  }

  /**
   * Registers a compilation unit override with the specified configuration.
   *
   * @param env
   *          The {@link IJavaEnvironment} in which the override should be registered.
   * @param source
   *          The source of the compilation unit.
   * @param qualifier
   *          The package qualifier of the compilation unit or {@code null} for the default package.
   * @param simpleName
   *          The simple name of the compilation unit (this is the Java file name without file extension. Corresponds to
   *          the name of the main type).
   * @param sourceFolder
   *          The source folder to which the compilation unit belongs. May be {@code null}.
   * @return The {@link IType} that corresponds to the main type of the specified {@link ICompilationUnitGenerator}.
   */
  public static IType registerCompilationUnit(IJavaEnvironment env, CharSequence source, String qualifier, String simpleName, Path sourceFolder) {
    var cuInfo = new CompilationUnitInfo(sourceFolder, qualifier, simpleName + JavaTypes.JAVA_FILE_SUFFIX);
    var reloadRequired = env.registerCompilationUnitOverride(source, cuInfo);
    if (reloadRequired) {
      env.reload();
    }

    var fqn = cuInfo.mainTypeFullyQualifiedName();
    return env.findType(fqn)
        .orElseThrow(() -> new AssertionFailedError("Generated type '" + fqn + "' could not be found."));
  }

  /**
   * Removes all white spaces of the given {@link String}.
   *
   * @param s
   *          The string in which the white spaces should be removed.
   * @return The input {@link String} without any white spaces.
   */
  public static String removeWhitespace(CharSequence s) {
    if (s == null) {
      return null;
    }
    return WHITESPACE_PAT.matcher(s).replaceAll("");
  }

  /**
   * Normalizes all new lines to unix style.
   *
   * @param text
   *          The text in which the new line characters should be normalized.
   * @return The input text with all {@code \r} removed.
   */
  public static CharSequence normalizeNewLines(CharSequence text) {
    //noinspection HardcodedLineSeparator
    return Strings.replace(text, "\r", "");
  }

  /**
   * normalizes all white space characters to one space. This removes any tabs, new-lines, etc.
   *
   * @param s
   *          The input {@link String} for witch the white spaces should be normalized.
   * @return The input {@link String} with the white spaces normalized.
   */
  public static String normalizeWhitespace(CharSequence s) {
    if (s == null) {
      return null;
    }
    return Strings.trim(WHITESPACE_PAT.matcher(s).replaceAll(" ")).toString();
  }
}
