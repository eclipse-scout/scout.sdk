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
package org.eclipse.scout.sdk.core.sourcebuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IArrayMetaValue;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.MetaValueType;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.annotation.AnnotationSourceBuilder;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.PropertyMap;

/**
 * <h3>{@link ExpressionSourceBuilderFactory}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 07.03.2013
 */
public final class ExpressionSourceBuilderFactory {
  private ExpressionSourceBuilderFactory() {
  }

  public static ISourceBuilder createClassLiteral(final String signature) {
    return new ISourceBuilder() {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        source.append(validator.useSignature(signature));
        source.append(SuffixConstants.SUFFIX_STRING_class);
      }
    };
  }

  public static ISourceBuilder createEnumValue(final String enumSignature, final String enumField) {
    return new ISourceBuilder() {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        String typeName = validator.useSignature(enumSignature);
        source.append(typeName);
        source.append('.');
        source.append(enumField);
      }
    };
  }

  /**
   * @param elements
   *          the {@link String} array elements (not yet quoted).
   * @param formatWithNewlines
   *          If <code>true</code> each element will be placed on a separate line.
   * @return an array builder that creates a { ... } expression with quoted string names.
   */
  public static ISourceBuilder createQuotedStringArray(final Collection<String> elements, final boolean formatWithNewlines) {
    List<ISourceBuilder> a = new ArrayList<>(elements.size());
    for (final String s : elements) {
      a.add(new ISourceBuilder() {
        @Override
        public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
          source.append(CoreUtils.toStringLiteral(s));
        }
      });
    }
    return createArray(a, formatWithNewlines);
  }

  /**
   * @param elements
   *          The elements of the array
   * @param formatWithNewlines
   *          If <code>true</code> each element will be placed on a separate line.
   * @return an array builder that creates a { ... } expression that can be used for annotation values of type array
   */
  public static ISourceBuilder createArray(final Collection<? extends ISourceBuilder> elements, final boolean formatWithNewlines) {
    return new ISourceBuilder() {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        //use newlines on multi-dimensional arrays and annotation arrays only
        char blockSeparator = (formatWithNewlines ? '\n' : ' ');
        source.append('{');
        source.append(blockSeparator);
        int n = elements.size();
        if (n > 0) {
          int i = 0;
          for (ISourceBuilder element : elements) {
            if (i > 0) {
              source.append(',');
              source.append(blockSeparator);
            }
            element.createSource(source, lineDelimiter, context, validator);
            i++;
          }
          source.append(blockSeparator);
        }
        source.append('}');
      }
    };
  }

  public static ISourceBuilder createFromMetaValue(final IMetaValue metaValue) {
    switch (metaValue.type()) {
      case Null:
        return new RawSourceBuilder("null");
      case Int:
        return new RawSourceBuilder(metaValue.get(Integer.class).toString());
      case Byte:
        return new RawSourceBuilder(metaValue.get(Byte.class).toString());
      case Short:
        return new RawSourceBuilder(metaValue.get(Short.class).toString());
      case Char:
        char ch = metaValue.get(Character.class);
        return new RawSourceBuilder("'" + ch + "'");
      case Float:
        float f = metaValue.get(Float.class);
        return new RawSourceBuilder(f + "f");
      case Double:
        return new RawSourceBuilder(metaValue.get(Double.class).toString());
      case Bool:
        return new RawSourceBuilder(metaValue.get(Boolean.class).toString());
      case Long:
        long l = metaValue.get(Long.class);
        return new RawSourceBuilder(l + "L");
      case String:
        String s = metaValue.get(String.class);
        return new RawSourceBuilder(CoreUtils.toStringLiteral(s));
      case Type:
        IType type = metaValue.get(IType.class);
        return createClassLiteral(SignatureUtils.getTypeSignature(type));
      case Enum:
        IField field = metaValue.get(IField.class);
        return createEnumValue(SignatureUtils.getTypeSignature(field.declaringType()), field.elementName());
      case Annotation:
        IAnnotation a = metaValue.get(IAnnotation.class);
        return new AnnotationSourceBuilder(a);
      case Array:
        IMetaValue[] metaArray = ((IArrayMetaValue) metaValue).metaValueArray();
        int n = metaArray.length;
        //use newlines on multi-dimensional arrays and annotation arrays only
        boolean useNewlines = (n > 0 && (metaArray[0].type() == MetaValueType.Array || metaArray[0].type() == MetaValueType.Annotation));
        List<ISourceBuilder> sourceBuilderList = new ArrayList<>(n);
        for (IMetaValue metaElement : metaArray) {
          sourceBuilderList.add(createFromMetaValue(metaElement));
        }
        return createArray(sourceBuilderList, useNewlines);
      default:
        return new RawSourceBuilder("UNKNOWN(" + metaValue.type() + ", " + metaValue + ")");
    }
  }
}
