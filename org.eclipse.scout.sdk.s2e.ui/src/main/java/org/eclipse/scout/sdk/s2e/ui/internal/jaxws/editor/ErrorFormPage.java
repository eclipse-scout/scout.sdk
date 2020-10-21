/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.jaxws.editor;

import static java.lang.System.lineSeparator;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.swt.SWT;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;

/**
 * <h3>{@link ErrorFormPage}</h3>
 *
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
    var form = managedForm.getForm();
    var toolkit = managedForm.getToolkit();
    var parent = form.getBody();

    var msgBuilder = new StringBuilder();
    if (Strings.hasText(m_message)) {
      msgBuilder.append(m_message).append(lineSeparator());
    }
    if (m_throwable != null) {
      msgBuilder.append(Strings.fromThrowable(m_throwable));
    }

    var text = toolkit.createText(parent, msgBuilder.toString(), SWT.MULTI);
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
