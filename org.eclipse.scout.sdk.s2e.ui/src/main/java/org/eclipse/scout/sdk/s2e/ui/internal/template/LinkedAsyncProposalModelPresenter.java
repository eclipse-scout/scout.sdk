/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.internal.template;

import java.lang.reflect.Method;
import java.util.Iterator;

import org.eclipse.jdt.internal.corext.fix.LinkedProposalModel;
import org.eclipse.jdt.internal.corext.fix.LinkedProposalPositionGroup;
import org.eclipse.jdt.internal.corext.fix.LinkedProposalPositionGroup.PositionInformation;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.EditorHighlightingSynchronizer;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedModeUI.ExitFlags;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.text.link.ProposalPosition;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;

/**
 * Does the setup of the linked mode from a {@link LinkedProposalModel}
 */
public class LinkedAsyncProposalModelPresenter {

  public void enterLinkedMode(ITextViewer viewer, IEditorPart editor, boolean switchedEditor, LinkedProposalModel linkedProposalModel) throws BadLocationException {
    IDocument document = viewer.getDocument();

    LinkedModeModel model = new LinkedModeModel();

    final LinkedModeUI[] holder = new LinkedModeUI[1];
    final Display display = Display.getCurrent();
    ILinkedAsyncProposalListener listener = new ILinkedAsyncProposalListener() {
      @Override
      public void loaded() {
        display.asyncExec(new Runnable() {
          @Override
          public void run() {
            try {
              LinkedModeUI ui = holder[0];
              if (ui == null) {
                return;
              }
              final Method m = LinkedModeUI.class.getDeclaredMethod("triggerContentAssist");
              m.setAccessible(true);
              m.invoke(ui);
            }
            catch (Throwable t) {
              SdkLog.debug(t);
            }
          }
        });
      }
    };
    Iterator<LinkedProposalPositionGroup> iterator = linkedProposalModel.getPositionGroupIterator();
    while (iterator.hasNext()) {
      LinkedProposalPositionGroup curr = iterator.next();

      LinkedPositionGroup group = new LinkedPositionGroup();
      LinkedProposalPositionGroup.PositionInformation[] positions = curr.getPositions();
      if (positions.length > 0) {
        if (curr instanceof ICompletionProposalProvider) {
          // lazy provider
          for (int i = 0; i < positions.length; i++) {
            LinkedProposalPositionGroup.PositionInformation pos = positions[i];
            if (pos.getOffset() != -1) {
              ICompletionProposalProvider proposalProvider = (ICompletionProposalProvider) curr;
              if (display != null) {
                proposalProvider.addListener(listener);
              }
              group.addPosition(new AsyncProposalPosition(document, pos.getOffset(), pos.getLength(), pos.getSequenceRank(), proposalProvider, model));
            }
          }
        }
        else {
          LinkedProposalPositionGroup.Proposal[] linkedModeProposals = curr.getProposals();
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
        }
        model.addGroup(group);
      }
    }

    model.forceInstall();

    if (editor instanceof JavaEditor) {
      model.addLinkingListener(new EditorHighlightingSynchronizer((JavaEditor) editor));
    }

    final LinkedModeUI ui = new EditorLinkedModeUI(model, viewer);
    holder[0] = ui;
    LinkedProposalPositionGroup.PositionInformation endPosition = linkedProposalModel.getEndPosition();
    if (endPosition != null && endPosition.getOffset() != -1) {
      ui.setExitPosition(viewer, endPosition.getOffset() + endPosition.getLength(), 0, Integer.MAX_VALUE);
    }
    else if (!switchedEditor) {
      int cursorPosition = viewer.getSelectedRange().x;
      if (cursorPosition != 0) {
        ui.setExitPosition(viewer, cursorPosition, 0, Integer.MAX_VALUE);
      }
    }
    ui.setExitPolicy(new LinkedModeExitPolicy());
    ui.enter();

    IRegion region = ui.getSelectedRegion();
    viewer.setSelectedRange(region.getOffset(), region.getLength());
    viewer.revealRange(region.getOffset(), region.getLength());
  }

  static class LinkedPositionProposalImpl implements ICompletionProposalExtension2, IJavaCompletionProposal {

    private final LinkedProposalPositionGroup.Proposal m_proposal;
    private final LinkedModeModel m_linkedPositionModel;

    LinkedPositionProposalImpl(LinkedProposalPositionGroup.Proposal proposal, LinkedModeModel model) {
      m_proposal = proposal;
      m_linkedPositionModel = model;
    }

    @Override
    public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
      IDocument doc = viewer.getDocument();
      LinkedPosition position = m_linkedPositionModel.findPosition(new LinkedPosition(doc, offset, 0));
      if (position == null) {
        return;
      }

      try {
        TextEdit edit = m_proposal.computeEdits(offset, position, trigger, stateMask, m_linkedPositionModel);
        if (edit != null) {
          edit.apply(position.getDocument(), 0);
        }
      }
      catch (Exception e) {
        SdkLog.info("Unable to apply text edit.", e);
      }
    }

    @Override
    public String getDisplayString() {
      return m_proposal.getDisplayString();
    }

    @Override
    public Image getImage() {
      return m_proposal.getImage();
    }

    @Override
    public int getRelevance() {
      return m_proposal.getRelevance();
    }

    @Override
    public void apply(IDocument document) {
      // not called
    }

    @Override
    public String getAdditionalProposalInfo() {
      return m_proposal.getAdditionalProposalInfo();
    }

    @Override
    public Point getSelection(IDocument document) {
      return null;
    }

    @Override
    public IContextInformation getContextInformation() {
      return null;
    }

    @Override
    public void selected(ITextViewer viewer, boolean smartToggle) {
    }

    @Override
    public void unselected(ITextViewer viewer) {
    }

    @Override
    public boolean validate(IDocument document, int offset, DocumentEvent event) {
      // ignore event
      String insert = getDisplayString();

      int off;
      LinkedPosition pos = m_linkedPositionModel.findPosition(new LinkedPosition(document, offset, 0));
      if (pos != null) {
        off = pos.getOffset();
      }
      else {
        off = Math.max(0, offset - insert.length());
      }
      int length = offset - off;

      if (offset <= document.getLength()) {
        try {
          String content = document.get(off, length);
          if (insert.startsWith(content)) {
            return true;
          }
        }
        catch (BadLocationException e) {
          JavaPlugin.log(e);
          // and ignore and return false
        }
      }
      return false;
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
