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
package org.eclipse.scout.sdk.ui.fields.bundletree;

import java.util.EventListener;

/**
 * <h3>ICheckSelectionListener</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 10.02.2010
 */
public interface ICheckStateListener extends EventListener {

  void fireNodeCheckStateChanged(ITreeNode node, boolean checkState);
}
