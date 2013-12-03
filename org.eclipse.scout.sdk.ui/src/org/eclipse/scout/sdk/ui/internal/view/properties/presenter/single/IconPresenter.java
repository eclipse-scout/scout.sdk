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
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.sdk.icon.IIconProvider;
import org.eclipse.scout.sdk.icon.ScoutIconDesc;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.icon.IconContentProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.icon.IconLabelProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractMethodPresenter;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.scout.sdk.workspace.type.config.ConfigPropertyUpdateOperation;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.scout.sdk.workspace.type.config.parser.IconSourcePropertyParser;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * <h3>IconPresenter</h3> ...
 */
public class IconPresenter extends AbstractMethodPresenter {

  private Label m_currentIconPresenter;
  private ProposalTextField m_proposalField;
  private IIconProvider m_iconProvider;
  private ScoutIconDesc m_defaultIcon;
  private ScoutIconDesc m_currentSourceIcon;
  private final OptimisticLock m_storeValueLock;
  private final IconSourcePropertyParser m_parser;

  public IconPresenter(PropertyViewFormToolkit toolkit, Composite parent) {
    super(toolkit, parent);
    m_parser = new IconSourcePropertyParser();
    m_storeValueLock = new OptimisticLock();
  }

  @Override
  protected Control createContent(Composite container) {
    Composite rootPane = getToolkit().createComposite(container);
    m_currentIconPresenter = getToolkit().createLabel(rootPane, "", SWT.FLAT);
    m_proposalField = getToolkit().createProposalField(rootPane, ProposalTextField.STYLE_NO_LABEL);
    toolkitAdapt(m_proposalField);
    m_proposalField.setLabelProvider(new IconLabelProvider(m_proposalField.getDisplay()));
    m_proposalField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        handleProposalAccepted(event);
      }
    });

    // layout
    GridLayout gLayout = new GridLayout(2, false);
    gLayout.horizontalSpacing = 0;
    gLayout.marginHeight = 0;
    gLayout.marginWidth = 0;
    gLayout.verticalSpacing = 0;
    rootPane.setLayout(gLayout);
    m_proposalField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

    GridData gData = new GridData(SdkProperties.TOOL_BUTTON_SIZE, SdkProperties.TOOL_BUTTON_SIZE);
    gData.exclude = true;
    m_currentIconPresenter.setLayoutData(gData);
    return rootPane;
  }

  @Override
  protected void init(ConfigurationMethod method) throws CoreException {
    if (method == null) return;
    super.init(method);
    IIconProvider newIconProvider = ScoutTypeUtility.findIconProvider(method.getType());
    if (!CompareUtility.equals(newIconProvider, m_iconProvider)) {
      m_iconProvider = newIconProvider;
      m_parser.setIconProvider(m_iconProvider);
      m_proposalField.setContentProvider(new IconContentProvider(m_iconProvider, (ILabelProvider) m_proposalField.getLabelProvider()));
      m_proposalField.setEnabled(m_iconProvider != null);
    }

    m_defaultIcon = parseInput(getMethod().computeDefaultValue());
    try {
      m_storeValueLock.acquire();
      m_currentSourceIcon = parseInput(getMethod().computeValue());
      // TODO handle not parsable see nls presenter
      m_proposalField.acceptProposal(m_currentSourceIcon);
      updateIcon(m_currentSourceIcon);
      m_proposalField.setEnabled(true);
    }
    finally {
      m_storeValueLock.release();
    }
  }

  public IconSourcePropertyParser getParser() {
    return m_parser;
  }

  protected void handleProposalAccepted(ContentProposalEvent event) {
    Object proposal = event.proposal;
    if (proposal == null) {
      m_proposalField.setText("");
      storeValue(null);
    }
    else {
      storeValue((ScoutIconDesc) proposal);
    }
  }

  @Override
  public void setEnabled(boolean enabled) {
    if (!isDisposed()) {
      m_proposalField.setEnabled(enabled && getIconProvider() != null);
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

  public IIconProvider getIconProvider() {
    return m_iconProvider;
  }

  protected ScoutIconDesc parseInput(String input) throws CoreException {
    return getParser().parseSourceValue(input, getMethod().peekMethod(), getMethod().getSuperTypeHierarchy());
  }

  public ScoutIconDesc getDefaultValue() {
    return m_defaultIcon;
  }

  /**
   * must run in UI thread
   */
  private void updateIcon(ScoutIconDesc newIcon) {
    Image icon = null;
    if (newIcon != null) {
      icon = ((ILabelProvider) m_proposalField.getLabelProvider()).getImage(newIcon);
    }
    boolean iconAvailable = icon != null;
    ((GridData) m_currentIconPresenter.getLayoutData()).exclude = !iconAvailable;
    m_currentIconPresenter.setVisible(iconAvailable);
    m_currentIconPresenter.setImage(icon);
    getContainer().layout(true, true);
  }

  protected synchronized void storeValue(ScoutIconDesc value) {
    try {
      if (m_storeValueLock.acquire()) {
        if (value == null) {
          m_proposalField.acceptProposal(getDefaultValue());
          value = getDefaultValue();
        }

        ConfigPropertyUpdateOperation<ScoutIconDesc> updateOp = new ConfigPropertyUpdateOperation<ScoutIconDesc>(getMethod(), getParser());
        updateOp.setValue(value);
        final OperationJob job = new OperationJob(updateOp);
        job.setDebug(true);
        final ScoutIconDesc finalValue = value;
        job.addJobChangeListener(new JobChangeAdapter() {
          @Override
          public void done(IJobChangeEvent event) {
            job.removeJobChangeListener(this);
            getContainer().getDisplay().asyncExec(new Runnable() {
              @Override
              public void run() {
                updateIcon(finalValue);
              }
            });
          }
        });
        job.schedule();
      }
    }
    catch (Exception e) {
      ScoutSdkUi.logError("could not parse default value of method '" + getMethod().getMethodName() + "' in type '" + getMethod().getType().getFullyQualifiedName() + "'.", e);
    }
    finally {
      m_storeValueLock.release();
    }
  }
}
