/*******************************************************************************
 * Copyright (m_constant) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.model.spi.internal;

import java.lang.reflect.Array;

import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IArrayMetaValue;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.MetaValueType;
import org.eclipse.scout.sdk.core.model.spi.AnnotationSpi;
import org.eclipse.scout.sdk.core.model.spi.FieldSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.util.CoreUtils;

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

  protected abstract static class AbstractValue implements IMetaValue {

    protected abstract Object getInternalObject(Class<?> expectedType);

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Class<T> expectedType) {
      if (expectedType.isArray()) {
        //value is scalar, requested is array (for example the Generated annotation)
        T array = (T) Array.newInstance(expectedType.getComponentType(), 1);
        Array.set(array, 0, getInternalObject(expectedType.getComponentType()));
        return array;
      }
      return (T) getInternalObject(expectedType);
    }
  }

  protected abstract static class AbstractConstantMetaValue extends AbstractValue {
    private final Constant m_constant;

    protected AbstractConstantMetaValue(Constant c) {
      this.m_constant = c;
    }

    protected final Constant getInternalConstant() {
      return m_constant;
    }

    @Override
    protected final Object getInternalObject(Class<?> expectedType) {
      if (expectedType == Object.class) {
        switch (type()) {
          case Bool:
            return m_constant.booleanValue();
          case Byte:
            return m_constant.byteValue();
          case Char:
            return m_constant.charValue();
          case Double:
            return m_constant.doubleValue();
          case Float:
            return m_constant.floatValue();
          case Int:
            return m_constant.intValue();
          case Long:
            return m_constant.longValue();
          case Short:
            return m_constant.shortValue();
          case String:
            return m_constant.stringValue();
          default:
            return m_constant;
        }
      }
      if (expectedType == boolean.class || expectedType == Boolean.class) {
        return m_constant.booleanValue();
      }
      if (expectedType == byte.class || expectedType == Byte.class) {
        return m_constant.byteValue();
      }
      if (expectedType == char.class || expectedType == Character.class) {
        return m_constant.charValue();
      }
      if (expectedType == double.class || expectedType == Double.class) {
        return m_constant.doubleValue();
      }
      if (expectedType == float.class || expectedType == Float.class) {
        return m_constant.floatValue();
      }
      if (expectedType == int.class || expectedType == Integer.class) {
        return m_constant.intValue();
      }
      if (expectedType == long.class || expectedType == Long.class) {
        return m_constant.longValue();
      }
      if (expectedType == short.class || expectedType == Short.class) {
        return m_constant.shortValue();
      }
      if (expectedType == String.class) {
        return m_constant.stringValue();
      }
      return m_constant;
    }

  }

  protected abstract static class AbstractArrayMetaValue extends AbstractValue implements IArrayMetaValue {
  }

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
        return "UNKNOWN(" + o.toString() + ")";
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
        return type.getElementName() + ".class";
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
        return enumConstant.getDeclaringType().getElementName() + "." + enumConstant.getElementName();
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
        return a.toString();
      }
    };
  }

  public static IMetaValue createArray(final IMetaValue[] metaArray) {
    if (metaArray == null) {
      return createNull();
    }
    return new AbstractArrayMetaValue() {
      @Override
      public MetaValueType type() {
        return MetaValueType.Array;
      }

      @Override
      public IMetaValue[] metaValueArray() {
        return metaArray;
      }

      @Override
      protected Object getInternalObject(Class<?> expectedType) {
        return metaArray;
      }

      @SuppressWarnings("unchecked")
      @Override
      public <T> T get(Class<T> expectedType) {
        int n = metaArray.length;
        Object array;
        Class<?> elementType;
        if (expectedType == Object.class) {
          array = new Object[n];
          elementType = Object.class;
        }
        else if (expectedType.isArray()) {
          elementType = expectedType.getComponentType();
          array = Array.newInstance(elementType, n);
        }
        else if (n == 1) {
          //value is array with length 1, requested is scalar (for example the Generated annotation)
          return metaArray[0].get(expectedType);
        }
        else {
          throw new ClassCastException("expected type must be an array type: " + expectedType);
        }
        for (int i = 0; i < n; i++) {
          Array.set(array, i, metaArray[i].get(elementType));
        }
        return (T) array;
      }

      @Override
      public String toString() {
        int n = metaArray.length;
        //use newlines on multi-dimensional arrays and annotation arrays only
        char blockSeparator;
        if (n > 0 && (metaArray[0].type() == MetaValueType.Array || metaArray[0].type() == MetaValueType.Annotation)) {
          blockSeparator = '\n';
        }
        else {
          blockSeparator = ' ';
        }
        StringBuilder buf = new StringBuilder();
        buf.append('{');
        buf.append(blockSeparator);
        if (n > 0) {
          for (int i = 0; i < n; i++) {
            IMetaValue element = metaArray[i];
            if (i > 0) {
              buf.append(',');
              buf.append(blockSeparator);
            }
            buf.append(element.toString());
          }
        }
        buf.append(blockSeparator);
        buf.append('}');
        return buf.toString();
      }
    };
  }

  public static IMetaValue createFromConstant(Constant c) {
    if (c == null) {
      return createNull();
    }
    switch (c.typeID()) {
      case TypeIds.T_int: {
        return new AbstractConstantMetaValue(c) {
          @Override
          public MetaValueType type() {
            return MetaValueType.Int;
          }

          @Override
          public String toString() {
            return Integer.toString(getInternalConstant().intValue());
          }
        };
      }
      case TypeIds.T_byte: {
        return new AbstractConstantMetaValue(c) {

          @Override
          public MetaValueType type() {
            return MetaValueType.Byte;
          }

          @Override
          public String toString() {
            return Byte.toString(getInternalConstant().byteValue());
          }
        };
      }
      case TypeIds.T_short: {
        return new AbstractConstantMetaValue(c) {
          @Override
          public MetaValueType type() {
            return MetaValueType.Short;
          }

          @Override
          public String toString() {
            return Short.toString(getInternalConstant().shortValue());
          }
        };
      }
      case TypeIds.T_char:

      {
        return new AbstractConstantMetaValue(c) {
          @Override
          public MetaValueType type() {
            return MetaValueType.Char;
          }

          @Override
          public String toString() {
            return "'" + getInternalConstant().charValue() + "'";
          }
        };
      }
      case TypeIds.T_float:

      {
        return new AbstractConstantMetaValue(c) {
          @Override
          public MetaValueType type() {
            return MetaValueType.Float;
          }

          @Override
          public String toString() {
            return getInternalConstant().floatValue() + "f";
          }
        };
      }
      case TypeIds.T_double:

      {
        return new AbstractConstantMetaValue(c) {
          @Override
          public MetaValueType type() {
            return MetaValueType.Double;
          }

          @Override
          public String toString() {
            return Double.toString(getInternalConstant().doubleValue());
          }
        };
      }
      case TypeIds.T_boolean:

      {
        return new AbstractConstantMetaValue(c) {
          @Override
          public MetaValueType type() {
            return MetaValueType.Bool;
          }

          @Override
          public String toString() {
            return Boolean.toString(getInternalConstant().booleanValue());
          }
        };
      }
      case TypeIds.T_long: {
        return new AbstractConstantMetaValue(c) {
          @Override
          public MetaValueType type() {
            return MetaValueType.Long;
          }

          @Override
          public String toString() {
            return getInternalConstant().longValue() + "L";
          }
        };
      }
      case TypeIds.T_JavaLangString:

      {
        return new AbstractConstantMetaValue(c) {
          @Override
          public MetaValueType type() {
            return MetaValueType.String;
          }

          @Override
          public String toString() {
            return CoreUtils.toStringLiteral(getInternalConstant().stringValue());
          }
        };
      }
      default: {
        return createUnknown(c);
      }
    }
  }
}
