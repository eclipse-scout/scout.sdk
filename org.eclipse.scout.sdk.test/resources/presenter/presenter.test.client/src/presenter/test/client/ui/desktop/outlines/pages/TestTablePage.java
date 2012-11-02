package presenter.test.client.ui.desktop.outlines.pages;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.ISearchForm;

import presenter.test.client.ui.searchforms.TestSearchForm;

public class TestTablePage extends AbstractPageWithTable<TestTablePage.Table> {

  @Override
  protected Class<? extends ISearchForm> getConfiguredSearchForm() {
    return TestSearchForm.class;
  }

  @Order(10.0)
  public class Table extends AbstractTable {
  }
}

