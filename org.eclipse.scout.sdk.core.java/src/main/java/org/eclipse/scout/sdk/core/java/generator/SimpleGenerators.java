/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.generator;

import static org.eclipse.scout.sdk.core.generator.ISourceGenerator.empty;
import static org.eclipse.scout.sdk.core.generator.ISourceGenerator.raw;
import static org.eclipse.scout.sdk.core.java.imports.ImportCollector.createImportDeclaration;
import static org.eclipse.scout.sdk.core.java.transformer.IWorkingCopyTransformer.transformAnnotation;

import java.util.Arrays;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.builder.expression.ExpressionBuilder;
import org.eclipse.scout.sdk.core.java.builder.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.java.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.java.generator.type.TypeGenerator;
import org.eclipse.scout.sdk.core.java.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.java.model.api.IAnnotationElement;
import org.eclipse.scout.sdk.core.java.model.api.IArrayMetaValue;
import org.eclipse.scout.sdk.core.java.model.api.IField;
import org.eclipse.scout.sdk.core.java.model.api.IImport;
import org.eclipse.scout.sdk.core.java.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.java.model.api.IUnresolvedType;
import org.eclipse.scout.sdk.core.java.model.api.MetaValueType;
import org.eclipse.scout.sdk.core.java.transformer.IWorkingCopyTransformer;

public final class SimpleGenerators {

  private SimpleGenerators() {
  }

  public static ISourceGenerator<ISourceBuilder<?>> createImportGenerator(IImport imp) {
    return raw(createImportDeclaration(imp.isStatic(), imp.name()));
  }

  public static ISourceGenerator<IExpressionBuilder<?>> createMetaValueGenerator(IMetaValue mv, IWorkingCopyTransformer transformer) {
    return switch (mv.type()) {
      case Null -> b -> b.append("null");
      case Int -> b -> b.append(mv.as(Integer.class));
      case Byte -> b -> b.append(mv.as(Byte.class));
      case Short -> b -> b.append(mv.as(Short.class));
      case Char -> b -> b.append('\'')
          .append(mv.as(Character.class))
          .append('\'');
      case Float -> b -> b.append(mv.as(Float.class))
          .append('f');
      case Double -> b -> b.append(mv.as(Double.class));
      case Bool -> b -> b.append(mv.as(Boolean.class));
      case Long -> b -> b.append(mv.as(Long.class))
          .append('L');
      case String -> b -> b.stringLiteral(mv.as(String.class));
      case Type -> b -> b.classLiteral(mv.as(IType.class).reference(true));
      case Enum -> {
        var field = mv.as(IField.class);
        yield b -> b.enumValue(field.requireDeclaringType().name(), field.elementName());
      }
      case Annotation -> transformAnnotation(mv.as(IAnnotation.class), transformer)
          .<ISourceGenerator<IExpressionBuilder<?>>> map(g -> b -> b.append(g))
          .orElseGet(ISourceGenerator::empty);
      case Array -> createArrayMetaValueGenerator((IArrayMetaValue) mv, transformer);
      default -> b -> b.append("UNKNOWN(").append(mv.type().toString()).append(", ").append(mv.toString()).append(')');
    };
  }

  public static ISourceGenerator<IExpressionBuilder<?>> createArrayMetaValueGenerator(IArrayMetaValue mv, IWorkingCopyTransformer transformer) {
    var metaArray = mv.metaValueArray();

    // do not inline this into the generator! the transformation must be executed before the generator is used!
    var generators = Arrays.stream(metaArray)
        .map(m -> createMetaValueGenerator(m, transformer))
        .map(g -> g.generalize(ExpressionBuilder::create))
        .toList();

    // use newlines on multidimensional arrays and annotation arrays only
    var useNewlines = metaArray.length > 0 && (metaArray[0].type() == MetaValueType.Array || metaArray[0].type() == MetaValueType.Annotation);
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
