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
package org.eclipse.scout.sdk.ws.jaxws.validator;

import org.eclipse.core.runtime.Path;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.PathNormalizer;

public final class UrlPatternValidator {

  private UrlPatternValidator() {
  }

  public static boolean validate(final String urlPattern, final String servletAlias, final IUrlPatternValidation validation) {
    if (!StringUtility.hasText(urlPattern) || new Path(urlPattern).makeRelative().isEmpty()) {
      validation.onEmpty();
      return false;
    }

    if (!urlPattern.matches("[\\w\\-/]*")) {
      validation.onIllegalCharacters();
      return false;
    }

    if (StringUtility.hasText(servletAlias)) {
      if (!urlPattern.startsWith(PathNormalizer.toServletAlias(servletAlias))) {
        validation.onNotStartingWithServletAlias();
        return false;
      }

      if (new Path(urlPattern).makeRelativeTo(new Path(PathNormalizer.toServletAlias(servletAlias))).isEmpty()) {
        validation.onEmpty();
        return false;
      }
    }

    if (!urlPattern.equals(PathNormalizer.toUrlPattern(urlPattern))) {
      validation.onWrongSeparators();
      return false;
    }

    return true;
  }
}
