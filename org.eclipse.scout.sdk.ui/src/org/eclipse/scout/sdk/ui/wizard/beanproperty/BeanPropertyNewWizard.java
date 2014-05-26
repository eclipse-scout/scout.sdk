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
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.BeanPropertyNewOperation;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.util.type.MethodFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;

public class BeanPropertyNewWizard extends AbstractWorkspaceWizard {

  public BeanPropertyNewWizard(IType declaringType) {
    setWindowTitle(Texts.get("NewProperty"));
    IJavaSearchScope createJavaSearchScope = SearchEngine.createJavaSearchScope(new IJavaElement[]{declaringType.getJavaProject()});

    BeanPropertyNewWizardPage beanPropertyWizardPage = new BeanPropertyNewWizardPage(createJavaSearchScope, declaringType);
    BeanPropertyNewOperation op = new BeanPropertyNewOperation(declaringType);
    beanPropertyWizardPage.setOperation(op);
    // find all used method names
    HashSet<String> notAllowedMethodNames = new HashSet<String>();
    Set<IMethod> methods = TypeUtility.getMethods(declaringType, MethodFilters.getNameRegexFilter(Pattern.compile("^(get|set|is).*")));
    for (IMethod m : methods) {
      notAllowedMethodNames.add(m.getElementName());
    }
    beanPropertyWizardPage.setNotAllowedNames(notAllowedMethodNames);
    addPage(beanPropertyWizardPage);
  }
}
