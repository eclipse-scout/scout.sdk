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
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.MultipleUpdateFormDataAction;
import org.eclipse.scout.sdk.ui.action.validation.ITypeResolver;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form.FormNodePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeFilters;

/**
 * <h3>{@link FormTemplateTablePage}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 11.09.2010
 */
public class FormTemplateTablePage extends AbstractPage {
  private ICachedTypeHierarchy m_formHierarchy;

  public FormTemplateTablePage(IPage parent) {
    setParent(parent);
    setName(Texts.get("FormTablePage"));
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.FormTemplate));
  }

  @Override
  public void refresh(boolean clearCache) {
    if (clearCache && m_formHierarchy != null) {
      m_formHierarchy.invalidate();
    }
    super.refresh(clearCache);
  }

  @Override
  public void unloadPage() {
    if (m_formHierarchy != null) {
      m_formHierarchy.removeHierarchyListener(getPageDirtyListener());
      m_formHierarchy = null;
    }
    super.unloadPage();
  }

  @Override
  protected void loadChildrenImpl() {
    for (IType formTemplate : resolveFormTemplates()) {
      new FormNodePage(this, formTemplate);
    }
  }

  protected IType[] resolveFormTemplates() {
    IType iForm = TypeUtility.getType(RuntimeClasses.IForm);

    if (m_formHierarchy == null) {
      m_formHierarchy = TypeUtility.getPrimaryTypeHierarchy(iForm);
      m_formHierarchy.addHierarchyListener(getPageDirtyListener());
    }
    ITypeFilter filter = TypeFilters.getMultiTypeFilter(
        ScoutTypeFilters.getInScoutBundles(getScoutBundle()),
        TypeFilters.getFlagsFilter(Flags.AccAbstract | Flags.AccPublic)
        );
    IType[] allSubtypes = m_formHierarchy.getAllSubtypes(iForm, filter, TypeComparators.getTypeNameComparator());
    return allSubtypes;
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    if (menu instanceof MultipleUpdateFormDataAction) {
      ((MultipleUpdateFormDataAction) menu).init(new ITypeResolver() {
        @Override
        public IType[] getTypes() {
          return resolveFormTemplates();
        }
      }, getScoutBundle());
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{MultipleUpdateFormDataAction.class};
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.FORM_TEMPLATE_TABLE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return true;
  }
}
