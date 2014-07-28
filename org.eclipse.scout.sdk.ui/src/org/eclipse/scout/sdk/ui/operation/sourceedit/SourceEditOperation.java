/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.operation.sourceedit;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

/**
 * <h3>{@link SourceEditOperation}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 28.10.2013
 */
public class SourceEditOperation implements IOperation {

  private TextEdit m_edit;
  private IDocument m_document;
  private Display m_display;

  public SourceEditOperation(TextEdit edit, IDocument document, Display display) {
    m_edit = edit;
    m_document = document;
    m_display = display;

  }

  @Override
  public String getOperationName() {
    return "Replace source...";
  }

  @Override
  public void validate() {
    if (getEdit() == null) {
      throw new IllegalArgumentException("Edit can not be null.");
    }
    if (getDocument() == null) {
      throw new IllegalArgumentException("Document can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    final Holder<CoreException> exceptionHolder = new Holder<CoreException>(CoreException.class);
    getDisplay().syncExec(new Runnable() {
      @Override
      public void run() {
        try {
          getEdit().apply(getDocument());
        }
        catch (MalformedTreeException e) {
          exceptionHolder.setValue(new CoreException(new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "Edit could not be applied", e)));
        }
        catch (BadLocationException e) {
          exceptionHolder.setValue(new CoreException(new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "Edit could not be applied", e)));
        }
      }
    });
    if (exceptionHolder.getValue() != null) {
      throw exceptionHolder.getValue();
    }
  }

  public TextEdit getEdit() {
    return m_edit;
  }

  public IDocument getDocument() {
    return m_document;
  }

  public Display getDisplay() {
    return m_display;
  }

}
