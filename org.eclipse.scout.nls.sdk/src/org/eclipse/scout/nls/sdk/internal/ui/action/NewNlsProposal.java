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
package org.eclipse.scout.nls.sdk.internal.ui.action;

import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.util.Language;
import org.eclipse.scout.nls.sdk.model.workspace.NlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.nls.sdk.ui.action.NlsEntryNewAction;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.ReplaceEdit;

/**
 * <h3>{@link NewNlsProposal}</h3>
 * 
 * @author Andreas Hoegger
 * @since 3.10.0 23.10.2013
 */
// suppressWarnings till bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=420186 is fixed
@SuppressWarnings("restriction")
public class NewNlsProposal extends AbstractJavaCompletionProposal implements ICompletionProposal, ICompletionProposalExtension, ICompletionProposalExtension2, ICompletionProposalExtension3, ICompletionProposalExtension4 {
  private final Image m_image = NlsCore.getImage(NlsCore.ICON_TOOL_ADD);
  private String m_prefix;
  private int m_offset;
  private INlsEntry m_nlsEntry;
  private Shell m_shell;
  private INlsProject m_project;

  public NewNlsProposal(INlsProject project, Shell shell, String prefix, int offset) {
    m_project = project;
    m_shell = Display.getDefault().getActiveShell();
    m_prefix = prefix;
    m_offset = offset;

  }

  @Override
  public String getSortString() {
    return "2" + getDisplayString();
  }

  @Override
  public StyledString getStyledDisplayString() {
    return new StyledString(getDisplayString());
  }

  @Override
  public IInformationControlCreator getInformationControlCreator() {
    return null;
  }

  @Override
  public CharSequence getPrefixCompletionText(IDocument document, int completionOffset) {
    return null;
  }

  @Override
  public int getPrefixCompletionStart(IDocument document, int completionOffset) {
    return 0;
  }

  @Override
  public void selected(ITextViewer viewer, boolean smartToggle) {
  }

  @Override
  public void unselected(ITextViewer viewer) {
  }

  @Override
  public boolean validate(IDocument document, int offset, DocumentEvent event) {
    return true;
  }

  @Override
  public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
    apply(viewer.getDocument(), trigger, offset);
  }

  @Override
  public void apply(IDocument document, char trigger, int offset) {
    String searchText = null;
    try {
      searchText = document.get(m_offset - m_prefix.length(), m_prefix.length() + offset - m_offset);
    }
    catch (BadLocationException e1) {
      NlsCore.logWarning(e1);
    }
    String proposalFieldText = "";

    if (!StringUtility.isNullOrEmpty(searchText)) {
      proposalFieldText = searchText;
    }
    String key = m_project.generateNewKey(proposalFieldText);
    NlsEntry entry = new NlsEntry(key, m_project);
    Language devLang = m_project.getDevelopmentLanguage();
    entry.addTranslation(devLang, proposalFieldText);
    if (!Language.LANGUAGE_DEFAULT.equals(devLang)) {
      entry.addTranslation(Language.LANGUAGE_DEFAULT, proposalFieldText);
    }
    NlsEntryNewAction action = new NlsEntryNewAction(m_shell, m_project, entry, true);
    action.run();
    try {
      action.join();
    }
    catch (InterruptedException e) {
      NlsCore.logWarning(e);
    }

    m_nlsEntry = action.getEntry();
    if (m_nlsEntry != null) {
      int offDiff = offset - m_offset;
      ReplaceEdit replaceEdit = new ReplaceEdit(m_offset - m_prefix.length(), m_prefix.length() + offDiff, m_nlsEntry.getKey());
      try {
        replaceEdit.apply(document);
      }
      catch (Exception e) {
        NlsCore.logWarning(e);
      }
    }
  }

  @Override
  public boolean isValidFor(IDocument document, int offset) {
    return validate(document, offset, null);
  }

  @Override
  public char[] getTriggerCharacters() {
    return null;
  }

  @Override
  public int getContextInformationPosition() {
    return 0;
  }

  @Override
  public Point getSelection(IDocument document) {
    return new Point(m_offset - m_prefix.length() + m_nlsEntry.getKey().length(), 0);
  }

  @Override
  public String getAdditionalProposalInfo() {
    return null;
  }

  @Override
  public String getDisplayString() {

    return "New text...";
  }

  @Override
  public Image getImage() {
    return m_image;
  }

  @Override
  public IContextInformation getContextInformation() {
    return null;
  }

  @Override
  public boolean isAutoInsertable() {
    return false;
  }

}
