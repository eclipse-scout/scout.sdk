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
package org.eclipse.scout.sdk.core.model.spi.internal.metavalue;

import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.MetaValueType;
import org.eclipse.scout.sdk.core.model.spi.AnnotationSpi;
import org.eclipse.scout.sdk.core.model.spi.FieldSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;

/**
 * <h3>{@link MetaValueFactory}</h3>
 *
 * @author Ivan Motsch
 * @since 5.1.0
 */
public final class MetaValueFactory {

  private MetaValueFactory() {
  }

  private static final IMetaValue NULL_META_VALUE = new IMetaValue() {
    @Override
    public MetaValueType type() {
      return MetaValueType.Null;
    }

    @Override
    public <T> T get(Class<T> expectedType) {
      return null;
    }

    @Override
    public String toString() {
      return "null";
    }
  };

  public static IMetaValue createNull() {
    return NULL_META_VALUE;
  }

  public static IMetaValue createUnknown(final Object o) {
    if (o == null) {
      return createNull();
    }

    return new AbstractValue() {
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
        b.append(o.toString());
        b.append(')');
        return b.toString();
      }
    };
  }

  public static IMetaValue createFromType(final TypeSpi type) {
    if (type == null) {
      return createNull();
    }

    return new AbstractValue() {
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
        return type.getElementName() + SuffixConstants.SUFFIX_STRING_class;
      }
    };
  }

  public static IMetaValue createFromEnum(final FieldSpi enumConstant) {
    if (enumConstant == null) {
      return createNull();
    }

    return new AbstractValue() {
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
        return enumConstant.getDeclaringType().getElementName() + '.' + enumConstant.getElementName();
      }
    };
  }

  public static IMetaValue createFromAnnotation(final AnnotationSpi a) {
    if (a == null) {
      return createNull();
    }

    return new AbstractValue() {
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

  public static IMetaValue createArray(final IMetaValue[] metaArray) {
    if (metaArray == null) {
      return createNull();
    }

    return new ArrayMetaValue(metaArray);
  }

  public static IMetaValue createFromConstant(Constant c) {
    if (c == null) {
      return createUnknown(null);
    }

    switch (c.typeID()) {
      case TypeIds.T_int: {
        return new ConstantMetaValue(c, MetaValueType.Int);
      }
      case TypeIds.T_null: {
        return createNull();
      }
      case TypeIds.T_byte: {
        return new ConstantMetaValue(c, MetaValueType.Byte);
      }
      case TypeIds.T_short: {
        return new ConstantMetaValue(c, MetaValueType.Short);
      }
      case TypeIds.T_char: {
        return new ConstantMetaValue(c, MetaValueType.Char);
      }
      case TypeIds.T_float: {
        return new ConstantMetaValue(c, MetaValueType.Float);
      }
      case TypeIds.T_double: {
        return new ConstantMetaValue(c, MetaValueType.Double);
      }
      case TypeIds.T_boolean: {
        return new ConstantMetaValue(c, MetaValueType.Bool);
      }
      case TypeIds.T_long: {
        return new ConstantMetaValue(c, MetaValueType.Long);
      }
      case TypeIds.T_JavaLangString: {
        return new ConstantMetaValue(c, MetaValueType.String);
      }
      default: {
        return createUnknown(c);
      }
    }
  }
}
