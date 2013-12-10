package org.eclipse.scout.sdk.internal.workspace;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class ResourcesAdapterFactory implements IAdapterFactory {

  @Override
  public Object getAdapter(Object adaptableObject, Class adapterType) {
    if (adaptableObject instanceof IResource) {
      IResource r = (IResource) adaptableObject;
      if (IScoutBundle.class.isAssignableFrom(adapterType)) {
        // convert IResource to IScoutBundle
        return ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundle(r.getProject());
      }
      else if (IJavaElement.class.isAssignableFrom(adapterType)) {
        // convert IResource to IJavaElement
        return JavaCore.create(r);
      }
    }
    return null;
  }

  @Override
  public Class[] getAdapterList() {
    return new Class[]{IScoutBundle.class, IJavaElement.class};
  }

}
