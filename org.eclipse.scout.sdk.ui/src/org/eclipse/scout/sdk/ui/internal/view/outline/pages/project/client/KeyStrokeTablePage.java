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

import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.create.KeyStrokeNewAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.InnerTypePageDirtyListener;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>KeyStrokesTablePage</h3>
 */
public class KeyStrokeTablePage extends AbstractPage {
  private final IType m_declaringType;
  private InnerTypePageDirtyListener m_keystrokeChangedListener;

  public KeyStrokeTablePage(IPage parentPage, IType declaringType) {
    m_declaringType = declaringType;
    setName(Texts.get("KeyStrokesTablePage"));
    setParent(parentPage);
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Keystrokes));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.KEY_STROKE_TABLE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  @Override
  public void unloadPage() {
    super.unloadPage();
    if (m_keystrokeChangedListener != null) {
      ScoutSdkCore.getJavaResourceChangedEmitter().removeInnerTypeChangedListener(getDeclaringType(), m_keystrokeChangedListener);
      m_keystrokeChangedListener = null;
    }
  }

  @Override
  protected void loadChildrenImpl() {
    if (m_keystrokeChangedListener == null) {
      IType iKeyStrokeType = TypeUtility.getType(IRuntimeClasses.IKeyStroke);
      m_keystrokeChangedListener = new InnerTypePageDirtyListener(this, iKeyStrokeType);
      ScoutSdkCore.getJavaResourceChangedEmitter().addInnerTypeChangedListener(getDeclaringType(), m_keystrokeChangedListener);
    }
    for (IType keyStroke : ScoutTypeUtility.getKeyStrokes(getDeclaringType())) {
      KeyStrokeNodePage childPage = new KeyStrokeNodePage();
      childPage.setParent(this);
      childPage.setType(keyStroke);
    }
  }

  @Override
  public Set<Class<? extends IScoutHandler>> getSupportedMenuActions() {
    return newSet(KeyStrokeNewAction.class);
  }

  public IType getDeclaringType() {
    return m_declaringType;
  }
}
