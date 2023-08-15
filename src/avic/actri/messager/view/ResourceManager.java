/**
 * @copyright actri.avic
 */
package avic.actri.messager.view;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/**
 * @author tdan 2010-10-08
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ResourceManager extends SWTResourceManager {

	public static void dispose() {
		disposeColors();
		disposeFonts();
		disposeImages();
		disposeCursors();
	}

	private static HashMap<ImageDescriptor, Image> m_DescriptorImageMap = new HashMap<ImageDescriptor, Image>();

	/**
	 * 
	 * @param clazz
	 * @param path
	 * @return
	 */
	public static ImageDescriptor getImageDescriptor(Class clazz, String path) {
		return ImageDescriptor.createFromFile(clazz, path);
	}

	/**
	 * 
	 * @param path
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static ImageDescriptor getImageDescriptor(String path) {
		try {
			return ImageDescriptor.createFromURL((new File(path)).toURL());
		} catch (MalformedURLException e) {
			return null;
		}
	}

	/**
	 * 
	 * @param descriptor
	 * @return
	 */
	public static Image getImage(ImageDescriptor descriptor) {
		if (descriptor == null)
			return null;
		Image image = (Image) m_DescriptorImageMap.get(descriptor);
		if (image == null) {
			image = descriptor.createImage();
			m_DescriptorImageMap.put(descriptor, image);
		}
		return image;
	}

	public static void disposeImages() {
		SWTResourceManager.disposeImages();
		//
		for (Object element : m_DescriptorImageMap.values())
			((Image) element).dispose();
		m_DescriptorImageMap.clear();
	}

	private static HashMap<URL, Image> m_URLImageMap = new HashMap<URL, Image>();

	/**
	 * 
	 * @param plugin
	 * @param name
	 * @return
	 */
	public static Image getPluginImage(Object plugin, String name) {
		try {
			try {
				URL url = getPluginImageURL(plugin, name);
				if (m_URLImageMap.containsKey(url))
					return (Image) m_URLImageMap.get(url);
				InputStream is = url.openStream();
				Image image;
				try {
					image = getImage(is);
					m_URLImageMap.put(url, image);
				} finally {
					is.close();
				}
				return image;
			} catch (Throwable e) {
			}
		} catch (Throwable e) {
		}
		return null;
	}

	/**
	 * 
	 * @param plugin
	 * @param name
	 * @return
	 */
	public static ImageDescriptor getPluginImageDescriptor(Object plugin,
			String name) {
		try {
			try {
				URL url = getPluginImageURL(plugin, name);
				return ImageDescriptor.createFromURL(url);
			} catch (Throwable e) {
				// Ignore any exceptions
			}
		} catch (Throwable e) {
			// Ignore any exceptions
		}
		return null;
	}

	/**
	 * 
	 * @param plugin
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public static URL getPluginImageURL(Object plugin, String name)
			throws Exception {
		// try to work with 'plugin' as with OSGI BundleContext
		try {
			Class bundleClass = Class.forName("org.osgi.framework.Bundle"); //$NON-NLS-1$
			Class bundleContextClass = Class
					.forName("org.osgi.framework.BundleContext"); //$NON-NLS-1$
			if (bundleContextClass.isAssignableFrom(plugin.getClass())) {
				Method getBundleMethod = bundleContextClass.getMethod(
						"getBundle", new Class[] {}); //$NON-NLS-1$
				Object bundle = getBundleMethod.invoke(plugin, new Object[] {});
				//
				Class ipathClass = Class
						.forName("org.eclipse.core.runtime.IPath"); //$NON-NLS-1$
				Class pathClass = Class
						.forName("org.eclipse.core.runtime.Path"); //$NON-NLS-1$
				Constructor pathConstructor = pathClass
						.getConstructor(new Class[] { String.class });
				Object path = pathConstructor
						.newInstance(new Object[] { name });
				//
				Class platformClass = Class
						.forName("org.eclipse.core.runtime.Platform"); //$NON-NLS-1$
				Method findMethod = platformClass.getMethod(
						"find", new Class[] { bundleClass, ipathClass }); //$NON-NLS-1$
				return (URL) findMethod.invoke(null, new Object[] { bundle,
						path });
			}
		} catch (Throwable e) {
			// Ignore any exceptions
		}
		// else work with 'plugin' as with usual Eclipse plugin
		{
			Class pluginClass = Class
					.forName("org.eclipse.core.runtime.Plugin"); //$NON-NLS-1$
			if (pluginClass.isAssignableFrom(plugin.getClass())) {
				//
				Class ipathClass = Class
						.forName("org.eclipse.core.runtime.IPath"); //$NON-NLS-1$
				Class pathClass = Class
						.forName("org.eclipse.core.runtime.Path"); //$NON-NLS-1$
				Constructor pathConstructor = pathClass
						.getConstructor(new Class[] { String.class });
				Object path = pathConstructor
						.newInstance(new Object[] { name });
				//
				Method findMethod = pluginClass.getMethod(
						"find", new Class[] { ipathClass }); //$NON-NLS-1$
				return (URL) findMethod.invoke(plugin, new Object[] { path });
			}
		}
		return null;
	}
}