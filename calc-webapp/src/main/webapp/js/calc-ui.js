function UI() {};

/**
 * Shows a modal with static backdrop to avoid user interaction
 */
UI.lock = function() {
	$uiLockModal = $("#ui-lock-modal");
	$uiLockModal.modal({keyboard:false, backdrop:"static"});
	$('body').addClass('locked');
};

/**
 * Remove the lock modal
 */
UI.unlock = function() {
	$uiLockModal.modal('hide');
	$uiLockModal.modal('removeBackdrop');
	$('body').removeClass('locked');
};

/**
 * Reset progress bar to its original state
 */
UI.resetProgressBar = function($progressBar) {
	$progressBar.removeClass();
	$progressBar.addClass("progress-bar");
	$progressBar.parent().removeClass();
	$progressBar.parent().addClass("progress");
};
