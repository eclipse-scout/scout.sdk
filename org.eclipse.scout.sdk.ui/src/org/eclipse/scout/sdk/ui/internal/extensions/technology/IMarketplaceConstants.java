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

  final static String SCOUT_JDBC_FEATURE_URL = "https://tools.bsiag.com/marketplace/jdbc/3.8";
  final static String SCOUT_RAYO_FEATURE_URL = "https://tools.bsiag.com/marketplace/rayo/3.8";

  final static String SCOUT_MYSQL_JDBC_FEATURE = "com.bsiag.scout.rt.server.jdbc.mysql5117.feature.feature.group";
  final static String MY_SQL_JDBC_PLUGIN = "com.bsiag.scout.rt.server.jdbc.mysql5117";
  final static String MY_SQL_JDBC_FRAGMENT = "com.mysql.jdbc_5117.fragment";

  final static String SCOUT_ORACLE_JDBC_FEATURE = "com.bsiag.scout.rt.server.jdbc.oracle11g2.feature.feature.group";
  final static String ORACLE_JDBC_PLUGIN = "com.bsiag.scout.rt.server.jdbc.oracle11g2";
  final static String ORACLE_JDBC_FRAGMENT = "com.oracle.oracle11g.jdbc.fragment";

  final static String SCOUT_POSTGRES_JDBC_FEATURE = "com.bsiag.scout.rt.server.jdbc.postgres9.feature.feature.group";
  final static String POSTGRES_JDBC_PLUGIN = "com.bsiag.scout.rt.server.jdbc.postgres9";
  final static String POSTGRES_JDBC_FRAGMENT = "org.postgres.postgres9.jdbc.fragment";

  final static String DERBY_JDBC_PLUGIN = "org.eclipse.scout.rt.jdbc.derby";
  final static String DERBY_JDBC_FRAGMENT = "org.apache.derby.jdbc_1082.fragment";

  final static String SCOUT_RAYO_LAF_FEATURE = "com.bsiag.scout.rt.ui.swing.laf.rayo.feature.feature.group";
  final static String RAYO_LAF_PLUGIN = "com.bsiag.scout.rt.ui.swing.rayo";
  final static String RAYO_LAF_FRAGMENT = "com.bsiag.scout.rt.ui.swing.laf.rayo.fragment";
}
