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
package org.eclipse.scout.sdk.core;

import org.eclipse.scout.sdk.core.fixture.BaseClass;
import org.eclipse.scout.sdk.core.fixture.ChildClass;
import org.eclipse.scout.sdk.core.model.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.IType;
import org.eclipse.scout.sdk.core.testing.TestingUtils;

/**
 *
 */
public final class CoreTestingUtils {

  public static final String SOURCE_FOLDER = "src/main/fixture";

  private static ICompilationUnit baseClassIcu;
  private static ICompilationUnit childClassIcu;

  private CoreTestingUtils() {
  }

  public static IType getBaseClassType() {
    ICompilationUnit icu = getChildClassIcu(); // do not get from getBaseClassIcu()
    return ((IType) icu.getTypes().get(0)).getSuperClass();
  }

  public static IType getChildClassType() {
    ICompilationUnit icu = getChildClassIcu();
    return (IType) icu.getTypes().get(0);
  }

  public static ICompilationUnit getChildClassIcu() {
    if (childClassIcu == null) {
      synchronized (TestingUtils.class) {
        if (childClassIcu == null) {
          childClassIcu = TestingUtils.getType(ChildClass.class.getName(), SOURCE_FOLDER).getCompilationUnit();
        }
      }
    }
    return childClassIcu;
  }

  public static ICompilationUnit getBaseClassIcu() {
    if (baseClassIcu == null) {
      synchronized (TestingUtils.class) {
        if (baseClassIcu == null) {
          baseClassIcu = TestingUtils.getType(BaseClass.class.getName(), SOURCE_FOLDER).getCompilationUnit();
        }
      }
    }
    return baseClassIcu;
  }
}
