package org.eclipse.scout.sdk.ui.fields.proposal;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.swt.widgets.Composite;

public class NlsDocsProposalTextField extends NlsProposalTextField {
  private IType m_type;

  public NlsDocsProposalTextField(Composite parent, INlsProject nlsProject, int type) {
    super(parent, nlsProject, type);
  }

  @Override
  protected String getNewKey(String value) {
    if (getType() != null) {
      // if the user has not entered a new key already: generate it based on the class name
      String fqn = getType().getFullyQualifiedName();
      String pckName = getType().getPackageFragment().getElementName();
      value = fqn.substring(pckName.length() + 1).replace('$', '.');
    }
    return super.getNewKey(value);
  }

  public void setType(IType type) {
    m_type = type;
  }

  public IType getType() {
    return m_type;
  }
}
