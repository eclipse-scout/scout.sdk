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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.page;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jdt.listener.ElementChangedListenerEx;
import org.eclipse.scout.sdk.operation.util.TypeDeleteOperation;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.WizardAction;
import org.eclipse.scout.sdk.ui.action.delete.DeleteAction;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.page.childpage.TablePageChildPageTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.table.TableNodePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.basic.beanproperty.BeanPropertyTablePage;
import org.eclipse.scout.sdk.ui.wizard.form.SearchFormNewWizard;
import org.eclipse.scout.sdk.ui.wizard.page.PageLinkWizard;
import org.eclipse.scout.sdk.ui.wizard.page.PageNewWizard;
import org.eclipse.scout.sdk.util.SdkMethodUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.SdkTypeUtility;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;

/**
 * <h3>PageWithTableNodePage</h3> ...
 */
public class PageWithTableNodePage extends AbstractScoutTypePage {
  final static String METHOD_EXEC_CREATE_CHILD_PAGE = "execCreateChildPage";
  final IType iTable = ScoutSdk.getType(RuntimeClasses.ITable);

  private P_MethodChangedListener m_methodChangedListener;

  public PageWithTableNodePage(IPage parent, IType type) {
    setParent(parent);
    setType(type);
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.PageTable));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.PAGE_WITH_TABLE_NODE_PAGE;
  }

  @Override
  protected String getMethodNameForTranslatedText() {
    return "getConfiguredTitle";
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
    if (m_methodChangedListener != null) {
      JavaCore.removeElementChangedListener(m_methodChangedListener);
      m_methodChangedListener = null;
    }
  }

  @Override
  protected void loadChildrenImpl() {
    if (m_methodChangedListener == null) {
      m_methodChangedListener = new P_MethodChangedListener();
      JavaCore.addElementChangedListener(m_methodChangedListener);
    }
    IType[] allSubtypes = SdkTypeUtility.getTables(getType());
    if (allSubtypes.length > 0) {
      TableNodePage tableNodePage = new TableNodePage();
      tableNodePage.setParent(this);
      tableNodePage.setType(allSubtypes[0]);
    }

    new BeanPropertyTablePage(this, getType());
    new TablePageChildPageTablePage(this, getType());
  }

  @Override
  public Action createDeleteAction() {
    TypeDeleteOperation op = new TypeDeleteOperation(getType());
    return new DeleteAction(getName(), getOutlineView().getSite().getShell(), op);
  }

  @Override
  public void fillContextMenu(IMenuManager manager) {
    super.fillContextMenu(manager);
    if (!TypeUtility.exists(TypeUtility.getMethod(getType(), METHOD_EXEC_CREATE_CHILD_PAGE))) {
      PageNewWizard wizard = new PageNewWizard(getScoutResource());
      manager.add(new WizardAction(Texts.get("Action_newTypeX", "Page"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.PageAdd), wizard));
      PageLinkWizard linkWizard = new PageLinkWizard(getScoutResource());
      linkWizard.setHolderType(getType());
      linkWizard.setHolderEnabled(false);
      manager.add(new WizardAction("Add Page...", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.PageLink), linkWizard));
    }

    manager.add(new Separator());
    SearchFormNewWizard wizard = new SearchFormNewWizard(getScoutResource());
    wizard.setTablePage(getType());
    IMethod titleMethod = TypeUtility.getMethod(getType(), "getConfiguredTitle");
    if (TypeUtility.exists(titleMethod)) {
      try {
        wizard.setNlsEntry(SdkMethodUtility.getReturnNlsEntry(titleMethod));
      }
      catch (CoreException e) {
        ScoutSdkUi.logWarning("could not parse nls entry for method '" + titleMethod.getElementName() + "'.", e);
      }
    }
    manager.add(new WizardAction("Create Search Form", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.SearchFormAdd), wizard));
  }

  private class P_MethodChangedListener extends ElementChangedListenerEx {
    @Override
    protected boolean visit(int kind, int flags, IJavaElement e, CompilationUnit ast) {
      if (e != null && e.getElementType() == IJavaElement.METHOD) {
        IMethod method = (IMethod) e;
        IType declaringType = method.getDeclaringType();
        if (TypeUtility.exists(declaringType) && declaringType.equals(getType()) && METHOD_EXEC_CREATE_CHILD_PAGE.equals(method.getElementName())) {
          markStructureDirty();
          return true;
        }
        return false;
      }
      return super.visit(kind, flags, e, ast);
    }
  } // end class P_MethodChangedListener

}
