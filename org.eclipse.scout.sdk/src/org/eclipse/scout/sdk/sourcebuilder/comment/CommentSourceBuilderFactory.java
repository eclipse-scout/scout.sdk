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
package org.eclipse.scout.sdk.sourcebuilder.comment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.sourcebuilder.ICommentSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.ISourceBuilder;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * <h3>{@link CommentSourceBuilderFactory}</h3> ...
 * 
 * @author aho
 * @since 3.10.0 07.03.2013
 */
public final class CommentSourceBuilderFactory {
  private static IJavaElementCommentBuilderService javaElementCommentBuilderService;
  private final static ICommentSourceBuilder emptyCommentSourceBuilder = new ICommentSourceBuilder() {
    @Override
    public void createSource(ISourceBuilder sourceBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
    }
  };

  static {
    BundleContext context = ScoutSdk.getDefault().getBundle().getBundleContext();
    ServiceReference<IJavaElementCommentBuilderService> reference = context.getServiceReference(IJavaElementCommentBuilderService.class);
    try {
      if (reference != null) {
        IJavaElementCommentBuilderService service = context.getService(reference);
        if (service != null) {
          javaElementCommentBuilderService = service;
        }
        else {
          ScoutSdk.logWarning("No valid java element comment builder service has been registered.");
        }
      }
      else {
        ScoutSdk.logWarning("No java element comment builder service has been registered.");
      }
    }
    finally {
      context.ungetService(reference);
    }
  }

  public static final ICommentSourceBuilder createPreferencesCompilationUnitCommentBuilder() {
    if (javaElementCommentBuilderService != null) {
      return javaElementCommentBuilderService.createCompilationUnitCommentBuilder();
    }
    else {
      return emptyCommentSourceBuilder;
    }
  }

  public static ICommentSourceBuilder createPreferencesMethodOverrideComment(String interfaceFqn) {
    if (javaElementCommentBuilderService != null) {
      return javaElementCommentBuilderService.createPreferencesMethodOverrideComment(interfaceFqn);
    }
    else {
      return emptyCommentSourceBuilder;
    }
  }

  public static ICommentSourceBuilder createPreferencesTypeCommentBuilder() {
    if (javaElementCommentBuilderService != null) {
      return javaElementCommentBuilderService.createPreferencesTypeCommentBuilder();
    }
    else {
      return emptyCommentSourceBuilder;
    }
  }

  public static ICommentSourceBuilder createPreferencesMethodCommentBuilder() {
    if (javaElementCommentBuilderService != null) {
      return javaElementCommentBuilderService.createPreferencesMethodCommentBuilder();
    }
    else {
      return emptyCommentSourceBuilder;
    }
  }

  public static final ICommentSourceBuilder createPreferencesFieldCommentBuilder() {

    if (javaElementCommentBuilderService != null) {
      return javaElementCommentBuilderService.createPreferencesFieldCommentBuilder();
    }
    else {
      return emptyCommentSourceBuilder;
    }
  }

  /**
   * @param comment
   * @return
   */
  public static ICommentSourceBuilder createCustomCommentBuilder(final String comment) {
    return new ICommentSourceBuilder() {

      public void createSource1(ISourceBuilder sourceBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        // normalize comment
        StringBuilder commentBuilder = new StringBuilder();
        BufferedReader inputReader = new BufferedReader(new StringReader(comment));
        try {
          // first line
          String lastLine;
          String line = inputReader.readLine();
          if (line.matches("^s*\\/\\*\\*s*$")) {
            commentBuilder.append(line);
          }
          else {
            commentBuilder.append("/** ").append(lineDelimiter).append(line);
          }
          lastLine = line;
          line = inputReader.readLine();
          while (line != null) {
            commentBuilder.append(lineDelimiter);
            if (line.matches("^s*\\*.*$")) {
              commentBuilder.append(line);
            }
            else {
              commentBuilder.append("* ").append(line);
            }
            lastLine = line;
            line = inputReader.readLine();
          }
          if (!lastLine.matches("\\*\\/s*$")) {
            commentBuilder.append(lineDelimiter);
            commentBuilder.append("*/");
          }
          String formattedComment = commentBuilder.toString();
          source.append(formattedComment);
        }
        catch (IOException ex) {
          ScoutSdk.logError("could not read commment '" + comment + "'.", ex);
        }
        finally {
          try {
            inputReader.close();
          }
          catch (IOException e) {
            // void here
          }
        }

      }

      @Override
      public void createSource(ISourceBuilder sourceBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        // normalize comment
        StringBuilder commentBuilder = new StringBuilder();
        BufferedReader inputReader = new BufferedReader(new StringReader(comment));
        try {
          commentBuilder.append("/**").append(lineDelimiter);
          String line = inputReader.readLine();
          while (line != null) {
            if (line.matches("^s*\\/\\*\\*s*$")) {
              line = inputReader.readLine();
            }
            else if (line.matches("^s*\\*\\*\\/s*$")) {
              line = inputReader.readLine();
            }
            else {
              if (line.matches("^s*\\*.*$")) {
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
        catch (IOException ex) {
          ScoutSdk.logError("could not read commment '" + comment + "'.", ex);
        }
        finally {
          try {
            inputReader.close();
          }
          catch (IOException e) {
            // void here
          }
        }

      }
    };
  }

}
