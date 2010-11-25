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
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.integerfield.AbstractIntegerField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.service.SERVICES;

import test.client.ui.forms.CompanyForm.MainBox.DetailsGroup;
import test.client.ui.forms.CompanyForm.MainBox.NameField;
import test.client.ui.forms.CompanyForm.MainBox.SinceField;
import test.client.ui.forms.CompanyForm.MainBox.DetailsGroup.AnzahlField;
import test.shared.Texts;
import test.shared.security.UpdateCompanyPermission;
import test.shared.services.process.CompanyFormData;
import test.shared.services.process.ICompanyProcessService;

@FormData
public class CompanyForm extends AbstractForm {

  private Long m_companyNr;

  public CompanyForm() throws ProcessingException {
    super();
  }

  @Override
  protected String getConfiguredTitle() {
    return Texts.get("Company");
  }

  @FormData
  public Long getCompanyNr() {
    return m_companyNr;
  }

  @FormData
  public void setCompanyNr(Long companyNr) {
    m_companyNr = companyNr;
  }

  public void startModify() throws ProcessingException {
    startInternal(new ModifyHandler());
  }

  public void startNew() throws ProcessingException {
    startInternal(new NewHandler());
  }

  public AnzahlField getAnzahlField() {
    return getFieldByClass(AnzahlField.class);
  }

  public DetailsGroup getDetailsGroup() {
    return getFieldByClass(DetailsGroup.class);
  }

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  public NameField getNameField() {
    return getFieldByClass(NameField.class);
  }

  public SinceField getSinceField() {
    return getFieldByClass(SinceField.class);
  }

  @Order(10.0)
  public class MainBox extends AbstractGroupBox {

    @Order(10.0)
    public class NameField extends AbstractStringField {

      @Override
      protected String getConfiguredLabel() {
        return ScoutTexts.get("Name");
      }
    }

    @Order(20.0)
    public class SinceField extends AbstractDateField {

      @Override
      protected String getConfiguredLabel() {
        return Texts.get("Since");
      }
    }

    @Order(30.0)
    public class DetailsGroup extends AbstractGroupBox {

      @Override
      protected String getConfiguredLabel() {
        return ScoutTexts.get("Details");
      }

      @Order(10.0)
      public class AnzahlField extends AbstractIntegerField {

        @Override
        protected String getConfiguredLabel() {
          return Texts.get("Anzahl");
        }
      }
    }
  }

  public class ModifyHandler extends AbstractFormHandler {

    @Override
    public void execLoad() throws ProcessingException {
      ICompanyProcessService service = SERVICES.getService(ICompanyProcessService.class);
      CompanyFormData formData = new CompanyFormData();
      exportFormData(formData);
      formData = service.load(formData);
      importFormData(formData);
      setEnabledPermission(new UpdateCompanyPermission());
    }

    @Override
    public void execStore() throws ProcessingException {
      ICompanyProcessService service = SERVICES.getService(ICompanyProcessService.class);
      CompanyFormData formData = new CompanyFormData();
      exportFormData(formData);
      formData = service.store(formData);
    }
  }

  public class NewHandler extends AbstractFormHandler {

    @Override
    public void execLoad() throws ProcessingException {
      ICompanyProcessService service = SERVICES.getService(ICompanyProcessService.class);
      CompanyFormData formData = new CompanyFormData();
      exportFormData(formData);
      formData = service.prepareCreate(formData);
      importFormData(formData);
    }

    @Override
    public void execStore() throws ProcessingException {
      ICompanyProcessService service = SERVICES.getService(ICompanyProcessService.class);
      CompanyFormData formData = new CompanyFormData();
      exportFormData(formData);
      formData = service.create(formData);
    }
  }
}
