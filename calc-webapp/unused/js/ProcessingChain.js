function ProcessingChain( $chain, $container ) {
	this.chain = $chain,
	this.getJobsUrl = "rest/workspaces/chains/"+this.chain.id+"/job.json",
	this.container = $container,
	this.form = null,
	this.job = null,
	
	// run the processing chain
	this.runChain = function(e) {
		e.preventDefault();
		$this = this;
		
		//disable the form
		$this.disableForm();
		
		// get the task ids to run
		$params = $this.form.serialize().split("&");
		$taskIds = [];
		$.each(
			$params,
			function($i, $p){
				$param = $p.split("=");
				if($param[1]=="yes"){
					$taskIds[$i] = $param[0];
				}
					 
			}
		);
		$taskIds = $taskIds.join(",");
		//console.log($p);
//		console.log($taskIds);

		//call to the server
		$url = "rest/workspaces/chains/"+$chain.id+"/run.json";
		$.getJSON(
			$url,
			{"taskIds": $taskIds }
		)
		.done(
				function($resp) {
					$this.loadTasks( $.proxy( $this.updateTasks, $this) );
				}
		)
		.fail(
				function( jqxhr, textStatus, error) {
					var err = textStatus + ', ' + error ;
//					console.log( "Request Failed: " + err);
					$this.container.find(".error-msg").html(err).fadeIn();
				}
		);
		
		return (false);
	},

	/**
	 * get the tasks from the server and call the callback function
	 */
	this.loadTasks = function (callback) {
		var $this = this;
		$.getJSON(
			$this.getJobsUrl		
		).done(
			function(response) {
				
				$this.job = response;
				
				callback();
				//
				//showProcessingChainJobForm();
			}
		); 
	}
	,
	//add tasks to the ui
	this.addTasks = function() {	
//		console.log(this);
		$tasks = this.job.tasks;
		$tbody = this.form.find('tbody');
		
		$.each(
			$tasks,
			function($i, $task) {
				
				$id = $task.id;
				$step = $task.calculationStep;
				
				$tr = $('<tr class="'+$id+'"></tr>');
				
				$tdStep = $('<td class="checkbox"><label class="checkbox"><input type="checkbox" value="yes" name="'+$id+'" checked="checked">'+$step.name+'</label></td>');
				$tr.append($tdStep);
				
				$tdStatus = $('<td class="status">'+$task.status+'</td>');
				$tr.append($tdStatus);
				
				$tbody.append($tr);
			}
		);
	},
	
	/**
	 * updates the tasks 
	 */
	this.updateTasks = function() {
		$status = this.job.status;
		$tasks = this.job.tasks;
		$form = this.form;
		
		$.each(
			$tasks,
			function($i, $task) {				
				$id = $task.id;
				$tr = $form.find('tr[class='+$id+']');
				
				$tdStatus = $tr.find('td[class=status]');
				$tdStatus.html($task.status);
				
			}
		);
		
		if( ! ($status == "FINISHED") ) {
//			console.log('a');
			window.setTimeout( $this.loadTasks( $.proxy( $this.updateTasks, $this) ) , 1000 );
			//window.setTimeout( this.loadTasks(this.updateTasks), 1000 );
		} else {
			$this.enableForm();
		}
	},
	
	this.enableForm = function() {
		$this.form.find("button[type=submit]").fadeIn(1000);
//		$this.form.find("button[type=submit]").removeAttr("disabled").removeClass('disabled');
	},
	
	this.disableForm = function() {
		$this.form.find("button[type=submit]").fadeOut(100);
//		$this.form.find("button[type=submit]").attr("disabled", "disabled").addClass('disabled');
	},
	//html functions
	this.getFormHTML = function() {
		var html = 
			'<form '+ 
				'name="exec-tasks-form "'+ 
				'class=" "'+
				'action="rest/execTasks.json "'+
				'method="get">'+
					+
			'</form>';
		return html;
	},
	
	this.getTableHTML = function() {
		var html =
			'<table class="table table-striped table-hover">'+
	  			'<thead>'+
	  				'<tr>'+	
		  				'<th>Step Name</th>'+
		  				'<th>Status</th>'+
	  				'</tr> '+ 				
	  			'</thead>'+
	  			'<tbody></tbody>'+
			'</table>';
		return html;
	}
	
	; 
};

ProcessingChain.prototype.show = function() {
	
	//empty the container
	this.container.empty();
	// add the form to the container
	this.form = $( this.getFormHTML() );
	table = $( this.getTableHTML() );	
	this.form.append(table);
	
	//submit button
	this.form.append('<button type="submit" value="Run" class="btn-inverse btn-small">Run</button>');
	this.form.on( "submit", $.proxy(this.runChain, this) );
	
	this.container.append(this.form);
	//error section
	this.container.append('<div class="error error-msg" style="display: none"></div>');
	
	this.loadTasks( $.proxy(this.addTasks, this) );

	// show the container
	this.container.fadeIn();
};

ProcessingChain.prototype.hide = function() {
	this.container.hide();
};


//ProcessingChain.prototype.runTasks = function() {
//	
//};
