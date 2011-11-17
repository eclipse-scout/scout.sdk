package com.bsiag.miniapp.shared.services.process;

import java.util.Map;
import org.eclipse.scout.rt.shared.data.form.ValidationRule;
import java.util.Date;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldData;
import org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData;

public class CompanyFormData extends AbstractFormData {
  private static final long serialVersionUID = 1L;

  public CompanyFormData() {
  }

  public CompanyNrProperty getCompanyNrProperty() {
    return getPropertyByClass(CompanyNrProperty.class);
  }

  /**
   * access method for property CompanyNr.
   */
  public Long getCompanyNr() {
    return getCompanyNrProperty().getValue();
  }

  /**
   * access method for property CompanyNr.
   */
  public void setCompanyNr(Long companyNr) {
    getCompanyNrProperty().setValue(companyNr);
  }

  public InternalProperty getInternalProperty() {
    return getPropertyByClass(InternalProperty.class);
  }

  /**
   * access method for property Internal.
   */
  public boolean isInternal() {
    return (getInternalProperty().getValue() == null) ? (false) : (getInternalProperty().getValue());
  }

  /**
   * access method for property Internal.
   */
  public void setInternal(boolean internal) {
    getInternalProperty().setValue(internal);
  }

  public Active getActive() {
    return getFieldByClass(Active.class);
  }

  public AdditionalInformationTable getAdditionalInformationTable() {
    return getFieldByClass(AdditionalInformationTable.class);
  }

  public AddressTable getAddressTable() {
    return getFieldByClass(AddressTable.class);
  }

  public ChangesTable getChangesTable() {
    return getFieldByClass(ChangesTable.class);
  }

  public CompanyNo getCompanyNo() {
    return getFieldByClass(CompanyNo.class);
  }

  public CompanyShortName getCompanyShortName() {
    return getFieldByClass(CompanyShortName.class);
  }

  public CompanyType getCompanyType() {
    return getFieldByClass(CompanyType.class);
  }

  public DocumentTable getDocumentTable() {
    return getFieldByClass(DocumentTable.class);
  }

  public FiguresTable getFiguresTable() {
    return getFieldByClass(FiguresTable.class);
  }

  public FiguresYtDTable getFiguresYtDTable() {
    return getFieldByClass(FiguresYtDTable.class);
  }

  public InvoicesDue getInvoicesDue() {
    return getFieldByClass(InvoicesDue.class);
  }

  public Language getLanguage() {
    return getFieldByClass(Language.class);
  }

  public MainAccountManager getMainAccountManager() {
    return getFieldByClass(MainAccountManager.class);
  }

  public Name getName() {
    return getFieldByClass(Name.class);
  }

  public Notes getNotes() {
    return getFieldByClass(Notes.class);
  }

  public OpenBills getOpenBills() {
    return getFieldByClass(OpenBills.class);
  }

  public Rating getRating() {
    return getFieldByClass(Rating.class);
  }

  public Region getRegion() {
    return getFieldByClass(Region.class);
  }

  public Sector getSector() {
    return getFieldByClass(Sector.class);
  }

  public class CompanyNrProperty extends AbstractPropertyData<Long> {
    private static final long serialVersionUID = 1L;

    public CompanyNrProperty() {
    }
  }

  public class InternalProperty extends AbstractPropertyData<Boolean> {
    private static final long serialVersionUID = 1L;

    public InternalProperty() {
    }
  }

  public static class Active extends AbstractValueFieldData<Boolean> {
    private static final long serialVersionUID = 1L;

    public Active() {
    }
  }

  public static class AdditionalInformationTable extends AbstractTableFieldData {
    private static final long serialVersionUID = 1L;

    public AdditionalInformationTable() {
    }

    public static final int ADDITIONAL_INFORMATION_COLUMN_ID = 0;
    public static final int DISPLAY_COLUMN_ID = 1;
    public static final int TEXT_COLUMN_ID = 2;
    public static final int NUMBER_COLUMN_ID = 3;
    public static final int DATE_COLUMN_ID = 4;

    public void setAdditionalInformation(int row, Long additionalInformation) {
      setValueInternal(row, ADDITIONAL_INFORMATION_COLUMN_ID, additionalInformation);
    }

    public Long getAdditionalInformation(int row) {
      return (Long) getValueInternal(row, ADDITIONAL_INFORMATION_COLUMN_ID);
    }

    public void setDisplay(int row, String display) {
      setValueInternal(row, DISPLAY_COLUMN_ID, display);
    }

    public String getDisplay(int row) {
      return (String) getValueInternal(row, DISPLAY_COLUMN_ID);
    }

    public void setText(int row, String text) {
      setValueInternal(row, TEXT_COLUMN_ID, text);
    }

    public String getText(int row) {
      return (String) getValueInternal(row, TEXT_COLUMN_ID);
    }

    public void setNumber(int row, Double number) {
      setValueInternal(row, NUMBER_COLUMN_ID, number);
    }

    public Double getNumber(int row) {
      return (Double) getValueInternal(row, NUMBER_COLUMN_ID);
    }

    public void setDate(int row, Date date) {
      setValueInternal(row, DATE_COLUMN_ID, date);
    }

    public Date getDate(int row) {
      return (Date) getValueInternal(row, DATE_COLUMN_ID);
    }

    @Override
    public int getColumnCount() {
      return 5;
    }

    @Override
    public Object getValueAt(int row, int column) {
      switch (column) {
        case ADDITIONAL_INFORMATION_COLUMN_ID:
          return getAdditionalInformation(row);
        case DISPLAY_COLUMN_ID:
          return getDisplay(row);
        case TEXT_COLUMN_ID:
          return getText(row);
        case NUMBER_COLUMN_ID:
          return getNumber(row);
        case DATE_COLUMN_ID:
          return getDate(row);
        default:
          return null;
      }
    }

    @Override
    public void setValueAt(int row, int column, Object value) {
      switch (column) {
        case ADDITIONAL_INFORMATION_COLUMN_ID:
          setAdditionalInformation(row, (Long) value);
          break;
        case DISPLAY_COLUMN_ID:
          setDisplay(row, (String) value);
          break;
        case TEXT_COLUMN_ID:
          setText(row, (String) value);
          break;
        case NUMBER_COLUMN_ID:
          setNumber(row, (Double) value);
          break;
        case DATE_COLUMN_ID:
          setDate(row, (Date) value);
          break;
      }
    }
  }

  public static class AddressTable extends AbstractTableFieldData {
    private static final long serialVersionUID = 1L;

    public AddressTable() {
    }

    public static final int ADDRESS_TYPE_COLUMN_ID = 0;
    public static final int ADDITIONAL_NAME_COLUMN_ID = 1;
    public static final int STREET_COLUMN_ID = 2;
    public static final int PO_BOX_COLUMN_ID = 3;
    public static final int CITY_COLUMN_ID = 4;
    public static final int PHONE_COLUMN_ID = 5;
    public static final int FAX_COLUMN_ID = 6;
    public static final int E_MAIL_COLUMN_ID = 7;
    public static final int WWW_COLUMN_ID = 8;

    public void setAddressType(int row, Long addressType) {
      setValueInternal(row, ADDRESS_TYPE_COLUMN_ID, addressType);
    }

    public Long getAddressType(int row) {
      return (Long) getValueInternal(row, ADDRESS_TYPE_COLUMN_ID);
    }

    public void setAdditionalName(int row, String additionalName) {
      setValueInternal(row, ADDITIONAL_NAME_COLUMN_ID, additionalName);
    }

    public String getAdditionalName(int row) {
      return (String) getValueInternal(row, ADDITIONAL_NAME_COLUMN_ID);
    }

    public void setStreet(int row, String street) {
      setValueInternal(row, STREET_COLUMN_ID, street);
    }

    public String getStreet(int row) {
      return (String) getValueInternal(row, STREET_COLUMN_ID);
    }

    public void setPOBox(int row, String pOBox) {
      setValueInternal(row, PO_BOX_COLUMN_ID, pOBox);
    }

    public String getPOBox(int row) {
      return (String) getValueInternal(row, PO_BOX_COLUMN_ID);
    }

    public void setCity(int row, Long city) {
      setValueInternal(row, CITY_COLUMN_ID, city);
    }

    public Long getCity(int row) {
      return (Long) getValueInternal(row, CITY_COLUMN_ID);
    }

    public void setPhone(int row, String phone) {
      setValueInternal(row, PHONE_COLUMN_ID, phone);
    }

    public String getPhone(int row) {
      return (String) getValueInternal(row, PHONE_COLUMN_ID);
    }

    public void setFax(int row, String fax) {
      setValueInternal(row, FAX_COLUMN_ID, fax);
    }

    public String getFax(int row) {
      return (String) getValueInternal(row, FAX_COLUMN_ID);
    }

    public void setEMail(int row, String eMail) {
      setValueInternal(row, E_MAIL_COLUMN_ID, eMail);
    }

    public String getEMail(int row) {
      return (String) getValueInternal(row, E_MAIL_COLUMN_ID);
    }

    public void setWww(int row, String www) {
      setValueInternal(row, WWW_COLUMN_ID, www);
    }

    public String getWww(int row) {
      return (String) getValueInternal(row, WWW_COLUMN_ID);
    }

    @Override
    public int getColumnCount() {
      return 9;
    }

    @Override
    public Object getValueAt(int row, int column) {
      switch (column) {
        case ADDRESS_TYPE_COLUMN_ID:
          return getAddressType(row);
        case ADDITIONAL_NAME_COLUMN_ID:
          return getAdditionalName(row);
        case STREET_COLUMN_ID:
          return getStreet(row);
        case PO_BOX_COLUMN_ID:
          return getPOBox(row);
        case CITY_COLUMN_ID:
          return getCity(row);
        case PHONE_COLUMN_ID:
          return getPhone(row);
        case FAX_COLUMN_ID:
          return getFax(row);
        case E_MAIL_COLUMN_ID:
          return getEMail(row);
        case WWW_COLUMN_ID:
          return getWww(row);
        default:
          return null;
      }
    }

    @Override
    public void setValueAt(int row, int column, Object value) {
      switch (column) {
        case ADDRESS_TYPE_COLUMN_ID:
          setAddressType(row, (Long) value);
          break;
        case ADDITIONAL_NAME_COLUMN_ID:
          setAdditionalName(row, (String) value);
          break;
        case STREET_COLUMN_ID:
          setStreet(row, (String) value);
          break;
        case PO_BOX_COLUMN_ID:
          setPOBox(row, (String) value);
          break;
        case CITY_COLUMN_ID:
          setCity(row, (Long) value);
          break;
        case PHONE_COLUMN_ID:
          setPhone(row, (String) value);
          break;
        case FAX_COLUMN_ID:
          setFax(row, (String) value);
          break;
        case E_MAIL_COLUMN_ID:
          setEMail(row, (String) value);
          break;
        case WWW_COLUMN_ID:
          setWww(row, (String) value);
          break;
      }
    }
  }

  public static class ChangesTable extends AbstractTableFieldData {
    private static final long serialVersionUID = 1L;

    public ChangesTable() {
    }

    public static final int TYPE_COLUMN_ID = 0;
    public static final int PERSON_COLUMN_ID = 1;
    public static final int DATE_COLUMN_ID = 2;
    public static final int MAIN_ACCOUNT_MANAGER_COLUMN_ID = 3;

    public void setType(int row, Long type) {
      setValueInternal(row, TYPE_COLUMN_ID, type);
    }

    public Long getType(int row) {
      return (Long) getValueInternal(row, TYPE_COLUMN_ID);
    }

    public void setPerson(int row, String person) {
      setValueInternal(row, PERSON_COLUMN_ID, person);
    }

    public String getPerson(int row) {
      return (String) getValueInternal(row, PERSON_COLUMN_ID);
    }

    public void setDate(int row, Date date) {
      setValueInternal(row, DATE_COLUMN_ID, date);
    }

    public Date getDate(int row) {
      return (Date) getValueInternal(row, DATE_COLUMN_ID);
    }

    public void setMainAccountManager(int row, String mainAccountManager) {
      setValueInternal(row, MAIN_ACCOUNT_MANAGER_COLUMN_ID, mainAccountManager);
    }

    public String getMainAccountManager(int row) {
      return (String) getValueInternal(row, MAIN_ACCOUNT_MANAGER_COLUMN_ID);
    }

    @Override
    public int getColumnCount() {
      return 4;
    }

    @Override
    public Object getValueAt(int row, int column) {
      switch (column) {
        case TYPE_COLUMN_ID:
          return getType(row);
        case PERSON_COLUMN_ID:
          return getPerson(row);
        case DATE_COLUMN_ID:
          return getDate(row);
        case MAIN_ACCOUNT_MANAGER_COLUMN_ID:
          return getMainAccountManager(row);
        default:
          return null;
      }
    }

    @Override
    public void setValueAt(int row, int column, Object value) {
      switch (column) {
        case TYPE_COLUMN_ID:
          setType(row, (Long) value);
          break;
        case PERSON_COLUMN_ID:
          setPerson(row, (String) value);
          break;
        case DATE_COLUMN_ID:
          setDate(row, (Date) value);
          break;
        case MAIN_ACCOUNT_MANAGER_COLUMN_ID:
          setMainAccountManager(row, (String) value);
          break;
      }
    }
  }

  public static class CompanyNo extends AbstractValueFieldData<String> {
    private static final long serialVersionUID = 1L;

    public CompanyNo() {
    }

    /**
     * list of derived validation rules.
     */
    @Override
    protected void initValidationRules(Map<String, Object> ruleMap) {
      super.initValidationRules(ruleMap);
      ruleMap.put(ValidationRule.MAX_LENGTH, 60);
    }
  }

  public static class CompanyShortName extends AbstractValueFieldData<String> {
    private static final long serialVersionUID = 1L;

    public CompanyShortName() {
    }

    /**
     * list of derived validation rules.
     */
    @Override
    protected void initValidationRules(Map<String, Object> ruleMap) {
      super.initValidationRules(ruleMap);
      ruleMap.put(ValidationRule.MANDATORY, true);
      ruleMap.put(ValidationRule.MAX_LENGTH, 60);
    }
  }

  public static class CompanyType extends AbstractValueFieldData<Long[]> {
    private static final long serialVersionUID = 1L;

    public CompanyType() {
    }
  }

  public static class DocumentTable extends AbstractDocumentTableFieldData {
    private static final long serialVersionUID = 1L;

    public DocumentTable() {
    }
  }

  public static class FiguresTable extends AbstractTableFieldData {
    private static final long serialVersionUID = 1L;

    public FiguresTable() {
    }

    public static final int YEAR_COLUMN_ID = 0;
    public static final int POTENTIAL_COLUMN_ID = 1;
    public static final int BUDGET_COLUMN_ID = 2;
    public static final int TURNOVER_COLUMN_ID = 3;

    public void setYear(int row, Long year) {
      setValueInternal(row, YEAR_COLUMN_ID, year);
    }

    public Long getYear(int row) {
      return (Long) getValueInternal(row, YEAR_COLUMN_ID);
    }

    public void setPotential(int row, Long potential) {
      setValueInternal(row, POTENTIAL_COLUMN_ID, potential);
    }

    public Long getPotential(int row) {
      return (Long) getValueInternal(row, POTENTIAL_COLUMN_ID);
    }

    public void setBudget(int row, Long budget) {
      setValueInternal(row, BUDGET_COLUMN_ID, budget);
    }

    public Long getBudget(int row) {
      return (Long) getValueInternal(row, BUDGET_COLUMN_ID);
    }

    public void setTurnover(int row, Long turnover) {
      setValueInternal(row, TURNOVER_COLUMN_ID, turnover);
    }

    public Long getTurnover(int row) {
      return (Long) getValueInternal(row, TURNOVER_COLUMN_ID);
    }

    @Override
    public int getColumnCount() {
      return 4;
    }

    @Override
    public Object getValueAt(int row, int column) {
      switch (column) {
        case YEAR_COLUMN_ID:
          return getYear(row);
        case POTENTIAL_COLUMN_ID:
          return getPotential(row);
        case BUDGET_COLUMN_ID:
          return getBudget(row);
        case TURNOVER_COLUMN_ID:
          return getTurnover(row);
        default:
          return null;
      }
    }

    @Override
    public void setValueAt(int row, int column, Object value) {
      switch (column) {
        case YEAR_COLUMN_ID:
          setYear(row, (Long) value);
          break;
        case POTENTIAL_COLUMN_ID:
          setPotential(row, (Long) value);
          break;
        case BUDGET_COLUMN_ID:
          setBudget(row, (Long) value);
          break;
        case TURNOVER_COLUMN_ID:
          setTurnover(row, (Long) value);
          break;
      }
    }
  }

  public static class FiguresYtDTable extends AbstractTableFieldData {
    private static final long serialVersionUID = 1L;

    public FiguresYtDTable() {
    }

    public static final int YEAR_COLUMN_ID = 0;
    public static final int POTENTIAL_COLUMN_ID = 1;
    public static final int BUDGET_COLUMN_ID = 2;
    public static final int TURNOVER_COLUMN_ID = 3;

    public void setYear(int row, Long year) {
      setValueInternal(row, YEAR_COLUMN_ID, year);
    }

    public Long getYear(int row) {
      return (Long) getValueInternal(row, YEAR_COLUMN_ID);
    }

    public void setPotential(int row, Long potential) {
      setValueInternal(row, POTENTIAL_COLUMN_ID, potential);
    }

    public Long getPotential(int row) {
      return (Long) getValueInternal(row, POTENTIAL_COLUMN_ID);
    }

    public void setBudget(int row, Long budget) {
      setValueInternal(row, BUDGET_COLUMN_ID, budget);
    }

    public Long getBudget(int row) {
      return (Long) getValueInternal(row, BUDGET_COLUMN_ID);
    }

    public void setTurnover(int row, Long turnover) {
      setValueInternal(row, TURNOVER_COLUMN_ID, turnover);
    }

    public Long getTurnover(int row) {
      return (Long) getValueInternal(row, TURNOVER_COLUMN_ID);
    }

    @Override
    public int getColumnCount() {
      return 4;
    }

    @Override
    public Object getValueAt(int row, int column) {
      switch (column) {
        case YEAR_COLUMN_ID:
          return getYear(row);
        case POTENTIAL_COLUMN_ID:
          return getPotential(row);
        case BUDGET_COLUMN_ID:
          return getBudget(row);
        case TURNOVER_COLUMN_ID:
          return getTurnover(row);
        default:
          return null;
      }
    }

    @Override
    public void setValueAt(int row, int column, Object value) {
      switch (column) {
        case YEAR_COLUMN_ID:
          setYear(row, (Long) value);
          break;
        case POTENTIAL_COLUMN_ID:
          setPotential(row, (Long) value);
          break;
        case BUDGET_COLUMN_ID:
          setBudget(row, (Long) value);
          break;
        case TURNOVER_COLUMN_ID:
          setTurnover(row, (Long) value);
          break;
      }
    }
  }

  public static class InvoicesDue extends AbstractValueFieldData<Double> {
    private static final long serialVersionUID = 1L;

    public InvoicesDue() {
    }

    /**
     * list of derived validation rules.
     */
    @Override
    protected void initValidationRules(Map<String, Object> ruleMap) {
      super.initValidationRules(ruleMap);
      ruleMap.put(ValidationRule.MAX_VALUE, 999999999.0);
      ruleMap.put(ValidationRule.MIN_VALUE, -999999999.0);
    }
  }

  public static class Language extends AbstractValueFieldData<Long> {
    private static final long serialVersionUID = 1L;

    public Language() {
    }

    /**
     * list of derived validation rules.
     */
    @Override
    protected void initValidationRules(Map<String, Object> ruleMap) {
      super.initValidationRules(ruleMap);
      ruleMap.put(ValidationRule.ZERO_NULL_EQUALITY, true);
    }
  }

  public static class MainAccountManager extends AbstractValueFieldData<Long> {
    private static final long serialVersionUID = 1L;

    public MainAccountManager() {
    }

    /**
     * list of derived validation rules.
     */
    @Override
    protected void initValidationRules(Map<String, Object> ruleMap) {
      super.initValidationRules(ruleMap);
      ruleMap.put(ValidationRule.MANDATORY, true);
      ruleMap.put(ValidationRule.ZERO_NULL_EQUALITY, true);
    }
  }

  public static class Name extends AbstractValueFieldData<String> {
    private static final long serialVersionUID = 1L;

    public Name() {
    }

    /**
     * list of derived validation rules.
     */
    @Override
    protected void initValidationRules(Map<String, Object> ruleMap) {
      super.initValidationRules(ruleMap);
      ruleMap.put(ValidationRule.MANDATORY, true);
      ruleMap.put(ValidationRule.MAX_LENGTH, 250);
    }
  }

  public static class Notes extends AbstractValueFieldData<String> {
    private static final long serialVersionUID = 1L;

    public Notes() {
    }

    /**
     * list of derived validation rules.
     */
    @Override
    protected void initValidationRules(Map<String, Object> ruleMap) {
      super.initValidationRules(ruleMap);
      ruleMap.put(ValidationRule.MAX_LENGTH, 2000);
    }
  }

  public static class OpenBills extends AbstractValueFieldData<Double> {
    private static final long serialVersionUID = 1L;

    public OpenBills() {
    }

    /**
     * list of derived validation rules.
     */
    @Override
    protected void initValidationRules(Map<String, Object> ruleMap) {
      super.initValidationRules(ruleMap);
      ruleMap.put(ValidationRule.MAX_VALUE, 999999999.0);
      ruleMap.put(ValidationRule.MIN_VALUE, -999999999.0);
    }
  }

  public static class Rating extends AbstractValueFieldData<Long> {
    private static final long serialVersionUID = 1L;

    public Rating() {
    }

    /**
     * list of derived validation rules.
     */
    @Override
    protected void initValidationRules(Map<String, Object> ruleMap) {
      super.initValidationRules(ruleMap);
      ruleMap.put(ValidationRule.ZERO_NULL_EQUALITY, true);
    }
  }

  public static class Region extends AbstractValueFieldData<Long> {
    private static final long serialVersionUID = 1L;

    public Region() {
    }

    /**
     * list of derived validation rules.
     */
    @Override
    protected void initValidationRules(Map<String, Object> ruleMap) {
      super.initValidationRules(ruleMap);
      ruleMap.put(ValidationRule.MANDATORY, true);
      ruleMap.put(ValidationRule.ZERO_NULL_EQUALITY, true);
    }
  }

  public static class Sector extends AbstractValueFieldData<Long> {
    private static final long serialVersionUID = 1L;

    public Sector() {
    }

    /**
     * list of derived validation rules.
     */
    @Override
    protected void initValidationRules(Map<String, Object> ruleMap) {
      super.initValidationRules(ruleMap);
      ruleMap.put(ValidationRule.MANDATORY, true);
      ruleMap.put(ValidationRule.ZERO_NULL_EQUALITY, true);
    }
  }
}
