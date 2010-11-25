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
package org.eclipse.scout.nls.sdk.internal.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

public class TextCompositionLayout extends Layout {
  private int m_labelProcentage;

  public TextCompositionLayout(int labelProcentage) {
    m_labelProcentage = labelProcentage;
  }

  @Override
  protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
    Point p = new Point(wHint, hHint);
    for (Control child : composite.getChildren()) {
      Point cP = child.computeSize(SWT.DEFAULT, SWT.DEFAULT);
      p.x = p.x + cP.x;
    }
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected void layout(Composite composite, boolean flushCache) {
    // TODO Auto-generated method stub

  }

  private int getProcentage(int value, int procentage) {
    return (int) ((value / 100.0) * procentage);
  }

}
