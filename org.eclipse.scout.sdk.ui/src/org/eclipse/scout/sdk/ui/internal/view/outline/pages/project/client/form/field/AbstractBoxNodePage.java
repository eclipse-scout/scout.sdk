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

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.ShowJavaReferencesAction;
import org.eclipse.scout.sdk.ui.action.create.CreateTemplateAction;
import org.eclipse.scout.sdk.ui.action.create.FormFieldNewAction;
import org.eclipse.scout.sdk.ui.action.delete.FormFieldDeleteAction;
import org.eclipse.scout.sdk.ui.action.rename.FormFieldRenameAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.extensions.FormFieldExtensionPoint;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.KeyStrokeTablePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.ITypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.InnerTypeOrderChangedPageDirtyListener;
import org.eclipse.scout.sdk.ui.view.outline.pages.InnerTypePageDirtyListener;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeComparators;

public abstract class AbstractBoxNodePage extends AbstractScoutTypePage {
  private InnerTypePageDirtyListener m_innerTypeListener;
  private InnerTypeOrderChangedPageDirtyListener m_orderChangedListener;

  public AbstractBoxNodePage() {
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Groupbox));
  }

  @Override
  public void unloadPage() {
    if (m_innerTypeListener != null) {
      ScoutSdkCore.getJavaResourceChangedEmitter().removeInnerTypeChangedListener(getType(), m_innerTypeListener);
      m_innerTypeListener = null;
    }
    if (m_orderChangedListener != null) {
      ScoutSdkCore.getJavaResourceChangedEmitter().removeJavaResourceChangedListener(m_orderChangedListener);
      m_orderChangedListener = null;
    }
    super.unloadPage();
  }

  @Override
  protected String getMethodNameForTranslatedText() {
    return "getConfiguredLabel";
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  @Override
  protected void loadChildrenImpl() {
    IType iFormField = TypeUtility.getType(RuntimeClasses.IFormField);

    if (m_innerTypeListener == null) {
      m_innerTypeListener = new InnerTypePageDirtyListener(this, iFormField);
      ScoutSdkCore.getJavaResourceChangedEmitter().addInnerTypeChangedListener(getType(), m_innerTypeListener);
    }
    if (m_orderChangedListener == null) {
      m_orderChangedListener = new InnerTypeOrderChangedPageDirtyListener(this, iFormField, getType());
      ScoutSdkCore.getJavaResourceChangedEmitter().addJavaResourceChangedListener(m_orderChangedListener);
    }

    new KeyStrokeTablePage(this, getType());

    ITypeHierarchy hierarchy = TypeUtility.getLocalTypeHierarchy(getType());
    IType[] allSubtypes = TypeUtility.getInnerTypes(getType(), TypeFilters.getSubtypeFilter(iFormField, hierarchy), ScoutTypeComparators.getOrderAnnotationComparator());
    for (IType t : allSubtypes) {
      ITypePage nodePage = (ITypePage) FormFieldExtensionPoint.createNodePage(t, hierarchy);
      if (nodePage != null) {
        nodePage.setParent(this);
        nodePage.setType(t);
      }
    }
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    super.prepareMenuAction(menu);
    if (menu instanceof FormFieldRenameAction) {
      FormFieldRenameAction a = (FormFieldRenameAction) menu;
      a.setFormField(getType());
      a.setOldName(getType().getElementName());
      a.setReadOnlySuffix(SdkProperties.SUFFIX_BOX);
    }
    else if (menu instanceof FormFieldNewAction) {
      ((FormFieldNewAction) menu).setType(getType());
    }
    else if (menu instanceof FormFieldDeleteAction) {
      ((FormFieldDeleteAction) menu).addFormFieldType(getType());
    }
    else if (menu instanceof CreateTemplateAction) {
      CreateTemplateAction action = (CreateTemplateAction) menu;
      action.setPage(this);
      action.setType(getType());
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{FormFieldRenameAction.class, ShowJavaReferencesAction.class, FormFieldNewAction.class,
        FormFieldDeleteAction.class, CreateTemplateAction.class};
  }
}
