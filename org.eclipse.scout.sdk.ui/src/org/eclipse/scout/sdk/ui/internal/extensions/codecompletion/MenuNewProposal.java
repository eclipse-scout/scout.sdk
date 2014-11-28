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

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.ui.action.create.MenuNewAction;
import org.eclipse.scout.sdk.ui.extensions.executor.ExecutorExtensionPoint;
import org.eclipse.scout.sdk.ui.extensions.executor.IExecutor;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.SdkIcons;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.swt.graphics.Image;

/**
 * <h3>{@link MenuNewProposal}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 11.07.2014
 */
public class MenuNewProposal extends AbstractSdkWizardProposal {

  MenuNewProposal(IType declaringType, int startOffset) {
    super(declaringType, TypeUtility.getType(IRuntimeClasses.IMenu), startOffset);
  }

  @Override
  public String getDisplayString() {
    return Texts.get("Action_newTypeX", "Menu");
  }

  @Override
  public Image getImage() {
    return ScoutSdkUi.getImage(SdkIcons.MenuAdd);
  }

  @Override
  protected IExecutor createExecutor() {
    return ExecutorExtensionPoint.getExecutorFor(MenuNewAction.class.getName());
  }
}
