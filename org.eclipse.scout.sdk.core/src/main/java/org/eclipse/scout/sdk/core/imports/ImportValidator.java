/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.imports;

import java.util.Objects;
import java.util.function.Function;

import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.JavaTypes.ReferenceParser;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link ImportValidator}</h3>
 *
 * @since 5.2.0
 */
public class ImportValidator implements IImportValidator {

  private IImportCollector m_importCollector;
  private final ReferenceParser m_parser;

  public ImportValidator(IImportCollector collector) {
    m_parser = new ReferenceParser(this::handleTypeReference);
    setImportCollector(collector);
  }

  @Override
  public void runWithImportCollector(Runnable r, Function<IImportCollector, IImportCollector> wrappingCollectorProvider) {
    IImportCollector origImportCollector = importCollector();
    IImportCollector wrappingCollector = wrappingCollectorProvider.apply(origImportCollector);
    try {
      setImportCollector(wrappingCollector);
      r.run();
    }
    finally {
      setImportCollector(origImportCollector); // reset scope
    }
  }

  @Override
  public String useReference(CharSequence fullyQualifiedName) {
    return referenceParser().useReference(fullyQualifiedName);
  }

  protected CharSequence handleTypeReference(CharSequence fqn, boolean isTypeArg) {
    TypeReferenceDescriptor cand = new TypeReferenceDescriptor(fqn, isTypeArg);
    IImportCollector collector = importCollector();
    String use = collector.checkExistingImports(cand);
    if (use == null) {
      use = collector.checkCurrentScope(cand);
      if (cand.isTypeArg()) {
        boolean foundInCurrentScope = use != null && use.indexOf(JavaTypes.C_DOT) < 0;
        boolean inSamePackage = Objects.equals(collector.getQualifier(), cand.getQualifier()) || (Strings.isBlank(collector.getQualifier()) && Strings.isBlank(cand.getQualifier()));
        if (foundInCurrentScope && inSamePackage) {
          // special case for type argument signature which are simple qualified because in same scope
          collector.registerElement(cand); // ensure it is registered as used so that it appears in the imports for inner types only
        }
      }
    }
    if (use == null) {
      use = collector.registerElement(cand);
    }
    return use;
  }

  @Override
  public IImportCollector importCollector() {
    return m_importCollector;
  }

  protected ReferenceParser referenceParser() {
    return m_parser;
  }

  protected void setImportCollector(IImportCollector collector) {
    m_importCollector = Ensure.notNull(collector);
  }
}
