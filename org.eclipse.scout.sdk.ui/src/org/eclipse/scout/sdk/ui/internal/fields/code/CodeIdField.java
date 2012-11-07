package org.eclipse.scout.sdk.ui.internal.fields.code;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.extensions.ICodeIdParser;
import org.eclipse.scout.sdk.ui.fields.FieldToolkit;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.extensions.CodeIdExtensionPoint;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class CodeIdField extends Composite {

  private StyledTextField m_nextCodeIdField;
  private ProposalTextField m_genericTypeField;

  private final IScoutProject m_project;
  private final IProposalAdapterListener m_genericFieldListener;

  public CodeIdField(Composite parent, IScoutProject project) {
    super(parent, SWT.NONE);
    m_project = project;
    m_genericFieldListener = new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        setCodeIdFieldState();
      }
    };

    createContent();
  }

  private void createContent() {
    m_nextCodeIdField = new FieldToolkit().createStyledTextField(this, Texts.get("CodeId"));

    setLayout(new GridLayout(1, false));
    m_nextCodeIdField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  private void setCodeIdFieldState() {
    boolean enabled = false;
    String sigProposal = getGenericSignature();

    // calculate if the codeId field should be enabled.
    if (sigProposal != null) {
      ICodeIdParser parser = CodeIdExtensionPoint.getCodeIdParser(sigProposal);
      enabled = parser != null;
    }

    // set the current code id
    if (enabled) {
      if (StringUtility.isNullOrEmpty(getValue())) {
        setValue(CodeIdExtensionPoint.getNextCodeId(m_project, getGenericSignature()));
      }
    }
    else {
      setValue(null);
    }

    m_nextCodeIdField.setEnabled(enabled);
  }

  private String getGenericSignature() {
    if (getGenericTypeField() != null && getGenericTypeField().getSelectedProposal() != null) {
      return (String) getGenericTypeField().getSelectedProposal();
    }
    return null;
  }

  public void addModifyListener(ModifyListener listener) {
    m_nextCodeIdField.addModifyListener(listener);
  }

  public void removeModifyListener(ModifyListener listener) {
    m_nextCodeIdField.removeModifyListener(listener);
  }

  public IStatus getStatus() throws JavaModelException {
    if (getGenericSignature() != null && !StringUtility.isNullOrEmpty(getValue())) {
      ICodeIdParser parser = CodeIdExtensionPoint.getCodeIdParser(getGenericSignature());

      if (parser != null) {
        if (!parser.isValid(getValue())) {
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

  public void setValue(String nextCodeId) {
    m_nextCodeIdField.setText(nextCodeId);
  }

  public String getValue() {
    return m_nextCodeIdField.getText();
  }

  public String getValueSource() {
    if (getGenericSignature() != null && !StringUtility.isNullOrEmpty(getValue())) {
      ICodeIdParser parser = CodeIdExtensionPoint.getCodeIdParser(getGenericSignature());
      if (parser != null) {
        return parser.getSource(getValue());
      }
    }
    return null;
  }

  public void setGenericTypeField(ProposalTextField genericTypeField) {
    if (genericTypeField != m_genericTypeField) {
      // remove old listener
      if (m_genericTypeField != null) {
        m_genericTypeField.removeProposalAdapterListener(m_genericFieldListener);
      }

      m_genericTypeField = genericTypeField;
      m_genericTypeField.addProposalAdapterListener(m_genericFieldListener);

      // generic field has changed -> the generic type could have changed
      setCodeIdFieldState();
    }
  }

  public ProposalTextField getGenericTypeField() {
    return m_genericTypeField;
  }
}
