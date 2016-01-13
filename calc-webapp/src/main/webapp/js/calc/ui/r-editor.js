/**
 *	REditor javascript class
 *
 *	@author Mino Togna
 */
REditor = function( textAreaId , readOnly  ){
	
	this.textArea			= $( "#"+textAreaId );
	
	var toggleFullScreen 	= function(cm) {
		var fullScreen = !cm.getOption("fullScreen");
		if( fullScreen ){
			Calc.footer.hide();
		} else {
			Calc.footer.show();
		}
		cm.setOption( "fullScreen", fullScreen ); 
	};
	
	this.editor 		= CodeMirror.fromTextArea( document.getElementById( textAreaId ) , {
		mode 			: "r",
		theme			: "neat",
		lineNumbers		: true,
		styleActiveLine	: true,
		matchBrackets	: true,
	    foldGutter		: true,
	    gutters			: [ "CodeMirror-linenumbers" , "CodeMirror-foldgutter" ],
		readOnly 		: readOnly,	
		extraKeys		: { 
							"Ctrl-Space"	: "autocomplete" , 
							"Ctrl-Q"		: function(cm) { cm.foldCode(cm.getCursor()); } ,
//							"F11"			: function(cm) { toggleFullScreen(cm); },
//							"F10"			: function(cm) { toggleFullScreen(cm); },
							"Esc"			: function(cm) { 
												if ( cm.getOption("fullScreen") ) {
													Calc.footer.show();
													cm.setOption("fullScreen", false);
													} 
												}
						  }			  
	});
	if( readOnly === true ){
		var elem =  $( this.editor.getWrapperElement() );
		elem.addClass( 'readOnly' );
	}
	/**
	 * entity instance variable used to populate the autocomplete dropdown in case it's set
	 */
	this.entity					= null;
	// custom variables to populate externally
	this.customVariables 		= [];
	
	this.init();
};

REditor.prototype.init = function(){
	var $this	= this;
	
	CodeMirror.hint.r = function( cm ){
		var hints 	= $this.getHints();
	    var cur 	= cm.getCursor();
	    
	    var token 	= cm.getTokenAt(cur), start, end, search;
	    if (token.string.match(/^\w+(\$\w*)?$/)) { 
//    	if (token.string.match(/^\w*(\$\w)?$/)) { 
	        search = token.string;
	        start = token.start;
	        end = token.end;
	    } else {
	        start = end = cur.ch;
	        search = "";
	    }
	    
	    var result = [];
		if( StringUtils.isBlank(search) ){
			result = hints ;
		} else {
			for( var i in hints ){
				var hint =  hints[ i ];
				if( StringUtils.contains( hint , search ) ){
					result.push( hint );
				}
			}
		}
		return { list: result, from: CodeMirror.Pos(cur.line, start), to: CodeMirror.Pos(cur.line, end) };
	};
	
};


/**
 * Returns the available hints 
 * @returns {Array}
 */
REditor.prototype.getHints = function(){
	var hints = new Array();
	if ( this.entity != null ) {
		hints.push( this.entity.name );
		
		var variables = this.entity.hierarchyVariables();
		for( var i in variables ){
			var variable = variables[i];
			var variableName = variable.name;
			var item = this.entity.name + "$" + variableName;
			hints.push( item );
		}
		
		for( var i in this.customVariables ){
			var customVariable = this.customVariables[ i ];
			var item = this.entity.name + "$" + customVariable;
			hints.push( item );
		}
		
		hints.sort();
	}
	
	hints =	hints.concat( REditor.rFunctions );
	
	return hints;
};

/**
 * Returns the text entered into the editor
 */
REditor.prototype.getValue = function(){
	return this.editor.getValue();
};

/**
 * Set the editor content.
 * @param value
 */
REditor.prototype.setValue = function( value ){
	return this.editor.setValue( value );
};

REditor.prototype.refresh = function(){
	this.editor.refresh();
};

// r functions used for content assist
REditor.rFunctions = [];

/**
 * static function to call to initialize the r editor
 * 
 *  @deprecated
 */
REditor.init = function(){
	// load r functions
//	$.ajax({
//		url		: "rest/r/functions.json",
//		dataType:"json"
//	})
//	.done(function(response) {
//		REditor.rFunctions = response;
//	})
//	.error( function() {
//		Calc.error.apply( this , arguments );
//	});
};

