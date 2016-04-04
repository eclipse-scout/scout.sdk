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
package org.eclipse.scout.sdk.core.s.sourcebuilder.testcase;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.model.ScoutAnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.ISourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.AbstractEntitySourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.PropertyMap;

/**
 * <h3>{@link TestSourceBuilder}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class TestSourceBuilder extends AbstractEntitySourceBuilder {

  private String m_runnerSignature;
  private String m_sessionSignature;
  private ISourceBuilder m_runWithSubjectValueBuilder;
  private boolean m_isClientTest;

  public TestSourceBuilder(String elementName, String packageName, IJavaEnvironment env) {
    super(elementName, packageName, env);
  }

  @Override
  public void setup() {
    setComment(CommentSourceBuilderFactory.createDefaultCompilationUnitComment(this));

    ITypeSourceBuilder testBuilder = new TypeSourceBuilder(getEntityName());
    testBuilder.setFlags(Flags.AccPublic);

    // add dummy comment type
    testBuilder.addType(new TypeSourceBuilder("") {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        source.append(CoreUtils.getCommentBlock("add test cases"));
      }
    });

    // @RunWith
    String runnerSignature = getRunnerSignature();
    if (StringUtils.isNotBlank(runnerSignature)) {
      testBuilder.addAnnotation(ScoutAnnotationSourceBuilderFactory.createRunWith(runnerSignature));
    }

    // @RunWithSubject
    testBuilder.addAnnotation(ScoutAnnotationSourceBuilderFactory.createRunWithSubject(getRunWithSubjectValueBuilder()));

    // @RunWithSession
    String sessionSig = getSessionSignature();
    if (isClientTest()) {
      testBuilder.addAnnotation(ScoutAnnotationSourceBuilderFactory.createRunWithClientSession(sessionSig));
    }
    else if (StringUtils.isNotBlank(sessionSig)) {
      testBuilder.addAnnotation(ScoutAnnotationSourceBuilderFactory.createRunWithServerSession(sessionSig));
    }

    addType(testBuilder);
  }

  public String getRunnerSignature() {
    return m_runnerSignature;
  }

  public void setRunnerSignature(String runnerSignature) {
    m_runnerSignature = runnerSignature;
  }

  public String getSessionSignature() {
    return m_sessionSignature;
  }

  public void setSessionSignature(String sessionSignature) {
    m_sessionSignature = sessionSignature;
  }

  public boolean isClientTest() {
    return m_isClientTest;
  }

  public void setClientTest(boolean isClientTest) {
    m_isClientTest = isClientTest;
  }

  public ISourceBuilder getRunWithSubjectValueBuilder() {
    return m_runWithSubjectValueBuilder;
  }

  /**
   * Sets an optional {@link ISourceBuilder} for creating the value of the @RunWithSubject annotation value.
   *
   * @param runWithSubjectValueBuilder
   *          the builder or <code>null</code>.
   */
  public void setRunWithSubjectValueBuilder(ISourceBuilder runWithSubjectValueBuilder) {
    m_runWithSubjectValueBuilder = runWithSubjectValueBuilder;
  }
}
