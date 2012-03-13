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

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.extensions.AbstractFormFieldWizard;

/**
 * <h3> {@link TableColumnNewWizard}</h3> This wizard is the starting wizard to create a new table column. and will be
 * followed by a {@link SmartTableColumnNewWizard} or a {@link DefaultTableColumnNewWizard}.
 * 
 * @see TableColumnNewWizardPage1
 */
public class TableColumnNewWizard extends AbstractFormFieldWizard {

  private TableColumnNewWizardPage1 m_page1;
  private CONTINUE_OPERATION m_nextOperation;

  public static enum CONTINUE_OPERATION {
    ADD_MORE_COLUMNS, FINISH
  }

  public TableColumnNewWizard(CONTINUE_OPERATION op) {
    setWindowTitle(Texts.get("NewTableColumn"));
    m_nextOperation = op;
  }

  @Override
  public void initWizard(IType declaringType) {
    super.initWizard(declaringType);
    m_page1 = new TableColumnNewWizardPage1(getDeclaringType(), m_nextOperation);
    addPage(m_page1);
  }

  @Override
  public void setSuperType(IType superType) {
    m_page1.setSuperType(superType);
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
