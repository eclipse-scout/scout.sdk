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
package org.eclipse.scout.nls.sdk.internal.model.workspace.manifest;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.osgi.framework.Bundle;

public class PlatformManifestReader extends AbstractManifest {

  private Bundle m_bundle;

  public PlatformManifestReader(Bundle bundle) throws IOException {
    super(bundle.getSymbolicName(), bundle.getEntry("META-INF/MANIFEST.MF").openStream());
    m_bundle = bundle;
  }

  public Bundle getBundle() {
    return m_bundle;
  }

  @Override
  public boolean isWriteable() {
    return false;
  }

  @Override
  public IStatus store(IProgressMonitor monitor) {
    throw new UnsupportedOperationException("is read only");
  }

}
