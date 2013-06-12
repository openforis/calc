package org.openforis.calc.engine;

import org.openforis.calc.workspace.Workspace;
import java.util.ArrayList;
import org.openforis.calc.common.UserObject;

public final class ProcessingChain extends UserObject {
	private Workspace workspace;
	private ArrayList<org.openforis.calc.engine.ProcessingChain.Step> steps = new ArrayList<Step>();
	private Parameters chainParameters;

	public ProcessingChainJob createJob(org.openforis.calc.engine.Task.Context context) {
		throw new UnsupportedOperationException();
	}
	public static final class Step extends UserObject {
		private String moduleName;
		private String moduleVersion;
		private String operationName;
		private int index;
		private ProcessingChain chain;
		private Parameters operationParameters;

		public ProcessingChain getChain() {
			return this.chain;
		}

		public Parameters getOperationParameters() {
			return this.operationParameters;
		}

		public void setModuleName(String moduleName) {
			this.moduleName = moduleName;
		}

		public String getModuleName() {
			return this.moduleName;
		}

		public void setModuleVersion(String moduleVersion) {
			this.moduleVersion = moduleVersion;
		}

		public String getModuleVersion() {
			return this.moduleVersion;
		}

		public void setOperationName(String operationName) {
			this.operationName = operationName;
		}

		public String getOperationName() {
			return this.operationName;
		}

		public void setIndex(int index) {
			this.index = index;
		}

		public int getIndex() {
			return this.index;
		}
	}
}