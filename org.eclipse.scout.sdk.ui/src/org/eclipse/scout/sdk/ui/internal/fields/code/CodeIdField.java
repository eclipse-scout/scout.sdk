package org.eclipse.scout.sdk.ui.internal.fields.code;

import java.util.HashMap;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.SignatureProposal;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.extensions.CodeIdExtensionPoint;
import org.eclipse.scout.sdk.ui.wizard.WizardPageFieldToolkit;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class CodeIdField extends Composite {

  private final static HashMap<String, ICodeIdFieldValueParser> valueParsers = new HashMap<String, ICodeIdFieldValueParser>(6);
  static {
    // Long
    valueParsers.put(Signature.createTypeSignature(Long.class.getName(), true), new AbstractNumberCodeIdFieldValueParser('L') {
      @Override
      protected void parseNum(String val) throws NumberFormatException {
        Long.parseLong(val);
      }
    });

    // Integer
    valueParsers.put(Signature.createTypeSignature(Integer.class.getName(), true), new AbstractNumberCodeIdFieldValueParser() {
      @Override
      protected void parseNum(String val) throws NumberFormatException {
        Integer.parseInt(val);
      }
    });

    // Double
    valueParsers.put(Signature.createTypeSignature(Double.class.getName(), true), new AbstractNumberCodeIdFieldValueParser('D') {
      @Override
      protected void parseNum(String val) throws NumberFormatException {
        Double.parseDouble(val);
      }
    });

    // Float
    valueParsers.put(Signature.createTypeSignature(Float.class.getName(), true), new AbstractNumberCodeIdFieldValueParser('F') {
      @Override
      protected void parseNum(String val) throws NumberFormatException {
        Float.parseFloat(val);
      }
    });

    // String
    valueParsers.put(Signature.createTypeSignature(String.class.getName(), true), new ICodeIdFieldValueParser() {
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
    valueParsers.put(Signature.createTypeSignature(Boolean.class.getName(), true), new ICodeIdFieldValueParser() {
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
    m_nextCodeIdField = new WizardPageFieldToolkit().createStyledTextField(this, Texts.get("CodeId"));

    setLayout(new GridLayout(1, false));
    m_nextCodeIdField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  private void setCodeIdFieldState() {
    boolean enabled = false;
    String sigProposal = getGenericSignature();

    // calculate if the codeId field should be enabled.
    if (sigProposal != null) {
      ICodeIdFieldValueParser parser = valueParsers.get(sigProposal);
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
    if (getGenericTypeField() == null || getGenericTypeField().getSelectedProposal() == null) return null;
    return ((SignatureProposal) getGenericTypeField().getSelectedProposal()).getSignature();
  }

  public void addModifyListener(ModifyListener listener) {
    m_nextCodeIdField.addModifyListener(listener);
  }

  public void removeModifyListener(ModifyListener listener) {
    m_nextCodeIdField.removeModifyListener(listener);
  }

  public IStatus getStatus() throws JavaModelException {
    if (getGenericSignature() != null && !StringUtility.isNullOrEmpty(getValue())) {
      ICodeIdFieldValueParser parser = valueParsers.get(getGenericSignature());

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
      ICodeIdFieldValueParser parser = valueParsers.get(getGenericSignature());
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

  private static interface ICodeIdFieldValueParser {
    boolean isValid(String val);

    String getSource(String val);
  }

  private static abstract class AbstractNumberCodeIdFieldValueParser implements ICodeIdFieldValueParser {

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
          return val + m_numTypeSuffixUp;
        }
      }
    }
  }
}
