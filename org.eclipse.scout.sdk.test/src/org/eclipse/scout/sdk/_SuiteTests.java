/**
 *
 */
package org.eclipse.scout.sdk;

import org.eclipse.scout.sdk.internal.test.operation.formdata.ExternalCheckboxFieldTest;
import org.eclipse.scout.sdk.internal.test.types.RecreateTypeTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * <h3>{@link _SuiteTests}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 14.07.2011
 */
@RunWith(Suite.class)
@SuiteClasses({ExternalCheckboxFieldTest.class,
    RecreateTypeTest.class})
public class _SuiteTests {

}
