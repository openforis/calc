package org.openforis.calc.schema;

import static org.jooq.impl.SQLDataType.INTEGER;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.TableField;
import org.jooq.impl.PrimarySamplingUnitTable;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiLevel;
import org.openforis.calc.metadata.SamplingDesign;
import org.openforis.calc.psql.Psql;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 *
 */
public class ExpansionFactorTable extends AbstractTable {

	private static final long serialVersionUID = 1L;
	private static final String TABLE_NAME = "_%s_expf";
	
	public final TableField<Record,Integer> STRATUM = createField( "stratum", INTEGER, this );
	public final TableField<Record,Integer> AOI_ID; //= createField( "aoi_id", INTEGER, this );
	public final TableField<Record,BigDecimal> WEIGHT = createField( "su_weight", Psql.DOUBLE_PRECISION, this );
	public final TableField<Record,BigDecimal> CLUSTER_WEIGHT = createField( "cluster_weight", Psql.DOUBLE_PRECISION, this );
	public final TableField<Record,BigDecimal> PROPORTION = createField("proportion", Psql.DOUBLE_PRECISION, this );
	public final TableField<Record,BigDecimal> AREA = createField( "area", Psql.DOUBLE_PRECISION, this );
	public final TableField<Record,BigDecimal> EXPF = createField( "expf", Psql.DOUBLE_PRECISION, this );
	
	// for two stages sampling
	public List<TableField<Record,?>> PSU_IDS 						= null;
	public final TableField<Record,BigDecimal> PSU_TOTAL 			= createField( "psu_total", Psql.DOUBLE_PRECISION, this );
	public final TableField<Record,BigDecimal> PSU_SAMPLED_TOTAL 	= createField( "psu_sampled_total", Psql.DOUBLE_PRECISION, this );
	public final TableField<Record,BigDecimal> PSU_AREA 			= createField( "psu_area", Psql.DOUBLE_PRECISION, this );
	public final TableField<Record,BigDecimal> SSU_COUNT			= createField( "ssu_count", Psql.DOUBLE_PRECISION, this );

	public final TableField<Record,BigDecimal> SSU_TOTAL			= createField( "ssu_total", Psql.DOUBLE_PRECISION, this );
	public final TableField<Record,BigDecimal> BU_TOTAL				= createField( "bu_total", Psql.DOUBLE_PRECISION, this );
	public final TableField<Record,BigDecimal> NO_THEORETICAL_BU	= createField( "no_theoretical_bu", Psql.DOUBLE_PRECISION, this );
	
	private AoiLevel aoiLevel;
	
//	@SuppressWarnings("unchecked")
//	private final UniqueKey<Record> primaryKey = KeyFactory.newUniqueKey(this, STRATUM_ID, AOI_ID, ENTITY_ID);
	@Deprecated
	public final TableField<Record,Integer> ENTITY_ID = createField("entity_id", INTEGER, this);
	@Deprecated
	public final TableField<Record,Integer> STRATUM_ID = createField("stratum_ID", INTEGER, this);
	
	protected ExpansionFactorTable(AoiLevel level, Schema schema) {
		super( String.format(TABLE_NAME, level.getNormalizedName()), schema);
		this.aoiLevel = level;
		AOI_ID = createField( level.getFkColumn(), INTEGER, this );
		
		Workspace workspace = aoiLevel.getHierarchy().getWorkspace();
		if( workspace.has2StagesSamplingDesign() ){
			this.PSU_IDS = new ArrayList<TableField<Record,?>>();
//			String psuIdColumn = workspace.getSamplingDesign().getTwoStagesSettingsObject().getPsuIdColumns();
			SamplingDesign samplingDesign = workspace.getSamplingDesign();
			PrimarySamplingUnitTable<?> psuTable = samplingDesign.getPrimarySamplingUnitTable();
			List<Field<?>> psuIdColumn = psuTable.getPsuFields();
			for (Field<?> psuIdField : psuIdColumn) {
				TableField<Record,?> field = super.copyField( psuIdField );
				this.PSU_IDS.add( field );
			}
//			PSU_ID = createField( psuIdColumn , INTEGER, this );
		}
	}
	
	@Deprecated
	protected ExpansionFactorTable(Schema schema) {
		super(TABLE_NAME, schema);
		AOI_ID = createField( "null", INTEGER, this );
	}

	public AoiLevel getAoiLevel() {
		return aoiLevel;
	}
	
//	@Override
//	public UniqueKey<Record> getPrimaryKey() {
//		return primaryKey;
//	}
}
