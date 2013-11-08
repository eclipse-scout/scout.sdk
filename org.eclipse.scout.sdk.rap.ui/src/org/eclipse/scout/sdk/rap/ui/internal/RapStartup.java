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
package org.eclipse.scout.sdk.rap.ui.internal;

import org.eclipse.scout.sdk.rap.var.RapTargetVariable;
import org.eclipse.ui.IStartup;

/**
 * <h3>{@link RapStartup}</h3> ...
 * 
 * @author Matthias Villiger
 * @since 3.9.0 14.01.2013
 */
public class RapStartup implements IStartup {
  @Override
  public void earlyStartup() {

    // ensure the plug-in is started and the variable is propagated
    RapTargetVariable.get().getValue();
  }
}
