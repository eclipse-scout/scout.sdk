/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.compatibility.v45.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.internal.ui.text.java.hover.JavadocHover;
import org.eclipse.jdt.internal.ui.text.javadoc.JavadocContentAccess2;
import org.eclipse.scout.sdk.compatibility.internal.service.IJavadocHoverCompatService;

/**
 * Implementation of {@link IJavadocHoverCompatService} for Eclipse Mars.
 */
@SuppressWarnings("restriction")
public class JavadocHoverCompatService implements IJavadocHoverCompatService {
  @Override
  public String addImageAndLabel(IJavaElement member, String imageName, String label) {
    return JavadocHover.getImageAndLabel(member, true, label);
  }

  @Override
  public String getHtmlContent(IMember member) throws CoreException {
    return JavadocContentAccess2.getHTMLContent(member, true);
  }
}
