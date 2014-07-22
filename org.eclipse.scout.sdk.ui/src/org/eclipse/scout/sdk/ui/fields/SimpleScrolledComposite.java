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
package org.eclipse.scout.sdk.ui.fields;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.SharedScrolledComposite;

/**
 * <h3>{@link SimpleScrolledComposite}</h3>
 * 
 * @author Matthias Villiger
 * @since 3.9.0 17.12.2012
 */
public class SimpleScrolledComposite extends SharedScrolledComposite {
  public SimpleScrolledComposite(Composite parent) {
    super(parent, SWT.V_SCROLL);
    setFont(parent.getFont());
    setExpandHorizontal(true);
    setExpandVertical(true);

    Composite body = new Composite(this, SWT.NONE);
    body.setFont(parent.getFont());
    setContent(body);

    GridData dd = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
    dd.exclude = true;
    dd.heightHint = getNextParentSize(parent);
    setLayoutData(dd);
  }

  public Composite getBody() {
    return (Composite) getContent();
  }

  private static int getNextParentSize(Composite container) {
    Composite parent = container;
    while ((parent = parent.getParent()) != null) {
      if (parent.getSize().y > 0) {
        return parent.getSize().y - 140;
      }
    }
    return 480;
  }
}
