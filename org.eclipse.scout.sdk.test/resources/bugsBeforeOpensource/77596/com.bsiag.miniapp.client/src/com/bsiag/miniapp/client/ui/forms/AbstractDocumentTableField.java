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

import java.util.List;

import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.annotations.FormData.DefaultSubtypeSdkCommand;
import org.eclipse.scout.commons.annotations.FormData.SdkCommand;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.IDNDSupport;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractDateColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractObjectColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractSmartColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.shared.TEXTS;

import com.bsiag.miniapp.shared.services.process.AbstractDocumentTableFieldData;

/**
 * <p>
 * provides
 * <ul>
 * <li>getConfiguredLabel Documents</li>
 * <li>Open Menu with @Order(10.0)</li>
 * <li>Separator with @Order(20.0)</li>
 * <li>Details Menu with @Order(30.0)</li> <br>
 * <br>
 * <li>Drag&Drop</li>
 * </ul>
 * </p>
 */
@FormData(value = AbstractDocumentTableFieldData.class, sdkCommand = SdkCommand.CREATE, defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.CREATE)
public abstract class AbstractDocumentTableField extends AbstractTableField<AbstractDocumentTableField.Table> {

  @Override
  protected boolean getConfiguredLabelVisible() {
    return false;
  }

  @Override
  protected int getConfiguredGridW() {
    return 2;
  }

  @Override
  protected int getConfiguredGridH() {
    return 7;
  }

  @Order(10.0f)
  public class Table extends AbstractTable {

    @Override
    protected Class<? extends IMenu> getConfiguredDefaultMenu() {
      return OpenDocumentMenu.class;
    }

    @Override
    protected boolean getConfiguredAutoResizeColumns() {
      return true;
    }

    @Override
    protected int getConfiguredDragType() {
      return IDNDSupport.TYPE_FILE_TRANSFER;
    }

    @Override
    protected int getConfiguredDropType() {
      return IDNDSupport.TYPE_FILE_TRANSFER;
    }

    @Override
    protected TransferObject execDrag(List<ITableRow> rows) throws ProcessingException {
      return super.execDrag(rows);
    }

    @Override
    protected void execDrop(ITableRow row, TransferObject t) throws ProcessingException {
      super.execDrop(row, t);
    }

    public DocumentNrColumn getDocumentNrColumn() {
      return getColumnSet().getColumnByClass(DocumentNrColumn.class);
    }

    public NameColumn getNameColumn() {
      return getColumnSet().getColumnByClass(NameColumn.class);
    }

    public DescriptionColumn getDescriptionColumn() {
      return getColumnSet().getColumnByClass(DescriptionColumn.class);
    }

    public DocumentTypeColumn getDocumentTypeColumn() {
      return getColumnSet().getColumnByClass(DocumentTypeColumn.class);
    }

    public RegisteredOnColumn getRegisteredOnColumn() {
      return getColumnSet().getColumnByClass(RegisteredOnColumn.class);
    }

    public ChangedOnColumn getChangedOnColumn() {
      return getColumnSet().getColumnByClass(ChangedOnColumn.class);
    }

    public RegisteredByColumn getRegisteredByColumn() {
      return getColumnSet().getColumnByClass(RegisteredByColumn.class);
    }

    public ContentColumn getContentColumn() {
      return getColumnSet().getColumnByClass(ContentColumn.class);
    }

    @Order(10.0f)
    public class DocumentNrColumn extends AbstractLongColumn {
      @Override
      protected boolean getConfiguredPrimaryKey() {
        return true;
      }

      @Override
      protected boolean getConfiguredDisplayable() {
        return false;
      }
    }

    @Order(20.0f)
    public class NameColumn extends AbstractSmartColumn<String> {
      @Override
      protected String getConfiguredHeaderText() {
        return TEXTS.get("Name");
      }

      @Override
      protected int getConfiguredWidth() {
        return 210;
      }

    }

    @Order(30.0f)
    public class DescriptionColumn extends AbstractStringColumn {
      @Override
      protected int getConfiguredWidth() {
        return 100;
      }

      @Override
      protected String getConfiguredHeaderText() {
        return TEXTS.get("Description");
      }
    }

    @Order(40.0f)
    public class DocumentTypeColumn extends AbstractSmartColumn<Long> {

      @Override
      protected String getConfiguredHeaderText() {
        return TEXTS.get("DocumentType");
      }

      @Override
      protected int getConfiguredWidth() {
        return 140;
      }
    }

    @Order(50.0f)
    public class RegisteredOnColumn extends AbstractDateColumn {
      @Override
      protected String getConfiguredHeaderText() {
        return TEXTS.get("Date");
      }

      @Override
      protected int getConfiguredWidth() {
        return 70;
      }
    }

    @Order(60.0f)
    public class RegisteredByColumn extends AbstractSmartColumn<Long> {
      @Override
      protected String getConfiguredHeaderText() {
        return TEXTS.get("Person");
      }

      @Override
      protected int getConfiguredWidth() {
        return 170;
      }

    }

    @Order(70.0f)
    public class ChangedOnColumn extends AbstractDateColumn {
      @Override
      protected boolean getConfiguredDisplayable() {
        return false;
      }
    }

    @Order(80.0f)
    public class ContentColumn extends AbstractObjectColumn {
      @Override
      protected boolean getConfiguredDisplayable() {
        return false;
      }
    }

    @Order(80.0f)
    public class NewMenu extends AbstractMenu {
      @Override
      protected String getConfiguredText() {
        return TEXTS.get("New2");
      }
    }

    @Order(90.0f)
    public class OpenDocumentMenu extends AbstractMenu {
      @Override
      protected String getConfiguredText() {
        return TEXTS.get("OpenDocument");
      }

      @Override
      protected boolean getConfiguredInheritAccessibility() {
        return false;
      }

    }

    @Order(100.0f)
    public class SaveNewVersionInDatabaseMenu extends AbstractMenu {
      @Override
      protected String getConfiguredText() {
        return TEXTS.get("SaveNewVersionInDatabase");
      }
    }

    @Order(110.0f)
    public class SeparatorMenu extends AbstractMenu {
      @Override
      protected boolean getConfiguredSeparator() {
        return true;
      }
    }

    @Order(120.0f)
    public class EditMenu extends AbstractMenu {
      @Override
      protected String getConfiguredText() {
        return TEXTS.get("Edit");
      }
    }

    @Order(130.0f)
    public class DeleteMenu extends AbstractMenu {
      @Override
      protected String getConfiguredText() {
        return TEXTS.get("Delete2");
      }

      @Override
      protected void execAction() throws ProcessingException {
        deleteRows(getSelectedRows());
      }
    }
  }
}
