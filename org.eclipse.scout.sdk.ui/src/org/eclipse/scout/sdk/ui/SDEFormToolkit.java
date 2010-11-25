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
package org.eclipse.scout.sdk.ui;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;

public class SDEFormToolkit extends FormToolkit {

  public SDEFormToolkit(Display display) {
    super(display);
  }

  public Hyperlink createTypeLink(Composite parent, String text, final IType type) {
    Hyperlink hyperlink = createHyperlink(parent, text, SWT.NONE);
    hyperlink.addHyperlinkListener(new HyperlinkAdapter() {
      @Override
      public void linkActivated(HyperlinkEvent event) {
        try {
          IType jdtType = type;
          if (jdtType != null) {
            JavaUI.openInEditor(jdtType);
          }
        }
        catch (Exception e) {
          ScoutSdkUi.logWarning(e);
        }
      }
    });
    return hyperlink;
  }

}
