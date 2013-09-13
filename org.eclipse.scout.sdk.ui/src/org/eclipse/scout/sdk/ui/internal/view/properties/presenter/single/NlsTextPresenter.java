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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.ILazyProposalContentProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalSelectionHandler;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.nls.NlsProposalDescriptionProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.nls.NlsTextContentProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.nls.NlsTextLabelProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.nls.NlsTextSelectionHandler;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractMethodPresenter;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.scout.sdk.workspace.type.config.ConfigPropertyUpdateOperation;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility.CustomImplementationException;
import org.eclipse.scout.sdk.workspace.type.config.parser.NlsPropertySourceParser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * <h3>NlsTextPresenter</h3> ...
 */
public class NlsTextPresenter extends AbstractMethodPresenter {

  private ProposalTextField m_proposalField;
  private INlsEntry m_currentSourceTuple;
  private INlsProject m_nlsProject;
  private OptimisticLock storeValueLock = new OptimisticLock();
  private NlsPropertySourceParser m_parser;

  public NlsTextPresenter(PropertyViewFormToolkit toolkit, Composite parent) {
    super(toolkit, parent);
  }

  @Override
  protected Control createContent(Composite container) {
    m_proposalField = new ProposalTextField(container, ProposalTextField.STYLE_NO_LABEL);
    toolkitAdapt(m_proposalField);
    m_proposalField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        handleProposalAccepted(event);
      }
    });
    m_proposalField.setEnabled(false);
    m_proposalField.setProposalDescriptionProvider(new NlsProposalDescriptionProvider());
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

  @Override
  protected void init(ConfigurationMethod method) throws CoreException {
    if (method == null) return;
    super.init(method);
    INlsProject newNlsProject = resolveNlsProject(method);
    if (!CompareUtility.equals(newNlsProject, m_nlsProject)) {
      m_nlsProject = newNlsProject;
      m_parser = new NlsPropertySourceParser(m_nlsProject);
      m_proposalField.setLabelProvider(createLabelProvider(getNlsProject()));
      m_proposalField.setContentProvider(createContentProvider(getNlsProject()));
      m_proposalField.setSelectionHandler(createSelectionHandler(getNlsProject()));
      m_proposalField.setEnabled(getNlsProject() != null);
    }

    try {
      storeValueLock.acquire();
      // check text only
      try {
        String simpleText = PropertyMethodSourceUtility.parseReturnParameterString(getMethod().computeValue(), getMethod().peekMethod(), getMethod().getSuperTypeHierarchy());
        if (simpleText != null) {
          throw new CoreException(new ScoutStatus(Status.INFO, "Text '" + simpleText + "'.", null));
        }
      }
      catch (CustomImplementationException e) {
        // void try to resolve nls entry
        m_currentSourceTuple = getParser().parseSourceValue(getMethod().computeValue(), getMethod().peekMethod(), getMethod().getSuperTypeHierarchy());
        m_proposalField.acceptProposal(m_currentSourceTuple);
      }
//      if (currentSourceValueKey != null) {
//        if (isNlsText.getValue()) {
//          m_currentSourceTuple = getNlsProject().getEntry(currentSourceValueKey);
//          if (m_currentSourceTuple == null) {
//            throw new CoreException(new ScoutStatus(Status.WARNING, "Key '" + currentSourceValueKey + "' not found!", null));
//          }
//          else {
//            m_proposalField.acceptProposal(m_currentSourceTuple);
//          }
//        }
//        else {
//          throw new CoreException(new ScoutStatus(Status.INFO, "Text '" + currentSourceValueKey + "'.", null));
//        }
//      }
//      else {
//        m_proposalField.acceptProposal(null);
//      }
      m_proposalField.setEnabled(true);
    }
    finally {
      storeValueLock.release();
    }
  }

  /**
   * might be overridden to provide another nls project
   * 
   * @param method
   *          not null
   * @return an nls project
   */
  protected INlsProject resolveNlsProject(ConfigurationMethod method) {
    return ScoutTypeUtility.findNlsProject(method.getType());
  }

  public NlsPropertySourceParser getParser() {
    return m_parser;
  }

  /**
   * might be overridden to provide an own implementation of a content provider
   * 
   * @param project
   *          might be null
   * @return a content provider or null
   */
  protected ILazyProposalContentProvider createContentProvider(INlsProject nlsProject) {
    if (nlsProject != null) {
      return new NlsTextContentProvider((NlsTextLabelProvider) getProposalField().getLabelProvider());
    }
    return null;
  }

  /**
   * might be overridden to provide an own implementation of a label provider
   * 
   * @param project
   *          might be null
   * @return a label provider or null
   */
  protected ILabelProvider createLabelProvider(INlsProject project) {
    if (project != null) {
      return new NlsTextLabelProvider(project);
    }
    return null;
  }

  /**
   * might be overridden to provide an own implementation of a selection handler
   * 
   * @param project
   *          might be null
   * @return a selection handler or null
   */
  protected IProposalSelectionHandler createSelectionHandler(INlsProject project) {
    if (project != null) {
      return new NlsTextSelectionHandler(getNlsProject());
    }
    return null;
  }

  protected ProposalTextField getProposalField() {
    return m_proposalField;
  }

  /**
   * @param event
   */
  protected void handleProposalAccepted(ContentProposalEvent event) {
    try {
      Object proposal = event.proposal;
      if (proposal == null) {
        m_proposalField.setText("");
        storeNlsText(null);
      }
      else {
        storeNlsText((INlsEntry) proposal);
      }
    }
    catch (CoreException e) {
      ScoutSdkUi.logError(e);
    }
  }

  protected synchronized void storeNlsText(final INlsEntry proposal) throws CoreException {
    try {
      if (storeValueLock.acquire()) {
        ConfigPropertyUpdateOperation<INlsEntry> updateOp = new ConfigPropertyUpdateOperation<INlsEntry>(getMethod(), getParser());
        updateOp.setValue(proposal);
        OperationJob job = new OperationJob(updateOp);
        job.setDebug(true);
        job.schedule();
      }
    }
    catch (Exception e) {
      ScoutSdkUi.logError("could not parse default value of method '" + getMethod().getMethodName() + "' in type '" + getMethod().getType().getFullyQualifiedName() + "'.", e);
    }
    finally {
      storeValueLock.release();
    }

//    try {
//      if (storeValueLock.acquire()) {
//        IOperation op = null;
//        INlsEntry defaultTuple = null;
//        String defaultKey = PropertyMethodSourceUtility.parseReturnParameterNlsKey(getMethod().computeDefaultValue());
//        if (defaultKey != null) {
//          defaultTuple = getNlsProject().getEntry(defaultKey);
//        }
//        if (UiUtility.equals(defaultTuple, proposal)) {
//          if (getMethod().isImplemented()) {
//            op = new ScoutMethodDeleteOperation(getMethod().peekMethod());
//          }
//        }
//        else {
//          if (proposal != null) {
//            op = new NlsTextMethodUpdateOperation(getMethod().getType(), getMethod().getMethodName(), false);
//            ((NlsTextMethodUpdateOperation) op).setNlsEntry(proposal);
//          }
//        }
//        if (op != null) {
//          new OperationJob(op).schedule();
//        }
//      }
//    }
//    finally {
//      storeValueLock.release();
//    }
  }

  public INlsProject getNlsProject() {
    return m_nlsProject;
  }
}
