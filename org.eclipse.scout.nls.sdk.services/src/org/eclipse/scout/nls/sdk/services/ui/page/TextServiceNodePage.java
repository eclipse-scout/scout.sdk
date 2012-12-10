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
package org.eclipse.scout.nls.sdk.services.ui.page;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.nls.sdk.services.ui.action.TextProviderServiceDeleteAction;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.EditorSelectionVisitor;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.INodeVisitor;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.project.server.service.AbstractServiceNodePage;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 * <h3>IconNodePage</h3> a node forks the Icon editor to open on selection.
 */
public class TextServiceNodePage extends AbstractServiceNodePage {

  public TextServiceNodePage(AbstractPage parentPage, IType type) {
    super(parentPage, type, TypeUtility.getType(RuntimeClasses.AbstractDynamicNlsTextProviderService), SdkProperties.SUFFIX_TEXT_SERVICE);
    setName(type.getElementName());
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Text));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.TEXT_SERVICE_NODE_PAGE;
  }

  /**
   * shared bundle
   */
  @Override
  public IScoutBundle getScoutResource() {
    return super.getScoutResource();
  }

  @Override
  public int accept(INodeVisitor visitor) {
    if (visitor instanceof EditorSelectionVisitor) {
      EditorSelectionVisitor v = (EditorSelectionVisitor) visitor;
      IJavaElement elementToSearch = v.getCurrentElement();
      if (CompareUtility.equals(this.getType(), elementToSearch)) {
        v.setNodeToSelect(this);
        return INodeVisitor.CANCEL;
      }
    }
    return INodeVisitor.CANCEL_SUBTREE;
  }

  @Override
  public boolean isChildrenLoaded() {
    return true;
  }

  @Override
  public void loadChildrenImpl() {
    // void
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{TextProviderServiceDeleteAction.class};
  }
}
