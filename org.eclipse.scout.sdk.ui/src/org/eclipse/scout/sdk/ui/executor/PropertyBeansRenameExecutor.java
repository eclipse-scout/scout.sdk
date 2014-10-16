/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.executor;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.jdt.JdtRenameTransaction;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.shared.SharedContextPropertyNodePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.basic.beanproperty.BeanPropertyNodePage;
import org.eclipse.scout.sdk.util.IRegEx;
import org.eclipse.scout.sdk.util.NamingUtility;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.type.IPropertyBean;
import org.eclipse.scout.sdk.util.type.MethodFilters;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;

/**
 * <h3>{@link PropertyBeansRenameExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 13.10.2014
 */
public class PropertyBeansRenameExecutor extends AbstractRenameExecutor {

  List<IPropertyBean> m_beans;

  @Override
  public boolean canRun(IStructuredSelection selection) {
    boolean superRun = super.canRun(selection);
    if (!superRun) {
      return false;
    }

    Object selectedElement = selection.getFirstElement();
    m_beans = new LinkedList<IPropertyBean>();
    if (selectedElement instanceof SharedContextPropertyNodePage) {
      SharedContextPropertyNodePage scpnp = (SharedContextPropertyNodePage) selectedElement;
      if (scpnp.getClientDesc() != null) {
        m_beans.add(scpnp.getClientDesc());
      }
      if (scpnp.getServerDesc() != null) {
        m_beans.add(scpnp.getServerDesc());
      }
    }
    else if (selectedElement instanceof BeanPropertyNodePage) {
      BeanPropertyNodePage bpnp = (BeanPropertyNodePage) selectedElement;
      m_beans.add(bpnp.getPropertyDescriptor());
    }

    if (m_beans.isEmpty()) {
      return false;
    }

    for (IPropertyBean p : m_beans) {
      if (!isEditable(p.getDeclaringType())) {
        return false;
      }
    }
    return true;
  }

  @Override
  protected void fillTransaction(JdtRenameTransaction transaction, String newName) throws CoreException {
    if (m_beans != null && m_beans.size() > 0) {
      setOldName(CollectionUtility.firstElement(m_beans).getBeanName());
    }

    String oldNameLower = NamingUtility.ensureStartWithLowerCase(getOldName());
    String oldNameUpper = NamingUtility.ensureStartWithUpperCase(getOldName());
    for (IPropertyBean d : m_beans) {
      for (IMember m : d.getAllMembers()) {
        String elementName = m.getElementName();
        switch (m.getElementType()) {
          case IMember.FIELD:
            transaction.add((IField) m, elementName.replaceAll(oldNameLower, NamingUtility.ensureStartWithLowerCase(newName)));
            break;
          case IMember.METHOD:
            transaction.add((IMethod) m, elementName.replaceAll(oldNameUpper, NamingUtility.ensureStartWithUpperCase(newName)));
            break;
        }
      }
    }
  }

  @Override
  protected IStatus validate(String newName) {
    IStatus inheritedStatus = ScoutUtility.validateJavaName(newName, getReadOnlySuffix());
    if (inheritedStatus.matches(IStatus.ERROR)) {
      return inheritedStatus;
    }

    String name = NamingUtility.ensureStartWithUpperCase(newName);

    for (IPropertyBean bean : m_beans) {
      // check that no value field has the same name. this could lead to errors in form data generation (duplicate methods).
      ITypeHierarchy typeHierarchy = TypeUtility.getLocalTypeHierarchy(bean.getDeclaringType());
      try {
        List<IType> allValueFields = TypeUtility.getAllTypes(bean.getDeclaringType().getCompilationUnit(), TypeFilters.getSubtypeFilter(TypeUtility.getType(IRuntimeClasses.IValueField), typeHierarchy));
        for (IType valueField : allValueFields) {
          String fieldName = ScoutUtility.removeFieldSuffix(valueField.getElementName());
          if (name.equals(fieldName)) {
            return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("Error_nameAlreadyUsed"));
          }
        }
      }
      catch (JavaModelException e) {
        ScoutSdkUi.logError("unable to validate property rename", e);
        return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "Fatal: " + e.getMessage());
      }

      if (TypeUtility.getMethods(bean.getDeclaringType(), MethodFilters.getNameRegexFilter(Pattern.compile("^(get|set|is)" + name))).size() > 0) {
        return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("Error_nameAlreadyUsed"));
      }
    }

    if (IRegEx.WELLFORMED_PROPERTY.matcher(newName).matches()) {
      return Status.OK_STATUS;
    }

    if (IRegEx.JAVAFIELD.matcher(newName).matches()) {
      return new Status(IStatus.WARNING, ScoutSdkUi.PLUGIN_ID, Texts.get("Warning_notWellformedJavaName"));
    }
    return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("Error_invalidFieldX", newName));
  }
}
