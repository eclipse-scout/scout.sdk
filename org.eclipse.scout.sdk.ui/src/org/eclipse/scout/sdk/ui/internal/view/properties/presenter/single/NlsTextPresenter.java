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
package org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.NlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.nls.sdk.ui.action.NlsEntryNewAction;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.method.NlsTextMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.method.ScoutMethodDeleteOperation;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.IContentProposalEx;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.NlsProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.NlsProposalTextField;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.fields.proposal.nls.NlsNewProposal;
import org.eclipse.scout.sdk.ui.internal.fields.proposal.nls.NlsNullProposal;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractMethodPresenter;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * <h3>NlsTextPresenter</h3> ...
 */
public class NlsTextPresenter extends AbstractMethodPresenter {

  protected NlsProposalTextField m_proposalField;
  private INlsEntry m_currentSourceTuple;
  private INlsEntry m_defaultTuple;
  private INlsProject m_nlsProject;
  private OptimisticLock storeValueLock = new OptimisticLock();

  public NlsTextPresenter(FormToolkit toolkit, Composite parent) {
    super(toolkit, parent);
  }

  @Override
  protected Control createContent(Composite container) {
    m_proposalField = new NlsProposalTextField(container, null, NlsProposalTextField.TYPE_NO_LABEL);
    toolkitAdapt(m_proposalField);
    m_proposalField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        handleProposalAccepted(event);
      }
    });
    m_proposalField.setEnabled(false);
    return m_proposalField;
  }

  @Override
  public void setEnabled(boolean enabled) {
    if (!isDisposed()) {
      m_proposalField.setEnabled(enabled && getNlsProject() != null);
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

  protected INlsProject getNlsProject(ConfigurationMethod method) {
    return ScoutTypeUtility.findNlsProject(method.getType());
  }

  @Override
  protected void init(ConfigurationMethod method) throws CoreException {
    if (method == null) return;
    super.init(method);
    m_nlsProject = getNlsProject(method);
    m_proposalField.setNlsProject(getNlsProject());
    if (getNlsProject() == null) {
      m_proposalField.setEnabled(false);
      return;
    }

    String defaultKey = PropertyMethodSourceUtility.parseReturnParameterNlsKey(getMethod().computeDefaultValue());
    if (defaultKey != null) {
      m_defaultTuple = getNlsProject().getEntry(defaultKey);
    }
    String currentSourceValueKey = PropertyMethodSourceUtility.parseReturnParameterNlsKey(getMethod().computeValue());
    try {
      storeValueLock.acquire();
      if (currentSourceValueKey != null) {
        m_currentSourceTuple = getNlsProject().getEntry(currentSourceValueKey);
        if (m_currentSourceTuple == null) {
          throw new CoreException(new ScoutStatus("could not parse nls presenter of : " + getMethod().getMethodName()));
        }
        else {
          m_proposalField.acceptProposal(new NlsProposal(m_currentSourceTuple, getNlsProject().getDevelopmentLanguage()));
        }
      }
      else {
        m_proposalField.acceptProposal(null);
      }
      m_proposalField.setEnabled(true);
    }
    finally {
      storeValueLock.release();
    }
  }

  /**
   * @param event
   */
  protected void handleProposalAccepted(ContentProposalEvent event) {
    IContentProposalEx proposal = event.proposal;
    if (proposal == null) {
      m_proposalField.setText("");
      storeNlsText(null);
    }
    else {
      if (proposal instanceof NlsNewProposal) {
        String proposalFieldText = event.text.substring(0, event.cursorPosition);
        String key = getNewKey(proposalFieldText);
        NlsEntry row = new NlsEntry(key, getNlsProject());
        row.addTranslation(getNlsProject().getDevelopmentLanguage(), proposalFieldText);
        NlsEntryNewAction action = new NlsEntryNewAction(row, getNlsProject());
        action.run();
        try {
          action.join();
        }
        catch (InterruptedException e) {
          ScoutSdkUi.logWarning(e);
        }
        row = action.getEntry();
        if (row != null) {
          NlsProposal newProposal = new NlsProposal(row, getNlsProject().getDevelopmentLanguage());
          m_proposalField.acceptProposal(newProposal);
          storeNlsText(newProposal);
        }
        else {
          storeNlsText(null);
          m_proposalField.setText("");
        }
      }
      else if (proposal instanceof NlsNullProposal) {
        storeNlsText(null);
      }
      else if (proposal instanceof NlsProposal) {
        storeNlsText((NlsProposal) proposal);
      }
    }
  }

  protected String getNewKey(String value) {
    List<String> existingKeys = Arrays.asList(getNlsProject().getAllKeys());
    if (value == null || value.length() == 0) {
      return null;
    }
    else {
      String[] split = value.split(" ");
      value = "";
      for (String splitValue : split) {
        value = value + Character.toUpperCase(splitValue.charAt(0)) + ((splitValue.length() > 1) ? (splitValue.substring(1)) : (""));
      }
      String newKey = value;
      int i = 0;
      while (existingKeys.contains(newKey)) {
        newKey = value + i++;
      }
      return newKey;
    }
  }

  protected synchronized void storeNlsText(final NlsProposal proposal) {
    try {
      if (storeValueLock.acquire()) {
        IOperation op = null;
        if (UiUtility.equals(m_defaultTuple, proposal)) {
          if (getMethod().isImplemented()) {
            op = new ScoutMethodDeleteOperation(getMethod().peekMethod());
          }
        }
        else {
          if (proposal != null) {
            op = new NlsTextMethodUpdateOperation(getMethod().getType(), getMethod().getMethodName(), false);
            ((NlsTextMethodUpdateOperation) op).setNlsEntry(proposal.getNlsEntry());
          }
        }
        if (op != null) {
          new OperationJob(op).schedule();
        }
      }
    }
    finally {
      storeValueLock.release();
    }
  }

  public INlsProject getNlsProject() {
    return m_nlsProject;
  }
}
