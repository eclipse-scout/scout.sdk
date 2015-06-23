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
package org.eclipse.scout.sdk.s2e.nls.internal.serviceProject;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.s2e.nls.internal.simpleProject.NlsType;
import org.eclipse.scout.sdk.s2e.nls.internal.simpleProject.SimpleNlsProject;
import org.eclipse.scout.sdk.s2e.nls.project.INlsProject;

public class ServiceNlsProject extends SimpleNlsProject {

  private final IType m_texts;

  public ServiceNlsProject(NlsType t) throws JavaModelException {
    super(t);
    m_texts = t.getType().getJavaProject().findType("org.eclipse.scout.rt.shared.TEXTS");
  }

  @Override
  public void setParent(INlsProject newParent) {
    super.setParent(newParent);
  }

  @Override
  public IType getNlsAccessorType() {
    return m_texts;
  }
}
