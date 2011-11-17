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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.action.delete.PropertyBeanDeleteAction;
import org.eclipse.scout.sdk.ui.action.rename.PropertyBeansRenameAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.type.IPropertyBean;

/**
 * <h3>BeanPropertyNodePage</h3> ...
 */
public class BeanPropertyNodePage extends AbstractPage {
  private final IPropertyBean m_propertyDescriptor;

  public BeanPropertyNodePage(IPage parentPage, IPropertyBean propertyDescriptor) {
    m_propertyDescriptor = propertyDescriptor;
    setParent(parentPage);
    setName(getPropertyDescriptor().getBeanName() + " (" + Signature.getSignatureSimpleName(getPropertyDescriptor().getBeanSignature()) + ")");
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Variable));
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

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends AbstractScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{PropertyBeansRenameAction.class, PropertyBeanDeleteAction.class};
  }

  @Override
  public void prepareMenuAction(AbstractScoutHandler menu) {
    if (menu instanceof PropertyBeansRenameAction) {
      ((PropertyBeansRenameAction) menu).setPropertyBeanDescriptors(new IPropertyBean[]{getPropertyDescriptor()});
    }
    else if (menu instanceof PropertyBeanDeleteAction) {
      ((PropertyBeanDeleteAction) menu).setBeanDesc(getPropertyDescriptor());
    }
  }
}
