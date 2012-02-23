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
package org.eclipse.scout.sdk.ui.view.properties;

import org.eclipse.scout.sdk.ui.fields.FieldToolkit;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * <h3>{@link PropertyViewFormToolkit}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 12.02.2012
 */
public class PropertyViewFormToolkit extends FormToolkit {

  private FieldToolkit m_fieldToolkit;

  /**
   * @param display
   */
  public PropertyViewFormToolkit(Display display) {
    super(display);
    m_fieldToolkit = new FieldToolkit();
  }

  public ProposalTextField createProposalField(Composite parent, int style) {
    return createProposalField(parent, null, style);
  }

  /**
   * @param parent
   * @param label
   * @return
   * @see org.eclipse.scout.sdk.ui.fields.FieldToolkit#createProposalField(org.eclipse.swt.widgets.Composite,
   *      java.lang.String)
   */
  public ProposalTextField createProposalField(Composite parent, String label) {
    return m_fieldToolkit.createProposalField(parent, label);
  }

  /**
   * @param parent
   * @param label
   * @param style
   * @return
   * @see org.eclipse.scout.sdk.ui.fields.FieldToolkit#createProposalField(org.eclipse.swt.widgets.Composite,
   *      java.lang.String, int)
   */
  public ProposalTextField createProposalField(Composite parent, String label, int style) {
    return m_fieldToolkit.createProposalField(parent, label, style);
  }

}
