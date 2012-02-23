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
package org.eclipse.scout.nls.sdk.internal.ui.fields;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.scout.nls.sdk.internal.ui.AbstractTextComposition;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class TextProposalField extends AbstractTextComposition {

  private Label m_label;
  private Text m_text;
  private ContentProposalAdapter m_proposalAdapter;

  public TextProposalField(Composite parent, IContentProposalProvider provider) {
    this(parent, provider, null);
  }

  public TextProposalField(Composite parent, IContentProposalProvider provider, KeyStroke keyStroke) {
    super(parent, SWT.NONE);
    createContent(this, provider, keyStroke);
  }

  public void createContent(Composite parent, IContentProposalProvider provider, KeyStroke keyStroke) {
    m_label = new Label(parent, SWT.RIGHT);
    m_text = new Text(parent, SWT.BORDER);
    m_proposalAdapter = new ContentProposalAdapter(m_text, new TextContentAdapter(), provider, keyStroke, null);
    m_proposalAdapter.setProposalAcceptanceStyle(ProposalAdapter.PROPOSAL_REPLACE);

    // TODO make it layoutable from outside
    setLayout(new FormLayout());
    FormData labelData = new FormData();
    labelData.top = new FormAttachment(0, 0);
    labelData.left = new FormAttachment(0, 0);
    labelData.right = new FormAttachment(40, 0);
    labelData.bottom = new FormAttachment(100, 0);
    m_label.setLayoutData(labelData);

    FormData textData = new FormData();
    textData.top = new FormAttachment(0, 0);
    textData.left = new FormAttachment(m_label, 5);
    textData.right = new FormAttachment(100, 0);
    textData.bottom = new FormAttachment(100, 0);
    m_text.setLayoutData(textData);
  }

  @Override
  public Text getTextControl() {
    return m_text;
  }

  public void setProposalProvider(IContentProposalProvider provider) {
    m_proposalAdapter.setContentProposalProvider(provider);

  }

  public IContentProposalProvider getProposalProvider() {
    return m_proposalAdapter.getContentProposalProvider();
  }

  public void setLabelProvider(ILabelProvider provider) {
    m_proposalAdapter.setLabelProvider(provider);
  }

  public ILabelProvider getLabelProvider() {
    return m_proposalAdapter.getLabelProvider();
  }

  public void addContentProposalListener(IContentProposalListener listener) {
    m_proposalAdapter.addContentProposalListener(listener);
  }

  public void addModifyListener(ModifyListener listener) {
    m_text.addModifyListener(listener);
  }

  public void removeModifyListener(ModifyListener listener) {
    m_text.removeModifyListener(listener);
  }

  public void addTextFocusListener(FocusListener listener) {
    m_text.addFocusListener(listener);
  }

  public void removeTextFocusListener(FocusListener listener) {
    m_text.removeFocusListener(listener);
  }

  public void addTextVerifyListener(VerifyListener listener) {
    m_text.addVerifyListener(listener);
  }

  public void removeTextVerifyListener(VerifyListener listener) {
    m_text.removeVerifyListener(listener);
  }

  public void setLabelText(String text) {
    m_label.setText(text);
  }

  public String getLabelText() {
    return m_label.getText();
  }

  public void setText(String text) {
    m_text.setText(text);
  }

  public String getText() {
    return m_text.getText();
  }

}
