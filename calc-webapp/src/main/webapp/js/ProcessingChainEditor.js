ProcessingChainEditor = function( $workspace, $container, $jobsContainer ) {
	//html
	this.getHtml = function() {
		$html = '<h4>Processing Chains <i class="icon-plus-sign pull-right icon-border" name="add-chain-icon"></i></h4>';
		
		$html += 
			'<table class="table table-striped table-hover" name="chains">'+
				'<thead>'+
					'<tr>'+
						'<th></th>'+
						'<th>Name</th>'+
					'</tr> '+ 				
				'</thead>'+
				'<tbody></tbody>'+
			'</table>';
		
		$html +=
			'<div id="add-chain" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="false" style="display: block;">' +
				'<form class="form-horizontal" name="edit-chain-form" method="post">' +
					'<div class="modal-header">' +
						'<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>' +
						'<h3 id="myModalLabel">Processing chain</h3>' +
					'</div>'+
					'<div class="modal-body">' +
	  					'<input type="hidden" name="wsId" value="" />'+	  					
	  					'<div class="control-group">' +
	  						'<label class="control-label" for="chain-name">Name</label>' +
	  						'<div class="controls">' +
	  							'<input type="text" name="chain-name" placeholder="chain name" required>' +
  							'</div>' +
						'</div>' +
					'</div>'+
					'<div class="modal-footer">'+
						'<button type="submit" class="btn btn-primary" name="save-chain-btn">Save</button>'+
						'<button class="btn" data-dismiss="modal" aria-hidden="true">Close</button>'+
					'</div>'+
			  '</form>' +
			'</div>';
		
		return ($html);
	}
	,
	this.getChainHtmlRow = function( $chain ) {
		$this = this;
		$tr = $('<tr name="'+$chain.id+'"></tr>');
		
		$td = $("<td class='"+$chain.id+"'></td>");
		$i = $( '<i class="icon-edit" name="'+$chain.id+'"></i>' );
		$i.on( "click" , $.proxy($this.editChainIconClick, $this) );
		$td.append($i);
		$tr.append($td);
		
		$td = $( "<td class='"+$chain.id+"'>"+$chain.name+"</td>" );
		$tr.append($td);
		
		return $tr;
	}
	,
	this.addChains = function( $chains ) {
		$this = this;
		$this.chains = $chains;
		
		$table = $this.container.find("table[name=chains]");
		
		$.each(
			$chains,
			function($i, $chain) {
				$tr = $this.getChainHtmlRow($chain);
				
				$table.append( $tr );
			}
		);
	}
	,
	this.getChain = function($chainId) {
		$chain = null;
		$.each(
			this.chains,
			function($i, $c){
				if($c.id == $chainId) {
					$chain = $c;
					return (false);
				}
			}
		);
		return $chain;
	}
	,
	/**
	 * Init function
	 */
	this.init = function() {
		$this = this;
		
		$this.container.empty();
		
		$html = $this.getHtml();
		$this.container.append( $html );
		
		$addChainIcon = $this.container.find( 'i[name=add-chain-icon]');
		$addChainIcon.on( "click" , $.proxy($this.addChainIconClick, $this) );
		
//		$saveChainBtn = $this.container.find( "button[name=save-chain-btn]" );
//		$saveChainBtn.on("click", $.proxy( $this.saveChainBtnClick, $this ) );
		
		$form = $this.container.find( 'form[name=edit-chain-form]');
		$form.submit( $.proxy( $this.addChainFormSubmit, $this ) );
		
		$this.addChains( $this.chains );
		return ( $this );
	}
	,
//	this.getChainsHeader = function() {
//		var html = "<h4>Processing Chains</h4>";
//		return (html);
//	}
//	,
//	this.getTableHTML = function() {
//		var html =
//			'<table class="table table-striped table-hover" name="chains">'+
//	  			'<thead>'+
//	  				'<tr>'+
//	  					'<th></th>'+
//		  				'<th>Name</th>'+
//	  				'</tr> '+ 				
//	  			'</thead>'+
//	  			'<tbody></tbody>'+
//			'</table>';
//		return html;
//	}
//	,
//	this.initChainsContainer = function( $container, $chains ) {
//		$addChainBtn = $( '<i class="icon-plus-sign pull-right icon-border" ></i>' ); //data-toggle="modal" data-target="#add-chain"
//		$addChainBtn.on( "click" , $.proxy(this.addChainBtnHandler, this) );
//		
//		$header = $( this.getChainsHeader() );
//		$header.append( $addChainBtn );
//		
//		$table = $( this.getTableHTML() );
//		
//		//empty the container and add the table
//		$container.empty();
//		$container.append( $header );
//		$container.append( $table );
//		
//		
//		
//		var addChainHtml = 
//		'<div id="add-chain" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="false" style="display: block;">' +
//		  '<div class="modal-header">' +
//		    '<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>' +
//		    '<h3 id="myModalLabel">Processing chain</h3>' +
//		  '</div>'+
//		  '<div class="modal-body">' +
//  			'<form action="javascript:void(0)" class="form-horizontal" name="edit-chain-form" method="post">' +
//  				'<input type="hidden" name="wsId" value="" />'+
//  				'<input type="hidden" name="chainId" value="" />'+
//			  '<div class="control-group">' +
//			    '<label class="control-label" for="name">Name</label>' +
//			    '<div class="controls">' +
//			      '<input type="text" name="name" placeholder="chain name" required>' +
//			    '</div>' +
//			  '</div>' +
//			'</form>' +
//		  '</div>'+
//		  '<div class="modal-footer">'+
//		  	'<button class="btn btn-primary" name="save-chain-btn">Save</button>'+
//		    '<button class="btn" data-dismiss="modal" aria-hidden="true">Close</button>'+
//		  '</div>'+
//		'</div>';
//		
//		
//		//add the chains (<tr/>) to the <table/> 
//		this.addChains( $container, $chains );
//		
//		$container.append( addChainHtml );
//		$form = $container.find( "button[name=save-chain-btn]" );
//		$form.on("click", $.proxy( this.saveChainBtnHandler, this ) );
//		return ( $container );
//	}
//	,
	/**
	 * Event handlers
	 */
	this.addChainIconClick = function(e) {
		$this = this;
		//clear form
		$form = $(document).find( 'form[name=edit-chain-form]');
		$form.find('input').val("");
		
		//set workspace id
		$form.find('input[name=wsId]').val($this.workspace.id);
		
		$("#add-chain").modal("show");
	}
	,
	this.addChainFormSubmit = function(e) {
		e.preventDefault();
		
		$this = this;
		$form = $(document).find( 'form[name=edit-chain-form]');//.getEditChainForm();

		$name = $form.find( 'input[name=chain-name]' );
		$data = { 'wsId': $this.workspace.id, 'name': $name.val() };
		console.log($data);
		$.ajax({
			type : 		"POST",
			url:        'rest/workspaces/chains.json',
			dataType : 	"json",
			data: $data,
			
			beforeSend: function() {
				$('#add-chain').modal( 'hide' );
			}
		})
		.done(
				function(response) {
			    	$this.workspace = response;
			    	$this.chains = $this.workspace.processingChains;
			    	
			    	$tbody = $this.container.find("table[name=chains] tbody");
			    	$tbody.empty();
			    	
			    	$this.addChains( $this.chains );					
				}
		);
//		$options = 
//		{
//			    success:  $.proxy( function (workspace, statusText, xhr, $form)  {
//			    	$this = this;
//			    	$this.workspace = workspace;
//			    	$this.chains = $this.workspace.processingChains;
//			    	
//			    	$tbody = $this.container.find("table[name=chains] tbody");
//			    	$tbody.empty();
//			    	
//			    	$this.addChains( $this.chains );
//			    } , $this)
//		}; 
//		
//		$form.ajaxSubmit( $options );
	}
	,
//	this.getEditChainForm = function(e) {
//		$form = $this.container.find("form[name=edit-chain-form]");
//		return ($form);
//	}
//	,
	this.editChainIconClick = function(e) {
//		console.log(e.currentTarget);
		$this = this;
		
		$i = $( e.currentTarget );
		$chainId = $i.attr("name");
		$chain = $this.getChain($chainId);
		
		$this.jobsEditor.show($chain);
//		console.log($chainId);
//		console.log(this);
	}
	,
	this.workspace = $workspace
	,
	this.chains = $workspace.processingChains
	,
	this.container = $container//this.initChainsContainer( $container, this.chains )
	,
	this.jobsContainer = $jobsContainer
	,
	this.jobsEditor = new ProcessingChainJobsEditor( this.jobsContainer, this )
	,
	this.__instance = this.init()
	;
};

//ProcessingChainEditor.prototype = new ProcessingChain( this.chain );

ProcessingChainEditor.prototype.showChains = function() {
	
//	this.addChains( $table );
	
	this.container.fadeIn( );
	
};

ProcessingChainEditor.prototype.updateChain = function( $chain ) {
	$this = this;
	
	$chainId = $chain.id;
	$oldRow = this.container.find( 'table[name=chains] tr[name='+$chainId+']' )
	$oldRow.hide();
	$tr = $this.getChainHtmlRow($chain);
	$tr.hide();
	
	$oldRow.replaceWith( $tr );
	$tr.fadeIn();
	$tr = this.container.find( 'table[name=chains] tr[name='+$chainId+']' )
	$tr.fadeIn();

	$.each(
			this.chains,
			$.proxy( function(i, c){
				if(c.id == $chainId) {
					this.chains[i] = $chain;
					return (false);
				}
			} , $this)
	)
	
};
//ProcessingChainEditor.prototype.show = function() {
//	
//}; 