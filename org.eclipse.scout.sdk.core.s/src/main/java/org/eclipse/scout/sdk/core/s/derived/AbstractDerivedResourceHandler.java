/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.derived;

import java.util.Collection;
import java.util.function.BiFunction;

import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IFuture;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link AbstractDerivedResourceHandler}</h3>
 *
 * @since 5.2.0
 */
public abstract class AbstractDerivedResourceHandler implements BiFunction<IEnvironment, IProgress, Collection<? extends IFuture<?>>> {

  private final IDerivedResourceInput m_input;

  protected AbstractDerivedResourceHandler(IDerivedResourceInput input) {
    m_input = Ensure.notNull(input);
  }

  @Override
  public final Collection<? extends IFuture<?>> apply(IEnvironment env, IProgress progress) {
    String backup = CoreUtils.getUsername();
    try {
      CoreUtils.setUsernameForThread("Scout robot");
      return execute(env, progress);
    }
    finally {
      CoreUtils.setUsernameForThread(backup);
    }
  }

  protected abstract Collection<? extends IFuture<?>> execute(IEnvironment env, IProgress progress);

  public IDerivedResourceInput getInput() {
    return m_input;
  }

  @Override
  public final int hashCode() {
    return m_input.hashCode();
  }

  @Override
  public final boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    AbstractDerivedResourceHandler other = (AbstractDerivedResourceHandler) obj;
    return m_input.equals(other.m_input);
  }
}
