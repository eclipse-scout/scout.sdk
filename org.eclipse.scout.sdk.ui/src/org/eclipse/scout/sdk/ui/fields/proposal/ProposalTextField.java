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

  public static final ISeparatorProposal SEPERATOR = new ISeparatorProposal() {
  };

  public static int STYLE_DEFAULT = 1 << 0;
  public static int STYLE_INITIAL_SHOW_POPUP = 1 << 1;
  public static int STYLE_SEARCH_EXPERT_MODE = 1 << 2;
  public static int STYLE_NO_LABEL = 1 << 10;
  // public static int TYPE_FIRE_ON_FOCUS_LOST=1 << 14;

  private IProposalSelectionHandler m_selectionHandler;

  private Button m_popupButton;
  private ProposalPopup m_popup;
  private P_ProposalFieldListener m_proposalFieldListener;
//  private P_ProposalLoaderJob m_currentLoader = null;
  private Object lockProposalAdpter = new Object();
  private Object m_selectedProposal = null;

  private Object m_focusGainedProposal = null;
  private Object m_lastFiredProposal = null;
  private EventListenerList m_eventListeners = new EventListenerList();
  private Lock m_updateLock = new Lock();
  private Lock m_focusLock = new Lock();
  private IProposalPopupListener m_popupListener = new P_PopupListener();
  private int m_style;
  private IProposalDescriptionProvider m_proposalDescriptionProvider;

  private Object m_input;

  public ProposalTextField(Composite parent) {
    this(parent, STYLE_DEFAULT);
  }

  public ProposalTextField(Composite parent, int style) {
    super(parent);
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

  private void detachProposalListener(StyledText textComponent) {

    if (m_proposalFieldListener != null) {
      textComponent.removeListener(SWT.KeyDown, m_proposalFieldListener);
      textComponent.removeListener(SWT.KeyUp, m_proposalFieldListener);
      textComponent.removeListener(SWT.Modify, m_proposalFieldListener);
      textComponent.removeListener(SWT.FocusOut, m_proposalFieldListener);
      textComponent.removeListener(SWT.FocusIn, m_proposalFieldListener);
      textComponent.removeListener(SWT.Traverse, m_proposalFieldListener);
      textComponent.removeListener(SWT.MouseUp, m_proposalFieldListener);
      textComponent.removeListener(SWT.Verify, m_proposalFieldListener);
      m_proposalFieldListener = null;
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
            if (m_popup.getShell() != null && !m_popup.getShell().isDisposed()) {
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

//    m_popup.getShell().addDisposeListener(new DisposeListener() {
//      @Override
//      public void widgetDisposed(DisposeEvent event) {
//        if (m_popup != null) {
//          m_popup.removePopupListener(m_popupListener);
//        }
//        m_popup = null;
//      }
//    });

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

//  protected P_RequestPattern getLastRequestPattern() {
//    return m_lastRequestPattern;
//  }

  @Override
  public int getStyle() {
    return m_style;
  }

//  protected void notifyAcceptProposalUpdateUi(Object proposal) {
//    try {
//      if (m_updateLock.aquire()) {
//        if (proposal != null) {
//          getTextComponent().setText(proposal.getLabel(false, m_searchExpertMode));
//          getTextComponent().setSelection(proposal.getCursorPosition(m_searchExpertMode, false));
//        }
//        else {
//          getTextComponent().setText("");
//          getTextComponent().setSelection(0);
//        }
//      }
//    }
//    finally {
//      m_updateLock.release();
//    }
//    m_selectedProposal = proposal;
//    notifyAcceptProposal(proposal);
//  }

  protected void notifyAcceptProposal(Object proposal) {
    ContentProposalEvent event = new ContentProposalEvent(this);
    event.proposal = proposal;
//    if (m_lastRequestPattern == null) {
//      event.text = "";
//      event.cursorPosition = 0;
//    }
//    else {
//      event.text = m_lastRequestPattern.getSearchText();
//      event.cursorPosition = m_lastRequestPattern.getCursorPosition();
//    }
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
//    SearchPatternInput searchPatternInput = new SearchPatternInput(getInput(), pattern);
    m_popup.updatePattern(pattern, getInput());
    if (!m_popup.isVisible()) {
//    m_popup.setSearchPattern(pattern);
      m_popup.open();
    }
//    P_RequestPattern searchPattern = new P_RequestPattern(getText(), index);
//    if (searchPattern.equals(m_lastRequestPattern)) {
//      showProposals(m_lastRequestPattern);
//      return;
//    }
//    if (m_currentLoader != null) {
//      m_currentLoader.cancel();
//    }
//    if (m_popup != null) {
//      m_popup.setLoading(true);
//    }
//    m_currentLoader = new P_ProposalLoaderJob(searchPattern);
//    m_currentLoader.schedule();
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

//  private synchronized void showProposals(P_RequestPattern searchPattern) {
//    m_lastRequestPattern = searchPattern;
//    if (m_lastRequestPattern.getProposals().length == 0) {
//      if (m_popup != null) {
//        closePopup();
//      }
//    }
//    else {
//      if (m_popup == null) {
//        m_popup.open();
//      }
//      m_popup.setProposals(m_lastRequestPattern.getProposals());
//    }
//  }

  private boolean equalProposals(Object prop1, Object prop2) {
    if (prop1 == null) {
      return prop2 == null;
    }
    else {
      return prop1.equals(prop2);
    }
  }

  public Object getSelectedProposal() {
    return m_selectedProposal;
  }

//  private class P_ProposalLoaderJob extends Job {
//
//    private final P_RequestPattern m_requestPattern;
//    private IProgressMonitor m_monitor;
//
//    public P_ProposalLoaderJob(P_RequestPattern requestPattern) {
//      super("Load proposals");
//      m_requestPattern = requestPattern;
//    }
//
//    @Override
//    protected IStatus run(IProgressMonitor monitor) {
//      m_monitor = monitor;
//      Object[] proposals;
//      synchronized (lockProposalAdpter) {
//        if (getContentProvider() != null) {
//          proposals = getContentProvider().getProposals(m_requestPattern.getSearchText(),
//              m_requestPattern.getCursorPosition(), monitor);
//        }
//        else {
//          proposals = new Object[0];
//        }
//      }
//      if (m_monitor.isCanceled()) {
//        return Status.OK_STATUS;
//      }
//      m_requestPattern.setProposals(proposals);
//      getDisplay().syncExec(new Runnable() {
//        @Override
//        public void run() {
//          if (m_monitor.isCanceled()) {
//            return;
//          }
//          showProposals(m_requestPattern);
//        }
//      });
//      return Status.OK_STATUS;
//    }
//
//    void setCanceled() {
//      m_monitor.setCanceled(true);
//    }
//
//  } // end P_ProposalLoaderJob

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
          // if(m_popup == null){
          // String text=getText();
          // String input="";
          // if(m_selectedProposal != null){
          // input=m_selectedProposal.getLabel(false, m_searchExpertMode);
          // }
          // if(!StringUtility.equalsIgnoreCase(text, input)){
          // acceptProposal(null);
          // }
          // }
          // else{
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
              // if(m_popup != null && !m_popup.isFocusOwner()){
              // }
              // else{
              // String text=getText();
              // String input="";
              // if(m_selectedProposal != null){
              // input=m_selectedProposal.getLabel(false, m_searchExpertMode);
              // }
              // if(!StringUtility.equalsIgnoreCase(text, input)){
              // acceptProposal(null);
              // }
              // // notifyAcceptProposal(m_selectedProposal);
              // }
            }

          });
          // }

          // if((m_type & TYPE_FIRE_ON_FOCUS_LOST) != 0){
          //
          // if(m_popup == null){
          // // focus lost forever
          // if(!equalProposals(m_focusGainedProposal, m_selectedProposal)){
          // notifyAcceptProposal(m_selectedProposal);
          // }
          // }
          // else{
          // // check if next focus owner is the popup
          // getDisplay().asyncExec(new Runnable(){
          // public void run(){
          // if(m_popup != null && !m_popup.isFocusOwner()){
          // if(!equalProposals(m_focusGainedProposal, m_selectedProposal)){
          // notifyAcceptProposal(m_selectedProposal);
          // }
          // m_popup.close();
          // }
          // }
          // });
          // }
          // }
          // else{
          // getDisplay().asyncExec(new Runnable(){
          // public void run(){
          // if(m_popup != null && !m_popup.isFocusOwner()){
          // m_popup.close();
          // }
          // }
          // });
          // }
          break;
        } // end FocusOut
        case SWT.FocusIn: {
          if (m_focusLock.aquire()) {
            try {
              m_focusGainedProposal = m_selectedProposal;
            }
            finally {
              m_focusLock.release();
            }
          }
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
              if (m_popup != null) {
                event.doit = false;
              }
              break;
            case SWT.CR:
              if (m_popup != null) {

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
      System.out.println("handle popup event " + event.getType());

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
//          m_lastRequestPattern = null;
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
