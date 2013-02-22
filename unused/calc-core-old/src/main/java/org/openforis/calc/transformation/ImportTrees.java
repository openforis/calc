/**
 * 
 */
package org.openforis.calc.transformation;

/**
 * @author M. Togna
 * 
 */
public class ImportTrees extends AbstractTransformation {

	private static ImportTrees INSTANCE;

	public static ImportTrees getInstance(String transformationFile, String inputFileName) {
		if ( INSTANCE == null ) {
			INSTANCE = new ImportTrees(transformationFile, inputFileName);
		}
		return INSTANCE;
	}

	ImportTrees(String transformationFile, String inputFileName) {
		super(transformationFile, inputFileName);
	}

}
