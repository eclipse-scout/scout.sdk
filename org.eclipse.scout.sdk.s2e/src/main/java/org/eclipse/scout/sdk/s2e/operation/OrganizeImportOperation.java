/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.operation;

import java.util.function.Consumer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.manipulation.JavaManipulation;
import org.eclipse.jdt.core.manipulation.OrganizeImportsOperation;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.s2e.environment.EclipseProgress;

/**
 * <h3>{@link OrganizeImportOperation}</h3>
 *
 * @since 9.0.0
 */
public class OrganizeImportOperation implements Consumer<EclipseProgress> {

  private final ICompilationUnit m_icu;

  /**
   * @param icu
   *          The {@link ICompilationUnit} for which the imports should be organized. Must not be {@code null}.
   */
  public OrganizeImportOperation(ICompilationUnit icu) {
    m_icu = Ensure.notNull(icu);
  }

  @Override
  public void accept(EclipseProgress t) {
    if (JavaManipulation.getPreferenceNodeId() == null) {
      return; // not configured. throws IllegalArgumentException. Do not organize imports
    }

    ICompilationUnit unit = getCompilationUnit();
    try {
      CodeGenerationSettings settings = JavaPreferencesSettings.getCodeGenerationSettings(unit.getJavaProject());
      OrganizeImportsOperation organizeImps = new OrganizeImportsOperation(unit, null, settings.importIgnoreLowercase, !unit.isWorkingCopy(), true, null);
      organizeImps.run(t.monitor());
    }
    catch (CoreException e) {
      throw new SdkException(e);
    }
  }

  public ICompilationUnit getCompilationUnit() {
    return m_icu;
  }

  @Override
  public String toString() {
    return "Organize imports of compilation unit '" + getCompilationUnit().getElementName() + "'.";
  }
}
