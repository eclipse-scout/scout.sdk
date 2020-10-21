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
package org.eclipse.scout.sdk.core.builder;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static java.util.function.Function.identity;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link SourceCollector}</h3>
 * <p>
 * An {@link ISourceGenerator} {@link Collector}.
 *
 * @since 6.1.0
 */
public class SourceCollector<T extends ISourceGenerator<ISourceBuilder<?>>> implements Collector<T, ISourceBuilder<?>, ISourceBuilder<?>> {

  private final ISourceBuilder<?> m_target;
  private final BiConsumer<ISourceBuilder<?>, T> m_appender;
  private final Set<Characteristics> m_characteristics;

  private Function<ISourceBuilder<?>, ISourceBuilder<?>> m_finisher;
  private BiConsumer<ISourceBuilder<?>, T> m_appenderForNextElement;

  public SourceCollector(ISourceBuilder<?> target, CharSequence prefix, CharSequence delim, CharSequence suffix) {
    m_target = Ensure.notNull(target);

    var hasSuffix = !Strings.isEmpty(suffix);
    var hasDelimiter = !Strings.isEmpty(delim);
    if (hasDelimiter) {
      m_appender = (a, b) -> target.append(delim).append(b);
    }
    else {
      m_appender = (a, b) -> target.append(b);
    }

    m_finisher = identity(); // by default we don't want any finisher. Only after we had the first item we want to print a suffix
    if (hasSuffix) {
      m_characteristics = emptySet();
    }
    else {
      m_characteristics = unmodifiableSet(EnumSet.of(Characteristics.IDENTITY_FINISH));
    }

    m_appenderForNextElement = (builder, b) -> {
      if (!Strings.isEmpty(prefix)) {
        builder.append(prefix);
      }
      if (hasSuffix) {
        // we have the first item: therefore we enable the finisher if necessary
        m_finisher = finishBuilder -> finishBuilder.append(suffix);
      }
      builder.append(b);
      m_appenderForNextElement = m_appender; // move to normal appender
    };
  }

  @Override
  public Supplier<ISourceBuilder<?>> supplier() {
    return () -> m_target;
  }

  @Override
  public BiConsumer<ISourceBuilder<?>, T> accumulator() {
    return (a, b) -> m_appenderForNextElement.accept(a, b);
  }

  @Override
  public BinaryOperator<ISourceBuilder<?>> combiner() {
    return (left, right) -> {
      throw new UnsupportedOperationException("Parallel streams are not supported");
    };
  }

  @Override
  public Function<ISourceBuilder<?>, ISourceBuilder<?>> finisher() {
    return a -> m_finisher.apply(a);
  }

  @Override
  public Set<Characteristics> characteristics() {
    return m_characteristics;
  }
}
