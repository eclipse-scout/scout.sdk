/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.fields.proposal;

import static java.util.function.Predicate.isEqual;

import java.util.Collection;
import java.util.Objects;

import javax.swing.event.EventListenerList;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.scout.sdk.s2e.ui.ISdkIcons;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.ProposalPopup.SearchPatternInput;
import org.eclipse.scout.sdk.s2e.ui.fields.text.TextField;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.scout.sdk.s2e.ui.util.NormalizedPattern;
import org.eclipse.scout.sdk.s2e.util.OptimisticLock;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

/**
 * <h3>ProposalTextField</h3> Use {@link #setContentProvider(IProposalContentProvider)} to define the proposal content
 * <br>
 * Use {@link #setLabelProvider(IBaseLabelProvider)} to define the strategy to map a proposal to a readable text<br>
 * Use {@link IProposalListener} to be notified about an accepted proposal.
 */
@SuppressWarnings({"findbugs:IS2_INCONSISTENT_SYNC", "FieldAccessedSynchronizedAndUnsynchronized"})
public class ProposalTextField extends TextField {

  /**
   * Type constant to show the popup initially without requiring a user interaction.
   */
  public static final int TYPE_INITIAL_SHOW_POPUP = 1 << 10;

  private Button m_popupButton;
  private ProposalPopup m_popup;
  private P_ProposalFieldListener m_proposalFieldListener;
  private Object m_selectedProposal;
  private Object m_input;

  private final EventListenerList m_eventListeners = new EventListenerList();
  private final OptimisticLock m_updateLock = new OptimisticLock();
  private final OptimisticLock m_focusLock = new OptimisticLock();

  /**
   * Creates a {@link ProposalTextField} with a label and no image.
   */
  public ProposalTextField(Composite parent) {
    this(parent, TYPE_LABEL);
  }

  /**
   * @param type
   *          One of {@link #TYPE_INITIAL_SHOW_POPUP}, {@link TextField#TYPE_LABEL}, {@link TextField#TYPE_HYPERLINK},
   *          {@link TextField#TYPE_IMAGE}
   */
  public ProposalTextField(Composite parent, int type) {
    this(parent, type, DEFAULT_LABEL_WIDTH);
  }

  /**
   * @param type
   *          One of {@link #TYPE_INITIAL_SHOW_POPUP}, {@link TextField#TYPE_LABEL}, {@link TextField#TYPE_HYPERLINK},
   *          {@link TextField#TYPE_IMAGE}
   * @param labelWidth
   *          The with of the label component. This includes the image if available.
   */
  public ProposalTextField(Composite parent, int type, int labelWidth) {
    super(parent, type, labelWidth);
  }

  /**
   * @param input
   *          The new input object
   */
  protected void setInput(Object input) {
    m_input = input;
    var searchPatternInput = new SearchPatternInput(input, getText());
    m_popup.setInput(searchPatternInput);
  }

  /**
   * @return The current input object
   */
  protected Object getInput() {
    return m_input;
  }

  /**
   * @param provider
   *          The content provider of this proposal.
   */
  public void setContentProvider(IProposalContentProvider provider) {
    if (!Objects.equals(provider, m_popup.getContentProvider())) {
      m_popup.setContentProvider(provider);
      acceptProposal(null);
    }
  }

  /**
   * @return The content provider of this field
   */
  public IContentProvider getContentProvider() {
    return m_popup.getContentProvider();
  }

  /**
   * Sets a new label provider to this field.
   */
  public void setLabelProvider(IBaseLabelProvider labelProvider) {
    m_popup.setLabelProvider(labelProvider);
  }

  /**
   * @return The current label provider of this field.
   */
  public IBaseLabelProvider getLabelProvider() {
    return m_popup.getLabelProvider();
  }

  protected void attachProposalListener(Widget textComponent) {
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

  @Override
  protected void createContent(Composite parent) {
    super.createContent(parent);
    addDisposeListener(e -> m_popup.dispose());

    m_popupButton = new Button(parent, SWT.PUSH | SWT.FLAT);
    m_popupButton.setImage(S2ESdkUiActivator.getImage(ISdkIcons.ToolDropdown));
    m_popupButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        try {
          if (m_updateLock.acquire()) {
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

    StyledText text = getTextComponent();
    parent.setTabList(new Control[]{text});

    // popup
    m_popup = new ProposalPopup(getTextComponent());
    m_popup.addPopupListener(new P_PopupListener());
    attachProposalListener(getTextComponent());

    // layout
    var textData = (FormData) text.getLayoutData();
    textData.right = new FormAttachment(m_popupButton, -2);

    var buttonData = new FormData(22, 22);
    buttonData.top = new FormAttachment(0, 0);
    buttonData.right = new FormAttachment(100, 0);
    m_popupButton.setLayoutData(buttonData);
  }

  /**
   * Adds a listener which is notified when a new proposal has been selected.
   */
  public void addProposalListener(IProposalListener listener) {
    m_eventListeners.add(IProposalListener.class, listener);
  }

  /**
   * Removes a listener
   */
  public void removeProposalAdapterListener(IProposalListener listener) {
    m_eventListeners.remove(IProposalListener.class, listener);
  }

  protected void notifyAcceptProposal(Object proposal) {
    for (var l : m_eventListeners.getListeners(IProposalListener.class)) {
      l.proposalAccepted(proposal);
    }
  }

  /**
   * Selects the given proposal even if it is not part of the items provided by the {@link IContentProvider}.
   *
   * @param proposal
   *          The new proposal
   */
  public void acceptProposal(Object proposal) {
    acceptProposal(proposal, false, true);
  }

  /**
   * Selects the given proposal.
   *
   * @param proposal
   *          The new proposal
   * @param onlyAcceptExistingProposals
   *          If {@code true} this field will only accept the proposal if it exists in the list provided by the
   *          {@link IContentProvider} of this field.
   * @param closeProposalPopup
   *          If {@code true} closes the popup if it is still open.
   */
  public synchronized void acceptProposal(Object proposal, boolean onlyAcceptExistingProposals, boolean closeProposalPopup) {
    // update ui
    if (proposal != null) {
      if (onlyAcceptExistingProposals && !isProposalPresent(proposal)) {
        proposal = null; // not part of the list -> reset
      }
      try {
        if (m_updateLock.acquire()) {
          String text;
          if (proposal != null) {
            text = m_popup.getText(proposal);
          }
          else {
            text = "";
          }
          var textComp = getTextComponent();
          if (textComp != null) {
            textComp.setText(text);
          }
        }
      }
      finally {
        m_updateLock.release();
      }
    }

    if (!Objects.equals(m_selectedProposal, proposal)) {
      m_selectedProposal = proposal;
      setInput(proposal);
      notifyAcceptProposal(m_selectedProposal);
    }

    if (closeProposalPopup) {
      closePopup();
    }
  }

  protected boolean isProposalPresent(Object proposal) {
    var contentProvider = m_popup.getContentProvider(); // the real provider, no async loading here
    var monitor = Job.getJobManager().createProgressGroup();
    Collection<Object> proposals;
    try {
      proposals = contentProvider.getProposals(NormalizedPattern.build(null), monitor);
    }
    finally {
      monitor.done();
    }

    if (proposals == null || proposals.isEmpty()) {
      return false;
    }

    return proposals.stream().anyMatch(isEqual(proposal));
  }

  private synchronized void acceptProposalInternal(Object proposal, boolean closeProposalPopup) {
    if (proposal instanceof ISeparatorProposal) {
      return;
    }
    acceptProposal(proposal, false, closeProposalPopup);
  }

  /**
   * Sets a new {@link IProposalDescriptionProvider} used to calculate detail information for a selected proposal. This
   * information will be displayed in the right half of the popup window. proposal.
   *
   * @param proposalDescriptionProvider
   *          The new provider
   */
  public void setProposalDescriptionProvider(IProposalDescriptionProvider proposalDescriptionProvider) {
    m_popup.setProposalDescriptionProvider(proposalDescriptionProvider);
  }

  /**
   * @return the current IProposalDescriptionProvider if any.
   */
  public IProposalDescriptionProvider getProposalDescriptionProvider() {
    return m_popup.getProposalDescriptionProvider();
  }

  @Override
  public void setText(String text) {
    try {
      if (m_updateLock.acquire()) {
        if (text == null) {
          text = "";
        }
        super.setText(text);
      }
    }
    finally {
      m_updateLock.release();
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
    var editable = super.getEditable();
    if (m_popupButton != null && !m_popupButton.isDisposed()) {
      editable = editable && m_popupButton.getEnabled();
    }
    return editable;
  }

  @Override
  public boolean isEditable() {
    var editable = super.isEditable();
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
    var enabled = super.getEnabled();
    if (m_popupButton != null && !m_popupButton.isDisposed()) {
      enabled = enabled && m_popupButton.getEnabled();
    }
    return enabled;
  }

  @Override
  public boolean isEnabled() {
    var enabled = super.isEnabled();
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
    var pattern = getText();
    var index = getSelection().x;
    if (index >= 0 && index < pattern.length()) {
      pattern = pattern.substring(0, index);
    }
    m_popup.updatePattern(pattern, getInput());
    if (!m_popup.isVisible()) {
      m_popup.open();
    }
  }

  protected boolean isProposalFieldFocusOwner() {
    if (m_popup != null && m_popup.getShell() != null && !m_popup.getShell().isDisposed()) {
      if (m_popup.isFocusOwner()) {
        return true;
      }
      var shells = m_popup.getShell().getShells();
      if ((shells != null && shells.length > 0)) {
        return true;
      }
    }
    if (getTextComponent() != null && !getTextComponent().isDisposed() && getTextComponent().isFocusControl()) {
      return true;
    }
    return m_popupButton != null && !m_popupButton.isDisposed() && m_popupButton.isFocusControl();
  }

  public synchronized Object getSelectedProposal() {
    return m_selectedProposal;
  }

  private class P_ProposalFieldListener implements Listener {

    private boolean equalsIgnoreCase(String a, String b) {
      return (a == null && b == null) || a != null && a.equalsIgnoreCase(b);
    }

    private synchronized void textModified() {
      updateProposals();
    }

    @Override
    public void handleEvent(Event event) {
      switch (event.type) {
        case SWT.Modify:
          try {
            if (m_updateLock.acquire()) {
              textModified();
            }
          }
          finally {
            m_updateLock.release();
          }
          break;
        case SWT.Verify:
          if ("\t".equals(event.text)) {
            event.doit = false;
          }
          break;
        case SWT.KeyDown:
          // do not use SWT.MOD1 here because in eclipse ctrl is used for code completion even on mac!
          if (event.keyCode == SWT.SPACE && (event.stateMask & SWT.CTRL) == SWT.CTRL) {
            if (!m_popup.isVisible()) {
              updateProposals();
            }
            else {
              m_popup.setFocus();
            }
          }
          else if (event.keyCode == SWT.HOME || event.keyCode == SWT.END) {
            textModified();
          }
          break;
        case SWT.KeyUp:
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
        case SWT.FocusOut:
          getDisplay().asyncExec(() -> {
            if (m_popupButton != null && !m_popupButton.isDisposed() && m_popupButton.equals(getDisplay().getFocusControl())) {
              return;
            }
            if (!isProposalFieldFocusOwner() && !isDisposed()) {
              var text = getText();
              var input = "";
              if (m_selectedProposal != null) {
                input = m_popup.getText(m_selectedProposal);
              }
              if (!equalsIgnoreCase(text, input)) {
                acceptProposalInternal(null, true);
              }
              closePopup();
            }
          });
          break;
        case SWT.FocusIn:
          if ((getType() & TYPE_INITIAL_SHOW_POPUP) != 0) {
            updateProposals();
          }
          break;
        case SWT.MouseUp:
          if (!getText().isEmpty()) {
            textModified();
          }
          break;
        case SWT.Traverse:
          switch (event.keyCode) {
            case SWT.ESC:
              if (m_popup.isVisible()) {
                event.doit = false;
              }
              break;
            case SWT.CR:
              if (m_popup.isVisible()) {
                acceptProposalInternal(m_popup.getSelectedProposal(), true);
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
        default:
          break;
      }
    }
  }

  private class P_PopupListener implements IProposalPopupListener {
    @Override
    public void popupChanged(ProposalPopupEvent event) {
      switch (event.getType()) {
        case ProposalPopupEvent.TYPE_PROPOSAL_ACCEPTED:
          try {
            m_focusLock.acquire();
            var moveFocusProp = (Boolean) event.getData(ProposalPopupEvent.IDENTIFIER_MOVE_FOCUS);
            var moveFocus = moveFocusProp != null && moveFocusProp;
            acceptProposalInternal(event.getData(ProposalPopupEvent.IDENTIFIER_SELECTED_PROPOSAL), moveFocus);
            if (moveFocus && !isDisposed()) {
              // only move to the next field, if the current field is not the last
              var siblings = getParent().getChildren();
              if (siblings[siblings.length - 1] != ProposalTextField.this) {
                getTextComponent().traverse(SWT.TRAVERSE_TAB_NEXT);
              }
            }
          }
          finally {
            m_focusLock.release();
          }
          break;
        case ProposalPopupEvent.TYPE_SEARCH_SHORTENED:
          updateProposals();
          break;
        default:
          break;
      }
    }
  }
}
