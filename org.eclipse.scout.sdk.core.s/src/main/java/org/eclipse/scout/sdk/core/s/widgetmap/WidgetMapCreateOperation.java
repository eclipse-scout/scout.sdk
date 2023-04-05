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
  private List<CharSequence> m_classSources;
  private Map<String /* widgetMap declaration field name */, CharSequence /* field declaration source */> m_declarationSources;
  private List<IES6Class> m_importsForModel;
  private List<String> m_importNamesForDeclarations;

  public void execute() {
    validateOperation();
    // TODO FSH: add real implementation and remove dummy impl below

    // Dummy implementation
    setImportsForModel(literal().containingModule().classes().limit(2).toList());
    setImportNamesForDeclarations(List.of("GeneratedWidgetMap", "GeneratedWidgetMap2"));
    setDeclarationSources(Map.of("widgetMap", "declare widgetMap: GeneratedWidgetMap2;"));
    setClassSources(List.of(
        """
            export type GeneratedWidgetMap = {
              // TODO
            };""".stripIndent(),
        """
            export type GeneratedWidgetMap2 = {
              // TODO 2
            };""".stripIndent()));
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
