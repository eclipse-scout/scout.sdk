/*
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.api;

import java.lang.reflect.Array;

import org.eclipse.scout.sdk.core.builder.java.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.generator.annotation.IAnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.transformer.IWorkingCopyTransformer;

/**
 * <h3>{@link AbstractMetaValue}</h3>
 *
 * @since 6.1.0
 */
public abstract class AbstractMetaValue implements IMetaValue {

  protected abstract Object getInternalObject(Class<?> expectedType);

  @Override
  public ISourceGenerator<IExpressionBuilder<?>> toWorkingCopy() {
    return toWorkingCopy(null);
  }

  @Override
  public ISourceGenerator<IExpressionBuilder<?>> toWorkingCopy(IWorkingCopyTransformer transformer) {
    switch (type()) {
      case Null:
        return b -> b.append("null");
      case Int:
        return b -> b.append(as(Integer.class).intValue());
      case Byte:
        return b -> b.append(as(Byte.class).byteValue());
      case Short:
        return b -> b.append(as(Short.class).shortValue());
      case Char:
        return b -> b.append('\'')
            .append(as(Character.class))
            .append('\'');
      case Float:
        return b -> b.append(as(Float.class))
            .append('f');
      case Double:
        return b -> b.append(as(Double.class).doubleValue());
      case Bool:
        return b -> b.append(as(Boolean.class).booleanValue());
      case Long:
        return b -> b.append(as(Long.class))
            .append('L');
      case String:
        return b -> b.stringLiteral(as(String.class));
      case Type:
        return b -> b.classLiteral(as(IType.class).reference(true));
      case Enum:
        IField field = as(IField.class);
        return b -> b.enumValue(field.declaringType().name(), field.elementName());
      case Annotation:
        IAnnotationGenerator<?> annotationGenerator = as(IAnnotation.class).toWorkingCopy(transformer);
        return b -> b.append(annotationGenerator);
      // array case is handled in corresponding subclass
      default:
        return b -> b.append("UNKNOWN(").append(type().toString()).append(", ").append(toString()).append(')');
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T as(Class<T> expectedType) {
    if (expectedType.isArray()) {
      // value is scalar, requested is array (for example the SuppressWarnings annotation)
      Class<?> componentType = expectedType.getComponentType();
      Object val = getInternalObject(componentType);
      if (val == null) {
        // null values for annotations are not allowed according to JLS.
        // it might happen even though if e.g. a Java file contains compile errors.
        // in that case treat it as if the element is not there at all (it is invalid anyway).
        return (T) Array.newInstance(componentType, 0);
      }

      T array = (T) Array.newInstance(componentType, 1);
      Array.set(array, 0, val);
      return array;
    }
    return (T) getInternalObject(expectedType);
  }
}
