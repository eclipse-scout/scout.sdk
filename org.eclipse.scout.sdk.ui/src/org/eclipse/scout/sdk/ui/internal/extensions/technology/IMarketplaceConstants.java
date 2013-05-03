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
package org.eclipse.scout.sdk.ui.internal.extensions.technology;

/**
 * <h3>{@link IMarketplaceConstants}</h3> ...
 * 
 * @author mvi
 * @since 3.8.0 15.02.2012
 */
public interface IMarketplaceConstants {

  // db drivers
  String SCOUT_JDBC_FEATURE_URL = "http://tools.bsiag.com/marketplace/jdbc/3.9";
  String SCOUT_MYSQL_JDBC_FEATURE = "com.bsiag.scout.rt.server.jdbc.mysql5117.source.feature.group";
  String MY_SQL_JDBC_PLUGIN = "com.bsiag.scout.rt.server.jdbc.mysql5117";
  String MY_SQL_JDBC_FRAGMENT = "com.mysql.jdbc_5117.fragment";
  String SCOUT_ORACLE_JDBC_FEATURE = "com.bsiag.scout.rt.server.jdbc.oracle11g2.source.feature.group";
  String ORACLE_JDBC_PLUGIN = "com.bsiag.scout.rt.server.jdbc.oracle11g2";
  String ORACLE_JDBC_FRAGMENT = "com.oracle.oracle11g.jdbc.fragment";
  String SCOUT_POSTGRES_JDBC_FEATURE = "com.bsiag.scout.rt.server.jdbc.postgres9.source.feature.group";
  String POSTGRES_JDBC_PLUGIN = "com.bsiag.scout.rt.server.jdbc.postgres9";
  String POSTGRES_JDBC_FRAGMENT = "org.postgres.postgres9.jdbc.fragment";
  String DERBY_JDBC_PLUGIN = "org.eclipse.scout.rt.jdbc.derby";
  String DERBY_JDBC_FRAGMENT = "org.apache.derby.jdbc_1091.fragment";

  // rayo swing look and feel
  String SCOUT_RAYO_FEATURE_URL = "http://tools.bsiag.com/marketplace/rayo/3.9";
  String SCOUT_RAYO_LAF_FEATURE = "com.bsiag.scout.rt.ui.swing.laf.rayo.source.feature.group";
  String RAYO_LAF_PLUGIN = "com.bsiag.scout.rt.ui.swing.rayo";
  String RAYO_LAF_FRAGMENT = "com.bsiag.scout.rt.ui.swing.laf.rayo.fragment";

  // docx4j support
  String SCOUT_DOCX4J_FEATURE_URL = "http://tools.bsiag.com/marketplace/docx4j/3.9";
  String DOCX4J_FEATURE = "org.eclipse.scout.docx4j.source.feature.group";
  String DOCX4J_PLUGIN = "com.bsiag.org.docx4j";
  String DOCX4J_SCOUT_PLUGIN = "org.eclipse.scout.docx4j";
  String DOCX4J_SCOUT_CLIENT_PLUGIN = "org.eclipse.scout.rt.docx4j.client";
  String DOCX4J_SDK_PLUGIN = "org.eclipse.scout.sdk.docx4j";
  String DOCX4J_SDK_FEATURE = "org.eclipse.scout.sdk.docx4j.source.feature.group";

  // logging bridge
  String SCOUT_LOGGING_BRIDGE_FEATURE_URL = "http://tools.bsiag.com/marketplace/logging_bridge/3.9";
  String LOGGING_BRIDGE_FEATURE = "org.eclipse.scout.logging.bridges.source.feature.group";
  String LOGGING_BRIDGE_LOG4J_FRAGMENT = "org.eclipse.scout.commons.log4j.bridge.fragment";
}
