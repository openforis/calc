package org.openforis.calc.persistence;

import java.io.IOException;

import org.jooq.Field;
import org.jooq.impl.Factory;
import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.io.flat.FlatRecord;
import org.openforis.calc.model.Specimen;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import static org.openforis.calc.persistence.jooq.Tables.SPECIMEN;
import org.openforis.calc.persistence.jooq.tables.records.SpecimenRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Component 
@Transactional
public class SpecimenDao extends JooqDaoSupport<SpecimenRecord, Specimen> {

	public SpecimenDao() {
		super(SPECIMEN, Specimen.class, SPECIMEN.PLOT_SECTION_ID, SPECIMEN.SPECIMEN_NO);
	}
	
	public void importSpecimenData(FlatDataStream stream) throws IOException {
		Factory create = getJooqFactory();
		FlatRecord r = null;
		while ( (r=stream.nextRecord()) != null ) {
			Object[] keys = extractKeys(r);
			Integer id = getIdByKey(keys);
			SpecimenRecord rec = create.newRecord(SPECIMEN);
			copyFields(r, rec, SPECIMEN.SPECIMEN_NO, SPECIMEN.SPECIMEN_SURVEY_DATE);
		}
	}

	protected Object[] extractKeys(FlatRecord r) {
		
		return null;
	}

	protected void copyFields(FlatRecord r, SpecimenRecord rec, Field<?>... fields) {
		for (Field<?> field : fields) {
			String name = field.getName();
			if ( field.getType().isAssignableFrom(Integer.class) ) {
				Integer value = r.getInteger(name);
				copyField(rec, field, value);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void copyField(SpecimenRecord rec, Field field, Object value) {
		rec.setValue(field, value);
	}
}