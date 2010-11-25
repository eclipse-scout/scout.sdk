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
import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.IDeleteOperation;
import org.eclipse.scout.sdk.operation.util.TypeDeleteOperation;
import org.eclipse.scout.sdk.ui.action.delete.DeleteAction;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.basic.beanproperty.BeanPropertyTablePage;

/**
 * <h3>{@link AttributeNodePage}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 02.09.2010
 */
public class AttributeNodePage extends AbstractScoutTypePage {

  public AttributeNodePage(IPage parentPage, IType declaringType) {
    setType(declaringType);
    setParent(parentPage);
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.COMPOSER_FIELD_ATTRIBUTE_NODE_PAGE;
  }

  @Override
  protected void loadChildrenImpl() {
    new BeanPropertyTablePage(this, getType());
  }

  @Override
  public Action createDeleteAction() {
    IDeleteOperation op = new TypeDeleteOperation(getType());
    return new DeleteAction(Texts.get("Action_deleteTypeX", getName()), getOutlineView().getSite().getShell(), op);
  }
}
