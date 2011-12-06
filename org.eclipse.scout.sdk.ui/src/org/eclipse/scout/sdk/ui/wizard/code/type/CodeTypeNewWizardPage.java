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
package org.eclipse.scout.sdk.ui.wizard.code.type;

import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.CodeTypeNewOperation;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.DefaultProposalProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ITypeProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.NlsProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.NlsProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ScoutProposalUtility;
import org.eclipse.scout.sdk.ui.fields.proposal.SignatureProposal;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.extensions.CodeIdExtensionPoint;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.Regex;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3> {@link CodeTypeNewWizardPage}</h3> ...
 */
public class CodeTypeNewWizardPage extends AbstractWorkspaceWizardPage {

  final IType iCodeType = TypeUtility.getType(RuntimeClasses.ICodeType);
  final IType abstractCodeType = TypeUtility.getType(RuntimeClasses.AbstractCodeType);

  private String m_nextCodeId;
  private NlsProposal m_nlsName;
  private String m_typeName;
  private ITypeProposal m_superType;
  private SignatureProposal m_genericSignature;

  private StyledTextField m_nextCodeIdField;
  private NlsProposalTextField m_nlsNameField;
  private StyledTextField m_typeNameField;
  private ProposalTextField m_superTypeField;
  private ProposalTextField m_genericTypeField;

  // process members

  private final IScoutBundle m_sharedBundle;
  private final HashMap<String, ICodeIdFieldValueParser> m_valueParsers;

  public CodeTypeNewWizardPage(IScoutBundle sharedBundle) {
    super(CodeTypeNewWizardPage.class.getName());
    m_sharedBundle = sharedBundle;
    setTitle(Texts.get("NewCodeType"));
    setDescription(Texts.get("CreateANewCodeType"));
    m_superType = ScoutProposalUtility.getScoutTypeProposalsFor(abstractCodeType)[0];
    m_genericSignature = new SignatureProposal(Signature.createTypeSignature(Long.class.getName(), true));
    m_valueParsers = new HashMap<String, CodeTypeNewWizardPage.ICodeIdFieldValueParser>();
    initValueParsers();
  }

  private void initValueParsers() {
    // Long
    m_valueParsers.put(Signature.createTypeSignature(Long.class.getName(), true), new AbstractNumberCodeIdFieldValueParser('L') {
      @Override
      protected void parseNum(String val) throws NumberFormatException {
        Long.parseLong(val);
      }
    });

    // Integer
    m_valueParsers.put(Signature.createTypeSignature(Integer.class.getName(), true), new AbstractNumberCodeIdFieldValueParser() {
      @Override
      protected void parseNum(String val) throws NumberFormatException {
        Integer.parseInt(val);
      }
    });

    // Double
    m_valueParsers.put(Signature.createTypeSignature(Double.class.getName(), true), new AbstractNumberCodeIdFieldValueParser('D') {
      @Override
      protected void parseNum(String val) throws NumberFormatException {
        Double.parseDouble(val);
      }
    });

    // Float
    m_valueParsers.put(Signature.createTypeSignature(Float.class.getName(), true), new AbstractNumberCodeIdFieldValueParser('F') {
      @Override
      protected void parseNum(String val) throws NumberFormatException {
        Float.parseFloat(val);
      }
    });

    // String
    m_valueParsers.put(Signature.createTypeSignature(String.class.getName(), true), new ICodeIdFieldValueParser() {
      @Override
      public boolean isValid(String val) {
        return true;
      }

      @Override
      public String getSource(String val) {
        if (val == null) return null;
        return JdtUtility.toStringLiteral(val);
      }
    });

    // Boolean
    m_valueParsers.put(Signature.createTypeSignature(Boolean.class.getName(), true), new ICodeIdFieldValueParser() {
      @Override
      public boolean isValid(String val) {
        return StringUtility.isNullOrEmpty(val) || "true".equals(val) || "false".equals(val);
      }

      @Override
      public String getSource(String val) {
        return val;
      }
    });
  }

  private void setCodeIdEnabledState(SignatureProposal sigProposal) {
    boolean enabled = false;

    // calculate if the codeId field should be enabled.
    if (sigProposal != null) {
      String sig = sigProposal.getSignature();
      if (sig != null) {
        ICodeIdFieldValueParser parser = m_valueParsers.get(sig);
        enabled = parser != null;
      }
    }

    // set the current code id
    if (enabled) {
      if (StringUtility.isNullOrEmpty(getNextCodeId())) {
        setNextCodeId(CodeIdExtensionPoint.getNextCodeId(getSharedBundle().getScoutProject(), getGenericSignature().getSignature()));
      }
    }
    else {
      setNextCodeId(null);
    }

    m_nextCodeIdField.setEnabled(enabled);
  }

  @Override
  public void postActivate() {
    m_nlsNameField.setFocus();
  }

  @Override
  protected void createContent(Composite parent) {
    if (getGenericSignature() != null) {
      setNextCodeId(CodeIdExtensionPoint.getNextCodeId(getSharedBundle().getScoutProject(), getGenericSignature().getSignature()));
    }
    m_nextCodeIdField = getFieldToolkit().createStyledTextField(parent, Texts.get("CodeId"));
    m_nextCodeIdField.setText(getNextCodeId());
    m_nextCodeIdField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        m_nextCodeId = m_nextCodeIdField.getText();
        pingStateChanging();
      }
    });

    m_nlsNameField = getFieldToolkit().createNlsProposalTextField(parent, getSharedBundle().findBestMatchNlsProject(), Texts.get("Name"));
    m_nlsNameField.acceptProposal(m_nlsName);
    m_nlsNameField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        try {
          setStateChanging(true);
          INlsEntry oldEntry = null;
          if (getNlsName() != null) {
            oldEntry = getNlsName().getNlsEntry();
          }
          m_nlsName = (NlsProposal) event.proposal;
          if (m_nlsName != null) {
            if (oldEntry == null || oldEntry.getKey().equals(m_typeNameField.getModifiableText()) || StringUtility.isNullOrEmpty(m_typeNameField.getModifiableText())) {
              m_typeNameField.setText(m_nlsName.getNlsEntry().getKey());
            }
          }
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    m_typeNameField = getFieldToolkit().createStyledTextField(parent, Texts.get("TypeName"));
    m_typeNameField.setReadOnlySuffix(SdkProperties.SUFFIX_CODE_TYPE);
    m_typeNameField.setText(m_typeName);
    m_typeNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        m_typeName = m_typeNameField.getText();
        pingStateChanging();
      }
    });

    ITypeProposal[] shotList = ScoutProposalUtility.getScoutTypeProposalsFor(TypeUtility.getType(RuntimeClasses.AbstractCodeType));

    ITypeProposal[] proposals = ScoutProposalUtility.getScoutTypeProposalsFor(ScoutTypeUtility.getAbstractTypesOnClasspath(iCodeType, getSharedBundle().getJavaProject()));

    m_superTypeField = getFieldToolkit().createProposalField(parent, new DefaultProposalProvider(shotList, proposals), Texts.get("SuperType"));
    m_superTypeField.acceptProposal(m_superType);
    m_superTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        try {
          setStateChanging(true);
          m_superType = (ITypeProposal) event.proposal;

          if (getSuperType() != null && TypeUtility.isGenericType(getSuperType().getType())) {
            m_genericTypeField.setEnabled(true);
            if (getGenericSignature() == null) {
              m_genericTypeField.acceptProposal(ScoutProposalUtility.getScoutTypeProposalsFor(TypeUtility.getType(Long.class.getName()))[0]);
            }
          }
          else {
            m_genericTypeField.setEnabled(false);
          }
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    m_genericTypeField = getFieldToolkit().createSignatureProposalField(parent, getSharedBundle(), Texts.get("GenericType"));
    m_genericTypeField.acceptProposal(getGenericSignature());
    m_genericTypeField.setEnabled(getSuperType() != null && TypeUtility.isGenericType(getSuperType().getType()));
    m_genericTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        try {
          setStateChanging(true);
          m_genericSignature = (SignatureProposal) event.proposal;
          setCodeIdEnabledState(getGenericSignature());
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    // layout
    parent.setLayout(new GridLayout(1, true));

    m_nextCodeIdField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_nlsNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_typeNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_superTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_genericTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  @Override
  public boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    CodeTypeNewOperation op = new CodeTypeNewOperation();
    // write back members
    op.setSharedBundle(getSharedBundle());
    op.setTypeName(getTypeName());
    if (getNlsName() != null) {
      op.setNlsEntry(getNlsName().getNlsEntry());
    }

    ITypeProposal superTypeProp = getSuperType();
    if (superTypeProp != null) {
      String sig = null;
      if (getGenericSignature() != null) {
        sig = Signature.createTypeSignature(superTypeProp.getType().getFullyQualifiedName() + "<" + Signature.toString(getGenericSignature().getSignature()) + ">", true);
      }
      else {
        sig = Signature.createTypeSignature(superTypeProp.getType().getFullyQualifiedName(), true);
      }
      op.setSuperTypeSignature(sig);
    }
    if (getGenericSignature() != null && getGenericSignature().getSignature() != null && !StringUtility.isNullOrEmpty(getNextCodeId())) {
      ICodeIdFieldValueParser parser = m_valueParsers.get(getGenericSignature().getSignature());
      if (parser != null) {
        op.setNextCodeId(parser.getSource(getNextCodeId()));
      }
    }
    if (getGenericSignature() != null) {
      op.setGenericTypeSignature(getGenericSignature().getSignature());
    }
    op.setFormatSource(true);
    op.validate();
    op.run(monitor, workingCopyManager);
    return true;
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    try {
      multiStatus.add(getStatusCodeIdField());
      multiStatus.add(getStatusNameField());
      multiStatus.add(getStatusSuperType());
      multiStatus.add(getStatusGenericType());
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logError("could not validate name field.", e);
    }
  }

  public IScoutBundle getSharedBundle() {
    return m_sharedBundle;
  }

  protected IStatus getStatusCodeIdField() throws JavaModelException {
    if (getGenericSignature() != null && getGenericSignature().getSignature() != null && !StringUtility.isNullOrEmpty(getNextCodeId())) {
      ICodeIdFieldValueParser parser = m_valueParsers.get(getGenericSignature().getSignature());

      if (parser != null) {
        if (!parser.isValid(getNextCodeId())) {
          return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("CodeIdNotValid"));
        }
      }
      else {
        // we have a code id but no parser that can handle it -> should not happen -> error
        return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("CodeIdNotValid"));
      }
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusNameField() throws JavaModelException {
    if (StringUtility.isNullOrEmpty(getTypeName()) || getTypeName().equals(SdkProperties.SUFFIX_CODE_TYPE)) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("Error_fieldNull"));
    }
    // check not allowed names
    if (TypeUtility.existsType(getSharedBundle().getPackageName(IScoutBundle.SHARED_PACKAGE_APPENDIX_SERVICES_CODE) + "." + getTypeName())) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("Error_nameAlreadyUsed"));
    }
    if (Regex.REGEX_WELLFORMD_JAVAFIELD.matcher(getTypeName()).matches()) {
      return Status.OK_STATUS;
    }
    else if (Regex.REGEX_JAVAFIELD.matcher(getTypeName()).matches()) {
      return new Status(IStatus.WARNING, ScoutSdkUi.PLUGIN_ID, Texts.get("Warning_notWellformedJavaName"));
    }
    else {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("Error_invalidFieldX", getTypeName()));
    }
  }

  protected IStatus getStatusSuperType() throws JavaModelException {
    if (getSuperType() == null) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("TheSuperTypeCanNotBeNull"));
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusGenericType() throws JavaModelException {
    if (getSuperType() != null && TypeUtility.isGenericType(getSuperType().getType())) {
      if (getGenericSignature() == null) {
        return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("GenericTypeCanNotBeNull"));
      }
    }
    return Status.OK_STATUS;
  }

  public void setNextCodeId(String nextCodeId) {
    try {
      setStateChanging(true);
      m_nextCodeId = nextCodeId;
      if (isControlCreated()) {
        m_nextCodeIdField.setText(nextCodeId);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  public String getNextCodeId() {
    return m_nextCodeId;
  }

  public NlsProposal getNlsName() {
    return m_nlsName;
  }

  public void setNlsName(NlsProposal nlsName) {
    try {
      setStateChanging(true);
      m_nlsName = nlsName;
      if (isControlCreated()) {
        m_nlsNameField.acceptProposal(nlsName);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  public String getTypeName() {
    return m_typeName;
  }

  public void setTypeName(String typeName) {
    try {
      setStateChanging(true);
      m_typeName = typeName;
      if (isControlCreated()) {
        m_typeNameField.setText(typeName);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  public ITypeProposal getSuperType() {
    return m_superType;
  }

  public void setSuperType(ITypeProposal superType) {
    try {
      setStateChanging(true);
      m_superType = superType;
      if (isControlCreated()) {
        m_superTypeField.acceptProposal(superType);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  public void setGenericSignature(SignatureProposal genericSignature) {
    try {
      setStateChanging(true);
      m_genericSignature = genericSignature;
      if (isControlCreated()) {
        m_genericTypeField.acceptProposal(genericSignature);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  public SignatureProposal getGenericSignature() {
    return m_genericSignature;
  }

  private static interface ICodeIdFieldValueParser {
    boolean isValid(String val);

    String getSource(String val);
  }

  private abstract static class AbstractNumberCodeIdFieldValueParser implements ICodeIdFieldValueParser {

    private final String m_numTypeSuffixLo;
    private final String m_numTypeSuffixUp;

    private AbstractNumberCodeIdFieldValueParser() {
      this(null);
    }

    private AbstractNumberCodeIdFieldValueParser(Character numTypeSuffix) {
      if (numTypeSuffix != null) {
        m_numTypeSuffixLo = (numTypeSuffix + "").toLowerCase();
        m_numTypeSuffixUp = (numTypeSuffix + "").toUpperCase();
      }
      else {
        m_numTypeSuffixLo = null;
        m_numTypeSuffixUp = null;
      }
    }

    protected abstract void parseNum(String val) throws NumberFormatException;

    @Override
    public final boolean isValid(String val) {
      try {
        if (m_numTypeSuffixLo != null && m_numTypeSuffixUp != null) {
          val = val.replaceAll("[" + m_numTypeSuffixUp + m_numTypeSuffixLo + "]{0,1}$", "");
        }
        parseNum(val);
        return true;
      }
      catch (NumberFormatException e) {
      }
      return false;
    }

    @Override
    public final String getSource(String val) {
      if (val == null) {
        return null;
      }
      else if (m_numTypeSuffixLo != null && val.toLowerCase().endsWith(m_numTypeSuffixLo)) {
        return val;
      }
      else {
        if (m_numTypeSuffixLo == null) {
          return val;
        }
        else {
          return val + m_numTypeSuffixLo;
        }
      }
    }
  }
}
