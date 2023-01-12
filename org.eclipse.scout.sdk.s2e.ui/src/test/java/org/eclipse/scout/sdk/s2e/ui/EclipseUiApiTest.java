/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.jface.text.link.LinkedModeUI;
import org.junit.jupiter.api.Test;

public class EclipseUiApiTest {

  @Test
  public void testLinkedModeUIApi() throws NoSuchMethodException {
    var m = LinkedModeUI.class.getDeclaredMethod("triggerContentAssist");
    assertNotNull(m);
  }
}
