/**
 *
 */
package org.eclipse.scout.sdk.test.operation.formdata;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * <h3>{@link AllFormDataUpdateTestSuite}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 18.02.2011
 */
@RunWith(Suite.class)
//@SuiteClasses({FormWithTemplateTest.class, TemplateFormDataTest.class})
@SuiteClasses({FormPropertiesTest.class,
    FormWithGroupboxesTest.class,
    FormWithTemplateTest.class,
    IgnoredFieldsFormTest.class,
    ListBoxFormTest.class,
    SimpleFormTest.class,
    TableFieldFormTest.class,
    TemplateFormDataTest.class})
public class AllFormDataUpdateTestSuite {

}
