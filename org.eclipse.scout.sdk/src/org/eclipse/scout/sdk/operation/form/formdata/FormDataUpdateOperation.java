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
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.FormDataChecksum;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ScoutSdkUtility;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.util.ScoutTypeNewOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.scout.sdk.workspace.type.SdkTypeUtility;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 *
 */
public class FormDataUpdateOperation implements IOperation {

  private final IType m_type;
  private FormDataAnnotation m_formDataAnnotation;
  private IType m_formDataType;
  private FormDataSourceBuilder m_formDataBuilder;
  private IScoutWorkingCopyManager m_workingCopyManager;
  private IProgressMonitor m_monitor;
  private long m_newChecksum = -1;
  private long m_oldCheckSum = -1;

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
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    m_monitor = monitor;
    m_workingCopyManager = workingCopyManager;
    setup(monitor);
    if (getFormDataAnnotation() != null && FormDataAnnotation.isSdkCommandCreate(getFormDataAnnotation()) &&
        !StringUtility.isNullOrEmpty(getFormDataAnnotation().getFormDataTypeSignature())) {
      createSourceBuilder(monitor);
      storeFormData(monitor);
    }
  }

  public void setup(IProgressMonitor monitor) {
    if (m_formDataAnnotation == null) {
      try {
        m_formDataAnnotation = SdkTypeUtility.findFormDataAnnotation(getType(), ScoutSdk.getSuperTypeHierarchy(getType()));
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

    m_formDataType = ScoutSdk.getTypeBySignature(getFormDataAnnotation().getFormDataTypeSignature());
    // checksum
    if (TypeUtility.exists(m_formDataType)) {
      IAnnotation checksumAnnotation = TypeUtility.getAnnotation(m_formDataType, FormDataChecksum.class.getName());
      if (TypeUtility.exists(checksumAnnotation)) {
        try {
          IMemberValuePair[] memberValuePairs = checksumAnnotation.getMemberValuePairs();
          for (IMemberValuePair p : memberValuePairs) {
            if (p.getMemberName().equals("value") && p.getValueKind() == IMemberValuePair.K_LONG) {
              m_oldCheckSum = (Long) p.getValue();
              break;
            }
          }
        }
        catch (CoreException e) {
          ScoutSdk.logError("could not parse checksum annotation of '" + m_formDataType.getFullyQualifiedName() + "'.", e);
        }
      }
    }

    m_newChecksum = ScoutSdkUtility.getAdler32Checksum(getType().getCompilationUnit());
  }

  public boolean isChecksumValid() {
    return m_oldCheckSum == m_newChecksum;
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
    if (!TypeUtility.exists(m_formDataType)) {
      IScoutBundle sharedBundle = findSharedBundle(SdkTypeUtility.getScoutProject(getType()));
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
      BundleContext bundleContext = ScoutSdk.getDefault().getBundle().getBundleContext();
      ServiceReference serviceReference = bundleContext.getServiceReference(ICreateFormDataRequest.class.getName());
      if (serviceReference != null) {
        try {
          ICreateFormDataRequest createRequest = (ICreateFormDataRequest) bundleContext.getService(serviceReference);
          boolean createFormData = createRequest.createFormData(getType(), packageName, simpleName);
          if (createFormData) {
            ScoutTypeNewOperation formDataOp = new ScoutTypeNewOperation(simpleName, packageName, sharedBundle) {
              @Override
              public void run(IProgressMonitor localMonitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
                super.run(localMonitor, workingCopyManager);
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
        finally {
          bundleContext.ungetService(serviceReference);
        }
      }
    }

    if (TypeUtility.exists(m_formDataType)) {
      FormDataSourceBuilder builder = new FormDataSourceBuilder(getType(), getFormDataAnnotation());
      builder.setElementName(m_formDataType.getElementName());
      builder.setPackageName(m_formDataType.getPackageFragment().getElementName());
      try {
        builder.createFormattedSource(monitor, SdkTypeUtility.getScoutBundle(m_formDataType).getJavaProject());
      }
      catch (Exception e) {
        ScoutSdk.logError("could not build form data builder for '" + getType().getFullyQualifiedName() + "'.", e);
      }
      m_formDataBuilder = builder;
    }
  }

  public void storeFormData(IProgressMonitor monitor) {
    if (m_formDataBuilder == null) {
      return;
    }
    if (!TypeUtility.exists(m_formDataType)) {
      return;
    }
    try {
      String oldSource = m_formDataType.getSource();
      //compare

      if (!oldSource.equals(m_formDataBuilder.getTypeSource())) {
        System.out.println("FORM DATA UPDATE");
        P_FormDataStoreOperation updateOp = new P_FormDataStoreOperation(m_formDataType, m_formDataBuilder);
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
    catch (JavaModelException e) {
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
    private final FormDataSourceBuilder m_sourceBuilder;
    private final IType m_formDataType;

    public P_FormDataStoreOperation(IType formDataType, FormDataSourceBuilder sourceBuilder) {
      m_formDataType = formDataType;
      m_sourceBuilder = sourceBuilder;
    }

    @Override
    public String getOperationName() {
      return "Update form data '" + m_formDataType.getElementName() + "'.";
    }

    @Override
    public void validate() throws IllegalArgumentException {
    }

    @Override
    public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {

      ICompilationUnit icu = m_formDataType.getCompilationUnit();
      if (icu != null) {
        icu.becomeWorkingCopy(monitor);
        try {
          icu.getBuffer().setContents(ScoutUtility.cleanLineSeparator(m_sourceBuilder.getSource(), icu));
          icu.getBuffer().save(monitor, true);
          icu.commitWorkingCopy(true, monitor);
        }
        catch (Exception e) {
          icu.discardWorkingCopy();
          ScoutSdk.logError("could not store new form data for '" + getType().getFullyQualifiedName() + "'.", e);
        }
      }

    }
  }

}
