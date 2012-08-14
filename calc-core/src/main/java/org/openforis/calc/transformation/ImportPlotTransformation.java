/**
 * 
 */
package org.openforis.calc.transformation;


/**
 * @author Mino Togna
 * 
 */
public class ImportPlotTransformation extends AbstractTransformation {

	private static ImportPlotTransformation INSTANCE;

	public static ImportPlotTransformation getInstance(String transformationFile, String inputFileName) {
		if ( INSTANCE == null ) {
			INSTANCE = new ImportPlotTransformation(transformationFile, inputFileName);
		}
		return INSTANCE;
	}

	ImportPlotTransformation(String transformationFile, String inputFileName) {
		super(transformationFile, inputFileName);
	}

	@Override
	String getName() {
		return "import_plot.ktr";
	}

}
