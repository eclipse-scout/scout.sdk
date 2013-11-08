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
package org.eclipse.scout.sdk.rap.var;

import org.eclipse.core.resources.IFile;

/**
 * <h3>{@link RapTargetVariableListenerAdapter}</h3> ...
 * 
 * @author Matthias Villiger
 * @since 3.9.0 14.01.2013
 */
public class RapTargetVariableListenerAdapter implements IRapTargetVariableListener {
  @Override
  public void valueChanged(String oldVal, String newVal) {
  }

  @Override
  public void emptyVariableInUse(IFile targetFile) {
  }
}
