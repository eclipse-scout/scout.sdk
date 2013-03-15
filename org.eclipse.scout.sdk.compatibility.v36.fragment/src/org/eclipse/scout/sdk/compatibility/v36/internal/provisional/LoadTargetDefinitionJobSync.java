package org.eclipse.scout.sdk.compatibility.v36.internal.provisional;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.equinox.frameworkadmin.BundleInfo;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.TargetPlatform;
import org.eclipse.pde.internal.core.ICoreConstants;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PDEPreferencesManager;
import org.eclipse.pde.internal.core.PDEState;
import org.eclipse.pde.internal.core.TargetPlatformResetJob;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.pde.internal.core.target.AbstractBundleContainer;
import org.eclipse.pde.internal.core.target.AbstractTargetHandle;
import org.eclipse.pde.internal.core.target.Messages;
import org.eclipse.pde.internal.core.target.ProfileBundleContainer;
import org.eclipse.pde.internal.core.target.provisional.IBundleContainer;
import org.eclipse.pde.internal.core.target.provisional.IResolvedBundle;
import org.eclipse.pde.internal.core.target.provisional.ITargetDefinition;
import org.eclipse.pde.internal.core.target.provisional.ITargetPlatformService;
import org.eclipse.pde.internal.core.target.provisional.LoadTargetDefinitionJob;
import org.eclipse.pde.internal.core.target.provisional.NameVersionDescriptor;

/**
 * <h3>{@link LoadTargetDefinitionJobSync}</h3> This class is a copy of
 * org.eclipse.pde.internal.core.target.provisional.LoadTargetDefinitionJob.
 * This is done because the original class schedules a TargetPlatformResetJob with the same rule as the running
 * operation which leads to a deadlock. Solution: we do not schedule this job but run it directly in the current job.
 *
 * @author mvi
 * @since 3.9.0 15.03.2013
 */
@SuppressWarnings({"restriction", "unchecked", "deprecation", "rawtypes"})
public class LoadTargetDefinitionJobSync extends WorkspaceJob {

	private static final String JOB_FAMILY_ID = "LoadTargetDefinitionJob"; //$NON-NLS-1$

	/**
	 * Target definition being loaded
	 */
	private ITargetDefinition fTarget;

	/**
	 * Whether a target definition was specified
	 */
	private boolean fNone = false;

	/**
	 * Constructs a new operation to load the specified target definition
	 * as the current target platform. When <code>null</code> is specified
	 * the target platform is empty and all other settings are default.  This
	 * method will cancel all existing LoadTargetDefinitionJob instances then
	 * schedules the operation as a user job.
	 *
	 * @param target target definition or <code>null</code> if none
	 */
	public static void load(ITargetDefinition target) {
		load(target, null);
	}

	/**
	 * Constructs a new operation to load the specified target definition
	 * as the current target platform. When <code>null</code> is specified
	 * the target platform is empty and all other settings are default.  This
	 * method will cancel all existing LoadTargetDefinitionJob instances then
	 * schedules the operation as a user job.  Adds the given listener to the
	 * job that is started.
	 *
	 * @param target target definition or <code>null</code> if none
	 * @param listener job change listener that will be added to the created job
	 */
	public static void load(ITargetDefinition target, IJobChangeListener listener) {
		Job.getJobManager().cancel(JOB_FAMILY_ID);
		Job job = new LoadTargetDefinitionJob(target);
		job.setUser(true);
		if (listener != null) {
			job.addJobChangeListener(listener);
		}
		job.schedule();
	}

	/**
	 * Constructs a new operation to load the specified target definition
	 * as the current target platform. When <code>null</code> is specified
	 * the target platform is empty and all other settings are default.
	 *<p>
	 * Clients should use {@link #getLoadJob(ITargetDefinition)} instead to ensure
	 * any existing jobs are cancelled.
	 * </p>
	 * @param target target definition or <code>null</code> if none
	 */
	public LoadTargetDefinitionJobSync(ITargetDefinition target) {
		super(Messages.LoadTargetDefinitionJob_0);
		fTarget = target;
		if (target == null) {
			fNone = true;
			ITargetPlatformService service = (ITargetPlatformService) PDECore.getDefault().acquireService(ITargetPlatformService.class.getName());
			fTarget = service.newTarget();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#belongsTo(java.lang.Object)
	 */
	public boolean belongsTo(Object family) {
		return JOB_FAMILY_ID.equals(family);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.WorkspaceJob#runInWorkspace(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
		try {
			PDEPreferencesManager preferences = PDECore.getDefault().getPreferencesManager();
			monitor.beginTask(Messages.LoadTargetOperation_mainTaskName, 100);

			loadEnvironment(preferences, new SubProgressMonitor(monitor, 5));
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}

			loadArgs(preferences, new SubProgressMonitor(monitor, 5));
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}

			loadJRE(preferences, new SubProgressMonitor(monitor, 15));
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}

			loadImplicitPlugins(preferences, new SubProgressMonitor(monitor, 15));
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}

			loadPlugins(preferences, new SubProgressMonitor(monitor, 60));
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}

			loadAdditionalPreferences(preferences);
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}

			PDECore.getDefault().getPreferencesManager().savePluginPreferences();
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
		} finally {
			monitor.done();
		}
		return Status.OK_STATUS;
	}

	/**
	 * Configures program and VM argument preferences based on the target
	 * definition.
	 *
	 * @param pref preference manager
	 * @param monitor progress monitor
	 */
	private void loadArgs(PDEPreferencesManager pref, IProgressMonitor monitor) {
		monitor.beginTask(Messages.LoadTargetOperation_argsTaskName, 2);
		String args = fTarget.getProgramArguments();
		pref.setValue(ICoreConstants.PROGRAM_ARGS, (args != null) ? args : ""); //$NON-NLS-1$
		monitor.worked(1);
		args = fTarget.getVMArguments();
		pref.setValue(ICoreConstants.VM_ARGS, (args != null) ? args : ""); //$NON-NLS-1$
		monitor.done();
	}

	/**
	 * Configures the environment preferences from the target definition.
	 *
	 * @param pref preference manager
	 * @param monitor progress monitor
	 */
	private void loadEnvironment(PDEPreferencesManager pref, IProgressMonitor monitor) {
		monitor.beginTask(Messages.LoadTargetOperation_envTaskName, 1);
		setEnvironmentPref(pref, ICoreConstants.ARCH, fTarget.getArch());
		setEnvironmentPref(pref, ICoreConstants.NL, fTarget.getNL());
		setEnvironmentPref(pref, ICoreConstants.OS, fTarget.getOS());
		setEnvironmentPref(pref, ICoreConstants.WS, fTarget.getWS());
		monitor.done();
	}

	/**
	 * Sets the given preference to default when <code>null</code> or the
	 * specified value.
	 *
	 * @param pref preference manager
	 * @param key preference key
	 * @param value preference value or <code>null</code>
	 */
	private void setEnvironmentPref(PDEPreferencesManager pref, String key, String value) {
		if (value == null) {
			pref.setToDefault(key);
		} else {
			pref.setValue(key, value);
		}
	}

	/**
	 * Sets the workspace default JRE based on the target's JRE container.
	 *
	 * @param pref
	 * @param monitor
	 */
	private void loadJRE(PDEPreferencesManager pref, IProgressMonitor monitor) {
		IPath container = fTarget.getJREContainer();
		monitor.beginTask(Messages.LoadTargetOperation_jreTaskName, 1);
		if (container != null) {
			IVMInstall jre = JavaRuntime.getVMInstall(container);
			if (jre != null) {
				IVMInstall def = JavaRuntime.getDefaultVMInstall();
				if (!jre.equals(def)) {
					try {
						JavaRuntime.setDefaultVMInstall(jre, null);
					} catch (CoreException e) {
					}
				}
			}
		}
		monitor.done();
	}

	/**
	 * Sets implicit dependencies, if any
	 *
	 * @param pref preference store
	 * @param monitor progress monitor
	 */
	private void loadImplicitPlugins(PDEPreferencesManager pref, IProgressMonitor monitor) {
		NameVersionDescriptor[] infos = fTarget.getImplicitDependencies();
		if (infos != null) {
			monitor.beginTask(Messages.LoadTargetOperation_implicitPluginsTaskName, infos.length + 1);
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < infos.length; i++) {
				buffer.append(infos[i].getId()).append(',');
				monitor.worked(1);
			}
			if (infos.length > 0)
				buffer.setLength(buffer.length() - 1);
			pref.setValue(ICoreConstants.IMPLICIT_DEPENDENCIES, buffer.toString());
		}
		monitor.done();
	}

	/**
	 * Resolves the bundles in the target platform and sets them in the corresponding
	 * CHECKED_PLUGINS preference. Sets home and addition location preferences as well.
	 *
	 * @param pref
	 * @param monitor
	 * @throws CoreException
	 */
	private void loadPlugins(PDEPreferencesManager pref, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask(Messages.LoadTargetOperation_loadPluginsTaskName, 100);
		String currentPath = pref.getString(ICoreConstants.PLATFORM_PATH);
		IBundleContainer[] containers = fTarget.getBundleContainers();
		// the first container is assumed to be the primary/home location
		String path = null;
		if (containers != null && containers.length > 0) {
			path = ((AbstractBundleContainer) containers[0]).getLocation(true);
		}
		if (path == null) {
			path = TargetPlatform.getDefaultLocation();
		} else {
			try {
				IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
				path = manager.performStringSubstitution(path);
			} catch (CoreException e) {
				return;
			}
		}
		monitor.worked(10);
		List additional = getAdditionalLocs();
		handleReload(path, additional, pref, new SubProgressMonitor(monitor, 85));

		// update preferences (Note: some preferences updated in handleReload())
		pref.setValue(ICoreConstants.PLATFORM_PATH, path);
		String mode = new Path(path).equals(new Path(TargetPlatform.getDefaultLocation())) ? ICoreConstants.VALUE_USE_THIS : ICoreConstants.VALUE_USE_OTHER;
		pref.setValue(ICoreConstants.TARGET_MODE, mode);

		ListIterator li = additional.listIterator();
		StringBuffer buffer = new StringBuffer();
		while (li.hasNext())
			buffer.append(li.next()).append(","); //$NON-NLS-1$
		if (buffer.length() > 0)
			buffer.setLength(buffer.length() - 1);
		pref.setValue(ICoreConstants.ADDITIONAL_LOCATIONS, buffer.toString());

		String newValue = currentPath;
		for (int i = 0; i < 4; i++) {
			String value = pref.getString(ICoreConstants.SAVED_PLATFORM + i);
			pref.setValue(ICoreConstants.SAVED_PLATFORM + i, newValue);
			if (!value.equals(currentPath))
				newValue = value;
			else
				break;
		}
		monitor.done();
	}

	/**
	 * Sets the TARGET_PROFILE preference which stores the ID of the target profile used
	 * (if based on an target extension) or the workspace location of the file that
	 * was used. For now we just clear it.
	 * <p>
	 * Sets the WORKSPACE_TARGET_HANDLE.
	 * </p>
	 * @param pref
	 */
	private void loadAdditionalPreferences(PDEPreferencesManager pref) throws CoreException {
		pref.setValue(ICoreConstants.TARGET_PROFILE, ""); //$NON-NLS-1$
		String memento = fTarget.getHandle().getMemento();
		if (fNone) {
			memento = ICoreConstants.NO_TARGET;
		}
		pref.setValue(ICoreConstants.WORKSPACE_TARGET_HANDLE, memento);
		IBundleContainer[] containers = fTarget.getBundleContainers();
		boolean profile = false;
		if (containers != null && containers.length > 0) {
			profile = containers[0] instanceof ProfileBundleContainer;
		}
		pref.setValue(ICoreConstants.TARGET_PLATFORM_REALIZATION, profile);
	}

	/**
	 * Returns a list of additional locations of bundles.
	 *
	 * @return additional bundle locations
	 */
	private List getAdditionalLocs() throws CoreException {
		ArrayList additional = new ArrayList();
		// secondary containers are considered additional
		IBundleContainer[] containers = fTarget.getBundleContainers();
		if (containers != null && containers.length > 1) {
			for (int i = 1; i < containers.length; i++) {
				additional.add(((AbstractBundleContainer) containers[i]).getLocation(true));
			}
		}
		return additional;
	}

	private void handleReload(String targetLocation, List additionalLocations, PDEPreferencesManager pref, IProgressMonitor monitor) throws CoreException {
		SubMonitor subMon = SubMonitor.convert(monitor, Messages.LoadTargetOperation_reloadTaskName, 100);
		try {
			Set included = new HashSet();
			Set duplicates = new HashSet();
			List infos = new ArrayList();
			Set includedIds = new HashSet();

			if (!fTarget.isResolved()) {
				// Even if there are errors in the target, don't interrupt the user with an error dialog
				fTarget.resolve(subMon.newChild(20));
			} else {
				subMon.worked(20);
			}

			if (subMon.isCanceled()) {
				return;
			}

			// collect all bundles, ignoring duplicates (symbolic name & version)
			IResolvedBundle[] resolved = fTarget.getBundles();
			List pooled = new ArrayList();
			boolean considerPool = false;
			for (int i = 0; i < resolved.length; i++) {
				if (resolved[i].getStatus().isOK()) {
					BundleInfo bundleInfo = resolved[i].getBundleInfo();
					NameVersionDescriptor desc = new NameVersionDescriptor(bundleInfo.getSymbolicName(), bundleInfo.getVersion());
					File file = new File(bundleInfo.getLocation());
					boolean inPool = AbstractTargetHandle.BUNDLE_POOL.isPrefixOf(new Path(file.getAbsolutePath()));
					considerPool = considerPool || inPool;
					if (!duplicates.contains(desc)) {
						if (inPool) {
							pooled.add(file);
						}
						infos.add(bundleInfo);
						included.add(bundleInfo);
						includedIds.add(bundleInfo.getSymbolicName());
						duplicates.add(desc);
					}
				}
			}

			// Compute missing (not included) bundles (preference need to know disabled/missing bundles)
			List missing = new ArrayList();
			NameVersionDescriptor[] restrictions = fTarget.getIncluded();
			if (restrictions != null) {
				IResolvedBundle[] all = fTarget.getAllBundles();
				for (int j = 0; j < all.length; j++) {
					IResolvedBundle bi = all[j];
					if (!included.contains(bi.getBundleInfo())) {
						missing.add(bi.getBundleInfo());
					}
				}
			}

			List paths = new ArrayList(infos.size() + missing.size());
			Iterator iterator = infos.iterator();
			while (iterator.hasNext()) {
				BundleInfo info = (BundleInfo) iterator.next();
				try {
					paths.add(new File(info.getLocation()).toURL());
				} catch (MalformedURLException e) {
					throw new CoreException(new Status(IStatus.ERROR, PDECore.PLUGIN_ID, Messages.LoadTargetDefinitionJob_1, e));
				}
			}

			// generate URLs and save CHECKED_PLUGINS (which are missing), and add to master list of paths
			StringBuffer checked = new StringBuffer();
			StringBuffer versions = new StringBuffer();
			int count = 0;
			iterator = missing.iterator();
			Set missingDescriptions = new HashSet(missing.size());
			while (iterator.hasNext()) {
				BundleInfo bi = (BundleInfo) iterator.next();
				NameVersionDescriptor desc = new NameVersionDescriptor(bi.getSymbolicName(), bi.getVersion());
				missingDescriptions.add(desc);
				try {
					paths.add(new File(bi.getLocation()).toURL());
				} catch (MalformedURLException e) {
					throw new CoreException(new Status(IStatus.ERROR, PDECore.PLUGIN_ID, Messages.LoadTargetDefinitionJob_1, e));
				}
				if (count > 0) {
					checked.append(" "); //$NON-NLS-1$
				}
				checked.append(bi.getSymbolicName());
				count++;
				if (includedIds.contains(bi.getSymbolicName())) {
					// multiple versions of the bundle are available and some are included - store version info of excluded bundles
					if (versions.length() > 0) {
						versions.append(" "); //$NON-NLS-1$
					}
					versions.append(desc.toPortableString());
				}
			}

			URL[] urls = (URL[]) paths.toArray(new URL[paths.size()]);
			PDEState state = new PDEState(urls, true, new SubProgressMonitor(monitor, 45));
			IPluginModelBase[] models = state.getTargetModels();
			for (int i = 0; i < models.length; i++) {
				NameVersionDescriptor nv = new NameVersionDescriptor(models[i].getPluginBase().getId(), models[i].getPluginBase().getVersion());
				models[i].setEnabled(!missingDescriptions.contains(nv));
			}

			if (subMon.isCanceled()) {
				return;
			}

			// save CHECKED_PLUGINS
			if (urls.length == 0) {
				pref.setValue(ICoreConstants.CHECKED_PLUGINS, ICoreConstants.VALUE_SAVED_NONE);
			} else if (missing.size() == 0) {
				pref.setValue(ICoreConstants.CHECKED_PLUGINS, ICoreConstants.VALUE_SAVED_ALL);
			} else {
				pref.setValue(ICoreConstants.CHECKED_PLUGINS, checked.toString());
			}
			// save CHECKED_VERSION_PLUGINS
			if (versions.length() > 0) {
				pref.setValue(ICoreConstants.CHECKED_VERSION_PLUGINS, versions.toString());
			} else {
				// no version information required
				pref.setValue(ICoreConstants.CHECKED_VERSION_PLUGINS, ICoreConstants.VALUE_SAVED_NONE);
			}

			// saved POOLED_BUNDLES
			if (pooled.isEmpty()) {
				if (considerPool) {
					// all pooled bundles are excluded
					pref.setValue(ICoreConstants.POOLED_URLS, ICoreConstants.VALUE_SAVED_NONE);
				} else {
					// nothing in the pool
					pref.setValue(ICoreConstants.POOLED_URLS, ""); //$NON-NLS-1$
				}
			} else {
				StringBuffer buf = new StringBuffer();
				Iterator iterator2 = pooled.iterator();
				while (iterator2.hasNext()) {
					File bundle = (File) iterator2.next();
					buf.append(bundle.getName()); // only store file name to make workspace portable
					if (iterator2.hasNext()) {
						buf.append(',');
					}
				}
				pref.setValue(ICoreConstants.POOLED_URLS, buf.toString());
				pref.setValue(ICoreConstants.POOLED_BUNDLES, ""); // NO LONGER USED //$NON-NLS-1$
			}

			// Save the feature list for the external feature model manager to EXTERNAL_FEATURES
			StringBuffer featureList = new StringBuffer();
			IFeatureModel[] features = fTarget.getAllFeatures();

			// If the target has includes, but only plug-ins are specified, just include all features

			// If the target has feature includes, only add features that are included (bug 308693)
			NameVersionDescriptor[] includes = fTarget.getIncluded();
			boolean featuresFound = false; // If only plug-ins are specified, include all features
			if (includes != null) {
				for (int i = 0; i < includes.length; i++) {
					if (includes[i].getType() == NameVersionDescriptor.TYPE_FEATURE) {
						featuresFound = true;
						IFeatureModel bestMatch = null;
						for (int j = 0; j < features.length; j++) {
							if (features[j].getFeature().getId().equals(includes[i].getId())) {
								if (includes[i].getVersion() != null) {
									// Try to find an exact feature match
									if (includes[i].getVersion().equals(features[j].getFeature().getVersion())) {
										// Exact match
										bestMatch = features[j];
										break;
									}
								} else if (bestMatch != null) {
									// If no version specified take the highest version
									Version v1 = Version.parseVersion(features[j].getFeature().getVersion());
									Version v2 = Version.parseVersion(bestMatch.getFeature().getVersion());
									if (v1.compareTo(v2) > 0) {
										bestMatch = features[j];
									}
								}

								if (bestMatch == null) {
									// If we can't find a version match, just take any name match
									bestMatch = features[j];
								}
							}
						}
						if (bestMatch != null) {
							if (featureList.length() > 0) {
								featureList.append(',');
							}
							featureList.append(bestMatch.getFeature().getId());
							featureList.append('@');
							featureList.append(bestMatch.getFeature().getVersion());
						}
					}
				}
			}

			if (includes == null || !featuresFound) {
				// Add all features to the list
				for (int i = 0; i < features.length; i++) {
					featureList.append(features[i].getFeature().getId());
					featureList.append('@');
					featureList.append(features[i].getFeature().getVersion());
					if (i < features.length - 1) {
						featureList.append(',');
					}
				}
			}

			pref.setValue(ICoreConstants.EXTERNAL_FEATURES, featureList.toString());

			P_TargetPlatformResetJobEx job = new P_TargetPlatformResetJobEx(state);
			job.run(monitor);
		} finally {
			if (monitor != null) {
				monitor.done();
			}
			subMon.done();
		}
	}

	  private static class P_TargetPlatformResetJobEx extends TargetPlatformResetJob {

	    public P_TargetPlatformResetJobEx(PDEState newState) {
	      super(newState);
	    }

	    @Override
	    public IStatus run(IProgressMonitor monitor) {
	      return super.run(monitor);
	    }
	  }
}
