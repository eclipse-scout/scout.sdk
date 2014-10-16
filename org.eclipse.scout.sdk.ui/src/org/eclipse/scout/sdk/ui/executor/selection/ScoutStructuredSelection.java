/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.executor.selection;

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.scout.sdk.ui.fields.proposal.SiblingProposal;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard.ContinueOperation;

/**
 * <h3>{@link ScoutStructuredSelection}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 09.10.2014
 */
public class ScoutStructuredSelection extends StructuredSelection {
  private SiblingProposal m_sibling;
  private IType m_superType;
  private String m_typeName;
  private ContinueOperation m_continueOperation;

  public ScoutStructuredSelection(Object[] elements) {
    super(elements);
  }

  public SiblingProposal getSibling() {
    return m_sibling;
  }

  public void setSibling(SiblingProposal sibling) {
    m_sibling = sibling;
  }

  public IType getSuperType() {
    return m_superType;
  }

  public void setSuperType(IType superType) {
    m_superType = superType;
  }

  public String getTypeName() {
    return m_typeName;
  }

  public void setTypeName(String typeName) {
    m_typeName = typeName;
  }

  public ContinueOperation getContinueOperation() {
    return m_continueOperation;
  }

  public void setContinueOperation(ContinueOperation continueOperation) {
    m_continueOperation = continueOperation;
  }
}
