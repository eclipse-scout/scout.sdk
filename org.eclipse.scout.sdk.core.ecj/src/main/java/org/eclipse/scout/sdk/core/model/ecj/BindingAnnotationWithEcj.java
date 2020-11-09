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
package org.eclipse.scout.sdk.core.model.ecj;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.ElementValuePair;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.internal.AnnotationImplementor;
import org.eclipse.scout.sdk.core.model.spi.AbstractJavaEnvironment;
import org.eclipse.scout.sdk.core.model.spi.AnnotatableSpi;
import org.eclipse.scout.sdk.core.model.spi.AnnotationElementSpi;
import org.eclipse.scout.sdk.core.model.spi.AnnotationSpi;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;

public class BindingAnnotationWithEcj extends AbstractJavaElementWithEcj<IAnnotation> implements AnnotationSpi {
  private final AnnotatableSpi m_owner;
  private final AnnotationBinding m_binding;
  private final FinalValue<Map<String, AnnotationElementSpi>> m_values; //sorted
  private final FinalValue<TypeSpi> m_type;
  private final FinalValue<ISourceRange> m_source;
  private final FinalValue<Annotation> m_annotationDeclaration;

  protected BindingAnnotationWithEcj(AbstractJavaEnvironment env, AnnotatableSpi owner, AnnotationBinding binding) {
    super(env);
    m_binding = Ensure.notNull(binding);
    m_owner = Ensure.notNull(owner);
    m_values = new FinalValue<>();
    m_type = new FinalValue<>();
    m_source = new FinalValue<>();
    m_annotationDeclaration = new FinalValue<>();
  }

  @Override
  public JavaElementSpi internalFindNewElement() {
    //not supported
    return null;
  }

  @Override
  protected IAnnotation internalCreateApi() {
    return new AnnotationImplementor(this);
  }

  public AnnotationBinding getInternalBinding() {
    return m_binding;
  }

  @Override
  public TypeSpi getType() {
    return m_type.computeIfAbsentAndGet(() -> SpiWithEcjUtils.bindingToType(javaEnvWithEcj(), m_binding.getAnnotationType()));
  }

  private static <T> Map<T, Integer> buildPositionsMap(Collection<T> collection) {
    Map<T, Integer> elementPositionMap = new HashMap<>(collection.size());
    var pos = 0;
    for (var name : collection) {
      elementPositionMap.put(name, pos);
      pos++;
    }
    return elementPositionMap;
  }

  private static Map<String, AnnotationElementSpi> buildAnnotationElementMap(Map<String, ?> defaultsMap, Object[] declaredPairs, AnnotationSpi owner, JavaEnvironmentWithEcj env) {
    // remember the position of each element by name
    var elementPositionMap = buildPositionsMap(defaultsMap.keySet());

    // fill declared values
    var resultArr = new AnnotationElementSpi[defaultsMap.size()];
    if (declaredPairs != null && declaredPairs.length > 0) {
      for (var declaredPair : declaredPairs) {
        var v = createAnnotationElementSpi(declaredPair, false, owner, env);
        var idx = elementPositionMap.get(v.getElementName());
        resultArr[idx] = v;
      }
    }

    var pos = 0;
    Map<String, AnnotationElementSpi> result = new LinkedHashMap<>(defaultsMap.size());
    for (Entry<String, ?> e : defaultsMap.entrySet()) {
      var declaredElement = resultArr[pos];
      if (declaredElement == null) {
        // add default value
        var defaultValue = e.getValue();
        var name = e.getKey();
        if (defaultValue == null) {
          // the annotation has no default value for this element and the element is not defined. this is a compile error but still might be possible in the source.
          result.put(name, env.createNullAnnotationValue(owner, name, true));
        }
        else {
          result.put(name, createAnnotationElementSpi(defaultValue, true, owner, env));
        }
      }
      else {
        result.put(declaredElement.getElementName(), declaredElement);
      }
      pos++;
    }
    return result;
  }

  private static AnnotationElementSpi createAnnotationElementSpi(Object nameValuePair, boolean syntheticDefaultValue, AnnotationSpi owner, JavaEnvironmentWithEcj env) {
    if (nameValuePair instanceof MemberValuePair) {
      return env.createDeclarationAnnotationValue(owner, (MemberValuePair) nameValuePair, syntheticDefaultValue);
    }
    if (nameValuePair instanceof ElementValuePair) {
      return env.createBindingAnnotationValue(owner, (ElementValuePair) nameValuePair, syntheticDefaultValue);
    }
    throw Ensure.newFail("Unsupported annotation element type: '{}'.", nameValuePair);
  }

  private static Map<String, ?> getAnnotationDefaultValues(TypeBinding annotationType, JavaEnvironmentWithEcj env) {
    if (annotationType instanceof ReferenceBinding) {
      return env.getBindingAnnotationSyntheticDefaultValues((ReferenceBinding) annotationType);
    }
    return env.getDeclarationAnnotationSyntheticDefaultValues(annotationType);
  }

  /**
   * @param annotation
   *          The annotation. Must be {@link Annotation} or {@link AnnotationBinding}
   * @param owner
   *          The owner {@link AnnotationSpi} that holds the elements
   * @param env
   *          The {@link JavaEnvironmentWithEcj} to use to create the element instances.
   * @return The {@link Map} with all elements (explicit and inherited (synthetic) ones)
   * @throws IllegalArgumentException
   *           if wrong annotation types are passed
   */
  static Map<String, AnnotationElementSpi> buildAnnotationElementMap(Object annotation, AnnotationSpi owner, JavaEnvironmentWithEcj env) {
    TypeBinding annotationType;
    Object[] explicitValues;
    if (annotation instanceof Annotation) {
      var binding = (Annotation) annotation;
      explicitValues = binding.memberValuePairs();
      annotationType = binding.type.resolvedType;
    }
    else if (annotation instanceof AnnotationBinding) {
      var binding = (AnnotationBinding) annotation;
      explicitValues = binding.getElementValuePairs();
      annotationType = binding.getAnnotationType();
    }
    else {
      throw Ensure.newFail("Unsupported annotation type: '{}'.", annotation);
    }

    var defaultValues = getAnnotationDefaultValues(annotationType, env);
    return buildAnnotationElementMap(defaultValues, explicitValues, owner, env);
  }

  @Override
  public Map<String, AnnotationElementSpi> getValues() {
    return m_values.computeIfAbsentAndGet(() -> buildAnnotationElementMap(m_binding, this, javaEnvWithEcj()));
  }

  @Override
  public AnnotatableSpi getOwner() {
    return m_owner;
  }

  @Override
  public String getElementName() {
    return getType().getElementName();
  }

  Annotation annotationDeclaration() {
    return m_annotationDeclaration.computeIfAbsentAndGet(() -> SpiWithEcjUtils.findAnnotationDeclaration(this));
  }

  @Override
  public ISourceRange getSource() {
    return m_source.computeIfAbsentAndGet(() -> {
      var decl = annotationDeclaration();
      if (decl == null) {
        return null;
      }
      var cu = SpiWithEcjUtils.declaringTypeOf(this).getCompilationUnit();
      return javaEnvWithEcj().getSource(cu, decl.sourceStart, decl.declarationSourceEnd);
    });
  }
}
