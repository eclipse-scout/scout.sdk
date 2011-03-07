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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form.field;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.MultipleUpdateFormDataAction;
import org.eclipse.scout.sdk.ui.action.validation.ITypeResolver;
import org.eclipse.scout.sdk.ui.internal.extensions.FormFieldExtensionPoint;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.ITypePage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ITypeFilter;
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.typecache.ICachedTypeHierarchy;

/**
 * <h3>{@link FormFieldTemplateTablePage}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 11.09.2010
 */
public class FormFieldTemplateTablePage extends AbstractPage {

  final IType iFormField = ScoutSdk.getType(RuntimeClasses.IFormField);
  private ICachedTypeHierarchy m_formFieldHierarchy;

  public FormFieldTemplateTablePage(IPage parent) {
    setParent(parent);
    setName("Form Fields");
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.FormFieldTemplate));
  }

  @Override
  public void refresh(boolean clearCache) {
    if (clearCache && m_formFieldHierarchy != null) {
      m_formFieldHierarchy.invalidate();
    }
    super.refresh(clearCache);
  }

  @Override
  public void unloadPage() {
    if (m_formFieldHierarchy != null) {
      m_formFieldHierarchy.removeHierarchyListener(getPageDirtyListener());
      m_formFieldHierarchy = null;
    }
    super.unloadPage();
  }

  @Override
  protected void loadChildrenImpl() {
    for (IType fieldTemplate : resolveFormFieldTemplates()) {
      ITypePage nodePage = (ITypePage) FormFieldExtensionPoint.createNodePage(fieldTemplate, m_formFieldHierarchy);
      nodePage.setParent(this);
      nodePage.setType(fieldTemplate);
    }
  }

  protected IType[] resolveFormFieldTemplates() {
    if (m_formFieldHierarchy == null) {
      m_formFieldHierarchy = ScoutSdk.getPrimaryTypeHierarchy(iFormField);
      m_formFieldHierarchy.addHierarchyListener(getPageDirtyListener());
    }
    ITypeFilter filter = TypeFilters.getMultiTypeFilter(
        TypeFilters.getInScoutBundles(getScoutResource()),
        TypeFilters.getFlagsFilter(Flags.AccAbstract | Flags.AccPublic)
        );
    IType[] allSubtypes = m_formFieldHierarchy.getAllSubtypes(iFormField, filter, TypeComparators.getTypeNameComparator());
    return allSubtypes;
  }

  @Override
  public void fillContextMenu(IMenuManager manager) {
    super.fillContextMenu(manager);
    manager.add(new MultipleUpdateFormDataAction(new ITypeResolver() {
      @Override
      public IType[] getTypes() {
        return resolveFormFieldTemplates();
      }
    }));
  }

  /**
   * @return the client bundle
   */
  @Override
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.FORM_FIELD_TEMPLATE_TABLE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return true;
  }

}
