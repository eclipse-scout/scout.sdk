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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.shared;

import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.ShowJavaReferencesAction;
import org.eclipse.scout.sdk.ui.action.create.CodeNewAction;
import org.eclipse.scout.sdk.ui.action.delete.DeleteAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.InnerTypePageDirtyListener;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;

public class CodeTypeNodePage extends AbstractScoutTypePage {

  private InnerTypePageDirtyListener m_innerTypeListener;

  public CodeTypeNodePage(IPage parent, IType type) {
    setParent(parent);
    setType(type);

    setName(PropertyMethodSourceUtility.getTranslatedMethodStringValue(getType(), "getConfiguredText"));
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.CodeType));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.CODE_TYPE_NODE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  @Override
  public void unloadPage() {
    super.unloadPage();
    if (m_innerTypeListener != null) {
      ScoutSdkCore.getJavaResourceChangedEmitter().removeInnerTypeChangedListener(getType(), m_innerTypeListener);
      m_innerTypeListener = null;
    }
  }

  @Override
  protected void loadChildrenImpl() {
    if (m_innerTypeListener == null) {
      IType iCode = TypeUtility.getType(IRuntimeClasses.ICode);
      m_innerTypeListener = new InnerTypePageDirtyListener(this, iCode);
      ScoutSdkCore.getJavaResourceChangedEmitter().addInnerTypeChangedListener(getType(), m_innerTypeListener);
    }

    Set<IType> codes = ScoutTypeUtility.getCodes(getType());
    for (IType code : codes) {
      new CodeNodePage(this, code);
    }
  }

  @Override
  public Set<Class<? extends IScoutHandler>> getSupportedMenuActions() {
    return newSet(DeleteAction.class, CodeNewAction.class, ShowJavaReferencesAction.class);
  }
}
