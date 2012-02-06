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
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.util.wellform.WellformCodeTypesOperation;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.WellformAction;
import org.eclipse.scout.sdk.ui.action.create.CodeTypeNewAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class CodeTypeTablePage extends AbstractPage {
  final IType iCodeType = TypeUtility.getType(RuntimeClasses.ICodeType);

  private ICachedTypeHierarchy m_codeTypeHierarchy;

  public CodeTypeTablePage(IPage parent) {
    setParent(parent);
    setName(Texts.get("EnumerationTablePage"));
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.CodeTypes));
  }

  @Override
  public void unloadPage() {
    if (m_codeTypeHierarchy != null) {
      m_codeTypeHierarchy.removeHierarchyListener(getPageDirtyListener());
    }
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.CODE_TYPE_TABLE_PAGE;
  }

  @Override
  public void refresh(boolean clearCache) {
    if (clearCache && m_codeTypeHierarchy != null) {
      m_codeTypeHierarchy.invalidate();
    }
    super.refresh(clearCache);
  }

  /**
   * shared bundle
   */
  @Override
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  @Override
  public void loadChildrenImpl() {
    if (m_codeTypeHierarchy == null) {
      m_codeTypeHierarchy = TypeUtility.getPrimaryTypeHierarchy(iCodeType);
      m_codeTypeHierarchy.addHierarchyListener(getPageDirtyListener());
    }
    ITypeFilter filter = TypeFilters.getClassesInProject(getScoutResource().getJavaProject());
    IType[] codeTypes = m_codeTypeHierarchy.getAllSubtypes(iCodeType, filter, TypeComparators.getTypeNameComparator());
    for (IType codeType : codeTypes) {
      new CodeTypeNodePage(this, codeType);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{WellformAction.class, CodeTypeNewAction.class};
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    if (menu instanceof WellformAction) {
      WellformAction action = (WellformAction) menu;
      action.setOperation(new WellformCodeTypesOperation(getScoutResource()));
      action.setLabel(Texts.get("WellformAllCodeTypes"));
    }
    else if (menu instanceof CodeTypeNewAction) {
      ((CodeTypeNewAction) menu).setScoutBundle(getScoutResource());
    }
  }
}
