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

import java.util.Set;

import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.ShowJavaReferencesAction;
import org.eclipse.scout.sdk.ui.action.WellformScoutTypeAction;
import org.eclipse.scout.sdk.ui.action.create.CreateTemplateAction;
import org.eclipse.scout.sdk.ui.action.delete.FormFieldDeleteAction;
import org.eclipse.scout.sdk.ui.action.dto.FormDataUpdateAction;
import org.eclipse.scout.sdk.ui.action.rename.FormFieldRenameAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.util.SdkProperties;

public abstract class AbstractFormFieldNodePage extends AbstractScoutTypePage {

  public AbstractFormFieldNodePage() {
    this(SdkProperties.SUFFIX_FORM_FIELD);
  }

  public AbstractFormFieldNodePage(String readOnlySuffix) {
    super(readOnlySuffix);
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.FormField));
  }

  @Override
  protected String getMethodNameForTranslatedText() {
    return "getConfiguredLabel";
  }

  @Override
  public Set<Class<? extends IScoutHandler>> getSupportedMenuActions() {
    return newSet(ShowJavaReferencesAction.class, FormDataUpdateAction.class, WellformScoutTypeAction.class,
        CreateTemplateAction.class, FormFieldRenameAction.class, FormFieldDeleteAction.class);
  }
}
