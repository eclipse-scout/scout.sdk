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

import java.util.TreeMap;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.jdt.IJavaResourceChangedListener;
import org.eclipse.scout.sdk.jdt.JdtEvent;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.AxisWebServiceProviderPublishAction;
import org.eclipse.scout.sdk.ui.action.WizardAction;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.ServiceOperationNodePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.wizard.services.ServiceOperationNewWizard;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.MethodComparators;
import org.eclipse.scout.sdk.workspace.type.MethodFilters;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;

/**
 * Page representing a service implementation
 */
public abstract class AbstractServiceNodePage extends AbstractScoutTypePage {

  private final IType m_interfaceType;

  private P_ServiceMethodsListener m_serviceMethodListener;

  /**
   * server bundle
   */
  @Override
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  public AbstractServiceNodePage(AbstractPage parent, IType type, IType interfaceType) {
    setParent(parent);
    setType(type);
    m_interfaceType = interfaceType;
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Service));

  }

  @Override
  public void unloadPage() {
    if (m_serviceMethodListener != null) {
      ScoutSdk.removeMethodChangedListener(getType(), m_serviceMethodListener);
      m_serviceMethodListener = null;
    }
    super.unloadPage();
  }

  @Override
  public void loadChildrenImpl() {
    if (m_interfaceType == null) {
      return;
    }
    if (m_serviceMethodListener == null) {
      m_serviceMethodListener = new P_ServiceMethodsListener();
      ScoutSdk.addMethodChangedListener(getType(), m_serviceMethodListener);
    }
    IMethod[] serviceMethods = TypeUtility.getMethods(getType(), MethodFilters.getFlagsFilter(Flags.AccPublic), MethodComparators.getNameComparator());

    TreeMap<String, IMethod> interfaceMethodsMap = new TreeMap<String, IMethod>();
    if (TypeUtility.exists(getInterfaceType())) {
      for (IMethod m : TypeUtility.getMethods(getType(), MethodFilters.getFlagsFilter(Flags.AccPublic | Flags.AccDefault), MethodComparators.getNameComparator())) {
        interfaceMethodsMap.put(m.getElementName(), m);
      }
    }

    for (IMethod implMethod : serviceMethods) {
      new ServiceOperationNodePage(this, interfaceMethodsMap.get(implMethod.getElementName()), implMethod);
    }

  }

  @Override
  public Action createDeleteAction() {
    Action deleteAction = super.createDeleteAction();
    if (deleteAction != null) {
      deleteAction.setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ServiceRemove));
    }
    return deleteAction;
  }

  @Override
  public void fillContextMenu(IMenuManager manager) {
    super.fillContextMenu(manager);
    manager.add(new WizardAction("New Service Operation...", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ServiceOperationAdd), new ServiceOperationNewWizard(getInterfaceType(), new IType[]{getType()})));
    manager.add(new Separator());
    manager.add(new AxisWebServiceProviderPublishAction(getOutlineView().getSite().getShell(), getType(), getInterfaceType()));
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
