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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.jdt.packageFragment.ExportPolicy;
import org.eclipse.scout.sdk.operation.jdt.type.PrimaryTypeNewOperation;
import org.eclipse.scout.sdk.operation.service.LookupServiceNewOperation;
import org.eclipse.scout.sdk.operation.service.ServiceRegistrationDescription;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.field.FieldSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodBodySourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
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
  private String m_serviceImplementationPackage;
  private IJavaProject m_serviceImplementationProject;
  private IType m_lookupService;
  private final List<ServiceRegistrationDescription> m_serviceRegistrationDescriptions;

  //out members
  private IType m_outLookupService;
  private IType m_outLookupServiceInterface;

  public LookupCallNewOperation(String lookupCallName, String packageName, IJavaProject project) throws JavaModelException {
    super(lookupCallName, packageName, project);
    m_serviceRegistrationDescriptions = new ArrayList<>();

    // defaults
    setFlags(Flags.AccPublic);
    setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(IRuntimeClasses.ILookupCall, project));
    setPackageExportPolicy(ExportPolicy.ADD_PACKAGE);
    setFormatSource(true);
    getCompilationUnitNewOp().setCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesCompilationUnitCommentBuilder());
    setTypeCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesTypeCommentBuilder());
  }

  @Override
  public String getOperationName() {
    return "New Lookupcall '" + getElementName() + "'...";
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
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

        StringBuilder ifcSuperInterface = new StringBuilder(IRuntimeClasses.ILookupService);
        String[] typeParams = Signature.getTypeArguments(getServiceSuperTypeSignature());
        if (typeParams != null && typeParams.length > 0) {
          ifcSuperInterface.append(Signature.C_GENERIC_START);
          ifcSuperInterface.append(Signature.toString(typeParams[0]));
          ifcSuperInterface.append(Signature.C_GENERIC_END);
        }

        serviceOp.addInterfaceInterfaceSignature(SignatureCache.createTypeSignature(ifcSuperInterface.toString()));
        serviceOp.setInterfacePackageName(getServiceInterfacePackageName());
        serviceOp.setImplementationSuperTypeSignature(getServiceSuperTypeSignature());
        serviceOp.setServiceRegistrations(getServiceRegistrations());
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
  public void validate() {
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

  public boolean addServiceRegistration(ServiceRegistrationDescription desc) {
    return m_serviceRegistrationDescriptions.add(desc);
  }

  public boolean removeServiceRegistration(ServiceRegistrationDescription desc) {
    return m_serviceRegistrationDescriptions.remove(desc);
  }

  public void setServiceRegistrations(List<ServiceRegistrationDescription> desc) {
    m_serviceRegistrationDescriptions.clear();
    m_serviceRegistrationDescriptions.addAll(desc);
  }

  public List<ServiceRegistrationDescription> getServiceRegistrations() {
    return Collections.unmodifiableList(m_serviceRegistrationDescriptions);
  }
}
