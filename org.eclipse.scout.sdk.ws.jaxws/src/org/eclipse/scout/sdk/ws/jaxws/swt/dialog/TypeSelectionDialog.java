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
package org.eclipse.scout.sdk.ws.jaxws.swt.dialog;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

public class TypeSelectionDialog extends SelectionDialog<IType> {

  public TypeSelectionDialog(Shell shell, String dialogTitle, String dialogMessage) {
    super(shell, dialogTitle, dialogMessage);
  }

  @Override
  protected void execDecorateElement(IType type, ViewerCell cell) {
    if (cell.getColumnIndex() == 0) {
      cell.setText(type.getElementName());
      try {
        ImageDescriptor desc;
        if (Flags.isAbstract(type.getFlags())) {
          desc = new JavaElementImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class), JavaElementImageDescriptor.ABSTRACT, new Point(16, 16));
        }
        else if (type.isInterface()) {
          desc = ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Interface);
        }
        else {
          desc = ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class);
        }
        cell.setImage(desc.createImage());
      }
      catch (JavaModelException e) {
        JaxWsSdk.logError(e);
      }
    }
    else {
      cell.setText(type.getFullyQualifiedName());
    }
  }

  @Override
  protected boolean getConfiguredIsDescriptionColumnVisible() {
    return true;
  }

  @Override
  protected String getConfiguredNameColumnText() {
    return Texts.get("Type");
  }

  @Override
  protected String getConfiguredDescriptionColumnText() {
    return Texts.get("FullyQualifiedName");
  }
}
