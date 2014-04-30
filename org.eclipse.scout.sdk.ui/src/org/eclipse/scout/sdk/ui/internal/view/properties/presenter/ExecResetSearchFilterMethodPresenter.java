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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.targetpackage.DefaultTargetPackage;
import org.eclipse.scout.sdk.extensions.targetpackage.IDefaultTargetPackage;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.jdt.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodBodySourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
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
        P_OverrideExecResetSearchFilterMethod methodOverrideOperation = new P_OverrideExecResetSearchFilterMethod(getMethod().getMethodName(), getMethod().getType());
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
      catch (CoreException e) {
        ScoutSdkUi.logWarning("could not override the method '" + getMethod().getMethodName() + "' on '" + getMethod().getType() + "'", e);
      }
    }
  }

  private class P_OverrideExecResetSearchFilterMethod extends MethodOverrideOperation {
    private final IType iForm = TypeUtility.getType(IRuntimeClasses.IForm);
    private final IType iSearchForm = TypeUtility.getType(IRuntimeClasses.ISearchForm);
    private IType m_formDataType;
    private IType m_formType;

    /**
     * @param method
     * @param content
     * @throws CoreException
     */
    public P_OverrideExecResetSearchFilterMethod(String methodName, IType declaringType) throws CoreException {
      super(methodName, declaringType);
      setFormatSource(true);
      m_formDataType = null;
      m_formType = declaringType;
      ICachedTypeHierarchy formHierarchy = TypeUtility.getPrimaryTypeHierarchy(iForm);
      if (TypeUtility.exists(m_formType) && formHierarchy.isSubtype(iSearchForm, m_formType)) {
        String formDataSimpleName = m_formType.getElementName().replaceAll(SdkProperties.SUFFIX_FORM + "$", SdkProperties.SUFFIX_FORM_DATA);
        IScoutBundle clientBundle = ScoutTypeUtility.getScoutBundle(getDeclaringType());
        for (IScoutBundle shared : clientBundle.getParentBundles(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SHARED), false)) {

          String formDataFqn = DefaultTargetPackage.get(shared, IDefaultTargetPackage.SERVER_SERVICES) + "." + formDataSimpleName;
          if (TypeUtility.existsType(formDataFqn)) {
            m_formDataType = TypeUtility.getType(formDataFqn);
            break;
          }
        }
      }

    }

    @Override
    public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
      if (m_formDataType != null && m_formType != null) {
        setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
          @Override
          public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
            source.append("super.execResetSearchFilter(searchFilter);").append(lineDelimiter);
            String simpleFormDataName = validator.getTypeName(SignatureCache.createTypeSignature(m_formDataType.getFullyQualifiedName()));
            source.append(simpleFormDataName).append(" formData = new ").append(simpleFormDataName).append("();").append(lineDelimiter);
            source.append("exportFormData(formData);").append(lineDelimiter);
            source.append("searchFilter.setFormData(formData);");
          }
        });
      }
      super.run(monitor, workingCopyManager);
    }

  }
}
