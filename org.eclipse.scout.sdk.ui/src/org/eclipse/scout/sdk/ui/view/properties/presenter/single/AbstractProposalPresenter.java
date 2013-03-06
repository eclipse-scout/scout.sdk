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
import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * <h3>AbstractProposalPresenter</h3> ...
 */
public abstract class AbstractProposalPresenter<T extends Object> extends AbstractMethodPresenter {

  private ProposalTextField m_proposalField;
  private T m_currentSourceValue;
  private T m_defaultValue;
  private boolean m_defaultValueInitialized;
  private OptimisticLock storeValueLock = new OptimisticLock();

  public AbstractProposalPresenter(PropertyViewFormToolkit toolkit, Composite parent) {
    super(toolkit, parent);
    m_defaultValueInitialized = false;
  }

  @Override
  protected Control createContent(Composite container) {
    m_proposalField = getToolkit().createProposalField(container, "", ProposalTextField.STYLE_NO_LABEL);
    createProposalFieldProviders(m_proposalField);
    m_proposalField.setEnabled(false);
    P_ProposalFieldListener listener = new P_ProposalFieldListener();
    m_proposalField.addProposalAdapterListener(listener);
    return m_proposalField;
  }

  /**
   * @param proposalField
   */
  protected abstract void createProposalFieldProviders(ProposalTextField proposalField);

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

  /**
   * @param input
   *          the source string
   * @return the value represented by the input string
   * @throws CoreException
   *           when the input could not be parsed.
   */
  protected abstract T parseInput(String input) throws CoreException;

  /**
   * to store the value in the source file.
   * 
   * @param value
   *          can be null
   */
  protected abstract void storeValue(T value) throws CoreException;

  public T getCurrentSourceValue() {
    return m_currentSourceValue;
  }

  public void setCurrentSourceValue(T value) {
    m_currentSourceValue = value;
  }

  public T getDefaultValue() throws CoreException {
    if (!m_defaultValueInitialized) {
      m_defaultValue = parseInput(getMethod().computeDefaultValue());
      m_defaultValueInitialized = true;
    }
    return m_defaultValue;
  }

  public ProposalTextField getProposalField() {
    return m_proposalField;
  }

  private class P_ProposalFieldListener implements IProposalAdapterListener {
    @Override
    @SuppressWarnings("unchecked")
    public void proposalAccepted(ContentProposalEvent event) {
      setCurrentSourceValue((T) event.proposal);
      try {
        if (storeValueLock.acquire()) {
          try {
            storeValue(getCurrentSourceValue());
          }
          catch (CoreException e) {
            ScoutSdkUi.logError("Error changing the property value.", e);
          }
        }
      }
      finally {
        storeValueLock.release();
      }
    }
  } // end class P_ProposalFieldListener
}
