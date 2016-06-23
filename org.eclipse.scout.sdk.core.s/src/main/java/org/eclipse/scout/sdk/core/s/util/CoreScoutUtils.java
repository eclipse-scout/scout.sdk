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
package org.eclipse.scout.sdk.core.s.util;

import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.IMavenConstants;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.s.annotation.OrderAnnotation;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <h3>{@link CoreScoutUtils}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public final class CoreScoutUtils {
  private CoreScoutUtils() {
  }

  /**
   * Gets the artifactId of the given pom {@link Document}.
   *
   * @param pom
   *          The pom {@link Document} to search the artifactId for.
   * @return The artifactId or <code>null</code> if no artifactId exists in the given {@link Document}.
   */
  public static String getArtifactIdOfPom(Document pom) {
    if (pom == null) {
      return null;
    }
    Element artifactIdElement = CoreUtils.getFirstChildElement(pom.getDocumentElement(), IMavenConstants.ARTIFACT_ID);
    if (artifactIdElement != null) {
      return artifactIdElement.getTextContent();
    }
    return null;
  }

  /**
   * Gets the groupId of the given pom {@link Document}.
   *
   * @param pom
   *          The pom {@link Document} to search the groupId for.
   * @return The groupId or <code>null</code> if no groupId exists in the given {@link Document}.
   */
  public static String getGroupIdOfPom(Document pom) {
    return getInheritedValueOfPom(pom, IMavenConstants.GROUP_ID);
  }

  /**
   * Gets the version of the given pom {@link Document}.
   *
   * @param pom
   *          The pom {@link Document} to search the version for.
   * @return The version or <code>null</code> if no version exists in the given {@link Document}.
   */
  public static String getVersionOfPom(Document pom) {
    return getInheritedValueOfPom(pom, IMavenConstants.VERSION);
  }

  /**
   * Gets the artifactId of the parent of the given pom {@link Document}.
   *
   * @param pom
   *          The pom {@link Document} to search the parent artifactId for.
   * @return The artifactId name or <code>null</code> if it does not exist.
   */
  public static String getParentArtifactId(Document pom) {
    if (pom == null) {
      return null;
    }
    Element parentElement = CoreUtils.getFirstChildElement(pom.getDocumentElement(), IMavenConstants.PARENT);
    if (parentElement == null) {
      return null;
    }
    Element artifactId = CoreUtils.getFirstChildElement(parentElement, IMavenConstants.ARTIFACT_ID);
    if (artifactId == null) {
      return null;
    }
    return artifactId.getTextContent();
  }

  static String getInheritedValueOfPom(Document pom, String tagName) {
    if (pom == null) {
      return null;
    }
    Element documentElement = pom.getDocumentElement();
    Element directValueElement = CoreUtils.getFirstChildElement(documentElement, tagName);
    if (directValueElement != null) {
      return directValueElement.getTextContent();
    }

    Element parentElement = CoreUtils.getFirstChildElement(documentElement, IMavenConstants.PARENT);
    if (parentElement == null) {
      return null;
    }
    directValueElement = CoreUtils.getFirstChildElement(parentElement, tagName);
    if (directValueElement == null) {
      return null;
    }
    return directValueElement.getTextContent();
  }

  /**
   * Gets the new order value for a type created in the given declaring type at given position.
   *
   * @param declaringType
   *          The container in which the ordered item will be placed.
   * @param orderDefinitionType
   *          The fully qualified interface name that defines siblings of the same order group
   * @param pos
   *          The source position of the compilation unit at which position the new item should be added. Must be inside
   *          the declaring type.
   * @return the new order value that should be used.
   */
  public static double getNewViewOrderValue(IType declaringType, String orderDefinitionType, int pos) {
    IType[] siblings = findSiblings(declaringType, pos, orderDefinitionType);
    Double orderValueBefore = getOrderAnnotationValue(siblings[0]);
    Double orderValueAfter = getOrderAnnotationValue(siblings[1]);

    // calculate next values
    if (orderValueBefore != null && orderValueAfter == null) {
      // insert at last position
      double v = Math.ceil(orderValueBefore.doubleValue() / ISdkProperties.VIEW_ORDER_ANNOTATION_VALUE_STEP) * ISdkProperties.VIEW_ORDER_ANNOTATION_VALUE_STEP;
      return v + ISdkProperties.VIEW_ORDER_ANNOTATION_VALUE_STEP;
    }
    else if (orderValueBefore == null && orderValueAfter != null) {
      // insert at first position
      double v = Math.floor(orderValueAfter.doubleValue() / ISdkProperties.VIEW_ORDER_ANNOTATION_VALUE_STEP) * ISdkProperties.VIEW_ORDER_ANNOTATION_VALUE_STEP;
      if (v > ISdkProperties.VIEW_ORDER_ANNOTATION_VALUE_STEP) {
        return ISdkProperties.VIEW_ORDER_ANNOTATION_VALUE_STEP;
      }
      return v - ISdkProperties.VIEW_ORDER_ANNOTATION_VALUE_STEP;
    }
    else if (orderValueBefore != null && orderValueAfter != null) {
      // insert between two types
      double a = orderValueBefore.doubleValue();
      double b = orderValueAfter.doubleValue();
      return getOrderValueInBetween(a, b);
    }

    // other cases. e.g. first item in a container
    return ISdkProperties.VIEW_ORDER_ANNOTATION_VALUE_STEP;
  }

  static Double getOrderAnnotationValue(IType sibling) {
    if (sibling == null) {
      return null;
    }
    return Double.valueOf(OrderAnnotation.valueOf(sibling, false));
  }

  static IType[] findSiblings(IType declaringType, int pos, String orderDefinitionType) {
    IType prev = null;
    for (IType t : declaringType.innerTypes().withInstanceOf(orderDefinitionType).list()) {
      if (t.source().start() > pos) {
        return new IType[]{prev, t};
      }
      prev = t;
    }
    return new IType[]{prev, null};
  }

  /**
   * Gets an order value that is between the two given values.<br>
   * The algorithm tries to stick to numbers without decimal places as long as possible.<br>
   * If a common pattern (like normal steps according to {@link ISdkProperties#VIEW_ORDER_ANNOTATION_VALUE_STEP}) are
   * found, the corresponding pattern is followed.
   *
   * @param a
   *          First value
   * @param b
   *          Second value
   * @return A value in between a and b.
   */
  static double getOrderValueInBetween(double a, double b) {
    double low = Math.min(a, b);
    double high = Math.max(a, b);
    double dif = high - low;
    double lowFloor = Math.floor(low);
    double lowCeil = Math.ceil(low);
    double highFloor = Math.floor(high);
    double nextIntLow = Math.min(lowCeil, highFloor);
    double prevIntHigh = Math.max(lowCeil, highFloor);

    // special case for stepwise increase
    if (low % ISdkProperties.VIEW_ORDER_ANNOTATION_VALUE_STEP == 0 && low + ISdkProperties.VIEW_ORDER_ANNOTATION_VALUE_STEP < high) {
      return low + ISdkProperties.VIEW_ORDER_ANNOTATION_VALUE_STEP;
    }

    if (lowFloor != highFloor && ((lowFloor != low && highFloor != high) || dif > 1.0)) {
      // integer value possible
      double intDif = prevIntHigh - nextIntLow;
      if (intDif == 1.0) {
        return prevIntHigh;
      }
      return nextIntLow + Math.floor(intDif / 2.0);
    }
    return low + (dif / 2);
  }
}
