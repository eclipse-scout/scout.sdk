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
package org.eclipse.scout.sdk.internal.test.operation;

import org.eclipse.scout.sdk.internal.test.operation.form._SuiteForm;
import org.eclipse.scout.sdk.internal.test.operation.formdata._SuiteFormData;
import org.eclipse.scout.sdk.internal.test.operation.project._SuiteProject;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * <h3>{@link _SuiteOperation}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 22.04.2010
 */
@RunWith(Suite.class)
@SuiteClasses({
    AnnotationTest.class,
    MethodTest.class,
    /*suites*/
    _SuiteFormData.class,
    _SuiteForm.class,
    _SuiteProject.class})
public class _SuiteOperation {

}
