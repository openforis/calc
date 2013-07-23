package org.openforis.calc.system;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

/**
 * 
 * @author G. Miceli
 *
 */
public abstract class SystemUtils {

	private SystemUtils() {
	}
	
	public static void addToClassPath(File file) {
		if ( !file.exists() ) {
			throw new RuntimeException(file+" not found");
		}
		try {
		    Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
		    method.setAccessible(true);
		    method.invoke(ClassLoader.getSystemClassLoader(), new Object[]{file.toURI().toURL()});
		} catch (Exception e) {
			throw new RuntimeException("Error adding JRI.jar to classpath");
		}
	}
	
	/**
	* Adds the specified path to the java library path
	*
	* Based on origin by Fahd Shariff
	* @param pathToAdd the path to add
	* @throws Exception
	*/
	public static void addLibraryPath(String pathToAdd) {
		try {
		    final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
		    usrPathsField.setAccessible(true);
		 
		    //get array of paths
		    final String[] paths = (String[]) usrPathsField.get(null);
		 
		    //check if the path to add is already present
		    for(String path : paths) {
		        if(path.equals(pathToAdd)) {
		            return;
		        }
		    }
		 
		    //add the new path
		    final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
		    newPaths[newPaths.length-1] = pathToAdd;
		    usrPathsField.set(null, newPaths);
		} catch ( Exception e ) {
			throw new RuntimeException("Could not add "+pathToAdd+" to library path");
		}
	}
}
