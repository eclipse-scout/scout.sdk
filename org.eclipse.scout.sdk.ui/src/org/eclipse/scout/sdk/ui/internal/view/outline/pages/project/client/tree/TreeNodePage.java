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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.tree;

import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.delete.DeleteAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.KeyStrokeTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.MenuTablePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;

public class TreeNodePage extends AbstractScoutTypePage {

  private final boolean m_hasDeleteAction;

  public TreeNodePage(AbstractPage parent, IType type) {
    this(parent, type, false);
  }

  public TreeNodePage(AbstractPage parent, IType type, boolean hasDeleteAction) {
    super();
    m_hasDeleteAction = hasDeleteAction;
    setParent(parent);
    setType(type);
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Tree));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.TREE_NODE_PAGE;
  }

  @Override
  protected String getMethodNameForTranslatedText() {
    return "getConfiguredTitle";
  }

  @Override
  protected void loadChildrenImpl() {
    new KeyStrokeTablePage(this, getType());
    new MenuTablePage(this, getType());
  }

  @Override
  public Set<Class<? extends IScoutHandler>> getSupportedMenuActions() {
    if (m_hasDeleteAction) {
      return newSet(DeleteAction.class);
    }
    return null;
  }
}
