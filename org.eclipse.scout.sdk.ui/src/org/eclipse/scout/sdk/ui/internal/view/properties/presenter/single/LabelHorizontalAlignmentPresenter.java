package org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.ConfigPropertyMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.method.ScoutMethodDeleteOperation;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.StaticContentProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.LabelHorizontalAlignmentPresenter.HorizontalAlignment;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractProposalPresenter;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

public class LabelHorizontalAlignmentPresenter extends AbstractProposalPresenter<HorizontalAlignment> {

  protected static enum HorizontalAlignment {
    Left,
    Center,
    Right,
    Default
  }

  public LabelHorizontalAlignmentPresenter(PropertyViewFormToolkit toolkit, Composite parent) {
    super(toolkit, parent);
  }

  @Override
  protected void createProposalFieldProviders(ProposalTextField proposalField) {
    ILabelProvider labelProvider = new LabelProvider() {
      @Override
      public String getText(Object element) {
        return element.toString();
      }

      @Override
      public Image getImage(Object element) {
        HorizontalAlignment value = (HorizontalAlignment) element;
        switch (value) {
          case Left:
            return ScoutSdkUi.getImage(ScoutSdkUi.HorizontalLeft);
          case Center:
            return ScoutSdkUi.getImage(ScoutSdkUi.HorizontalCenter);
          case Right:
            return ScoutSdkUi.getImage(ScoutSdkUi.HorizontalRight);
        }
        return ScoutSdkUi.getImage(ScoutSdkUi.Default);
      }

    };
    getProposalField().setLabelProvider(labelProvider);
    StaticContentProvider provider = new StaticContentProvider(HorizontalAlignment.values(), labelProvider);
    getProposalField().setContentProvider(provider);
  }

  @Override
  protected HorizontalAlignment parseInput(String input) throws CoreException {
    int parsedInt = PropertyMethodSourceUtility.parseReturnParameterInteger(input, getMethod().peekMethod(), getMethod().getSuperTypeHierarchy());
    switch (parsedInt) {
      case -1:
        return HorizontalAlignment.Left;
      case 0:
        return HorizontalAlignment.Center;
      case 1:
        return HorizontalAlignment.Right;
    }
    return HorizontalAlignment.Default;
  }

  @Override
  protected void storeValue(HorizontalAlignment value) {
    if (value == null) {
      // set to default
      getProposalField().acceptProposal(getDefaultValue());
      value = getDefaultValue();
    }
    IOperation op = null;
    if (UiUtility.equals(getDefaultValue(), value)) {
      if (getMethod().isImplemented()) {
        op = new ScoutMethodDeleteOperation(getMethod().peekMethod());
      }
    }
    else {
      String sourceValue;
      switch (value) {
        case Left:
          sourceValue = "-1";
          break;
        case Center:
          sourceValue = "0";
          break;
        case Right:
          sourceValue = "1";
          break;
        default:
          sourceValue = "1000";
      }
      op = new ConfigPropertyMethodUpdateOperation(getMethod().getType(), getMethod().getMethodName(), "  return " + sourceValue + ";", true);
    }

    if (op != null) {
      OperationJob job = new OperationJob(op);
      job.schedule();
    }
  }

}
