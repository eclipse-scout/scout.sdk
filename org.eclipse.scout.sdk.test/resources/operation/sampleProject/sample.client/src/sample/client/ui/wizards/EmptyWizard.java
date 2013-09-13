package sample.client.ui.wizards;

import org.eclipse.scout.rt.client.ui.wizard.AbstractWizard;
import org.eclipse.scout.rt.shared.TEXTS;

public class EmptyWizard extends AbstractWizard {

  public EmptyWizard() {
    super();
  }

  @Override
  protected String getConfiguredTitle() {
    return TEXTS.get("Empty");
  }
}
