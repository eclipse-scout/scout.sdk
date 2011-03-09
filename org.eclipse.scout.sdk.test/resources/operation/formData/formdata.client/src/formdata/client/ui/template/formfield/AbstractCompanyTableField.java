package formdata.client.ui.template.formfield;

import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.annotations.FormData.SdkCommand;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.shared.ScoutTexts;

import formdata.shared.Texts;
import formdata.shared.services.process.AbstractCompanyTableFieldData;

@FormData(value = AbstractCompanyTableFieldData.class, sdkCommand = SdkCommand.CREATE)
public abstract class AbstractCompanyTableField extends AbstractTableField<AbstractCompanyTableField.Table> {

  @Override
  protected String getConfiguredLabel() {
    return Texts.get("Company");
  }

  @Order(10.0)
  public class Table extends AbstractTable {

    public NameColumn getNameColumn() {
      return getColumnSet().getColumnByClass(NameColumn.class);
    }

    @Order(10.0)
    public class NameColumn extends AbstractStringColumn {

      @Override
      protected String getConfiguredHeaderText() {
        return ScoutTexts.get("Name");
      }
    }
  }
}
