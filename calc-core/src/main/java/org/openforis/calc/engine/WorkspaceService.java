package org.openforis.calc.engine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.EntityDao;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.SamplingDesign;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.metadata.VariableAggregateDao;
import org.openforis.calc.metadata.Variable.Scale;
import org.openforis.calc.metadata.VariableAggregate;
import org.openforis.calc.metadata.VariableDao;
import org.openforis.calc.schema.InputSchemaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages {@link Workspace} instances.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
@Service
public class WorkspaceService {

	@Autowired
	private WorkspaceDao workspaceDao;
	
	@Autowired
	private EntityDao entityDao;

	@Autowired
	private VariableDao variableDao;
	
	@Autowired
	private VariableAggregateDao variableAggregateDao;
	
	@Autowired
	private InputSchemaDao inputSchemaDao;
	
	@Autowired
	private ProcessingChainService processingChainService;

	@Autowired
	private SamplingDesignDao samplingDesignDao;
	
	private Map<Integer, SimpleLock> locks;

	public WorkspaceService() {
		this.locks = new HashMap<Integer, SimpleLock>();
	}

	@Transactional
	public Workspace get(int workspaceId) {
		return workspaceDao.find(workspaceId);
	}

	@Transactional
	public Workspace fetchByName(String name) {
		return workspaceDao.fetchByName(name);
	}

	@Transactional
	public Workspace fetchCollectSurveyUri(String uri) {
		return workspaceDao.fetchByCollectSurveyUri(uri);
	}

	@Transactional
	public Workspace save(Workspace workspace) {
		return workspaceDao.save(workspace);
	}

	@Transactional
	public List<Workspace> loadAll() {
		return workspaceDao.loadAll();
	}

	/**
	 * It returns the active workspace
	 * 
	 * @return
	 */
	public Workspace getActiveWorkspace() {
		Workspace workspace = workspaceDao.fetchActive();
		return workspace;
	}

	synchronized public SimpleLock lock(int workspaceId) throws WorkspaceLockedException {
		SimpleLock lock = locks.get(workspaceId);
		if (lock == null) {
			lock = new SimpleLock();
			locks.put(workspaceId, lock);
		}
		if (!lock.tryLock()) {
			throw new WorkspaceLockedException();
		}
		return lock;
	}

	synchronized public boolean isLocked(int workspaceId) {
		SimpleLock lock = locks.get(workspaceId);
		if (lock == null) {
			return false;
		} else {
			return lock.isLocked();
		}
	}

	public Workspace createAndActivate(String name, String uri, String schema) {
		workspaceDao.deactivateAll();

		Workspace ws = new Workspace();
		ws.setActive(true);
		ws.setCollectSurveyUri(uri);
		ws.setInputSchema(schema);
		ws.setName(name);
		ws.setCaption(name);
		workspaceDao.save(ws);

		processingChainService.createDefaultProcessingChain(ws);

		return ws;
	}
	
	public QuantitativeVariable saveQuantitativeVariable(Entity entity, String name) {
		QuantitativeVariable variable = new QuantitativeVariable();
		variable.setName(name);
		variable.setInputValueColumn(name);
		variable.setOutputValueColumn(name);
		variable.setScale(Scale.RATIO);
		
		entity.addVariable(variable);

		variableDao.save(variable);
		
		inputSchemaDao.addUserDefinedVariableColumn(variable);
		inputSchemaDao.createView(entity);
		
		return variable;
	}

	public void addUserDefinedVariableColumns(Workspace ws) {
		for (Variable<?> v : ws.getUserDefinedVariables()) {
			if ( v instanceof QuantitativeVariable ) {
				inputSchemaDao.addUserDefinedVariableColumn((QuantitativeVariable) v);
			}
		}
	}

	public void activate(Workspace ws) {
		workspaceDao.deactivateAll();
		ws.setActive(true);
		workspaceDao.save(ws);
	}
	
	public void createViews(Workspace ws) {
		inputSchemaDao.createViews(ws);
	}

	@Transactional
	public Workspace setActiveWorkspaceSamplingUnit(int entityId) {
		Workspace workspace = getActiveWorkspace();
		SamplingDesign samplingDesign = workspace.getSamplingDesign();
		if(samplingDesign == null){
			samplingDesign = new SamplingDesign();
			samplingDesignDao.save(samplingDesign);
			workspace.setSamplingDesign(samplingDesign);
		}
		Entity samplingUnit = workspace.getEntityById(entityId);
		samplingDesign.setSamplingUnit(samplingUnit );
		workspace = workspaceDao.save(workspace);
		return workspace;
	}

	@Transactional
	public QuantitativeVariable createVariableAggregate(QuantitativeVariable variable, String agg) {
		if( !variable.hasAggregate(agg) ) {
			if( VariableAggregate.AGGREGATE_TYPE.isValid(agg) ) {
				VariableAggregate varAgg = new VariableAggregate();
				varAgg.setVariable(variable);
				varAgg.setAggregateType(agg);
				varAgg.setAggregateFormula("");
				variableAggregateDao.save(varAgg);
			} else {
				throw new IllegalArgumentException("Invalild aggregate type: " + agg);
			}
		}
		variable = (QuantitativeVariable) variableDao.find(variable.getId());
		return variable;
	}

	@Transactional
	public QuantitativeVariable deleteVariableAggregate(QuantitativeVariable variable, String agg) {
		VariableAggregate aggregate = variable.getAggregate(agg);
		if(aggregate != null) {
			variable.deleteAggregate(agg);
			variableDao.save(variable);
			variableAggregateDao.delete(aggregate.getId());
		}
		variable = (QuantitativeVariable) variableDao.find(variable.getId());
		return variable;
	}
	
}
