package org.eclipse.scout.sdk.ui.workingset;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.ui.IPersistableElement;

public class ScoutBundleAdapterFactory implements IAdapterFactory {
  @Override
  public Object getAdapter(Object adaptableObject, Class adapterType) {
    if (IPersistableElement.class.equals(adapterType)) {
      if (adaptableObject instanceof IScoutBundle) {
        return new ScoutBundlePersistableElementAdapter((IScoutBundle) adaptableObject);
      }
    }
    return null;
  }

  @Override
  public Class[] getAdapterList() {
    return new Class[]{IPersistableElement.class};
  }
}
