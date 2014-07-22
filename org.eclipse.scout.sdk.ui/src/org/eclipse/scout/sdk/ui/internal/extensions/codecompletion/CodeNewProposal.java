/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.internal.extensions.codecompletion;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.ui.fields.proposal.SiblingProposal;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.SdkIcons;
import org.eclipse.scout.sdk.ui.wizard.IWorkspaceWizard;
import org.eclipse.scout.sdk.ui.wizard.code.CodeNewWizard;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.swt.graphics.Image;

/**
 * <h3>{@link CodeNewProposal}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 25.10.2013
 */
public class CodeNewProposal extends AbstractSdkWizardProposal {

  CodeNewProposal(IType declaringType) {
    super(declaringType, TypeUtility.getType(IRuntimeClasses.ICode));
  }

  @Override
  public String getDisplayString() {
    return Texts.get("Action_newTypeX", "Code");
  }

  @Override
  public Image getImage() {
    return ScoutSdkUi.getImage(SdkIcons.CodeAdd);
  }

  @Override
  protected IWorkspaceWizard createWizard(IJavaElement sibling) {
    CodeNewWizard wizard = new CodeNewWizard();
    wizard.initWizard(getDeclaringType());
    if (TypeUtility.exists(sibling)) {
      wizard.setSibling(new SiblingProposal(sibling));
    }
    return wizard;
  }
}