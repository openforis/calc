/**
 * 
 */
package org.openforis.calc.schema;

import java.util.ArrayList;
import java.util.List;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectQuery;
import org.openforis.calc.engine.DataRecord;
import org.openforis.calc.engine.DataRecordVisitor;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.persistence.jooq.AbstractJooqDao;
import org.springframework.stereotype.Repository;

/**
 * @author Mino Togna
 * 
 */
@Repository
public class EntityDataViewDao extends AbstractJooqDao {

	public List<DataRecord> query(DataRecordVisitor visitor, Workspace workspace, Integer offset, Integer numberOfRows, String entityName, String... fields) {
		if (fields == null || fields.length == 0) {
			throw new IllegalArgumentException("fields must be specifed");
		}

		List<DataRecord> records = new ArrayList<DataRecord>();

		Schemas schemas = new Schemas(workspace);
		Entity entity = workspace.getEntityByName(entityName);
		EntityDataView view = schemas.getInputSchema().getDataView(entity);

		// prepare query
		SelectQuery<Record> select = psql().selectQuery();
		select.addFrom(view);
		select.addSelect(view.getIdField());
		for (String field : fields) {
			select.addSelect(view.field(field));
		}
		// add offset to query
		if (offset != null && numberOfRows != null) {
			select.addLimit(offset, numberOfRows);
		} else if (offset == null && numberOfRows != null) {
			select.addLimit(0, numberOfRows);
		} else if (offset != null && numberOfRows == null) {
			select.addLimit(offset);
		}
		// execute the query
		Result<Record> result = select.fetch();

		// process results
		for (Record record : result) {
			Long id = record.getValue(view.getIdField().getName(), Long.class);
			DataRecord dataRecord = new DataRecord(id);
			for (String field : fields) {
				Object value = record.getValue(field);
				dataRecord.add(field, value);
			}
			if (visitor != null) {
				visitor.visit(dataRecord);
			}
		}
		return records;
	}

}
