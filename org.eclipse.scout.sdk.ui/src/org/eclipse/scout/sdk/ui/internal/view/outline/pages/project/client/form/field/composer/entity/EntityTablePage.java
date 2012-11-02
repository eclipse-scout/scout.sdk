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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form.field.composer.entity;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.create.EntityNewAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.InnerTypePageDirtyListener;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.TypeCacheAccessor;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>{@link EntityTablePage}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 01.09.2010
 */
public class EntityTablePage extends AbstractPage {

  protected final IType iComposerEntity = TypeUtility.getType(RuntimeClasses.IComposerEntity);
  private final IType m_declaringType;

  private InnerTypePageDirtyListener m_entityChangedListener;

  public EntityTablePage(IPage parent, IType declaringType) {
    super();
    m_declaringType = declaringType;
    setParent(parent);
    setName(Texts.get("Entities"));
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ComposerEntries));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.COMPOSER_FIELD_ENTITY_TABLE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  @Override
  public void unloadPage() {
    super.unloadPage();
    if (m_entityChangedListener != null) {
      TypeCacheAccessor.getJavaResourceChangedEmitter().removeInnerTypeChangedListener(getDeclaringType(), m_entityChangedListener);
      m_entityChangedListener = null;
    }
  }

  @Override
  protected void loadChildrenImpl() {
    if (m_entityChangedListener == null) {
      m_entityChangedListener = new InnerTypePageDirtyListener(this, iComposerEntity);
      TypeCacheAccessor.getJavaResourceChangedEmitter().addInnerTypeChangedListener(getDeclaringType(), m_entityChangedListener);
    }
    for (IType entity : ScoutTypeUtility.getComposerEntities(getDeclaringType())) {
      new EntityNodePage(this, entity);
    }
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    ((EntityNewAction) menu).setType(getDeclaringType());
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{EntityNewAction.class};
  }

  public IType getDeclaringType() {
    return m_declaringType;
  }
}
