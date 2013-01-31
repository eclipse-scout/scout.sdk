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
package org.eclipse.scout.sdk.ws.jaxws.util;

import org.eclipse.core.runtime.Path;
import org.eclipse.scout.commons.StringUtility;

/**
 * Ensures leading and trailing file separators for the given file type.
 */
public class PathNormalizer {

  private PathNormalizer() {
  }

  public static String toUrlPattern(String urlPattern) {
    if (!StringUtility.hasText(urlPattern)) {
      return null;
    }
    return new Path(urlPattern).makeAbsolute().removeTrailingSeparator().toString();
  }

  public static String toUrlPattern(String alias, String urlPattern) {
    if (!StringUtility.hasText(alias) || !StringUtility.hasText(urlPattern)) {
      return null;
    }
    return PathNormalizer.toUrlPattern(new Path(alias).append(urlPattern).toString());
  }

  public static String toServletAlias(String servletAlias) {
    if (!StringUtility.hasText(servletAlias)) {
      return null;
    }
    return new Path(servletAlias).makeAbsolute().removeTrailingSeparator().toString();
  }

  public static String toWsdlPath(String wsdlPath) {
    if (!StringUtility.hasText(wsdlPath)) {
      return null;
    }
    return new Path(wsdlPath).makeRelative().removeTrailingSeparator().toString();
  }

  public static String toTargetNamespace(String targetNamespace) {
    if (!StringUtility.hasText(targetNamespace)) {
      return null;
    }
    return new Path(targetNamespace).addTrailingSeparator().toString();
  }

  public static String toJarPath(String jarPath) {
    if (!StringUtility.hasText(jarPath)) {
      return null;
    }
    return new Path(jarPath).makeRelative().removeTrailingSeparator().toString();
  }
}
