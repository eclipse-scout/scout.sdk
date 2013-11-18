/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
/**
 *
 */
package org.eclipse.scout.sdk.ws.jaxws.swt.action;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
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
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsRuntimeClasses;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.swt.widgets.Shell;

@SuppressWarnings("restriction")
public abstract class TypeNewAction extends AbstractLinkAction {
  private IScoutBundle m_bundle;
  private String m_packageFragment;
  private boolean m_allowModifyPackageFragment;
  private String m_superTypeSignature;
  private boolean m_allowModifySuperType;
  private List<String> m_interfaceTypeSignatures;
  private boolean m_allowModifyInterfaces;
  private String m_typeName;
  private IType m_createdType;

  public TypeNewAction(String label) {
    super(Texts.get("Action_newTypeX", label), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ToolAdd));
    setLinkText(Texts.get("here"));
    setLeadingText(Texts.get("CreateNewXByClicking", label));
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    try {
      m_createdType = openNewTypeDialog();
    }
    catch (JavaModelException e) {
      throw new ExecutionException("", e);
    }
    return null;
  }

  @Override
  public boolean isVisible() {
    return !m_bundle.isBinary();
  }

  public void init(IScoutBundle bundle) {
    m_bundle = bundle;
  }

  protected IType openNewTypeDialog() throws JavaModelException {
    NewClassWizardPage page = new NewClassWizardPage();
    page.setDescription(getLabel());
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
      List<String> interfaceTypeNames = new LinkedList<String>();
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

    m_createdType = page.getCreatedType();
    if (TypeUtility.exists(m_createdType)) {
      IWorkingCopyManager level = ScoutSdkCore.createWorkingCopyManger();
      try {
        level.register(m_createdType.getCompilationUnit(), new NullProgressMonitor());
        TypeUtility.getType(m_createdType.getFullyQualifiedName());
        level.reconcile(m_createdType.getCompilationUnit(), new NullProgressMonitor());
        TypeUtility.getPrimaryTypeHierarchy(TypeUtility.getType(JaxWsRuntimeClasses.IServerSessionFactory)).invalidate();
      }
      catch (CoreException e) {
        JaxWsSdk.logError(e);
      }
      level.unregisterAll(new NullProgressMonitor());

      return m_createdType;
    }
    return null;
  }

  private String stripSignatureToFQN(String signature, boolean eraseGenericTypes) {
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

  public IScoutBundle getBundle() {
    return m_bundle;
  }

  public void setBundle(IScoutBundle bundle) {
    m_bundle = bundle;
  }

  public void setSuperTypeSignature(String superTypeSignature, boolean allowModify) {
    m_superTypeSignature = superTypeSignature;
    m_allowModifySuperType = allowModify;
  }

  public void setInterfaceTypeSignatures(List<String> interfaceTypeSignatures, boolean allowModify) {
    m_interfaceTypeSignatures = interfaceTypeSignatures;
    m_allowModifyInterfaces = allowModify;
  }

  public void setPackageFragment(String packageFragment, boolean allowModify) {
    m_packageFragment = packageFragment;
    m_allowModifyPackageFragment = allowModify;
  }

  public IType getCreatedType() {
    return m_createdType;
  }

  public void setTypeName(String typeName) {
    m_typeName = typeName;
  }
}
