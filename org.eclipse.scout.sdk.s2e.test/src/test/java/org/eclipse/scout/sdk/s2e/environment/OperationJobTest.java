/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.environment;

import static org.eclipse.scout.sdk.s2e.environment.OperationJob.getJobName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.BiConsumer;

import org.junit.jupiter.api.Test;

/**
 * <h3>{@link OperationJobTest}</h3>
 *
 * @since 6.1.0
 */
public class OperationJobTest {

  @Test
  public void testExecuteOperations() {
    var j = new OperationJob(createEmptyOperationWithName("a"), "name");
    j.scheduleWithFuture().awaitDoneThrowingOnErrorOrCancel();
  }

  @Test
  public void testGetJobName() {
    BiConsumer<?, ?> op = (a, b) -> {
    };
    assertEquals("", getJobName(op));
    assertEquals("", getJobName(new Object()));
    assertEquals("", getJobName(createEmptyOperationWithName(" ")));
    assertEquals("", getJobName(null));
    assertEquals("", getJobName(""));
    assertEquals("text", getJobName(createEmptyOperationWithName("text")));
  }

  protected static BiConsumer<? super EclipseEnvironment, ? super EclipseProgress> createEmptyOperationWithName(String name) {
    @SuppressWarnings("unchecked")
    BiConsumer<? super EclipseEnvironment, ? super EclipseProgress> operation = mock(BiConsumer.class);
    when(operation.toString()).thenReturn(name);
    return operation;
  }
}
