/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.nls;

import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.ISdkConstants;
import org.eclipse.scout.sdk.core.s.annotation.OrderAnnotation;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;

/**
 * <h3>{@link TextProviderService}</h3>
 * <p>
 * Represents a Scout text provider service. This is a sub class of
 * {@code org.eclipse.scout.rt.shared.services.common.text.ITextProviderService}.
 *
 * @since 7.0.0
 */
public class TextProviderService {

  /**
   * Default file suffix for text-provider-services
   */
  public static final String TEXT_SERVICE_FILE_SUFFIX = ISdkConstants.SUFFIX_TEXT_PROVIDER_SERVICE + JavaTypes.JAVA_FILE_SUFFIX;

  private final IType m_txtSvc;
  private final double m_order;

  /**
   * Creates a new {@link TextProviderService} based on the specified {@link IType}.
   *
   * @param txtSvc
   *          The {@link IType} of the service. Must not be {@code null}.
   */
  public TextProviderService(IType txtSvc) {
    m_txtSvc = Ensure.notNull(txtSvc);
    m_order = OrderAnnotation.valueOf(txtSvc, true);
  }

  /**
   * @return The bean order of the service.
   */
  public double order() {
    return m_order;
  }

  /**
   * @return The {@link IType} of the service.
   */
  public IType type() {
    return m_txtSvc;
  }

  @Override
  public String toString() {
    var builder = new StringBuilder();
    builder.append(TextProviderService.class.getSimpleName()).append(" [")
        .append(m_txtSvc.name()).append(", ");
    builder.append("order=").append(m_order).append(']');
    return builder.toString();
  }

  @Override
  public int hashCode() {
    return m_txtSvc.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    var other = (TextProviderService) obj;
    return m_txtSvc.name().equals(other.m_txtSvc.name());
  }
}
