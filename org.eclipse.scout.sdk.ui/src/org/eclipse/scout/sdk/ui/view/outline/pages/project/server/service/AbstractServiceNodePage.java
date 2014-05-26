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
package org.eclipse.scout.sdk.ui.view.outline.pages.project.server.service;

import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.operation.ITypeResolver;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.ShowJavaReferencesAction;
import org.eclipse.scout.sdk.ui.action.create.ServiceOperationNewAction;
import org.eclipse.scout.sdk.ui.action.delete.ServiceDeleteAction;
import org.eclipse.scout.sdk.ui.action.rename.ServiceRenameAction;
import org.eclipse.scout.sdk.ui.action.validation.FormDataSqlBindingValidateAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.ServiceOperationNodePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.util.jdt.IJavaResourceChangedListener;
import org.eclipse.scout.sdk.util.jdt.JdtEvent;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.MethodComparators;
import org.eclipse.scout.sdk.util.type.MethodFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;

/**
 * Page representing a service implementation
 */
public abstract class AbstractServiceNodePage extends AbstractScoutTypePage {

  private final IType m_interfaceType;
  private final String m_readOnlySuffix;
  private P_ServiceMethodsListener m_serviceMethodListener;

  public AbstractServiceNodePage(AbstractPage parent, IType type, IType interfaceType, String readOnlySuffix) {
    setParent(parent);
    setType(type);
    m_interfaceType = interfaceType;
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Service));
    m_readOnlySuffix = readOnlySuffix;
  }

  @Override
  public void unloadPage() {
    if (m_serviceMethodListener != null) {
      ScoutSdkCore.getJavaResourceChangedEmitter().removeMethodChangedListener(getType(), m_serviceMethodListener);
      m_serviceMethodListener = null;
    }
    super.unloadPage();
  }

  @Override
  protected void loadChildrenImpl() {
    if (m_interfaceType == null) {
      return;
    }
    if (m_serviceMethodListener == null) {
      m_serviceMethodListener = new P_ServiceMethodsListener();
      ScoutSdkCore.getJavaResourceChangedEmitter().addMethodChangedListener(getType(), m_serviceMethodListener);
    }
    Set<IMethod> serviceMethods = TypeUtility.getMethods(getType(), MethodFilters.getFlagsFilter(Flags.AccPublic), MethodComparators.getNameComparator());

    try {
      TreeMap<String, IMethod> interfaceMethodsMap = new TreeMap<String, IMethod>();
      if (TypeUtility.exists(getInterfaceType())) {
        for (IMethod m : TypeUtility.getMethods(getInterfaceType())) {
          interfaceMethodsMap.put(SignatureUtility.getMethodIdentifier(m), m);
        }
      }

      for (IMethod implMethod : serviceMethods) {
        new ServiceOperationNodePage(this, interfaceMethodsMap.get(SignatureUtility.getMethodIdentifier(implMethod)), implMethod);
      }
    }
    catch (CoreException e) {
      ScoutSdkUi.logError(e);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{ServiceRenameAction.class, ShowJavaReferencesAction.class, FormDataSqlBindingValidateAction.class,
        ServiceOperationNewAction.class, ServiceDeleteAction.class};
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    super.prepareMenuAction(menu);
    if (menu instanceof ServiceRenameAction) {
      ServiceRenameAction sra = (ServiceRenameAction) menu;
      sra.setServiceImplementation(getType());
      sra.setServiceInterface(getInterfaceType());
      sra.setReadOnlySuffix(m_readOnlySuffix);
      sra.setOldName(getType().getElementName());
    }
    else if (menu instanceof FormDataSqlBindingValidateAction) {
      ((FormDataSqlBindingValidateAction) menu).setTyperesolver(new ITypeResolver() {
        @Override
        public Set<IType> getTypes() {
          return CollectionUtility.hashSet(getType());
        }
      });
    }
    else if (menu instanceof ServiceOperationNewAction) {
      ((ServiceOperationNewAction) menu).init(getInterfaceType(), getType());
    }
    else if (menu instanceof ServiceDeleteAction) {
      ServiceDeleteAction action = (ServiceDeleteAction) menu;
      action.setServiceImplementation(getType());
      action.setServiceInterface(getInterfaceType());
    }
  }

  public IType getInterfaceType() {
    return m_interfaceType;
  }

  private class P_ServiceMethodsListener implements IJavaResourceChangedListener {
    @Override
    public void handleEvent(JdtEvent event) {
      markStructureDirty();
    }
  }
}
