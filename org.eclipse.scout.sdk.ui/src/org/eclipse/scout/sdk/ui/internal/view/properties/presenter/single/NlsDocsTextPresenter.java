package org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.NlsDocsProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.NlsProposalTextField;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.SdkTypeUtility;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class NlsDocsTextPresenter extends NlsTextPresenter {

  public NlsDocsTextPresenter(FormToolkit toolkit, Composite parent) {
    super(toolkit, parent);
  }

  @Override
  protected Control createContent(Composite container) {
    m_proposalField = new NlsDocsProposalTextField(container, null, NlsProposalTextField.TYPE_NO_LABEL);
    toolkitAdapt(m_proposalField);
    m_proposalField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        handleProposalAccepted(event);
      }
    });
    m_proposalField.setEnabled(false);
    return m_proposalField;
  }

  public void setType(IType t) {
    ((NlsDocsProposalTextField) m_proposalField).setType(t);
  }

  @Override
  protected INlsProject getNlsProject(ConfigurationMethod method) {
    return findDocsNlsProject(method.getType());
  }

  private INlsProject findDocsNlsProject(IJavaElement element) {
    IScoutBundle scoutBundle = SdkTypeUtility.getScoutBundle(element);
    return scoutBundle.getScoutProject().getDocsNlsProject();
  }
}
