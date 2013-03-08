package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.*;

import org.jooq.impl.Factory;
import org.openforis.calc.model.SpecimenCategoricalValue;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.Specimen;
import org.openforis.calc.persistence.jooq.tables.records.SpecimenCategoricalValueRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Component
@Transactional
public class SpecimenCategoricalValueDao extends JooqDaoSupport<SpecimenCategoricalValueRecord, SpecimenCategoricalValue> {

	public SpecimenCategoricalValueDao() {
		super(Tables.SPECIMEN_CATEGORICAL_VALUE, SpecimenCategoricalValue.class);
	}

	public void deleteByObsUnit(int id) {
		Factory create = getJooqFactory();
		org.openforis.calc.persistence.jooq.tables.SpecimenCategoricalValue v = SPECIMEN_CATEGORICAL_VALUE.as("v");
		Specimen o = SPECIMEN.as("o");
		create.delete(v)
			  .where(v.SPECIMEN_ID.in(
					  	create.select(o.SPECIMEN_ID)
					  		  .from(o)
					  		  .where(o.OBS_UNIT_ID.eq(id))));
	}
}
