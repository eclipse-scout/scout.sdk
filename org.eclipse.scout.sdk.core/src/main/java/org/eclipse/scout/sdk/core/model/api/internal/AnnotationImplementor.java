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
package org.eclipse.scout.sdk.core.model.api.internal;

import static java.util.Collections.unmodifiableMap;
import static org.eclipse.scout.sdk.core.generator.transformer.IWorkingCopyTransformer.transformAnnotation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.generator.annotation.IAnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.model.api.AbstractManagedAnnotation;
import org.eclipse.scout.sdk.core.model.api.IAnnotatable;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IAnnotationElement;
import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.spi.AnnotationElementSpi;
import org.eclipse.scout.sdk.core.model.spi.AnnotationSpi;
import org.eclipse.scout.sdk.core.util.FinalValue;

@SuppressWarnings("squid:S2160")
public class AnnotationImplementor extends AbstractJavaElementImplementor<AnnotationSpi> implements IAnnotation {
  private FinalValue<Map<String, IAnnotationElement>> m_values;

  public AnnotationImplementor(AnnotationSpi spi) {
    super(spi);
    m_values = new FinalValue<>();
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
  public Optional<IAnnotationElement> element(String name) {
    return Optional.ofNullable(elements().get(name));
  }

  @Override
  public Map<String, IAnnotationElement> elements() {
    return m_values.computeIfAbsentAndGet(() -> {
      Set<Entry<String, AnnotationElementSpi>> entrySet = m_spi.getValues().entrySet();
      Map<String, IAnnotationElement> values = new LinkedHashMap<>(entrySet.size());
      for (Entry<String, AnnotationElementSpi> e : entrySet) {
        values.put(e.getKey(), e.getValue().wrap());
      }
      return unmodifiableMap(values);
    });
  }

  @Override
  public Stream<? extends IJavaElement> children() {
    return elements().values().stream();
  }

  @Override
  public IAnnotatable owner() {
    return m_spi.getOwner().wrap();
  }

  @Override
  public <A extends AbstractManagedAnnotation> A wrap(Class<A> managedAnnotationType) {
    return AbstractManagedAnnotation.wrap(this, managedAnnotationType);
  }

  @Override
  public void internalSetSpi(AnnotationSpi spi) {
    super.internalSetSpi(spi);
    m_values = new FinalValue<>();
  }

  @Override
  public IAnnotationGenerator<?> toWorkingCopy(IWorkingCopyTransformer transformer) {
    return transformAnnotation(this, transformer);
  }

  @Override
  public IAnnotationGenerator<?> toWorkingCopy() {
    return toWorkingCopy(null);
  }
}
