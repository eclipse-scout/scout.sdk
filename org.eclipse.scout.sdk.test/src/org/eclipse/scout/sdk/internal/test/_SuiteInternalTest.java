/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.internal.test;

import org.eclipse.scout.sdk.internal.test.bug.beforeopensource._SuiteBugsBeforeOpensource;
import org.eclipse.scout.sdk.internal.test.operation._SuiteOperation;
import org.eclipse.scout.sdk.internal.test.types._SuiteTypes;
import org.eclipse.scout.sdk.internal.test.util._SuiteUtil;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    _SuiteBugsBeforeOpensource.class,
    _SuiteOperation.class,
    _SuiteTypes.class,
    _SuiteUtil.class})
public class _SuiteInternalTest {

}
