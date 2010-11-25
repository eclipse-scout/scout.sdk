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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class ProposalPopup extends Window {

  private static final int POPUP_OFFSET = 2;
  /*
   * The character height hint for the popup. May be overridden by using
   * setInitialPopupSize.
   */
  private static final int POPUP_CHAR_HEIGHT = 10;

  /*
   * The minimum pixel width for the popup. May be overridden by using
   * setInitialPopupSize.
   */
  private static final int POPUP_MINIMUM_WIDTH = 300;

  private TableViewer m_tableViewer;
  private Label m_itemCountLabel;
  private Button m_showAllProposals;
  private Control m_proposalField;
  private Point popupSize;
  private IContentProposalEx[] m_proposals;
  private boolean m_ignoreClose;
  private List<IProposalPopupListener> m_selectionListeners = new ArrayList<IProposalPopupListener>();
  // private int m_selectionIndex=-1;
  private boolean m_expertModusChecked;
  private final boolean m_supportsEpertModus;

  public ProposalPopup(Control proposalField, boolean supportsExpertModus, boolean expertModusChecked) {
    super(proposalField.getShell());
    m_proposalField = proposalField;
    m_supportsEpertModus = supportsExpertModus;
    m_expertModusChecked = expertModusChecked;
    setShellStyle(SWT.RESIZE | SWT.ON_TOP);
    setBlockOnOpen(false);
  }

  @Override
  protected void configureShell(Shell shell) {
    super.configureShell(shell);
    shell.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
  }

  @Override
  protected Control createContents(Composite parent) {
    parent.setLayout(new FillLayout());
    Composite rootArea = new Composite(parent, SWT.INHERIT_FORCE);
    rootArea.setBackground(rootArea.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    Table table = new Table(rootArea, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
    m_tableViewer = new TableViewer(table);
    m_tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        if (!selection.isEmpty()) {
          // selectProposal((int)selection.getFirstElement());
          ProposalPopupEvent delegateEvent = new ProposalPopupEvent(ProposalPopupEvent.TYPE_PROPOSAL_SELECTED);
          delegateEvent.setData(ProposalPopupEvent.IDENTIFIER_SELECTED_PROPOSAL, selection.getFirstElement());
          firePopupEvent(delegateEvent);
        }
      }
    });

    m_tableViewer.addDoubleClickListener(new IDoubleClickListener() {
      @Override
      public void doubleClick(DoubleClickEvent event) {
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        if (!selection.isEmpty()) {
          // selectProposal(table.getSelectionIndex());
          ProposalPopupEvent delegateEvent = new ProposalPopupEvent(ProposalPopupEvent.TYPE_PROPOSAL_ACCEPTED);
          delegateEvent.setData(ProposalPopupEvent.IDENTIFIER_SELECTED_PROPOSAL, selection.getFirstElement());
          firePopupEvent(delegateEvent);
        }
      }
    });
    table.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        switch (e.keyCode) {
          case SWT.CR:
            break;
          case ' ':
            IStructuredSelection selection = (IStructuredSelection) m_tableViewer.getSelection();
            if (!selection.isEmpty()) {
              ProposalPopupEvent delegateEvent = new ProposalPopupEvent(ProposalPopupEvent.TYPE_PROPOSAL_ACCEPTED);
              delegateEvent.setData(ProposalPopupEvent.IDENTIFIER_SELECTED_PROPOSAL, selection.getFirstElement());
              firePopupEvent(delegateEvent);
            }
            break;

          default:
            break;
        }
        // TODO Auto-generated method stub
        super.keyReleased(e);
      }
    });
    table.setHeaderVisible(false);

    // m_proposalTable.addKeyListener(new KeyAdapter(){
    // @Override
    // public void keyPressed(KeyEvent e){
    // if(e.character == ' '){
    // ProposalPopupEvent event=new ProposalPopupEvent(ProposalPopupEvent.TYPE_PROPOSAL_SELECTED);
    // event.setData(ProposalPopupEvent.IDENTIFIER_SELECTED_PROPOSAL, m_proposals[m_proposalTable.getSelectionIndex()]);
    // firePopupEvent(event);
    // }
    // }
    // });
    //
    // m_proposalTable.addSelectionListener(new SelectionAdapter(){
    // @Override
    // public void widgetSelected(SelectionEvent e){
    // selectProposal(m_proposalTable.getSelectionIndex());
    // ProposalPopupEvent event=new ProposalPopupEvent(ProposalPopupEvent.TYPE_PROPOSAL_SELECTED);
    // int index=m_proposalTable.indexOf((TableItem)e.item);
    // if(index > 0 && index < m_proposals.length){
    // event.setData(ProposalPopupEvent.IDENTIFIER_SELECTED_PROPOSAL, m_proposals[index]);
    // }
    // firePopupEvent(event);
    // }
    //
    // @Override
    // public void widgetDefaultSelected(SelectionEvent e){
    // ProposalPopupEvent event=new ProposalPopupEvent(ProposalPopupEvent.TYPE_PROPOSAL_ACCEPTED);
    // int index=m_proposalTable.indexOf((TableItem)e.item);
    // if(index > 0 && index < m_proposals.length){
    // event.setData(ProposalPopupEvent.IDENTIFIER_SELECTED_PROPOSAL, m_proposals[index]);
    // }
    // firePopupEvent(event);
    // }
    // });
    Group group = new Group(rootArea, SWT.SHADOW_ETCHED_OUT);
    group.setBackground(group.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    group.setForeground(group.getDisplay().getSystemColor(SWT.COLOR_BLUE));
    m_itemCountLabel = new Label(group, SWT.NONE);
    m_itemCountLabel.setBackground(m_itemCountLabel.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    m_showAllProposals = new Button(group, SWT.CHECK);
    m_showAllProposals.setBackground(m_showAllProposals.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    m_showAllProposals.setText(Texts.get("ProposalField_checkBox_showAllProposals"));
    m_showAllProposals.setEnabled(m_supportsEpertModus);
    m_showAllProposals.setSelection(m_expertModusChecked);

    m_showAllProposals.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        ProposalPopupEvent event = new ProposalPopupEvent(ProposalPopupEvent.TYPE_SEARCH_SHORTENED);
        event.setData(ProposalPopupEvent.IDENTIFIER_SELECTION_SEARCH_SHORTENED, new Boolean(m_showAllProposals.getSelection()));
        firePopupEvent(event);
      }
    });
    // layout
    rootArea.setLayout(new FormLayout());
    FormData data = new FormData();
    data.top = new FormAttachment(0, 0);
    data.left = new FormAttachment(0, 0);
    data.right = new FormAttachment(100, 0);
    data.bottom = new FormAttachment(group, -2);
    table.setLayoutData(data);
    data = new FormData();
    data.left = new FormAttachment(0, 2);
    data.right = new FormAttachment(100, -2);
    data.bottom = new FormAttachment(100, -2);
    group.setLayoutData(data);

    GridLayout gridLayout = new GridLayout(2, false);
    gridLayout.marginHeight = 0;
    group.setLayout(gridLayout);
    GridData gd = new GridData(GridData.BEGINNING | GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
    m_itemCountLabel.setLayoutData(gd);
    gd = new GridData(GridData.END);
    m_showAllProposals.setLayoutData(gd);
    return rootArea;
  }

  public void setProposals(IContentProposalEx[] proposals) {

    // m_selectionIndex=-1;
    if (proposals == null) {
      proposals = new IContentProposalEx[0];
    }
    m_proposals = proposals;
    try {
      m_tableViewer.getTable().setRedraw(false);
      m_tableViewer.getTable().setItemCount(proposals.length);
      TableItem[] items = m_tableViewer.getTable().getItems();
      for (int i = 0; i < items.length; i++) {
        TableItem item = items[i];

        IContentProposalEx proposal = proposals[i];
        item.setText(getText(proposal, false));
        item.setImage(getImage(proposal, false));
        item.setData(proposal);
      }
    }
    finally {
      m_tableViewer.getTable().setRedraw(true);

    }
    // Default to the first selection if there is content.
    if (proposals.length > 0) {
      m_tableViewer.getTable().select(0);
    }
    // if(proposals.length > 0){
    // selectProposal(0);
    // }
    m_itemCountLabel.setText(proposals.length + " items found");
  }

  @Override
  public int open() {

    Shell shell = getShell();
    if (shell == null || shell.isDisposed()) {
      shell = null;
      // create the window
      create();
      shell = getShell();
    }

    // provide a hook for adjusting the bounds. This is only
    // necessary when there is content driven sizing that must be
    // adjusted each time the dialog is opened.
    adjustBounds();

    // limit the shell size to the display size
    constrainShellSize();

    shell.setVisible(true);
    return OK;
  }

  public void setIgnoreClose(boolean ignore) {
    m_ignoreClose = ignore;
  }

  @Override
  public boolean close() {
    if (m_ignoreClose) {
      return false;
    }
    else {
      firePopupEvent(new ProposalPopupEvent(ProposalPopupEvent.TYPE_POPUP_CLOSED));
      return super.close();
    }
  }

  public boolean isFocusOwner() {
    Control focusControl = getShell().getDisplay().getFocusControl();
    return (focusControl != null && focusControl.getShell() == getShell());
  }

  public void addPopupListener(IProposalPopupListener proposalSelectionListener) {
    m_selectionListeners.add(proposalSelectionListener);
  }

  public void removePopupListener(IProposalPopupListener proposalSelectionListener) {
    m_selectionListeners.remove(proposalSelectionListener);
  }

  public void setExpertMode(boolean searchExpertMode) {
    m_expertModusChecked = searchExpertMode;
    m_showAllProposals.setSelection(searchExpertMode);
  }

  private void firePopupEvent(ProposalPopupEvent event) {
    IProposalPopupListener[] listeners;
    synchronized (m_selectionListeners) {
      listeners = m_selectionListeners.toArray(new IProposalPopupListener[m_selectionListeners.size()]);
    }
    for (IProposalPopupListener listener : listeners) {
      listener.popupChanged(event);
    }
  }

  // private void selectProposal(int index){
  // // remove old selection
  // if(m_selectionIndex >= 0){
  // TableItem item=m_proposalTable.getItem(m_selectionIndex);
  // item.setText(getText(m_proposals[m_selectionIndex], false));
  // }
  //    Assert.isTrue(index >= 0, "Proposal index should never be negative"); //$NON-NLS-1$
  // if(m_proposals == null || index >= m_proposals.length){
  // return;
  // }
  // m_selectionIndex=index;
  // TableItem item=m_proposalTable.getItem(m_selectionIndex);
  // item.setText(getText(m_proposals[m_selectionIndex], true));
  // m_proposalTable.setSelection(m_selectionIndex);
  // m_proposalTable.showSelection();
  // }

  IContentProposalEx getSelectedProposal() {
    IStructuredSelection selection = (IStructuredSelection) m_tableViewer.getSelection();
    if (!selection.isEmpty()) {
      // ProposalPopupEvent delegateEvent=new ProposalPopupEvent(ProposalPopupEvent.TYPE_PROPOSAL_ACCEPTED);
      // delegateEvent.setData(ProposalPopupEvent.IDENTIFIER_SELECTED_PROPOSAL, selection.getFirstElement());
      // firePopupEvent(delegateEvent);
      return (IContentProposalEx) selection.getFirstElement();
    }
    return null;
    // int index=m_proposalTable.getSelectionIndex();
    // if(index < 0){
    // return null;
    // }
    // else{
    // return m_proposals[index];
    // }
  }

  private String getText(IContentProposalEx proposal, boolean selected) {
    return proposal.getLabel(selected, m_expertModusChecked);
  }

  private Image getImage(IContentProposalEx proposal, boolean selected) {
    return proposal.getImage(selected, m_expertModusChecked);
  }

  boolean setFocus() {
    return m_tableViewer.getTable().setFocus();
  }

  private void adjustBounds() {
    // Get our control's location in display coordinates.

    Point location = m_proposalField.getDisplay().map(m_proposalField.getParent(), null, m_proposalField.getLocation());
    int initialX = location.x + POPUP_OFFSET;
    int initialY = location.y + m_proposalField.getSize().y + POPUP_OFFSET;

    // If there is no specified size, force it by setting
    // up a layout on the table.
    if (popupSize == null) {
      FormData data = (FormData) m_tableViewer.getTable().getLayoutData();
      data.height = m_tableViewer.getTable().getItemHeight() * POPUP_CHAR_HEIGHT;
      data.width = Math.max(m_proposalField.getSize().x, POPUP_MINIMUM_WIDTH);
      m_tableViewer.getTable().setLayoutData(data);
      getShell().pack();
      popupSize = getShell().getSize();
    }
    getShell().setBounds(initialX, initialY, popupSize.x, popupSize.y);

  }

}
