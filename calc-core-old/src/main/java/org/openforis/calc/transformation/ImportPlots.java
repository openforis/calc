/**
 * 
 */
package org.openforis.calc.transformation;

/**
 * @author Mino Togna
 * 
 */
public class ImportPlots extends AbstractTransformation {

	private static ImportPlots INSTANCE;

	public static ImportPlots getInstance(String transformationFile, String inputFileName) {
		if ( INSTANCE == null ) {
			INSTANCE = new ImportPlots(transformationFile, inputFileName);
		}
		return INSTANCE;
	}

	ImportPlots(String transformationFile, String inputFileName) {
		super(transformationFile, inputFileName);
	}

}
