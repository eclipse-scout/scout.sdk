package org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.nls.sdk.model.util.Language;
import org.eclipse.scout.nls.sdk.model.workspace.NlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.nls.sdk.ui.action.NlsEntryNewAction;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalSelectionHandler;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.nls.NlsTextSelectionHandler;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class NlsDocsTextPresenter extends NlsTextPresenter {

  public NlsDocsTextPresenter(PropertyViewFormToolkit toolkit, Composite parent) {
    super(toolkit, parent);

  }

  @Override
  protected ProposalTextField createContent(Composite container) {
    ProposalTextField proposalTextField = super.createContent(container);
    proposalTextField.getTextComponent().addPasteListener(new Listener() {

      @Override
      public void handleEvent(Event event) {
        String key = getNewKey("");
        NlsEntry entry = new NlsEntry(key, getNlsProject());
        Language devLang = getNlsProject().getDevelopmentLanguage();
        entry.addTranslation(devLang, event.text);
        if (!Language.LANGUAGE_DEFAULT.equals(devLang)) {
          entry.addTranslation(Language.LANGUAGE_DEFAULT, event.text);
        }
        ProposalTextField proposalField = getProposalField();
        NlsEntryNewAction action = new NlsEntryNewAction(proposalField.getShell(), getNlsProject(), entry, true);
        action.run();
        try {
          action.join();
        }
        catch (InterruptedException e) {
          ScoutSdkUi.logWarning(e);
        }
        entry = action.getEntry();
        if (entry != null) {
          proposalField.acceptProposal(entry);
          return;
        }
        else {
          proposalField.acceptProposal(proposalField.getSelectedProposal());
        }

        event.doit = false;
      }
    });
    return proposalTextField;

  }

  @Override
  protected INlsProject resolveNlsProject(ConfigurationMethod method) {
    IScoutBundle scoutBundle = ScoutTypeUtility.getScoutBundle(method.getType());
    return scoutBundle.getDocsNlsProject();
  }

  @Override
  protected IProposalSelectionHandler createSelectionHandler(INlsProject project) {
    if (project != null) {
      return new P_NlsProposalFieldSelectionHandler(project);
    }
    else {
      return null;
    }
  }

  protected String getNewKey(String value) {
    ConfigurationMethod method = getMethod();
    if (method != null && method.getType() != null) {
      IType type = method.getType();
      // if the user has not entered a new key already: generate it based on the class name
      String fqn = type.getFullyQualifiedName();
      String pckName = type.getPackageFragment().getElementName();
      value = fqn.substring(pckName.length() + 1).replace('$', '.');
    }
    return getNlsProject().generateNewKey(value);
  }

  private class P_NlsProposalFieldSelectionHandler extends NlsTextSelectionHandler {

    public P_NlsProposalFieldSelectionHandler(INlsProject nlsProject) {
      super(nlsProject);
    }

    @Override
    protected String getNewKey(String value) {
      return NlsDocsTextPresenter.this.getNewKey(value);
    }
  }
}
