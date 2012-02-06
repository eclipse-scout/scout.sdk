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
package org.eclipse.scout.sdk.ui.view.outline.pages.project.client.ui.form.field;

import org.eclipse.scout.sdk.ui.action.FormDataUpdateAction;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.ShowJavaReferencesAction;
import org.eclipse.scout.sdk.ui.action.create.CreateTemplateAction;
import org.eclipse.scout.sdk.ui.action.delete.FormFieldDeleteAction;
import org.eclipse.scout.sdk.ui.action.rename.FormFieldRenameAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.util.SdkProperties;

public abstract class AbstractFormFieldNodePage extends AbstractScoutTypePage {

  public AbstractFormFieldNodePage() {
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.FormField));
  }

  @Override
  protected String getMethodNameForTranslatedText() {
    return "getConfiguredLabel";
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{ShowJavaReferencesAction.class, FormDataUpdateAction.class,
        CreateTemplateAction.class, FormFieldRenameAction.class, FormFieldDeleteAction.class};
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    super.prepareMenuAction(menu);
    if (menu instanceof FormDataUpdateAction) {
      ((FormDataUpdateAction) menu).setType(getType());
    }
    else if (menu instanceof CreateTemplateAction) {
      CreateTemplateAction action = (CreateTemplateAction) menu;
      action.setPage(this);
      action.setType(getType());
    }
    else if (menu instanceof FormFieldRenameAction) {
      FormFieldRenameAction a = (FormFieldRenameAction) menu;
      a.setFormField(getType());
      a.setOldName(getType().getElementName());
      a.setReadOnlySuffix(SdkProperties.SUFFIX_FORM_FIELD);
    }
    else if (menu instanceof FormFieldDeleteAction) {
      FormFieldDeleteAction action = (FormFieldDeleteAction) menu;
      action.addFormFieldType(getType());
    }
  }
}
