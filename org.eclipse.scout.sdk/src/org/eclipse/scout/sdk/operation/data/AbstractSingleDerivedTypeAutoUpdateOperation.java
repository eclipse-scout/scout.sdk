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
package org.eclipse.scout.sdk.operation.data;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.IMessageBoxService.YesNo;
import org.eclipse.scout.sdk.MessageBoxServiceFactory;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.targetpackage.IDefaultTargetPackage;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.jobs.AbstractWorkspaceBlockingJob;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.ManifestExportPackageOperation;
import org.eclipse.scout.sdk.operation.form.formdata.FormDataUtility;
import org.eclipse.scout.sdk.operation.form.formdata.ITypeSourceBuilder;
import org.eclipse.scout.sdk.operation.util.ScoutTypeNewOperation;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * Abstract implementation that supports the creation of one {@link IType} that is derived form the original Scout model
 * class.
 * 
 * @since 3.10.0-M1
 */
public abstract class AbstractSingleDerivedTypeAutoUpdateOperation implements IAutoUpdateOperation {

  private final IType m_modelType;
  private IType m_derivedModelType;

  public AbstractSingleDerivedTypeAutoUpdateOperation(IType modelType) {
    m_modelType = modelType;
  }

  @Override
  public IType getModelType() {
    return m_modelType;
  }

  protected IType getDerivedModelType() {
    return m_derivedModelType;
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (!TypeUtility.exists(getModelType())) {
      throw new IllegalArgumentException("model type must exist: [" + getModelType() + "]");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    if (prepare()) {
      m_derivedModelType = generateDerivedType(monitor, workingCopyManager);
    }
  }

  /**
   * Prepares the update operation.
   * 
   * @return Returns <code>true</code> if the update can be performed. Otherwise <code>false</code>.
   */
  protected abstract boolean prepare();

  /**
   * @return Returns the type signature of the derived type.
   */
  protected abstract String getDerivedTypeSignature();

  /**
   * Creates a source builder for the given derived type.
   * 
   * @param derivedType
   * @return
   */
  protected abstract ITypeSourceBuilder createTypeSourceBuilder(IType derivedType);

  /**
   * Checks the given existing derived type's type hierarchy.
   * 
   * @param type
   * @param hierarchy
   * @return Returns <code>true</code> if the given existing type can be modified. Otherwise <code>false</code>.
   */
  protected abstract boolean checkExistingDerivedTypeSuperTypeHierarchy(IType type, ITypeHierarchy hierarchy);

  /**
   * Generates the one derived {@link IType}.
   * 
   * @param monitor
   * @param workingCopyManager
   */
  protected IType generateDerivedType(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    String signature = getDerivedTypeSignature();
    IType type = TypeUtility.getTypeBySignature(signature);
    if (TypeUtility.exists(type)) {
      if (!checkExistingPrimaryType(type)) {
        return null;
      }
    }
    else {
      type = createPrimaryType(monitor, workingCopyManager, signature);
    }
    if (type == null) {
      return null;
    }
    String source = createSource(monitor, type);
    if (source == null) {
      return null;
    }
    return writePrimaryTypeContents(monitor, workingCopyManager, type, source);
  }

  /**
   * Returns <code>true</code> if the given existing derived type fulfills all requirements, so that it can be updated.
   * 
   * @param type
   * @return Returns <code>true</code> if the existing type can be overridden, otherwise <code>false</code>.
   */
  protected boolean checkExistingPrimaryType(IType type) {
    // type is read-only
    if (type.isReadOnly()) {
      MessageBoxServiceFactory.getMessageBoxService().showWarning(getOperationName(), "The class '" + type.getFullyQualifiedName() + "' is read only. Operation is aborted.");
      return false;
    }

    // type has unusual super type
    ITypeHierarchy existingPageDataHierarchy = TypeUtility.getSuperTypeHierarchy(type);
    if (!checkExistingDerivedTypeSuperTypeHierarchy(type, existingPageDataHierarchy)) {
      if (MessageBoxServiceFactory.getMessageBoxService().showYesNoQuestion(getOperationName(), "Are you sure to replace '" + type.getFullyQualifiedName() + "' with a new generated data class?", YesNo.NO) == YesNo.NO) {
        return false;
      }
    }

    return true;
  }

  /**
   * Creates a primary type with the given signature.
   * 
   * @param monitor
   * @param workingCopyManager
   * @param signature
   * @return Returns the generated {@link IType} or <code>null</code> if it could not have been generated.
   */
  protected IType createPrimaryType(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager, String signature) {
    IScoutBundle clientBundle = ScoutTypeUtility.getScoutBundle(getModelType().getJavaProject());
    IScoutBundle sharedBundle = clientBundle.getParentBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SHARED), false);
    if (sharedBundle == null) {
      return null;
    }

    String packageName = Signature.getSignatureQualifier(signature);
    if (StringUtility.isNullOrEmpty(packageName)) {
      packageName = sharedBundle.getDefaultPackage(IDefaultTargetPackage.SHARED_SERVICES);
    }
    String simpleName = Signature.getSignatureSimpleName(Signature.getTypeErasure(signature));

    final String question = Texts.get("ModelDataExistsConfirmationMessage", packageName + "." + simpleName, getModelType().getElementName());
    if (MessageBoxServiceFactory.getMessageBoxService().showYesNoQuestion(getOperationName(), question, YesNo.YES) == YesNo.YES) {
      ScoutTypeNewOperation createPrimaryTypeOp = new ScoutTypeNewOperation(simpleName, packageName, sharedBundle) {
        @Override
        public void run(IProgressMonitor localMonitor, IWorkingCopyManager localWorkingCopyManager) throws CoreException {
          super.run(localMonitor, localWorkingCopyManager);
          // ensure the package of the page data is exported in the shared plug-in
          ManifestExportPackageOperation manifestOp = new ManifestExportPackageOperation(ManifestExportPackageOperation.TYPE_ADD_WHEN_NOT_EMTPY, new IPackageFragment[]{getCreatedType().getPackageFragment()}, true);
          manifestOp.run(localMonitor, localWorkingCopyManager);
          localWorkingCopyManager.register(getModelType().getCompilationUnit(), localMonitor);
          getModelType().getCompilationUnit().createImport(getCreatedType().getFullyQualifiedName(), null, localMonitor);
        }
      };

      if (runOrElevateToWorkspaceBlockingJob(monitor, workingCopyManager, createPrimaryTypeOp)) {
        return createPrimaryTypeOp.getCreatedType();
      }
    }

    return null;
  }

  /**
   * Creates the derived type's source.
   * 
   * @param monitor
   * @param type
   * @return Returns the derived type's source or <code>null</code>.
   */
  protected String createSource(IProgressMonitor monitor, IType type) {
    ITypeSourceBuilder sourceBuilder = createTypeSourceBuilder(type);
    if (sourceBuilder == null) {
      return null;
    }
    try {
      int flags = Flags.AccPublic;
      try {
        if (Flags.isAbstract(getModelType().getFlags())) {
          flags |= Flags.AccAbstract;
        }
      }
      catch (JavaModelException e) {
        ScoutSdk.logWarning("could not determine abstract flag of '" + getModelType().getFullyQualifiedName() + "'.", e);
      }
      sourceBuilder.setFlags(flags);
      String packageName = type.getPackageFragment().getElementName();
      IJavaProject project = ScoutTypeUtility.getScoutBundle(type).getJavaProject();
      String icuSource = FormDataUtility.createCompilationUnitSource(sourceBuilder, packageName, project, monitor);
      return icuSource;
    }
    catch (Exception e) {
      ScoutSdk.logError("could not build form data builder for '" + getModelType().getFullyQualifiedName() + "'.", e);
    }
    return null;
  }

  /**
   * Writes the contents of the existing given type if it differs.
   * 
   * @param monitor
   * @param workingCopyManager
   * @param type
   * @param source
   */
  protected IType writePrimaryTypeContents(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager, IType type, String source) {
    if (monitor.isCanceled() || StringUtility.isNullOrEmpty(source) || !TypeUtility.exists(type)) {
      return null;
    }
    try {
      String oldSource = FormDataUtility.getTypeSource(type.getSource(), type.getElementName());
      //compare
      String newSource = FormDataUtility.getTypeSource(source, type.getElementName());
      if (oldSource != null && newSource != null) {
        newSource = newSource.trim();
        oldSource = oldSource.trim();
        if (!oldSource.equals(newSource)) {
          WritePrimaryTypeContentsOperation updateOp = new WritePrimaryTypeContentsOperation(type, source);
          if (!runOrElevateToWorkspaceBlockingJob(monitor, workingCopyManager, updateOp)) {
            return null;
          }
        }
      }
    }
    catch (Exception e) {
      ScoutSdk.logError("Exception while storig derived resource '" + type.getElementName() + "' for model type '" + getModelType().getFullyQualifiedName() + "'.", e);
    }
    return type;
  }

  /**
   * Performs the operation if the given working copy manager is not null. Otherwise it is performed within an
   * {@link AbstractWorkspaceBlockingJob}.
   * 
   * @param monitor
   * @param workingCopyManager
   * @param operation
   * @return Returns <code>true</code> if the operation completed successfully, otherwise <code>false</code>.
   */
  protected boolean runOrElevateToWorkspaceBlockingJob(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager, IOperation operation) {
    if (workingCopyManager != null) {
      try {
        operation.run(monitor, workingCopyManager);
      }
      catch (Throwable t) {
        ScoutSdk.logError("opeartion failed: [" + operation.getOperationName() + "]", t);
        return false;
      }
      return true;
    }

    // elevate to workspace blocking job
    OperationJob operationJob = new OperationJob(operation);
    operationJob.schedule();
    try {
      operationJob.join();
    }
    catch (InterruptedException e) {
      ScoutSdk.logError("Joining on elevated operation failed.", e);
    }
    return operationJob.getResult().isOK();
  }
}
