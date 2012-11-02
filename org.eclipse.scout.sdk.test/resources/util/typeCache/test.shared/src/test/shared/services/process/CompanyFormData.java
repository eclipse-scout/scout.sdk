package test.shared.services.process;

import java.util.Date;
import org.eclipse.scout.rt.shared.data.form.ValidationRule;
import java.util.Map;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
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

  public Anzahl getAnzahl() {
    return getFieldByClass(Anzahl.class);
  }

  public Name getName() {
    return getFieldByClass(Name.class);
  }

  public Since getSince() {
    return getFieldByClass(Since.class);
  }

  public class CompanyNrProperty extends AbstractPropertyData<Long> {
    private static final long serialVersionUID = 1L;

    public CompanyNrProperty() {
    }
  }

  public static class Anzahl extends AbstractValueFieldData<Integer> {
    private static final long serialVersionUID = 1L;

    public Anzahl() {
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
      ruleMap.put(ValidationRule.MAX_LENGTH, 4000);
    }
  }

  public static class Since extends AbstractValueFieldData<Date> {
    private static final long serialVersionUID = 1L;

    public Since() {
    }
  }
}
