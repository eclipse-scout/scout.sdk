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
package org.eclipse.scout.sdk.ui.extensions;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;

public interface IFormFieldExtension {

  String getName();

  IType getModelType();

  boolean isInShortList();

  Class<? extends AbstractFormFieldWizard> getNewWizardClazz();

  Class<? extends AbstractScoutTypePage> getNodePage();

  AbstractWorkspaceWizard createNewWizard();

  IPage createNodePage();

}
