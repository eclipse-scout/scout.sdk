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

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.action.ShowJavaReferencesAction;
import org.eclipse.scout.sdk.ui.action.create.CodeNewAction;
import org.eclipse.scout.sdk.ui.action.delete.DeleteAction;
import org.eclipse.scout.sdk.ui.action.rename.TypeRenameAction;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.InnerTypePageDirtyListener;
import org.eclipse.scout.sdk.util.ScoutSourceUtilities;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.SdkTypeUtility;

public class CodeNodePage extends AbstractScoutTypePage {
  final IType iCode = ScoutSdk.getType(RuntimeClasses.ICode);
  private InnerTypePageDirtyListener m_innerTypeListener;

  public CodeNodePage(IPage parent, IType type) {
    setParent(parent);
    setType(type);
    setName(ScoutSourceUtilities.getTranslatedMethodStringValue(getType(), "getConfiguredText"));
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Code));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.CODE_NODE_PAGE;
  }

  @Override
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  @Override
  public void unloadPage() {
    super.unloadPage();
    if (m_innerTypeListener != null) {
      ScoutSdk.removeInnerTypeChangedListener(getType(), m_innerTypeListener);
      m_innerTypeListener = null;
    }
  }

  @Override
  public void loadChildrenImpl() {
    if (m_innerTypeListener == null) {
      m_innerTypeListener = new InnerTypePageDirtyListener(this, iCode);
      ScoutSdk.addInnerTypeChangedListener(getType(), m_innerTypeListener);
    }
//    ITypeHierarchy codeHierarchy = ScoutSdk.getTypeHierarchyPrimaryTypes(iCode).combinedTypeHierarchy(getType());
//    ITypeFilter filter = TypeFilters.getMultiTypeFilter(
//        TypeFilters.getSubtypeFilter(iCode, codeHierarchy),
//        TypeFilters.getClassFilter());

    IType[] codes = SdkTypeUtility.getCodes(getType());//TypeUtility.getInnerTypes(getType(), filter, TypeComparators.getOrderAnnotationComparator());
    for (IType code : codes) {
      new CodeNodePage(this, code);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends AbstractScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{TypeRenameAction.class, ShowJavaReferencesAction.class, CodeNewAction.class, DeleteAction.class};
  }

  @Override
  public void prepareMenuAction(AbstractScoutHandler menu) {
    super.prepareMenuAction(menu);
    if (menu instanceof CodeNewAction) {
      ((CodeNewAction) menu).setType(getType());
    }
    else if (menu instanceof DeleteAction) {
      DeleteAction action = (DeleteAction) menu;
      action.addType(getType());
      action.setName(getName());
      action.setImage(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.CodeRemove));
    }
  }
}
