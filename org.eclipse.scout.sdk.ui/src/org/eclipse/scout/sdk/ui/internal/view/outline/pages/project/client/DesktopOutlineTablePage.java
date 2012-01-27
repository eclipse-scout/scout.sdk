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

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.action.create.OutlineNewAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.jdt.IJavaResourceChangedListener;
import org.eclipse.scout.sdk.util.jdt.JdtEvent;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.TypeCacheAccessor;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>DesktopOutlineTablePage</h3> ...
 */
public class DesktopOutlineTablePage extends AbstractPage {
  final IType iOutline = TypeUtility.getType(RuntimeClasses.IOutline);
  final String getConfiguredOutlines = "getConfiguredOutlines";
  private IType m_desktopType;
  private P_MethodListener m_methodListener;
  private ICachedTypeHierarchy m_outlineTypeHierarchy;

  public DesktopOutlineTablePage(IPage parentPage, IType desktopType) {
    super.setParent(parentPage);
    setName(Texts.get("OutlineTablePage"));
    m_desktopType = desktopType;
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Outlines));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.DESKTOP_OUTLINE_TABLE_PAGE;
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

  /**
   * @return the desktopType
   */
  public IType getDesktopType() {
    return m_desktopType;
  }

  @Override
  public void unloadPage() {
    if (m_methodListener != null) {
      TypeCacheAccessor.getJavaResourceChangedEmitter().removeMethodChangedListener(getDesktopType(), m_methodListener);
      m_methodListener = null;
    }
    if (m_outlineTypeHierarchy != null) {
      m_outlineTypeHierarchy.removeHierarchyListener(getPageDirtyListener());
      m_outlineTypeHierarchy = null;
    }
    super.unloadPage();
  }

  @Override
  public void loadChildrenImpl() {
    if (m_outlineTypeHierarchy == null) {
      m_outlineTypeHierarchy = TypeUtility.getPrimaryTypeHierarchy(iOutline);
      m_outlineTypeHierarchy.addHierarchyListener(getPageDirtyListener());
    }
    if (m_methodListener == null) {
      m_methodListener = new P_MethodListener();
      TypeCacheAccessor.getJavaResourceChangedEmitter().addMethodChangedListener(getDesktopType(), m_methodListener);
    }
    try {
      IMethod outlinesMethod = TypeUtility.getMethod(getDesktopType(), getConfiguredOutlines);
      if (outlinesMethod != null) {
        IType[] outlineCandidates = ScoutTypeUtility.getTypeOccurenceInMethod(outlinesMethod);
        for (IType candidate : outlineCandidates) {
          if (m_outlineTypeHierarchy.isSubtype(iOutline, candidate)) {
            new OutlineNodePage(this, candidate);
          }
        }
      }
    }
    catch (Exception e) {
      ScoutSdkUi.logError("error during loading children of '" + getClass().getName() + "'", e);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends AbstractScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{OutlineNewAction.class};
  }

  @Override
  public void prepareMenuAction(AbstractScoutHandler menu) {
    ((OutlineNewAction) menu).init(getScoutResource(), getDesktopType());
  }

  private class P_MethodListener implements IJavaResourceChangedListener {
    @Override
    public void handleEvent(JdtEvent event) {
      if (event.getElement().getElementName().equals(getConfiguredOutlines)) {
        markStructureDirty();
      }
    }
  }

}
