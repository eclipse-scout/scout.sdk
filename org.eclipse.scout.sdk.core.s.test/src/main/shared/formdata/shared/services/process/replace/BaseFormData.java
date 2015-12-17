/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package formdata.shared.services.process.replace;

import javax.annotation.Generated;

import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated by the Scout SDK. No manual modifications recommended.
 */
@Generated(value = "formdata.client.ui.forms.replace.BaseForm", comments = "This class is auto generated by the Scout SDK. No manual modifications recommended.")
public class BaseFormData extends AbstractFormData {

  private static final long serialVersionUID = 1L;

  public Lookup getLookup() {
    return getFieldByClass(Lookup.class);
  }

  public Name getName() {
    return getFieldByClass(Name.class);
  }

  public SdkCommandCreate getSdkCommandCreate() {
    return getFieldByClass(SdkCommandCreate.class);
  }

  public SdkCommandNone getSdkCommandNone() {
    return getFieldByClass(SdkCommandNone.class);
  }

  public SdkCommandUse getSdkCommandUse() {
    return getFieldByClass(SdkCommandUse.class);
  }

  public Smart getSmart() {
    return getFieldByClass(Smart.class);
  }

  public static class Lookup extends AbstractValueFieldData<Long> {

    private static final long serialVersionUID = 1L;
  }

  public static class Name extends AbstractValueFieldData<String> {

    private static final long serialVersionUID = 1L;
  }

  public static class SdkCommandCreate extends AbstractValueFieldData<String> {

    private static final long serialVersionUID = 1L;
  }

  public static class SdkCommandNone extends AbstractValueFieldData<String> {

    private static final long serialVersionUID = 1L;
  }

  public static class SdkCommandUse extends UsingFormFieldData {

    private static final long serialVersionUID = 1L;
  }

  public static class Smart extends AbstractValueFieldData<Long> {

    private static final long serialVersionUID = 1L;
  }
}
