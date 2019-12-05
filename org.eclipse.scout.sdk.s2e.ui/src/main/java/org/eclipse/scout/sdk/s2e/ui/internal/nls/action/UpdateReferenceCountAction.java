/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.nls.action;

import java.util.concurrent.CancellationException;
import java.util.function.Supplier;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.nls.query.TranslationKeysQuery;
import org.eclipse.scout.sdk.core.s.util.search.IFileQueryResult;
import org.eclipse.scout.sdk.s2e.ui.ISdkIcons;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.scout.sdk.s2e.ui.internal.nls.editor.NlsReferenceProvider;
import org.eclipse.scout.sdk.s2e.ui.internal.nls.editor.NlsTableController;
import org.eclipse.scout.sdk.s2e.util.EclipseWorkspaceWalker;
import org.eclipse.swt.widgets.Display;

/**
 * <h4>UpdateReferenceCountAction</h4>
 */
public class UpdateReferenceCountAction extends Action {
  private final NlsTableController m_controller;
  private final Display m_display;

  public UpdateReferenceCountAction(NlsTableController controller, Display display) {
    m_controller = controller;
    m_display = display;
    setText("Show Translation Key usage");
    setImageDescriptor(S2ESdkUiActivator.getImageDescriptor(ISdkIcons.Find));
  }

  @Override
  public void run() {
    setEnabled(false);

    EclipseWorkspaceWalker.executeQuery(new TranslationKeysQuery(m_controller.stack(), getText()))
        .whenComplete(this::handleEndSearch);
  }

  private void handleEndSearch(Supplier<IFileQueryResult> query, Throwable error) {
    m_display.asyncExec(() -> handleEndSearchInUi(query, error));
  }

  private void handleEndSearchInUi(Supplier<IFileQueryResult> supplier, Throwable error) {
    if (error == null) {
      TranslationKeysQuery query = (TranslationKeysQuery) supplier.get();
      m_controller.setReferenceProvider(new NlsReferenceProvider(query.resultByKey()));
    }
    else {
      if (error instanceof CancellationException || error instanceof OperationCanceledException) {
        SdkLog.debug("Reference count calculation cancelled.", error);
      }
      else {
        SdkLog.error("Error during reference count calculation.", error);
      }
    }
    setEnabled(true);
  }
}
