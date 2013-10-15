/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.workspace.dto;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.extensions.targetpackage.IDefaultTargetPackage;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.internal.workspace.dto.IDtoAutoUpdateOperation;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.jdt.packageFragment.ExportPolicy;
import org.eclipse.scout.sdk.operation.jdt.type.PrimaryTypeNewOperation;
import org.eclipse.scout.sdk.operation.util.OrganizeImportOperation;
import org.eclipse.scout.sdk.operation.util.SourceFormatOperation;
import org.eclipse.scout.sdk.service.IMessageBoxService.YesNo;
import org.eclipse.scout.sdk.service.MessageBoxServiceFactory;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>{@link AbstractDtoAutoUpdateOperation}</h3>
 * 
 * @author aho
 * @since 3.10.0 16.08.2013
 */
public abstract class AbstractDtoAutoUpdateOperation implements IDtoAutoUpdateOperation {

  private final IType m_modelType;
  private IType m_derivedType;

  public AbstractDtoAutoUpdateOperation(IType modelType) {
    m_modelType = modelType;
  }

  @Override
  public String getOperationName() {
    return "Update DTO for '" + getModelType().getElementName() + "'";
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof AbstractDtoAutoUpdateOperation)) {
      return false;
    }
    return m_modelType.equals(((AbstractDtoAutoUpdateOperation) obj).m_modelType);
  }

  @Override
  public int hashCode() {
    return m_modelType.hashCode();
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (!TypeUtility.exists(getModelType())) {
      throw new IllegalArgumentException("model type must exist: [" + getModelType() + "]");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    String backup = ScoutUtility.getUsername();
    try {
      ScoutUtility.setUsernameForThread("Scout robot");
      runImpl(monitor, workingCopyManager);
    }
    finally {
      ScoutUtility.setUsernameForThread(backup);
    }
  }

  protected void runImpl(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    IType derivedType = ensureDerivedType();
    if (getModelType().equals(derivedType)) {
      ScoutSdk.logError("DTO Auto Update cannot be performed when the DTO annotation points to itself.");
      return;
    }

    if (TypeUtility.exists(derivedType)) {
      String oldSource = derivedType.getCompilationUnit().getSource();
      String newSource = createDerivedTypeSource(monitor);

      // format source
      SourceFormatOperation op = new SourceFormatOperation(derivedType.getJavaProject(), new Document(newSource), null);
      op.validate();
      op.run(monitor, null);
      newSource = op.getDocument().get();

      // compare
      if (!isSourceEquals(oldSource, newSource)) {
        // write source
        P_FormDataStoreOperation storeOp = new P_FormDataStoreOperation(getModelType(), derivedType.getCompilationUnit(), newSource);
        if (workingCopyManager != null) {
          storeOp.run(monitor, workingCopyManager);
        }
        else {
          OperationJob job = new OperationJob(storeOp);
          job.schedule();
        }
      }
    }
  }

  protected void consumeAllTypeNamesRec(ITypeSourceBuilder builder, IImportValidator validator) {
    String fqn = builder.getFullyQualifiedName();
    validator.getTypeName(SignatureCache.createTypeSignature(fqn));
    for (ITypeSourceBuilder child : builder.getTypeSourceBuilder()) {
      consumeAllTypeNamesRec(child, validator);
    }
  }

  protected abstract String getDerivedTypeSignature() throws CoreException;

  protected abstract String createDerivedTypeSource(IProgressMonitor monitor) throws CoreException;

  @Override
  public IType getModelType() {
    return m_modelType;
  }

  public IType getDerivedType() {
    return m_derivedType;
  }

  protected IType ensureDerivedType() throws CoreException {
    if (TypeUtility.exists(m_derivedType)) {
      return m_derivedType;
    }

    IType type = null;
    String signature = getDerivedTypeSignature();
    if (!StringUtility.isNullOrEmpty(signature)) {
      type = TypeUtility.getTypeBySignature(signature);
      if (!TypeUtility.exists(type)) {
        IScoutBundle clientBundle = ScoutTypeUtility.getScoutBundle(getModelType().getJavaProject());
        IScoutBundle sharedBundle = clientBundle.getParentBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SHARED), false);
        if (sharedBundle == null) {
          return null;
        }

        String packageName = Signature.getSignatureQualifier(signature);
        if (StringUtility.isNullOrEmpty(packageName)) {
          packageName = sharedBundle.getDefaultPackage(IDefaultTargetPackage.SHARED_SERVICES);
        }
        String simpleName = (Signature.getSignatureSimpleName(Signature.getTypeErasure(signature)));
        final String question = Texts.get("ModelDataExistsConfirmationMessage", packageName + "." + simpleName, getModelType().getElementName());
        if (MessageBoxServiceFactory.getMessageBoxService().showYesNoQuestion(getOperationName(), question, YesNo.YES) == YesNo.YES) {

          PrimaryTypeNewOperation formDataOp = new PrimaryTypeNewOperation(simpleName, packageName, sharedBundle.getJavaProject());
          formDataOp.setFlags(Flags.AccPublic);
          formDataOp.setSuperTypeSignature(SignatureCache.createTypeSignature(RuntimeClasses.AbstractFormData));
          formDataOp.setPackageExportPolicy(ExportPolicy.AddPackage);
          OperationJob job = new OperationJob(formDataOp);
          job.schedule();
          try {
            job.join();
          }
          catch (InterruptedException e) {
            ScoutSdk.logError("could not join form data compilation unit create job.", e);
          }
          type = formDataOp.getCreatedType();
        }
      }
    }
    m_derivedType = type;
    return type;
  }

  private static boolean isSourceEquals(String source1, String source2) {
    if (source1 == null && source2 == null) {
      return true;
    }
    else if (source1 == null) {
      return false;
    }
    else if (source2 == null) {
      return false;
    }
    String sourceTrimmed1 = source1.trim();
    String sourceTrimmed2 = source2.trim();
    if (sourceTrimmed1.length() != sourceTrimmed2.length()) {
      return false;
    }

    return sourceTrimmed1.equals(sourceTrimmed2);
  }

  private static class P_FormDataStoreOperation implements IOperation {
    private final String m_icuSource;
    private final ICompilationUnit m_derivedType;
    private final IType m_type;

    public P_FormDataStoreOperation(IType type, ICompilationUnit derivedType, String icuSource) {
      m_type = type;
      m_derivedType = derivedType;
      m_icuSource = icuSource;
    }

    @Override
    public String getOperationName() {
      return "Update form data '" + m_derivedType.getElementName() + "'.";
    }

    @Override
    public void validate() throws IllegalArgumentException {
    }

    @Override
    public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
      ICompilationUnit icu = m_derivedType;
      try {
        icu.becomeWorkingCopy(monitor);

        // store new form data content to buffer
        icu.getBuffer().setContents(ScoutUtility.cleanLineSeparator(m_icuSource, icu));

        // save buffer
        icu.getBuffer().save(monitor, true);

        // organize import required to ensure the JDT settings for the imports are applied
        OrganizeImportOperation o = new OrganizeImportOperation(icu);
        o.validate();
        o.run(monitor, workingCopyManager);

        icu.commitWorkingCopy(true, monitor);
      }
      catch (Exception e) {
        ScoutSdk.logError("could not store DTO for '" + m_type.getFullyQualifiedName() + "'.", e);
      }
      finally {
        icu.discardWorkingCopy();
      }
    }
  }
}
