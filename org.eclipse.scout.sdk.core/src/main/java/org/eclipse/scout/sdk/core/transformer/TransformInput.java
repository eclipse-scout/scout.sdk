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
package org.eclipse.scout.sdk.core.transformer;

import java.util.function.Supplier;

import org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer.ITransformInput;
import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;

/**
 * <h3>{@link TransformInput}</h3>
 *
 * @since 8.0.0
 */
public class TransformInput<MODEL extends IJavaElement, GENERATOR> implements ITransformInput<MODEL, GENERATOR> {

  private final MODEL m_model;
  private final Supplier<GENERATOR> m_defaultGeneratorSupplier;
  private final FinalValue<GENERATOR> m_defaultGenerator;

  public TransformInput(MODEL model, Supplier<GENERATOR> defaultGeneratorSupplier) {
    m_model = Ensure.notNull(model);
    m_defaultGeneratorSupplier = Ensure.notNull(defaultGeneratorSupplier);
    m_defaultGenerator = new FinalValue<>();
  }

  @Override
  public MODEL model() {
    return m_model;
  }

  @Override
  public GENERATOR requestDefaultWorkingCopy() {
    return m_defaultGenerator.computeIfAbsentAndGet(m_defaultGeneratorSupplier);
  }
}
