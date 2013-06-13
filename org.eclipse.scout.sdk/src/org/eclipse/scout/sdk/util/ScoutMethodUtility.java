/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.util;

import java.util.regex.Matcher;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;

/**
 *
 */
public final class ScoutMethodUtility {
  private ScoutMethodUtility() {
  }

  public static String getMethodReturnValue(IMethod method) {
    try {
      String src = method.getSource();
      if (src != null) {
        Matcher m = Regex.REGEX_PROPERTY_METHOD_REPRESENTER_VALUE.matcher(src);
        if (m.find()) {
          return m.group(1).trim();
        }
        else {
          ScoutSdk.logInfo("Could not find return value of method '" + method.getElementName() + "' in type '" + method.getDeclaringType().getFullyQualifiedName() + "'.");
        }
      }
      else {
        ScoutSdk.logWarning("Could not find source for method '" + method.getElementName() + "' in type '" + method.getDeclaringType().getFullyQualifiedName() + "'.");
      }
    }
    catch (JavaModelException e) {
      ScoutSdk.logError("Could not find return value of method '" + method.getElementName() + "' in type '" + method.getDeclaringType().getFullyQualifiedName() + "'.");
    }
    return null;
  }

  public static INlsEntry getReturnNlsEntry(IMethod method) throws CoreException {
    String value = getMethodReturnValue(method);
    return getReturnNlsEntry(value, method);
  }

  private static INlsEntry getReturnNlsEntry(String value, IMethod method) throws CoreException {
    String key = PropertyMethodSourceUtility.parseReturnParameterNlsKey(value);
    if (!StringUtility.isNullOrEmpty(key)) {
      INlsProject nlsProject = ScoutTypeUtility.findNlsProject(method);
      if (nlsProject == null) {
        ScoutSdk.logWarning("could not find nls project for method '" + method.getElementName() + "' in type '" + method.getDeclaringType().getFullyQualifiedName() + "'");
      }
      else {
        return nlsProject.getEntry(key);
      }
    }
    return null;
  }
}
