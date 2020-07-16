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
package org.eclipse.scout.sdk.core.generator;

import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.core.generator.ISourceGenerator.empty;
import static org.eclipse.scout.sdk.core.generator.ISourceGenerator.raw;
import static org.eclipse.scout.sdk.core.generator.transformer.IWorkingCopyTransformer.transformAnnotation;
import static org.eclipse.scout.sdk.core.imports.ImportCollector.createImportDeclaration;

import java.util.Arrays;
import java.util.List;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.expression.ExpressionBuilder;
import org.eclipse.scout.sdk.core.builder.java.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.generator.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.generator.type.TypeGenerator;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IAnnotationElement;
import org.eclipse.scout.sdk.core.model.api.IArrayMetaValue;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IImport;
import org.eclipse.scout.sdk.core.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.IUnresolvedType;
import org.eclipse.scout.sdk.core.model.api.MetaValueType;
import org.eclipse.scout.sdk.core.util.JavaTypes;

public final class SimpleGenerators {

  private SimpleGenerators() {
  }

  public static ISourceGenerator<ISourceBuilder<?>> createImportGenerator(IImport imp) {
    return raw(createImportDeclaration(imp.isStatic(), imp.name()));
  }

  public static ISourceGenerator<IExpressionBuilder<?>> createMetaValueGenerator(IMetaValue mv, IWorkingCopyTransformer transformer) {
    switch (mv.type()) {
      case Null:
        return b -> b.append("null");
      case Int:
        return b -> b.append(mv.as(Integer.class).intValue());
      case Byte:
        return b -> b.append(mv.as(Byte.class).byteValue());
      case Short:
        return b -> b.append(mv.as(Short.class).shortValue());
      case Char:
        return b -> b.append('\'')
            .append(mv.as(Character.class))
            .append('\'');
      case Float:
        return b -> b.append(mv.as(Float.class))
            .append('f');
      case Double:
        return b -> b.append(mv.as(Double.class).doubleValue());
      case Bool:
        return b -> b.append(mv.as(Boolean.class).booleanValue());
      case Long:
        return b -> b.append(mv.as(Long.class))
            .append('L');
      case String:
        return b -> b.stringLiteral(mv.as(String.class));
      case Type:
        return b -> b.classLiteral(mv.as(IType.class).reference(true));
      case Enum:
        IField field = mv.as(IField.class);
        return b -> b.enumValue(field.requireDeclaringType().name(), field.elementName());
      case Annotation:
        return transformAnnotation(mv.as(IAnnotation.class), transformer)
            .<ISourceGenerator<IExpressionBuilder<?>>> map(g -> b -> b.append(g))
            .orElseGet(ISourceGenerator::empty);
      case Array:
        return createArrayMetaValueGenerator((IArrayMetaValue) mv, transformer);
      default:
        return b -> b.append("UNKNOWN(").append(mv.type().toString()).append(", ").append(mv.toString()).append(')');
    }
  }

  public static ISourceGenerator<IExpressionBuilder<?>> createArrayMetaValueGenerator(IArrayMetaValue mv, IWorkingCopyTransformer transformer) {
    IMetaValue[] metaArray = mv.metaValueArray();

    // do not inline this into the generator! the transformation must be executed before the generator is used!
    List<ISourceGenerator<ISourceBuilder<?>>> generators = Arrays.stream(metaArray)
        .map(m -> createMetaValueGenerator(m, transformer))
        .map(g -> g.generalize(ExpressionBuilder::create))
        .collect(toList());

    // use newlines on multi-dimensional arrays and annotation arrays only
    boolean useNewlines = metaArray.length > 0 && (metaArray[0].type() == MetaValueType.Array || metaArray[0].type() == MetaValueType.Annotation);
    return b -> b.array(generators.stream(), useNewlines);
  }

  public static ISourceGenerator<IExpressionBuilder<?>> createAnnotationElementGenerator(IAnnotationElement ae, IWorkingCopyTransformer transformer) {
    if (ae.isDefault()) {
      return empty();
    }
    return b -> b.append(ae.elementName()).equalSign().append(ae.value().toWorkingCopy(transformer).generalize(ExpressionBuilder::create));
  }

  public static ITypeGenerator<?> createUnresolvedTypeGenerator(IUnresolvedType ut, IWorkingCopyTransformer transformer) {
    return ut.type()
        .<ITypeGenerator<?>> map(t -> TypeGenerator.create(t, transformer))
        .orElseGet(() -> TypeGenerator.create()
            .setDeclaringFullyQualifiedName(JavaTypes.qualifier(ut.name().replace(JavaTypes.C_DOLLAR, JavaTypes.C_DOT)))
            .withElementName(ut.elementName()));
  }
}
