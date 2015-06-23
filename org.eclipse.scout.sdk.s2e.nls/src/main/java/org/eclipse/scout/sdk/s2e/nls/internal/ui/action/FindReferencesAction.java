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
package org.eclipse.scout.sdk.s2e.nls.internal.ui.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.sdk.s2e.nls.INlsIcons;
import org.eclipse.scout.sdk.s2e.nls.NlsCore;
import org.eclipse.scout.sdk.s2e.nls.internal.search.NlsKeySearchQuery;
import org.eclipse.scout.sdk.s2e.nls.project.INlsProject;
import org.eclipse.search.ui.NewSearchUI;

/** <h4>FindReferencesAction</h4> */
public class FindReferencesAction extends Action {

  private final INlsProject m_project;
  private final String m_key;

  public FindReferencesAction(INlsProject project, String key) {
    super("Find References to '" + key + "'");
    m_project = project;
    m_key = key;
  }

  @Override
  public void run() {
    NewSearchUI.runQueryInBackground(new NlsKeySearchQuery(getProject(), getKey()));
  }

  @Override
  public ImageDescriptor getImageDescriptor() {
    return NlsCore.getImageDescriptor(INlsIcons.FIND_OBJECT);
  }

  public String getKey() {
    return m_key;
  }

  public INlsProject getProject() {
    return m_project;
  }

}
