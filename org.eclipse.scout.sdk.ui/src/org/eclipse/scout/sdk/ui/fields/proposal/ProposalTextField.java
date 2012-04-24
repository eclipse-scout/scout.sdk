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
package org.eclipse.scout.sdk.ui.fields.proposal;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ui.fields.TextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalPopup.SearchPatternInput;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>ProposalTextField</h3> ...
 */
public class ProposalTextField extends TextField {

  public static int STYLE_DEFAULT = 1 << 0;
  public static int STYLE_INITIAL_SHOW_POPUP = 1 << 1;
  public static int STYLE_NO_LABEL = 1 << 10;

  private IProposalSelectionHandler m_selectionHandler;
  private Button m_popupButton;
  private ProposalPopup m_popup;
  private P_ProposalFieldListener m_proposalFieldListener;
  private Object m_selectedProposal = null;
  private Object m_lastFiredProposal = null;
  private EventListenerList m_eventListeners = new EventListenerList();
  private Lock m_updateLock = new Lock();
  private Lock m_focusLock = new Lock();
  private IProposalPopupListener m_popupListener = new P_PopupListener();
  private int m_style;
  private Object m_input;

  public ProposalTextField(Composite parent) {
    this(parent, STYLE_DEFAULT);
  }

  public ProposalTextField(Composite parent, int style) {
    super(parent);
    addDisposeListener(new DisposeListener() {
      @Override
      public void widgetDisposed(DisposeEvent e) {
        m_popup.dispose();
      }
    });
    m_style = style;
    init();
  }

  public void setInput(Object input) {
    m_input = input;
    SearchPatternInput searchPatternInput = new SearchPatternInput(input, getText());
    m_popup.setInput(searchPatternInput);
  }

  public Object getInput() {
    return m_input;
  }

  public void setContentProvider(IContentProvider provider) {
    if (!CompareUtility.equals(provider, m_popup.getContentProvider())) {
      m_popup.setContentProvider(provider);
      acceptProposal(null);
    }
  }

  public IContentProvider getContentProvider() {
    return m_popup.getContentProvider();
  }

  public void setLabelProvider(IBaseLabelProvider labelProvider) {
    m_popup.setLabelProvider(labelProvider);
  }

  public IBaseLabelProvider getLabelProvider() {
    return m_popup.getLabelProvider();
  }

  public void setSelectionHandler(IProposalSelectionHandler selectionHandler) {
    m_selectionHandler = selectionHandler;
  }

  public IProposalSelectionHandler getSelectionHandler() {
    return m_selectionHandler;
  }

  private void attachProposalListener(StyledText textComponent) {
    if (m_proposalFieldListener == null) {
      m_proposalFieldListener = new P_ProposalFieldListener();
      textComponent.addListener(SWT.KeyDown, m_proposalFieldListener);
      textComponent.addListener(SWT.KeyUp, m_proposalFieldListener);
      textComponent.addListener(SWT.Modify, m_proposalFieldListener);
      textComponent.addListener(SWT.FocusOut, m_proposalFieldListener);
      textComponent.addListener(SWT.FocusIn, m_proposalFieldListener);
      textComponent.addListener(SWT.Traverse, m_proposalFieldListener);
      textComponent.addListener(SWT.MouseUp, m_proposalFieldListener);
      textComponent.addListener(SWT.Verify, m_proposalFieldListener);
    }
  }

  private void init() {
    Label label = getLabelComponent();
    StyledText text = getTextComponent();
    FormData labelData = (FormData) label.getLayoutData();
    FormData textData = (FormData) text.getLayoutData();
    if ((m_style & STYLE_NO_LABEL) != 0) {
      label.setVisible(false);
      labelData.right = new FormAttachment(0, 0);
      textData.left = new FormAttachment(0, 0);
    }
  }

  @Override
  protected void createContent(Composite parent) {
    super.createContent(parent);
    Label label = getLabelComponent();
    StyledText text = getTextComponent();
    m_popupButton = new Button(parent, SWT.PUSH | SWT.FLAT);
    m_popupButton.setImage(ScoutSdkUi.getImage(ScoutSdkUi.ToolDropdown));
    m_popupButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        try {
          if (m_updateLock.aquire()) {
            if (m_popup.isVisible()) {
              closePopup();
            }
            else {
              getTextComponent().setSelection(0);
              getTextComponent().setFocus();
              updateProposals();
            }
          }
        }
        finally {
          m_updateLock.release();
        }
      }
    });
    parent.setTabList(new Control[]{text});

    // popup
    m_popup = new ProposalPopup(getTextComponent());
    m_popupListener = new P_PopupListener();
    m_popup.addPopupListener(m_popupListener);
    attachProposalListener(getTextComponent());

    // layout
    parent.setLayout(new FormLayout());
    FormData labelData = new FormData();
    labelData.top = new FormAttachment(0, 0);
    labelData.left = new FormAttachment(0, 0);
    labelData.right = new FormAttachment(40, 0);
    labelData.bottom = new FormAttachment(100, 0);
    label.setLayoutData(labelData);

    FormData textData = new FormData();
    textData.top = new FormAttachment(0, 0);
    textData.left = new FormAttachment(label, 5);
    textData.right = new FormAttachment(m_popupButton, -2);
    textData.bottom = new FormAttachment(100, 0);
    text.setLayoutData(textData);

    FormData buttonData = new FormData(SdkProperties.TOOL_BUTTON_SIZE, SdkProperties.TOOL_BUTTON_SIZE);
    buttonData.top = new FormAttachment(0, 0);
    buttonData.right = new FormAttachment(100, 0);
    buttonData.bottom = new FormAttachment(100, 0);
    m_popupButton.setLayoutData(buttonData);
  }

  public void addProposalAdapterListener(IProposalAdapterListener listener) {
    m_eventListeners.add(IProposalAdapterListener.class, listener);
  }

  public void removeProposalAdapterListener(IProposalAdapterListener listener) {
    m_eventListeners.remove(IProposalAdapterListener.class, listener);

  }

  @Override
  public int getStyle() {
    return m_style;
  }

  protected void notifyAcceptProposal(Object proposal) {
    ContentProposalEvent event = new ContentProposalEvent(this);
    event.proposal = proposal;
    for (IProposalAdapterListener l : m_eventListeners.getListeners(IProposalAdapterListener.class)) {
      l.proposalAccepted(event);
    }
    m_lastFiredProposal = proposal;
  }

  public synchronized void acceptProposal(Object proposal) {
    // update ui
    try {
      if (m_updateLock.aquire()) {
        String text;
        if (proposal != null) {
          text = m_popup.getText(proposal);
        }
        else {
          text = "";
        }
        if (getTextComponent() != null) {
          getTextComponent().setText(text);
          if (getLabelProvider() instanceof IContentProposalLabelProvider) {
            getTextComponent().setSelection(((IContentProposalLabelProvider) getLabelProvider()).getCursorPosition(proposal));
          }
          else {
            getTextComponent().setSelection(text.length());
          }
        }
      }
    }
    finally {
      m_updateLock.release();
    }
    if (!CompareUtility.equals(m_selectedProposal, proposal)) {
      m_selectedProposal = proposal;
      notifyAcceptProposal(m_selectedProposal);
    }
    closePopup();
  }

  private synchronized void acceptProposalInternal(Object proposal) {
    if (getSelectionHandler() != null) {
      getSelectionHandler().handleProposalAccepted(proposal, m_popup.getInput().getPattern(), this);
    }
    else if (proposal instanceof ISeparatorProposal) {
      // void
    }
    else {
      acceptProposal(proposal);
    }
  }

  public void setProposalDescriptionProvider(IProposalDescriptionProvider proposalDescriptionProvider) {
    m_popup.setProposalDescriptionProvider(proposalDescriptionProvider);
  }

  public IProposalDescriptionProvider getProposalDescriptionProvider() {
    return m_popup.getProposalDescriptionProvider();
  }

  @Override
  public void setText(String text) {
    if (m_updateLock.aquire()) {
      try {
        if (text == null) {
          text = "";
        }
        super.setText(text);
      }
      finally {
        m_updateLock.release();
      }
    }
  }

  @Override
  public void setEditable(boolean editable) {
    super.setEditable(editable);
    if (m_popupButton != null && !m_popupButton.isDisposed()) {
      m_popupButton.setEnabled(editable);
    }
  }

  @Override
  public boolean getEditable() {
    boolean editable = super.getEditable();
    if (m_popupButton != null && !m_popupButton.isDisposed()) {
      editable = editable && m_popupButton.getEnabled();
    }
    return editable;
  }

  @Override
  public boolean isEditable() {
    boolean editable = super.isEditable();
    if (m_popupButton != null && !m_popupButton.isDisposed()) {
      editable = editable && m_popupButton.getEnabled();
    }
    return editable;
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    if (m_popupButton != null && !m_popupButton.isDisposed()) {
      m_popupButton.setEnabled(enabled);
    }
  }

  @Override
  public boolean getEnabled() {
    boolean enabled = super.getEnabled();
    if (m_popupButton != null && !m_popupButton.isDisposed()) {
      enabled = enabled && m_popupButton.getEnabled();
    }
    return enabled;
  }

  @Override
  public boolean isEnabled() {
    boolean enabled = super.isEnabled();
    if (m_popupButton != null && !m_popupButton.isDisposed()) {
      enabled = enabled && m_popupButton.getEnabled();
    }
    return enabled;
  }

  // ########################
  // Popup Handling
  // ########################
  protected synchronized void closePopup() {
    if (m_popup.getShell() != null && !m_popup.getShell().isDisposed()) {
      m_popup.close();
    }
  }

  private synchronized void updateProposals() {
    String pattern = getText();
    int index = getSelection().x;
    if (index >= 0 && index < pattern.length()) {
      pattern = pattern.substring(0, index);
    }
    m_popup.updatePattern(pattern, getInput());
    if (!m_popup.isVisible()) {
      m_popup.open();
    }
  }

  private synchronized void textModified() {
    String text = getText();
    int cursorPosition = getSelection().x;
    if (cursorPosition > 0 && m_selectedProposal != null && m_popup.getText(m_selectedProposal).equals(text.substring(0, cursorPosition))) {
      return;
    }
    updateProposals();
  }

  private boolean isProposalFieldFocusOwner() {
    if (m_popup != null && m_popup.getShell() != null && !m_popup.getShell().isDisposed()) {
      if (m_popup.isFocusOwner()) {
        return true;
      }
      else {
        Shell[] shells = m_popup.getShell().getShells();
        if ((shells != null && shells.length > 0)) {
          return true;
        }
      }
    }
    if (getTextComponent() != null && !getTextComponent().isDisposed()) {
      if (getTextComponent().isFocusControl()) {
        return true;
      }
    }
    if (m_popupButton != null && !m_popupButton.isDisposed()) {
      return m_popupButton.isFocusControl();
    }
    return false;
  }

  public Object getSelectedProposal() {
    return m_selectedProposal;
  }

  private class P_ProposalFieldListener implements Listener {
    @Override
    public void handleEvent(Event event) {
      switch (event.type) {
        case SWT.Modify: {
          if (m_updateLock.aquire()) {
            try {
              // acceptProposal(null);
              textModified();
              // notifyTextModified();
            }
            finally {
              m_updateLock.release();
            }
          }
          break;
        }
        case SWT.Verify: {
          if ("\t".equals(event.text)) {
            event.doit = false;
          }
          break;
        }
        case SWT.KeyUp: {
          switch (event.keyCode) {
            case SWT.ESC:
              closePopup();
              event.doit = false;
              break;
            case SWT.ARROW_DOWN:
              if (!m_popup.isVisible()) {
                updateProposals();
              }
              else {
                m_popup.setFocus();
              }
              break;
            case SWT.ARROW_LEFT:
            case SWT.ARROW_RIGHT:
              textModified();
              break;

            default:
              break;
          }
          break;
        }
        case SWT.FocusOut: {
          getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
              if (m_popupButton != null && !m_popupButton.isDisposed()) {
                if (CompareUtility.equals(getDisplay().getFocusControl(), m_popupButton)) {
                  return;
                }
              }
              if (!isProposalFieldFocusOwner() && !isDisposed()) {
                String text = getText();
                String input = "";
                if (m_selectedProposal != null) {
                  input = m_popup.getText(m_selectedProposal);
                }
                if (!StringUtility.equalsIgnoreCase(text, input)) {
                  acceptProposalInternal(null);
                }
                // notifyAcceptProposal(m_selectedProposal);
                closePopup();
              }
            }
          });
          break;
        } // end FocusOut
        case SWT.FocusIn: {
          if ((m_style & STYLE_INITIAL_SHOW_POPUP) != 0) {
            updateProposals();
          }
          break;
        } // end FocusIn
        case SWT.MouseUp: {
          if (getText().length() > 0) textModified();
          break;
        } // end MouseUp
        case SWT.Traverse: {
          switch (event.keyCode) {
            case SWT.ESC:
              if (m_popup.isVisible()) {
                event.doit = false;
              }
              break;
            case SWT.CR:
              if (m_popup.isVisible()) {
                acceptProposalInternal(m_popup.getSelectedProposal());
                event.doit = false;
              }
              break;
            case SWT.TAB:
            case SWT.LF:
              if (m_popup.isVisible()) {
                event.doit = false;
                m_popup.setFocus();
              }
              break;
            default:
              break;
          }
          break;
        } // end Traverse
        default:
          break;
      }

    }
  } // end class P_ProposalFieldListener

  private class P_PopupListener implements IProposalPopupListener {
    @Override
    public void popupChanged(ProposalPopupEvent event) {
      switch (event.getType()) {
        case ProposalPopupEvent.TYPE_PROPOSAL_ACCEPTED:
          try {
            m_focusLock.aquire();
            acceptProposalInternal((Object) event.getData(ProposalPopupEvent.IDENTIFIER_SELECTED_PROPOSAL));
            // only move to the next field, if the current field is not the last (ticket 84'140).
            Control[] siblings = getParent().getChildren();
            if (siblings[siblings.length - 1] != ProposalTextField.this) {

              getTextComponent().traverse(SWT.TRAVERSE_TAB_NEXT);
            }
          }
          finally {
            m_focusLock.release();
          }
          break;
        case ProposalPopupEvent.TYPE_SEARCH_SHORTENED:
          updateProposals();
          break;
        case ProposalPopupEvent.TYPE_PROPOSAL_SELECTED:
          updateProposals();
          break;
        case ProposalPopupEvent.TYPE_POPUP_CLOSED:
          if (m_focusLock.aquire()) {
            try {
              if (!CompareUtility.equals(m_lastFiredProposal, m_selectedProposal)) {
                // if(m_selectedProposal != null){
                // notifyAcceptProposal(m_selectedProposal);
                // }
              }
//              m_popup.close();

            }
            finally {
              m_focusLock.release();
            }
          }
          break;
        default:
          break;
      }
    }
  } // end class P_PopupListener

}
