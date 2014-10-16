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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form;

import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.operation.ITypeResolver;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.WellformAction;
import org.eclipse.scout.sdk.ui.action.create.FormNewAction;
import org.eclipse.scout.sdk.ui.action.dto.TypeResolverFormDataAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeFilters;

/**
 * <h3>FormTablePage</h3>
 */
public class FormTablePage extends AbstractPage implements ITypeResolver {
  private ICachedTypeHierarchy m_formHierarchy;

  public FormTablePage(AbstractPage parent) {
    setName(Texts.get("FormTablePage"));
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Forms));
    setParent(parent);
  }

  @Override
  public void unloadPage() {
    if (m_formHierarchy != null) {
      m_formHierarchy.removeHierarchyListener(getPageDirtyListener());
    }
  }

  @Override
  public void refresh(boolean clearCache) {
    if (clearCache && m_formHierarchy != null) {
      m_formHierarchy.invalidate();
    }
    super.refresh(clearCache);
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.FORM_TABLE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  @Override
  protected void loadChildrenImpl() {
    for (IType t : getTypes()) {
      new FormNodePage(this, t);
    }
  }

  @Override
  public Set<IType> getTypes() {
    IType iForm = TypeUtility.getType(IRuntimeClasses.IForm);
    IType iSearchForm = TypeUtility.getType(IRuntimeClasses.ISearchForm);

    if (m_formHierarchy == null) {
      m_formHierarchy = TypeUtility.getPrimaryTypeHierarchy(iForm);
      m_formHierarchy.addHierarchyListener(getPageDirtyListener());
    }
    IScoutBundle sb = getScoutBundle();
    Set<IType> searchForms = m_formHierarchy.getAllSubtypes(iSearchForm, ScoutTypeFilters.getClassesInScoutBundles(sb));
    Set<IType> allForms = m_formHierarchy.getAllSubtypes(iForm,
        TypeFilters.getMultiTypeFilterAnd(ScoutTypeFilters.getClassesInScoutBundles(sb), TypeFilters.getNotInTypes(searchForms)),
        TypeComparators.getTypeNameComparator());
    return allForms;
  }

  @Override
  public Set<Class<? extends IScoutHandler>> getSupportedMenuActions() {
    return newSet(WellformAction.class, FormNewAction.class, TypeResolverFormDataAction.class);
  }
}
