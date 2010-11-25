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
package org.eclipse.scout.nls.sdk;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.pde.internal.core.natures.PDE;
import org.eclipse.scout.commons.nls.NlsUtility;
import org.eclipse.scout.nls.sdk.internal.jdt.INlsFolder;
import org.eclipse.scout.nls.sdk.internal.jdt.NlsFolder;
import org.eclipse.scout.nls.sdk.internal.model.workspace.NlsWorkspace;
import org.eclipse.scout.nls.sdk.internal.model.workspace.manifest.ManifestElement;
import org.eclipse.scout.nls.sdk.internal.model.workspace.manifest.WorkspaceManifestReader;
import org.eclipse.scout.nls.sdk.model.util.Language;
import org.eclipse.scout.nls.sdk.model.workspace.INlsWorkspace;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
@SuppressWarnings("restriction")
public class NlsCore extends AbstractUIPlugin {

  // static vars
  // colors
  public static final String COLOR_NLS_ROW_INACTIVE_FOREGROUND = "nlsRowInactiveForeground";
  public static final String COLOR_TABLE_CURSOR_BACKGROUND = "color_table_cursor_background";
  public static final String COLOR_TABLE_CURSOR_FOREGROUND = "color_table_cursor_foreground";
  public static final String COLOR_TABLE_CURSOR_INACTIVE_BACKGROUND = "color_table_cursor_inactive_background";
  public static final String COLOR_TABLE_CURSOR_INACTIVE_FOREGROUND = "color_table_cursor_inactive_foreground";

  public static final String FILE_NAME_UNKNOWN = "unknown";

  public static final String ICON_SORT_ASC = "sortUp";
  public static final String ICON_MAGNIFIER = "magnifier";
  public static final String ICON_COMMENT = "comment";
  public static final String ICON_SORT_DESC = "sortDown";
  public static final String ICON_WARNING_8 = "warning8";
  public static final String ICON_TOOL_ADD = "add";
  public static final String ICON_TOOL_REFRESH = "refresh";
  public static final String ICON_TOOL_UPDATE = "updates_obj";
  public static final String ICON_TOOL_EXPORT = "export";

  // The plug-in ID
  public static final String PLUGIN_ID = "org.eclipse.scout.nls.sdk";

  // The shared instance
  private static NlsCore plugin;
  static String imagePath = "resources/icons/";
  private BundleContext m_context;

  private ColorRegistry m_colorRegistry;
  private INlsWorkspace m_nlsWorkspace;

  /**
   * The constructor
   */
  public NlsCore() {
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(BundleContext context) throws Exception {
    plugin = this;
    m_context = context;
    m_colorRegistry = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();
    m_colorRegistry.put(COLOR_NLS_ROW_INACTIVE_FOREGROUND, new RGB(178, 178, 178));
    m_colorRegistry.put(COLOR_TABLE_CURSOR_INACTIVE_BACKGROUND, new RGB(255, 255, 255));
    m_colorRegistry.put(COLOR_TABLE_CURSOR_BACKGROUND, new RGB(13, 58, 161));
    m_colorRegistry.put(COLOR_TABLE_CURSOR_FOREGROUND, new RGB(255, 255, 255));
    m_colorRegistry.put(COLOR_TABLE_CURSOR_INACTIVE_FOREGROUND, new RGB(0, 0, 0));
    m_nlsWorkspace = new NlsWorkspace();
    super.start(context);

  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(BundleContext context) throws Exception {
    plugin = null;
    m_context = null;
    super.stop(context);
  }

  public static void log(IStatus log) {
    getDefault().getLog().log(log);
  }

  public static void logInfo(String message) {
    logInfo(message, null);
  }

  public static void logInfo(String message, Throwable t) {
    if (message == null) {
      message = "";
    }
    log(new Status(IStatus.INFO, PLUGIN_ID, message, t));
  }

  public static void logWarning(String message) {
    logWarning(message, null);
  }

  public static void logWarning(Throwable t) {
    logWarning(null, t);
  }

  public static void logWarning(String message, Throwable t) {
    if (message == null) {
      message = "";
    }
    log(new Status(IStatus.WARNING, PLUGIN_ID, message, t));
  }

  public static void logError(Throwable t) {
    logError("", t);
  }

  public static void logError(String message) {
    logError(message, null);
  }

  public static void logError(String message, Throwable t) {
    if (message == null) {
      message = "";
    }
    log(new Status(IStatus.ERROR, PLUGIN_ID, message, t));
  }

  /**
   * Returns the shared instance
   *
   * @return the shared instance
   */
  public static NlsCore getDefault() {
    return plugin;
  }

  public BundleContext getBundleContext() {
    return m_context;
  }

  public static Image getImage(String name) {
    Image img = plugin.getImageRegistry().get(name);
    if (img == null) {
      getImageDescriptor(name);
    }
    return plugin.getImageRegistry().get(name);
  }

  public static Color getColor(String key) {
    return getDefault().m_colorRegistry.get(key);
  }

  public static ImageDescriptor getImageDescriptor(String name) {
    ImageDescriptor desc = plugin.getImageRegistry().getDescriptor(name);
    if (desc == null) {

      // gif
      desc = imageDescriptorFromPlugin(PLUGIN_ID, imagePath + name + ".gif");
      // png
      if (desc == null) {
        desc = imageDescriptorFromPlugin(PLUGIN_ID, imagePath + name + ".png");
      }
      // jpg
      if (desc == null) {
        desc = imageDescriptorFromPlugin(PLUGIN_ID, imagePath + name + ".jpg");
      }
      if (desc == null) {
        System.err.println("could not find image for plugin: " + PLUGIN_ID + " under: " + imagePath + name);
        // NlsCore.logWarning("could not find image for plugin: "+PLUGIN_ID+" under: "+imagePath+name);
      }
      plugin.getImageRegistry().put(name, desc);
    }
    return desc;
  }

  /**
   * Returns a list of all fragments of the host plugin specified by the hostPluginId. If no plugins are found
   * an empty list is returned.
   *
   * @param hostPluginId
   * @return all fragments of the passed host plugin id found in the workspace
   * @throws CoreException
   */
  public static List<IProject> getWorkspaceFragments(String hostPluginId) throws CoreException {
    List<IProject> fragments = new LinkedList<IProject>();
    IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
    for (IProject lookAt : allProjects) {
      if (lookAt.isOpen() && lookAt.hasNature(JavaCore.NATURE_ID) && lookAt.hasNature(PDE.PLUGIN_NATURE)) {
        WorkspaceManifestReader manifest = new WorkspaceManifestReader(lookAt);
        if (manifest.hasElement("Fragment-Host")) {
          ManifestElement hostPluginEntry = manifest.getAttribute("Fragment-Host").getFirstElement();
          if (hostPluginEntry.getValue().equals(hostPluginId)) {
            fragments.add(lookAt);
          }

        }
      }
    }
    return fragments;
  }

  /**
   * finds all translations of the passed file in the toLookAt project. e.g. if a file
   * /lib/translations/messages.properties is passed all files /lib/translations/messages_[de|en|..].properties will be
   * returned.
   *
   * @param toLookAt
   *          the project to take a look at usually a fragment of the project the passed file is in.
   * @param file
   *          the definition of the translation file.
   * @return
   * @throws CoreException
   */
  public static List<IFile> getAllTranslations(IProject toLookAt, IPath path, String fileNamePrefix)
      throws CoreException {
    List<IFolder> folders = new LinkedList<IFolder>();
    List<INlsFolder> nlsFolders = getFoldersOfProject(toLookAt, path, true);
    for (INlsFolder folder : nlsFolders) {
      folders.add(folder.getFolder());
    }

    return getAllTranslations(folders, fileNamePrefix);
  }

  public static List<IFile> getAllTranslations(List<IFolder> folders, String fileNamePrefix) throws CoreException {
    List<IFile> files = new LinkedList<IFile>();
    for (IFolder folder : folders) {
      if (folder.exists()) {
        IResource[] resources = folder.members(IResource.NONE);
        for (IResource resource : resources) {
          if (resource instanceof IFile && resource.getName().matches(fileNamePrefix + "(_[a-zA-Z]{2}){0,3}" + "\\.properties")) {
            files.add((IFile) resource);
          }
        }
      }
    }
    return files;
  }

  public static INlsWorkspace getNlsWorkspace() {
    return getDefault().getNlsWorkspaceImpl();
  }

  private INlsWorkspace getNlsWorkspaceImpl() {
    return m_nlsWorkspace;
  }

  // public static String getLanguageIso(String fileName) {
  // String language = null;
  // Pattern p = Pattern.compile("_..\\.properties");
  // Matcher m = p.matcher(fileName);
  // if (m.find()) {
  // language = fileName.substring(m.start() + 1, m.start() + 3);
  // } else {
  // return INlsConstants.LANGUAGE_DEFAULT;
  // }
  // return language;
  // }

  // public static Language getLanguage(String languageKey) {
  // return m_languages.get(languageKey.toLowerCase());
  // }
  // public static Comparator<Language> getLanguageDefaultComparator(){
  // return new Comparator<Language>(){
  // public int compare(Language o1, Language o2) {
  // if(o1.getIsoCode() == INlsConstants.LANGUAGE_DEFAULT){
  // return -1;
  // }else if(o2.getIsoCode() == INlsConstants.LANGUAGE_DEFAULT){
  // return 1;
  // }
  // return o1.getDispalyName().compareTo(o2.getDispalyName());
  // }
  // };
  // }

  // public static Language[] getAllLanguages() {
  // return m_languages.values().toArray(new Language[m_languages.size()]);
  // }

  public static Language getLanguage(String simpleFileName) {
    String regexp = "^[^_.]*(_([A-Za-z]{2}))?(_([A-Za-z]{2}))?(_([A-Za-z]{2}))?\\.properties$";
    Pattern p = Pattern.compile(regexp);
    Matcher matcher = p.matcher(simpleFileName);
    if (matcher.matches()) {
      if (matcher.group(2) == null) {
        // default
        return Language.LANGUAGE_DEFAULT;
      }
      else {
        String languageIso = matcher.group(2);
        if (languageIso == null) {
          languageIso = "";
        }
        String countryIso = matcher.group(4);
        if (countryIso == null) {
          countryIso = "";
        }
        String variantIso = matcher.group(5);
        if (variantIso == null) {
          variantIso = "";
        }
        return new Language(new Locale(languageIso, countryIso, variantIso));
      }
    }
    return null;
  }

  public static String getLocalizedPropertiesFileName(String prefix, Language language) {
    String fileName = prefix;
    Locale locale = language.getLocale();
    fileName = fileName + "_" + locale.toString() + ".properties";
    return fileName;
  }

  public static List<INlsFolder> getFoldersOfProject(IProject project, IPath path, boolean runntimeDir)
      throws JavaModelException, CoreException {
    List<INlsFolder> folders = new LinkedList<INlsFolder>();
    if (project.isOpen()) {
      if (project.hasNature(JavaCore.NATURE_ID)) {
        // check runtime dir
        if (runntimeDir) {
          IJavaProject jp = JavaCore.create(project);
          IClasspathEntry[] clEntries = jp.getRawClasspath();
          for (IClasspathEntry entry : clEntries) {
            if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
              IPath toCheck = new Path(entry.getPath().lastSegment()).append(path);
              if (project.getFolder(toCheck).exists()) {
                folders.add(new NlsFolder(project.getFolder(toCheck), INlsFolder.TYPE_PACKAGE_FOLDER));
              }
            }
          }
        }
        IFolder foundFolder = project.getFolder(path);
        if (foundFolder != null && foundFolder.exists()) {
          folders.add(new NlsFolder(foundFolder, INlsFolder.TYPE_SIMPLE_FOLDER));
        }
      }

    }
    return folders;
  }

  /**
   * finds all fragments to a cretain plugin project
   *
   * @param project
   * @return
   * @throws FileNotFoundException
   * @throws CoreException
   */
  public static List<IProject> getProjectGroup(IProject project) throws FileNotFoundException, CoreException {
    List<IProject> anesters = getFragementsForProject(project);
    anesters.add(project);
    return anesters;
  }

  public static List<IProject> getFragementsForProject(IProject project) throws CoreException {
    List<IProject> anesters = new LinkedList<IProject>();
    WorkspaceManifestReader originalReader = new WorkspaceManifestReader(project);

    String originalSymbolicName = originalReader.getAttribute("Bundle-SymbolicName").getFirstElement().getValue();
    IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
    for (IProject lookAt : allProjects) {
      if (lookAt.isOpen() && lookAt.hasNature(JavaCore.NATURE_ID) && lookAt.getNature(PDE.PLUGIN_NATURE) != null) {
        WorkspaceManifestReader manifest = new WorkspaceManifestReader(lookAt);
        if (manifest.hasElement("Fragment-Host")) {
          ManifestElement hostPluginEntry = manifest.getAttribute("Fragment-Host").getFirstElement();
          if (hostPluginEntry.getValue().equals(originalSymbolicName)) {
            anesters.add(lookAt);
          }
        }
      }
    }
    return anesters;
  }

  public static IFile getNlsFile(String pathString) {
    String[] args = pathString.split(":");
    if (args.length != 2) {
      logWarning("invalid parent file entry in Manifest: " + pathString
          + " format should be: [pluginId]:[path to *.nls file]");
      return null;
    }
    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(args[0]);
    if (project == null) {
      logWarning("invalid project name: " + args[0]);
      return null;
    }
    IFile file = project.getFile(new Path(args[1]));
    if (file == null || !file.exists()) {
      logWarning("could not find file: " + args[1] + " in project " + args[0]);
      return null;
    }
    return file;
  }

  public static void dynamicBindFields(Class<?> clazz) {
    NlsUtility.dynamicBindFields(clazz);
  }

  /**
   * @param project
   * @return all keys of the whole project hierarchy
   */
  public static String[] getProjectKeys(INlsProject project) {
    HashSet<String> collector = new HashSet<String>();
    plugin.addKeysRec(project, collector);
    return collector.toArray(new String[collector.size()]);
  }

  private void addKeysRec(INlsProject project, HashSet<String> collector) {
    if (project == null) {
      return;
    }
    for (String k : project.getAllKeys()) {
      if (!collector.add(k)) {
        // XXX NlsCore.logWarning("the key '"+k+"' in project "+project.getName()+" is already used.");
      }
    }
    addKeysRec(project.getParent(), collector);
  }

  // /**
  // * @param project
  // * @return all nls entries of the given project and all its ancestors.
  // */
  // public static Map<String,INlsEntry > getAllEntries(INlsProject project){
  // HashMap<String,INlsEntry> collector = new HashMap<String, INlsEntry>();
  // plugin.addEntriesRec(project, collector);
  // return collector;
  // }
  //
  // public static INlsEntry getEntry(INlsProject project,String key){
  // Map<String, INlsEntry> allEntries = NlsCore.getAllEntries(project);
  // return allEntries.get(key);
  // }
  //
  // private void addEntriesRec(INlsProject project, HashMap<String,INlsEntry> collector){
  // if(project == null){
  // return;
  // }
  // for(INlsEntry k: project.getAllRows().values()){
  // if(collector.put(k.getKey(), k) != null){
  // //XXX NlsCore.logWarning("the key '"+k+"' in project "+project.getName()+" is already used.");
  // }
  // }
  // addEntriesRec(project.getParent(), collector);
  // }

  public static IStatus getHighestSeverityStatus(IStatus status) {
    return getDefault().getHighestSeverityStatusImpl(status, Status.OK_STATUS);
  }

  private IStatus getHighestSeverityStatusImpl(IStatus status, IStatus highestSeverity) {
    if (status.isMultiStatus()) {
      for (IStatus child : status.getChildren()) {
        highestSeverity = getHighestSeverityStatusImpl(child, highestSeverity);
      }
      return highestSeverity;
    }
    else {
      if (highestSeverity.getSeverity() < status.getSeverity()) {
        highestSeverity = status;
      }
      return highestSeverity;
    }
  }

}
