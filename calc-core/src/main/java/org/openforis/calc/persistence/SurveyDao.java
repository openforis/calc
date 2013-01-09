package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.*;

import org.jooq.Field;
import org.jooq.Result;
import org.jooq.impl.Factory;
import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.model.Survey;
import org.openforis.calc.persistence.jooq.DialectAwareJooqFactory;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.tables.records.SurveyRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Component
@Transactional
public class SurveyDao extends JooqDaoSupport<SurveyRecord, Survey> {

	public SurveyDao() {
		super(SURVEY, Survey.class);
	}
	
	public Survey findByUri(String uri) {
		return fetchOne(SURVEY.URI, uri);
	}

	public Survey findByName(String name) {
		return fetchOne(SURVEY.NAME, name);
	}

//	public Integer findIdByName(String name) {
//		Factory create = getJooqFactory();
//		return create.select(SURVEY.ID)
//				.from(SURVEY)
//				.where(SURVEY.NAME.eq(name))
//				.fetchOne(SURVEY.ID);
//	}
	
	public FlatDataStream streamByName(String[] fieldNames, String name) {
		return stream(fieldNames, SURVEY.NAME, name);
	}
	
	public FlatDataStream streamAll(String[] fieldNames) {
		Field<?>[] fields = getFields(fieldNames);
		return streamAll(fields);
	}
	
	private FlatDataStream streamAll(Field<?>[] fields) {
		DialectAwareJooqFactory create = getJooqFactory();
		Result<?> result = create.select(fields).from(SURVEY).fetch();
		return stream(result);
	}

}
