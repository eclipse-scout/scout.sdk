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
package org.eclipse.scout.sdk.ui.wizard.beanproperty;

import java.util.HashSet;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.operation.BeanPropertyNewOperation;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.workspace.type.MethodFilters;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;

public class BeanPropertyNewWizard extends AbstractWorkspaceWizard {

  public BeanPropertyNewWizard(IType declaringType) {
    BeanPropertyNewWizardPage beanPropertyWizardPage = new BeanPropertyNewWizardPage(TypeUtility.newSearchScope(declaringType.getJavaProject()));
    BeanPropertyNewOperation op = new BeanPropertyNewOperation(declaringType);
    beanPropertyWizardPage.setOperation(op);
    // find all used method names
    HashSet<String> notAllowedMethodNames = new HashSet<String>();
    IMethod[] methods = TypeUtility.getMethods(declaringType, MethodFilters.getNameRegexFilter("^(get|set|is).*"));
    for (IMethod m : methods) {
      notAllowedMethodNames.add(m.getElementName());
    }
    beanPropertyWizardPage.setNotAllowedNames(notAllowedMethodNames);
    addPage(beanPropertyWizardPage);
  }

}
