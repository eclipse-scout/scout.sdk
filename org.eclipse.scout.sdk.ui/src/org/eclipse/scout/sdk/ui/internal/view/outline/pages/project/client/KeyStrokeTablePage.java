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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.action.WizardAction;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.InnerTypePageDirtyListener;
import org.eclipse.scout.sdk.ui.wizard.keystroke.KeyStrokeNewWizard;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.SdkTypeUtility;

/**
 * <h3>KeyStrokesTablePage</h3> ...
 */
public class KeyStrokeTablePage extends AbstractPage {
  final IType iKeyStrokeType = ScoutSdk.getType(RuntimeClasses.IKeyStroke);

  private final IType m_declaringType;

  private InnerTypePageDirtyListener m_keystrokeChangedListener;

  public KeyStrokeTablePage(IPage parentPage, IType declaringType) {
    m_declaringType = declaringType;
    setName(Texts.get("KeyStrokesTablePage"));
    setParent(parentPage);
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.KEY_STROKE_TABLE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  /**
   * client bundle
   */
  @Override
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  @Override
  public void unloadPage() {
    super.unloadPage();
    if (m_keystrokeChangedListener != null) {
      ScoutSdk.removeInnerTypeChangedListener(getDeclaringType(), m_keystrokeChangedListener);
      m_keystrokeChangedListener = null;
    }
  }

  @Override
  public void loadChildrenImpl() {
    if (m_keystrokeChangedListener == null) {
      m_keystrokeChangedListener = new InnerTypePageDirtyListener(this, iKeyStrokeType);
      ScoutSdk.addInnerTypeChangedListener(getDeclaringType(), m_keystrokeChangedListener);
    }
    for (IType keyStroke : SdkTypeUtility.getKeyStrokes(getDeclaringType())) {
      KeyStrokeNodePage childPage = new KeyStrokeNodePage();
      childPage.setParent(this);
      childPage.setType(keyStroke);
    }

  }

  @Override
  public Action createNewAction() {
    return new WizardAction(Texts.get("Action_newTypeX", "Key stroke"), JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_CLASS), new KeyStrokeNewWizard(getDeclaringType()));

  }

  public IType getDeclaringType() {
    return m_declaringType;
  }

}
