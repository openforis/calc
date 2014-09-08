/**
 *	REditor javascript class
 *
 *	@author Mino Togna
 */
REditor = function( textAreaId ){
	
	this.textArea		= $( "#"+textAreaId );
	var textArea 		= document.getElementById( textAreaId );
	this.editor 		= CodeMirror.fromTextArea( textArea, {
		mode 			: "r",
		lineNumbers		: true,
		styleActiveLine	: true,
		matchBrackets	: true,
//		extraKeys: {"Ctrl-Q": function(cm){ cm.foldCode(cm.getCursor()); }},
	    foldGutter: true,
	    gutters: [ "CodeMirror-linenumbers" , "CodeMirror-foldgutter" ],
		theme			: "elegant",
		extraKeys: { "Ctrl-Space": "autocomplete" }
	});
	
	this.init();
};

REditor.prototype.init = function(){
	var $this	= this;
	
	
	CodeMirror.hint.r = function(cm) {
		var inner = { from: cm.getCursor(), to: cm.getCursor(), list: [] };
		var hints = $this.getHints();
		
		var doc 	= cm.getDoc();
	    var POS 	= doc.getCursor();
	    var mode 	= CodeMirror.innerMode(cm.getMode(), cm.getTokenAt(POS).state).mode.name;
	    var line 	= doc.getLine( POS.line )
		line		= StringUtils.getLastWord( line , [" ", "\n"], POS.ch ).trim();
		
		if( Utils.isBlankString(line) ){
			inner.list = inner.list.concat( hints );
		} else {
			var list = [];
			for( var i in hints ){
				var hint =  hints[ i ];
				if( StringUtils.contains( hint , line ) ){
					list.push( hint );
				}
			}
			inner.list = inner.list.concat( list );
		}
		return inner;
	};
	
};
/**
 * Returns the available hints 
 * @returns {Array}
 */
REditor.prototype.getHints = function(){
	var hints =	 REditor.rFunctions;
	
	return hints;
};
/**
 * Returns the text entered into the editor
 */
REditor.prototype.getText = function(){
	return this.editor.getValue();
};




// r functions used for content assist
REditor.rFunctions = [];
/**
 * static function to call to initialize the r editor 
 */
REditor.init = function(){
	// load r functions
	$.ajax({
		url		: "rest/r/functions.json",
		dataType:"json"
	})
	.done(function(response) {
		REditor.rFunctions = response;
	})
	.error( function() {
		Calc.error.apply( this , arguments );
	});
};

