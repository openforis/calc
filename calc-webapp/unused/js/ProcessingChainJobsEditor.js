ProcessingChainJobsEditor = function ($container, $processingChainEditor) {
	/**
	 * component html
	 */
	this.getHtml = function() {
		$html = 
			
			'<div class="row-fluid" name="chain-name-section">'+
				'<h4>Processing Chain</h4>'	+
			'</div>'+
			'<div class="row-fluid" name="chain-name-section">'+
				'<input type="text" name="chain-name" value="" >'+
				'<i class="" name="chain-name-icon"></i>'+
			'</div>'+
			'<hr>'+
			'<div class="row-fluid" name="chain-name-section">'+
				'<h4>Calculation Steps'+ 
					'<i class="icon-plus-sign pull-right icon-border" name="add-step-icon"></i>'+ // data-toggle="modal" data-target="#edit-step" 
				'</h4>'+
			'</div>'+
			'<div class="row-fluid" name="tasks-section">'+
				'<table class="table table-striped table-hover" name="tasks-table">'+
		  			'<thead>'+
		  				'<tr>'+
		  					'<th></th>'+
		  					'<th></th>'+
		  					'<th>Step no</th>'+
			  				'<th>Step Name</th>'+
			  				//'<th>Status</th>'+
		  				'</tr> '+ 				
		  			'</thead>'+
		  			'<tbody></tbody>'+
				'</table>'
			'</div>';
		
		
		//edit task html modal window
		$html += 
		'<div id="edit-step" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="addStepLabel" aria-hidden="false" style="display: block;">' +
			'<form class="form-horizontal" name="edit-step-form" method="post">' +				
				
				'<div class="modal-header">' +
					'<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>' +
					'<h3 id="addStepLabel">Calculation Step</h3>' +
				'</div>'+
		  
				'<div class="modal-body">' +
					'<div class="control-group">' +
						'<label class="control-label" for="name">Name</label>' +
						'<div class="controls">' +
							'<input type="text" name="name" placeholder="calculation step name" required>' +
						'</div>' +
					'</div>' +
					'<div class="control-group">' +
						'<label class="control-label" for="operation">Operation</label>' +
						'<div class="controls">' +
							'<select name="operation" placeholder="your custom code here" required></select>' +
						'</div>' +
					'</div>' +
					'<div class="control-group">' +
						'<label class="control-label" for="code">Code</label>' +
						'<div class="controls">' +
							'<textarea name="code" placeholder="your custom code here" rows="5" required></textarea>' +
						'</div>' +
					'</div>' +
				'</div>'+
		  
				'<div class="modal-footer" style="text-align: center">'+
					'<button type="submit" class="btn btn-primary" name="save-btn">Save</button>'+
					'<button class="btn" data-dismiss="modal" aria-hidden="true">Close</button>'+
				'</div>'+
			'</form>' +
		'</div>';
		
		return ( $html );
	}
	,
	/**
	 * Update html functions
	 */
	this.updateChain = function( chain ) {
		$this = this;
		$this.chain = chain;
		
		//set chain name
		$chainName = this.container.find( "input[name=chain-name]" );
		$chainName.val( this.chain.name );
		//empty the jobs table
		$tbody = this.container.find( "table[name=tasks-table] tbody" );
		$tbody.empty();
		
		$calcSteps = $this.chain.calculationSteps;
		$.each(
				$calcSteps,
				function( $idx, $step ) {
					
//					$step = $task.calculationStep;
					$stepName = $step.name;
					$stepNo = $step.stepNo;
					$stepId = $step.id;
//					console.log($step);
					
					$tr = $( '<tr name="'+$stepId+'"></tr>' );
					//reorder icon
					$td= $( '<td></td>' );
					$i = $( '<i class="icon-reorder" name="'+$stepId+'"></i>' );
					$td.append( $i );
					$tr.append( $td );
					
					//edit icon
					$td= $( '<td></td>' );
					$i = $( '<i class="icon-edit" name="'+$stepId+'" ></i>' ); //data-toggle="modal" data-target="#edit-step"
					$i.click( $.proxy($this.editStepIconClick, $this) );
					$td.append( $i );
					$tr.append( $td );

					//step no
					$td= $( '<td class="step-no">'+$stepNo+'</td>' );
					$tr.append( $td );
					
					//step name
					$td= $( '<td>'+$stepName+'</td>' );
					$tr.append( $td );
					
					$tbody.append( $tr );
					
					//set the rows sortable
					$tbody.sortable({
						update: $.proxy( $this.updateStepNos , $this)
					});
					$tbody.disableSelection();
				}
		);
	}
	,
	this.updateStepNos = function( e , i ) {
		$this = this;

		$tr = i.item;
		$tbody = $tr.parent('tbody');

		// set the temporary loading icon to the step-no column
		$tdStepNos = $tbody.find( 'td[class=step-no]' );
		$tdStepNos.html( '<i class="icon-spinner icon-spin"></i>' );
		
		$sortedIDs = $tbody.sortable( "toArray", {attribute: "name"} );
		
		//send the request to update the step no
		$chainId = $this.chain.id;		
		$url = "rest/workspaces/chains/"+$chainId+"/steps/no.json";
		$.ajax({
			type : "PUT",
			dataType : "json",
			url : $url,
			data : { stepIds: $sortedIDs.join(",") }
		})
		.done(
			$.proxy(
					function(response) {
						$this = this;
						$this.chain = response;
						$this.updateChain( $this.chain );
					}
			, $this)	
		);
	}
	,
	/**
	 * Event handlers
	 */
	this.chainNameFocusOut = function( e ) {
		$this = this;
		$chainName = $this.container.find( 'input[name=chain-name]' );
		
		$data = { "chainId" : $this.chain.id, "name" : $chainName.val() };
		//update chain name
		$.ajax({
			type : "PUT",
			dataType : "json",
			url : 'rest/workspaces/chains.json',
			data : $data,
			beforeSend : function(){
				$icon = $this.container.find( 'i[name=chain-name-icon]' );
				$icon.addClass('icon-spinner');
				$icon.addClass('icon-spin');
				$icon.removeClass('icon-ok');
				$icon.removeClass('alert-success');
				$icon.show();
			}
		}).done(
				$.proxy( function(response) {
					$icon.removeClass('icon-spinner');
					$icon.removeClass('icon-spin');
					
					$icon.addClass('icon-ok');
					$icon.addClass('alert-success');
					$icon.fadeOut(3000);
					//console.log( response );
					
					this.chain = response;
					this.processingChainEditor.updateChain( response );
					
				}, $this)
		);
		console.log($data);
	}
	,
	this.addStepIconClick = function ( e ){
		$this = this;
		// set to null the current step
		$this.currentStep = null;
		
		//reset form to blank values
		$form = $(document).find( "form[name=edit-step-form]" );

		$form.find('input').val("");
		$form.find('textarea').val("");
		$form.find('select[name=operation]').prop('selectedIndex','-1');
		$form.find('select[name=operation]').val( "" );
		//open the modal form
		$("#edit-step").modal("show");
	}
	,
	this.editStepIconClick = function ( e ){
		$this = this;
		
		$i = $( e.currentTarget );
		$stepId = $i.attr('name');
		$.each(
				$this.chain.calculationSteps,
				function($i, $s){
					if( $s.id == $stepId){
						$this.currentStep = $s;
						return (false);
					}
				}
		);
		
		//set form values with selected step
		$form = $(document).find( 'form[name=edit-step-form]' );
		$name = $form.find( 'input[name=name]' );
		$name.val( $this.currentStep.name );
		
		$operation = $form.find( 'select[name=operation]' );
		$op = $this.currentStep.operationName+' '+$this.currentStep.moduleName;
		$operation.val( $op );
		
		//TODO for now it takes the first parameter
		$parameters = $this.currentStep.parameters;
		$first = null;
		for (var i in $parameters) {
		    if ($parameters.hasOwnProperty(i) && typeof(i) !== 'function') {
		        $first = $parameters[i];
		        break;
		    }
		}
//		console.log($first);
		$code = $form.find( 'textarea[name=code]' );
		$code.val( $first );
		
//		$form.find( 'input[name=chainId]' ).val( $this.chain.id );
		$("#edit-step").modal("show");
		
//		$form.find('input').val();
//		$form.find('select').prop('selectedIndex','-1');
	}
	,
	this.editStepFormSubmit = function( e ){
		e.preventDefault();
		$this = this;
		
		$form = $( e.currentTarget );// .container.find( 'form[name=edit-step-form]' );
		
		$stepsTable = $this.container.find( 'table[name=tasks-table]' );
		
		$op = $form.find( 'select[name=operation]' ).val();
		$opVals = $op.split(' ');		
		$operationName = $opVals[0];
		$moduleName = $opVals[1];
		$moduleVersion = '1.0';//TODO
		
		$stepName = $form.find('input[name=name]').val();
		
		//TODO check how it has to be 
		$paramValue = $form.find( 'textarea[name=code]' ).val();
		if($operationName == "exec-sql") {
			$paramName = "sql";  
			$parameters = {"sql": $paramValue};
		} else {
			$paramName = "r";
			$parameters = {"r": $paramValue};
		}
		
		if($this.currentStep == null ){
			//adding new calculation step
			$type = "POST";
			$url = "rest/workspaces/chains/"+$this.chain.id+"/steps/step.json";
			$stepNo = $stepsTable.find( 'tr' ).length;
		} else {
			// editing a calculation step
			$type = "PUT";
			$url = "rest/workspaces/chains/"+$this.chain.id+"/steps/step/"+$this.currentStep.id+".json";
			$stepNo = $this.currentStep.stepNo;
		}
		
		$data = {'stepNo':$stepNo , 'moduleName':$moduleName , 'moduleVersion':$moduleVersion , 'operationName':$operationName , 'name':$stepName, 'parameters': JSON.stringify($parameters) };
		console.log($data);
		$.ajax({
			url: $url,
			type: $type,
			dataType: "json",
			data: $data,
			beforeSend: function() {
				$("#edit-step").modal("hide");
//				console.log('before send');
				$tbody = $stepsTable.find( 'tbody' );
				$tbody.empty();
				
				$tr = $( '<tr><td colspan="4"><i class="icon-spinner icon-spin"></i></td></tr>')
				$tbody.append( $tr );
			} 
		})
		.done(
				function(response) {
					$this.chain = response;
					$this.updateChain($this.chain);
				}
		);
	}
	,
	/**
	 * Init functions
	 */
	this.initContainer = function( $container ) {
		$this = this;
//		$container.hide();
		$container.empty();
		
		$html = $( this.getHtml() );
		
		$container.append( $html );
		
		$chainNameInput = $container.find( 'input[name=chain-name]' );
		$chainNameInput.focusout( $.proxy(this.chainNameFocusOut, this) ); 

		
		// add the modules to the edit operation from select
		$operationSelect = $container.find( 'select[name=operation]' );
		$.each(
			$this.modules,
			function($idx, $module) {
				
				$moduleName = $module.name;
				$operations = $module.operations;
				
				$.each(
					$operations,
					function($oIdx, $operation){
						$operationName = $operation.name;
						
						$optionValue = $operationName+' '+$moduleName;
						$option = $( '<option value="'+$optionValue+'">'+$operationName+' ('+$moduleName+')</option>' );
						$operationSelect.append( $option );
					}
				);
			}
		);
//		$operationSelect.prop( 'selectedIndex','-1' );
		
		$addStepIcon = $container.find( 'i[name=add-step-icon]');
		$addStepIcon.click( $.proxy($this.addStepIconClick , $this) );
		
		$editStepForm = $container.find( 'form[name=edit-step-form]');
		$editStepForm.on( "submit", $.proxy($this.editStepFormSubmit , $this) );
		
		return ($container);
	}
	,
	this.init = function() {
		$this = this;
		
		//load available modules
		$.ajax({
			url: "rest/modules.json",
			type: "GET",
			dataType: "json"
		})
		.done(
			$.proxy( function(response) {
				this.modules = response;
			}, $this)	
		);
		
		
		return this;
	}
	,
	/**
	 * Instance variables
	 */
	this.container = $container// = this.init( $container )
	,
	this.chain = null
	,
	this.job = null
	,
	this.modules = null
	,
	//active step for editing
	this.currentStep = null
	,
	this.processingChainEditor = $processingChainEditor
	,
	this.__instance = this.init()
	;
};

/*
 * Public functions
 */

/**
 * Hides the components
 */
ProcessingChainJobsEditor.prototype.hide = function(  ) {
	this.container.hide();
}
/**
 * Shows the jobs for the processing chain passed as parameter
 */
ProcessingChainJobsEditor.prototype.show = function( $chain ) {
	$this = this;
	
	$this.chain = $chain;
	
	// init html container
	$this.container = $this.initContainer( $this.container );
	
	//update ui 
	$this.updateChain( $this.chain );
	
	// show the html container
	$this.container.fadeIn();
	
};
