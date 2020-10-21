/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.derived;

import static java.util.Collections.emptyList;

import java.util.Collection;

import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.MissingTypeException;
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
public abstract class AbstractDerivedResourceHandler implements IDerivedResourceHandler {

  private final IDerivedResourceInput m_input;

  protected AbstractDerivedResourceHandler(IDerivedResourceInput input) {
    m_input = Ensure.notNull(input);
  }

  @Override
  public final Collection<? extends IFuture<?>> apply(IEnvironment env, IProgress progress) {
    var backup = CoreUtils.getUsername();
    try {
      CoreUtils.setUsernameForThread("Scout robot");
      return execute(env, progress);
    }
    catch (MissingTypeException mte) {
      var inputTypeName = getInput().getSourceType(env).map(IType::name).orElse(null);
      SdkLog.info("Skip type '{}' because it contains compile errors", inputTypeName, mte);
      return emptyList();
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
    var other = (AbstractDerivedResourceHandler) obj;
    return m_input.equals(other.m_input);
  }
}
