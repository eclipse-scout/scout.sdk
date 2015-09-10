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
package org.eclipse.scout.sdk.core.sourcebuilder.annotation;

import javax.annotation.Generated;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.util.CoreUtils;

/**
 * <h3>{@link AnnotationSourceBuilderFactory}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 07.03.2013
 */
public final class AnnotationSourceBuilderFactory {

  private AnnotationSourceBuilderFactory() {
  }

  private static final String GENERATED_MSG = "This class is auto generated. No manual modifications recommended.";

  public static IAnnotationSourceBuilder createOverride() {
    return new AnnotationSourceBuilder(Override.class.getName());
  }

  public static IAnnotationSourceBuilder createDeprecated() {
    return new AnnotationSourceBuilder(Deprecated.class.getName());
  }

  /**
   * @param text
   *          without quotes
   * @return
   */
  public static IAnnotationSourceBuilder createSupressWarnings(String text) {
    AnnotationSourceBuilder a = new AnnotationSourceBuilder(SuppressWarnings.class.getName());
    a.putValue("value", CoreUtils.toStringLiteral(text));
    return a;
  }

  /**
   * @param typeThatGeneratedTheCode
   *          should be the effective type that caused generation of this new type. This is useful for housekeeping. If
   *          the typeThatGeneratedTheCode does not exist anymore then all generated classes can be deleted as well.
   */
  public static IAnnotationSourceBuilder createGenerated(String typeThatGeneratedTheCode) {
    return createGenerated(typeThatGeneratedTheCode, GENERATED_MSG);
  }

  /**
   * @param typeThatGeneratedTheCode
   *          should be the effective type that caused generation of this new type. This is useful for housekeeping. If
   *          the typeThatGeneratedTheCode does not exist anymore then all generated classes can be deleted as well.
   */
  public static IAnnotationSourceBuilder createGenerated(final String typeThatGeneratedTheCode, final String comments) {
    AnnotationSourceBuilder a = new AnnotationSourceBuilder(Generated.class.getName());
    a.putValue("value", CoreUtils.toStringLiteral(typeThatGeneratedTheCode));
    if (StringUtils.isNotBlank(comments)) {
      a.putValue("comments", CoreUtils.toStringLiteral(comments));
    }
    return a;
  }

}
