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
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.ShowJavaReferencesAction;
import org.eclipse.scout.sdk.ui.action.delete.OutlineDeleteAction;
import org.eclipse.scout.sdk.ui.action.rename.TypeRenameAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.SdkProperties;

/**
 * <h3>OutlineNodePage</h3>
 */
public class OutlineNodePage extends AbstractScoutTypePage {

  public OutlineNodePage(IPage parentPage, IType outlineType) {
    super(SdkProperties.SUFFIX_OUTLINE);
    setParent(parentPage);
    setType(outlineType);
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Outline));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.OUTLINE_NODE_PAGE;
  }

  @Override
  protected String getMethodNameForTranslatedText() {
    return "getConfiguredTitle";
  }

  @Override
  public boolean isFolder() {
    return false;
  }

  @Override
  protected void loadChildrenImpl() {
    new OutlinePageChildPageTablePage(this, getType());
  }

  @Override
  public Set<Class<? extends IScoutHandler>> getSupportedMenuActions() {
    return newSet(TypeRenameAction.class, ShowJavaReferencesAction.class, OutlineDeleteAction.class);
  }
}
