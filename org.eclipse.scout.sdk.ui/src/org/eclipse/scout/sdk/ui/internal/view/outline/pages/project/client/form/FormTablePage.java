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

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.util.wellform.WellformFormsOperation;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.MultipleUpdateFormDataAction;
import org.eclipse.scout.sdk.ui.action.WellformAction;
import org.eclipse.scout.sdk.ui.action.create.FormNewAction;
import org.eclipse.scout.sdk.ui.action.validation.ITypeResolver;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 * <h3>FormTablePage</h3> ...
 */
public class FormTablePage extends AbstractPage {

  final IType iForm = TypeUtility.getType(RuntimeClasses.IForm);
  final IType iSearchForm = TypeUtility.getType(RuntimeClasses.ISearchForm);

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

  /**
   * client bundle
   */
  @Override
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  @Override
  public void loadChildrenImpl() {
    for (IType t : resolveForms()) {
      new FormNodePage(this, t);
    }
  }

  protected IType[] resolveForms() {
    if (m_formHierarchy == null) {
      m_formHierarchy = TypeUtility.getPrimaryTypeHierarchy(iForm);
      m_formHierarchy.addHierarchyListener(getPageDirtyListener());
    }
    IJavaProject javaProject = getScoutResource().getJavaProject();
    IType[] searchForms = m_formHierarchy.getAllSubtypes(iSearchForm, TypeFilters.getClassesInProject(javaProject));
    IType[] allForms = m_formHierarchy.getAllSubtypes(iForm,
        TypeFilters.getMultiTypeFilter(
            TypeFilters.getClassesInProject(javaProject),
            TypeFilters.getNotInTypes(searchForms)
            ),
        TypeComparators.getTypeNameComparator());

    return allForms;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{WellformAction.class, FormNewAction.class, MultipleUpdateFormDataAction.class};
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    if (menu instanceof WellformAction) {
      WellformAction action = (WellformAction) menu;
      action.setOperation(new WellformFormsOperation(getScoutResource()));
      action.setLabel(Texts.get("WellformAllForms"));
    }
    else if (menu instanceof FormNewAction) {
      ((FormNewAction) menu).setScoutBundle(getScoutResource());
    }
    else if (menu instanceof MultipleUpdateFormDataAction) {
      ((MultipleUpdateFormDataAction) menu).setTypeResolver(new ITypeResolver() {
        @Override
        public IType[] getTypes() {
          return resolveForms();
        }
      });
    }
  }
}
