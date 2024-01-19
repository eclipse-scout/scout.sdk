/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package formdata.shared.services.process;

import java.math.BigDecimal;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.composer.AbstractComposerData;
import org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData;

import formdata.shared.IFormDataInterface02;
import formdata.shared.IFormDataInterface03;
import formdata.shared.TestRunnable;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated by the Scout SDK. No manual modifications recommended.
 */
@ClassId("c7cabd65-9a96-4af5-9a93-65f8a4425494-formdata")
@Generated(value = "formdata.client.ui.forms.SimpleForm", comments = "This class is auto generated by the Scout SDK. No manual modifications recommended.")
public class SimpleFormData extends AbstractFormData implements IFormDataInterface02, IFormDataInterface03 {

  private static final long serialVersionUID = 1L;

  public Date getDate() {
    return getFieldByClass(Date.class);
  }

  public Double getDouble() {
    return getFieldByClass(Double.class);
  }

  public MultiTypeArgsBox getMultiTypeArgsBox() {
    return getFieldByClass(MultiTypeArgsBox.class);
  }

  @Override
  public SampleComposer getSampleComposer() {
    return getFieldByClass(SampleComposer.class);
  }

  public SampleDate getSampleDate() {
    return getFieldByClass(SampleDate.class);
  }

  public SampleSmart getSampleSmart() {
    return getFieldByClass(SampleSmart.class);
  }

  public SampleString getSampleString() {
    return getFieldByClass(SampleString.class);
  }

  /**
   * access method for property SimpleNr.
   */
  public Long getSimpleNr() {
    return getSimpleNrProperty().getValue();
  }

  /**
   * access method for property SimpleNr.
   */
  public void setSimpleNr(Long simpleNr) {
    getSimpleNrProperty().setValue(simpleNr);
  }

  public SimpleNrProperty getSimpleNrProperty() {
    return getPropertyByClass(SimpleNrProperty.class);
  }

  @ClassId("25f5ffc9-512f-42b7-87d5-17ffe7a697e1-formdata")
  public static class Date extends AbstractValueFieldData<Integer> {

    private static final long serialVersionUID = 1L;
  }

  @ClassId("9a38e162-eade-444f-abac-64f03525117c-formdata")
  public static class Double extends AbstractValueFieldData<BigDecimal> {

    private static final long serialVersionUID = 1L;
  }

  @ClassId("dd8fe220-5d65-476e-ad02-94f0a0512eef-formdata")
  public static class MultiTypeArgsBox extends AbstractValueFieldData<TestRunnable> {

    private static final long serialVersionUID = 1L;
  }

  @ClassId("e8b88aa0-5a20-442f-8d5b-10092359fea0-formdata")
  public static class SampleComposer extends AbstractComposerData {

    private static final long serialVersionUID = 1L;
  }

  @ClassId("27ee583b-82d0-4694-ab7a-149fea22a14c-formdata")
  public static class SampleDate extends AbstractValueFieldData<java.util.Date> {

    private static final long serialVersionUID = 1L;
  }

  @ClassId("65a4fc9f-ba69-4c71-a3fa-5146ae85f2db-formdata")
  public static class SampleSmart extends AbstractValueFieldData<Long> {

    private static final long serialVersionUID = 1L;
  }

  @ClassId("dadf7c7a-226e-4dd3-8874-b326feb5209e-formdata")
  public static class SampleString extends AbstractValueFieldData<String> {

    private static final long serialVersionUID = 1L;
  }

  public static class SimpleNrProperty extends AbstractPropertyData<Long> {

    private static final long serialVersionUID = 1L;
  }
}
