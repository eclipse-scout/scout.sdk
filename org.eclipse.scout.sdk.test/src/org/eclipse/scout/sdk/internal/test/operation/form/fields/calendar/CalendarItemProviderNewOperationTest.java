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
package org.eclipse.scout.sdk.internal.test.operation.form.fields.calendar;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.test.AbstractSdkTestWithSampleProject;
import org.eclipse.scout.sdk.operation.form.field.calendar.CalendarItemProviderNewOperation;
import org.eclipse.scout.sdk.operation.jdt.JavaElementDeleteOperation;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeComparators;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link CalendarItemProviderNewOperationTest}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 14.03.2013
 */
public class CalendarItemProviderNewOperationTest extends AbstractSdkTestWithSampleProject {

  @Test
  public void testNewItemProvider() throws Exception {
    Assert.assertTrue(TypeUtility.exists(getSharedJavaProject()));
    IType calendar = TypeUtility.getType("sample.client.person.PersonForm.MainBox.CalendarField.Calendar");
    CalendarItemProviderNewOperation calendarItemProviderOp = new CalendarItemProviderNewOperation("SimpleItemProvider", calendar);
    calendarItemProviderOp.setSuperTypeSignature(Signature.createTypeSignature("org.eclipse.scout.rt.client.ui.basic.calendar.provider.AbstractCalendarItemProvider", true));
    calendarItemProviderOp.setSibling(TypeUtility.getType("sample.client.person.PersonForm.MainBox.CalendarField.Calendar.FirstItemProvider"));
    executeBuildAssertNoCompileErrors(calendarItemProviderOp);
    IType itemProvider = calendarItemProviderOp.getCreatedType();
    System.out.println(itemProvider.getCompilationUnit().getSource());
    SdkAssert.assertExist(itemProvider);
    SdkAssert.assertPublic(itemProvider).assertNoMoreFlags();

    IType[] itemProviders = TypeUtility.getInnerTypesOrdered(calendar, TypeUtility.getType(RuntimeClasses.ICalendarItemProvider), ScoutTypeComparators.getOrderAnnotationComparator());
    SdkAssert.assertEquals(2, itemProviders.length);
    SdkAssert.assertEquals(calendarItemProviderOp.getElementName(), itemProviders[0].getElementName());
    SdkAssert.assertOrderAnnotation(itemProvider, Double.valueOf(10));

    // clean up
    JavaElementDeleteOperation delOp = new JavaElementDeleteOperation();
    delOp.addMember(itemProvider);
    executeBuildAssertNoCompileErrors(delOp);
    itemProviders = TypeUtility.getInnerTypesOrdered(calendar, TypeUtility.getType(RuntimeClasses.ICalendarItemProvider), ScoutTypeComparators.getOrderAnnotationComparator());
    SdkAssert.assertEquals(1, itemProviders.length);
  }

  @Test
  public void testNewHolidayItemProvider() throws Exception {
    Assert.assertTrue(TypeUtility.exists(getSharedJavaProject()));
    IType calendar = TypeUtility.getType("sample.client.person.PersonForm.MainBox.CalendarField.Calendar");
    CalendarItemProviderNewOperation calendarItemProviderOp = new CalendarItemProviderNewOperation("HolidayItemProvider", calendar);
    calendarItemProviderOp.setSuperTypeSignature(Signature.createTypeSignature("org.eclipse.scout.rt.client.ui.basic.calendar.provider.AbstractHolidayItemProvider", true));
    executeBuildAssertNoCompileErrors(calendarItemProviderOp);
    IType itemProvider = calendarItemProviderOp.getCreatedType();
    System.out.println(itemProvider.getCompilationUnit().getSource());
    SdkAssert.assertExist(itemProvider);
    SdkAssert.assertPublic(itemProvider).assertNoMoreFlags();

    IType[] itemProviders = TypeUtility.getInnerTypesOrdered(calendar, TypeUtility.getType(RuntimeClasses.ICalendarItemProvider), ScoutTypeComparators.getOrderAnnotationComparator());
    SdkAssert.assertEquals(2, itemProviders.length);
    SdkAssert.assertEquals(calendarItemProviderOp.getElementName(), itemProviders[1].getElementName());
    SdkAssert.assertOrderAnnotation(itemProvider, Double.valueOf(20));

    // clean up
    JavaElementDeleteOperation delOp = new JavaElementDeleteOperation();
    delOp.addMember(itemProvider);
    executeBuildAssertNoCompileErrors(delOp);
    itemProviders = TypeUtility.getInnerTypesOrdered(calendar, TypeUtility.getType(RuntimeClasses.ICalendarItemProvider), ScoutTypeComparators.getOrderAnnotationComparator());
    SdkAssert.assertEquals(1, itemProviders.length);
  }
}
