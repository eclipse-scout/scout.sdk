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
package org.eclipse.scout.sdk.ui.fields.javacode;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.swt.graphics.Image;

public class JavaCodeFieldLabelProvider extends LabelProvider {

  @Override
  public Image getImage(Object element) {
    return ScoutSdkUi.getImage(ScoutSdkUi.Class);
  }

  @Override
  public String getText(Object element) {
    return ((IContentProposal) element).getLabel();
  }
}
