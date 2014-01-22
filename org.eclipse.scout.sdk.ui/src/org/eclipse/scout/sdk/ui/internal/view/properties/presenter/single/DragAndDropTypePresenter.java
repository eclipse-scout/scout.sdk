/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.StaticContentProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.styled.SearchRangeStyledLabelProvider;
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

/**
 * <h3>{@link DragAndDropTypePresenter}</h3>
 * 
 * @author Matthias Villiger
 * @since 3.10.0 02.12.2013
 */
public class DragAndDropTypePresenter extends AbstractProposalPresenter<FieldProperty<Integer>> {

  protected static final List<FieldProperty<Integer>> PROPOSALS;
  static {
    IType iDNDSupport = TypeUtility.getType(IRuntimeClasses.IDNDSupport);
    PROPOSALS = new ArrayList<FieldProperty<Integer>>(5);
    PROPOSALS.add(new NoneFieldProperty());
    PROPOSALS.add(new UiFieldProperty<Integer>(iDNDSupport.getField("TYPE_FILE_TRANSFER"), "File Transfer"));
    PROPOSALS.add(new UiFieldProperty<Integer>(iDNDSupport.getField("TYPE_JAVA_ELEMENT_TRANSFER"), "Java Element Transfer"));
    PROPOSALS.add(new UiFieldProperty<Integer>(iDNDSupport.getField("TYPE_TEXT_TRANSFER"), "Text Transfer"));
    PROPOSALS.add(new UiFieldProperty<Integer>(iDNDSupport.getField("TYPE_IMAGE_TRANSFER"), "Image Transfer"));
  }

  private final FieldReferencePropertyParser<Integer> m_parser;

  public DragAndDropTypePresenter(PropertyViewFormToolkit toolkit, Composite parent) {
    super(toolkit, parent);
    m_parser = new IntegerFieldReferencePropertyParser(PROPOSALS);
  }

  @Override
  protected void createProposalFieldProviders(ProposalTextField proposalField) {
    ILabelProvider labelProvider = new SearchRangeStyledLabelProvider() {
      @Override
      public String getText(Object element) {
        return element.toString();
      }

      @Override
      public Image getImage(Object element) {
        return ScoutSdkUi.getImage(ScoutSdkUi.Default);
      }

    };
    getProposalField().setLabelProvider(labelProvider);
    StaticContentProvider provider = new StaticContentProvider(PROPOSALS.toArray(new FieldProperty[PROPOSALS.size()]), labelProvider);
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

  private static final class NoneFieldProperty extends UiFieldProperty<Integer> {
    private NoneFieldProperty() {
      super(new VirtualNoneField(), "None");
    }

    @Override
    public Integer getSourceValue() throws JavaModelException {
      return Integer.valueOf(0);
    }
  }

  @SuppressWarnings("restriction")
  private static final class VirtualNoneField extends org.eclipse.jdt.internal.core.ResolvedSourceField {
    public VirtualNoneField() {
      super(null, "0", null);
    }

    @Override
    public String getElementName() {
      return "0";
    }
  }
}
