/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.swt.view.proposal;

import org.eclipse.core.runtime.IPath;
import org.eclipse.scout.sdk.ui.fields.proposal.SimpleProposal;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;

public class PathProposal extends SimpleProposal {
  private static final String DATA_PATH = "dataPath";

  public PathProposal(IPath path) {
    super(path.toString(), ScoutSdkUi.getImage(ScoutSdkUi.Package));
    setData(DATA_PATH, path);
  }

  public IPath getPath() {
    return (IPath) getData(DATA_PATH);
  }
}
