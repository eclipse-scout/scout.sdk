package @@BUNDLE_RAP_NAME@@;

import @@BUNDLE_CLIENT_NAME@@.ClientSession;
import org.eclipse.scout.rt.ui.rap.mobile.AbstractTabletStandaloneRwtEnvironment;

public class TabletStandaloneRwtEnvironment extends AbstractTabletStandaloneRwtEnvironment {

  public TabletStandaloneRwtEnvironment() {
    super(Activator.getDefault().getBundle(), ClientSession.class);
  }
}
