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
package org.eclipse.scout.sdk.ui.wizard.tablecolumn;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.extensions.AbstractInnerTypeWizard;
import org.eclipse.ui.IWorkbench;

/**
 * <h3> {@link TableColumnNewWizard}</h3> This wizard is the starting wizard to create a new table column. and will be
 * followed by a {@link SmartTableColumnNewWizard} or a {@link DefaultTableColumnNewWizard}.
 *
 * @see TableColumnNewWizardPage1
 */
public class TableColumnNewWizard extends AbstractInnerTypeWizard {

  private TableColumnNewWizardPage1 m_page1;

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    super.init(workbench, selection);

    setWindowTitle(Texts.get("NewTableColumn"));

    m_page1 = new TableColumnNewWizardPage1(getDeclaringType(), getContinueOperation());
    if (getSiblingProposal() != null) {
      m_page1.setSibling(getSiblingProposal());
    }
    if (getTypeName() != null) {
      m_page1.setTypeName(getTypeName());
    }
    if (getSuperType() != null) {
      m_page1.setSuperType(getSuperType());
    }
    addPage(m_page1);
  }

  @Override
  protected boolean beforeFinish() throws CoreException {
    // we never want to execute the finish operations on this wizard.
    // They have been passed to the concrete nested wizard and will be executed there.
    getPerformFinishOperationsInternal().clear();
    return super.beforeFinish();
  }

  @Override
  public boolean needsPreviousAndNextButtons() {
    return true;
  }

  @Override
  public boolean canFinish() {
    return false;
  }
}
