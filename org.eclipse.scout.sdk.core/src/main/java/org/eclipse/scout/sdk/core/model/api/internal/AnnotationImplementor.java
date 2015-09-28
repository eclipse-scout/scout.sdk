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
import java.util.Map.Entry;
import java.util.Set;

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
  private Map<String, IAnnotationValue> m_values;

  public AnnotationImplementor(AnnotationSpi spi) {
    super(spi);
  }

  @Override
  public String name() {
    return type().name();
  }

  @Override
  public IType type() {
    return m_spi.getType().wrap();
  }

  @Override
  public IAnnotationValue value(String name) {
    return values().get(name);
  }

  @Override
  public Map<String, IAnnotationValue> values() {
    if (m_values == null) {
      Set<Entry<String, AnnotationValueSpi>> entrySet = m_spi.getValues().entrySet();
      Map<String, IAnnotationValue> values = new LinkedHashMap<>(entrySet.size());
      for (Map.Entry<String, AnnotationValueSpi> e : entrySet) {
        AnnotationValueSpi spiValue = e.getValue();
        values.put(e.getKey(), spiValue != null ? spiValue.wrap() : null);
      }
      m_values = values;
    }
    return m_values;
  }

  @Override
  public IAnnotatable owner() {
    return m_spi.getOwner().wrap();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    JavaModelPrinter.print(this, sb);
    return sb.toString();
  }

}
