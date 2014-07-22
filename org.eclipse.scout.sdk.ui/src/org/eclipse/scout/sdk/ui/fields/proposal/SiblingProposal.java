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
package org.eclipse.scout.sdk.ui.fields.proposal;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.operation.ITypeSibling;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;

/**
 * <h3>SiblingProposal</h3>
 */
public class SiblingProposal extends SimpleProposal implements ITypeSibling {
  public static final SiblingProposal SIBLING_BEGINNING = new SiblingProposal("first", SiblingType.BEGINNING);
  public static final SiblingProposal SIBLING_END = new SiblingProposal("last", SiblingType.END);

  // local data names
  private static final String DATA_JAVA_ELEMENT = "javaElement";
  private static final String DATA_SIBLING_TYPE = "siblingType";

  public SiblingProposal(IJavaElement sibling) {
    this(sibling.getElementName() + " [before]", SiblingType.SIBLING);
    setData(DATA_JAVA_ELEMENT, sibling);
  }

  public SiblingProposal(String text, SiblingType siblingType) {
    super(text, ScoutSdkUi.getImage(ScoutSdkUi.FormField));
    setData(DATA_SIBLING_TYPE, siblingType);
  }

  @Override
  public IJavaElement getElement() {
    return (IType) getData(DATA_JAVA_ELEMENT);
  }

  @Override
  public SiblingType getSiblingType() {
    return (SiblingType) getData(DATA_SIBLING_TYPE);
  }

}
