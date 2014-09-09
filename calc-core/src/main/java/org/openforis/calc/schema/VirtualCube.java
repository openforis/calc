package org.openforis.calc.schema;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.jooq.Field;
import org.openforis.calc.mondrian.Rolap;
import org.openforis.commons.collection.CollectionUtils;

/**
 * 
 * @author M. Togna
 * 
 */
public class VirtualCube {

	private static String AREA_CALCULATED_MEMBER_NAME = "AREA";
	
//	private RolapSchema rolapSchema;
	private Cube childCube;
	private Cube samplingUnitCube;
	private List<CubeUsage> cubeUsages;
	private List<VirtualCubeDimension> virtualCubeDimensions;
	private List<VirtualCubeMeasure> virtualCubeMeasures;
	private List<CalculatedMember> calculatedMembers;
	private String name;
	private Measure areaMeasure;
	
	public VirtualCube(Cube samplingUnitCube, Cube childCube) {
		this.samplingUnitCube = samplingUnitCube;
//		this.rolapSchema = this.samplingUnitCube.getRolapSchema();
		this.childCube = childCube;
		this.name = this.childCube.getFactTable().getEntity().getName();
		
		//TODO suCube.getAreaMeasure()
		Map<Measure, Field<BigDecimal>> map = this.samplingUnitCube.getMeasures();
		this.areaMeasure = map.keySet().iterator().next();
		
		initCubeUsages();
		initVirtualCubeDimensions();
		initVirtualCubeMeasures();
		initCalculatedMembers();
	}

	private void initCubeUsages() {
		cubeUsages = new ArrayList<CubeUsage>();
		
		cubeUsages.add( new CubeUsage(this.samplingUnitCube.getName()) );
		cubeUsages.add( new CubeUsage(this.childCube.getName()) );
	}
	
	public Cube getChildCube() {
		return childCube;
	}
	
	public Cube getSamplingUnitCube() {
		return samplingUnitCube;
	}
	
	private void initVirtualCubeDimensions() {
		this.virtualCubeDimensions = new ArrayList<VirtualCube.VirtualCubeDimension>();
		
		String samplingUnitCubeName = this.samplingUnitCube.getName();
		
		// aoi dims
		Map<AoiDimension, Field<Integer>> aoiDimensionUsages = this.samplingUnitCube.getAoiDimensionUsages();
		for ( AoiDimension aoiDim : aoiDimensionUsages.keySet() ) {
			addVirtualCubeDimension( samplingUnitCubeName , aoiDim.getName() );
		}
		//stratum dim
		if( this.samplingUnitCube.getStratumDimension() != null ){
			addVirtualCubeDimension( samplingUnitCubeName , this.samplingUnitCube.getStratumDimension().getName() );
		}
		// other dims
		for( Dimension dimension : this.samplingUnitCube.getDimensionUsages().keySet() ){
			addVirtualCubeDimension( samplingUnitCubeName , dimension.getName() );
		}
		for( Dimension dimension : this.childCube.getDimensionUsages().keySet() ){
			addVirtualCubeDimension(  this.childCube.getName() , dimension.getName() );
		}
	}
	
	private void addVirtualCubeDimension(String cubeName, String name) {
		VirtualCubeDimension virtualCubeDimension = new VirtualCubeDimension( cubeName, name );
		if( !this.virtualCubeDimensions.contains(virtualCubeDimension) ){
			this.virtualCubeDimensions.add(virtualCubeDimension);
		}
	}

	private void initVirtualCubeMeasures() {
		this.virtualCubeMeasures = new ArrayList<VirtualCube.VirtualCubeMeasure>();
		
		Map<Measure, Field<BigDecimal>> measures = this.childCube.getMeasures();
		for (Measure measure : measures.keySet()) {
			addVirtualMeasure( measure, true );
		}
		// add error measures
		for ( Measure measure : this.childCube.getErrorMeasures() ){
			addVirtualMeasure( measure, true );
		}
		// add virtual area measure
		addVirtualMeasure(areaMeasure, true );
	}

	private void addVirtualMeasure(Measure measure, boolean visible) {
		String cubeName = measure.getCube().getName();
		String name = measure.getName();
		VirtualCubeMeasure virtualMeasure = new VirtualCubeMeasure(cubeName , name, visible);
		this.virtualCubeMeasures.add(virtualMeasure);
	}

	private void initCalculatedMembers() {
		this.calculatedMembers = new ArrayList<VirtualCube.CalculatedMember>();
		
		String formula = Rolap.validMeasure( Rolap.getMdxMeasureName(this.areaMeasure.getName()) );
		CalculatedMember areaClaculatedMember = new CalculatedMember( Rolap.MEASURES, AREA_CALCULATED_MEMBER_NAME, "Area / Ha", formula , false );
		this.calculatedMembers.add( areaClaculatedMember );
		
		Map<Measure, Field<BigDecimal>> measures = this.childCube.getMeasures();
		for ( Measure measure : measures.keySet() ) {
			
			String name = measure.getName()+" "+ Measure.HA;
			String caption = measure.getCaption() + " " + Measure.HA;
			formula = Rolap.getMdxMeasureName( measure.getName() ) + " / " + Rolap.getMdxMeasureName( AREA_CALCULATED_MEMBER_NAME );
			
			CalculatedMember calculatedMember = new CalculatedMember( Rolap.MEASURES, name, caption, formula, true);
			this.calculatedMembers.add( calculatedMember  );
		}
	}

	public String getName() {
		return this.name;
	}

	public Collection<CubeUsage> getCubeUsages(){
		return CollectionUtils.unmodifiableCollection( this.cubeUsages );
	}

	public Collection<VirtualCubeDimension> getVirtualCubeDimensions() {
		return CollectionUtils.unmodifiableCollection( virtualCubeDimensions );
	}
	
	public Collection<VirtualCubeMeasure> getVirtualCubeMeasures() {
		ArrayList<VirtualCubeMeasure> list = new ArrayList<VirtualCubeMeasure>( virtualCubeMeasures );
		list.sort( new Comparator<VirtualCubeMeasure>() {

			@Override
			public int compare(VirtualCubeMeasure o1, VirtualCubeMeasure o2) {
				String o1Name = o1.getName();
				String o2Name = o2.getName();
				int compareTo = o1Name.compareTo(o2Name);
				return compareTo;
			}
		});
		return CollectionUtils.unmodifiableCollection( list );
	}
	
	public Collection<CalculatedMember> getCalculatedMembers() {
		return CollectionUtils.unmodifiableCollection( calculatedMembers );
	}
	
	
	
	public class CubeUsage {
		private String cubeName;

		public CubeUsage(String cubeName) {
			this.cubeName = cubeName;
		}
		
		public String getCubeName() {
			return cubeName;
		}
	}
	
	
	public class VirtualCubeDimension {
		private String cubeName;
		private String name;
		public VirtualCubeDimension(String cubeName, String name) {
			this.cubeName = cubeName;
			this.name = name;
		}
		
		public String getCubeName() {
			return cubeName;
		}
		public String getName() {
			return name;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			VirtualCubeDimension other = (VirtualCubeDimension) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}

		private VirtualCube getOuterType() {
			return VirtualCube.this;
		}
		
	}
	
	
	public class VirtualCubeMeasure {
		private String cubeName;
		private String name;
		private boolean visible;

		public VirtualCubeMeasure(String cubeName, String name, boolean visible) {
			this.cubeName = cubeName;
			this.name = name;
			this.visible = visible;
		}

		public String getCubeName() {
			return cubeName;
		}
		public String getName() {
			return name;
		}
		public boolean isVisible() {
			return visible;
		}
		
	}

	
	public class CalculatedMember {
		private String name;
		private String caption;
		private String formula;
		private boolean visible;
		private String dimension;
		
		public CalculatedMember( String dimension, String name, String caption, String formula, boolean visible ) {
			super();
			this.name = name;
			this.caption = caption;
			this.formula = formula;
			this.visible = visible;
			this.dimension = dimension;
		}

		public String getName() {
			return name;
		}

		public String getCaption() {
			return caption;
		}

		public String getFormula() {
			return formula;
		}

		public boolean isVisible() {
			return visible;
		}

		public String getDimension() {
			return dimension;
		}
	}

	
}
