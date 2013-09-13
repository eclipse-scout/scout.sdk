package org.eclipse.scout.sdk.internal.test.operation.form;

import org.eclipse.scout.sdk.internal.test.operation.form.fields._SuiteFormFields;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    FormHandlerNewOperationTest.class,
    FormNewOperationTest.class,
    FormStackNewOperationTest.class,
    // Suites
    _SuiteFormFields.class
})
public class _SuiteForm {

}
