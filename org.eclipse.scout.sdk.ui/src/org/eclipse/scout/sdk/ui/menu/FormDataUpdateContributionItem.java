package org.eclipse.scout.sdk.ui.menu;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.form.formdata.FormDataAutoUpdater;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class FormDataUpdateContributionItem extends ContributionItem {
  public static final String ID = "org.eclipse.scout.sdk.ui.menu.updateFormdataAutomatically";

  public FormDataUpdateContributionItem() {
  }

  public FormDataUpdateContributionItem(String id) {
    super(id);
  }

  @Override
  public void fill(Menu menu, int index) {
    MenuItem item = new MenuItem(menu, SWT.CHECK, index);
    item.setText(Texts.get("UpdateFormDataAutomatically"));
    boolean selected = ScoutSdkUi.getDefault().getPreferenceStore().getBoolean(FormDataAutoUpdater.PROP_FORMDATA_AUTO_UPDATE);
    item.setSelection(selected);
    item.addSelectionListener(new P_SelectionListener());
  }

  private class P_SelectionListener extends SelectionAdapter {
    @Override
    public void widgetSelected(SelectionEvent e) {
      ScoutSdkUi.getDefault().getPreferenceStore().setValue(FormDataAutoUpdater.PROP_FORMDATA_AUTO_UPDATE, ((MenuItem) e.widget).getSelection());
    }
  }
}
