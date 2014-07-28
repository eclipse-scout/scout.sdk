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

import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.swt.graphics.Image;

/**
 * <h3>{@link MoreElementsProposal}</h3>
 *
 * @author Matthias Villiger
 * @since 3.8.0 16.04.2012
 */
public final class MoreElementsProposal implements ISeparatorProposal {
  private static final String LINE = "---------------";
  private static final String LABEL = LINE + " " + Texts.get("MoreElements") + " " + LINE;

  public static final ISeparatorProposal INSTANCE = new MoreElementsProposal();

  private MoreElementsProposal() {
  }

  @Override
  public String getLabel() {
    return LABEL;
  }

  @Override
  public Image getImage() {
    return ScoutSdkUi.getImage(ScoutSdkUi.Separator);
  }
}
