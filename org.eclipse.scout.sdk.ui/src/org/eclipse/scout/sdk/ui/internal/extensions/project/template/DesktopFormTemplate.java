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
package org.eclipse.scout.sdk.ui.internal.extensions.project.template;

import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.project.CreateClientPluginOperation;
import org.eclipse.scout.sdk.operation.project.template.SingleFormTemplateOperation;
import org.eclipse.scout.sdk.ui.extensions.project.template.IProjectTemplate;
import org.eclipse.scout.sdk.ui.wizard.project.IScoutProjectWizard;

public class DesktopFormTemplate implements IProjectTemplate {
  @Override
  public String getText() {
    return Texts.get("ApplicationWithASingleForm");
  }

  @Override
  public String getDescription() {
    return Texts.get("SingleFormTemplateDesc");
  }

  @Override
  public boolean isApplicable(IScoutProjectWizard wizard) {
    return wizard.getProjectWizardPage().isBundleNodesSelected(CreateClientPluginOperation.BUNDLE_ID);
  }

  @Override
  public String getId() {
    return SingleFormTemplateOperation.TEMPLATE_ID;
  }
}
