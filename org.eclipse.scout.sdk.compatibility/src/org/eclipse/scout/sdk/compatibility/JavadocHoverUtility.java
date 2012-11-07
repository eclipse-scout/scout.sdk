package org.eclipse.scout.sdk.compatibility;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.scout.sdk.compatibility.internal.ScoutCompatibilityActivator;
import org.eclipse.scout.sdk.compatibility.internal.service.IJavadocHoverCompatService;

public final class JavadocHoverUtility {
  private JavadocHoverUtility() {
  }

  public static String addImageAndLabel(IJavaElement member, String imageName, String label) {
    IJavadocHoverCompatService svc = ScoutCompatibilityActivator.getDefault().acquireCompatibilityService(IJavadocHoverCompatService.class);
    if(svc != null){
    return svc.addImageAndLabel(member, imageName, label);
    }else{
    	return null;

    }
  }
}
