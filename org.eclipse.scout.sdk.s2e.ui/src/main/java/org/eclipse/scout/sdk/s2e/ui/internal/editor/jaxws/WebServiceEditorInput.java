/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.internal.editor.jaxws;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.part.FileEditorInput;

/**
 * <h3>{@link WebServiceEditorInput}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class WebServiceEditorInput extends FileEditorInput {

  private String m_pageIdToActivate;

  public WebServiceEditorInput(IFile file) {
    super(file);
  }

  public String getPageIdToActivate() {
    return m_pageIdToActivate;
  }

  public void setPageIdToActivate(String pageIdToActivate) {
    m_pageIdToActivate = pageIdToActivate;
  }

}
