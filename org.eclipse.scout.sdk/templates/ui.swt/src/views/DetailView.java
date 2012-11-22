package @@BUNDLE_SWT_NAME@@.views;

import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.window.desktop.view.AbstractScoutView;
import @@BUNDLE_SWT_NAME@@.Activator;

public class DetailView extends AbstractScoutView {

  public DetailView() {
  }

  @Override
  protected ISwtEnvironment getSwtEnvironment() {
    return Activator.getDefault().getEnvironment();
  }
}
