package org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.ConfigPropertyMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.method.ScoutMethodDeleteOperation;
import org.eclipse.scout.sdk.ui.fields.proposal.ScoutProposalUtility;
import org.eclipse.scout.sdk.ui.internal.fields.proposal.ConstantFieldProposal;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractProposalPresenter;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class LabelHorizontalAlignmentPresenter extends AbstractProposalPresenter<ConstantFieldProposal<Integer>> {
  public static final int LEFT = -1;
  public static final int CENTER = 0;
  public static final int RIGHT = 1;
  public static final int DEFAULT = 1000;

  public LabelHorizontalAlignmentPresenter(FormToolkit toolkit, Composite parent) {
    super(toolkit, parent);
  }

  @Override
  protected void init(ConfigurationMethod method) throws CoreException {
    setProposals(ScoutProposalUtility.getLabelHorizontalAlignmentProposals());
    super.init(method);
  }

  @Override
  protected ConstantFieldProposal<Integer> parseInput(String input) throws CoreException {
    int parsedInt = PropertyMethodSourceUtility.parseReturnParameterInteger(input, getMethod().peekMethod(), getMethod().getSuperTypeHierarchy());
    return findProposal(parsedInt);
  }

  @Override
  protected void storeValue(ConstantFieldProposal<Integer> value) {
    if (value == null) {
      // set to default
      getProposalComponent().acceptProposal(getDefaultValue());
      value = getDefaultValue();
    }
    IOperation op = null;
    if (UiUtility.equals(getDefaultValue(), value)) {
      if (getMethod().isImplemented()) {
        op = new ScoutMethodDeleteOperation(getMethod().peekMethod());
      }
    }
    else {
      String sourceValue = "" + value.getConstantValue();
      op = new ConfigPropertyMethodUpdateOperation(getMethod().getType(), getMethod().getMethodName(), "  return " + sourceValue + ";", false);
    }

    if (op != null) {
      OperationJob job = new OperationJob(op);
      job.schedule();
    }
  }

  private ConstantFieldProposal<Integer> findProposal(int id) {
    if (id == DEFAULT) {
      id = DEFAULT;
    }
    else if (id > 0) {
      id = RIGHT;
    }
    else if (id == 0) {
      id = CENTER;
    }
    else {
      id = LEFT;
    }
    for (ConstantFieldProposal<Integer> prop : getProposals()) {
      if (prop.getConstantValue() == id) {
        return prop;
      }
    }
    return null;
  }
}
