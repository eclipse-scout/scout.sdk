/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.builder;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.PropertySupport;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link BuilderContext}</h3>
 *
 * @since 6.1.0
 */
public class BuilderContext implements IBuilderContext {

  private final String m_nl;
  private final FinalValue<PropertySupport> m_properties; // lazy initialized

  public BuilderContext() {
    this(null);
  }

  public BuilderContext(String nl) {
    this(nl, null);
  }

  public BuilderContext(String nl, PropertySupport properties) {
    //noinspection HardcodedLineSeparator
    m_nl = Strings.notEmpty(nl).orElse("\n"); // do not use System.lineSeparator() here so that the created source is not platform dependent.
    m_properties = new FinalValue<>();
    Optional.ofNullable(properties).ifPresent(m_properties::set);
  }

  @Override
  public String lineDelimiter() {
    return m_nl;
  }

  @Override
  public PropertySupport properties() {
    return m_properties.computeIfAbsentAndGet(PropertySupport::new);
  }

  @Override
  public int hashCode() {
    var prime = 31;
    var h = prime + m_nl.hashCode();
    return prime * h + m_properties.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    var other = (BuilderContext) obj;
    return Objects.equals(m_nl, other.m_nl)
        && Objects.equals(m_properties, other.m_properties);
  }
}
