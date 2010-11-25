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
package test.client.ui.forms;

import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;

import test.shared.Texts;

@FormData
public class Test1Form extends AbstractForm {

  private Long m_test1Nr;

  public Test1Form() throws ProcessingException {
    super();
  }

  @Override
  protected boolean getConfiguredMinimized() {
    return true;
  }

  @Override
  protected String getConfiguredTitle() {
    return Texts.get("Test1");
  }

  @Override
  protected void execDataChanged(Object... dataTypes) throws ProcessingException {
    //TODO [${user.name}] Auto-generated method stub.
  }

  @Override
  protected void execInitForm() throws ProcessingException {
    //TODO [${user.name}] Auto-generated method stub.
  }

  @Override
  protected boolean execValidate() throws ProcessingException {
    //TODO [${user.name}] Auto-generated method stub.
    return true;
  }

  @FormData
  public Long getTest1Nr() {
    return m_test1Nr;
  }

  @FormData
  public void setTest1Nr(Long test1Nr) {
    m_test1Nr = test1Nr;
  }

  public void startModify() throws ProcessingException {
    startInternal(new ModifyHandler());
  }

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  @Order(10.0)
  public class MainBox extends AbstractGroupBox {
  }

  @Order(10.0)
  public class ModifyHandler extends AbstractFormHandler {
  }
}
