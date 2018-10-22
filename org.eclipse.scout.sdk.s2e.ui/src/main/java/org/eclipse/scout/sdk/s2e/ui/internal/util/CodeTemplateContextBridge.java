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
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.scout.sdk.core.util.SdkLog;

/**
 * <h3>{@link CodeTemplateContextBridge}</h3>Bridge to support Eclipse IDE 2018-12 and older.<br>
 * The class CodeTemplateContext was moved for release 2018-12. This class can be deleted as soon as 2018-12 is the
 * oldest supported version.
 *
 * @since 8.0.0
 */
public final class CodeTemplateContextBridge {

  private static Class<?> codeTemplateContextClass;

  private CodeTemplateContextBridge() {
  }

  @SuppressWarnings({"squid:S1181", "squid:S2259"})
  public static TemplateContext createCodeTemplateContext(String contextTypeName, IJavaProject project, String lineDelim) {
    try {
      return (TemplateContext) getCodeTemplateContextClass()
          .getConstructor(String.class, IJavaProject.class, String.class)
          .newInstance(contextTypeName, project, lineDelim);
    }
    catch (Throwable e) {
      SdkLog.error(e);
      return null;
    }
  }

  @SuppressWarnings("squid:S1166")
  private static Class<?> getCodeTemplateContextClass() {
    Class<?> ret = codeTemplateContextClass;
    if (ret == null) {
      try {
        ret = CodeTemplateContextBridge.class.getClassLoader().loadClass("org.eclipse.jdt.internal.core.manipulation.CodeTemplateContext");
      }
      catch (ClassNotFoundException e) {
        ret = loadLegacyCodeTemplateContextClass();
      }
      codeTemplateContextClass = ret;
    }
    return ret;
  }

  @SuppressWarnings("squid:S1166")
  private static Class<?> loadLegacyCodeTemplateContextClass() {
    try {
      return CodeTemplateContextBridge.class.getClassLoader().loadClass("org.eclipse.jdt.internal.corext.template.java.CodeTemplateContext");
    }
    catch (ClassNotFoundException e1) {
      SdkLog.error("Could not found a CodeTemplateContext class on classpath.");
      return null;
    }
  }
}
