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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((m_pageIdToActivate == null) ? 0 : m_pageIdToActivate.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    WebServiceEditorInput other = (WebServiceEditorInput) obj;
    if (m_pageIdToActivate == null) {
      if (other.m_pageIdToActivate != null) {
        return false;
      }
    }
    else if (!m_pageIdToActivate.equals(other.m_pageIdToActivate)) {
      return false;
    }
    return true;
  }
}
