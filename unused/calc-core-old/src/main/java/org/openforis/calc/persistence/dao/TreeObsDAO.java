/**
 * 
 */
package org.openforis.calc.persistence.dao;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Query;
import javax.sql.DataSource;

import org.openforis.calc.model.TreeObs;
import org.openforis.calc.model.TreeObsView;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 * 
 */
public class TreeObsDAO extends AbstractDAO {

	@Transactional
	public List<TreeObs> getAll() {
		Query query = createQuery("SELECT t FROM " + TreeObs.class.getName() + " t");
		@SuppressWarnings("unchecked")
		List<TreeObs> trees = query.getResultList();
		return trees;
	}

	@Transactional
	public List<TreeObsView> getAllFromView() {
		Query query = createQuery("SELECT t FROM " + TreeObsView.class.getName() + " t");
		@SuppressWarnings("unchecked")
		List<TreeObsView> trees = query.getResultList();
		return trees;
	}

	public void batchUpdate(List<TreeObsView> treesObs, String propertyName) {
		DataSource ds = getDataSource();
		Connection connection = null;
		PreparedStatement ps = null;
		SQLException exception = null;
		try {
			connection = ds.getConnection();
			connection.setAutoCommit(false);
			PropertyDescriptor descriptor = null;
			try {
				descriptor = getPropertyDescriptor(propertyName);
			} catch ( IntrospectionException e ) {
				throw new RuntimeException("Unable to get property descriptor for property " + propertyName, e);
			}
			String colName = getColumnName(descriptor);
			Method method = descriptor.getReadMethod();

			ps = connection.prepareStatement("update calc.tree_obs set " + colName + " = ? where id = ?");
			for ( TreeObsView treeObsView : treesObs ) {
				BigDecimal value = null;
				try {
					value = (BigDecimal) method.invoke(treeObsView);
				} catch ( Exception e ) {
					throw new RuntimeException("Error while invoking getter method on property " + propertyName, e);
				}
				ps.setBigDecimal(1, value);
				ps.setInt(2, treeObsView.getId());
				ps.addBatch();
			}
			ps.executeBatch();
			connection.commit();
		} catch ( SQLException e ) {
			exception = e;
		} finally {
			if ( connection != null )
				try {
					connection.close();
				} catch ( SQLException e ) {}
			if ( ps != null )
				try {
					ps.close();
				} catch ( SQLException e ) {}
		}
		if ( exception != null ) {
			throw new RuntimeException("Error while updating tree heights", exception);
		}
	}

	private String getColumnName(PropertyDescriptor propertyDescriptor) {
		Method method = propertyDescriptor.getReadMethod();
		Column column = method.getAnnotation(Column.class);
		return column.name();
	}

	private PropertyDescriptor getPropertyDescriptor(String propertyName) throws IntrospectionException {
		BeanInfo beanInfo = Introspector.getBeanInfo(TreeObsView.class);
		PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
		for ( PropertyDescriptor propertyDescriptor : descriptors ) {
			if ( propertyName.equals(propertyDescriptor.getName()) ) {
				return propertyDescriptor;
			}
		}
		return null;
	}
}
