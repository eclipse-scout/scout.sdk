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
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.action.create.ToolbuttonNewAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeComparators;

/**
 * <h3>ToolTablePage</h3> Finds all inner subclasses of ITool for the given parent type.
 * 
 * @see ToolButtonNodePage
 */
public class ToolButtonTablePage extends AbstractPage {

  private final IType m_declaringType;
  private IType[] m_toolbuttons;

  public ToolButtonTablePage(IPage parentPage, IType declaringType) {
    this(parentPage, declaringType, null);

  }

  public ToolButtonTablePage(IPage parentPage, IType declaringType, IType[] toolbuttons) {
    m_declaringType = declaringType;
    m_toolbuttons = toolbuttons;
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
   * client bundle
   */
  @Override
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  /**
   * @return the declaringType
   */
  public IType getDeclaringType() {
    return m_declaringType;
  }

  /**
   * @return the toolbuttons
   */
  public IType[] getToolbuttons() {
    return m_toolbuttons;
  }

  @Override
  public void loadChildrenImpl() {
    if (getToolbuttons() == null) {
      m_toolbuttons = TypeUtility.getInnerTypes(getDeclaringType(), TypeFilters.getSubtypeFilter(TypeUtility.getType(RuntimeClasses.IToolButton)), ScoutTypeComparators.getOrderAnnotationComparator());
    }
    for (IType toolbutton : getToolbuttons()) {
      new ToolButtonNodePage(this, toolbutton);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends AbstractScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{ToolbuttonNewAction.class};
  }

  @Override
  public void prepareMenuAction(AbstractScoutHandler menu) {
    ((ToolbuttonNewAction) menu).init(getDeclaringType());
  }
}
