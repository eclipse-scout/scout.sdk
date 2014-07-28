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

import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.operation.ITypeResolver;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.dto.TypeResolverFormDataAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.extensions.FormFieldExtensionPoint;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.ITypePage;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchyResult;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeFilters;

/**
 * <h3>{@link FormFieldTemplateTablePage}</h3>
 *
 * @author Andreas Hoegger
 * @since 1.0.8 11.09.2010
 */
public class FormFieldTemplateTablePage extends AbstractPage {
  private ICachedTypeHierarchyResult m_formFieldHierarchy;

  public FormFieldTemplateTablePage(IPage parent) {
    setParent(parent);
    setName(Texts.get("FormFields"));
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
    Set<IType> resolveFormFieldTemplates = resolveFormFieldTemplates();
    ITypeHierarchy localTypeHierarchy = TypeUtility.getLocalTypeHierarchy(resolveFormFieldTemplates);
    for (IType fieldTemplate : resolveFormFieldTemplates) {
      ITypePage nodePage = (ITypePage) FormFieldExtensionPoint.createNodePage(fieldTemplate, localTypeHierarchy);
      if (nodePage != null) {
        nodePage.setParent(this);
        nodePage.setType(fieldTemplate);
      }
    }
  }

  protected Set<IType> resolveFormFieldTemplates() {
    if (m_formFieldHierarchy == null) {
      IType iFormField = TypeUtility.getType(IRuntimeClasses.IFormField);
      IScoutBundle workspaceBundle = getScoutBundle().getParentBundle(ScoutBundleFilters.getWorkspaceBundlesFilter(), true);
      m_formFieldHierarchy = TypeUtility.getAbstractTypesOnClasspathHierarchy(iFormField, workspaceBundle.getJavaProject());
      m_formFieldHierarchy.addHierarchyListener(getPageDirtyListener());
    }
    ITypeFilter filter = TypeFilters.getMultiTypeFilterAnd(TypeFilters.getPrimaryTypeFilter(), ScoutTypeFilters.getInScoutBundles(getScoutBundle()));
    return m_formFieldHierarchy.getAllTypes(filter, TypeComparators.getTypeNameComparator());
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    if (menu instanceof TypeResolverFormDataAction) {
      ((TypeResolverFormDataAction) menu).init(new ITypeResolver() {
        @Override
        public Set<IType> getTypes() {
          return resolveFormFieldTemplates();
        }
      }, getScoutBundle());
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{TypeResolverFormDataAction.class};
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
