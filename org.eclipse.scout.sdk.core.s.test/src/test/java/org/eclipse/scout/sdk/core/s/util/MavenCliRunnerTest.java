/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link MavenCliRunnerTest}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class MavenCliRunnerTest {
  @Test
  public void testAdd() {
    String[] actuals = MavenCliRunner.getMavenArgs(new String[]{"-a", "-b=testle"}, "-c", null, null);
    Assert.assertArrayEquals(new String[]{"-a", "-b=testle", "-c"}, actuals);
  }

  @Test
  public void testNotAdd() {
    String[] actuals = MavenCliRunner.getMavenArgs(new String[]{"-a", "-b=testle"}, "-a", null, null);
    Assert.assertArrayEquals(new String[]{"-a", "-b=testle"}, actuals);
  }

  @Test
  public void testNotAddArg() {
    String[] actuals = MavenCliRunner.getMavenArgs(new String[]{"-a", "-b=testle"}, "-b=nottest", null, null);
    Assert.assertArrayEquals(new String[]{"-a", "-b=testle"}, actuals);
  }

  @Test
  public void testNotAddArgMulti() {
    String[] actuals = MavenCliRunner.getMavenArgs(new String[]{"-a", "-b=testle"}, "-b=nottest -c=testle", null, null);
    Assert.assertArrayEquals(new String[]{"-a", "-b=testle", "-c=testle"}, actuals);
  }

  @Test
  public void testNotAddArgMultiSameStart() {
    String[] actuals = MavenCliRunner.getMavenArgs(new String[]{"-a", "-b=testle", "-cc=testle"}, "-b=nottest -c=testle", null, null);
    Assert.assertArrayEquals(new String[]{"-a", "-b=testle", "-cc=testle", "-c=testle"}, actuals);
  }

  @Test
  public void testSettings() {
    String[] actuals = MavenCliRunner.getMavenArgs(new String[]{"-a", "-b=testle", "-cc=testle", "-s=blaa.xml"}, "-b=nottest -c=testle -s=blubisettingsxml", "myglobal.xml", "mysettings.xml");
    Assert.assertArrayEquals(new String[]{"-a", "-b=testle", "-cc=testle", "-gs=myglobal.xml", "-s=mysettings.xml", "-c=testle"}, actuals);
  }
}
