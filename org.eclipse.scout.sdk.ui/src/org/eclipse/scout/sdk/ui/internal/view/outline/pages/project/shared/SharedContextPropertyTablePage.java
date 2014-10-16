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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.shared;

import java.util.Set;
import java.util.TreeMap;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.create.SharedContextBeanPropertyNewAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.jdt.AbstractElementChangedListener;
import org.eclipse.scout.sdk.util.type.IMethodFilter;
import org.eclipse.scout.sdk.util.type.IPropertyBean;
import org.eclipse.scout.sdk.util.type.PropertyBean;
import org.eclipse.scout.sdk.util.type.TypeUtility;

/**
 * <h3>SharedContextPropertyTablePage</h3>
 */
public class SharedContextPropertyTablePage extends AbstractPage {
  private final IType m_clientSession;
  private final IType m_serverSession;

  private P_MethodChangedListener m_methodChangedListener;

  public SharedContextPropertyTablePage(IPage parent, IType clientSession, IType serverSession) {
    setParent(parent);
    m_clientSession = clientSession;
    m_serverSession = serverSession;
    setName(Texts.get("SharedContextTablePage", clientSession.getElementName()));
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Variables));
  }

  @Override
  public void unloadPage() {
    if (m_methodChangedListener != null) {
      JavaCore.removeElementChangedListener(m_methodChangedListener);
    }
    super.unloadPage();
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.SHARED_CONTEXT_PROPERTY_TABLE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  @Override
  protected void loadChildrenImpl() {
    if (m_methodChangedListener == null) {
      m_methodChangedListener = new P_MethodChangedListener();
      JavaCore.addElementChangedListener(m_methodChangedListener);
    }
    P_PropertyMethodFilter clientFilter = new P_PropertyMethodFilter();
    TypeUtility.getMethods(getClientSession(), clientFilter, null);
    P_PropertyMethodFilter serverFilter = new P_PropertyMethodFilter();
    TypeUtility.getMethods(getServerSession(), serverFilter, null);
    for (IPropertyBean bean : serverFilter.m_beans.values()) {
      IPropertyBean clientBean = clientFilter.m_beans.get(bean.getBeanName());
      if (clientBean != null) {
        new SharedContextPropertyNodePage(this, clientBean, bean);
      }
    }
  }

  @Override
  public Set<Class<? extends IScoutHandler>> getSupportedMenuActions() {
    return newSet(SharedContextBeanPropertyNewAction.class);
  }

  public IType getClientSession() {
    return m_clientSession;
  }

  public IType getServerSession() {
    return m_serverSession;
  }

  private class P_PropertyMethodFilter implements IMethodFilter {
    TreeMap<String, PropertyBean> m_beans = new TreeMap<String, PropertyBean>();

    @Override
    public boolean accept(IMethod candidate) {
      if (TypeUtility.exists(candidate)) {
        try {
          String source = candidate.getSource();
          if (source != null) {
            if (source.contains("setSharedContextVariable")) {
              String propName = candidate.getElementName();
              propName = propName.replaceFirst("^set", "");
              PropertyBean bean = m_beans.get(propName);
              if (bean == null) {
                bean = new PropertyBean(candidate.getDeclaringType(), propName);
                m_beans.put(propName, bean);
              }
              bean.setWriteMethod(candidate);
            }
            else if (source.contains("getSharedContextVariable")) {
              String propName = candidate.getElementName();
              propName = propName.replaceFirst("^(get|is)", "");
              PropertyBean bean = m_beans.get(propName);
              if (bean == null) {
                bean = new PropertyBean(candidate.getDeclaringType(), propName);
                m_beans.put(propName, bean);
              }
              bean.setReadMethod(candidate);
            }
          }
        }
        catch (JavaModelException e) {
          ScoutSdkUi.logError("could not parse method '" + candidate.getElementName() + "' on type '" + candidate.getDeclaringType().getFullyQualifiedName() + "'.", e);
        }
      }
      return false;
    }
  }

  private class P_MethodChangedListener extends AbstractElementChangedListener {
    @Override
    protected boolean visit(int kind, int flags, IJavaElement e, CompilationUnit ast) {
      if (e != null && e.getElementType() == IJavaElement.METHOD) {
        IType declaringType = ((IMethod) e).getDeclaringType();
        if (declaringType.equals(getClientSession()) || declaringType.equals(getServerSession())) {
          markStructureDirty();
          return false;
        }
      }
      return super.visit(kind, flags, e, ast);
    }
  } // end class P_MethodChangedListener
}
