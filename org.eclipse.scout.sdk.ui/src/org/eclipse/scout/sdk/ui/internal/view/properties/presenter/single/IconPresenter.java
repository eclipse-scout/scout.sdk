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
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ScoutSdkUtility;
import org.eclipse.scout.sdk.jdt.signature.IImportValidator;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.ConfigPropertyMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.method.ScoutMethodDeleteOperation;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.fields.proposal.IconProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.ScoutProposalUtility;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractProposalPresenter;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.SdkTypeUtility;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtilities;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * <h3>IconPresenter</h3> ...
 */
public class IconPresenter extends AbstractProposalPresenter<IconProposal> {

  private Label m_currentIconPresenter;

  public IconPresenter(FormToolkit toolkit, Composite parent) {
    super(toolkit, parent);
  }

  @Override
  protected Control createContent(Composite container) {
    Composite rootPane = getToolkit().createComposite(container);
    m_currentIconPresenter = getToolkit().createLabel(rootPane, "", SWT.FLAT);
    Control text = super.createContent(rootPane);

    // layout
    GridLayout gLayout = new GridLayout(2, false);
    gLayout.horizontalSpacing = 0;
    gLayout.marginHeight = 0;
    gLayout.marginWidth = 0;
    gLayout.verticalSpacing = 0;
    rootPane.setLayout(gLayout);
    text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

    GridData gData = new GridData(ScoutIdeProperties.TOOL_BUTTON_SIZE, ScoutIdeProperties.TOOL_BUTTON_SIZE);
    gData.exclude = true;
    m_currentIconPresenter.setLayoutData(gData);
    return rootPane;
  }

  @Override
  public void setCurrentSourceValue(IconProposal value) {
    super.setCurrentSourceValue(value);
    Image icon = null;
    if (value != null) {
      icon = value.getImage();
    }
    if (icon != null) {
      ((GridData) m_currentIconPresenter.getLayoutData()).exclude = false;
      m_currentIconPresenter.setVisible(true);
    }
    else {
      ((GridData) m_currentIconPresenter.getLayoutData()).exclude = true;
      m_currentIconPresenter.setVisible(false);
    }
    m_currentIconPresenter.setImage(icon);
    getContainer().layout(true, true);
  }

  @Override
  protected void init(ConfigurationMethod method) throws CoreException {
    IScoutBundle scoutBundle = SdkTypeUtility.getScoutBundle(method.getType());
    setProposals(ScoutProposalUtility.getScoutIconProposals(ScoutSdkUi.getDisplay(), scoutBundle));
    super.init(method);
  }

  @Override
  protected IconProposal parseInput(String input) throws CoreException {
    String parsedString = PropertyMethodSourceUtilities.parseReturnParameterIcon(input, getMethod().peekMethod());
    IconProposal findProposal = findProposal(parsedString);
    return findProposal;
  }

  @Override
  protected synchronized void storeValue(final IconProposal value) {
    IOperation op = null;
    if (ScoutSdkUtility.equals(getDefaultValue(), value)) {
      if (getMethod().isImplemented()) {
        op = new ScoutMethodDeleteOperation(getMethod().peekMethod());
      }
    }
    else {
      op = new ConfigPropertyMethodUpdateOperation(getMethod().getType(), getMethod().getMethodName(), null, true) {
        @Override
        protected String createMethodBody(IMethod methodToOverride, IImportValidator validator) throws JavaModelException {
          StringBuilder source = new StringBuilder();
          source.append("return ");
          if (value != null) {
            String iconTypeSig = Signature.createTypeSignature(value.getImageDescription().getConstantField().getDeclaringType().getFullyQualifiedName(), false);
            source.append("  " + ScoutSdkUtility.getSimpleTypeRefName(iconTypeSig, validator) + "." + value.getImageDescription().getConstantField().getElementName());
            source.append(";");
          }
          else {
            source.append("null;");
          }
          return source.toString();
        }
      };
    }
    if (op != null) {
      new OperationJob(op).schedule();
    }
  }

  private IconProposal findProposal(String value) {
    if (value != null) {
      String simpleIconName = value.replaceAll("^.*\\.([^\\.]+)$", "$1");
      for (IconProposal prop : getProposals()) {
        if (prop.getImageDescription().getIconName().equals(simpleIconName)) {
          return prop;
        }
      }
    }
    return null;
  }

}
