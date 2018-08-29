/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.internal.template;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.jdt.internal.corext.fix.LinkedProposalModel;
import org.eclipse.jdt.internal.corext.fix.LinkedProposalPositionGroup;
import org.eclipse.scout.sdk.core.util.SdkLog;

/**
 * <h3>{@link PositionInformationBridge}</h3> This class is used to bridge the
 *
 * <pre>
 * org.eclipse.jdt.internal.corext.fix.LinkedProposalPositionGroup.PositionInformation
 * </pre>
 *
 * to
 *
 * <pre>
 * org.eclipse.jdt.internal.corext.fix.LinkedProposalPositionGroupCore.PositionInformation
 * </pre>
 *
 * The API of JDT did change with release Photon SR1 M2. Once Photon is not supported anymore this class can be removed.
 */
@SuppressWarnings("squid:S1166")
public final class PositionInformationBridge {

  private static final String POSITION_INFORMATION_FQN = "org.eclipse.jdt.internal.corext.fix.LinkedProposalPositionGroupCore$PositionInformation";
  private static final String POSITION_INFORMATION_FQN_OLD = "org.eclipse.jdt.internal.corext.fix.LinkedProposalPositionGroup$PositionInformation";

  private static final String LINKED_POSITION_GROUP_CORE_FQN = "org.eclipse.jdt.internal.corext.fix.LinkedProposalPositionGroupCore";
  private static final String LINKED_POSITION_GROUP_FQN = "org.eclipse.jdt.internal.corext.fix.LinkedProposalPositionGroup";

  private static String[] fqnOrder = {POSITION_INFORMATION_FQN_OLD, POSITION_INFORMATION_FQN};

  private PositionInformationBridge() {
  }

  @SuppressWarnings("squid:S1168")
  public static Object[] getPositions(LinkedProposalPositionGroup group) {
    // call with reflection
    try {
      Method method = LinkedProposalPositionGroup.class.getMethod("getPositions");
      method.setAccessible(true);
      return (Object[]) method.invoke(group);
    }
    catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      SdkLog.error(e);
      return null;
    }
  }

  public static void addPosition(LinkedAsyncProposalPositionGroup group, Object position) {
    try {
      Method method = LinkedProposalPositionGroup.class.getMethod("addPosition", findPostionInformationClass());
      method.setAccessible(true);
      method.invoke(group, position);
    }
    catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassNotFoundException e) {
      SdkLog.error(e);
    }
  }

  private static Class<?> findPostionInformationClass() throws ClassNotFoundException {
    Class<?> positionInformationClazz = null;
    try {
      positionInformationClazz = Class.forName(fqnOrder[0]);
      return positionInformationClazz;
    }
    catch (ClassNotFoundException e) {
      // try second
    }
    positionInformationClazz = Class.forName(fqnOrder[1]);
    // switch
    String[] newOrder = {fqnOrder[1], fqnOrder[0]};
    fqnOrder = newOrder;
    return positionInformationClazz;
  }

  public static Object getEndPosition(LinkedProposalModel model) {
    // call with reflection
    try {
      Method method = LinkedProposalModel.class.getMethod("getEndPosition");
      method.setAccessible(true);
      return method.invoke(model);
    }
    catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      SdkLog.error(e);
      return null;
    }
  }

  public static int getOffset(Object positionInformation) {
    // call with reflection
    try {
      Method method = positionInformation.getClass().getMethod("getOffset");
      method.setAccessible(true);
      Object resultRaw = method.invoke(positionInformation);
      return ((Integer) resultRaw).intValue();
    }
    catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      SdkLog.error(e);
      return -1;
    }
  }

  public static int getLength(Object positionInformation) {
    // call with reflection
    try {
      Method method = positionInformation.getClass().getMethod("getLength");
      method.setAccessible(true);
      Object resultRaw = method.invoke(positionInformation);
      return ((Integer) resultRaw).intValue();
    }
    catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      SdkLog.error(e);
      return -1;
    }
  }

  public static int getSequenceRank(Object positionInformation) {
    // call with reflection
    try {
      Method method = positionInformation.getClass().getMethod("getSequenceRank");
      method.setAccessible(true);
      Object resultRaw = method.invoke(positionInformation);
      return ((Integer) resultRaw).intValue();
    }
    catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      SdkLog.error(e);
      return -1;
    }

  }

  @SuppressWarnings("squid:S1141")
  public static void addPositionGroup(LinkedProposalModel linkedProposalModel, LinkedProposalPositionGroup group) {
    Method method = null;
    try {
      try {
        // step 1
        method = linkedProposalModel.getClass().getMethod("addPositionGroup", Class.forName(LINKED_POSITION_GROUP_FQN));
      }
      catch (NoSuchMethodException | SecurityException | ClassNotFoundException e) {
        // step 2
        method = linkedProposalModel.getClass().getMethod("addPositionGroup", Class.forName(LINKED_POSITION_GROUP_CORE_FQN));
      }
      method.setAccessible(true);
      method.invoke(linkedProposalModel, group);
    }
    catch (NoSuchMethodException | SecurityException | ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      SdkLog.error(e);
    }
  }

}
