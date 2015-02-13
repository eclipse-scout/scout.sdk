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
package org.eclipse.scout.sdk.ws.jaxws.executor;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.corext.template.java.SignatureUtil;
import org.eclipse.jdt.ui.actions.OpenNewClassWizardAction;
import org.eclipse.jdt.ui.wizards.NewClassWizardPage;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.ui.executor.AbstractExecutor;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsRuntimeClasses;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link AbstractTypeNewExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 14.10.2014
 */
@SuppressWarnings("restriction")
public abstract class AbstractTypeNewExecutor extends AbstractExecutor {

  private String m_packageFragment;
  private boolean m_allowModifyPackageFragment;
  private String m_superTypeSignature;
  private boolean m_allowModifySuperType;
  private List<String> m_interfaceTypeSignatures;
  private boolean m_allowModifyInterfaces;
  private String m_typeName;
  private IScoutBundle m_bundle;

  @Override
  public boolean canRun(IStructuredSelection selection) {
    m_bundle = UiUtility.getScoutBundleFromSelection(selection);
    return isEditable(m_bundle);
  }

  protected abstract void init(IScoutBundle bundle);

  @Override
  public Object run(Shell shell, IStructuredSelection selection, ExecutionEvent event) {
    try {
      init(m_bundle);
      openNewTypeDialog();
    }
    catch (JavaModelException e) {
      JaxWsSdk.logError("Error while creating new type.", e);
    }
    return null;
  }

  protected String stripSignatureToFQN(String signature, boolean eraseGenericTypes) {
    if (signature == null) {
      return null;
    }

    String typeSignature = SignatureUtil.stripSignatureToFQN(signature);
    if (!eraseGenericTypes) {
      // support for generic type parameters
      String typeArguments = null;
      for (String typeArgument : Signature.getTypeArguments(signature)) {
        typeArguments = StringUtility.join(",", typeArguments, SignatureUtil.stripSignatureToFQN(typeArgument));
      }
      if (typeArguments != null) {
        typeSignature = typeSignature + Signature.C_GENERIC_START + typeArguments + Signature.C_GENERIC_END;
      }
    }

    return typeSignature;
  }

  protected IType openNewTypeDialog() throws JavaModelException {
    NewClassWizardPage page = new NewClassWizardPage();
    page.setEnclosingTypeSelection(false, false);
    page.setMethodStubSelection(false, true, true, true);
    IPackageFragment packageFragment = null;
    packageFragment = m_bundle.getPackageFragment(m_packageFragment);

    if (packageFragment != null) {
      page.setPackageFragmentRoot((IPackageFragmentRoot) packageFragment.getParent(), m_allowModifyPackageFragment);
      page.setPackageFragment(packageFragment, m_allowModifyPackageFragment);
    }
    else {
      String rootPackageName = m_bundle.getSymbolicName();
      page.setPackageFragmentRoot((IPackageFragmentRoot) m_bundle.getPackageFragment(rootPackageName).getParent(), true);
    }
    if (m_superTypeSignature != null) {
      page.setSuperClass(stripSignatureToFQN(m_superTypeSignature, false), m_allowModifySuperType);
    }
    if (m_interfaceTypeSignatures != null && m_interfaceTypeSignatures.size() > 0) {
      List<String> interfaceTypeNames = new LinkedList<>();
      for (String signature : m_interfaceTypeSignatures) {
        interfaceTypeNames.add(stripSignatureToFQN(signature, false));
      }
      page.setSuperInterfaces(interfaceTypeNames, m_allowModifyInterfaces);
    }

    if (m_typeName != null) {
      page.setTypeName(Signature.getSimpleName(m_typeName), true);
    }
    OpenNewClassWizardAction action = new OpenNewClassWizardAction();
    action.setConfiguredWizardPage(page);
    action.run();

    IType createdType = page.getCreatedType();
    if (TypeUtility.exists(createdType)) {
      IWorkingCopyManager level = ScoutSdkCore.createWorkingCopyManger();
      try {
        level.register(createdType.getCompilationUnit(), new NullProgressMonitor());
        TypeUtility.getType(createdType.getFullyQualifiedName());
        level.reconcile(createdType.getCompilationUnit(), new NullProgressMonitor());
        TypeUtility.getPrimaryTypeHierarchy(TypeUtility.getType(JaxWsRuntimeClasses.IServerSessionFactory)).invalidate();
      }
      catch (CoreException e) {
        JaxWsSdk.logError(e);
      }
      level.unregisterAll(new NullProgressMonitor());

      return createdType;
    }
    return null;
  }

  protected void setSuperTypeSignature(String superTypeSignature, boolean allowModify) {
    m_superTypeSignature = superTypeSignature;
    m_allowModifySuperType = allowModify;
  }

  protected void setInterfaceTypeSignatures(List<String> interfaceTypeSignatures, boolean allowModify) {
    m_interfaceTypeSignatures = interfaceTypeSignatures;
    m_allowModifyInterfaces = allowModify;
  }

  protected void setPackageFragment(String packageFragment, boolean allowModify) {
    m_packageFragment = packageFragment;
    m_allowModifyPackageFragment = allowModify;
  }

  protected void setTypeName(String typeName) {
    m_typeName = typeName;
  }
}
