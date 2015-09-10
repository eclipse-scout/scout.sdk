/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.sdk.core.model.sugar;

import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.util.SdkException;

/**
 * <h3>{@link ManagedAnnotationUtil}</h3>
 * <p>
 * This util is used to create, copy and manage annotations as it would be done in normal java source code
 *
 * @author imo
 * @since 5.1.0
 */
public final class ManagedAnnotationUtil {
  private ManagedAnnotationUtil() {
  }

  public static <A extends AbstractManagedAnnotation> A wrap(IAnnotation a, Class<A> managedAnnotationType) {
    try {
      A annotation = managedAnnotationType.newInstance();
      annotation.postConstruct(a);
      return annotation;
    }
    catch (Exception e) {
      throw new IllegalArgumentException("create " + managedAnnotationType.getName() + " with " + a, e);
    }
  }

  /**
   * @return the value of the static field TYPE_NAME
   */
  public static String typeName(Class<? extends AbstractManagedAnnotation> a) {
    try {
      return (String) a.getField("TYPE_NAME").get(null);
    }
    catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
      throw new SdkException("failed to read field " + a.getName() + ".TYPE_NAME", e);
    }
  }

}
