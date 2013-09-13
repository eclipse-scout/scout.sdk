/**
 *
 */
package org.eclipse.scout.sdk.internal.test.operation.jdt;

import org.eclipse.scout.sdk.internal.test.operation.jdt.annotation._SuiteAnnotationOperations;
import org.eclipse.scout.sdk.internal.test.operation.jdt.icu._SuiteIcuOperations;
import org.eclipse.scout.sdk.internal.test.operation.jdt.method._SuiteMethodOperations;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * <h3>{@link _SuiteJdtOperations}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 18.02.2011
 */
@RunWith(Suite.class)
@SuiteClasses({
    FieldNewOperationTest.class,
    GenericTypeNewOperationTest.class,
    PackageFragmentNewOperationTest.class,
    SourceFormatOperationTest.class,
    TypeNewOperationTest.class,

    // suites
    _SuiteAnnotationOperations.class,
    _SuiteIcuOperations.class,
    _SuiteMethodOperations.class
})
public class _SuiteJdtOperations {

}
