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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form.field.composer.attribute;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.create.AttributeNewAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.InnerTypePageDirtyListener;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>{@link AttributeTablePage}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 01.09.2010
 */
public class AttributeTablePage extends AbstractPage {

  protected IType iDataModelAttribute = TypeUtility.getType(RuntimeClasses.IDataModelAttribute);
  private final IType m_declaringType;

  private InnerTypePageDirtyListener m_attributeChangedListener;

  public AttributeTablePage(IPage parent, IType declaringType) {
    super();
    m_declaringType = declaringType;
    setParent(parent);
    setName(Texts.get("Attributes"));
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ComposerAttributes));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.COMPOSER_FIELD_ATTRIBUTE_TABLE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  @Override
  public void unloadPage() {
    super.unloadPage();
    if (m_attributeChangedListener != null) {
      ScoutSdkCore.getJavaResourceChangedEmitter().removeInnerTypeChangedListener(getDeclaringType(), m_attributeChangedListener);
      m_attributeChangedListener = null;
    }
  }

  @Override
  protected void loadChildrenImpl() {
    if (m_attributeChangedListener == null) {
      m_attributeChangedListener = new InnerTypePageDirtyListener(this, iDataModelAttribute);
      ScoutSdkCore.getJavaResourceChangedEmitter().addInnerTypeChangedListener(getDeclaringType(), m_attributeChangedListener);
    }
    for (IType attribute : ScoutTypeUtility.getDataModelAttributes(getDeclaringType())) {
      new AttributeNodePage(this, attribute);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{AttributeNewAction.class};
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    ((AttributeNewAction) menu).setType(getDeclaringType());
  }

  public IType getDeclaringType() {
    return m_declaringType;
  }

}
