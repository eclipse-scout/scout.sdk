package org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.StaticContentProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractProposalPresenter;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.config.ConfigPropertyUpdateOperation;
import org.eclipse.scout.sdk.workspace.type.config.parser.FieldReferencePropertyParser;
import org.eclipse.scout.sdk.workspace.type.config.parser.IntegerFieldReferencePropertyParser;
import org.eclipse.scout.sdk.workspace.type.config.property.FieldProperty;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

public class LabelPositionPresenter extends AbstractProposalPresenter<FieldProperty<Integer>> {

  protected static final UiFieldProperty<Integer> LABEL_POSITION_DEFAULT;
  protected static final UiFieldProperty<Integer> LABEL_POSITION_LEFT;
  protected static final UiFieldProperty<Integer> LABEL_POSITION_ON_FIELD;
  protected static final UiFieldProperty<Integer> LABEL_POSITION_RIGHT;
  protected static final UiFieldProperty<Integer> LABEL_POSITION_TOP;

  protected static final List<FieldProperty<Integer>> PROPOSALS;
  static {
    IType iFormField = TypeUtility.getType(IRuntimeClasses.IFormField);
    LABEL_POSITION_DEFAULT = new UiFieldProperty<Integer>(iFormField.getField("LABEL_POSITION_DEFAULT"), "default");
    LABEL_POSITION_LEFT = new UiFieldProperty<Integer>(iFormField.getField("LABEL_POSITION_LEFT"), "left");
    LABEL_POSITION_ON_FIELD = new UiFieldProperty<Integer>(iFormField.getField("LABEL_POSITION_ON_FIELD"), "on field");
    LABEL_POSITION_RIGHT = new UiFieldProperty<Integer>(iFormField.getField("LABEL_POSITION_RIGHT"), "right");
    LABEL_POSITION_TOP = new UiFieldProperty<Integer>(iFormField.getField("LABEL_POSITION_TOP"), "top");
    PROPOSALS = new ArrayList<FieldProperty<Integer>>(5);
    PROPOSALS.add(LABEL_POSITION_DEFAULT);
    PROPOSALS.add(LABEL_POSITION_LEFT);
    PROPOSALS.add(LABEL_POSITION_ON_FIELD);
    PROPOSALS.add(LABEL_POSITION_RIGHT);
    PROPOSALS.add(LABEL_POSITION_TOP);
  }

  private final FieldReferencePropertyParser<Integer> m_parser;

  public LabelPositionPresenter(PropertyViewFormToolkit toolkit, Composite parent) {
    super(toolkit, parent);
    m_parser = new IntegerFieldReferencePropertyParser(PROPOSALS);
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
        if (CompareUtility.equals(LABEL_POSITION_LEFT, element)) {
          return ScoutSdkUi.getImage(ScoutSdkUi.HorizontalLeft);
        }
        else if (CompareUtility.equals(LABEL_POSITION_RIGHT, element)) {
          return ScoutSdkUi.getImage(ScoutSdkUi.HorizontalRight);
        }
        else if (CompareUtility.equals(LABEL_POSITION_TOP, element)) {
          return ScoutSdkUi.getImage(ScoutSdkUi.VerticalTop);
        }
        else {
          return ScoutSdkUi.getImage(ScoutSdkUi.Default);
        }
      }

    };
    getProposalField().setLabelProvider(labelProvider);
    StaticContentProvider provider = new StaticContentProvider(PROPOSALS.toArray(new UiFieldProperty[PROPOSALS.size()]), labelProvider);
    getProposalField().setContentProvider(provider);
  }

  public FieldReferencePropertyParser<Integer> getParser() {
    return m_parser;
  }

  @Override
  protected FieldProperty<Integer> parseInput(String input) throws CoreException {
    return getParser().parseSourceValue(input, getMethod().peekMethod(), getMethod().getSuperTypeHierarchy());
  }

  @Override
  protected synchronized void storeValue(FieldProperty<Integer> value) throws CoreException {
    if (value == null) {
      getProposalField().acceptProposal(getDefaultValue());
      value = getDefaultValue();
    }
    try {
      ConfigPropertyUpdateOperation<FieldProperty<Integer>> updateOp = new ConfigPropertyUpdateOperation<FieldProperty<Integer>>(getMethod(), getParser());
      updateOp.setValue(value);
      OperationJob job = new OperationJob(updateOp);
      job.setDebug(true);
      job.schedule();
    }
    catch (Exception e) {
      ScoutSdkUi.logError("could not parse default value of method '" + getMethod().getMethodName() + "' in type '" + getMethod().getType().getFullyQualifiedName() + "'.", e);
    }
  }
}
