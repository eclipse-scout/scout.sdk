/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.internal.util;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.scout.sdk.core.util.SdkLog;

/**
 * <h3>{@link StubUtilityBridge}</h3> Bridge to support Eclipse IDE 2018-12 and older.<br>
 * The class StubUtility was moved for release 2018-12. This class can be deleted as soon as 2018-12 is the oldest
 * supported version.
 *
 * @since 8.0.0
 */
public final class StubUtilityBridge {

  private static Class<?> stubUtilityClass;

  private StubUtilityBridge() {
  }

  @SuppressWarnings({"squid:S1181", "squid:S2259"})
  public static Template getCodeTemplate(String id, IJavaProject project) {
    try {
      return (Template) getStubUtilityClass()
          .getMethod("getCodeTemplate", String.class, IJavaProject.class)
          .invoke(null, id, project);
    }
    catch (Throwable e) {
      SdkLog.error(e);
      return null;
    }
  }

  @SuppressWarnings("squid:S1166")
  private static Class<?> getStubUtilityClass() {
    Class<?> ret = stubUtilityClass;
    if (ret == null) {
      try {
        ret = StubUtilityBridge.class.getClassLoader().loadClass("org.eclipse.jdt.internal.core.manipulation.StubUtility");
      }
      catch (ClassNotFoundException e) {
        ret = loadLegacyStubUtilityClass();
      }
      stubUtilityClass = ret;
    }
    return ret;
  }

  @SuppressWarnings("squid:S1166")
  private static Class<?> loadLegacyStubUtilityClass() {
    try {
      return StubUtilityBridge.class.getClassLoader().loadClass("org.eclipse.jdt.internal.corext.codemanipulation.StubUtility");
    }
    catch (ClassNotFoundException e1) {
      SdkLog.error("Could not found a StubUtility class on classpath.");
      return null;
    }
  }
}
