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
package org.eclipse.scout.sdk.core.sourcebuilder.comment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Pattern;

import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.sourcebuilder.ISourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.ICompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.field.IFieldSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.core.util.PropertyMap;
import org.eclipse.scout.sdk.core.util.SdkException;

/**
 * <h3>{@link CommentSourceBuilderFactory}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 2013-03-07
 */
public final class CommentSourceBuilderFactory {

  private static volatile ICommentSourceBuilderSpi commentSourceBuilderSpi;

  private static final Pattern REGEX_COMMENT_PATTERN1 = Pattern.compile("^s*\\/\\*\\*s*$");
  private static final Pattern REGEX_COMMENT_PATTERN2 = Pattern.compile("^s*\\*\\*\\/s*$");
  private static final Pattern REGEX_COMMENT_PATTERN3 = Pattern.compile("^s*\\*.*$");

  private CommentSourceBuilderFactory() {
  }

  private static final ISourceBuilder EMPTY_COMMENT_SOURCE_BUILDER = new ISourceBuilder() {
    @Override
    public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
      // must be empty
    }
  };

  public static ISourceBuilder createDefaultCompilationUnitComment(ICompilationUnitSourceBuilder target) {
    if (getCommentSourceBuilderSpi() != null) {
      return getCommentSourceBuilderSpi().createCompilationUnitComment(target);
    }
    return EMPTY_COMMENT_SOURCE_BUILDER;
  }

  public static ISourceBuilder createDefaultTypeComment(ITypeSourceBuilder target) {
    if (getCommentSourceBuilderSpi() != null) {
      return getCommentSourceBuilderSpi().createTypeComment(target);
    }
    return EMPTY_COMMENT_SOURCE_BUILDER;
  }

  public static ISourceBuilder createDefaultMethodComment(IMethodSourceBuilder target) {
    if (getCommentSourceBuilderSpi() != null) {
      return getCommentSourceBuilderSpi().createMethodComment(target);
    }
    return EMPTY_COMMENT_SOURCE_BUILDER;
  }

  public static ISourceBuilder createDefaultOverrideMethodComment(IMethodSourceBuilder target, String interfaceFqn) {
    if (getCommentSourceBuilderSpi() != null) {
      return getCommentSourceBuilderSpi().createOverrideMethodComment(target, interfaceFqn);
    }
    return EMPTY_COMMENT_SOURCE_BUILDER;
  }

  public static ISourceBuilder createDefaultGetterMethodComment(IMethodSourceBuilder target) {
    if (getCommentSourceBuilderSpi() != null) {
      return getCommentSourceBuilderSpi().createGetterMethodComment(target);
    }
    return EMPTY_COMMENT_SOURCE_BUILDER;
  }

  public static ISourceBuilder createDefaultSetterMethodComment(IMethodSourceBuilder target) {
    if (getCommentSourceBuilderSpi() != null) {
      return getCommentSourceBuilderSpi().createSetterMethodComment(target);
    }
    return EMPTY_COMMENT_SOURCE_BUILDER;
  }

  public static ISourceBuilder createDefaultFieldComment(IFieldSourceBuilder target) {
    if (getCommentSourceBuilderSpi() != null) {
      return getCommentSourceBuilderSpi().createFieldComment(target);
    }
    return EMPTY_COMMENT_SOURCE_BUILDER;
  }

  /**
   * @param comment
   *          without / ** and * /
   * @return
   */
  public static ISourceBuilder createCustomCommentBuilder(final String comment) {
    return new ISourceBuilder() {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        // normalize comment
        StringBuilder commentBuilder = new StringBuilder();
        try (BufferedReader inputReader = new BufferedReader(new StringReader(comment))) {
          commentBuilder.append("/**").append(lineDelimiter);
          String line = inputReader.readLine();
          while (line != null) {
            if (REGEX_COMMENT_PATTERN1.matcher(line).matches() || REGEX_COMMENT_PATTERN2.matcher(line).matches()) {
              line = inputReader.readLine();
            }
            else {
              if (REGEX_COMMENT_PATTERN3.matcher(line).matches()) {
                commentBuilder.append(line);
              }
              else {
                commentBuilder.append("* ").append(line);
              }
              commentBuilder.append(lineDelimiter);
              line = inputReader.readLine();
            }
          }
          commentBuilder.append("*/");
          String formattedComment = commentBuilder.toString();
          source.append(formattedComment);
        }
        catch (IOException e) {
          throw new SdkException("Unable to format comment.", e);
        }
      }
    };
  }

  public static ICommentSourceBuilderSpi getCommentSourceBuilderSpi() {
    return commentSourceBuilderSpi;
  }

  public static void setCommentSourceBuilderSpi(ICommentSourceBuilderSpi commentSourceBuilderSpi) {
    CommentSourceBuilderFactory.commentSourceBuilderSpi = commentSourceBuilderSpi;
  }

}
