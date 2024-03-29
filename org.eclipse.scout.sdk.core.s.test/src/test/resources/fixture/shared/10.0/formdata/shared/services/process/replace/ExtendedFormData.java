/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package formdata.shared.services.process.replace;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated by the Scout SDK. No manual modifications recommended.
 */
@Generated(value = "formdata.client.ui.forms.replace.ExtendedForm", comments = "This class is auto generated by the Scout SDK. No manual modifications recommended.")
public class ExtendedFormData extends BaseFormData {

  private static final long serialVersionUID = 1L;

  public FirstName getFirstName() {
    return getFieldByClass(FirstName.class);
  }

  public IgnoringGroupBoxExCreate getIgnoringGroupBoxExCreate() {
    return getFieldByClass(IgnoringGroupBoxExCreate.class);
  }

  public IgnoringGroupBoxExUse getIgnoringGroupBoxExUse() {
    return getFieldByClass(IgnoringGroupBoxExUse.class);
  }

  public NameEx getNameEx() {
    return getFieldByClass(NameEx.class);
  }

  public SdkCommandCreateCreate getSdkCommandCreateCreate() {
    return getFieldByClass(SdkCommandCreateCreate.class);
  }

  public SdkCommandCreateIgnore getSdkCommandCreateIgnore() {
    return getFieldByClass(SdkCommandCreateIgnore.class);
  }

  public SdkCommandCreateNone getSdkCommandCreateNone() {
    return getFieldByClass(SdkCommandCreateNone.class);
  }

  public SdkCommandCreateUse getSdkCommandCreateUse() {
    return getFieldByClass(SdkCommandCreateUse.class);
  }

  public SdkCommandIgnoreCreate getSdkCommandIgnoreCreate() {
    return getFieldByClass(SdkCommandIgnoreCreate.class);
  }

  public SdkCommandIgnoreUse getSdkCommandIgnoreUse() {
    return getFieldByClass(SdkCommandIgnoreUse.class);
  }

  public SdkCommandNoneCreate getSdkCommandNoneCreate() {
    return getFieldByClass(SdkCommandNoneCreate.class);
  }

  public SdkCommandNoneIgnore getSdkCommandNoneIgnore() {
    return getFieldByClass(SdkCommandNoneIgnore.class);
  }

  public SdkCommandNoneNone getSdkCommandNoneNone() {
    return getFieldByClass(SdkCommandNoneNone.class);
  }

  public SdkCommandNoneUse getSdkCommandNoneUse() {
    return getFieldByClass(SdkCommandNoneUse.class);
  }

  public SdkCommandUseCreate getSdkCommandUseCreate() {
    return getFieldByClass(SdkCommandUseCreate.class);
  }

  public SdkCommandUseIgnore getSdkCommandUseIgnore() {
    return getFieldByClass(SdkCommandUseIgnore.class);
  }

  public SdkCommandUseNone getSdkCommandUseNone() {
    return getFieldByClass(SdkCommandUseNone.class);
  }

  public SdkCommandUseUse getSdkCommandUseUse() {
    return getFieldByClass(SdkCommandUseUse.class);
  }

  public SmartEx getSmartEx() {
    return getFieldByClass(SmartEx.class);
  }

  public static class FirstName extends AbstractValueFieldData<String> {

    private static final long serialVersionUID = 1L;
  }

  @Replace
  public static class IgnoringGroupBoxExCreate extends AbstractValueFieldData<String> {

    private static final long serialVersionUID = 1L;
  }

  @Replace
  public static class IgnoringGroupBoxExUse extends UsingFormFieldData {

    private static final long serialVersionUID = 1L;
  }

  @Replace
  public static class NameEx extends Name {

    private static final long serialVersionUID = 1L;
  }

  @Replace
  public static class SdkCommandCreateCreate extends SdkCommandCreate {

    private static final long serialVersionUID = 1L;
  }

  @Replace
  public static class SdkCommandCreateIgnore extends SdkCommandCreate {

    private static final long serialVersionUID = 1L;
  }

  @Replace
  public static class SdkCommandCreateNone extends SdkCommandCreate {

    private static final long serialVersionUID = 1L;
  }

  @Replace
  public static class SdkCommandCreateUse extends SdkCommandCreate {

    private static final long serialVersionUID = 1L;
  }

  @Replace
  public static class SdkCommandIgnoreCreate extends AbstractValueFieldData<String> {

    private static final long serialVersionUID = 1L;
  }

  @Replace
  public static class SdkCommandIgnoreUse extends UsingFormFieldData {

    private static final long serialVersionUID = 1L;
  }

  @Replace
  public static class SdkCommandNoneCreate extends SdkCommandNone {

    private static final long serialVersionUID = 1L;
  }

  @Replace
  public static class SdkCommandNoneIgnore extends SdkCommandNone {

    private static final long serialVersionUID = 1L;
  }

  @Replace
  public static class SdkCommandNoneNone extends SdkCommandNone {

    private static final long serialVersionUID = 1L;
  }

  @Replace
  public static class SdkCommandNoneUse extends SdkCommandNone {

    private static final long serialVersionUID = 1L;
  }

  @Replace
  public static class SdkCommandUseCreate extends SdkCommandUse {

    private static final long serialVersionUID = 1L;
  }

  @Replace
  public static class SdkCommandUseIgnore extends SdkCommandUse {

    private static final long serialVersionUID = 1L;
  }

  @Replace
  public static class SdkCommandUseNone extends SdkCommandUse {

    private static final long serialVersionUID = 1L;
  }

  @Replace
  public static class SdkCommandUseUse extends SdkCommandUse {

    private static final long serialVersionUID = 1L;
  }

  @Replace
  public static class SmartEx extends Smart {

    private static final long serialVersionUID = 1L;
  }
}
