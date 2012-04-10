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
package org.eclipse.scout.nls.sdk.services.model.ws;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.nls.sdk.simple.model.ws.NlsType;
import org.eclipse.scout.sdk.util.type.TypeUtility;

public class NlsServiceType extends NlsType {
  private static final Pattern REGEX_RESOURCE_BUNDLE_GETTER = Pattern.compile("return\\s*\\\"([^\\\"]*)\\\"\\s*\\;", Pattern.DOTALL);
  public static final String DYNAMIC_NLS_BASE_NAME_GETTER = "getDynamicNlsBaseName";

  public NlsServiceType(IType serviceType) {
    super(serviceType);
  }

  @Override
  protected void loadSuperTypeHierarchy() throws JavaModelException {
  }

  @Override
  protected String getBundleValue() throws JavaModelException {
    IMethod getter = m_type.getMethod(DYNAMIC_NLS_BASE_NAME_GETTER, new String[]{});
    if (TypeUtility.exists(getter)) {
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
