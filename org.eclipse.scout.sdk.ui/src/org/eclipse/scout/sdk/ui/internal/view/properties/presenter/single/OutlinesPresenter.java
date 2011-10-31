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
package org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ScoutSdkUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jdt.signature.IImportValidator;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.ConfigPropertyMethodUpdateOperation;
import org.eclipse.scout.sdk.ui.dialog.JavaElementSelectionDialog;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractJavaElementListPresenter;
import org.eclipse.scout.sdk.util.ScoutSourceUtilities;
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.typecache.ICachedTypeHierarchy;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * <h3>OutlinesPresenter</h3> ...
 */
public class OutlinesPresenter extends AbstractJavaElementListPresenter {
  final IType iOutline = ScoutSdk.getType(RuntimeClasses.IOutline);

  public OutlinesPresenter(FormToolkit toolkit, Composite parent) {
    super(toolkit, parent);
  }

  @Override
  public IJavaElement[] readSource() throws CoreException {
    //ICachedTypeHierarchy outlineHierarchy = ScoutSdk.getPrimaryTypeHierarchy(iOutline);
    ArrayList<IJavaElement> props = new ArrayList<IJavaElement>();
    for (IType type : TypeUtility.getTypeOccurenceInMethod(getMethod().peekMethod())) {
      //if (outlineHierarchy.isSubtype(iOutline, type)) {
      props.add(type);
      //}
    }
    return props.toArray(new IJavaElement[props.size()]);
  }

  private String getFieldName(IType field, String labelMethodName) {
    return ScoutSourceUtilities.getTranslatedMethodStringValue(field, labelMethodName);
  }

  @Override
  protected void handleAddComponent() {
    HashSet<IJavaElement> sourceProposals = new HashSet<IJavaElement>(Arrays.asList(getSourceProps()));
    ArrayList<IJavaElement> candidates = new ArrayList<IJavaElement>();
    ICachedTypeHierarchy outlineHierarchy = ScoutSdk.getPrimaryTypeHierarchy(iOutline);
    for (IType t : outlineHierarchy.getAllSubtypes(iOutline, TypeFilters.getTypesOnClasspath(getMethod().getType().getJavaProject()), TypeComparators.getTypeNameComparator())) {
      if (!sourceProposals.contains(t)) {
        candidates.add(t);
      }
    }
    JavaElementSelectionDialog dialog = new JavaElementSelectionDialog(getContainer().getShell(), Texts.get("AddOutline"));
    dialog.addPropertyChangeListener(new P_OutlineDialogPropertyListener(dialog));
    dialog.setMultiSelect(true);
    dialog.setJavaElements(candidates.toArray(new IJavaElement[candidates.size()]));
    dialog.create();
    dialog.getOkButton().setEnabled(false);
    if (dialog.open() == IDialogConstants.OK_ID) {
      IJavaElement[] newOutlines = dialog.getSelectedElements();
      if (newOutlines.length > 0) {
        ArrayList<IJavaElement> outlines = new ArrayList<IJavaElement>(Arrays.asList(getSourceProps()));
        outlines.addAll(Arrays.asList(newOutlines));
        store(outlines.toArray(new IJavaElement[outlines.size()]));
      }
    }
  }

  @Override
  public synchronized void store(final IJavaElement[] proposals) {
    if (Arrays.equals(proposals, getSourceProps())) {
      return;
    }

    ConfigPropertyMethodUpdateOperation op = new ConfigPropertyMethodUpdateOperation(getMethod().getType(), getMethod().getMethodName()) {
      @Override
      protected String createMethodBody(IMethod methodToOverride, IImportValidator validator) throws JavaModelException {
        StringBuilder source = new StringBuilder();
        source.append("  return ");
        source.append("new Class[]{");
        if (proposals.length > 0) {
          for (int i = 0; i < proposals.length; i++) {
            source.append(ScoutSdkUtility.getSimpleTypeRefName(Signature.createTypeSignature(((IType) proposals[i]).getFullyQualifiedName(), true), validator) + ".class");
            if (i < (proposals.length - 1)) {
              source.append(",\n  ");
            }
          }
        }
        source.append("\n};");

        return source.toString();
      }
    };
    op.setFormatSource(true);
    new OperationJob(op).schedule();
  }

  private class P_OutlineDialogPropertyListener implements PropertyChangeListener {
    private final JavaElementSelectionDialog m_dialog;

    private P_OutlineDialogPropertyListener(JavaElementSelectionDialog dialog) {
      m_dialog = dialog;

    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (evt.getPropertyName().equals(JavaElementSelectionDialog.PROP_SELECTED_ELEMENTS)) {
        IJavaElement[] selection = (IJavaElement[]) evt.getNewValue();
        m_dialog.getOkButton().setEnabled(selection.length > 0);
      }
    }
  }

}
