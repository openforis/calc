package org.openforis.calc.engine;

import java.util.ArrayList;

public class ProcessingChain {
	public ArrayList<org.openforis.calc.engine.ProcessingChain.Step> steps = new ArrayList<Step>();
	public static class Step {
		private ProcessingChain processingChain;
		private Operation operation;
		private org.openforis.calc.engine.Operation.Parameters parameters;

		public Operation getOperation() {
			return this.operation;
		}

		public org.openforis.calc.engine.Operation.Parameters getParameters() {
			return this.parameters;
		}
	}
}