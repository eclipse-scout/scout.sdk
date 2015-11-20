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
package org.eclipse.scout.sdk.s2e.nls.internal.serviceproject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.s2e.nls.internal.simpleproject.NlsType;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;

public class NlsServiceType extends NlsType {
  private static final Pattern REGEX_RESOURCE_BUNDLE_GETTER = Pattern.compile("return\\s*\\\"([^\\\"]*)\\\"\\s*\\;", Pattern.DOTALL);
  public static final String DYNAMIC_NLS_BASE_NAME_GETTER = "getDynamicNlsBaseName";

  public NlsServiceType(IType serviceType) {
    super(serviceType);
  }

  @Override
  protected void loadSuperTypeHierarchy() throws JavaModelException {
    // not required for services
  }

  @Override
  protected String getBundleValue() throws JavaModelException {
    IMethod getter = m_type.getMethod(DYNAMIC_NLS_BASE_NAME_GETTER, new String[]{});
    if (S2eUtils.exists(getter)) {
      int flags = getter.getFlags();
      int refFlags = Flags.AccProtected;
      if ((refFlags & flags) == refFlags) {
        String source = getter.getSource();
        if (source != null) {
          Matcher matcher = REGEX_RESOURCE_BUNDLE_GETTER.matcher(source);
          if (matcher.find()) {
            return matcher.group(1);
          }
        }
      }
    }
    return null;
  }
}
