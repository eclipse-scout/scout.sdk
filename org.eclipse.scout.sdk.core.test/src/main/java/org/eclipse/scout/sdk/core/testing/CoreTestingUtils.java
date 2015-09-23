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

import org.eclipse.scout.sdk.core.fixture.BaseClass;
import org.eclipse.scout.sdk.core.fixture.ChildClass;
import org.eclipse.scout.sdk.core.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.util.JavaEnvironmentBuilder;

/**
 * helpers used for general core unit tests (not specific to scout generated code)
 */
public final class CoreTestingUtils {
  private static ICompilationUnit baseClassIcu;
  private static ICompilationUnit childClassIcu;

  private CoreTestingUtils() {
  }

  /**
   * @return a {@link IJavaEnvironment} containing the source folder <code>src/main/fixture</code>
   */
  public static IJavaEnvironment createJavaEnvironment() {
    return new JavaEnvironmentBuilder()
        .withExcludeScoutSdk()
        .withSourceFolder("src/main/fixture")
        .build();
  }

  /**
   * @return a {@link IJavaEnvironment} containing the binary folder <code>target/classes</code>
   */
  public static IJavaEnvironment createJavaEnvironmentWithBinaries() {
    return new JavaEnvironmentBuilder()
        .withExcludeScoutSdk()
        .withExcludeAllSources()
        .withClassesFolder("target/classes")
        .build();
  }

  public static IType getBaseClassType() {
    ICompilationUnit icu = getChildClassIcu(); // do not get from getBaseClassIcu()
    return icu.getTypes().get(0).getSuperClass();
  }

  public static IType getChildClassType() {
    ICompilationUnit icu = getChildClassIcu();
    return icu.getTypes().get(0);
  }

  public static synchronized ICompilationUnit getChildClassIcu() {
    if (childClassIcu == null) {
      childClassIcu = createJavaEnvironment().findType(ChildClass.class.getName()).getCompilationUnit();
    }
    return childClassIcu;
  }

  public static synchronized ICompilationUnit getBaseClassIcu() {
    if (baseClassIcu == null) {
      baseClassIcu = createJavaEnvironment().findType(BaseClass.class.getName()).getCompilationUnit();
    }
    return baseClassIcu;
  }

  public static String removeWhitespace(String s) {
    if (s == null) {
      return null;
    }
    return s.replaceAll("\\s+", "");
  }

  public static String normalizeWhitespace(String s) {
    if (s == null) {
      return null;
    }
    return s.replaceAll("\\s+", " ").trim();
  }

}
