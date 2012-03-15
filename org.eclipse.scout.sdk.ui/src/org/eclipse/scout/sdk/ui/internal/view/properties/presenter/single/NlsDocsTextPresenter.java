package org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalSelectionHandler;
import org.eclipse.scout.sdk.ui.fields.proposal.nls.NlsTextSelectionHandler;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.swt.widgets.Composite;

public class NlsDocsTextPresenter extends NlsTextPresenter {

  public NlsDocsTextPresenter(PropertyViewFormToolkit toolkit, Composite parent) {
    super(toolkit, parent);
  }

  @Override
  protected INlsProject resolveNlsProject(ConfigurationMethod method) {
    IScoutBundle scoutBundle = ScoutTypeUtility.getScoutBundle(method.getType());
    return scoutBundle.getScoutProject().getDocsNlsProject();
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

  private class P_NlsProposalFieldSelectionHandler extends NlsTextSelectionHandler {

    public P_NlsProposalFieldSelectionHandler(INlsProject nlsProject) {
      super(nlsProject);
    }

    @Override
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
  }
}
