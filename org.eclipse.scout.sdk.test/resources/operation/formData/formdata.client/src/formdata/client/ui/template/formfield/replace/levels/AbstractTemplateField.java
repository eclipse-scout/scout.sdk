package formdata.client.ui.template.formfield.replace.levels;

import java.util.List;

import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.holders.IHolder;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;

import formdata.shared.ui.template.formfield.replace.levels.AbstractTemplateFieldData;

@FormData(value = AbstractTemplateFieldData.class, defaultSubtypeSdkCommand = FormData.DefaultSubtypeSdkCommand.CREATE, sdkCommand = FormData.SdkCommand.USE, genericOrdinal = 0)
public abstract class AbstractTemplateField<T> extends AbstractTableField<AbstractTemplateField<T>.Table> implements IHolder<List<T>> {

  public class Table extends AbstractTable {

  }

  @FormData
  @Override
  public List<T> getValue() {
    return null;
  }

  @FormData
  @Override
  public void setValue(List<T> o) {
  }

  @FormData
  @Override
  public Class<List<T>> getHolderType() {
    return null;
  }
}
