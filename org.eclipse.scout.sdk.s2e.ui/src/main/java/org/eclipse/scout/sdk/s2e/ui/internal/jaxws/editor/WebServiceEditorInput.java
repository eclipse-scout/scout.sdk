/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.internal.jaxws.editor;

import java.util.Objects;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.part.FileEditorInput;

/**
 * <h3>{@link WebServiceEditorInput}</h3>
 *
 * @since 5.2.0
 */
public class WebServiceEditorInput extends FileEditorInput {

  private final String m_pageIdToActivate;

  public WebServiceEditorInput(IFile file, String pageIdToActivate) {
    super(file);
    m_pageIdToActivate = pageIdToActivate;
  }

  public String getPageIdToActivate() {
    return m_pageIdToActivate;
  }

  @Override
  public int hashCode() {
    var prime = 31;
    var result = super.hashCode();
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
    var other = (WebServiceEditorInput) obj;
    return Objects.equals(m_pageIdToActivate, other.m_pageIdToActivate);
  }
}
