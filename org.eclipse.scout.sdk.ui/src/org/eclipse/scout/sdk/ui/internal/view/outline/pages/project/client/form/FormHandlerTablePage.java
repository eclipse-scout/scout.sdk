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

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.action.WizardAction;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.InnerTypePageDirtyListener;
import org.eclipse.scout.sdk.ui.wizard.form.handler.FormHandlerNewWizard;
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;

/**
 * <h3>FormHandlersTablePage</h3> ...
 */
public class FormHandlerTablePage extends AbstractPage {

  final IType iFormHandler = ScoutSdk.getType(RuntimeClasses.IFormHandler);
  private InnerTypePageDirtyListener m_innerTypeListener;

  private final IType m_formType;

  public FormHandlerTablePage(AbstractPage parent, IType formType) {
    m_formType = formType;
    setParent(parent);
    setName(Texts.get("FormHandlersTablePage"));
  }

  @Override
  public void unloadPage() {
    if (m_innerTypeListener != null) {
      ScoutSdk.removeInnerTypeChangedListener(getFormType(), m_innerTypeListener);
      m_innerTypeListener = null;
    }
    super.unloadPage();
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.FORM_HANDLER_TABLE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  @Override
  public void loadChildrenImpl() {
    if (m_innerTypeListener == null) {
      m_innerTypeListener = new InnerTypePageDirtyListener(this, iFormHandler);
      ;
      ScoutSdk.addInnerTypeChangedListener(getFormType(), m_innerTypeListener);
    }
    for (IType formHandlerType : TypeUtility.getInnerTypesOrdered(getFormType(), iFormHandler, TypeComparators.getTypeNameComparator())) {
      new FormHandlerNodePage(this, formHandlerType);
    }

  }

  @Override
  public Action createNewAction() {

    return new WizardAction(Texts.get("Action_newTypeX", "Handler"), JavaUI.getSharedImages().getImageDescriptor(
        ISharedImages.IMG_OBJS_CLASS), new FormHandlerNewWizard(getFormType()));
  }

  public IType getFormType() {
    return m_formType;
  }

}
