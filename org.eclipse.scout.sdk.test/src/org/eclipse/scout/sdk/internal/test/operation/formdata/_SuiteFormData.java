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
package org.eclipse.scout.sdk.internal.test.operation.formdata;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * <h3>{@link _SuiteFormData}</h3>
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 18.02.2011
 */
@RunWith(Suite.class)
@SuiteClasses({
    AnnotationCopyTest.class,
    ExternalCheckboxFieldTest.class,
    ExternalGroupboxTest.class,
    ExternalTableFieldTest.class,
    FormPropertiesTest.class,
    FormWithGroupboxesTest.class,
    FormWithTemplateTest.class,
    IgnoredFieldsFormTest.class,
    ListBoxFormTest.class,
    ReplaceFormFieldTest.class,
    SimpleFormTest.class,
    TableFieldBeanTest.class,
    TableFieldFormTest.class,
    TableFieldWithIgnoredColumnsTest.class,
    MasterFieldFormDataTest.class
})
public class _SuiteFormData {

}
