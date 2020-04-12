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
package org.eclipse.scout.sdk.s2e.ui.wizard;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment;
import org.eclipse.scout.sdk.s2e.environment.EclipseProgress;
import org.eclipse.swt.widgets.Display;

/**
 * <h3>{@link WizardFinishTask}</h3>
 *
 * @since 7.1.0
 */
public class WizardFinishTask<OP extends BiConsumer<? super EclipseEnvironment, ? super EclipseProgress>> implements BiConsumer<EclipseEnvironment, EclipseProgress> {

  private final Display m_display;
  private FinalValue<OP> m_operation;
  private Supplier<OP> m_operationSupplier;
  private BiConsumer<PageToOperationMappingInput, ? extends OP> m_mapper;
  private BiConsumer<OP, Display> m_uiAction;

  public WizardFinishTask(Display display) {
    m_display = Ensure.notNull(display);
    m_operation = new FinalValue<>();
  }

  @Override
  public void accept(EclipseEnvironment environment, EclipseProgress progress) {
    int workForMapping = 1;
    int workForOperation = 1000;
    progress.init("Values from UI to operation", workForMapping + workForOperation);
    OP operation = Ensure.notNull(operation());
    mapper().accept(new PageToOperationMappingInput(environment, progress.newChild(workForMapping)), operation);
    operation.accept(environment, progress.newChild(workForOperation));
    uiAction().ifPresent(action -> action.accept(operation, m_display));
  }

  public WizardFinishTask<OP> withOperation(Supplier<OP> supplier) {
    m_operationSupplier = Ensure.notNull(supplier);
    m_operation = new FinalValue<>();
    return this;
  }

  protected OP operation() {
    return m_operation.computeIfAbsentAndGet(operationSupplier());
  }

  public Supplier<OP> operationSupplier() {
    return m_operationSupplier;
  }

  @SuppressWarnings("squid:S00108")
  public WizardFinishTask<OP> withMapper(BiConsumer<PageToOperationMappingInput, ? extends OP> mapper) {
    m_mapper = Optional.ofNullable(mapper).orElse((input, op) -> {
    });
    return this;
  }

  @SuppressWarnings("unchecked")
  public <T extends OP> BiConsumer<PageToOperationMappingInput, T> mapper() {
    return (BiConsumer<PageToOperationMappingInput, T>) m_mapper;
  }

  public WizardFinishTask<OP> withUiAction(BiConsumer<OP, Display> action) {
    m_uiAction = action;
    return this;
  }

  public Optional<BiConsumer<OP, Display>> uiAction() {
    return Optional.ofNullable(m_uiAction);
  }

  @Override
  public String toString() {
    return operation().toString();
  }

  public static class PageToOperationMappingInput {
    private final EclipseEnvironment m_env;
    private final EclipseProgress m_progress;

    public PageToOperationMappingInput(EclipseEnvironment env, EclipseProgress progress) {
      m_env = Ensure.notNull(env);
      m_progress = Ensure.notNull(progress);
    }

    public EclipseEnvironment environment() {
      return m_env;
    }

    public EclipseProgress progress() {
      return m_progress;
    }
  }
}
