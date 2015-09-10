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
package org.eclipse.scout.sdk.core.model.api.internal;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.scout.sdk.core.model.api.IAnnotatable;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IAnnotationValue;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.spi.AnnotationSpi;
import org.eclipse.scout.sdk.core.model.spi.AnnotationValueSpi;

/**
 *
 */
public class AnnotationImplementor extends AbstractJavaElementImplementor<AnnotationSpi>implements IAnnotation {
  private LinkedHashMap<String, IAnnotationValue> m_values;

  public AnnotationImplementor(AnnotationSpi spi) {
    super(spi);
  }

  @Override
  public String getName() {
    return getType().getName();
  }

  @Override
  public IType getType() {
    return m_spi.getType().wrap();
  }

  @Override
  public IAnnotationValue getValue(String name) {
    return getValues().get(name);
  }

  @Override
  public Map<String, IAnnotationValue> getValues() {
    if (m_values == null) {
      m_values = new LinkedHashMap<>();
      for (Map.Entry<String, AnnotationValueSpi> e : m_spi.getValues().entrySet()) {
        AnnotationValueSpi spiValue = e.getValue();
        m_values.put(e.getKey(), spiValue != null ? spiValue.wrap() : null);
      }
    }
    return m_values;
  }

  @Override
  public IAnnotatable getOwner() {
    return m_spi.getOwner().wrap();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    JavaModelPrinter.print(this, sb);
    return sb.toString();
  }

}
