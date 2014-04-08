/**
 * 
 */
package org.openforis.calc.metadata;

import org.openforis.calc.engine.Workspace;
import org.openforis.calc.persistence.jooq.Sequences;
import org.openforis.calc.persistence.jooq.tables.daos.SamplingDesignDao;
import org.openforis.calc.psql.Psql;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Mino Togna
 *
 */
@Repository
public class SamplingDesignManager {

	@Autowired
	private SamplingDesignDao samplingDesignDao;
	
	@Autowired
	private Psql psql;
	
	@Transactional
	public void delete( Workspace workspace ) {
		SamplingDesign sd = workspace.getSamplingDesign();
		if( sd != null ) {
			Integer id = sd.getId();
			samplingDesignDao.deleteById( id );
		}
		workspace.setSamplingDesign( null );
	}
	
	@Transactional
	public void insert( Workspace workspace , SamplingDesign samplingDesign ) {
		Long nextval = psql.nextval( Sequences.SAMPLING_DESIGN_ID_SEQ );
		samplingDesign.setId( nextval.intValue() );
		
		workspace.setSamplingDesign( samplingDesign );
		
		samplingDesignDao.insert( samplingDesign );
	}
	
}
