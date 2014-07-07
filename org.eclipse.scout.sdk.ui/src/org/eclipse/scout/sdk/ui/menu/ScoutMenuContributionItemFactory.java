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
package org.eclipse.scout.sdk.ui.menu;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.internal.expressions.TestExpression;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.extensions.ContextMenuContributorExtensionPoint;
import org.eclipse.swt.SWT;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.keys.BindingService;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;

@SuppressWarnings("restriction")
public class ScoutMenuContributionItemFactory extends ExtensionContributionFactory {

  private static final Map<String, KeyBinding> USED_BINDINGS = new HashMap<String, KeyBinding>();
  private static final Map<String, IHandlerActivation> USED_ACTIVATIONS = new HashMap<String, IHandlerActivation>();

  @Override
  public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {

    Map<IScoutHandler.Category, ArrayList<IScoutHandler>> sorted = ContextMenuContributorExtensionPoint.getAllRegisteredContextMenusByCategory();

    for (Entry<IScoutHandler.Category, ArrayList<IScoutHandler>> entry : sorted.entrySet()) {
      for (IScoutHandler a : entry.getValue()) {
        Command cmd = getCommand(serviceLocator, a);

        CommandContributionItemParameter p = new CommandContributionItemParameter(serviceLocator, cmd.getId(), cmd.getId(), SWT.PUSH);
        p.label = a.getLabel();
        p.icon = a.getImage();
        p.tooltip = a.getToolTip();

        CommandContributionItem item = new CommandContributionItem(p);

        Object[] args = new Object[]{a.getClass(), cmd, serviceLocator};
        TestExpression e = new TestExpression("org.eclipse.scout.sdk.ui.menu", "menuVisibilityTester", args, Boolean.TRUE);
        additions.addContributionItem(item, e);
      }
      additions.addContributionItem(new Separator(entry.getKey().getId()), null);
    }
  }

  public static IScoutHandler getMenuInstance(Class<? extends IScoutHandler> type) {
    try {
      Constructor<? extends IScoutHandler> c = type.getConstructor(new Class[]{});
      IScoutHandler ret = c.newInstance();
      return ret;
    }
    catch (Exception e) {
      ScoutSdkUi.logWarning(e);
    }
    return null;
  }

  private Category getCategory(IServiceLocator serviceLocator, String categoryId) {
    ICommandService cs = (ICommandService) serviceLocator.getService(ICommandService.class);
    Category cat = cs.getCategory(categoryId);
    if (!cat.isDefined()) {
      cat.define("", "");
    }
    return cat;
  }

  public static synchronized void registerKeyStroke(String keyStroke, Command cmd) {
    if (keyStroke != null) {
      try {
        BindingService bs = (BindingService) Workbench.getInstance().getService(IBindingService.class);

        KeyBinding oldBinding = USED_BINDINGS.remove(keyStroke);
        if (oldBinding != null) {
          bs.removeBinding(oldBinding);
        }

        ParameterizedCommand paramCmd = new ParameterizedCommand(cmd, null);
        KeyBinding kb = new KeyBinding(KeySequence.getInstance(keyStroke), paramCmd,
            "org.eclipse.ui.defaultAcceleratorConfiguration",
            "org.eclipse.scout.sdk.explorer.context", null, null, null, Binding.USER);
        bs.addBinding(kb);
        USED_BINDINGS.put(keyStroke, kb);
      }
      catch (ParseException e) {
        ScoutSdkUi.logError(e);
      }
    }
  }

  public static synchronized void activateHandler(IServiceLocator serviceLocator, IScoutHandler h) {
    if (h != null) {
      IHandlerService hs = (IHandlerService) serviceLocator.getService(IHandlerService.class);
      IHandlerActivation existingActivation = USED_ACTIVATIONS.remove(h.getId());
      if (existingActivation != null) {
        hs.deactivateHandler(existingActivation);
      }
      IHandlerActivation ha = hs.activateHandler(h.getId(), h);
      USED_ACTIVATIONS.put(h.getId(), ha);
    }
  }

  private Command getCommand(IServiceLocator serviceLocator, IScoutHandler action) {
    ICommandService cs = (ICommandService) serviceLocator.getService(ICommandService.class);
    Command cmd = cs.getCommand(action.getId());
    if (!cmd.isDefined()) {
      activateHandler(serviceLocator, action);

      Category cat = null;
      if (action.getCategory() == null) {
        cat = cs.getCategory(ICommandService.AUTOGENERATED_CATEGORY_ID);
      }
      else {
        cat = getCategory(serviceLocator, action.getCategory().getId());
      }
      cmd.define(action.getId(), null, cat);
    }
    return cmd;
  }
}
