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
import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.WizardAction;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.wizard.lookupcall.LookupCallNewWizard;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ITypeFilter;
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.typecache.ICachedTypeHierarchy;

public class LookupCallTablePage extends AbstractPage {
  final IType lookupCall = ScoutSdk.getType(RuntimeClasses.LookupCall);
  private ICachedTypeHierarchy m_lookupCallHierarchy;

  public LookupCallTablePage(AbstractPage parent) {
    setParent(parent);
    setName(Texts.get("LookupCallTablePage"));
  }

  @Override
  public void unloadPage() {
    if (m_lookupCallHierarchy != null) {
      m_lookupCallHierarchy.removeHierarchyListener(getPageDirtyListener());
      m_lookupCallHierarchy = null;
    }
    super.unloadPage();
  }

  @Override
  public void refresh(boolean clearCache) {
    if (clearCache && m_lookupCallHierarchy != null) {
      m_lookupCallHierarchy.invalidate();
    }
    super.refresh(clearCache);
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.LOOKUP_CALL_TABLE_PAGE;
  }

  /**
   * shared bundle
   */
  @Override
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  @Override
  public void loadChildrenImpl() {
    if (m_lookupCallHierarchy == null) {
      m_lookupCallHierarchy = ScoutSdk.getPrimaryTypeHierarchy(lookupCall);
      m_lookupCallHierarchy.addHierarchyListener(getPageDirtyListener());
    }
    ITypeFilter filter = TypeFilters.getClassesInProject(getScoutResource().getJavaProject());
    IType[] lookupCallTypes = m_lookupCallHierarchy.getAllSubtypes(lookupCall, filter, TypeComparators.getTypeNameComparator());
    for (IType type : lookupCallTypes) {
      new LookupCallNodePage(this, type);
    }
  }

  @Override
  public Action createNewAction() {
    return new WizardAction(Texts.get("Action_newTypeX", "LookupCall"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.IMG_TOOL_ADD),
        new LookupCallNewWizard(getScoutResource()));
  }

}
