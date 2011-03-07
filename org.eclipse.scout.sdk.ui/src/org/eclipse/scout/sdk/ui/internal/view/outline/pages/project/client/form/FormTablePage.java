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

import java.util.HashSet;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.util.wellform.WellformFormsOperation;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.MultipleUpdateFormDataAction;
import org.eclipse.scout.sdk.ui.action.WellformAction;
import org.eclipse.scout.sdk.ui.action.WizardAction;
import org.eclipse.scout.sdk.ui.action.validation.ITypeResolver;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.wizard.form.FormNewWizard;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ITypeFilter;
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.typecache.ICachedTypeHierarchy;

/**
 * <h3>FormTablePage</h3> ...
 */
public class FormTablePage extends AbstractPage {

  final IType iForm = ScoutSdk.getType(RuntimeClasses.IForm);
  final IType iSearchForm = ScoutSdk.getType(RuntimeClasses.ISearchForm);

  private ICachedTypeHierarchy m_formHierarchy;

  public FormTablePage(AbstractPage parent) {
    setName(Texts.get("FormTablePage"));
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
      m_formHierarchy = ScoutSdk.getPrimaryTypeHierarchy(iForm);
      m_formHierarchy.addHierarchyListener(getPageDirtyListener());
    }
    IType[] searchForms = m_formHierarchy.getAllSubtypes(iSearchForm, TypeFilters.getClassesInProject(getScoutResource().getJavaProject()));
    IType[] allSubtypes = m_formHierarchy.getAllSubtypes(iForm, new P_FormFilter(searchForms, getScoutResource().getJavaProject()), TypeComparators.getTypeNameComparator());
    return allSubtypes;
  }

  @Override
  public void fillContextMenu(IMenuManager manager) {
    super.fillContextMenu(manager);
    manager.add(new Separator());
    manager.add(new WellformAction(getOutlineView().getSite().getShell(), "Wellform all forms...", new WellformFormsOperation(getScoutResource())));
    manager.add(new MultipleUpdateFormDataAction(new ITypeResolver() {
      @Override
      public IType[] getTypes() {
        return resolveForms();
      }
    }));
  }

  @Override
  public Action createNewAction() {
    return new WizardAction(Texts.get("Action_newTypeX", "Form"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.FormAdd),
        new FormNewWizard(getScoutResource()));
  }

  private class P_FormFilter implements ITypeFilter {
    private HashSet<String> m_searchForms = new HashSet<String>();
    private ITypeFilter m_classesInProjectFilter;

    public P_FormFilter(IType[] searchForms, IJavaProject javaProject) {
      m_classesInProjectFilter = TypeFilters.getClassesInProject(javaProject);
      for (IType t : searchForms) {
        m_searchForms.add(t.getHandleIdentifier());
      }
    }

    @Override
    public boolean accept(IType type) {
      if (!m_searchForms.contains(type.getHandleIdentifier())) {
        return m_classesInProjectFilter.accept(type);
      }
      return false;
    }
  }

}
