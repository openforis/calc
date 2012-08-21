/**
 * 
 */
package org.openforis.calc.web.viewmodel;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Scanner;

import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.openforis.calc.transformation.ImportPlots;
import org.openforis.calc.transformation.ImportTrees;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.media.Media;
import org.zkoss.zhtml.Fileupload;
import org.zkoss.zk.ui.WebApps;

/**
 * @author Mino Togna
 * 
 */
public class ImportDataViewModel extends AbstractViewModel{

	private static final String PLOT_CSV = "plot.csv";
	private static final String PLOT_TMP_CSV = "plot_tmp.csv";
	private static final String IMPORT_PLOTS_TRANS_NAME = "ImportPlots.ktr";

	private static final String TREE_CSV = "tree.csv";
	private static final String TREE_TMP_CSV = "tree_tmp.csv";
	private static final String IMPORT_TREES_TRANS_NAME = "ImportTrees.ktr";

	private Date lastImportPlots;
	private Date lastImportTrees;

	@Command("uploadPlots")
	public synchronized void uploadPlots() throws IOException {
		uploadFile(PLOT_TMP_CSV);
	}

	@Command("uploadTrees")
	public synchronized void uploadTrees() throws IOException {
		uploadFile(TREE_TMP_CSV);
	}

	private void uploadFile(String destinationFile) throws IOException {
		Media[] medias = Fileupload.get(-1);

		if ( medias != null && medias.length > 0 ) {

			Media media = medias[0];
			String contentType = media.getContentType();
			if ( !contentType.equals("text/csv") ) {
				throw new IllegalArgumentException("Only csv file are allowed");
			}

			Reader reader = media.getReaderData();
			Scanner scanner = new Scanner(reader);
			StringBuilder sb = new StringBuilder();
			while ( scanner.hasNextLine() ) {
				String line = scanner.nextLine();
				sb.append(line);
				sb.append("\n");
			}
			scanner.close();
			reader.close();

			File dir = getUploadDir();
			FileUtils.write(new File(dir, destinationFile), sb.toString(), Charset.forName("UTF-8"));
		}
	}

	@Command("import")
	@NotifyChange({ "lastImportTrees", "lastImportPlots" })
	public synchronized void importData() {
		// Import plots
		File plotTmp = new File(getUploadDir(), PLOT_TMP_CSV);
		try {
			if ( plotTmp.exists() ) {

				long lastModified = plotTmp.lastModified();
				plotTmp.renameTo(new File(getUploadDir(), PLOT_CSV));

				ImportPlots trans = getImportPlotsTrans();
				trans.execute(null);

				lastImportPlots = new Date(lastModified);
			}
			System.gc();
		} catch ( Exception e ) {
			plotTmp.renameTo(new File(getUploadDir(), PLOT_TMP_CSV));
			error("Error while importing plots", e);
			throw new RuntimeException("Error while importing plots", e);
		}

		// Import Trees
		File treeTmp = new File(getUploadDir(), TREE_TMP_CSV);
		try {
			if ( treeTmp.exists() ) {

				long lastModified = treeTmp.lastModified();
				treeTmp.renameTo(new File(getUploadDir(), TREE_CSV));

				ImportTrees trans = getImportTreesTrans();
				trans.execute(null);

				lastImportTrees = new Date(lastModified);
			}
			System.gc();
		} catch ( Exception e ) {
			treeTmp.renameTo(new File(getUploadDir(), TREE_TMP_CSV));
			error("Error while importing trees", e);
			throw new RuntimeException("Error while importing trees", e);
		}

	}

	@Init
	public void initLastImpotDates() {
		File uploadDir = getUploadDir();
		File plot = new File(uploadDir, PLOT_CSV);
		if ( plot.exists() ) {
			lastImportPlots = new Date(plot.lastModified());
		}

		File tree = new File(uploadDir, TREE_CSV);
		if ( tree.exists() ) {
			lastImportTrees = new Date(tree.lastModified());
		}
	}

	private ImportPlots getImportPlotsTrans() {
		File kettleFile = new File(getKettleDir(), IMPORT_PLOTS_TRANS_NAME);
		File plotFile = new File(getUploadDir(), PLOT_CSV);

		return ImportPlots.getInstance(kettleFile.getAbsolutePath(), plotFile.getAbsolutePath());
	}

	private ImportTrees getImportTreesTrans() {
		File kettleFile = new File(getKettleDir(), IMPORT_TREES_TRANS_NAME);
		File plotFile = new File(getUploadDir(), TREE_CSV);

		return ImportTrees.getInstance(kettleFile.getAbsolutePath(), plotFile.getAbsolutePath());
	}

	public Date getLastImportPlots() {
		return lastImportPlots;
	}

	public Date getLastImportTrees() {
		return lastImportTrees;
	}

	private static File getKettleDir() {
		ServletContext servletContext = WebApps.getCurrent().getServletContext();
		String dirName = servletContext.getRealPath("WEB-INF/kettle");
		File dir = new File(dirName);
		return dir;
	}

	private static File getUploadDir() {
		ServletContext servletContext = WebApps.getCurrent().getServletContext();
		String dirName = servletContext.getRealPath("WEB-INF/upload");
		File dir = new File(dirName);
		return dir;
	}

}
