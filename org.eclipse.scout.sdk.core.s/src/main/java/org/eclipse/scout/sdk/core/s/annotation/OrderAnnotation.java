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
package org.eclipse.scout.sdk.core.s.annotation;

import static org.eclipse.scout.sdk.core.model.api.Flags.isAbstract;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;

import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.api.AbstractManagedAnnotation;
import org.eclipse.scout.sdk.core.model.api.IAnnotatable;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.JavaTypes;

/**
 * <h3>{@link OrderAnnotation}</h3>
 *
 * @since 5.2.0
 */
public class OrderAnnotation extends AbstractManagedAnnotation {

  public static final String TYPE_NAME = IScoutRuntimeTypes.Order;
  public static final String VALUE_ELEMENT_NAME = "value";

  public static double valueOf(IAnnotatable owner, boolean isBean) {
    Optional<OrderAnnotation> first = owner.annotations()
        .withManagedWrapper(OrderAnnotation.class)
        .first();

    //don't evaluate as stream to prevent auto boxing
    if (first.isPresent()) {
      return first.get().value();
    }

    if (isBean) {
      return ISdkProperties.DEFAULT_BEAN_ORDER;
    }
    return ISdkProperties.DEFAULT_VIEW_ORDER;
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
    return getNewViewOrderValue(orderValueBefore, orderValueAfter);
  }

  /**
   * Gets a new view order value in between of the two values specified.
   *
   * @param orderValueBefore
   *          The lower bound. May be {@code null}.
   * @param orderValueAfter
   *          The upper bound. May be {@code null}.
   * @return A new value in the middle between the two values given. The algorithm tries to avoid decimals as possible.
   */
  @SuppressWarnings("squid:S2589") // second arg is required so that the compiler is happy
  public static double getNewViewOrderValue(Double orderValueBefore, Double orderValueAfter) {
    // calculate next values
    if (orderValueBefore != null && orderValueAfter == null) {
      // insert at last position
      double orderValueBeforeAsDouble = orderValueBefore;
      validateOrderRange(orderValueBeforeAsDouble);
      double v = Math.ceil(orderValueBeforeAsDouble / ISdkProperties.VIEW_ORDER_ANNOTATION_VALUE_STEP) * ISdkProperties.VIEW_ORDER_ANNOTATION_VALUE_STEP;
      return v + ISdkProperties.VIEW_ORDER_ANNOTATION_VALUE_STEP;
    }
    if (orderValueBefore == null && orderValueAfter != null) {
      // insert at first position
      double orderValueAfterAsDouble = orderValueAfter;
      validateOrderRange(orderValueAfterAsDouble);
      double v = Math.floor(orderValueAfterAsDouble / ISdkProperties.VIEW_ORDER_ANNOTATION_VALUE_STEP) * ISdkProperties.VIEW_ORDER_ANNOTATION_VALUE_STEP;
      if (v > ISdkProperties.VIEW_ORDER_ANNOTATION_VALUE_STEP) {
        return ISdkProperties.VIEW_ORDER_ANNOTATION_VALUE_STEP;
      }
      return v - ISdkProperties.VIEW_ORDER_ANNOTATION_VALUE_STEP;
    }
    //noinspection ConstantConditions
    if (orderValueBefore != null && orderValueAfter != null) {
      // insert between two types
      double a = orderValueBefore;
      double b = orderValueAfter;
      validateOrderRange(a);
      validateOrderRange(b);
      return getOrderValueInBetween(a, b);
    }

    // other cases. e.g. first item in a container
    return ISdkProperties.VIEW_ORDER_ANNOTATION_VALUE_STEP;
  }

  private static void validateOrderRange(double order) {
    if (order > ISdkProperties.DEFAULT_VIEW_ORDER) {
      NumberFormat f = NumberFormat.getNumberInstance(Locale.ENGLISH);
      f.setGroupingUsed(false);
      String orderAsString = f.format(order);
      SdkLog.warning("The @Order value {} is very large and therefore may not be precise enough. It is recommended to use a lower value.", orderAsString);
    }
  }

  static Double getOrderAnnotationValue(IAnnotatable sibling) {
    if (sibling == null) {
      return null;
    }
    return OrderAnnotation.valueOf(sibling, false);
  }

  static IType[] findSiblings(IType declaringType, int pos, String orderDefinitionType) {
    Iterable<IType> i = declaringType.innerTypes()
        .withInstanceOf(orderDefinitionType).stream()
        .filter(candidate -> !isAbstract(candidate.flags()))::iterator;

    IType prev = null;
    for (IType t : i) {
      if (t.source().get().start() > pos) {
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
    //noinspection NumericCastThatLosesPrecision
    if ((int) low % ISdkProperties.VIEW_ORDER_ANNOTATION_VALUE_STEP == 0 && low + ISdkProperties.VIEW_ORDER_ANNOTATION_VALUE_STEP < high) {
      return low + ISdkProperties.VIEW_ORDER_ANNOTATION_VALUE_STEP;
    }

    if (isDoubleDifferent(lowFloor, highFloor) && ((isDoubleDifferent(lowFloor, low) && isDoubleDifferent(highFloor, high)) || dif > 1.0)) {
      // integer value possible
      double intDif = prevIntHigh - nextIntLow;
      if (!isDoubleDifferent(intDif, 1.0)) {
        return prevIntHigh;
      }
      return nextIntLow + Math.floor(intDif / 2.0);
    }
    return low + (dif / 2);
  }

  static boolean isDoubleDifferent(double d1, double d2) {
    return CoreUtils.isDoubleDifferent(d1, d2, 0.0000000001);
  }

  /**
   * Converts the given double to a {@link String} suitable to place into an @Order annotation value. The algorithm
   * tries to avoid decimal places and literal specifiers as possible.
   *
   * @param order
   *          The order to format as Java source {@link String}.
   * @return The created source {@link String} ready to be placed into a Java file. Never returns {@code null}.
   */
  public static String convertToJavaSource(double order) {
    NumberFormat f = NumberFormat.getNumberInstance(Locale.ENGLISH);
    f.setGroupingUsed(false);
    String newOrderStr = f.format(order);

    String zeroSuffix = JavaTypes.C_DOT + "0";
    if (newOrderStr.endsWith(zeroSuffix)) {
      newOrderStr = newOrderStr.substring(0, newOrderStr.length() - zeroSuffix.length());
    }

    if (order > Integer.MAX_VALUE && newOrderStr.indexOf(JavaTypes.C_DOT) < 0) {
      // we must specify the double data type because we do not have a decimal separator and we do not fit into an integer literal.
      newOrderStr += 'd';
    }
    return newOrderStr;
  }

  public double value() {
    return getValue(VALUE_ELEMENT_NAME, double.class, null);
  }
}
