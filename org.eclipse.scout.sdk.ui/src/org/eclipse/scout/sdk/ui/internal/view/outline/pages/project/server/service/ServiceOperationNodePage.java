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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.IRegion;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.delete.MemberListDeleteAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.INodeVisitor;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * <h3>ServiceOperationNodePage</h3> ...
 */
public class ServiceOperationNodePage extends AbstractPage {

  private final IMethod m_interfaceOpMethod;
  private final IMethod m_implementationOpMethod;

  public ServiceOperationNodePage(AbstractPage parent, IMethod method, IMethod implementationMethod) {
    setParent(parent);
    m_interfaceOpMethod = method;
    m_implementationOpMethod = implementationMethod;
    setName(getMethodDisplayName(m_implementationOpMethod));
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ServiceOperation));
  }

  private static String getMethodDisplayName(IMethod m) {
    StringBuilder displayName = new StringBuilder(m.getElementName());
    displayName.append("(");
    String[] paramTypes = m.getParameterTypes();
    for (int i = 0; i < paramTypes.length; i++) {
      displayName.append(Signature.getSignatureSimpleName(paramTypes[i]));
      if (i < paramTypes.length - 1) {
        displayName.append(", ");
      }
    }
    displayName.append(")");
    return displayName.toString();
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.SERVICE_OPERATION_NODE_PAGE;
  }

  /**
   * server bundle
   */
  @Override
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  @Override
  public int accept(INodeVisitor visitor) {
    return INodeVisitor.CANCEL_SUBTREE;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{MemberListDeleteAction.class};
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    super.prepareMenuAction(menu);
    if (menu instanceof MemberListDeleteAction) {
      MemberListDeleteAction action = (MemberListDeleteAction) menu;
      if (TypeUtility.exists(getInterfaceOpMethod())) {
        action.addMemberToDelete(getInterfaceOpMethod());
      }
      action.addMemberToDelete(getImplementationOpMethod());
      action.setImage(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ServiceOperationRemove));
    }
  }

  @Override
  public boolean handleDoubleClickedDelegate() {
    try {
      IJavaElement jdtMember = getImplementationOpMethod();
      IEditorPart editor = JavaUI.openInEditor(jdtMember);
      JavaUI.revealInEditor(editor, jdtMember);
      if (editor instanceof ITextEditor) {
        ITextEditor textEditor = (ITextEditor) editor;
        IRegion reg = textEditor.getHighlightRange();
        if (reg != null) {
          textEditor.setHighlightRange(reg.getOffset(), reg.getLength(), true);
        }
      }
      return true;
    }
    catch (Exception ex) {
      ScoutSdkUi.logWarning(ex);
      return false;
    }
  }

  public IMethod getInterfaceOpMethod() {
    return m_interfaceOpMethod;
  }

  public IMethod getImplementationOpMethod() {
    return m_implementationOpMethod;
  }
}
