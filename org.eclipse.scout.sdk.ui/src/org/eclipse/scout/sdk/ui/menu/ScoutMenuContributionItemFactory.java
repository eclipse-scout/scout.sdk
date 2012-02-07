package org.eclipse.scout.sdk.ui.menu;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

  private static HashMap<String, KeyBinding> m_usedBindings = new HashMap<String, KeyBinding>();
  private static HashMap<String, IHandlerActivation> m_usedActivations = new HashMap<String, IHandlerActivation>();

  @Override
  public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {

    Map<IScoutHandler.Category, ArrayList<IScoutHandler>> sorted = ContextMenuContributorExtensionPoint.getAllRegisteredContextMenusByCategory();

    for (IScoutHandler.Category c : sorted.keySet()) {
      for (IScoutHandler a : sorted.get(c)) {
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
      additions.addContributionItem(new Separator(c.getId()), null);
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

  public synchronized static void registerKeyStroke(String keyStroke, Command cmd) {
    if (keyStroke != null) {
      try {
        BindingService bs = (BindingService) Workbench.getInstance().getService(IBindingService.class);

        KeyBinding oldBinding = m_usedBindings.remove(keyStroke);
        if (oldBinding != null) {
          bs.removeBinding(oldBinding);
        }

        ParameterizedCommand paramCmd = new ParameterizedCommand(cmd, null);
        KeyBinding kb = new KeyBinding(KeySequence.getInstance(keyStroke), paramCmd,
            "org.eclipse.ui.defaultAcceleratorConfiguration",
            "org.eclipse.scout.sdk.explorer.context", null, null, null, Binding.USER);
        bs.addBinding(kb);
        m_usedBindings.put(keyStroke, kb);
      }
      catch (ParseException e) {
        ScoutSdkUi.logError(e);
      }
    }
  }

  public synchronized static void activateHandler(IServiceLocator serviceLocator, IScoutHandler h) {
    if (h != null) {
      IHandlerService hs = (IHandlerService) serviceLocator.getService(IHandlerService.class);
      IHandlerActivation existingActivation = m_usedActivations.remove(h.getId());
      if (existingActivation != null) {
        hs.deactivateHandler(existingActivation);
      }
      IHandlerActivation ha = hs.activateHandler(h.getId(), h);
      m_usedActivations.put(h.getId(), ha);
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
