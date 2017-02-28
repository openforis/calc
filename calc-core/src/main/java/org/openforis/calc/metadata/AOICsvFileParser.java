/**
 * 
 */
package org.openforis.calc.metadata;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.calc.persistence.jooq.Sequences;
import org.openforis.calc.psql.Psql;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.commons.io.csv.CsvLine;
import org.openforis.commons.io.csv.CsvReader;

/**
 * @author M. Togna
 *
 */
public class AOICsvFileParser {

	private AoiHierarchy aoiHierarchy;
	private List<Stratum> strata;
	private Map<String,StratumAoi> strataAois;
	
	private String filepath;
	private Psql psql;

	public AOICsvFileParser(String filepath, Psql psql) {
		this.filepath 	= filepath;
		this.psql 		= psql;
		
		this.strata 		= new ArrayList<Stratum>();
		this.strataAois 	= new HashMap<String, StratumAoi>();
	}
	
	public AoiHierarchy getAoiHierarchy() {
		return aoiHierarchy;
	}
	
	public List<Stratum> getStrata() {
		return CollectionUtils.unmodifiableList( strata );
	}
	
	public Collection<StratumAoi> getStrataAois(){
		return CollectionUtils.unmodifiableCollection( strataAois.values() );
	}
	
	public void parseForImport(int levels , String[] captions , boolean hasStrata) throws IOException{
		@SuppressWarnings("resource")
		CsvReader csvReader = new CsvReader(filepath);
		csvReader.readHeaders();
		
		// create Hierarchy
		aoiHierarchy = new AoiHierarchy();
		Long aoiHierId = psql.nextval( Sequences.AOI_HIERARCHY_ID_SEQ );
		aoiHierarchy.setId( aoiHierId.intValue() );
		aoiHierarchy.setCaption("Administrative unit");
		aoiHierarchy.setName("Administrative unit");
		
		// create levels
		List<AoiLevel> aoiLevels = new ArrayList<AoiLevel>(levels);
		for(int i = 0 ; i < levels ; i++ ){
			AoiLevel level = new AoiLevel();
			Long aoiLevelId = psql.nextval( Sequences.AOI_LEVEL_ID_SEQ );
			level.setId( aoiLevelId.intValue() );
			String caption = captions[ i ];
			level.setName(caption);
			level.setCaption(caption);
			level.setRank(i);
			
			aoiLevels.add(level);
		}
		aoiHierarchy.setLevels(aoiLevels);
		
		// create aois
		CsvLine record = (CsvLine) csvReader.nextRecord();
		
		do {
			if( !record.toString().equals("[]")){
				for(int i = 0 ; i < levels ; i++ ){
					String code 		= record.getValue((i) * 2, String.class);
					String caption 		= record.getValue((i) * 2 + 1, String.class);
					
					AoiLevel aoiLevel 	= aoiLevels.get(i);
					Aoi aoi				= getOrCreateAoi( aoiLevel , code , caption );
					
					// update land area
					BigDecimal area = new BigDecimal( record.getValue( record.getLine().length - 1, Double.class ) );
					
					BigDecimal landArea = aoi.getLandArea();
					landArea = ( landArea == null ) ? area : landArea.add( area );
					aoi.setLandArea(landArea);
					
					// if not root aoi, it sets the parent aoi
					if( i != 0 ){
						String parentCode	= record.getValue( (i-1) * 2, String.class);
						Aoi parentAoi		= getOrCreateAoi( aoiLevels.get(i-1) , parentCode , null );
						parentAoi.addChild( aoi );
					}
					
					if( hasStrata ){
						Integer stratumCode 	= record.getValue("stratum_code", Integer.class );
						String stratumLabel		= record.getValue("stratum_label", String.class );
						Double stratumArea		= record.getValue("stratum_area", Double.class );
						
						Stratum stratum 		= getOrCreateStratum( stratumCode, stratumLabel );
						
						StratumAoi stratumAoi	= getOrCreateStratumAoi( stratum , aoi );
						stratumAoi.setArea( stratumAoi.getArea() + stratumArea );
					}
					
				}
			}
			record = (CsvLine) csvReader.nextRecord();
		} while (record != null);
		
		Aoi rootAoi = aoiLevels.get(0).getAois().iterator().next();
		aoiHierarchy.setRootAoi( rootAoi );
		
	}

	private StratumAoi getOrCreateStratumAoi(Stratum stratum, Aoi aoi) {
		String key = aoi.getId() + "_" + stratum.getId();
		StratumAoi stratumAoi = this.strataAois.get(key);
		if( stratumAoi == null ){
			stratumAoi 			= new StratumAoi();
			Long stratumAoiId 	= psql.nextval( Sequences.STRATUM_AOI_ID_SEQ );
			stratumAoi.setId( stratumAoiId.intValue() );
			stratumAoi.setAoi(aoi);
			stratumAoi.setStratum(stratum);
			stratumAoi.setArea( 0.0);
			
			this.strataAois.put( key , stratumAoi );
		}
		return stratumAoi;
	}

	private Stratum getOrCreateStratum(Integer stratumCode, String stratumLabel) {
		Stratum stratum = null;
		for (Stratum s : this.strata) {
			if( s.getStratumNo().equals(stratumCode) ){
				stratum = s;
				break;
			}
		}
		
		if( stratum == null ){
			stratum = new Stratum();
			Long stratumId = psql.nextval( Sequences.STRATUM_ID_SEQ );
			stratum.setId( stratumId.intValue() );
			stratum.setStratumNo(stratumCode);
			stratum.setCaption(stratumLabel);
			
			this.strata.add( stratum );
		}
		
		return stratum;
	}

	private Aoi getOrCreateAoi(AoiLevel aoiLevel, String code, String caption) {
		Aoi aoi = null;
		for (Aoi a : aoiLevel.getAois()) {
			if( a.getCode().equalsIgnoreCase(code) ){
				aoi = a;
				break;
			}
		}
		
		if( aoi == null ){
			aoi = new Aoi();
			
			Long aoiId = psql.nextval( Sequences.AOI_ID_SEQ );
			aoi.setId( aoiId.intValue() );

			aoi.setCode(code);
			aoi.setCaption(caption);

			aoiLevel.addAoi( aoi );
		}
		
		return aoi;
	}

}
