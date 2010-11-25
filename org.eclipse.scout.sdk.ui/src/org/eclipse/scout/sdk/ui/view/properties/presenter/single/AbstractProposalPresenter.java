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
package org.eclipse.scout.sdk.ui.view.properties.presenter.single;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.sdk.operation.method.ScoutMethodDeleteOperation;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.OperationAction;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.DefaultProposalProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.IContentProposalEx;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * <h3>AbstractProposalPresenter</h3> ...
 */
public abstract class AbstractProposalPresenter<T extends IContentProposalEx> extends AbstractMethodPresenter {

  private ProposalTextField m_proposalField;
  private T m_currentSourceValue;
  private T m_defaultValue;
  private DefaultProposalProvider m_proposalProvider;
  private OptimisticLock storeValueLock = new OptimisticLock();

  public AbstractProposalPresenter(FormToolkit toolkit, Composite parent) {
    super(toolkit, parent);
  }

  @Override
  protected Control createContent(Composite container) {
    m_proposalProvider = new DefaultProposalProvider();
    m_proposalField = new ProposalTextField(container, m_proposalProvider, ProposalTextField.TYPE_NO_LABEL);
    m_proposalField.setEnabled(false);
    P_ProposalFieldListener listener = new P_ProposalFieldListener();
    m_proposalField.addProposalAdapterListener(listener);
    MenuManager manager = new MenuManager();
    Menu menu = manager.createContextMenu(m_proposalField);
    manager.addMenuListener(new IMenuListener() {
      public void menuAboutToShow(IMenuManager managerInside) {
        managerInside.removeAll();
        createContextMenu((MenuManager) managerInside);
      }
    });
    m_proposalField.setMenu(menu);
    return m_proposalField;
  }

  @Override
  public void setEnabled(boolean enabled) {
    if (!isDisposed()) {
      m_proposalField.setEnabled(enabled);
    }
    super.setEnabled(enabled);
  }

  @Override
  public boolean isEnabled() {
    if (!isDisposed()) {
      return m_proposalField.getEnabled() && super.isEnabled();
    }
    return false;
  }

  @Override
  protected void init(ConfigurationMethod method) throws CoreException {
    super.init(method);
    m_defaultValue = parseInput(getMethod().computeDefaultValue());
    setCurrentSourceValue(parseInput(getMethod().computeValue()));
    try {
      storeValueLock.acquire();
      if (getCurrentSourceValue() != null) {
        m_proposalField.acceptProposal(getCurrentSourceValue());
      }
      else {
        m_proposalField.setText("");
      }
      m_proposalField.setEnabled(true);
    }
    finally {
      storeValueLock.release();
    }
  }

  protected abstract T parseInput(String input) throws CoreException;

  protected abstract void storeValue(T value);

  public T getCurrentSourceValue() {
    return m_currentSourceValue;
  }

  public void setCurrentSourceValue(T value) {
    m_currentSourceValue = value;
  }

  public T getDefaultValue() {
    return m_defaultValue;
  }

  public ProposalTextField getProposalComponent() {
    return m_proposalField;
  }

  protected void createContextMenu(MenuManager manager) {
    if (getMethod().isImplemented()) {
      manager.add(new OperationAction("set default value", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.IMG_INFO), new ScoutMethodDeleteOperation(getMethod().peekMethod())));
    }
  }

  private class P_ProposalFieldListener implements IProposalAdapterListener {
    @SuppressWarnings("unchecked")
    public void proposalAccepted(ContentProposalEvent event) {
      setCurrentSourceValue((T) event.proposal);
      try {
        if (storeValueLock.acquire()) {
          storeValue(getCurrentSourceValue());
        }
      }
      finally {
        storeValueLock.release();
      }
    }

  } // end class P_TextListener

  @SuppressWarnings("unchecked")
  public T[] getProposals() {
    return (T[]) m_proposalProvider.getShortList();
  }

  public void setProposals(T[] proposals) {
    m_proposalProvider.setShortList(proposals);
  }

}
