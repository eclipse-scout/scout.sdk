/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.testing;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Pattern;

import org.eclipse.scout.sdk.core.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * Testing helpers
 */
public final class CoreTestingUtils {
  private static final Pattern WHITESPACE_PAT = Pattern.compile("\\s+");

  private CoreTestingUtils() {
  }

  /**
   * Registers the source of the specified {@link ICompilationUnitGenerator} within the specified
   * {@link IJavaEnvironment} as override.
   *
   * @param env
   *          The {@link IJavaEnvironment} in which the compilation unit should be registered as override (see
   *          {@link IJavaEnvironment#registerCompilationUnitOverride(String, String, CharSequence)}.
   * @param generator
   *          The {@link ICompilationUnitGenerator} that builds the source.
   * @return The {@link IType} that corresponds to the main type of the specified {@link ICompilationUnitGenerator}.
   */
  public static IType registerCompilationUnit(IJavaEnvironment env, ICompilationUnitGenerator<?> generator) {
    var src = Ensure.notNull(generator).toJavaSource(Ensure.notNull(env));
    return registerCompilationUnit(env, generator.packageName().orElse(null), generator.mainType().get().elementName().get(), src);
  }

  /**
   * Registers a compilation unit override with the specified configuration.
   *
   * @param env
   *          The {@link IJavaEnvironment} in which the override should be registered.
   * @param qualifier
   *          The package qualifier of the compilation unit or {@code null} for the default package.
   * @param simpleName
   *          The simple name of the compilation unit (this is the Java file name without file extension. Corresponds to
   *          the name of the main type).
   * @param source
   *          The source of the compilation unit.
   * @return The {@link IType} that corresponds to the main type of the specified {@link ICompilationUnitGenerator}.
   */
  public static IType registerCompilationUnit(IJavaEnvironment env, String qualifier, String simpleName, CharSequence source) {
    var reloadRequired = env.registerCompilationUnitOverride(qualifier, simpleName + JavaTypes.JAVA_FILE_SUFFIX, source);
    if (reloadRequired) {
      env.reload();
    }

    var fqn = new StringBuilder();
    if (Strings.hasText(qualifier)) {
      fqn.append(qualifier).append(JavaTypes.C_DOT);
    }
    fqn.append(simpleName);

    var t = env.findType(fqn.toString());
    assertTrue(t.isPresent(), "Generated type '" + fqn + "' could not be found.");
    return t.get();
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
    return WHITESPACE_PAT.matcher(s).replaceAll(" ").trim();
  }
}
