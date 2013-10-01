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
package org.eclipse.scout.sdk.util;

import java.util.regex.Pattern;

public interface Regex {
  Pattern REGEX_WELLFORMD_JAVAFIELD = Pattern.compile("\\b[A-Z][a-zA-Z0-9_]{0,200}\\b");
  Pattern REGEX_WELLFORMED_PROPERTY = Pattern.compile("\\b[a-zA-Z0-9_]{0,200}\\b");
  Pattern REGEX_JAVAFIELD = Pattern.compile("\\b[A-Za-z][a-zA-Z0-9_]{0,200}\\b");
  Pattern REGEX_PROPERTY_METHOD_TRIM = Pattern.compile("^getConfigured(.*)$");
  Pattern REGEX_METHOD_NEW_TYPE_OCCURRENCES = Pattern.compile("\\s*new\\s*([^\\(]*)\\([^\\)]*\\)\\s*", Pattern.DOTALL);
  Pattern REGEX_METHOD_DEFINITION = Pattern.compile("[ \\t]*(public|protected|private)?\\s*(static)?\\s*(void|[^\\s]*)\\s*[^\\s\\(]*\\s*\\([^\\)]*\\)\\s*\\{", Pattern.DOTALL);
  Pattern REGEX_FIELD_DECLARATION = Pattern.compile("\\s*(@Deprecated){0,1}\\s*(public|private|protected)?\\s*(final\\s*|static\\s*|transient\\s*|volatile\\s*){0,4}([a-zA-Z0-9_]*(<([a-zA-Z0-9_]|(,\\s*))*>)?)\\s*([a-zA-Z0-9_]*)(\\s*=\\s*(.*))?\\s*;.*", Pattern.DOTALL);
  Pattern REGEX_SEMI_COLOLN_END = Pattern.compile("\\;$");
}
