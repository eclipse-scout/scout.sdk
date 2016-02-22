/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.internal.wizard.code;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.core.IJavaRuntimeTypes;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.ITypeParameter;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.s2e.CachingJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.IJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.internal.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.ui.IScoutHelpContextIds;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.IProposalListener;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.content.TypeContentProvider;
import org.eclipse.scout.sdk.s2e.ui.util.PackageContainer;
import org.eclipse.scout.sdk.s2e.ui.wizard.CompilationUnitNewWizardPage;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.scout.sdk.s2e.util.ScoutTier;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.PlatformUI;

/**
 * <h3>{@link CodeTypeNewWizardPage}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class CodeTypeNewWizardPage extends CompilationUnitNewWizardPage {

  private final IJavaEnvironmentProvider m_provider;
  private final ProposalTextField[] m_typeArgFields;
  private static final int NUM_ARG_FIELDS = 3;

  public CodeTypeNewWizardPage(PackageContainer packageContainer) {
    super(CodeTypeNewWizardPage.class.getName(), packageContainer, ISdkProperties.SUFFIX_CODE_TYPE, IScoutRuntimeTypes.ICodeType, IScoutRuntimeTypes.AbstractCodeType, ScoutTier.Shared);
    setTitle("Create a new CodeType");
    setDescription(getTitle());
    setIcuGroupName("New CodeType Details");
    m_provider = new CachingJavaEnvironmentProvider();
    m_typeArgFields = new ProposalTextField[NUM_ARG_FIELDS];
  }

  @Override
  protected void createContent(Composite parent) {
    super.createContent(parent);

    createArgumentsGroup(parent);

    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IScoutHelpContextIds.SCOUT_CODETYPE_NEW_WIZARD_PAGE);
  }

  protected void createArgumentsGroup(Composite p) {
    Group parent = getFieldToolkit().createGroupBox(p, "Type Arguments");
    parent.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    parent.setLayout(new GridLayout(1, true));

    // type arg fields
    for (int i = 0; i < NUM_ARG_FIELDS; i++) {
      m_typeArgFields[i] = getFieldToolkit().createTypeProposalField(parent, getTypeArgLabel(i), getJavaProject());
      m_typeArgFields[i].setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
      m_typeArgFields[i].addProposalListener(new IProposalListener() {
        @Override
        public void proposalAccepted(Object proposal) {
          pingStateChanging();
        }
      });
    }
    syncTypeArgFieldsToSuperType();
  }

  protected String getTypeArgLabel(int index) {
    switch (index) {
      case 0:
        return "First Argument";
      case 1:
        return "Second Argument";
      case 2:
        return "Third Argument";
      default:
        throw new SdkException("unsupported index: " + index);
    }
  }

  protected IJavaEnvironment getEnvironment() {
    return m_provider.get(getJavaProject());
  }

  @Override
  protected void handleJavaProjectChanged() {
    super.handleJavaProjectChanged();
    if (!isControlCreated()) {
      return;
    }
    for (ProposalTextField field : m_typeArgFields) {
      ((TypeContentProvider) field.getContentProvider()).setJavaProject(getJavaProject());
    }
  }

  @Override
  protected void handleSuperTypeChanged() {
    super.handleSuperTypeChanged();
    if (isControlCreated()) {
      syncTypeArgFieldsToSuperType();
    }
  }

  protected void syncTypeArgFieldsToSuperType() {
    IType superType = getSuperType();
    if (!S2eUtils.exists(superType)) {
      for (ProposalTextField field : m_typeArgFields) {
        field.setEnabled(false);
      }
    }
    else {
      List<ITypeParameter> typeParameters = S2eUtils.jdtTypeToScoutType(superType, getEnvironment()).typeParameters();
      for (int i = 0; i < NUM_ARG_FIELDS; i++) {
        boolean typeParamAvailable = typeParameters.size() > i;
        m_typeArgFields[i].setEnabled(typeParamAvailable);
        if (typeParamAvailable) {
          List<org.eclipse.scout.sdk.core.model.api.IType> bounds = typeParameters.get(i).bounds();
          TypeContentProvider typeContentProvider = (TypeContentProvider) m_typeArgFields[i].getContentProvider();
          if (bounds.isEmpty()) {
            typeContentProvider.setBaseClassFqn(null);
          }
          else {
            typeContentProvider.setBaseClassFqn(bounds.get(0).name());
          }
        }
      }
    }
  }

  public String getSuperTypeSignature() {
    IType superType = getSuperType();
    StringBuilder superTypeBuilder = new StringBuilder(superType.getFullyQualifiedName());
    try {
      int numParams = superType.getTypeParameters().length;
      if (numParams > 0) {
        superTypeBuilder.append(ISignatureConstants.C_GENERIC_START);
        for (int i = 0; i < numParams; i++) {
          if (i != 0) {
            superTypeBuilder.append(',');
          }
          String param = null;
          boolean appendCodeGeneric = false;
          if (i < NUM_ARG_FIELDS) {
            IType selectedProposal = (IType) m_typeArgFields[i].getSelectedProposal();
            appendCodeGeneric = selectedProposal.getTypeParameters().length > 0 && S2eUtils.hierarchyContains(selectedProposal.newSupertypeHierarchy(null), IScoutRuntimeTypes.ICode);
            param = selectedProposal.getFullyQualifiedName();
          }
          else {
            param = IJavaRuntimeTypes.java_lang_Object;
          }
          superTypeBuilder.append(param);
          if (appendCodeGeneric) {
            superTypeBuilder.append(ISignatureConstants.C_GENERIC_START);
            superTypeBuilder.append(SignatureUtils.toFullyQualifiedName(getCodeIdDatatypeSignature()));
            superTypeBuilder.append(ISignatureConstants.C_GENERIC_END);
          }
        }
        superTypeBuilder.append(ISignatureConstants.C_GENERIC_END);
      }
    }
    catch (JavaModelException e) {
      throw new SdkException(e);
    }

    return Signature.createTypeSignature(superTypeBuilder.toString());
  }

  public String getCodeIdDatatypeSignature() {
    return getCodeTypeTypeArgDatatypeSig(IScoutRuntimeTypes.TYPE_PARAM_CODETYPE__CODE_ID);
  }

  public String getCodeTypeIdDatatypeSignature() {
    return getCodeTypeTypeArgDatatypeSig(IScoutRuntimeTypes.TYPE_PARAM_CODETYPE__CODE_TYPE_ID);
  }

  protected String getCodeTypeTypeArgDatatypeSig(int typeParamIndex) {
    org.eclipse.scout.sdk.core.model.api.IType superType = S2eUtils.jdtTypeToScoutType(getSuperType(), getEnvironment());
    org.eclipse.scout.sdk.core.model.api.IType codeTypeIdArg = superType.superTypes().withName(IScoutRuntimeTypes.ICodeType).first().typeArguments().get(typeParamIndex);
    if (codeTypeIdArg.isParameterType()) {
      // it is a type parameter. So the super class does not define the data type. We must check in our type argument fields
      List<ITypeParameter> typeParameters = superType.typeParameters();
      int index = -1;
      for (int i = 0; i < typeParameters.size(); i++) {
        if (typeParameters.get(i).elementName().equals(codeTypeIdArg.elementName())) {
          index = i;
          break;
        }
      }
      IType result = null;
      if (index >= 0 && index < NUM_ARG_FIELDS) {
        result = (IType) m_typeArgFields[index].getSelectedProposal();
      }
      if (result != null) {
        return Signature.createTypeSignature(result.getFullyQualifiedName());
      }
    }
    else {
      // the super class specifies the code type id data type
      return codeTypeIdArg.signature();
    }
    return Signature.createTypeSignature(IJavaRuntimeTypes.java_lang_Object);
  }

  @Override
  public CodeTypeNewWizard getWizard() {
    return (CodeTypeNewWizard) super.getWizard();
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    super.validatePage(multiStatus);
    multiStatus.add(getStatusTypeArgFields());
  }

  protected IStatus getStatusTypeArgFields() {
    if (isControlCreated()) {
      for (ProposalTextField field : m_typeArgFields) {
        if (field.isEnabled()) {
          IType selected = (IType) field.getSelectedProposal();
          if (!S2eUtils.exists(selected)) {
            return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "Please choose the type arguments.");
          }
        }
      }
    }
    return Status.OK_STATUS;
  }
}
