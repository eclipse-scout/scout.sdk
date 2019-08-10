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
package org.eclipse.scout.sdk.s2e.ui.internal.template;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.internal.corext.fix.LinkedProposalModel;
import org.eclipse.jdt.internal.corext.fix.LinkedProposalPositionGroup;
import org.eclipse.jdt.internal.corext.fix.LinkedProposalPositionGroupCore.PositionInformation;
import org.eclipse.jdt.internal.ui.javaeditor.EditorHighlightingSynchronizer;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
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
    IDocument document = viewer.getDocument();
    AtomicReference<LinkedModeUI> linkedModeUiRef = new AtomicReference<>();

    // setup linked-mode model
    LinkedModeModel model = createLinkedModeModel(linkedProposalModel, document, linkedModeUiRef);
    model.forceInstall();
    if (editor instanceof JavaEditor) {
      model.addLinkingListener(new EditorHighlightingSynchronizer((JavaEditor) editor));
    }

    // setup linked-mode UI
    LinkedModeUI ui = createLinkedModeUi(viewer, switchedEditor, model, linkedProposalModel);
    linkedModeUiRef.compareAndSet(null, ui);
    ui.enter();

    IRegion region = ui.getSelectedRegion();
    viewer.setSelectedRange(region.getOffset(), region.getLength());
    viewer.revealRange(region.getOffset(), region.getLength());
  }

  static LinkedModeUI createLinkedModeUi(ITextViewer viewer, boolean switchedEditor, LinkedModeModel model, LinkedProposalModel linkedProposalModel) throws BadLocationException {
    LinkedModeUI ui = new EditorLinkedModeUI(model, viewer);
    PositionInformation endPosition = linkedProposalModel.getEndPosition();
    int offset = -1;
    if (endPosition != null) {
      offset = endPosition.getOffset();
    }
    if (offset != -1 && endPosition != null) {
      ui.setExitPosition(viewer, offset + endPosition.getLength(), 0, Integer.MAX_VALUE);
    }
    else if (!switchedEditor) {
      int cursorPosition = viewer.getSelectedRange().x;
      if (cursorPosition != 0) {
        ui.setExitPosition(viewer, cursorPosition, 0, Integer.MAX_VALUE);
      }
    }
    ui.setExitPolicy(new LinkedModeExitPolicy());
    return ui;
  }

  static LinkedModeModel createLinkedModeModel(LinkedProposalModel linkedProposalModel, IDocument document, AtomicReference<LinkedModeUI> linkedModeUiRef) throws BadLocationException {
    Iterator<LinkedProposalPositionGroup> iterator = linkedProposalModel.getPositionGroupIterator();
    LinkedModeModel model = new LinkedModeModel();
    while (iterator.hasNext()) {
      LinkedProposalPositionGroup curr = iterator.next();
      PositionInformation[] positions = curr.getPositions();
      if (positions.length > 0) {
        LinkedPositionGroup group = createGroup(document, model, curr, linkedModeUiRef);
        model.addGroup(group);
      }
    }
    return model;
  }

  static LinkedPositionGroup createGroup(IDocument document, LinkedModeModel model, LinkedProposalPositionGroup propPositionGroup, AtomicReference<LinkedModeUI> linkedModeUiRef) throws BadLocationException {

    if (propPositionGroup instanceof ICompletionProposalProvider) {
      return createAsyncGroup(document, model, (ICompletionProposalProvider) propPositionGroup, linkedModeUiRef);
    }

    LinkedPositionGroup group = new LinkedPositionGroup();
    PositionInformation[] positions = propPositionGroup.getPositions();
    LinkedProposalPositionGroup.Proposal[] linkedModeProposals = propPositionGroup.getProposals();
    if (linkedModeProposals.length <= 1) {
      for (PositionInformation pos : positions) {
        if (pos.getOffset() != -1) {
          group.addPosition(new LinkedPosition(document, pos.getOffset(), pos.getLength(), pos.getSequenceRank()));
        }
      }
    }
    else {
      LinkedPositionProposalImpl[] proposalImpls = new LinkedPositionProposalImpl[linkedModeProposals.length];
      for (int i = 0; i < linkedModeProposals.length; i++) {
        proposalImpls[i] = new LinkedPositionProposalImpl(linkedModeProposals[i], model);
      }

      for (PositionInformation pos : positions) {
        if (pos.getOffset() != -1) {
          group.addPosition(new ProposalPosition(document, pos.getOffset(), pos.getLength(), pos.getSequenceRank(), proposalImpls));
        }
      }
    }
    return group;
  }

  static LinkedPositionGroup createAsyncGroup(IDocument document, LinkedModeModel model, ICompletionProposalProvider proposalProvider, AtomicReference<LinkedModeUI> linkedModeUiRef) throws BadLocationException {
    // lazy (async) provider
    PositionInformation[] positions = proposalProvider.getPositions();
    LinkedPositionGroup group = new LinkedPositionGroup();
    Display display = Display.getCurrent();
    ILinkedAsyncProposalListener listener = new P_ProposalListener(display, linkedModeUiRef);

    for (PositionInformation pos : positions) {
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
      LinkedModeUI linkedModeUi = m_linkedModeUiRef.get();
      if (linkedModeUi == null) {
        return;
      }

      try {
        Method m = LinkedModeUI.class.getDeclaredMethod("triggerContentAssist");
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
