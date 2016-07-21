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
package org.eclipse.scout.sdk.core.importcollector;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.signature.SignatureDescriptor;

/**
 * <h3>{@link EmptyImportCollector}</h3> An empty {@link IImportCollector} that never collects any imports and always
 * returns simple names. Can be used when not import validation is required.
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class EmptyImportCollector implements IImportCollector {

  @Override
  public IJavaEnvironment getJavaEnvironment() {
    return null;
  }

  @Override
  public String getQualifier() {
    return null;
  }

  @Override
  public void addStaticImport(String fqn) {
    // empty collector must not add anything
  }

  @Override
  public void addImport(String fqn) {
    // empty collector must not add anything
  }

  @Override
  public void reserveElement(SignatureDescriptor cand) {
    // empty collector must not add anything
  }

  @Override
  public String registerElement(SignatureDescriptor cand) {
    return cand.getSimpleName();
  }

  @Override
  public String checkExistingImports(SignatureDescriptor cand) {
    return cand.getSimpleName();
  }

  @Override
  public String checkCurrentScope(SignatureDescriptor cand) {
    return null;
  }

  @Override
  public List<String> createImportDeclarations() {
    return Collections.emptyList();
  }

  @Override
  public Collection<String> getStaticImports() {
    return Collections.emptyList();
  }

  @Override
  public Collection<String> getImports() {
    return Collections.emptyList();
  }

  @Override
  public List<String> createImportDeclarations(boolean includeExisting) {
    return Collections.emptyList();
  }
}
