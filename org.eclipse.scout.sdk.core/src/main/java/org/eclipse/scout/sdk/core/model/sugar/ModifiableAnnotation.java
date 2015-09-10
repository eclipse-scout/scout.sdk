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
package org.eclipse.scout.sdk.core.model.sugar;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.scout.sdk.core.model.api.IAnnotatable;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IAnnotationValue;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.MetaValueType;
import org.eclipse.scout.sdk.core.model.spi.AnnotationSpi;
import org.eclipse.scout.sdk.core.model.spi.AnnotationValueSpi;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;

/**
 * <h3>{@link ModifiableAnnotation}</h3>
 *
 * @author imo
 * @since 5.1.0
 */
public class ModifiableAnnotation implements IAnnotation {
  private IType m_type;
  private IAnnotatable m_owner;
  private Map<String, IAnnotationValue> m_values = new LinkedHashMap<>();

  /**
   * Make sure to set ALL values of the annotation with {@link #setValue(String, MetaValueType, Object, boolean)} in
   * order to meet the requirement that an annotation has no null values at all!
   *
   * @param annotationType
   * @param owner
   */
  public ModifiableAnnotation(IType annotationType, IAnnotatable owner) {
    m_type = annotationType;
    m_owner = owner;
  }

  /**
   * Override and set another value for the annotation wrapped in this {@link ModifiableAnnotation}
   *
   * @param name
   * @param type
   * @param value
   * @param syntheticDefaultValue
   */
  public void setValue(String name, final MetaValueType type, final Object value, boolean syntheticDefaultValue) {
    setValue(name, new IMetaValue() {
      @Override
      public MetaValueType getType() {
        return type;
      }

      @Override
      @SuppressWarnings("unchecked")
      public <T> T getObject(Class<T> expectedType) {
        return (T) value;
      }
    }, syntheticDefaultValue);
  }

  /**
   * Override and set another value for the annotation wrapped in this {@link ModifiableAnnotation}
   *
   * @param name
   * @param value
   * @param metaValue
   * @param syntheticDefaultValue
   */
  public void setValue(String name, IMetaValue metaValue, boolean syntheticDefaultValue) {
    m_values.put(name, new SyntheticAnnotationValue(name, metaValue, syntheticDefaultValue));
  }

  @Override
  public Map<String, IAnnotationValue> getValues() {
    return Collections.unmodifiableMap(m_values);
  }

  @Override
  public IAnnotationValue getValue(String name) {
    return m_values.get(name);
  }

  @Override
  public IJavaEnvironment getJavaEnvironment() {
    return m_type.getJavaEnvironment();
  }

  @Override
  public IType getType() {
    return m_type;
  }

  @Override
  public IAnnotatable getOwner() {
    return m_owner;
  }

  @Override
  public String getElementName() {
    return m_type.getElementName();
  }

  @Override
  public ISourceRange getSource() {
    return null;
  }

  @Override
  public String getName() {
    return m_type.getName();
  }

  @Override
  public void internalSetSpi(JavaElementSpi spi) {
  }

  @Override
  public AnnotationSpi unwrap() {
    return null;
  }

  private class SyntheticAnnotationValue implements IAnnotationValue {
    private final String m_name;
    private final IMetaValue m_metaValue;
    private final boolean m_syntheticDefaultValue;

    SyntheticAnnotationValue(String name, IMetaValue metaValue, boolean syntheticDefaultValue) {
      m_name = name;
      m_metaValue = metaValue;
      m_syntheticDefaultValue = syntheticDefaultValue;
    }

    @Override
    public IJavaEnvironment getJavaEnvironment() {
      return ModifiableAnnotation.this.getJavaEnvironment();
    }

    @Override
    public String getElementName() {
      return m_name;
    }

    @Override
    public IMetaValue getMetaValue() {
      return m_metaValue;
    }

    @Override
    public IAnnotation getDeclaringAnnotation() {
      return ModifiableAnnotation.this;
    }

    @Override
    public boolean isSyntheticDefaultValue() {
      return m_syntheticDefaultValue;
    }

    @Override
    public ISourceRange getSource() {
      return null;
    }

    @Override
    public ISourceRange getSourceOfExpression() {
      return null;
    }

    @Override
    public AnnotationValueSpi unwrap() {
      return null;
    }

    @Override
    public void internalSetSpi(JavaElementSpi spi) {
    }
  }

}
