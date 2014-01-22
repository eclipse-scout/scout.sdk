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

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.SharedContextBeanPropertyNewOperation;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.util.type.IMethodFilter;
import org.eclipse.scout.sdk.util.type.MethodFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;

public class SharedContextBeanPropertyNewWizard extends AbstractWorkspaceWizard {

  public SharedContextBeanPropertyNewWizard(IType serverSessionType, IType clientSessionType) {
    setWindowTitle(Texts.get("NewSharedContextProperty"));
    IJavaSearchScope searchScope = SearchEngine.createJavaSearchScope(new IJavaElement[]{serverSessionType.getJavaProject()});
    BeanPropertyNewWizardPage beanPropertyWizardPage = new BeanPropertyNewWizardPage(searchScope, serverSessionType);

    SharedContextBeanPropertyNewOperation op = new SharedContextBeanPropertyNewOperation(serverSessionType, clientSessionType);
    beanPropertyWizardPage.setOperation(op);

    // find all used method names
    HashSet<String> notAllowedMethodNames = new HashSet<String>();
    collectMethodNames(serverSessionType, notAllowedMethodNames);
    if (clientSessionType != null) {
      collectMethodNames(clientSessionType, notAllowedMethodNames);
    }
    beanPropertyWizardPage.setNotAllowedNames(notAllowedMethodNames);
    addPage(beanPropertyWizardPage);
  }

  protected void collectMethodNames(IType type, Set<String> collector) {
    IMethodFilter filter = MethodFilters.getMultiMethodFilter(
        MethodFilters.getFlagsFilter(Flags.AccPublic),
        MethodFilters.getNameRegexFilter(Pattern.compile("^(get|set|is).*")));

    for (IMethod m : TypeUtility.getMethods(type, filter)) {
      collector.add(m.getElementName());
    }
  }
}
