/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.sourcebuilder.compilationunit;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.scout.sdk.core.importcollector.IImportCollector;
import org.eclipse.scout.sdk.core.importcollector.WrappedImportCollector;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.signature.SignatureDescriptor;

/**
 * Do not instantiate this class directly, it is automatically created in
 * {@link ICompilationUnitSourceBuilder#createSource(StringBuilder, String, org.eclipse.scout.sdk.core.util.PropertyMap, org.eclipse.scout.sdk.core.importvalidator.IImportValidator)}
 * <p>
 * ignore imports when in same package or types in same compilation unit
 */
public class CompilationUnitScopedImportCollector extends WrappedImportCollector {
  private final String m_packageName;
  private final Map<String/* simpleName */, Boolean /* exists in own package*/> m_existsInSamePackageCache = new HashMap<>();

  public CompilationUnitScopedImportCollector(IImportCollector inner, String packageName) {
    super(inner);
    m_packageName = packageName;
  }

  @Override
  public String getQualifier() {
    return m_packageName;
  }

  @Override
  public String checkCurrentScope(SignatureDescriptor cand) {
    //same qualifier
    if (Objects.equals(getQualifier(), cand.getQualifier())) {
      return cand.getSimpleName();
    }

    // check if simpleName (with other packageName) exists in same package
    IJavaEnvironment env = getJavaEnvironment();
    if (env != null) {
      Boolean existsInSamePackage = m_existsInSamePackageCache.get(cand.getSimpleName());
      if (existsInSamePackage == null) {
        // load to cache
        String nameInOwnPackage = getQualifier() + '.' + cand.getSimpleName();
        existsInSamePackage = env.findType(nameInOwnPackage) != null;
        m_existsInSamePackageCache.put(cand.getSimpleName(), existsInSamePackage);
      }
      if (existsInSamePackage) {
        //must qualify
        return cand.getQualifiedName();
      }
    }

    return super.checkCurrentScope(cand);
  }

}
