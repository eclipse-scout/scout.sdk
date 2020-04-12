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
package org.eclipse.scout.sdk.core.model.ecj.metavalue;

import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.scout.sdk.core.model.api.AbstractMetaValue;
import org.eclipse.scout.sdk.core.model.api.ArrayMetaValue;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IField;
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
        StringBuilder b = new StringBuilder(type().toString());
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

    switch (c.typeID()) {
      case TypeIds.T_int:
        return new ConstantMetaValue(c, MetaValueType.Int);
      case TypeIds.T_null:
        return createNull();
      case TypeIds.T_byte:
        return new ConstantMetaValue(c, MetaValueType.Byte);
      case TypeIds.T_short:
        return new ConstantMetaValue(c, MetaValueType.Short);
      case TypeIds.T_char:
        return new ConstantMetaValue(c, MetaValueType.Char);
      case TypeIds.T_float:
        return new ConstantMetaValue(c, MetaValueType.Float);
      case TypeIds.T_double:
        return new ConstantMetaValue(c, MetaValueType.Double);
      case TypeIds.T_boolean:
        return new ConstantMetaValue(c, MetaValueType.Bool);
      case TypeIds.T_long:
        return new ConstantMetaValue(c, MetaValueType.Long);
      case TypeIds.T_JavaLangString:
        return new ConstantMetaValue(c, MetaValueType.String);
      default:
        return createUnknown(c);
    }
  }
}
