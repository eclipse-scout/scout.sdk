/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.swt.view.pages;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.ITypeHierarchyChangedListener;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsIcons;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsRuntimeClasses;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.swt.action.SessionFactoryNewAction;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;

public class SessionFactoryTablePage extends AbstractPage {

  private ITypeHierarchy m_hierarchy;

  private ITypeHierarchyChangedListener m_hierarchyChangedListener;

  public SessionFactoryTablePage(IPage parent) {
    setParent(parent);
    setImageDescriptor(JaxWsSdk.getImageDescriptor(JaxWsIcons.SessionFactoryFolder));
    setName(Texts.get("SessionFactories1"));

    try {
      m_hierarchy = JaxWsRuntimeClasses.IServerSessionFactory.newTypeHierarchy(new NullProgressMonitor());
      m_hierarchyChangedListener = new P_TypeHierarchyChangedListener();
      m_hierarchy.addTypeHierarchyChangedListener(m_hierarchyChangedListener);
    }
    catch (JavaModelException e) {
      JaxWsSdk.logError(e);
    }
  }

  @Override
  public String getPageId() {
    return IJaxWsPageConstants.SESSION_FACTORY_TABLE_PAGE;
  }

  @Override
  public void unloadPage() {
    if (m_hierarchy != null && m_hierarchyChangedListener != null) {
      m_hierarchy.removeTypeHierarchyChangedListener(m_hierarchyChangedListener);
    }
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  @Override
  public void refresh(boolean clearCache) {
    if (clearCache) {
      try {
        if (m_hierarchy == null) {
          m_hierarchy = JaxWsRuntimeClasses.IServerSessionFactory.newTypeHierarchy(new NullProgressMonitor());
          m_hierarchyChangedListener = new P_TypeHierarchyChangedListener();
          m_hierarchy.addTypeHierarchyChangedListener(m_hierarchyChangedListener);
        }
        else {
          m_hierarchy.refresh(new NullProgressMonitor());
        }
      }
      catch (JavaModelException e) {
        JaxWsSdk.logError(e);
      }
    }
    super.refresh(clearCache);
  }

  @Override
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  @Override
  public void prepareMenuAction(AbstractScoutHandler menu) {
    if (menu instanceof SessionFactoryNewAction) {
      ((SessionFactoryNewAction) menu).init(getScoutResource());
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends AbstractScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{SessionFactoryNewAction.class};
  }

  @Override
  protected void loadChildrenImpl() {
    if (m_hierarchy == null) {
      return;
    }

    // Scout SDK hierarchy (IPrimaryTypeTypeHierarchy) cannot be used as type creation events are not propagates because they are created by JDT NewClassWizardPage.
    List<IType> types = new ArrayList<IType>();
    try {
      for (IType type : m_hierarchy.getAllSubtypes(JaxWsRuntimeClasses.IServerSessionFactory)) {
        if (TypeUtility.isOnClasspath(type, getScoutResource().getJavaProject()) &&
            !type.isInterface() && !Flags.isAbstract(type.getFlags()) &&
            !Signature.getQualifier(type.getFullyQualifiedName()).contains("internal")) {
          types.add(type);
        }
      }
    }
    catch (JavaModelException e) {
      JaxWsSdk.logError(e);
    }
    JaxWsSdkUtility.sortTypesByName(types, true);

    for (IType type : types) {
      new SessionFactoryNodePage(this, type);
    }
  }

  private class P_TypeHierarchyChangedListener implements ITypeHierarchyChangedListener {

    @Override
    public void typeHierarchyChanged(ITypeHierarchy typeHierarchy) {
      try {
        m_hierarchy.refresh(new NullProgressMonitor());
        markStructureDirty();
      }
      catch (JavaModelException e) {
        JaxWsSdk.logError(e);
      }
    }
  }
}
