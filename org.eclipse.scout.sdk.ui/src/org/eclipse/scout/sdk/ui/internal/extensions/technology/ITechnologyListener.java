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
package org.eclipse.scout.sdk.ui.internal.extensions.technology;

import java.util.EventListener;

/**
 * <h3>{@link ITechnologyListener}</h3> ...
 * 
 * @author mvi
 * @since 3.8.0 19.02.2012
 */
public interface ITechnologyListener extends EventListener {
  void handleSelectionChanged(boolean newSelection);
}
