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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.table;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.action.WizardAction;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.InnerTypePageDirtyListener;
import org.eclipse.scout.sdk.ui.wizard.tablecolumn.TableColumnNewWizard;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.SdkTypeUtility;

/**
 * <h3>ColumnTablePage</h3> ...
 */
public class ColumnTablePage extends AbstractPage {

  final IType iColumn = ScoutSdk.getType(RuntimeClasses.IColumn);
  private IType m_columnDeclaringType;

  private InnerTypePageDirtyListener m_innerTypeListener;

  public ColumnTablePage(IPage parent, IType columnDeclaringType) {
    super();
    setParent(parent);
    m_columnDeclaringType = columnDeclaringType;
    setName(Texts.get("OutlineColumnsTablePage"));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.COLUMN_TABLE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  /**
   * client bundle
   */
  @Override
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  @Override
  public void unloadPage() {
    super.unloadPage();
    if (m_innerTypeListener != null) {
      ScoutSdk.removeInnerTypeChangedListener(getColumnDeclaringType(), m_innerTypeListener);
      m_innerTypeListener = null;
    }
  }

  @Override
  public void loadChildrenImpl() {
    if (m_innerTypeListener == null) {
      m_innerTypeListener = new InnerTypePageDirtyListener(this, iColumn);
      ScoutSdk.addInnerTypeChangedListener(getColumnDeclaringType(), m_innerTypeListener);
    }
    for (IType innerType : SdkTypeUtility.getColumns(getColumnDeclaringType())) {
      ColumnNodePage childPage = new ColumnNodePage();
      childPage.setParent(this);
      childPage.setType(innerType);
    }
  }

  @Override
  public Action createNewAction() {
    TableColumnNewWizard wizard = new TableColumnNewWizard();
    wizard.initWizard(getColumnDeclaringType());
    return new WizardAction(Texts.get("Action_newTypeX", "Column"), JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_CLASS), wizard);
  }

  public IType getColumnDeclaringType() {
    return m_columnDeclaringType;
  }

}
