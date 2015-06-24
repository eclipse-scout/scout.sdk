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
package org.eclipse.scout.sdk.core.sourcebuilder.comment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Pattern;

import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.sourcebuilder.ICommentSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.ISourceBuilder;
import org.eclipse.scout.sdk.core.util.PropertyMap;

/**
 * <h3>{@link CommentSourceBuilderFactory}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 07.03.2013
 */
public final class CommentSourceBuilderFactory {

  public static volatile IJavaElementCommentBuilder javaElementCommentBuilder;

  private static final Pattern REGEX_COMMENT_PATTERN1 = Pattern.compile("^s*\\/\\*\\*s*$");
  private static final Pattern REGEX_COMMENT_PATTERN2 = Pattern.compile("^s*\\*\\*\\/s*$");
  private static final Pattern REGEX_COMMENT_PATTERN3 = Pattern.compile("^s*\\*.*$");

  private CommentSourceBuilderFactory() {
  }

  private static final ICommentSourceBuilder EMPTY_COMMENT_SOURCE_BUILDER = new ICommentSourceBuilder() {
    @Override
    public void createSource(ISourceBuilder sourceBuilder, StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
    }
  };

  public static ICommentSourceBuilder createPreferencesCompilationUnitCommentBuilder() {
    if (javaElementCommentBuilder != null) {
      return javaElementCommentBuilder.createPreferencesCompilationUnitCommentBuilder();
    }
    return EMPTY_COMMENT_SOURCE_BUILDER;
  }

  public static ICommentSourceBuilder createPreferencesMethodOverrideComment(String interfaceFqn) {
    if (javaElementCommentBuilder != null) {
      return javaElementCommentBuilder.createPreferencesMethodOverrideComment(interfaceFqn);
    }
    return EMPTY_COMMENT_SOURCE_BUILDER;
  }

  public static ICommentSourceBuilder createPreferencesTypeCommentBuilder() {
    if (javaElementCommentBuilder != null) {
      return javaElementCommentBuilder.createPreferencesTypeCommentBuilder();
    }
    return EMPTY_COMMENT_SOURCE_BUILDER;
  }

  public static ICommentSourceBuilder createPreferencesMethodCommentBuilder() {
    if (javaElementCommentBuilder != null) {
      return javaElementCommentBuilder.createPreferencesMethodCommentBuilder();
    }
    return EMPTY_COMMENT_SOURCE_BUILDER;
  }

  public static ICommentSourceBuilder createPreferencesMethodGetterCommentBuilder() {
    if (javaElementCommentBuilder != null) {
      return javaElementCommentBuilder.createPreferencesMethodGetterCommentBuilder();
    }
    return EMPTY_COMMENT_SOURCE_BUILDER;
  }

  public static ICommentSourceBuilder createPreferencesMethodSetterCommentBuilder() {
    if (javaElementCommentBuilder != null) {
      return javaElementCommentBuilder.createPreferencesMethodSetterCommentBuilder();
    }
    return EMPTY_COMMENT_SOURCE_BUILDER;
  }

  public static ICommentSourceBuilder createPreferencesFieldCommentBuilder() {
    if (javaElementCommentBuilder != null) {
      return javaElementCommentBuilder.createPreferencesFieldCommentBuilder();
    }
    return EMPTY_COMMENT_SOURCE_BUILDER;
  }

  /**
   * @param comment
   * @return
   */
  public static ICommentSourceBuilder createCustomCommentBuilder(final String comment) {
    return new ICommentSourceBuilder() {
      @Override
      public void createSource(ISourceBuilder sourceBuilder, StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        // normalize comment
        StringBuilder commentBuilder = new StringBuilder();
        try (BufferedReader inputReader = new BufferedReader(new StringReader(comment))) {
          commentBuilder.append("/**").append(lineDelimiter);
          String line = inputReader.readLine();
          while (line != null) {
            if (REGEX_COMMENT_PATTERN1.matcher(line).matches()) {
              line = inputReader.readLine();
            }
            else if (REGEX_COMMENT_PATTERN2.matcher(line).matches()) {
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
          throw new RuntimeException("Unable to format comment.", e);
        }
      }
    };
  }
}