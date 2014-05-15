package org.openforis.calc.schema;

import java.util.List;

import org.jooq.impl.SQLDataType;
import org.openforis.calc.metadata.Category;
import org.openforis.calc.metadata.CategoryHierarchy;
import org.openforis.calc.metadata.CategoryLevel;
import org.openforis.calc.metadata.MultiwayVariable;

/**
 * 
 * @author Mino Togna
 * @author S. Ricci
 */
public class CategoryDimensionTable extends DimensionTable {

	private static final long serialVersionUID = 1L;

	private MultiwayVariable variable;

	CategoryDimensionTable(RelationalSchema schema, MultiwayVariable variable) {
		super( getLevel(variable).getTableName() , schema );

		this.variable = variable;

		initFields();
	}

	private static CategoryLevel getLevel(MultiwayVariable variable) {
		Category category = variable.getCategory();
		List<CategoryHierarchy> hierarchies = category.getHierarchies();
		CategoryHierarchy hierarchy = hierarchies.get(0);
		List<CategoryLevel> levels = hierarchy.getLevels();
		CategoryLevel categoryLevel = levels.get(0);
		return categoryLevel;
	}

	public MultiwayVariable getVariable() {
		return variable;
	}

	@Override
	protected void initFields() {
		CategoryLevel level = getLevel( getVariable() );
		
		setIdField( createField(level.getIdColumn(), SQLDataType.INTEGER, this) );
		setCaptionField( createField(level.getCaptionColumn() , SQLDataType.VARCHAR, this) );
		setCodeField( createField(level.getCodeColumn() , SQLDataType.VARCHAR, this) );
	}
}
