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

/**
 * <h3>{@link ISeparatorLabelProvider}</h3>
 *
 * @author Matthias Villiger
 * @since 3.8.0 20.04.2012
 */
public interface ISeparatorLabelProvider {

  String getSeparatorText(ISeparatorProposal element);

  Image getSeparatorImage(ISeparatorProposal element);
}
