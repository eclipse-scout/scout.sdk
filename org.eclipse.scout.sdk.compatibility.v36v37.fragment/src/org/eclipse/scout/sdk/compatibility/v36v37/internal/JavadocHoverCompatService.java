package org.eclipse.scout.sdk.compatibility.v36v37.internal;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.text.java.hover.JavadocHover;
import org.eclipse.scout.sdk.compatibility.internal.service.IJavadocHoverCompatService;

@SuppressWarnings("restriction")
public class JavadocHoverCompatService implements IJavadocHoverCompatService {
  @Override
  public String addImageAndLabel(IJavaElement member, String imageName, String label) {
    StringBuffer buffer = new StringBuffer();
    JavadocHover.addImageAndLabel(buffer, imageName, 16, 16, label, 22, 0);
    return buffer.toString();
  }
}
