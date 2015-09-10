/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
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
        String typeName = SignatureUtils.useSignature(signature, validator);
        source.append(typeName + ".class");
      }
    };
  }

  public static ISourceBuilder createEnumValue(final String enumSignature, final String enumField) {
    return new ISourceBuilder() {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        String typeName = SignatureUtils.useSignature(enumSignature, validator);
        source.append(typeName + "." + enumField);
      }
    };
  }

  /**
   * @param elements
   *          (string names not yet quoted)
   * @param formatWithNewlines
   * @return an array builder that creates a { ... } expression with quoted string names
   */
  public static ISourceBuilder createQuotedStringArray(final Collection<String> elements, final boolean formatWithNewlines) {
    ArrayList<ISourceBuilder> a = new ArrayList<>(elements.size());
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
   * @param formatWithNewlines
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
    switch (metaValue.getType()) {
      case Null:
        return new RawSourceBuilder("null");
      case Int:
        return new RawSourceBuilder(metaValue.getObject(Integer.class).toString());
      case Byte:
        return new RawSourceBuilder(metaValue.getObject(Byte.class).toString());
      case Short:
        return new RawSourceBuilder(metaValue.getObject(Short.class).toString());
      case Char:
        char ch = metaValue.getObject(Character.class);
        return new RawSourceBuilder("'" + ch + "'");
      case Float:
        float f = metaValue.getObject(Float.class);
        return new RawSourceBuilder(f + "f");
      case Double:
        return new RawSourceBuilder(metaValue.getObject(Double.class).toString());
      case Bool:
        return new RawSourceBuilder(metaValue.getObject(Boolean.class).toString());
      case Long:
        long l = metaValue.getObject(Long.class);
        return new RawSourceBuilder(l + "L");
      case String:
        String s = metaValue.getObject(String.class);
        return new RawSourceBuilder(CoreUtils.toStringLiteral(s));
      case Type:
        IType type = metaValue.getObject(IType.class);
        return createClassLiteral(SignatureUtils.getTypeSignature(type));
      case Enum:
        IField field = metaValue.getObject(IField.class);
        return createEnumValue(SignatureUtils.getTypeSignature(field.getDeclaringType()), field.getElementName());
      case Annotation:
        IAnnotation a = metaValue.getObject(IAnnotation.class);
        return new AnnotationSourceBuilder(a);
      case Array:
        IMetaValue[] metaArray = ((IArrayMetaValue) metaValue).getMetaValueArray();
        int n = metaArray.length;
        //use newlines on multi-dimensional arrays and annotation arrays only
        boolean useNewlines = (n > 0 && (metaArray[0].getType() == MetaValueType.Array || metaArray[0].getType() == MetaValueType.Annotation));
        ArrayList<ISourceBuilder> sourceBuilderList = new ArrayList<>(n);
        for (IMetaValue metaElement : metaArray) {
          sourceBuilderList.add(createFromMetaValue(metaElement));
        }
        return createArray(sourceBuilderList, useNewlines);
      default:
        return new RawSourceBuilder("UNKNOWN(" + metaValue.getType() + ", " + metaValue + ")");
    }
  }
}
