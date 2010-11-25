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

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractTypeProposalPresenter;
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * <h3>CodeTypePresenter</h3> ...
 */
public class CodeTypeProposalPresenter extends AbstractTypeProposalPresenter {
  final IType iCodeType = ScoutSdk.getType(RuntimeClasses.ICodeType);

  public CodeTypeProposalPresenter(FormToolkit toolkit, Composite parent) {
    super(toolkit, parent, "getConfiguredText", true);
  }

  @Override
  protected IType[] provideScoutTypes(IJavaProject project, IType ownerType) {
    return ScoutSdk.getPrimaryTypeHierarchy(iCodeType).getAllSubtypes(iCodeType, TypeFilters.getTypesOnClasspath(project), TypeComparators.getTypeNameComparator());
  }

}
