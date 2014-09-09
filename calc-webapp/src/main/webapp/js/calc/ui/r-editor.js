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
		theme			: "neat",
		lineNumbers		: true,
		styleActiveLine	: true,
		matchBrackets	: true,
//		extraKeys: {"Ctrl-Q": function(cm){ cm.foldCode(cm.getCursor()); }},
	    foldGutter: true,
	    gutters: [ "CodeMirror-linenumbers" , "CodeMirror-foldgutter" ],
		extraKeys: { "Ctrl-Space": "autocomplete" , "Ctrl-Q": function(cm){ cm.foldCode(cm.getCursor()); } }
	});
	
	this.init();
};

REditor.prototype.init = function(){
	var $this	= this;
	
	
	CodeMirror.hint.r = function(cm) {
		var hints 	= $this.getHints();
	    var cur 	= cm.getCursor();
	    
	    var token 	= cm.getTokenAt(cur), start, end, search;
	    if (token.string.match(/^[.\w@]\w*$/)) {
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
			var list = [];
			for( var i in hints ){
				var hint =  hints[ i ];
				if( StringUtils.contains( hint , search ) ){
					list.push( hint );
				}
			}
			result = list ;
		}
		return {list: result, from: CodeMirror.Pos(cur.line, start), to: CodeMirror.Pos(cur.line, end)};
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

