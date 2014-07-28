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
package org.eclipse.scout.sdk.internal.test.operation.form.fields.table;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * <h3>{@link _SuiteTable}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.8.0 13.02.2013
 */
@RunWith(Suite.class)
@SuiteClasses({
  TableColumnNewOperationTest.class,
  TableFieldNewOperationTest.class
})
public class _SuiteTable {

}
