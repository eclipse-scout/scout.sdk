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

import java.util.Map;
import java.util.Set;
import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractBigDecimalColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractBooleanColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractDateColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractDoubleColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractObjectColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractSmartColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractTimeColumn;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;

@FormData
public class TableFieldForm extends AbstractForm{

  public TableFieldForm() throws ProcessingException{
    super();
  }

  public MainBox getMainBox(){
    return getFieldByClass(MainBox.class);
  }

  public class MainBox extends AbstractGroupBox{

    @Order(10)
    public class TableField extends AbstractTableField<TableField.Table>{
      public class Table extends AbstractTable{
        public StringColumn getStringColumn(){
          return getColumnSet().getColumnByClass(StringColumn.class);
        }

        public IntegerColumn getIntegerColumn(){
          return getColumnSet().getColumnByClass(IntegerColumn.class);
        }

        public LongColumn getLongColumn(){
          return getColumnSet().getColumnByClass(LongColumn.class);
        }

        public DoubleColumn getDoubleColumn(){
          return getColumnSet().getColumnByClass(DoubleColumn.class);
        }

        public DateColumn getDateColumn(){
          return getColumnSet().getColumnByClass(DateColumn.class);
        }

        public BigDecimalColumn getBigDecimalColumn(){
          return getColumnSet().getColumnByClass(BigDecimalColumn.class);
        }

        public BooleanColumn getBooleanColumn(){
          return getColumnSet().getColumnByClass(BooleanColumn.class);
        }

        public TimeColumn getTimeColumn(){
          return getColumnSet().getColumnByClass(TimeColumn.class);
        }

        public ObjectColumn getObjectColumn(){
          return getColumnSet().getColumnByClass(ObjectColumn.class);
        }

        public SmartLongColumn getSmartLongColumn(){
          return getColumnSet().getColumnByClass(SmartLongColumn.class);
        }

        public SmartCustomColumn getSmartCustomColumn(){
          return getColumnSet().getColumnByClass(SmartCustomColumn.class);
        }

        @Order(10)
        public class StringColumn extends AbstractStringColumn{
        }

        @Order(20)
        public class IntegerColumn extends AbstractIntegerColumn{
        }

        @Order(30)
        public class LongColumn extends AbstractLongColumn{
        }

        @Order(40)
        public class DoubleColumn extends AbstractDoubleColumn{
        }

        @Order(50)
        public class DateColumn extends AbstractDateColumn{
        }

        @Order(60)
        public class BigDecimalColumn extends AbstractBigDecimalColumn{
        }

        @Order(70)
        public class BooleanColumn extends AbstractBooleanColumn{
        }

        @Order(80)
        public class TimeColumn extends AbstractTimeColumn{
        }

        @Order(90)
        public class ObjectColumn extends AbstractObjectColumn{
        }

        @Order(100)
        public class SmartLongColumn extends AbstractSmartColumn<Long>{
        }

        @Order(110)
        public class SmartCustomColumn extends AbstractColumn<Set<Map<String, Integer>>>{
        }
      }
    }
  }
}
