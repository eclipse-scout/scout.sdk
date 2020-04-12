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
package org.eclipse.scout.sdk.core.s.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link ScoutTierTest}</h3>
 *
 * @since 6.1.0
 */
public class ScoutTierTest {

  @Test
  public void testFilter() {
    assertTrue(ScoutTier.Shared.isIncludedIn(ScoutTier.Client));
    assertTrue(ScoutTier.Shared.isIncludedIn(ScoutTier.Server));
    assertTrue(ScoutTier.Shared.isIncludedIn(ScoutTier.Shared));
    assertTrue(ScoutTier.Shared.isIncludedIn(ScoutTier.HtmlUi));

    assertTrue(ScoutTier.Client.isIncludedIn(ScoutTier.Client));
    assertFalse(ScoutTier.Client.isIncludedIn(ScoutTier.Server));
    assertFalse(ScoutTier.Client.isIncludedIn(ScoutTier.Shared));
    assertTrue(ScoutTier.Client.isIncludedIn(ScoutTier.HtmlUi));

    assertFalse(ScoutTier.Server.isIncludedIn(ScoutTier.Client));
    assertTrue(ScoutTier.Server.isIncludedIn(ScoutTier.Server));
    assertFalse(ScoutTier.Server.isIncludedIn(ScoutTier.Shared));
    assertFalse(ScoutTier.Server.isIncludedIn(ScoutTier.HtmlUi));

    assertFalse(ScoutTier.HtmlUi.isIncludedIn(ScoutTier.Client));
    assertFalse(ScoutTier.HtmlUi.isIncludedIn(ScoutTier.Server));
    assertFalse(ScoutTier.HtmlUi.isIncludedIn(ScoutTier.Shared));
    assertTrue(ScoutTier.HtmlUi.isIncludedIn(ScoutTier.HtmlUi));

    assertFalse(ScoutTier.HtmlUi.isIncludedIn(null));
    assertFalse(ScoutTier.HtmlUi.isIncludedIn(null));
    assertFalse(ScoutTier.HtmlUi.isIncludedIn(null));
    assertFalse(ScoutTier.HtmlUi.isIncludedIn(null));
  }

  @Test
  public void testConvert() {
    assertNull(ScoutTier.Shared.convert(ScoutTier.Client, null));
    assertEquals("", ScoutTier.Shared.convert(ScoutTier.Client, ""));
    assertEquals("  ", ScoutTier.Shared.convert(ScoutTier.Client, "  "));
    assertEquals("my.test.client.whatever", ScoutTier.Shared.convert(ScoutTier.Client, "my.test.shared.whatever"));
    assertEquals("my.test.shared.whatever", ScoutTier.Shared.convert(ScoutTier.Shared, "my.test.shared.whatever"));
    assertEquals("my.test.ui.html.whatever", ScoutTier.Shared.convert(ScoutTier.HtmlUi, "my.test.shared.whatever"));
    assertEquals("my.test.server.whatever", ScoutTier.HtmlUi.convert(ScoutTier.Server, "my.test.ui.html.whatever"));
  }

  @Test
  public void testFilterJavaElement() {
    IJavaEnvironment p = mock(IJavaEnvironment.class);
    IType mock = mock(IType.class);
    when(mock.javaEnvironment()).thenReturn(p);

    assertFalse(ScoutTier.Shared.test(null));
    assertFalse(ScoutTier.Client.test(null));
    assertFalse(ScoutTier.Server.test(null));
    assertFalse(ScoutTier.HtmlUi.test(null));

    assertFalse(ScoutTier.Shared.test(mock));
    assertFalse(ScoutTier.Client.test(mock));
    assertFalse(ScoutTier.Server.test(mock));
    assertFalse(ScoutTier.HtmlUi.test(mock));

    assertFalse(ScoutTier.Shared.test(mock));
    assertFalse(ScoutTier.Client.test(mock));
    assertFalse(ScoutTier.Server.test(mock));
    assertFalse(ScoutTier.HtmlUi.test(mock));

    assertFalse(ScoutTier.Shared.test(mock));
    assertFalse(ScoutTier.Client.test(mock));
    assertFalse(ScoutTier.Server.test(mock));
    assertFalse(ScoutTier.HtmlUi.test(mock));

    when(p.exists(IScoutRuntimeTypes.ISession)).thenReturn(Boolean.TRUE);
    assertTrue(ScoutTier.Shared.test(mock));
    assertFalse(ScoutTier.Client.test(mock));
    assertFalse(ScoutTier.Server.test(mock));
    assertFalse(ScoutTier.HtmlUi.test(mock));

    when(p.exists(IScoutRuntimeTypes.IServerSession)).thenReturn(Boolean.TRUE);
    assertTrue(ScoutTier.Shared.test(mock));
    assertFalse(ScoutTier.Client.test(mock));
    assertTrue(ScoutTier.Server.test(mock));
    assertFalse(ScoutTier.HtmlUi.test(mock));

    when(p.exists(IScoutRuntimeTypes.IClientSession)).thenReturn(Boolean.TRUE);
    assertTrue(ScoutTier.Shared.test(mock));
    assertTrue(ScoutTier.Client.test(mock));
    assertFalse(ScoutTier.Server.test(mock));
    assertFalse(ScoutTier.HtmlUi.test(mock));

    when(p.exists(IScoutRuntimeTypes.UiServlet)).thenReturn(Boolean.TRUE);
    assertTrue(ScoutTier.Shared.test(mock));
    assertTrue(ScoutTier.Client.test(mock));
    assertFalse(ScoutTier.Server.test(mock));
    assertTrue(ScoutTier.HtmlUi.test(mock));
  }
}
