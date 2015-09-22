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
package org.eclipse.scout.sdk.ui.view.outline;

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPageFilter;
import org.eclipse.scout.sdk.ui.view.outline.pages.ITypePage;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class ViewLabelProvider extends LabelProvider implements IColorProvider, IFontProvider {

  private Composite m_parent;
  private Font m_fontPlainItalic;
  private final IPageFilterable m_filterable;

  public ViewLabelProvider(Composite parent, IPageFilterable explorerPart) {
    m_parent = parent;
    m_filterable = explorerPart;
  }

  @Override
  public String getText(Object obj) {
    if (obj instanceof IPage) {
      IPage p = (IPage) obj;
      IPageFilter filter = m_filterable.getPageFilter(p);
      if (filter == null || filter.isEmpty()) {
        return p.getName();
      }
      else {
        return p.getName() + " [" + filter.getFilterExpression() + "]";
      }
    }
    else {
      return obj.toString();
    }
  }

  @Override
  public Image getImage(Object obj) {
    if (obj instanceof AbstractPage) {
      AbstractPage p = (AbstractPage) obj;
      return p.getImage();
    }
    else {
      return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
    }
  }

  @Override
  public Color getBackground(Object element) {
    return null;
  }

  @Override
  public Color getForeground(Object element) {
    if (element instanceof IPage) {
      IPage p = (IPage) element;
      if (p instanceof ITypePage) {
        IType t = ((ITypePage) p).getType();
        if (TypeUtility.exists(t) && t.isBinary()) {
          return ScoutSdkUi.getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW);
        }
      }

      IScoutBundle scoutBundle = p.getScoutBundle();
      if (scoutBundle != null && scoutBundle.isBinary()) {
        return ScoutSdkUi.getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW);
      }
    }
    return null;
  }

  @Override
  public Font getFont(Object element) {
    ensureFontCache();
    if (element instanceof IPage) {
      IPage p = (IPage) element;
      IPageFilter filter = m_filterable.getPageFilter(p);
      if (filter == null || filter.isEmpty()) {
        return null; // no special font
      }
      else {
        return m_fontPlainItalic;
      }
    }
    return null;
  }

  private void ensureFontCache() {
    if (m_fontPlainItalic == null) {
      try {
        Font f = m_parent.getFont();
        if (f != null) {
          FontData[] d = f.getFontData();
          if (d != null && d.length > 0) {
            if (m_fontPlainItalic == null) {
              m_fontPlainItalic = new Font(f.getDevice(), d[0].getName(), d[0].getHeight(), SWT.ITALIC);
            }
          }
        }
      }
      catch (Exception t) {
        ScoutSdkUi.logWarning(t);
      }
    }
  }
}