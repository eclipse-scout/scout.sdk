/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.widgetmap;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

import java.util.List;
import java.util.Map;

import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.IObjectLiteral;
import org.eclipse.scout.sdk.core.util.Ensure;

public class WidgetMapCreateOperation {

  // in 
  private IObjectLiteral m_literal;
  private boolean m_isPage;

  // out
  private List<CharSequence> m_classSources = emptyList();
  private Map<String /* widgetMap declaration field name */, CharSequence /* field declaration source */> m_declarationSources = emptyMap();
  private List<IES6Class> m_importsForModel = emptyList();
  private List<String> m_importNamesForDeclarations = emptyList();

  public void execute() {
    validateOperation();
    // TODO FSH

    setClassSources(List.of(
        "export type GeneratedWidgetMap = {\n" +
            "  // TODO\n" +
            "};",
        "export type GeneratedWidgetMap2 = {\n" +
            "  // TODO 2\n" +
            "};"));
  }

  protected void validateOperation() {
    Ensure.notNull(literal(), "No object-literal provided.");
  }

  public IObjectLiteral literal() {
    return m_literal;
  }

  public void setLiteral(IObjectLiteral literal) {
    m_literal = literal;
  }

  public boolean isPage() {
    return m_isPage;
  }

  public void setPage(boolean page) {
    m_isPage = page;
  }

  public List<CharSequence> classSources() {
    return m_classSources;
  }

  protected void setClassSources(List<CharSequence> classSources) {
    m_classSources = classSources;
  }

  public Map<String, CharSequence> declarationSources() {
    return m_declarationSources;
  }

  protected void setDeclarationSources(Map<String, CharSequence> declarationSources) {
    m_declarationSources = declarationSources;
  }

  public List<IES6Class> importsForModel() {
    return m_importsForModel;
  }

  protected void setImportsForModel(List<IES6Class> importsForModel) {
    m_importsForModel = importsForModel;
  }

  public List<String> importNamesForDeclarations() {
    return m_importNamesForDeclarations;
  }

  protected void setImportNamesForDeclarations(List<String> importNamesForDeclarations) {
    m_importNamesForDeclarations = importNamesForDeclarations;
  }
}
