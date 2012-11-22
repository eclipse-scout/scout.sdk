package @@BUNDLE_SWT_NAME@@.views;

import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.window.desktop.view.AbstractScoutView;
import @@BUNDLE_SWT_NAME@@.Activator;

public class SearchView extends AbstractScoutView {

  public SearchView() {
  }

  @Override
  protected ISwtEnvironment getSwtEnvironment() {
    return Activator.getDefault().getEnvironment();
  }
}
