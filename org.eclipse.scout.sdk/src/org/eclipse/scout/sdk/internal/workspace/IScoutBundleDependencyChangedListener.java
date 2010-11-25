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
package org.eclipse.scout.sdk.internal.workspace;

import java.util.EventListener;

import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 *
 */
public interface IScoutBundleDependencyChangedListener extends EventListener {

  void dependenciesChanged(IScoutBundle scoutBundle);
}
