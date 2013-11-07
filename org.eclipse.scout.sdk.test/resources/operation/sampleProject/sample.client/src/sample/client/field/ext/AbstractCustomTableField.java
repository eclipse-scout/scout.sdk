/**
 *
 */
package sample.client.field.ext;

import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;

import sample.client.field.ext.AbstractCustomTableField.Table;

/**
 *  @author Andreas Hoegger
 */
public abstract class AbstractCustomTableField extends AbstractTableField<Table> {

  public class Table extends AbstractTable {

  }
}
