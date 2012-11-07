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
/**
 *
 */
package org.eclipse.scout.sdk.internal.test.types;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * <h3>{@link _SuiteTypes}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 22.04.2010
 */
@RunWith(Suite.class)
@SuiteClasses({
    RecreateTypeTest.class,
    TypeCacheTest.class,
    TypeHierarchyTest1.class,
    TypeHierarchyTest2.class,
    TypeUtilityTest.class})
public class _SuiteTypes {

}
