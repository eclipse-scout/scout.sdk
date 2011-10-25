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
package org.eclipse.scout.nls.sdk.simple.model.ws.translationfile;

import java.io.InputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.nls.sdk.NlsCore;
import org.eclipse.scout.nls.sdk.model.util.Language;
import org.eclipse.scout.nls.sdk.model.workspace.translationResource.AbstractTranslationResource;

public class PlatformTranslationFile extends AbstractTranslationResource {

  private String m_fileName;

  public PlatformTranslationFile(InputStream is, Language language) {
    super(language);
    m_fileName = language.getDispalyName();
    try {
      parseResource(is);
    }
    catch (Exception e) {
      NlsCore.logError("could not parse translation file: " + getLanguage().getDispalyName());
    }
  }

  @Override
  public void reload(IProgressMonitor monitor) {
    // void here
  }

  @Override
  public boolean isReadOnly() {
    return true;
  }
}
