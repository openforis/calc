/**
 * 
 */
package org.openforis.calc.persistence.jooq.rolap;

import java.util.ArrayList;
import java.util.List;

import mondrian.olap.MondrianDef.CalculatedMember;
import mondrian.olap.MondrianDef.Cube;
import mondrian.olap.MondrianDef.CubeDimension;
import mondrian.olap.MondrianDef.Measure;
import mondrian.olap.MondrianDef.VirtualCube;
import mondrian.olap.MondrianDef.VirtualCubeDimension;
import mondrian.olap.MondrianDef.VirtualCubeMeasure;

import org.openforis.calc.model.ObservationUnitMetadata;

/**
 * @author M. Togna
 * 
 */
//TODO check if it has to be generic
public class SpecimenVirtualCubeGenerator {

	private Cube cube;
	private ObservationUnitMetadata unit;
	private MondrianDefFactory mdf;
	private VirtualCube virtualCube;

	private List<String> excludedPerHaEstimateMeasures;
	
	public SpecimenVirtualCubeGenerator(SpecimenCubeGenerator cubeGenerator) {
		RolapSchemaGenerator schemaGenerator = cubeGenerator.getSchemaGenerator();
		this.mdf = schemaGenerator.getMondrianDefFactory();
		this.cube = cubeGenerator.getCube();
		this.unit = cubeGenerator.getObservationUnitMetadata();
		
		excludedPerHaEstimateMeasures = new ArrayList<String>();
		excludedPerHaEstimateMeasures.add( MondrianDefFactory.toMdxName(SpecimenFactTable.MEASURE_INCLUSION_AREA).toUpperCase() );
		excludedPerHaEstimateMeasures.add( MondrianDefFactory.toMdxName(SpecimenFactTable.MEASURE_PLOT_SECTION_AREA).toUpperCase() );
		excludedPerHaEstimateMeasures.add( MondrianDefFactory.toMdxName(SpecimenFactTable.MEASURE_COUNT).toUpperCase() );
	}

	public VirtualCube createCube() {
		virtualCube = mdf.createVirtualCube(unit.getObsUnitName());

		initDimensions();
		initMeasures();
		initCalculatedMembers();

		return virtualCube;
	}

	private boolean hasPerHaEstimate(Measure measure) {
		String name = measure.name.toUpperCase();
		return !excludedPerHaEstimateMeasures.contains(name);
	}

	private void initMeasures() {
		List<VirtualCubeMeasure> virtualCubeMeasures = new ArrayList<VirtualCubeMeasure>();
		
		for ( Measure measure : cube.measures ) {
			String name = measure.name;
			VirtualCubeMeasure m = mdf.createVirtualCubeMeasure(cube.name, name, measure.visible);
			virtualCubeMeasures.add(m);
		}
		String cubeName = MondrianDefFactory.toMdxName( unit.getObsUnitParent().getObsUnitName() );
		VirtualCubeMeasure m = mdf.createVirtualCubeMeasure(cubeName, PlotFactTable.MEASURE_EST_AREA, true);
		virtualCubeMeasures.add(m);
		
		virtualCube.measures = virtualCubeMeasures.toArray(new VirtualCubeMeasure[0]);
	}

	private void initDimensions() {
		List<VirtualCubeDimension> virtualCubeDimensions = new ArrayList<VirtualCubeDimension>();
		for ( CubeDimension dim : cube.dimensions ) {
			VirtualCubeDimension d = mdf.createVirtualCubeDimension(cube.name, dim.name);
			virtualCubeDimensions.add(d);
		}
		virtualCube.dimensions = virtualCubeDimensions.toArray(new VirtualCubeDimension[0]);
	}
	
	private void initCalculatedMembers() {
		List<CalculatedMember> calculatedMembers = new ArrayList<CalculatedMember>();

		CalculatedMember resultArea = mdf.createResultAreaCalculatedMember(unit.getDimensionTableName(), unit.getObsUnitParent().getDimensionTableName());
		calculatedMembers.add(resultArea);

		for ( Measure measure : cube.measures ) {
			if( hasPerHaEstimate(measure) ) {
				CalculatedMember m = mdf.createPerHaCalculatedMember(measure.name);
				calculatedMembers.add(m);
			}
		}
		virtualCube.calculatedMembers = calculatedMembers.toArray(new CalculatedMember[0]);
	}
}
