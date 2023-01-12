/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.model.api;

import static org.eclipse.scout.sdk.core.model.api.ArrayMetaValue.withoutNullElements;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

public class ArrayMetaValueTest {

  @Test
  public void testWithoutNullElements() {
    var mock = mock(IMetaValue.class);
    var noNullsSmall = new IMetaValue[]{mock};
    var noNulls = new IMetaValue[]{mock, mock, mock, mock};
    IMetaValue[] withNulls = {null, null, mock, mock, mock, null, mock, null, mock, mock, null, null, null};

    assertEquals(0, withoutNullElements(null).length);
    assertEquals(6, withoutNullElements(withNulls).length);
    assertSame(noNullsSmall, withoutNullElements(noNullsSmall));
    assertSame(noNulls, withoutNullElements(noNulls));
  }
}
