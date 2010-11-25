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

import org.eclipse.swt.graphics.Image;

public interface IContentProposalEx {

  String getLabel(boolean selected, boolean expertMode);

  Image getImage(boolean selected, boolean expertMode);

  int getCursorPosition(boolean selected, boolean expertMode);
}
