/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.fields.proposal.content;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.core.s.nls.ITranslationStore;
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreStack;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment;
import org.eclipse.swt.graphics.Image;

/**
 * <h3>{@link TranslationStoreContentProvider}</h3>
 *
 * @since 7.0.0
 */
public class TranslationStoreContentProvider extends AbstractContentProviderAdapter {

  private final TranslationStoreStack m_stack;
  private final Map<org.eclipse.scout.sdk.core.model.api.IType, IType> m_jdtTypeCache;

  public TranslationStoreContentProvider(TranslationStoreStack stack) {
    m_stack = Ensure.notNull(stack);
    m_jdtTypeCache = new HashMap<>();
  }

  @Override
  public Image getImage(Object element) {
    return super.getImage(toTextService(element));
  }

  @Override
  public String getText(Object element) {
    return ((ITranslationStore) element).service().type().elementName();
  }

  @Override
  public String getTextSelected(Object element) {
    IType t = toTextService(element);
    StringBuilder sb = new StringBuilder(t.getElementName());
    String packageName = t.getPackageFragment().getElementName();
    if (Strings.hasText(packageName)) {
      sb.append(" - ").append(packageName);
    }
    return sb.toString();
  }

  @Override
  public Object createDescriptionContent(Object element, IProgressMonitor monitor) {
    return super.createDescriptionContent(toTextService(element), monitor);
  }

  public IType toTextService(Object element) {
    ITranslationStore store = (ITranslationStore) element;
    return m_jdtTypeCache.computeIfAbsent(store.service().type(), EclipseEnvironment::toJdtType);
  }

  @Override
  protected Collection<?> loadProposals(IProgressMonitor monitor) {
    return m_stack.allEditableStores()
        .collect(toList());
  }
}
