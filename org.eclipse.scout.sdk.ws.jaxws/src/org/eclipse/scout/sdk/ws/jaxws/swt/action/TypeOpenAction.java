/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
/**
 *
 */
package org.eclipse.scout.sdk.ws.jaxws.swt.action;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

public class TypeOpenAction extends AbstractLinkAction {
  private IType m_type;

  public TypeOpenAction() {
    super("Open", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class));
  }

  public void init(IType type) {
    m_type = type;

    if (type != null) {
      setLabel(Texts.get("openX", type.getElementName()));
      setLinkText(type.getElementName());
      try {
        if (type.isInterface()) {
          setImage(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Interface));
        }
        else if (Flags.isAbstract(type.getFlags())) {
          setImage(new JavaElementImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class), JavaElementImageDescriptor.ABSTRACT, new Point(16, 16)));
        }
        else {
          setImage(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class));
        }
      }
      catch (JavaModelException e) {
        setImage(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class));
      }
    }
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    if (m_type == null) {
      return null;
    }
    // open the class in the editor
    try {
      JavaUI.openInEditor(m_type);
    }
    catch (Exception e) {
      JaxWsSdk.logWarning(e);
    }
    return null;
  }
}
