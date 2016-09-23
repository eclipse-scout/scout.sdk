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
package org.eclipse.scout.sdk.s2e.util;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * <h3>{@link ScoutTierTest}</h3>
 *
 * @author Matthias Villiger
 * @since 6.1.0
 */
public class ScoutTierTest {

  @Test
  public void testFilter() {
    Assert.assertTrue(ScoutTier.Shared.isIncludedIn(ScoutTier.Client));
    Assert.assertTrue(ScoutTier.Shared.isIncludedIn(ScoutTier.Server));
    Assert.assertTrue(ScoutTier.Shared.isIncludedIn(ScoutTier.Shared));
    Assert.assertTrue(ScoutTier.Shared.isIncludedIn(ScoutTier.HtmlUi));

    Assert.assertTrue(ScoutTier.Client.isIncludedIn(ScoutTier.Client));
    Assert.assertFalse(ScoutTier.Client.isIncludedIn(ScoutTier.Server));
    Assert.assertFalse(ScoutTier.Client.isIncludedIn(ScoutTier.Shared));
    Assert.assertTrue(ScoutTier.Client.isIncludedIn(ScoutTier.HtmlUi));

    Assert.assertFalse(ScoutTier.Server.isIncludedIn(ScoutTier.Client));
    Assert.assertTrue(ScoutTier.Server.isIncludedIn(ScoutTier.Server));
    Assert.assertFalse(ScoutTier.Server.isIncludedIn(ScoutTier.Shared));
    Assert.assertFalse(ScoutTier.Server.isIncludedIn(ScoutTier.HtmlUi));

    Assert.assertFalse(ScoutTier.HtmlUi.isIncludedIn(ScoutTier.Client));
    Assert.assertFalse(ScoutTier.HtmlUi.isIncludedIn(ScoutTier.Server));
    Assert.assertFalse(ScoutTier.HtmlUi.isIncludedIn(ScoutTier.Shared));
    Assert.assertTrue(ScoutTier.HtmlUi.isIncludedIn(ScoutTier.HtmlUi));

    Assert.assertFalse(ScoutTier.HtmlUi.isIncludedIn(null));
    Assert.assertFalse(ScoutTier.HtmlUi.isIncludedIn(null));
    Assert.assertFalse(ScoutTier.HtmlUi.isIncludedIn(null));
    Assert.assertFalse(ScoutTier.HtmlUi.isIncludedIn(null));
  }

  @Test
  public void testConvert() {
    Assert.assertNull(ScoutTier.Shared.convert(ScoutTier.Client, (String) null));
    Assert.assertEquals("", ScoutTier.Shared.convert(ScoutTier.Client, ""));
    Assert.assertEquals("  ", ScoutTier.Shared.convert(ScoutTier.Client, "  "));
    Assert.assertEquals("my.test.client.whatever", ScoutTier.Shared.convert(ScoutTier.Client, "my.test.shared.whatever"));
    Assert.assertEquals("my.test.shared.whatever", ScoutTier.Shared.convert(ScoutTier.Shared, "my.test.shared.whatever"));
    Assert.assertEquals("my.test.ui.html.whatever", ScoutTier.Shared.convert(ScoutTier.HtmlUi, "my.test.shared.whatever"));
    Assert.assertEquals("my.test.server.whatever", ScoutTier.HtmlUi.convert(ScoutTier.Server, "my.test.ui.html.whatever"));
  }

  @Test
  public void testFilterJavaElement() throws JavaModelException {
    IJavaProject p = Mockito.mock(IJavaProject.class);

    IType mock = Mockito.mock(IType.class);
    Mockito.when(mock.getJavaProject()).thenReturn(p);

    Assert.assertFalse(ScoutTier.Shared.test(null));
    Assert.assertFalse(ScoutTier.Client.test(null));
    Assert.assertFalse(ScoutTier.Server.test(null));
    Assert.assertFalse(ScoutTier.HtmlUi.test(null));

    Mockito.when(mock.exists()).thenReturn(Boolean.TRUE.booleanValue());
    Assert.assertFalse(ScoutTier.Shared.test(mock));
    Assert.assertFalse(ScoutTier.Client.test(mock));
    Assert.assertFalse(ScoutTier.Server.test(mock));
    Assert.assertFalse(ScoutTier.HtmlUi.test(mock));

    Mockito.when(p.exists()).thenReturn(Boolean.TRUE.booleanValue());
    Assert.assertFalse(ScoutTier.Shared.test(mock));
    Assert.assertFalse(ScoutTier.Client.test(mock));
    Assert.assertFalse(ScoutTier.Server.test(mock));
    Assert.assertFalse(ScoutTier.HtmlUi.test(mock));

    Assert.assertFalse(ScoutTier.Shared.test(mock));
    Assert.assertFalse(ScoutTier.Client.test(mock));
    Assert.assertFalse(ScoutTier.Server.test(mock));
    Assert.assertFalse(ScoutTier.HtmlUi.test(mock));

    Mockito.when(p.findType(IScoutRuntimeTypes.ISession)).thenReturn(mock);
    Assert.assertTrue(ScoutTier.Shared.test(mock));
    Assert.assertFalse(ScoutTier.Client.test(mock));
    Assert.assertFalse(ScoutTier.Server.test(mock));
    Assert.assertFalse(ScoutTier.HtmlUi.test(mock));

    Mockito.when(p.findType(IScoutRuntimeTypes.IServerSession)).thenReturn(mock);
    Assert.assertTrue(ScoutTier.Shared.test(mock));
    Assert.assertFalse(ScoutTier.Client.test(mock));
    Assert.assertTrue(ScoutTier.Server.test(mock));
    Assert.assertFalse(ScoutTier.HtmlUi.test(mock));

    Mockito.when(p.findType(IScoutRuntimeTypes.IClientSession)).thenReturn(mock);
    Assert.assertTrue(ScoutTier.Shared.test(mock));
    Assert.assertTrue(ScoutTier.Client.test(mock));
    Assert.assertFalse(ScoutTier.Server.test(mock));
    Assert.assertFalse(ScoutTier.HtmlUi.test(mock));

    Mockito.when(p.findType(IScoutRuntimeTypes.UiServlet)).thenReturn(mock);
    Assert.assertTrue(ScoutTier.Shared.test(mock));
    Assert.assertTrue(ScoutTier.Client.test(mock));
    Assert.assertFalse(ScoutTier.Server.test(mock));
    Assert.assertTrue(ScoutTier.HtmlUi.test(mock));
  }
}
