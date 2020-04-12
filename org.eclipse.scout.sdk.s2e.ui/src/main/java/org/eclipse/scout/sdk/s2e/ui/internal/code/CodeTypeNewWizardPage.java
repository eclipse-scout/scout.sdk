/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.code;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scout.sdk.core.model.api.ITypeParameter;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.s.util.ScoutTier;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.s2e.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment;
import org.eclipse.scout.sdk.s2e.ui.IScoutHelpContextIds;
import org.eclipse.scout.sdk.s2e.ui.fields.FieldToolkit;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.content.StrictHierarchyTypeContentProvider;
import org.eclipse.scout.sdk.s2e.ui.util.PackageContainer;
import org.eclipse.scout.sdk.s2e.ui.wizard.CompilationUnitNewWizardPage;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.PlatformUI;

/**
 * <h3>{@link CodeTypeNewWizardPage}</h3>
 *
 * @since 5.2.0
 */
public class CodeTypeNewWizardPage extends CompilationUnitNewWizardPage {

  private final ProposalTextField[] m_typeArgFields;
  private static final int NUM_ARG_FIELDS = 3;
  private EclipseEnvironment m_provider;

  public CodeTypeNewWizardPage(PackageContainer packageContainer) {
    super(CodeTypeNewWizardPage.class.getName(), packageContainer, ISdkProperties.SUFFIX_CODE_TYPE, IScoutRuntimeTypes.ICodeType, IScoutRuntimeTypes.AbstractCodeType, ScoutTier.Shared);
    setTitle("Create a new CodeType");
    setDescription(getTitle());
    setIcuGroupName("New CodeType Details");

    m_typeArgFields = new ProposalTextField[NUM_ARG_FIELDS];

  }

  @Override
  protected void createContent(Composite parent) {
    super.createContent(parent);
    m_provider = EclipseEnvironment.createUnsafe(env -> getControl().addDisposeListener(e -> env.close()));
    createArgumentsGroup(parent);

    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IScoutHelpContextIds.SCOUT_CODETYPE_NEW_WIZARD_PAGE);
  }

  protected void createArgumentsGroup(Composite p) {
    Group parent = FieldToolkit.createGroupBox(p, "Type Arguments");
    GridDataFactory
        .defaultsFor(parent)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .applyTo(parent);
    GridLayoutFactory
        .swtDefaults()
        .applyTo(parent);

    // type arg fields
    for (int i = 0; i < NUM_ARG_FIELDS; i++) {
      m_typeArgFields[i] = FieldToolkit.createTypeProposalField(parent, getTypeArgLabel(i), getJavaProject(), getLabelWidth());
      m_typeArgFields[i].addProposalListener(proposal -> pingStateChanging());
      GridDataFactory
          .defaultsFor(m_typeArgFields[i])
          .align(SWT.FILL, SWT.CENTER)
          .grab(true, false)
          .applyTo(m_typeArgFields[i]);
    }
    syncTypeArgFieldsToSuperType();
  }

  protected static String getTypeArgLabel(int index) {
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

  @Override
  protected void handleJavaProjectChanged() {
    super.handleJavaProjectChanged();
    if (!isControlCreated()) {
      return;
    }
    for (ProposalTextField field : m_typeArgFields) {
      ((StrictHierarchyTypeContentProvider) field.getContentProvider()).setJavaProject(getJavaProject());
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
    if (!JdtUtils.exists(superType)) {
      for (ProposalTextField field : m_typeArgFields) {
        field.setEnabled(false);
      }
    }
    else {
      List<ITypeParameter> typeParameters = m_provider.toScoutType(superType).typeParameters().collect(toList());
      for (int i = 0; i < NUM_ARG_FIELDS; i++) {
        boolean typeParamAvailable = typeParameters.size() > i;
        m_typeArgFields[i].setEnabled(typeParamAvailable);
        if (typeParamAvailable) {
          List<org.eclipse.scout.sdk.core.model.api.IType> bounds = typeParameters.get(i).bounds().collect(toList());
          StrictHierarchyTypeContentProvider typeContentProvider = (StrictHierarchyTypeContentProvider) m_typeArgFields[i].getContentProvider();
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

  public String getSuperClassReference() {
    IType superType = getSuperType();
    StringBuilder superTypeBuilder = new StringBuilder(superType.getFullyQualifiedName());
    try {
      int numParams = superType.getTypeParameters().length;
      if (numParams > 0) {
        superTypeBuilder.append(JavaTypes.C_GENERIC_START);
        for (int i = 0; i < numParams; i++) {
          if (i != 0) {
            superTypeBuilder.append(JavaTypes.C_COMMA);
          }
          String param;
          boolean appendCodeGeneric = false;
          if (i < NUM_ARG_FIELDS) {
            IType selectedProposal = (IType) m_typeArgFields[i].getSelectedProposal();
            appendCodeGeneric = selectedProposal.getTypeParameters().length > 0 && JdtUtils.hierarchyContains(selectedProposal.newSupertypeHierarchy(null), IScoutRuntimeTypes.ICode);
            param = selectedProposal.getFullyQualifiedName();
          }
          else {
            param = Object.class.getName();
          }
          superTypeBuilder.append(param);
          if (appendCodeGeneric) {
            superTypeBuilder.append(JavaTypes.C_GENERIC_START);
            superTypeBuilder.append(getCodeIdDataType());
            superTypeBuilder.append(JavaTypes.C_GENERIC_END);
          }
        }
        superTypeBuilder.append(JavaTypes.C_GENERIC_END);
      }
    }
    catch (JavaModelException e) {
      throw new SdkException(e);
    }

    return superTypeBuilder.toString();
  }

  public String getCodeIdDataType() {
    return getCodeTypeTypeArgDatatype(IScoutRuntimeTypes.TYPE_PARAM_CODETYPE__CODE_ID);
  }

  public String getCodeTypeIdDataType() {
    return getCodeTypeTypeArgDatatype(IScoutRuntimeTypes.TYPE_PARAM_CODETYPE__CODE_TYPE_ID);
  }

  protected String getCodeTypeTypeArgDatatype(int typeParamIndex) {
    org.eclipse.scout.sdk.core.model.api.IType superType = m_provider.toScoutType(getSuperType());
    org.eclipse.scout.sdk.core.model.api.IType codeTypeIdArg = superType.superTypes()
        .withSelf(false)
        .withName(IScoutRuntimeTypes.ICodeType)
        .first().get()
        .typeArguments()
        .skip(typeParamIndex)
        .findAny().get();

    if (codeTypeIdArg.isParameterType()) {
      // it is a type parameter. So the super class does not define the data type. We must check in our type argument fields
      List<ITypeParameter> typeParameters = superType.typeParameters().collect(toList());
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
        return result.getFullyQualifiedName();
      }
    }
    else {
      // the super class specifies the code type id data type
      return codeTypeIdArg.reference();
    }
    return Object.class.getName();
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
          IJavaElement selected = (IJavaElement) field.getSelectedProposal();
          if (!JdtUtils.exists(selected)) {
            return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "Please choose the type arguments.");
          }
        }
      }
    }
    return Status.OK_STATUS;
  }
}
