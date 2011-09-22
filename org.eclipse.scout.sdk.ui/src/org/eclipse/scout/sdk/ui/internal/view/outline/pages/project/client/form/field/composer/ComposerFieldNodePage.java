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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form.field.composer;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.KeyStrokeTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form.field.composer.attribute.AttributeTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form.field.composer.entity.EntityTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.tree.TreeNodePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.InnerTypePageDirtyListener;
import org.eclipse.scout.sdk.ui.view.outline.pages.project.client.ui.form.field.AbstractFormFieldNodePage;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.typecache.ITypeHierarchy;

public class ComposerFieldNodePage extends AbstractFormFieldNodePage {

  private boolean m_hasTree;
  protected IType abstractComposerField_tree = ScoutSdk.getType(RuntimeClasses.AbstractComposerField_Tree);

  private InnerTypePageDirtyListener m_innerTypeListener;

  public ComposerFieldNodePage() {
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ComposerField));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.COMPOSER_FIELD_NODE_PAGE;
  }

  @Override
  public void unloadPage() {
    if (m_innerTypeListener != null) {
      ScoutSdk.removeInnerTypeChangedListener(getType(), m_innerTypeListener);
      m_innerTypeListener = null;
    }
    super.unloadPage();
  }

  @Override
  public void loadChildrenImpl() {
    if (m_innerTypeListener == null) {
      m_innerTypeListener = new InnerTypePageDirtyListener(this, abstractComposerField_tree);
      ScoutSdk.addInnerTypeChangedListener(getType(), m_innerTypeListener);
    }
    new KeyStrokeTablePage(this, getType());
    // find tree
    m_hasTree = false;
    ITypeHierarchy hierarchy = ScoutSdk.getLocalTypeHierarchy(getType(), abstractComposerField_tree);
    if (hierarchy != null) {
      IType[] trees = TypeUtility.getInnerTypes(getType(), TypeFilters.getSubtypeFilter(abstractComposerField_tree, hierarchy));
      if (trees.length > 0) {
        new TreeNodePage(this, trees[0], true);
        m_hasTree = true;
      }
    }
    // entities
    new AttributeTablePage(this, getType());
    new EntityTablePage(this, getType());
  }
}
