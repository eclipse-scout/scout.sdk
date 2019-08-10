/*
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.derived;

import java.util.Optional;

import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.api.IClasspathEntry;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.MissingTypeException;
import org.eclipse.scout.sdk.core.s.derived.IDerivedResourceInput;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;

/**
 * <h3>{@link DerivedResourceInputWithJdt}</h3>
 *
 * @since 7.0.0
 */
public class DerivedResourceInputWithJdt implements IDerivedResourceInput {

  private final org.eclipse.jdt.core.IType m_jdtType;

  public DerivedResourceInputWithJdt(org.eclipse.jdt.core.IType jdtType) {
    m_jdtType = Ensure.notNull(jdtType);
  }

  @Override
  public Optional<IType> getSourceType(IEnvironment env) {
    return toScoutType(m_jdtType, env);
  }

  protected static Optional<IType> toScoutType(org.eclipse.jdt.core.IType jdtType, IEnvironment env) {
    try {
      return Optional.ofNullable(EclipseEnvironment.narrow(env).toScoutType(jdtType));
    }
    catch (MissingTypeException e) {
      SdkLog.info("Unable to update DTO for '{}' because there are compile errors in the compilation unit.", jdtType.getFullyQualifiedName(), e);
      return Optional.empty();
    }
  }

  @Override
  public Optional<IClasspathEntry> getSourceFolderOf(IType type, IEnvironment env) {
    return Optional.ofNullable(type)
        .map(EclipseEnvironment::toJdtType)
        .map(JdtUtils::getSourceFolder)
        .map(root -> EclipseEnvironment.narrow(env).toScoutSourceFolder(root));
  }

  @Override
  public String toString() {
    return m_jdtType.getFullyQualifiedName();
  }

  @Override
  public final int hashCode() {
    return m_jdtType.hashCode();
  }

  @Override
  public final boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj == null || obj.getClass() != getClass()) {
      return false;
    }

    DerivedResourceInputWithJdt other = (DerivedResourceInputWithJdt) obj;
    return m_jdtType.equals(other.m_jdtType);
  }
}
