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
package org.eclipse.scout.sdk.ui.action.rename;

import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.jdt.JdtRenameTransaction;
import org.eclipse.scout.sdk.util.type.FieldFilters;
import org.eclipse.scout.sdk.util.type.IPropertyBean;
import org.eclipse.scout.sdk.util.type.MethodFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;

public class PropertyBeansRenameAction extends AbstractRenameAction {

  private IPropertyBean[] m_propertyBeanDescriptors;

  public PropertyBeansRenameAction() {
    setReadOnlySuffix("");
    setOldName("");
  }

  @Override
  protected void fillTransaction(JdtRenameTransaction transaction, String newName) throws CoreException {
    String oldNameLower = getBeanName(getOldName(), false);
    String oldNameUpper = getBeanName(getOldName(), true);
    for (IPropertyBean d : getPropertyBeanDescriptors()) {
      for (IMember m : d.getAllMembers()) {
        String elementName = m.getElementName();
        switch (m.getElementType()) {
          case IMember.FIELD:
            transaction.add((IField) m, elementName.replaceAll(oldNameLower, getBeanName(newName, false)));
            break;
          case IMember.METHOD:
            transaction.add((IMethod) m, elementName.replaceAll(oldNameUpper, getBeanName(newName, true)));
            break;
        }

      }
    }
  }

  public String getBeanName(String name, boolean startWithUpperCase) {
    if (StringUtility.isNullOrEmpty(name) || name.length() < 2) {
      return null;
    }
    if (startWithUpperCase) {
      return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
    else {
      return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }
  }

  @Override
  protected IStatus validate(String newName) {
    IStatus inheritedStatus = getJavaNameStatus(newName);
    if (inheritedStatus.matches(IStatus.ERROR)) {
      return inheritedStatus;
    }
    for (IPropertyBean bean : getPropertyBeanDescriptors()) {
      if (TypeUtility.getFirstMethod(bean.getDeclaringType(), MethodFilters.getNameRegexFilter(Pattern.compile("(get|set|is)" + newName))) != null) {
        return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "Name already in use.");
      }
      String fieldName = "m_" + Character.toLowerCase(newName.charAt(0));
      if (bean.getBeanName().length() > 1) {
        fieldName = fieldName + newName.substring(1);
        if (TypeUtility.getFirstField(bean.getDeclaringType(), FieldFilters.getNameRegexFilter(Pattern.compile(fieldName))) != null) {
          return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "Name already in use.");
        }
      }
    }

    return inheritedStatus;
  }

  /**
   * @return the propertyBeanDescriptors
   */
  public IPropertyBean[] getPropertyBeanDescriptors() {
    return m_propertyBeanDescriptors;
  }

  public void setPropertyBeanDescriptors(IPropertyBean[] propertyBeanDescriptors) {
    m_propertyBeanDescriptors = propertyBeanDescriptors;
    if (getPropertyBeanDescriptors() != null && getPropertyBeanDescriptors().length > 0) {
      setOldName(getPropertyBeanDescriptors()[0].getBeanName());
    }
  }
}
