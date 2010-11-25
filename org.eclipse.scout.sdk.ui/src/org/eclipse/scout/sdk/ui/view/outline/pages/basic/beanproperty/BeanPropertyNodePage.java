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
package org.eclipse.scout.sdk.ui.view.outline.pages.basic.beanproperty;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.delete.PropertyBeanDeleteAction;
import org.eclipse.scout.sdk.ui.action.rename.PropertyBeansRenameAction;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.workspace.member.IPropertyBean;

/**
 * <h3>BeanPropertyNodePage</h3> ...
 */
public class BeanPropertyNodePage extends AbstractPage {
  private final IPropertyBean m_propertyDescriptor;

  public BeanPropertyNodePage(IPage parentPage, IPropertyBean propertyDescriptor) {
    m_propertyDescriptor = propertyDescriptor;
    setParent(parentPage);
    setName(getPropertyDescriptor().getBeanName() + " (" + Signature.getSignatureSimpleName(getPropertyDescriptor().getBeanSignature()) + ")");
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.BEAN_PROPERTY_NODE_PAGE;
  }

  @Override
  public ImageDescriptor getBaseImageDescriptor() {
    ImageDescriptor imgDesc = JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_FIELD_DEFAULT);
    if (getPropertyDescriptor().getReadMethod() != null) {
      try {
        int flags = getPropertyDescriptor().getReadMethod().getFlags();
        if ((flags & Flags.AccPublic) != 0) {
          imgDesc = JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_PUBLIC);
        }
        else if ((flags & Flags.AccProtected) != 0) {
          imgDesc = JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_PROTECTED);
        }
        else if ((flags & Flags.AccDefault) != 0) {
          imgDesc = JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_DEFAULT);
        }
        else if ((flags & Flags.AccPrivate) != 0) {
          imgDesc = JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_PRIVATE);
        }
      }
      catch (JavaModelException e) {
        ScoutSdkUi.logWarning(e);
      }
    }
    return imgDesc;
  }

  public IPropertyBean getPropertyDescriptor() {
    return m_propertyDescriptor;
  }

  @Override
  public Action createRenameAction() {
    return new PropertyBeansRenameAction(getOutlineView().getSite().getShell(), "Rename...", new IPropertyBean[]{getPropertyDescriptor()});
  }

  @Override
  public Action createDeleteAction() {
    return new PropertyBeanDeleteAction(Texts.get("Action_deleteTypeX", getName()), getOutlineView().getSite().getShell(), getPropertyDescriptor());
  }

}
