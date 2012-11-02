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
package com.bsiag.miniapp.client.ui.forms;

import java.beans.PropertyChangeListener;

import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.service.SERVICES;

import com.bsiag.miniapp.client.ui.forms.PersonForm.MainBox.CountryField;
import com.bsiag.miniapp.client.ui.forms.PersonForm.MainBox.HugoField;
import com.bsiag.miniapp.shared.security.UpdatePersonPermission;
import com.bsiag.miniapp.shared.services.process.IPersonProcessService;
import com.bsiag.miniapp.shared.services.process.PersonFormData;

@FormData
public class PersonForm extends AbstractForm {

  protected static IScoutLogger LOG = ScoutLogManager.getLogger(PersonForm.class);
  public static final String PROP_NAME = "name";
  private Long m_personNr;
  private boolean m_test;
  private boolean m_myvar;

  public PersonForm() throws ProcessingException {
    super();

  }

  @FormData
  public boolean isMyvar() {
    return m_myvar;
  }

  @FormData
  public void setMyvar(boolean myvar) {
    m_myvar = myvar;
  }

  @FormData
  public boolean isTest() {
    return m_test;

  }

  @FormData
  public void setTest(boolean test) {
    m_test = test;
  }

  @FormData
  public Long getPersonNr() {
    return m_personNr;
  }

  @FormData
  public void setPersonNr(Long personNr) {
    m_personNr = personNr;
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    super.addPropertyChangeListener(listener);
  }

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  public HugoField getHugoField() {
    return getFieldByClass(HugoField.class);
  }

  public CountryField getCountryField() {
    return getFieldByClass(CountryField.class);
  }

  @Override
  public String getConfiguredTitle() {
    return TEXTS.get("Person");
  }

  @Override
  @Order(45.0)
  protected boolean getConfiguredAskIfNeedSave() {
    return false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  public void startKolak() throws ProcessingException {
    startInternal(new KolakHandler());
  }

  public void startModify() throws ProcessingException {
    startInternal(new ModifyHandler());
  }

  public void startNew() throws ProcessingException {
    startInternal(new NewHandler());
  }

  @Override
  public String toString() {
    return super.toString();
  }

  @Order(10.0)
  public class MainBox extends AbstractGroupBox {

    @Order(10.0)
    public class HugoField extends AbstractStringField {

      @Override
      public String getConfiguredLabel() {
        return TEXTS.get("Hugo");
      }

      @Override
      protected boolean getConfiguredFocusable() {
        return true;
      }
    }

    @Order(20.0)
    public class CountryField extends AbstractSmartField<Long> {

      @Override
      public String getConfiguredLabel() {
        return TEXTS.get("Country");
      }
    }
  }

  @Order(10.0)
  public class NewHandler extends AbstractFormHandler {

    @Override
    public void execLoad() throws ProcessingException {
      IPersonProcessService service = SERVICES.getService(IPersonProcessService.class);
      PersonFormData formData = new PersonFormData();
      exportFormData(formData);
      formData = service.prepareCreate(formData);
      importFormData(formData);
    }

    @Override
    public void execStore() throws ProcessingException {
      IPersonProcessService service = SERVICES.getService(IPersonProcessService.class);
      PersonFormData formData = new PersonFormData();
      exportFormData(formData);
      formData = service.create(formData);
    }
  }

  @Order(20.0)
  public class ModifyHandler extends AbstractFormHandler {

    @Override
    public void execLoad() throws ProcessingException {
      IPersonProcessService service = SERVICES.getService(IPersonProcessService.class);
      PersonFormData formData = new PersonFormData();
      exportFormData(formData);
      formData = service.load(formData);
      importFormData(formData);
      setEnabledPermission(new UpdatePersonPermission());
    }

    @Override
    public void execStore() throws ProcessingException {
      IPersonProcessService service = SERVICES.getService(IPersonProcessService.class);
      PersonFormData formData = new PersonFormData();
      exportFormData(formData);
      formData = service.store(formData);
    }
  }

  @Order(30.0)
  public class KolakHandler extends AbstractFormHandler {
  }
}
