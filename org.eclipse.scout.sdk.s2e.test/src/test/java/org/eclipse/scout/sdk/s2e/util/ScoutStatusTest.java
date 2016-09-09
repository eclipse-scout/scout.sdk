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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link ScoutStatusTest}</h3>
 *
 * @author Matthias Villiger
 * @since 6.1.0
 */
public class ScoutStatusTest {

  private static final String TEST_MESSAGE = "usrmsg";
  private static final String TEST_EXC_MSG = "excmsg";

  @Test
  public void testMessageAndThrowableMessage() {
    String test1 = CoreUtils.getThrowableAsString(new CoreException(new ScoutStatus(TEST_MESSAGE)));
    Assert.assertTrue(test1.indexOf(TEST_MESSAGE) > 0);

    String test2 = CoreUtils.getThrowableAsString(new CoreException(new ScoutStatus(new Exception(TEST_EXC_MSG))));
    Assert.assertTrue(test2.indexOf(TEST_EXC_MSG) > 0);

    CoreException ex = new CoreException(new ScoutStatus(TEST_MESSAGE, new Exception(TEST_EXC_MSG)));
    String test3 = CoreUtils.getThrowableAsString(ex);
    Assert.assertTrue(test3.indexOf(TEST_MESSAGE) > 0);
    Assert.assertTrue(test3.indexOf(TEST_EXC_MSG) > 0);

    String test4 = CoreUtils.getThrowableAsString(new CoreException(new ScoutStatus(IStatus.ERROR, TEST_MESSAGE, new Exception(TEST_EXC_MSG))));
    Assert.assertTrue(test4.indexOf(TEST_MESSAGE) > 0);
    Assert.assertTrue(test4.indexOf(TEST_EXC_MSG) > 0);
  }
}
