/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.job;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IResource;
import org.eclipse.scout.sdk.s2e.operation.IOperation;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

/**
 * <h3>{@link ResourceBlockingOperationJobTest}</h3>
 *
 * @since 6.1.0
 */
public class ResourceBlockingOperationJobTest {

  @Test
  public void testGetJobName() {
    String MISSING_OP_NAME = "Missing operation name.";
    IResource resource = Mockito.mock(IResource.class);
    Mockito.when(resource.contains(Matchers.same(resource))).thenReturn(Boolean.TRUE);
    Mockito.when(resource.isConflicting(Matchers.same(resource))).thenReturn(Boolean.TRUE);

    Assert.assertEquals(MISSING_OP_NAME, new ResourceBlockingOperationJob(getOperations(""), resource).getName());
    Assert.assertEquals("", new ResourceBlockingOperationJob(getOperations(), resource).getName());
    Assert.assertEquals("", new ResourceBlockingOperationJob(getOperations((String) null), resource).getName());
    Assert.assertEquals("", new ResourceBlockingOperationJob(getOperations((String[]) null), resource).getName());
    Assert.assertEquals("a, b", new ResourceBlockingOperationJob(getOperations("a", "b"), resource).getName());
    Assert.assertEquals("a, b, c", new ResourceBlockingOperationJob(getOperations("a", "b", null, "c"), resource).getName());
    Assert.assertEquals("a, b, " + MISSING_OP_NAME + ", c", new ResourceBlockingOperationJob(getOperations("a", "b", null, "", "c"), resource).getName());
    Assert.assertEquals("a, b, c", new ResourceBlockingOperationJob(getOperations(null, "a", "b", null, "c", null), resource).getName());

    Assert.assertEquals("a", new ResourceBlockingOperationJob(getOperations("a").iterator().next(), resource).getName());
    Assert.assertEquals("", new ResourceBlockingOperationJob((IOperation) null, resource).getName());
  }

  protected Collection<IOperation> getOperations(String... names) {
    if (names == null) {
      return null;
    }

    Collection<IOperation> result = new ArrayList<>(names.length);
    for (String name : names) {
      if (name == null) {
        result.add(null);
      }
      else {
        IOperation operation = Mockito.mock(IOperation.class);
        Mockito.when(operation.getOperationName()).thenReturn(name);
        result.add(operation);
      }
    }
    return result;
  }
}
