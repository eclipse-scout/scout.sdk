package @@BUNDLE_MOBILE_CLIENT_NAME@@.ui.desktop;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.mobile.transformation.IDeviceTransformationService;
import org.eclipse.scout.rt.client.mobile.transformation.MobileDeviceTransformation;
import org.eclipse.scout.rt.client.mobile.ui.basic.table.AbstractMobileTable;
import org.eclipse.scout.rt.client.mobile.ui.desktop.MobileDesktopUtility;
import org.eclipse.scout.rt.client.mobile.ui.form.AbstractMobileForm;
import org.eclipse.scout.rt.client.mobile.ui.form.outline.IOutlineChooserForm;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.service.SERVICES;

import com.bsiag.crm.client.mobile.ui.desktop.HomeForm.MainBox.OutlinesTableField;

public class HomeForm extends AbstractMobileForm implements IOutlineChooserForm {

  public HomeForm() throws ProcessingException {
    super();
  }

  @Override
  protected boolean getConfiguredAskIfNeedSave() {
    return false;
  }

  @Override
  protected int getConfiguredDisplayHint() {
    return DISPLAY_HINT_VIEW;
  }

  @Override
  protected String getConfiguredDisplayViewId() {
    return VIEW_ID_CENTER;
  }

  @Override
  protected String getConfiguredTitle() {
    return TEXTS.get("MobileOutlineChooserTitle");
  }

  public void startView() throws ProcessingException {
    startInternal(new ViewHandler());
  }

  public MainBox getMainBox() {
    return (MainBox) getRootGroupBox();
  }

  public OutlinesTableField getOutlinesTableField() {
    return getFieldByClass(OutlinesTableField.class);
  }

  @Override
  protected boolean getConfiguredFooterVisible() {
    return true;
  }

  @Order(10.0)
  public class MainBox extends AbstractGroupBox {

    @Override
    protected boolean getConfiguredBorderVisible() {
      return false;
    }

    @Override
    protected void execInitField() throws ProcessingException {
      //Table already is scrollable, it's not necessary to make the form scrollable too
      IDeviceTransformationService service = SERVICES.getService(IDeviceTransformationService.class);
      if (service != null && service.getDeviceTransformer() != null) {
        service.getDeviceTransformer().getDeviceTransformationExcluder().excludeFieldTransformation(this, MobileDeviceTransformation.MAKE_MAINBOX_SCROLLABLE);
      }
    }

    @Order(10.0)
    public class OutlinesTableField extends AbstractTableField<OutlinesTableField.Table> {

      @Override
      protected boolean getConfiguredLabelVisible() {
        return false;
      }

      @Override
      protected int getConfiguredGridH() {
        return 2;
      }

      @Order(10.0)
      public class Table extends AbstractMobileTable {

        @Override
        protected boolean execIsAutoCreateTableRowForm() {
          return false;
        }

        @Override
        protected boolean getConfiguredAutoDiscardOnDelete() {
          return true;
        }

        @Override
        protected String getConfiguredDefaultIconId() {
          return AbstractIcons.TreeNode;
        }

        @Override
        protected boolean getConfiguredAutoResizeColumns() {
          return true;
        }

        @Override
        protected boolean getConfiguredSortEnabled() {
          return false;
        }

        public LabelColumn getLableColumn() {
          return getColumnSet().getColumnByClass(LabelColumn.class);
        }

        public OutlineColumn getOutlineColumn() {
          return getColumnSet().getColumnByClass(OutlineColumn.class);
        }

        @Override
        protected void execDecorateRow(ITableRow row) throws ProcessingException {
          final String outlineIcon = getOutlineColumn().getValue(row).getIconId();
          if (outlineIcon != null) {
            row.setIconId(outlineIcon);
          }
        }

        @Order(10.0)
        public class OutlineColumn extends AbstractColumn<IOutline> {

          @Override
          protected boolean getConfiguredDisplayable() {
            return false;
          }
        }

        @Order(20.0)
        public class LabelColumn extends AbstractStringColumn {

        }

        @Override
        protected void execRowsSelected(ITableRow[] rows) throws ProcessingException {
          if (rows == null || rows.length == 0) {
            return;
          }

          IOutline outline = getOutlineColumn().getValue(rows[0]);

          MobileDesktopUtility.activateOutline(outline);
          getDesktop().removeForm(HomeForm.this);

          clearSelectionDelayed();
        }
      }
    }

    @Order(20.0)
    public class LogoutButton extends AbstractButton {

      @Override
      protected String getConfiguredLabel() {
        return TEXTS.get("Logoff");
      }

      @Override
      protected void execClickAction() throws ProcessingException {
        ClientJob.getCurrentSession().stopSession();
      }

    }
  }

  @Order(10.0)
  public class ViewHandler extends AbstractFormHandler {

    @Override
    protected void execLoad() throws ProcessingException {
      final OutlinesTableField.Table table = getOutlinesTableField().getTable();

      IOutline[] outlines = getDesktop().getAvailableOutlines();
      for (IOutline outline : outlines) {
        if (outline.isVisible() && outline.getRootNode() != null) {
          ITableRow row = table.createRow(new Object[]{outline, outline.getTitle()});
          row.setEnabled(outline.isEnabled());
          table.addRow(row);
        }
      }
    }

    @Override
    protected void execFinally() throws ProcessingException {
      final OutlinesTableField.Table table = getOutlinesTableField().getTable();
      table.discardAllRows();
    }
  }
}
