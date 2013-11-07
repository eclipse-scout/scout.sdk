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
package org.eclipse.scout.sdk.operation.form;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.jdt.JavaElementDeleteOperation;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>{@link FormHandlerDeleteOperation}</h3> ...
 * 
 *  @author Andreas Hoegger
 * @since 3.9.0 30.04.2013
 */
public class FormHandlerDeleteOperation extends JavaElementDeleteOperation {

  private final IType m_formHandler;

  public FormHandlerDeleteOperation(IType formHandler) {
    this(formHandler, true);
  }

  public FormHandlerDeleteOperation(IType formHandler, boolean formatSource) {
    super(formatSource);
    m_formHandler = formHandler;
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    addMember(getFormHandler());
    IMethod startMethod = findStartMethod();
    if (startMethod != null) {
      addMember(startMethod);
    }
    validate();
    super.run(monitor, workingCopyManager);
  }

  public IMethod findStartMethod() {
    // find form
    ITypeHierarchy hierarchy = TypeUtility.getLocalTypeHierarchy(getFormHandler().getCompilationUnit());
    IType form = TypeUtility.getAncestor(getFormHandler(), TypeFilters.getMultiTypeFilterOr(
        TypeFilters.getSubtypeFilter(TypeUtility.getType(RuntimeClasses.IForm), hierarchy),
        TypeFilters.getTopLevelTypeFilter()));
    if (TypeUtility.exists(form) && TypeUtility.exists(getFormHandler())) {
      String startMethodName = getFormHandler().getElementName().replaceFirst(SdkProperties.SUFFIX_FORM_HANDLER + "\\b", "");
      if (startMethodName.length() > 1) {
        startMethodName = Character.toUpperCase(startMethodName.charAt(0)) + startMethodName.substring(1);
        startMethodName = "start" + startMethodName;
      }
      return TypeUtility.getMethod(form, startMethodName);
    }
    return null;
  }

  public IType getFormHandler() {
    return m_formHandler;
  }

}
