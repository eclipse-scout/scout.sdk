/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.internal.editor.jaxws;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * <h3>{@link ErrorFormPage}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class ErrorFormPage extends FormPage {

  private final Throwable m_throwable;
  private final String m_message;

  public ErrorFormPage(FormEditor editor, String message, Throwable throwable) {
    super(editor, "ErrorFormPage", "Error");
    m_message = message;
    m_throwable = throwable;
  }

  @Override
  public WebServiceEditor getEditor() {
    return (WebServiceEditor) super.getEditor();
  }

  @Override
  protected void createFormContent(IManagedForm managedForm) {
    ScrolledForm form = managedForm.getForm();
    FormToolkit toolkit = managedForm.getToolkit();
    Composite parent = form.getBody();

    StringBuilder msgBuilder = new StringBuilder();
    if (StringUtils.isNotBlank(m_message)) {
      msgBuilder.append(m_message).append("\n");
    }
    if (m_throwable != null) {
      msgBuilder.append(CoreUtils.getThrowableAsString(m_throwable));
    }

    Text text = toolkit.createText(parent, msgBuilder.toString(), SWT.MULTI);
    text.setEditable(false);

    // layout
    GridLayoutFactory
        .swtDefaults()
        .applyTo(parent);
    GridDataFactory
        .defaultsFor(text)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .applyTo(text);
  }
}