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
package org.eclipse.scout.sdk.operation.lookupcall;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.jdt.packageFragment.ExportPolicy;
import org.eclipse.scout.sdk.operation.jdt.type.PrimaryTypeNewOperation;
import org.eclipse.scout.sdk.operation.service.LookupServiceNewOperation;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.field.FieldSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodBodySourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 *
 */
public class LookupCallNewOperation extends PrimaryTypeNewOperation {
  // in members
  private String m_serviceSuperTypeSignature;
  private IJavaProject m_serviceProxyRegistrationProject;
  private String m_serviceInterfacePackageName;
  private IJavaProject m_serviceInterfaceProject;
  private IJavaProject m_serviceRegistrationProject;
  private String m_serviceImplementationPackage;
  private IJavaProject m_serviceImplementationProject;
  private IType m_lookupService;

  //out members
  private IType m_outLookupService;
  private IType m_outLookupServiceInterface;

  public LookupCallNewOperation(String lookupCallName, String packageName, IJavaProject project) throws JavaModelException {
    super(lookupCallName, packageName, project);

    // defaults
    setFlags(Flags.AccPublic);
    setSuperTypeSignature(SignatureCache.createTypeSignature(RuntimeClasses.LookupCall));
    setPackageExportPolicy(ExportPolicy.AddPackage);
    setFormatSource(true);
    getCompilationUnitNewOp().setCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesCompilationUnitCommentBuilder());
    setTypeCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesTypeCommentBuilder());
  }

  @Override
  public String getOperationName() {
    return "New Lookupcall '" + getElementName() + "'...";
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    String namePrefix = getElementName();
    namePrefix = namePrefix.replaceAll(SdkProperties.SUFFIX_LOOKUP_CALL + "$", "");
    // service
    IType lookupServiceInterface = getLookupService();
    if (lookupServiceInterface == null) {
      if (!StringUtility.isNullOrEmpty(getServiceSuperTypeSignature())) {
        LookupServiceNewOperation serviceOp = new LookupServiceNewOperation(namePrefix + SdkProperties.SUFFIX_LOOKUP_SERVICE);
        serviceOp.addProxyRegistrationProject(getServiceProxyRegistrationProject());
        serviceOp.setImplementationProject(getServiceImplementationProject());
        serviceOp.setImplementationPackageName(getServiceImplementationPackage());
        serviceOp.setInterfaceProject(getServiceInterfaceProject());
        serviceOp.addInterfaceInterfaceSignature(SignatureCache.createTypeSignature(RuntimeClasses.ILookupService));
        serviceOp.setInterfacePackageName(getServiceInterfacePackageName());
        serviceOp.setImplementationSuperTypeSignature(getServiceSuperTypeSignature());
        serviceOp.addServiceRegistrationProject(getServiceRegistrationProject());
        serviceOp.validate();
        serviceOp.run(monitor, workingCopyManager);
        lookupServiceInterface = serviceOp.getCreatedServiceInterface();
        m_outLookupService = serviceOp.getCreatedServiceImplementation();
      }
    }
    m_outLookupServiceInterface = lookupServiceInterface;
    // lookup call
    addFieldSourceBuilder(FieldSourceBuilderFactory.createSerialVersionUidBuilder());
    if (getOutLookupServiceInterface() != null) {
      // getConfiguredService method
      IMethodSourceBuilder getConfiguredServiceBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(getSourceBuilder(), "getConfiguredService");
      getConfiguredServiceBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
        @Override
        public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
          source.append("  return ").append(validator.getTypeName(SignatureCache.createTypeSignature(getOutLookupServiceInterface().getFullyQualifiedName()))).append(".class;");
        }
      });
      addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(getConfiguredServiceBuilder), getConfiguredServiceBuilder);
    }

    super.run(monitor, workingCopyManager);
  }

  @Override
  public void validate() throws IllegalArgumentException {
  }

  public void setServiceProxyRegistrationProject(IJavaProject serviceProxyRegistrationProject) {
    m_serviceProxyRegistrationProject = serviceProxyRegistrationProject;
  }

  public IJavaProject getServiceProxyRegistrationProject() {
    return m_serviceProxyRegistrationProject;
  }

  public void setServiceInterfaceProject(IJavaProject serviceInterfaceProject) {
    m_serviceInterfaceProject = serviceInterfaceProject;
  }

  public IJavaProject getServiceInterfaceProject() {
    return m_serviceInterfaceProject;
  }

  public void setServiceSuperTypeSignature(String serviceSuperTypeSignature) {
    m_serviceSuperTypeSignature = serviceSuperTypeSignature;
  }

  public String getServiceSuperTypeSignature() {
    return m_serviceSuperTypeSignature;
  }

  public IJavaProject getServiceRegistrationProject() {
    return m_serviceRegistrationProject;
  }

  public void setServiceRegistrationProject(IJavaProject serviceRegistrationProject) {
    m_serviceRegistrationProject = serviceRegistrationProject;
  }

  public void setServiceImplementationProject(IJavaProject serviceImplementationProject) {
    m_serviceImplementationProject = serviceImplementationProject;
  }

  public IJavaProject getServiceImplementationProject() {
    return m_serviceImplementationProject;
  }

  public void setLookupService(IType lookupService) {
    m_lookupService = lookupService;
  }

  public IType getLookupService() {
    return m_lookupService;
  }

  public IType getOutLookupService() {
    return m_outLookupService;
  }

  public IType getOutLookupServiceInterface() {
    return m_outLookupServiceInterface;
  }

  public String getServiceImplementationPackage() {
    return m_serviceImplementationPackage;
  }

  public void setServiceImplementationPackage(String serviceImplementationPackage) {
    m_serviceImplementationPackage = serviceImplementationPackage;
  }

  public String getServiceInterfacePackageName() {
    return m_serviceInterfacePackageName;
  }

  public void setServiceInterfacePackageName(String serviceInterfacePackageName) {
    m_serviceInterfacePackageName = serviceInterfacePackageName;
  }

}
