/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.sourcebuilder.codetype;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.model.ScoutAnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.ISourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.RawSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.AbstractEntitySourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.field.FieldSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.field.FieldSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.field.IFieldSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.PropertyMap;

/**
 * <h3>{@link CodeTypeSourceBuilder}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class CodeTypeSourceBuilder extends AbstractEntitySourceBuilder {

  private static final String ID_CONSTANT_NAME = "ID";

  private String m_superTypeSignature;
  private String m_codeTypeIdSignature;
  private String m_classIdValue;
  private ISourceBuilder m_idValueBuilder;

  public CodeTypeSourceBuilder(String elementName, String packageName, IJavaEnvironment env) {
    super(elementName, packageName, env);
  }

  @Override
  public void setup() {
    setComment(CommentSourceBuilderFactory.createDefaultCompilationUnitComment(this));

    ITypeSourceBuilder codeTypeBuilder = new TypeSourceBuilder(getEntityName());
    codeTypeBuilder.setComment(CommentSourceBuilderFactory.createDefaultTypeComment(codeTypeBuilder));
    codeTypeBuilder.setFlags(Flags.AccPublic);
    codeTypeBuilder.setSuperTypeSignature(getSuperTypeSignature());

    // class id
    if (StringUtils.isNotBlank(getClassIdValue())) {
      codeTypeBuilder.addAnnotation(ScoutAnnotationSourceBuilderFactory.createClassId(getClassIdValue()));
    }

    codeTypeBuilder.addField(FieldSourceBuilderFactory.createSerialVersionUidBuilder());
    codeTypeBuilder.addField(createId());

    IMethodSourceBuilder getId = MethodSourceBuilderFactory.createOverride(codeTypeBuilder, getJavaEnvironment(), "getId");
    getId.setBody(new RawSourceBuilder("return " + ID_CONSTANT_NAME + ';'));
    codeTypeBuilder.addMethod(getId);

    addType(codeTypeBuilder);
  }

  protected IFieldSourceBuilder createId() {
    IFieldSourceBuilder id = new FieldSourceBuilder(ID_CONSTANT_NAME) {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        super.createSource(source, lineDelimiter, context, validator);
        if (getIdValueBuilder() == null) {
          source.append(CoreUtils.getCommentBlock("set id value"));
        }
      }
    };
    id.setFlags(Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
    id.setSignature(SignatureUtils.unboxToPrimitiveSignature(getCodeTypeIdSignature()));
    if (getIdValueBuilder() != null) {
      id.setValue(getIdValueBuilder());
    }
    else {
      id.setValue(new RawSourceBuilder("null"));
    }
    return id;
  }

  public String getSuperTypeSignature() {
    return m_superTypeSignature;
  }

  public void setSuperTypeSignature(String superTypeSignature) {
    m_superTypeSignature = superTypeSignature;
  }

  public String getCodeTypeIdSignature() {
    return m_codeTypeIdSignature;
  }

  public void setCodeTypeIdSignature(String codeTypeIdSignature) {
    m_codeTypeIdSignature = codeTypeIdSignature;
  }

  public ISourceBuilder getIdValueBuilder() {
    return m_idValueBuilder;
  }

  public void setIdValueBuilder(ISourceBuilder idValueBuilder) {
    m_idValueBuilder = idValueBuilder;
  }

  public String getClassIdValue() {
    return m_classIdValue;
  }

  public void setClassIdValue(String classIdValue) {
    m_classIdValue = classIdValue;
  }
}
