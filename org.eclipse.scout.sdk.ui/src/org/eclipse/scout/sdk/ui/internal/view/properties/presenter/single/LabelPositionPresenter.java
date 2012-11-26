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
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.LabelPositionPresenter.LabelPosition;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractProposalPresenter;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

public class LabelPositionPresenter extends AbstractProposalPresenter<LabelPosition> {

  protected static enum LabelPosition {
    Left, // 1
    OnField, // 2
    Right, // 3
    Top, // 4
    Default
    /* 0 */
  }

  public LabelPositionPresenter(PropertyViewFormToolkit toolkit, Composite parent) {
    super(toolkit, parent);
  }

  @Override
  protected void createProposalFieldProviders(ProposalTextField proposalField) {
    ILabelProvider labelProvider = new LabelProvider() {
      @Override
      public String getText(Object element) {
        LabelPosition value = (LabelPosition) element;
        switch (value) {
          case OnField:
            return "On Field";
        }
        return element.toString();
      }

      @Override
      public Image getImage(Object element) {
        LabelPosition value = (LabelPosition) element;
        switch (value) {
          case Left:
            return ScoutSdkUi.getImage(ScoutSdkUi.HorizontalLeft);
          case OnField:
            return ScoutSdkUi.getImage(ScoutSdkUi.Default);
          case Right:
            return ScoutSdkUi.getImage(ScoutSdkUi.HorizontalRight);
          case Top:
            return ScoutSdkUi.getImage(ScoutSdkUi.VerticalTop);
        }
        return ScoutSdkUi.getImage(ScoutSdkUi.Default);
      }

    };
    getProposalField().setLabelProvider(labelProvider);
    StaticContentProvider provider = new StaticContentProvider(LabelPosition.values(), labelProvider);
    getProposalField().setContentProvider(provider);
  }

  @Override
  protected LabelPosition parseInput(String input) throws CoreException {
    int parsedInt = PropertyMethodSourceUtility.parseReturnParameterInteger(input, getMethod().peekMethod(), getMethod().getSuperTypeHierarchy());
    switch (parsedInt) {
      case 1:
        return LabelPosition.Left;
      case 2:
        return LabelPosition.OnField;
      case 3:
        return LabelPosition.Right;
      case 4:
        return LabelPosition.Top;
      case 0:
        return LabelPosition.Default;
    }
    return null;
  }

  @Override
  protected void storeValue(LabelPosition value) throws CoreException {
    if (value == null) {
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
      StringBuilder source = new StringBuilder("return ");
      switch (value) {
        case Left:
          source.append("LABEL_POSITION_LEFT");
          break;
        case OnField:
          source.append("LABEL_POSITION_ON_FIELD");
          break;
        case Right:
          source.append("LABEL_POSITION_RIGHT");
          break;
        case Top:
          source.append("LABEL_POSITION_TOP");
          break;
        case Default:
          source.append("LABEL_POSITION_DEFAULT");
          break;
        default:
          ScoutSdkUi.logWarning("could not store value '" + value + "'.");
          return;
      }
      source.append(";");
      op = new ConfigPropertyMethodUpdateOperation(getMethod().getType(), getMethod().getMethodName(), source.toString(), true);
    }
    if (op != null) {
      new OperationJob(op).schedule();
    }
  }
}
