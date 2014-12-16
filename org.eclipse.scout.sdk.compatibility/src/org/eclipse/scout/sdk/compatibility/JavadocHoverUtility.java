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
package org.eclipse.scout.sdk.compatibility;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.scout.sdk.compatibility.internal.ScoutCompatibilityActivator;
import org.eclipse.scout.sdk.compatibility.internal.service.IJavadocHoverCompatService;

public final class JavadocHoverUtility {
  private JavadocHoverUtility() {
  }

  public static String getHtmlContent(IMember member) throws CoreException {
    IJavadocHoverCompatService svc = ScoutCompatibilityActivator.getDefault().acquireCompatibilityService(IJavadocHoverCompatService.class);
    if (svc != null) {
      return svc.getHtmlContent(member);
    }
    else {
      return null;
    }
  }

  public static String addImageAndLabel(IJavaElement member, String imageName, String label) {
    IJavadocHoverCompatService svc = ScoutCompatibilityActivator.getDefault().acquireCompatibilityService(IJavadocHoverCompatService.class);
    if (svc != null) {
      return svc.addImageAndLabel(member, imageName, label);
    }
    else {
      return null;
    }
  }
}
