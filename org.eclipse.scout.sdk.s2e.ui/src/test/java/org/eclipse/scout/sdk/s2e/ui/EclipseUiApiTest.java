/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
