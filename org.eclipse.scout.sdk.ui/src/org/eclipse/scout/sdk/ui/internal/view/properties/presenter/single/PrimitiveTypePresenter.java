/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single;

import java.util.Date;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.javaelement.JavaElementContentProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.javaelement.JavaElementLabelProvider;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractTypeProposalPresenter;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>PrimitiveTypePresenter</h3> A proposal presenter which allows to select a primitive wrapper type or
 * String
 */
public class PrimitiveTypePresenter extends AbstractTypeProposalPresenter {

  private static final IType[] PRIMITIVE_TYPES = new IType[]{
      TypeUtility.getType(String.class.getName()),
      TypeUtility.getType(Double.class.getName()),
      TypeUtility.getType(Float.class.getName()),
      TypeUtility.getType(Long.class.getName()),
      TypeUtility.getType(Integer.class.getName()),
      TypeUtility.getType(Short.class.getName()),
      TypeUtility.getType(Date.class.getName()),
      TypeUtility.getType(Byte.class.getName())};

  public PrimitiveTypePresenter(PropertyViewFormToolkit toolkit, Composite parent) {
    super(toolkit, parent);
  }

  @Override
  protected void createProposalFieldProviders(ProposalTextField proposalField) {
    JavaElementLabelProvider labelProvider = new JavaElementLabelProvider();
    getProposalField().setLabelProvider(labelProvider);
    JavaElementContentProvider contentProvider = new JavaElementContentProvider(labelProvider, PRIMITIVE_TYPES);
    getProposalField().setContentProvider(contentProvider);
  }
}
