(function($) {
    /**
     * Use this plugin if you want to find out the current pixel position of the
     * caret on a text box (either text input or textarea).
     *
     * @returns an object with the properties `left`, `top`, and childPos. Left
     *     and top represent the x and y coordinates, respectively; childPos
     *     is an object with properties left and top, representing the x and y
     *     coordinates *inside* the textbox.
     */
    $.fn.caretpixelpos = function () {
        if (typeof this.positioner == 'undefined') {
            this.positioner = new maxkir.CursorPosition(this[0], 7);
        }

        var child_pos = this.positioner.getPixelCoordinates();
        var t_pos = this.offset();

        return {
            left: t_pos.left + child_pos[0],
            top: t_pos.top + child_pos[1],
            childPos: {
                left: child_pos[0],
                top: child_pos[1]
            }
        };
    };
    
    $.fn.getCaretCoordinates = function () {
        if (typeof this.positioner == 'undefined') {
            this.positioner = new maxkir.CursorPosition(this[0], 7);
        }

        var caretPosition = this.positioner.getCursorCoordinates();
        return {x: caretPosition[0], y: caretPosition[1]};
    };
    
}.call(this, jQuery));