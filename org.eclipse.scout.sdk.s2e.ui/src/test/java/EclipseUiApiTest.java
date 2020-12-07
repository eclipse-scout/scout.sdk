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
