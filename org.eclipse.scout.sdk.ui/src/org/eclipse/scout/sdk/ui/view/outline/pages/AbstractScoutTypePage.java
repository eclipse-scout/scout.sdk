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
package org.eclipse.scout.sdk.ui.view.outline.pages;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.ShowJavaReferencesAction;
import org.eclipse.scout.sdk.util.ScoutSeverityManager;
import org.eclipse.scout.sdk.util.ScoutSourceUtilities;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

public abstract class AbstractScoutTypePage extends AbstractPage implements ITypePage {

  private IType m_type;
  private int m_nodeTypeIndex = -1;

  public void setType(IType type) {
    m_type = type;
    String methodNameForTranslatedText = getMethodNameForTranslatedText();
    setName(ScoutSourceUtilities.getTranslatedMethodStringValue(getType(), methodNameForTranslatedText));
  }

  public IType getType() {
    return m_type;
  }

  protected String getMethodNameForTranslatedText() {
    return null;
  }

  @Override
  public boolean isFolder() {
    return false;
  }

  @Override
  public int getQuality() {
    int quality = IMarker.SEVERITY_INFO;
    if (getType().exists()) {
      quality = ScoutSeverityManager.getInstance().getSeverityOf(getType());
    }
    return quality;
  }

  @Override
  public void fillContextMenu(IMenuManager manager) {
    super.fillContextMenu(manager);
    manager.add(new Separator());
    manager.add(new ShowJavaReferencesAction(getType()));
  }

  @Override
  public boolean handleDoubleClickedDelegate() {
    try {
      IEditorPart editor = JavaUI.openInEditor(getType());
      // JavaUI.revealInEditor(editor, (IJavaElement)jdtMember);
      if (editor instanceof ITextEditor) {
        ITextEditor textEditor = (ITextEditor) editor;
        IRegion reg = textEditor.getHighlightRange();
        if (reg != null) {
          textEditor.setHighlightRange(reg.getOffset(), reg.getLength(), true);
        }
      }
      return true;
    }
    catch (Exception ex) {
      ScoutSdkUi.logWarning(ex);
      return false;
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof AbstractScoutTypePage)) {
      return false;
    }
    AbstractScoutTypePage page = (AbstractScoutTypePage) obj;
    return getType().equals(page.getType()) && CompareUtility.equals(page.getParent(), getParent());
  }

}
