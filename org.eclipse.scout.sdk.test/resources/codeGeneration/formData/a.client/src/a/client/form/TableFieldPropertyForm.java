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
package a.client.form;

import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;

@FormData
public class TableFieldPropertyForm extends AbstractForm{

  public TableFieldPropertyForm() throws ProcessingException{
    super();
  }

  public MainBox getMainBox(){
    return getFieldByClass(MainBox.class);
  }

  public class MainBox extends AbstractGroupBox{

    @Order(10)
    public class TableField extends AbstractTableField<TableField.Table>{
      private String m_tableFieldProperty;

      @FormData
      public String getTableFieldProperty(){
        return m_tableFieldProperty;
      }

      @FormData
      public void setTableFieldProperty(String tableFieldProperty){
        m_tableFieldProperty=tableFieldProperty;
      }

      public class Table extends AbstractTable{
        private Long m_tableProperty;

        @FormData
        public Long getTableProperty(){
          return m_tableProperty;
        }

        @FormData
        public void setTableProperty(Long tableProperty){
          m_tableProperty=tableProperty;
        }

        public StringColumn getStringColumn(){
          return getColumnSet().getColumnByClass(StringColumn.class);
        }

        public IntegerColumn getIntegerColumn(){
          return getColumnSet().getColumnByClass(IntegerColumn.class);
        }

        @Order(10)
        public class StringColumn extends AbstractStringColumn{
          private Double m_columnProperty;

          @FormData
          public Double getColumnProperty(){
            return m_columnProperty;
          }

          @FormData
          public void setColumnProperty(Double columnProperty){
            m_columnProperty=columnProperty;
          }
        }

        @Order(20)
        public class IntegerColumn extends AbstractIntegerColumn{
        }
      }
    }
  }
}
