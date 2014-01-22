package sample.client.ui.wizards;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.wizard.AbstractWizard;
import org.eclipse.scout.rt.client.ui.wizard.AbstractWizardStep;

public class SampleWizard extends AbstractWizard {

  public SampleWizard() {
    super();
  }

  public static class InnerForm extends AbstractForm {
    public InnerForm() throws ProcessingException {
      super();
    }
  }

  @Order(10.0)
  public class FirstStep extends AbstractWizardStep<InnerForm> {
  }

  public FirstStep getFirstStep() {
    return getStep(SampleWizard.FirstStep.class);
  }

  @Order(20.0)
  public class SecondStep extends AbstractWizardStep<InnerForm> {

  }

  public SecondStep getSecondStep() {
    return getStep(SampleWizard.SecondStep.class);
  }

  @Order(30.0)
  public class ThirdStep extends AbstractWizardStep<InnerForm> {
  }

  public ThirdStep getThirdStep() {
    return getStep(SampleWizard.ThirdStep.class);
  }
}
