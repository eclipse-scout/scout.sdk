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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.ui.dialog.JavaElementSelectionDialog;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractJavaElementListPresenter;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.config.ConfigPropertyUpdateOperation;
import org.eclipse.scout.sdk.workspace.type.config.parser.IPropertySourceParser;
import org.eclipse.scout.sdk.workspace.type.config.parser.OutlinesParser;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>OutlinesPresenter</h3> ...
 */
public class OutlinesPresenter extends AbstractJavaElementListPresenter {

  private IPropertySourceParser<IType[]> m_parser;

  public OutlinesPresenter(PropertyViewFormToolkit toolkit, Composite parent) {
    super(toolkit, parent);
    m_parser = new OutlinesParser();
  }

  public IPropertySourceParser<IType[]> getParser() {
    return m_parser;
  }

  @Override
  public IJavaElement[] readSource() throws CoreException {
    return getParser().parseSourceValue(getMethod().getSource(), getMethod().peekMethod(), getMethod().getSuperTypeHierarchy());
  }

  @Override
  protected void handleAddComponent() {
    IType iOutline = TypeUtility.getType(IRuntimeClasses.IOutline);
    HashSet<IJavaElement> sourceProposals = new HashSet<IJavaElement>(Arrays.asList(getSourceProps()));
    ArrayList<IJavaElement> candidates = new ArrayList<IJavaElement>();
    ICachedTypeHierarchy outlineHierarchy = TypeUtility.getPrimaryTypeHierarchy(iOutline);
    ITypeFilter filter = TypeFilters.getMultiTypeFilter(TypeFilters.getTypesOnClasspath(getMethod().getType().getJavaProject()), TypeFilters.getClassFilter());
    for (IType t : outlineHierarchy.getAllSubtypes(iOutline, filter, TypeComparators.getTypeNameComparator())) {
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
    // cast
    IType[] types = null;
    if (proposals != null) {
      types = new IType[proposals.length];
      for (int i = 0; i < proposals.length; i++) {
        types[i] = (IType) proposals[i];
      }
    }
    try {
      ConfigPropertyUpdateOperation<IType[]> updateOp = new ConfigPropertyUpdateOperation<IType[]>(getMethod(), getParser()) {
        @Override
        protected void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
          source.append(super.getParser().formatSourceValue(super.getValue(), lineDelimiter, validator));
        }
      };
      updateOp.setValue(types);
      OperationJob job = new OperationJob(updateOp);
      job.setDebug(true);
      job.schedule();
    }
    catch (Exception e) {
      ScoutSdkUi.logError("could not parse default value of method '" + getMethod().getMethodName() + "' in type '" + getMethod().getType().getFullyQualifiedName() + "'.", e);
    }
  }

  private static final class P_OutlineDialogPropertyListener implements PropertyChangeListener {
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
