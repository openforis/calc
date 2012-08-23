/**
 * 
 */
package org.openforis.calc.operation;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.openforis.calc.persistence.dao.GenericDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Mino Togna
 * 
 */
abstract class AbstractSQLUpdateOperation extends Operation {

	private static final String FILE_EXTENSION = ".sql";

	private String sql;

	@Autowired
	private GenericDAO genericDao;

	public AbstractSQLUpdateOperation() {
	}

	@Transactional
	public void evaluate() throws OperationException {
		this.genericDao.executeNativeUpdate(sql);
	}

	@PostConstruct
	protected void init() {
		String fileName = this.getClass().getName().replaceAll("\\.", "/");
		fileName += FILE_EXTENSION;
		try {
			InputStream stream = getClass().getClassLoader().getResourceAsStream(fileName);
			StringWriter writer = new StringWriter();
			IOUtils.copy(stream, writer);
			sql = writer.toString();
			stream.close();
		} catch ( IOException e ) {
			throw new RuntimeException("Error while creating Operation", e);
		}
	}

}
