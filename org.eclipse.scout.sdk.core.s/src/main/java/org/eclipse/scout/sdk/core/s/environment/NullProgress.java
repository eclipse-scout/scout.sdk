/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.environment;

/**
 * <h3>{@link NullProgress}</h3>
 *
 * @since 7.0.0
 */
public class NullProgress implements IProgress {

  @Override
  public IProgress init(String name, int totalWork) {
    return this;
  }

  @Override
  public IProgress newChild(int work) {
    return this;
  }

  @Override
  public IProgress setWorkRemaining(int i) {
    return this;
  }

  @Override
  public IProgress worked(int i) {
    return this;
  }
}
