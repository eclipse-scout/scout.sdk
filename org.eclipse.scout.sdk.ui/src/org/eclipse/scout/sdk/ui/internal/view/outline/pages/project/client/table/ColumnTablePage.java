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
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.TableColumnWidthsPasteAction;
import org.eclipse.scout.sdk.ui.action.create.TableColumnNewAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.InnerTypePageDirtyListener;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>ColumnTablePage</h3> ...
 */
public class ColumnTablePage extends AbstractPage {

  private IType m_columnDeclaringType;
  private InnerTypePageDirtyListener m_innerTypeListener;

  public ColumnTablePage(IPage parent, IType columnDeclaringType) {
    super();
    setParent(parent);
    m_columnDeclaringType = columnDeclaringType;
    setName(Texts.get("OutlineColumnsTablePage"));
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.TableColumns));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.COLUMN_TABLE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  @Override
  public void unloadPage() {
    super.unloadPage();
    if (m_innerTypeListener != null) {
      ScoutSdkCore.getJavaResourceChangedEmitter().removeInnerTypeChangedListener(getColumnDeclaringType(), m_innerTypeListener);
      m_innerTypeListener = null;
    }
  }

  @Override
  protected void loadChildrenImpl() {
    if (m_innerTypeListener == null) {
      IType iColumn = TypeUtility.getType(IRuntimeClasses.IColumn);
      m_innerTypeListener = new InnerTypePageDirtyListener(this, iColumn);
      ScoutSdkCore.getJavaResourceChangedEmitter().addInnerTypeChangedListener(getColumnDeclaringType(), m_innerTypeListener);
    }
    for (IType innerType : ScoutTypeUtility.getColumns(getColumnDeclaringType())) {
      ColumnNodePage childPage = new ColumnNodePage();
      childPage.setParent(this);
      childPage.setType(innerType);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{TableColumnNewAction.class, TableColumnWidthsPasteAction.class};
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    if (menu instanceof TableColumnNewAction) {
      ((TableColumnNewAction) menu).init(getColumnDeclaringType());
    }
    else if (menu instanceof TableColumnWidthsPasteAction) {
      ((TableColumnWidthsPasteAction) menu).init(this);
    }
  }

  public IType getColumnDeclaringType() {
    return m_columnDeclaringType;
  }

}
