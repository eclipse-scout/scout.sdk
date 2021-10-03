/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.code;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scout.sdk.core.apidef.IClassNameSupplier;
import org.eclipse.scout.sdk.core.s.ISdkConstants;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
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
import org.eclipse.scout.sdk.s2e.ui.wizard.AbstractCompilationUnitNewWizardPage;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

/**
 * <h3>{@link CodeTypeNewWizardPage}</h3>
 *
 * @since 5.2.0
 */
public class CodeTypeNewWizardPage extends AbstractCompilationUnitNewWizardPage {

  private final ProposalTextField[] m_typeArgFields;
  private static final int NUM_ARG_FIELDS = 3;
  private EclipseEnvironment m_provider;

  public CodeTypeNewWizardPage(PackageContainer packageContainer) {
    super(CodeTypeNewWizardPage.class.getName(), packageContainer, ISdkConstants.SUFFIX_CODE_TYPE, ScoutTier.Shared);
    setTitle("Create a new CodeType");
    setDescription(getTitle());
    setIcuGroupName("New CodeType Details");

    m_typeArgFields = new ProposalTextField[NUM_ARG_FIELDS];
  }

  @Override
  protected Optional<IClassNameSupplier> calcSuperTypeDefaultFqn() {
    return scoutApi().map(IScoutApi::AbstractCodeType);
  }

  @Override
  protected Optional<IClassNameSupplier> calcSuperTypeDefaultBaseFqn() {
    return scoutApi().map(IScoutApi::ICodeType);
  }

  @Override
  protected void createContent(Composite parent) {
    super.createContent(parent);
    m_provider = EclipseEnvironment.createUnsafe(env -> getControl().addDisposeListener(e -> env.close()));
    createArgumentsGroup(parent);

    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IScoutHelpContextIds.SCOUT_CODETYPE_NEW_WIZARD_PAGE);
  }

  protected void createArgumentsGroup(Composite p) {
    var parent = FieldToolkit.createGroupBox(p, "Type Arguments");
    GridDataFactory
        .defaultsFor(parent)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .applyTo(parent);
    GridLayoutFactory
        .swtDefaults()
        .applyTo(parent);

    // type arg fields
    for (var i = 0; i < NUM_ARG_FIELDS; i++) {
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
    for (var field : m_typeArgFields) {
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
    var superType = getSuperType();
    if (!JdtUtils.exists(superType)) {
      for (var field : m_typeArgFields) {
        field.setEnabled(false);
      }
    }
    else {
      var typeParameters = m_provider.toScoutType(superType).typeParameters().collect(toList());
      for (var i = 0; i < NUM_ARG_FIELDS; i++) {
        var typeParamAvailable = typeParameters.size() > i;
        m_typeArgFields[i].setEnabled(typeParamAvailable);
        if (typeParamAvailable) {
          var bounds = typeParameters.get(i).bounds().collect(toList());
          var typeContentProvider = (StrictHierarchyTypeContentProvider) m_typeArgFields[i].getContentProvider();
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

  public String getSuperClassReference(EclipseEnvironment environment) {
    var superType = getSuperType();
    var superTypeBuilder = new StringBuilder(superType.getFullyQualifiedName());
    try {
      var numParams = superType.getTypeParameters().length;
      if (numParams > 0) {
        superTypeBuilder.append(JavaTypes.C_GENERIC_START);
        for (var i = 0; i < numParams; i++) {
          if (i != 0) {
            superTypeBuilder.append(JavaTypes.C_COMMA);
          }
          String param;
          var appendCodeGeneric = false;
          if (i < NUM_ARG_FIELDS) {
            var selectedProposal = (IType) m_typeArgFields[i].getSelectedProposal();
            var scoutApi = scoutApi();
            appendCodeGeneric = selectedProposal.getTypeParameters().length > 0
                && scoutApi.isPresent()
                && JdtUtils.hierarchyContains(selectedProposal.newSupertypeHierarchy(null), scoutApi.orElseThrow().ICode().fqn());
            param = selectedProposal.getFullyQualifiedName();
          }
          else {
            param = Object.class.getName();
          }
          superTypeBuilder.append(param);
          if (appendCodeGeneric) {
            superTypeBuilder.append(JavaTypes.C_GENERIC_START);
            superTypeBuilder.append(getCodeIdDataType(environment));
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

  public String getCodeIdDataType(EclipseEnvironment environment) {
    var iCodeTypeApi = scoutApi().orElseThrow().ICodeType();
    return getCodeTypeTypeArgDatatype(iCodeTypeApi.codeIdTypeParamIndex(), iCodeTypeApi, environment);
  }

  public String getCodeTypeIdDataType(EclipseEnvironment environment) {
    var iCodeTypeApi = scoutApi().orElseThrow().ICodeType();
    return getCodeTypeTypeArgDatatype(iCodeTypeApi.codeTypeIdTypeParamIndex(), iCodeTypeApi, environment);
  }

  protected String getCodeTypeTypeArgDatatype(int typeParamIndex, IClassNameSupplier iCodeTypeApi, EclipseEnvironment environment) {
    var superType = environment.toScoutType(getSuperType()); // don't use m_provider here because it might already have been closed.
    var codeTypeIdArg = superType.superTypes()
        .withSelf(false)
        .withName(iCodeTypeApi.fqn())
        .first().orElseThrow()
        .typeArguments()
        .skip(typeParamIndex)
        .findAny().orElseThrow();

    if (codeTypeIdArg.isParameterType()) {
      // it is a type parameter. So the super class does not define the data type. We must check in our type argument fields
      var typeParameters = superType.typeParameters().collect(toList());
      var index = IntStream.range(0, typeParameters.size())
          .filter(i -> typeParameters.get(i).elementName().equals(codeTypeIdArg.elementName()))
          .findFirst()
          .orElse(-1);
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
      var missing = Arrays.stream(m_typeArgFields)
          .filter(ProposalTextField::isEnabled)
          .map(field -> (IJavaElement) field.getSelectedProposal())
          .anyMatch(selected -> !JdtUtils.exists(selected));
      if (missing) {
        return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "Please choose the type arguments.");
      }
    }
    return Status.OK_STATUS;
  }
}
