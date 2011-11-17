package org.eclipse.scout.sdk.ui.menu;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeMap;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.internal.expressions.TestExpression;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.extensions.ContextMenuContributorExtensionPoint;
import org.eclipse.swt.SWT;
import org.eclipse.ui.commands.ICommandService;
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

  @Override
  public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {

    TreeMap<IScoutHandler.Category, ArrayList<AbstractScoutHandler>> sorted =
        new TreeMap<IScoutHandler.Category, ArrayList<AbstractScoutHandler>>(new Comparator<IScoutHandler.Category>() {
          @Override
          public int compare(IScoutHandler.Category o1, IScoutHandler.Category o2) {
            return new Integer(o1.getOrder()).compareTo(o2.getOrder());
          }
        });

    // group and sort all actions by category
    for (AbstractScoutHandler a : ContextMenuContributorExtensionPoint.getAllRegisteredContextMenus()) {
      ArrayList<AbstractScoutHandler> listOfCurCat = sorted.get(a.getCategory());
      if (listOfCurCat == null) {
        listOfCurCat = new ArrayList<AbstractScoutHandler>();
        sorted.put(a.getCategory(), listOfCurCat);
      }
      listOfCurCat.add(a);
    }

    for (AbstractScoutHandler.Category c : sorted.keySet()) {
      for (AbstractScoutHandler a : sorted.get(c)) {
        Command cmd = getCommand(serviceLocator, a);

        CommandContributionItemParameter p = new CommandContributionItemParameter(serviceLocator, cmd.getId(), cmd.getId(), SWT.PUSH);
        p.label = a.getLabel();
        p.icon = a.getImage();
        p.tooltip = a.getToolTip();

        CommandContributionItem item = new CommandContributionItem(p);

        Object[] args = new Object[]{a.getClass(), cmd};
        TestExpression e = new TestExpression("org.eclipse.scout.sdk.ui.menu", "menuVisibilityTester", args, Boolean.TRUE);
        additions.addContributionItem(item, e);
      }
      additions.addContributionItem(new Separator(c.getId()), null);
    }
  }

  public static AbstractScoutHandler getMenuInstance(Class<? extends AbstractScoutHandler> type) {
    try {
      Constructor<? extends AbstractScoutHandler> c = type.getConstructor(new Class[]{});
      AbstractScoutHandler ret = c.newInstance();
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

  public static void registerKeyStroke(String keyStroke, Command cmd) {
    if (keyStroke != null) {
      try {

        BindingService bs = (BindingService) Workbench.getInstance().getService(IBindingService.class);

        KeyBinding oldBinding = m_usedBindings.get(keyStroke);
        if (oldBinding != null) {
          bs.removeBinding(oldBinding);
        }

        ParameterizedCommand paramCmd = new ParameterizedCommand(cmd, null);
        KeyBinding kb = new KeyBinding(KeySequence.getInstance(keyStroke), paramCmd,
            "org.eclipse.ui.defaultAcceleratorConfiguration",
            "org.eclipse.scout.sdk.explorer.context", null, null, null, Binding.USER);
        m_usedBindings.put(keyStroke, kb);
        bs.addBinding(kb);
      }
      catch (ParseException e) {
        ScoutSdkUi.logError(e);
      }
    }
  }

  private Command getCommand(IServiceLocator serviceLocator, AbstractScoutHandler action) {
    ICommandService cs = (ICommandService) serviceLocator.getService(ICommandService.class);
    Command cmd = cs.getCommand(action.getClass().getName());
    if (!cmd.isDefined()) {
      Category cat = null;
      if (action.getCategory() == null) {
        cs.getCategory(ICommandService.AUTOGENERATED_CATEGORY_ID);
      }
      else {
        cat = getCategory(serviceLocator, action.getCategory().getId());
      }
      cmd.define("", "", cat);
    }
    return cmd;
  }
}
