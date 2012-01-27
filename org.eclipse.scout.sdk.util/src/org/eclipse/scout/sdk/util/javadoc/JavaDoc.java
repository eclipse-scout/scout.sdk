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
package org.eclipse.scout.sdk.util.javadoc;

import java.util.ArrayList;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.util.ResourcesUtility;
import org.eclipse.scout.sdk.util.internal.SdkUtilActivator;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.TextEdit;

/**
 *
 */
public class JavaDoc {

  private final IMember m_member;
  private ArrayList<String> m_newLines = new ArrayList<String>();

  public JavaDoc(IMember member) {
    m_member = member;
  }

  public void appendLine(String commentLine) {
    m_newLines.add(commentLine);
  }

  public void removeLine(String commentLine) {
    m_newLines.remove(commentLine);
  }

  protected String[] getNewLines() {
    return m_newLines.toArray(m_newLines.toArray(new String[m_newLines.size()]));
  }

  public TextEdit getEdit() {
    String NL = ResourcesUtility.getLineSeparator(getMember().getCompilationUnit());
    StringBuilder javaDoc = new StringBuilder();
    String[] newLines = getNewLines();
    if (newLines.length > 0) {
      for (String line : newLines) {
        javaDoc.append("* " + line + NL);
      }
      try {
        int insertPosition = -1;
        ISourceRange javadocRange = getMember().getJavadocRange();
        if (javadocRange != null) {
          String existingDoc = getMember().getCompilationUnit().getBuffer().getText(javadocRange.getOffset(), javadocRange.getLength());
          insertPosition = javadocRange.getOffset() + existingDoc.lastIndexOf("*/");
        }
        else {
          insertPosition = getMember().getSourceRange().getOffset();
          javaDoc.insert(0, "/**" + NL);
          javaDoc.append("*/" + NL);
        }
        if (insertPosition > 0) {
          return new InsertEdit(insertPosition, javaDoc.toString());
        }
      }
      catch (JavaModelException e) {
        SdkUtilActivator.logWarning("could not parse java doc of '" + getMember().getElementName() + "' in compilation unit '" + getMember().getCompilationUnit().getElementName() + "'.", e);
      }
    }
    return null;
  }

  public IMember getMember() {
    return m_member;
  }
}
