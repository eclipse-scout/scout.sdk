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

import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.KeyStrokeTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form.field.composer.attribute.AttributeTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form.field.composer.entity.EntityTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.tree.TreeNodePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.InnerTypePageDirtyListener;
import org.eclipse.scout.sdk.ui.view.outline.pages.project.client.ui.form.field.AbstractFormFieldNodePage;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;

public class ComposerFieldNodePage extends AbstractFormFieldNodePage {

  protected IType abstractComposerField_tree = TypeUtility.getType(IRuntimeClasses.AbstractComposerField_Tree);
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
      ScoutSdkCore.getJavaResourceChangedEmitter().removeInnerTypeChangedListener(getType(), m_innerTypeListener);
      m_innerTypeListener = null;
    }
    super.unloadPage();
  }

  @Override
  protected void loadChildrenImpl() {
    if (m_innerTypeListener == null) {
      m_innerTypeListener = new InnerTypePageDirtyListener(this, abstractComposerField_tree);
      ScoutSdkCore.getJavaResourceChangedEmitter().addInnerTypeChangedListener(getType(), m_innerTypeListener);
    }
    new KeyStrokeTablePage(this, getType());
    // find tree
    ITypeHierarchy hierarchy = TypeUtility.getLocalTypeHierarchy(getType(), abstractComposerField_tree);
    if (hierarchy != null) {
      Set<IType> trees = TypeUtility.getInnerTypes(getType(), TypeFilters.getSubtypeFilter(abstractComposerField_tree, hierarchy));
      if (trees.size() > 0) {
        new TreeNodePage(this, CollectionUtility.firstElement(trees), true);
      }
    }
    // entities
    new AttributeTablePage(this, getType());
    new EntityTablePage(this, getType());
  }
}
