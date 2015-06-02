package @@BUNDLE_RAP_NAME@@;

import @@BUNDLE_CLIENT_NAME@@.ClientSession;
import org.eclipse.scout.rt.ui.rap.mobile.AbstractMobileStandaloneRwtEnvironment;

public class MobileStandaloneRwtEnvironment extends AbstractMobileStandaloneRwtEnvironment {

  private static final long serialVersionUID = 1L;

  public MobileStandaloneRwtEnvironment() {
    super(Activator.getDefault().getBundle(), ClientSession.class);
  }
}
