/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.internal.view.properties.presenter;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.extensions.quickassist.ClassIdDocumentationSupport;
import org.eclipse.scout.sdk.ui.extensions.quickassist.IClassIdDocumentationListener;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.AbstractPresenter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

/**
 * <h3>{@link DocumentationPresenter}</h3>
 * 
 * @author Matthias Villiger
 * @since 3.10.0 08.01.2014
 */
public class DocumentationPresenter extends AbstractPresenter {
  private final IType m_type;
  private final ClassIdDocumentationSupport m_support;

  private Text m_textComponent;
  private ImageHyperlink m_editText;

  public DocumentationPresenter(PropertyViewFormToolkit toolkit, Composite parent, AbstractScoutTypePage page) {
    super(toolkit, parent);
    m_type = page.getType();
    m_support = new ClassIdDocumentationSupport(m_type, page.getScoutBundle().getDocsNlsProject());
    m_support.addModifiedListener(new IClassIdDocumentationListener() {
      @Override
      public void modified(int eventType, INlsEntry entry, IType owner) {
        refresh(entry);
      }
    });

    createContent(getContainer());

    refresh(m_support.getNlsEntry());
  }

  public IType getType() {
    return m_type;
  }

  protected void createContent(Composite container) {
    m_textComponent = getToolkit().createText(container, "", SWT.BORDER | SWT.LEFT | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.WRAP);
    m_textComponent.setEnabled(false);

    m_editText = getToolkit().createImageHyperlink(container, SWT.PUSH);
    m_editText.setImage(ScoutSdkUi.getImage(ScoutSdkUi.ToolEdit));
    m_editText.setToolTipText(Texts.get("EditDocumentation"));
    m_editText.addHyperlinkListener(new HyperlinkAdapter() {
      @Override
      public void linkActivated(HyperlinkEvent e) {
        handleLinkClicked();
      }
    });
    m_editText.setEnabled(!getType().isBinary());

    // layout
    GridLayout layout = new GridLayout(2, false);
    layout.marginHeight = 0;
    layout.horizontalSpacing = 3;
    layout.verticalSpacing = 3;
    layout.marginWidth = 0;
    container.setLayout(layout);

    GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL | GridData.GRAB_HORIZONTAL);
    data.heightHint = 100;
    m_textComponent.setLayoutData(data);

    data = new GridData(GridData.FILL_VERTICAL);
    data.verticalAlignment = SWT.CENTER;
    m_editText.setLayoutData(data);
  }

  protected void handleLinkClicked() {
    m_support.editDocumentation(getContainer().getShell());
  }

  protected void refresh(INlsEntry entry) {
    if (entry != null) {
      String text = entry.getTranslation(entry.getProject().getDevelopmentLanguage(), true);
      if (StringUtility.hasText(text)) {
        m_textComponent.setText(text);
      }
      else {
        m_textComponent.setText("");
      }
    }
  }
}
