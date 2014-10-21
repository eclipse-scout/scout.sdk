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

import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.IRegion;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.jdt.compile.ScoutSeverityManager;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.ShowJavaReferencesAction;
import org.eclipse.scout.sdk.ui.action.rename.TypeRenameAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchyProvider;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

public abstract class AbstractScoutTypePage extends AbstractPage implements ITypePage {

  private IType m_type;
  private final String m_readOnlySuffix;

  public AbstractScoutTypePage() {
    this(null);
  }

  public AbstractScoutTypePage(String readOnlySuffix) {
    m_readOnlySuffix = readOnlySuffix;
  }

  @Override
  public void setType(IType type) {
    m_type = type;
    String methodNameForTranslatedText = getMethodNameForTranslatedText();
    setName(PropertyMethodSourceUtility.getTranslatedMethodStringValue(getType(), methodNameForTranslatedText));
  }

  @Override
  public IType getType() {
    return m_type;
  }

  public String getReadOnlySuffix() {
    return m_readOnlySuffix;
  }

  protected ITypeHierarchy getLocalHierarchy() {
    IPage p = this;
    while (p != null) {
      if (p instanceof ITypeHierarchyProvider) {
        return ((ITypeHierarchyProvider) p).getTypeHierarchy();
      }
      p = p.getParent();
    }
    return TypeUtility.getLocalTypeHierarchy(getType()); // fall back if no parent has a type hierarchy created yet (e.g. for templates).
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
    if (TypeUtility.exists(getType())) {
      quality = ScoutSeverityManager.getInstance().getSeverityOf(getType());
    }
    return quality;
  }

  @Override
  public Set<Class<? extends IScoutHandler>> getSupportedMenuActions() {
    return newSet(TypeRenameAction.class, ShowJavaReferencesAction.class);
  }

  @Override
  public boolean handleDoubleClickedDelegate() {
    try {
      IEditorPart editor = JavaUI.openInEditor(getType());
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
  public int hashCode() {
    int hash = super.hashCode();
    IType type = getType();
    if (type != null) {
      hash += (31 * hash) + type.hashCode();
    }
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    boolean equals = super.equals(obj);
    if (!equals) {
      return false;
    }
    if (!(obj instanceof AbstractScoutTypePage)) {
      return false;
    }
    return CompareUtility.equals(getType(), ((AbstractScoutTypePage) obj).getType());
  }
}
