package org.eclipse.scout.sdk.internal.test.operation.form.fields;

import org.eclipse.scout.sdk.internal.test.operation.form.fields.calendar._SuiteCalendarField;
import org.eclipse.scout.sdk.internal.test.operation.form.fields.composer._SuiteComposerField;
import org.eclipse.scout.sdk.internal.test.operation.form.fields.smartfield._SuiteSmartField;
import org.eclipse.scout.sdk.internal.test.operation.form.fields.table._SuiteTable;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    BigdecimalFieldNewOperationTest.class,
    BooleanFieldNewOperationTest.class,
    ButtonFieldNewOperationTest.class,
    CheckboxFieldNewOperationTest.class,
    ComposerFieldNewOperationTest.class,
    DateFieldNewOperationTest.class,
    DoubleFieldNewOperationTest.class,
    FileChooserFieldNewOperationTest.class,
    GroupBoxNewOperationTest.class,
    HtmlFieldNewOperationTest.class,
    ListBoxFieldNewOperationTest.class,
    PlannerFieldNewOperationTest.class,
    SequenceBoxNewOperationTest.class,
    StringFieldNewOperationTest.class,
    TabBoxNewOperationTest.class,
    TreeBoxNewOperationTest.class,
    TreeFieldNewOperationTest.class,

    // suites
    _SuiteCalendarField.class,
    _SuiteComposerField.class,
    _SuiteSmartField.class,
    _SuiteTable.class
})
public class _SuiteFormFields {

}
