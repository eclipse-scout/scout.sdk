/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.testing;

import java.util.List;

import org.eclipse.scout.sdk.s2e.testing.mock.PlatformMocksInitStatement;
import org.junit.Before;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * <h3>{@link SdkPlatformTestRunner}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class SdkPlatformTestRunner extends BlockJUnit4ClassRunner {
  public SdkPlatformTestRunner(final Class<?> clazz) throws InitializationError {
    super(clazz);
  }

  @Override
  protected Statement withBefores(final FrameworkMethod method, final Object target, final Statement statement) {
    final List<FrameworkMethod> befores = getTestClass().getAnnotatedMethods(Before.class);
    if (befores.isEmpty()) {
      return new PlatformMocksInitStatement(statement, target);
    }

    Statement beforeStatement = new Statement() {
      @Override
      public void evaluate() throws Throwable {
        for (FrameworkMethod each : befores) {
          each.invokeExplosively(target);
        }
      }
    };

    return new PlatformMocksInitStatement(new InterceptedBeforeStatement(statement, beforeStatement), target);
  }

  protected static class InterceptedBeforeStatement extends Statement {

    private final Statement m_statement;
    private final Statement m_interceptedBeforeStatement;

    public InterceptedBeforeStatement(Statement statement, Statement interceptedBeforeStatement) {
      m_statement = statement;
      m_interceptedBeforeStatement = interceptedBeforeStatement;
    }

    @Override
    public void evaluate() throws Throwable {
      m_interceptedBeforeStatement.evaluate();
      m_statement.evaluate();
    }

  }
}
