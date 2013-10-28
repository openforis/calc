package sandbox;
import java.io.FileWriter;
import java.io.IOException;

import org.openforis.commons.io.csv.CsvLine;
import org.openforis.commons.io.csv.CsvReader;


public class SplitSpeciesNames {
	public static void main(String[] args) throws IOException {
		CsvReader in = new CsvReader("/home/gino/workspace/tzdata/products.csv");
		in.readHeaders();
		FileWriter out = new FileWriter("/home/gino/workspace/tzdata/product-species.csv");
		CsvLine line;
		while ( (line = in.readNextLine()) != null ) {
			String cat = line.getValue("category", String.class);
			String species = line.getValue("species", String.class);
			if ( species != null && !species.isEmpty() ) {
				String[] list = species.split(",");
				for (String sp : list) {
					sp = sp.trim();
//					out.write(cat == null ? "" : cat);
//					out.write(",");
					out.write(sp);
					out.write("\n");
				}
			}
		}
		out.close();
		in.close();
	}
}
