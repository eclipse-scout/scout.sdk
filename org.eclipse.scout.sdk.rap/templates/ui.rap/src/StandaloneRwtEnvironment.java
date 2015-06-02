package @@BUNDLE_RAP_NAME@@;

import @@BUNDLE_CLIENT_NAME@@.ClientSession;
import org.eclipse.scout.rt.ui.rap.AbstractStandaloneRwtEnvironment;

public class StandaloneRwtEnvironment extends AbstractStandaloneRwtEnvironment {

  private static final long serialVersionUID = 1L;

  public StandaloneRwtEnvironment() {
    super(Activator.getDefault().getBundle(), ClientSession.class);
  }
}
