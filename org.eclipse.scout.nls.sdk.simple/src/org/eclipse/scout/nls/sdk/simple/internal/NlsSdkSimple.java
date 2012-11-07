package org.eclipse.scout.nls.sdk.simple.internal;

import java.io.FileNotFoundException;
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
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.HostSpecification;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.core.natures.PDE;
import org.eclipse.scout.nls.sdk.internal.jdt.INlsFolder;
import org.eclipse.scout.nls.sdk.internal.jdt.NlsFolder;
import org.eclipse.scout.nls.sdk.model.util.Language;
import org.eclipse.scout.sdk.util.log.SdkLogManager;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
@SuppressWarnings("restriction")
public class NlsSdkSimple extends AbstractUIPlugin {

  // The plug-in ID
  public static final String PLUGIN_ID = "org.eclipse.scout.nls.sdk.simple"; //$NON-NLS-1$
  private static final Pattern PATTERN = Pattern.compile("^[^_.]*(_([A-Za-z]{2}))?(_([A-Za-z]{2}))?(_([A-Za-z]{2}))?\\.properties$");

  // The shared instance
  private static NlsSdkSimple plugin;
  private static SdkLogManager logManager;

  /**
   * The constructor
   */
  public NlsSdkSimple() {
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
        IPluginModelBase b = PluginRegistry.findModel(lookAt);
        if (b != null) {
          HostSpecification hspec = b.getBundleDescription().getHost();
          if (hspec != null) {
            BundleDescription[] hosts = hspec.getHosts();
            if (hosts != null && hosts.length > 0) {
              if (hosts[0].getName().equals(hostPluginId)) {
                fragments.add(lookAt);
              }
            }
          }
        }
      }
    }
    return fragments;
  }

  public static List<IProject> getWorkspaceFragments(IProject project) throws CoreException {
    return getWorkspaceFragments(project.getName());
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
    List<IProject> anesters = getWorkspaceFragments(project);
    anesters.add(project);
    return anesters;
  }

  public static Language getLanguage(String simpleFileName) {
    Matcher matcher = PATTERN.matcher(simpleFileName);
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

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    plugin = this;
    logManager = new SdkLogManager(this);
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(BundleContext context) throws Exception {
    plugin = null;
    logManager = null;
    super.stop(context);

  }

  /**
   * Returns the shared instance
   * 
   * @return the shared instance
   */
  public static NlsSdkSimple getDefault() {
    return plugin;
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

  public static String getLocalizedPropertiesFileName(String prefix, Language language) {
    String fileName = prefix;
    Locale locale = language.getLocale();
    fileName = fileName + "_" + locale.toString() + ".properties";
    return fileName;
  }

  public static void logInfo(Throwable t) {
    logManager.logInfo(t);
  }

  public static void logInfo(String message) {
    logManager.logInfo(message);
  }

  public static void logInfo(String message, Throwable t) {
    logManager.logInfo(message, t);
  }

  public static void logWarning(String message) {
    logManager.logWarning(message);
  }

  public static void logWarning(Throwable t) {
    logManager.logWarning(t);
  }

  public static void logWarning(String message, Throwable t) {
    logManager.logWarning(message, t);
  }

  public static void logError(Throwable t) {
    logManager.logError(t);
  }

  public static void logError(String message) {
    logManager.logError(message);
  }

  public static void logError(String message, Throwable t) {
    logManager.logError(message, t);
  }

}
