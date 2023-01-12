/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.internal.template;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.internal.corext.fix.LinkedProposalModel;
import org.eclipse.jdt.internal.corext.fix.LinkedProposalPositionGroup;
import org.eclipse.jdt.internal.ui.javaeditor.EditorHighlightingSynchronizer;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedModeUI.ExitFlags;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.text.link.ProposalPosition;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;

/**
 * Does the setup of the linked mode from a {@link LinkedProposalModel}
 */
public final class LinkedAsyncProposalModelPresenter {

  private LinkedAsyncProposalModelPresenter() {
  }

  public static void enterLinkedMode(ITextViewer viewer, IEditorPart editor, boolean switchedEditor, LinkedProposalModel linkedProposalModel) throws BadLocationException {
    var document = viewer.getDocument();
    var linkedModeUiRef = new AtomicReference<LinkedModeUI>();

    // setup linked-mode model
    var model = createLinkedModeModel(linkedProposalModel, document, linkedModeUiRef);
    model.forceInstall();
    if (editor instanceof JavaEditor) {
      model.addLinkingListener(new EditorHighlightingSynchronizer((JavaEditor) editor));
    }

    // setup linked-mode UI
    var ui = createLinkedModeUi(viewer, switchedEditor, model, linkedProposalModel);
    linkedModeUiRef.compareAndSet(null, ui);
    ui.enter();

    var region = ui.getSelectedRegion();
    viewer.setSelectedRange(region.getOffset(), region.getLength());
    viewer.revealRange(region.getOffset(), region.getLength());
  }

  static LinkedModeUI createLinkedModeUi(ITextViewer viewer, boolean switchedEditor, LinkedModeModel model, LinkedProposalModel linkedProposalModel) throws BadLocationException {
    LinkedModeUI ui = new EditorLinkedModeUI(model, viewer);
    var endPosition = linkedProposalModel.getEndPosition();
    var offset = -1;
    if (endPosition != null) {
      offset = endPosition.getOffset();
    }
    //noinspection ConstantConditions
    if (offset != -1 && endPosition != null) {
      ui.setExitPosition(viewer, offset + endPosition.getLength(), 0, Integer.MAX_VALUE);
    }
    else if (!switchedEditor) {
      var cursorPosition = viewer.getSelectedRange().x;
      if (cursorPosition != 0) {
        ui.setExitPosition(viewer, cursorPosition, 0, Integer.MAX_VALUE);
      }
    }
    ui.setExitPolicy(new LinkedModeExitPolicy());
    return ui;
  }

  static LinkedModeModel createLinkedModeModel(LinkedProposalModel linkedProposalModel, IDocument document, AtomicReference<LinkedModeUI> linkedModeUiRef) throws BadLocationException {
    var iterator = linkedProposalModel.getPositionGroupIterator();
    var model = new LinkedModeModel();
    while (iterator.hasNext()) {
      var curr = iterator.next();
      var positions = curr.getPositions();
      if (positions.length > 0) {
        var group = createGroup(document, model, curr, linkedModeUiRef);
        model.addGroup(group);
      }
    }
    return model;
  }

  static LinkedPositionGroup createGroup(IDocument document, LinkedModeModel model, LinkedProposalPositionGroup propPositionGroup, AtomicReference<LinkedModeUI> linkedModeUiRef) throws BadLocationException {

    if (propPositionGroup instanceof ICompletionProposalProvider) {
      return createAsyncGroup(document, model, (ICompletionProposalProvider) propPositionGroup, linkedModeUiRef);
    }

    var group = new LinkedPositionGroup();
    var positions = propPositionGroup.getPositions();
    var linkedModeProposals = propPositionGroup.getProposals();
    if (linkedModeProposals.length <= 1) {
      for (var pos : positions) {
        if (pos.getOffset() != -1) {
          group.addPosition(new LinkedPosition(document, pos.getOffset(), pos.getLength(), pos.getSequenceRank()));
        }
      }
    }
    else {
      var proposalImpls = Arrays.stream(linkedModeProposals)
          .map(linkedModeProposal -> new LinkedPositionProposalImpl(linkedModeProposal, model))
          .toArray(LinkedPositionProposalImpl[]::new);
      for (var pos : positions) {
        if (pos.getOffset() != -1) {
          group.addPosition(new ProposalPosition(document, pos.getOffset(), pos.getLength(), pos.getSequenceRank(), proposalImpls));
        }
      }
    }
    return group;
  }

  static LinkedPositionGroup createAsyncGroup(IDocument document, LinkedModeModel model, ICompletionProposalProvider proposalProvider, AtomicReference<LinkedModeUI> linkedModeUiRef) throws BadLocationException {
    // lazy (async) provider
    var positions = proposalProvider.getPositions();
    var group = new LinkedPositionGroup();
    var display = Display.getCurrent();
    ILinkedAsyncProposalListener listener = new P_ProposalListener(display, linkedModeUiRef);

    for (var pos : positions) {
      if (pos.getOffset() != -1) {
        if (display != null) {
          proposalProvider.addListener(listener);
        }
        group.addPosition(new AsyncProposalPosition(document, pos.getOffset(), pos.getLength(), pos.getSequenceRank(), proposalProvider, model));
      }
    }
    return group;
  }

  protected static class P_ProposalListener implements ILinkedAsyncProposalListener {

    private final Display m_display;
    private final AtomicReference<LinkedModeUI> m_linkedModeUiRef;

    protected P_ProposalListener(Display display, AtomicReference<LinkedModeUI> linkedModeUiRef) {
      m_display = display;
      m_linkedModeUiRef = linkedModeUiRef;
    }

    @Override
    public void loaded() {
      m_display.asyncExec(this::triggerContentAssist);
    }

    private void triggerContentAssist() {
      var linkedModeUi = m_linkedModeUiRef.get();
      if (linkedModeUi == null) {
        return;
      }

      try {
        var m = LinkedModeUI.class.getDeclaredMethod("triggerContentAssist");
        m.setAccessible(true);
        m.invoke(linkedModeUi);
      }
      catch (Exception t) {
        SdkLog.warning(t);
      }
    }
  }

  private static final class LinkedModeExitPolicy implements LinkedModeUI.IExitPolicy {
    @Override
    public ExitFlags doExit(LinkedModeModel model, VerifyEvent event, int offset, int length) {
      if (event.character == '=') {
        return new ExitFlags(ILinkedModeListener.EXIT_ALL, true);
      }
      return null;
    }
  }
}
