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
package org.eclipse.scout.sdk.operation.form.formdata;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.ManifestExportPackageOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.operation.util.ScoutTypeNewOperation;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.SWT;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 *
 */
public class FormDataUpdateOperation implements IOperation {

  private final IType m_type;
  private FormDataAnnotation m_formDataAnnotation;
  private IType m_formDataType;
  private IWorkingCopyManager m_workingCopyManager;
  private IProgressMonitor m_monitor;
  private String m_formDataIcuSource;

  public FormDataUpdateOperation(IType type) {
    this(type, null);
  }

  public FormDataUpdateOperation(IType type, FormDataAnnotation annotation) {
    m_type = type;
    m_formDataAnnotation = annotation;
  }

  @Override
  public String getOperationName() {
    return "update form data for" + getType().getElementName() + "'...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (!TypeUtility.exists(getType())) {
      throw new IllegalArgumentException("form to create form data for is null or does not exist!");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    m_monitor = monitor;
    m_workingCopyManager = workingCopyManager;
    setup(monitor);
    if (getFormDataAnnotation() != null && FormDataAnnotation.isCreate(getFormDataAnnotation()) &&
        !StringUtility.isNullOrEmpty(getFormDataAnnotation().getFormDataTypeSignature())) {
      createSourceBuilder(monitor);
      storeFormData(monitor);
    }
  }

  public void setup(IProgressMonitor monitor) {
    if (m_formDataAnnotation == null) {
      try {
        m_formDataAnnotation = ScoutTypeUtility.findFormDataAnnotation(getType(), TypeUtility.getSuperTypeHierarchy(getType()));
      }
      catch (JavaModelException e) {
        ScoutSdk.logWarning("could not find form data annotation for '" + getType().getElementName() + "'.", e);
      }
    }
    // form data
    if (getFormDataAnnotation() == null ||
        !FormDataAnnotation.isSdkCommandCreate(getFormDataAnnotation()) ||
        StringUtility.isNullOrEmpty(getFormDataAnnotation().getFormDataTypeSignature())) {
      return;
    }

    m_formDataType = TypeUtility.getTypeBySignature(getFormDataAnnotation().getFormDataTypeSignature());
  }

  /**
   */
  public FormDataAnnotation getFormDataAnnotation() {
    return m_formDataAnnotation;
  }

  /**
   * @return the formDataType
   */
  public IType getFormDataType() {
    return m_formDataType;
  }

  public void createSourceBuilder(IProgressMonitor monitor) {
    if (getFormDataAnnotation() == null) {
      return;
    }
    BundleContext bundleContext = ScoutSdk.getDefault().getBundle().getBundleContext();
    ServiceReference serviceReference = null;
    try {
      ICreateFormDataRequest request = null;
      serviceReference = bundleContext.getServiceReference(ICreateFormDataRequest.class.getName());
      if (serviceReference != null) {
        request = (ICreateFormDataRequest) bundleContext.getService(serviceReference);
      }
      if (!TypeUtility.exists(m_formDataType)) {
        IScoutBundle sharedBundle = findSharedBundle(ScoutTypeUtility.getScoutProject(getType()));
        if (sharedBundle == null) {
          return;
        }
        String packageName = Signature.getSignatureQualifier(getFormDataAnnotation().getFormDataTypeSignature());
        if (StringUtility.isNullOrEmpty(packageName)) {
          if (sharedBundle != null) {
            packageName = sharedBundle.getPackageName(IScoutBundle.SHARED_PACKAGE_APPENDIX_SERVICES_PROCESS);
          }
        }
        String simpleName = (Signature.getSignatureSimpleName(Signature.getTypeErasure(getFormDataAnnotation().getFormDataTypeSignature())));

        ICreateFormDataRequest createRequest = (ICreateFormDataRequest) bundleContext.getService(serviceReference);
        boolean createFormData = createRequest.createFormData(getType(), packageName, simpleName);
        if (createFormData) {
          ScoutTypeNewOperation formDataOp = new ScoutTypeNewOperation(simpleName, packageName, sharedBundle) {
            @Override
            public void run(IProgressMonitor localMonitor, IWorkingCopyManager workingCopyManager) throws CoreException {
              super.run(localMonitor, workingCopyManager);
              // ensure the package of the form data is exported in the shared plugin
              ManifestExportPackageOperation manifestOp = new ManifestExportPackageOperation(ManifestExportPackageOperation.TYPE_ADD_WHEN_NOT_EMTPY, new IPackageFragment[]{getCreatedType().getPackageFragment()}, true);
              manifestOp.run(localMonitor, workingCopyManager);
              workingCopyManager.register(getType().getCompilationUnit(), localMonitor);
              getType().getCompilationUnit().createImport(getCreatedType().getFullyQualifiedName(), null, localMonitor);
            }
          };
          if (m_workingCopyManager != null && m_monitor != null) {
            try {
              formDataOp.run(m_monitor, m_workingCopyManager);
            }
            catch (CoreException e) {
              ScoutSdk.logError("could not create form data compilation unit.", e);
              return;
            }
          }
          else {
            OperationJob newFormDataJob = new OperationJob(formDataOp);
            newFormDataJob.schedule();
            try {
              newFormDataJob.join();
            }
            catch (InterruptedException e) {
              ScoutSdk.logError("could not join form data compilation unit create job.", e);
            }
            if (!newFormDataJob.getResult().isOK()) {
              return;
            }
          }

          m_formDataType = formDataOp.getCreatedType();

        }

      }

      if (TypeUtility.exists(m_formDataType)) {
        ITypeHierarchy existingFormDataHierarchy = TypeUtility.getSuperTypeHierarchy(m_formDataType);
        if (m_formDataType.isReadOnly()) {
          if (request != null) {
            request.showQuestion("Read only Form Data", "Form data '" + m_formDataType.getFullyQualifiedName() + "' is read only. The update will be canceled!", SWT.ICON_WARNING | SWT.OK);
          }
          return;
        }
        if (!existingFormDataHierarchy.contains(TypeUtility.getType(RuntimeClasses.AbstractFormData)) &&
            !existingFormDataHierarchy.contains(TypeUtility.getType(RuntimeClasses.AbstractFormFieldData))) {
          if (request == null || SWT.NO == request.showQuestion("Unusal Form Data", "Are you sure to replace '" + m_formDataType.getFullyQualifiedName() + "' with new generated form data?", SWT.ICON_QUESTION | SWT.YES | SWT.NO)) {
            return;
          }
        }

        try {
          ITypeHierarchy hierarchy = TypeUtility.getLocalTypeHierarchy(getType());
          ITypeSourceBuilder sourceBuilder = FormDataUtility.getPrimaryTypeFormDataSourceBuilder(getFormDataAnnotation().getSuperTypeSignature(), getType(), hierarchy);
          sourceBuilder.setElementName(m_formDataType.getElementName());
          sourceBuilder.setSuperTypeSignature(FormDataUtility.getFormDataSuperTypeSignature(getFormDataAnnotation(), getType(), hierarchy));
          int flags = Flags.AccPublic;
          try {
            if (Flags.isAbstract(getType().getFlags())) {
              flags |= Flags.AccAbstract;
            }
          }
          catch (JavaModelException e) {
            ScoutSdk.logWarning("could not determ abstract flag of '" + getType().getFullyQualifiedName() + "'.", e);
          }
          sourceBuilder.setFlags(flags);
          String icuSource = FormDataUtility.createCompilationUnitSource(sourceBuilder, m_formDataType.getPackageFragment().getElementName(), ScoutTypeUtility.getScoutBundle(m_formDataType).getJavaProject(), monitor);
          m_formDataIcuSource = icuSource;
        }
        catch (Exception e) {
          ScoutSdk.logError("could not build form data builder for '" + getType().getFullyQualifiedName() + "'.", e);
        }
      }
    }
    finally {
      if (serviceReference != null) {
        bundleContext.ungetService(serviceReference);
      }
    }

  }

  public void storeFormData(IProgressMonitor monitor) {
    if (StringUtility.isNullOrEmpty(m_formDataIcuSource)) {
      return;
    }
    if (!TypeUtility.exists(m_formDataType)) {
      return;
    }
    try {
      String oldSource = FormDataUtility.getTypeSource(m_formDataType.getSource(), m_formDataType.getElementName());
      //compare
      String newSource = FormDataUtility.getTypeSource(m_formDataIcuSource, m_formDataType.getElementName());
      if (oldSource != null && newSource != null) {
        newSource = newSource.trim();
        oldSource = oldSource.trim();
        if (!oldSource.equals(newSource)) {
          P_FormDataStoreOperation updateOp = new P_FormDataStoreOperation(m_formDataType, m_formDataIcuSource);
          if (m_monitor != null && m_workingCopyManager != null) {
            try {
              updateOp.run(m_monitor, m_workingCopyManager);
            }
            catch (Exception e) {
              ScoutSdk.logError("craete form data for '" + getType().getFullyQualifiedName() + "' failed.", e);
            }
          }
          else {
            OperationJob updateJob = new OperationJob(updateOp);
            updateJob.schedule();
            try {
              updateJob.join();
            }
            catch (InterruptedException e) {
              ScoutSdk.logError("could not join form data update job.", e);
            }
          }
        }
      }

    }
    catch (Exception e) {
      ScoutSdk.logError("could not update form data for '" + getType().getFullyQualifiedName() + "'.", e);
    }
  }

  private IScoutBundle findSharedBundle(IScoutProject project) {
    if (project == null) {
      return null;
    }
    if (project.getSharedBundle() == null) {
      return findSharedBundle(project.getParentProject());
    }
    return project.getSharedBundle();
  }

  /**
   * @return the form
   */
  public IType getType() {
    return m_type;
  }

  private class P_FormDataStoreOperation implements IOperation {
    private final IType m_formDataType;
    private final String m_icuSource;

    public P_FormDataStoreOperation(IType formDataType, String icuSource) {
      m_formDataType = formDataType;
      m_icuSource = icuSource;
    }

    @Override
    public String getOperationName() {
      return "Update form data '" + m_formDataType.getElementName() + "'.";
    }

    @Override
    public void validate() throws IllegalArgumentException {
    }

    @Override
    public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {

      ICompilationUnit icu = m_formDataType.getCompilationUnit();
      if (icu != null) {
        try {
          icu.becomeWorkingCopy(monitor);

          // store new formdata content to buffer
          icu.getBuffer().setContents(ScoutUtility.cleanLineSeparator(m_icuSource, icu));

          // format buffer & organize imports
          JavaElementFormatOperation formatOp = new JavaElementFormatOperation(icu, true);
          formatOp.validate();
          formatOp.run(monitor, workingCopyManager);

          // save buffer
          icu.getBuffer().save(monitor, true);

          icu.commitWorkingCopy(true, monitor);
        }
        catch (Exception e) {
          ScoutSdk.logError("could not store new form data for '" + getType().getFullyQualifiedName() + "'.", e);
        }
        finally {
          icu.discardWorkingCopy();
        }
      }
    }
  }
}
