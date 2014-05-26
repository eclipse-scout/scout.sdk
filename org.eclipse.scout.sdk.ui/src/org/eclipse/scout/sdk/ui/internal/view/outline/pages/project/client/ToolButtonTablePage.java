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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.create.ToolbuttonNewAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.InnerTypePageDirtyListener;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeComparators;

/**
 * <h3>ToolTablePage</h3> Finds all inner subclasses of ITool for the given parent type.
 * 
 * @see ToolButtonNodePage
 */
public class ToolButtonTablePage extends AbstractPage {

  private InnerTypePageDirtyListener m_toolButtonChangedListener;
  private final IType m_declaringType;

  public ToolButtonTablePage(IPage parentPage, IType declaringType) {
    m_declaringType = declaringType;
    setName(Texts.get("ToolTablePage"));
    setParent(parentPage);
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Buttons));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.TOOL_BUTTON_TABLE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  /**
   * @return the declaringType
   */
  public IType getDeclaringType() {
    return m_declaringType;
  }

  @Override
  public void unloadPage() {
    super.unloadPage();
    if (m_toolButtonChangedListener != null) {
      ScoutSdkCore.getJavaResourceChangedEmitter().removeInnerTypeChangedListener(getDeclaringType(), m_toolButtonChangedListener);
      m_toolButtonChangedListener = null;
    }
  }

  @Override
  protected void loadChildrenImpl() {
    IType iToolButton = TypeUtility.getType(IRuntimeClasses.IToolButton);

    if (m_toolButtonChangedListener == null) {
      m_toolButtonChangedListener = new InnerTypePageDirtyListener(this, iToolButton);
      ScoutSdkCore.getJavaResourceChangedEmitter().addInnerTypeChangedListener(getDeclaringType(), m_toolButtonChangedListener);
    }
    for (IType toolbutton : TypeUtility.getInnerTypesOrdered(getDeclaringType(), iToolButton, ScoutTypeComparators.getOrderAnnotationComparator())) {
      new ToolButtonNodePage(this, toolbutton);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{ToolbuttonNewAction.class};
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    ((ToolbuttonNewAction) menu).init(getDeclaringType());
  }
}
