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
package org.eclipse.scout.sdk.ui.fields.proposal.javaelement;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>{@link JavaElementAbstractTypeContentProvider}</h3> ...
 * 
 * @author Matthias Villiger
 * @since 3.8.0 20.04.2012
 */
public class JavaElementAbstractTypeContentProvider extends AbstractJavaElementContentProvider {

  private final IType m_superType;
  private final IJavaProject m_project;
  private final IType[] m_mostlyUsed;

  public JavaElementAbstractTypeContentProvider(IType superType, IJavaProject project, IType... mostlyUsed) {
    m_superType = superType;
    m_project = project;
    m_mostlyUsed = mostlyUsed;
  }

  @Override
  protected Object[][] computeProposals() {
    return new Object[][]{m_mostlyUsed, ScoutTypeUtility.getAbstractTypesOnClasspath(m_superType, m_project, m_mostlyUsed)};
  }
}
