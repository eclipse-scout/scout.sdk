package org.eclipse.scout.sdk.compatibility.v38v42.internal.service;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.internal.ui.text.java.hover.JavadocHover;
import org.eclipse.jdt.internal.ui.text.javadoc.JavadocContentAccess2;
import org.eclipse.scout.sdk.compatibility.internal.service.IJavadocHoverCompatService;

@SuppressWarnings("restriction")
public class JavadocHoverCompatService implements IJavadocHoverCompatService {
  @Override
  public String addImageAndLabel(IJavaElement member, String imageName, String label) {
    StringBuffer buffer = new StringBuffer();
    JavadocHover.addImageAndLabel(buffer, member, imageName, 16, 16, label, 22, 0);
    return buffer.toString();
  }

  @Override
  public String getHtmlContent(IMember member) throws CoreException {
    return JavadocContentAccess2.getHTMLContent(member, true);
  }
}
