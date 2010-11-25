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
package org.eclipse.scout.nls.sdk.internal.ui;

import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;

public class NlsUi {

  public static Control decorate(TextComposition textComp, boolean onFocus) {
    Control control = textComp.getTextControl();
    // Decorate the text widget with the light-bulb image denoting content
    // assist
    int bits = SWT.TOP | SWT.LEFT;
    ControlDecoration controlDecoration = new ControlDecoration(control, bits);
    // Configure text widget decoration
    // No margin
    controlDecoration.setMarginWidth(0);
    // Custom hover tip text
    controlDecoration.setDescriptionText(
        "Content Assist Availabele (Ctrl + Space)");
    // Custom hover properties
    controlDecoration.setShowHover(true);
    controlDecoration.setShowOnlyOnFocus(onFocus);
    // Hover image to use
    FieldDecoration contentProposalImage =
        FieldDecorationRegistry.getDefault().getFieldDecoration(
            FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
    controlDecoration.setImage(contentProposalImage.getImage());
    return control;
  }
}
