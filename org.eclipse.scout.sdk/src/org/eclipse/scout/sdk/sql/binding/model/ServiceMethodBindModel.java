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
package org.eclipse.scout.sdk.sql.binding.model;

import java.util.HashMap;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

/**
 * <h3>{@link ServiceMethodBindModel}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 25.02.2011
 */
public class ServiceMethodBindModel {
  HashMap<String /*paramName*/, IType /*form data type*/> m_methodParameters;
  private final IMethod m_serviceMethod;

  public ServiceMethodBindModel(IMethod serviceMethod) {
    m_serviceMethod = serviceMethod;
    m_methodParameters = new HashMap<String, IType>();
  }

  /**
   * @return the serviceMethod
   */
  public IMethod getServiceMethod() {
    return m_serviceMethod;
  }

  public void addFormDataMethodParameter(String paramName, IType formDataType) {
    m_methodParameters.put(paramName, formDataType);
  }

  public IType getFormData(String paramName) {
    return m_methodParameters.get(paramName);
  }

  public boolean hasFormDataMethodParameters() {
    return !m_methodParameters.isEmpty();
  }

  public String[] getFormDataParameterNames() {
    return m_methodParameters.keySet().toArray(new String[m_methodParameters.size()]);
  }

}
