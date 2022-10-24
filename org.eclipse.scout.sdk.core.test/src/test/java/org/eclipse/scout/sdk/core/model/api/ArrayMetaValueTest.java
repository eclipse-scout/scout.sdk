/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
