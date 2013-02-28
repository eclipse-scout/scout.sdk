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
package org.eclipse.scout.sdk.ui.internal.view.properties.presenter;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.widgets.Composite;

public class ExecResetSearchFilterMethodPresenter extends ExecMethodPresenter {

  public ExecResetSearchFilterMethodPresenter(PropertyViewFormToolkit toolkit, Composite parent) {
    super(toolkit, parent);
  }

  @Override
  protected void overrideMethod() {
    if (!getMethod().isImplemented()) {
      try {
        P_OverrideExecResetSearchFilterMethod methodOverrideOperation = new P_OverrideExecResetSearchFilterMethod(getMethod().getType(), getMethod().getMethodName());
        OperationJob job = new OperationJob(methodOverrideOperation);
        job.schedule();
        try {
          job.join();
        }
        catch (InterruptedException e) {
        }
        if (methodOverrideOperation.getCreatedMethod() != null) {
          showJavaElementInEditor(methodOverrideOperation.getCreatedMethod());
        }
      }
      catch (JavaModelException e) {
        ScoutSdkUi.logWarning("could not override the method '" + getMethod().getMethodName() + "' on '" + getMethod().getType() + "'", e);
      }
    }
  }

  private class P_OverrideExecResetSearchFilterMethod extends MethodOverrideOperation {
    private final IType iForm = TypeUtility.getType(RuntimeClasses.IForm);
    private final IType iSearchForm = TypeUtility.getType(RuntimeClasses.ISearchForm);
    private IType m_formDataType;
    private IType m_formType;

    /**
     * @param method
     * @param content
     * @throws JavaModelException
     */
    public P_OverrideExecResetSearchFilterMethod(IType declaringType, String methodName) throws JavaModelException {
      super(declaringType, methodName, true);
      m_formDataType = null;
      m_formType = declaringType;
      ICachedTypeHierarchy formHierarchy = TypeUtility.getPrimaryTypeHierarchy(iForm);
      if (TypeUtility.exists(m_formType) && formHierarchy.isSubtype(iSearchForm, m_formType)) {
        m_formDataType = ScoutTypeUtility.findFormDataForForm(m_formType);
      }
    }

    @Override
    protected String createMethodBody(IImportValidator validator) throws JavaModelException {
      if (m_formDataType != null && m_formType != null) {
        StringBuilder content = new StringBuilder();
        content.append("super.execResetSearchFilter(searchFilter);\n");
        String simpleFormDataName = validator.getTypeName(SignatureCache.createTypeSignature(m_formDataType.getFullyQualifiedName()));
        content.append(simpleFormDataName + " formData = new " + simpleFormDataName + "();\n");
        content.append("exportFormData(formData);\n");
        content.append("searchFilter.setFormData(formData);");
        return content.toString();
      }
      else {
        return super.createMethodBody(validator);
      }
    }
  }
}
