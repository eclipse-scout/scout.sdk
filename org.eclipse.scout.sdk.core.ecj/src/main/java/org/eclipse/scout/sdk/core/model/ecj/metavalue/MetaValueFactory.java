/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.model.ecj.metavalue;

import java.util.stream.Stream;

import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.scout.sdk.core.model.api.AbstractMetaValue;
import org.eclipse.scout.sdk.core.model.api.ArrayMetaValue;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.MetaValueType;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.model.spi.MemberSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.util.JavaTypes;

/**
 * <h3>{@link MetaValueFactory}</h3>
 *
 * @since 5.1.0
 */
public final class MetaValueFactory {

  private static final IMetaValue NULL_META_VALUE = new AbstractMetaValue() {
    @Override
    public MetaValueType type() {
      return MetaValueType.Null;
    }

    @Override
    protected Object getInternalObject(Class<?> expectedType) {
      return null;
    }

    @Override
    public String toString() {
      return "null";
    }
  };

  private MetaValueFactory() {
  }

  public static IMetaValue createNull() {
    return NULL_META_VALUE;
  }

  public static IMetaValue createUnknown(Object o) {
    if (o == null) {
      return createNull();
    }

    return new AbstractMetaValue() {
      @Override
      public MetaValueType type() {
        return MetaValueType.Unknown;
      }

      @Override
      protected Object getInternalObject(Class<?> expectedType) {
        return o;
      }

      @Override
      public String toString() {
        var b = new StringBuilder(type().toString());
        b.append('(');
        b.append(o);
        b.append(')');
        return b.toString();
      }
    };
  }

  public static IMetaValue createFromType(TypeSpi type) {
    if (type == null) {
      return createNull();
    }

    return new AbstractMetaValue() {
      @Override
      public MetaValueType type() {
        return MetaValueType.Type;
      }

      @Override
      protected Object getInternalObject(Class<?> expectedType) {
        if (IType.class == expectedType || Object.class == expectedType) {
          return type.wrap();
        }
        if (String.class == expectedType) {
          return type.getName();
        }
        return type;
      }

      @Override
      public String toString() {
        return type.getElementName() + JavaTypes.CLASS_FILE_SUFFIX;
      }
    };
  }

  public static IMetaValue createFromEnum(MemberSpi enumConstant) {
    if (enumConstant == null) {
      return createNull();
    }

    return new AbstractMetaValue() {
      @Override
      public MetaValueType type() {
        return MetaValueType.Enum;
      }

      @Override
      protected Object getInternalObject(Class<?> expectedType) {
        if (IField.class == expectedType || Object.class == expectedType) {
          return enumConstant.wrap();
        }
        if (String.class == expectedType) {
          return enumConstant.getElementName();
        }
        return enumConstant;
      }

      @Override
      public String toString() {
        return enumConstant.getDeclaringType().getElementName() + JavaTypes.C_DOT + enumConstant.getElementName();
      }
    };
  }

  public static IMetaValue createFromAnnotation(JavaElementSpi a) {
    if (a == null) {
      return createNull();
    }

    return new AbstractMetaValue() {
      @Override
      public MetaValueType type() {
        return MetaValueType.Annotation;
      }

      @Override
      public Stream<IJavaElement> children() {
        return Stream.of(as(IAnnotation.class));
      }

      @Override
      protected Object getInternalObject(Class<?> expectedType) {
        if (IAnnotation.class == expectedType || Object.class == expectedType) {
          return a.wrap();
        }
        return a;
      }

      @Override
      public String toString() {
        return getInternalObject(IAnnotation.class).toString();
      }
    };
  }

  public static IMetaValue createArray(IMetaValue[] metaArray) {
    if (metaArray == null) {
      return createNull();
    }

    return new ArrayMetaValue(metaArray);
  }

  public static IMetaValue createFromConstant(Constant c) {
    if (c == null) {
      return createNull();
    }

    return switch (c.typeID()) {
      case TypeIds.T_int -> new ConstantMetaValue(c, MetaValueType.Int);
      case TypeIds.T_null -> createNull();
      case TypeIds.T_byte -> new ConstantMetaValue(c, MetaValueType.Byte);
      case TypeIds.T_short -> new ConstantMetaValue(c, MetaValueType.Short);
      case TypeIds.T_char -> new ConstantMetaValue(c, MetaValueType.Char);
      case TypeIds.T_float -> new ConstantMetaValue(c, MetaValueType.Float);
      case TypeIds.T_double -> new ConstantMetaValue(c, MetaValueType.Double);
      case TypeIds.T_boolean -> new ConstantMetaValue(c, MetaValueType.Bool);
      case TypeIds.T_long -> new ConstantMetaValue(c, MetaValueType.Long);
      case TypeIds.T_JavaLangString -> new ConstantMetaValue(c, MetaValueType.String);
      default -> createUnknown(c);
    };
  }
}
