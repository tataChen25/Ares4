
$(function(){

	$(document).on('click change', globalAction);

});

function globalAction(e) {
	var that = this;
	var eo = $(e.target),
		eoRel = eo.is('[rel]') ? eo : (eo.closest('[rel]')[0] ? eo.closest('[rel]') : null);

	if (e.type == 'change' && !eo.is('[rel],:checkbox,:radio')) {
		return that;
	}

	if (eoRel && /^([^&]+)&?(.*)$/.test(eoRel.attr('rel'))) {
		var method = RegExp.$1;
		var options = deparam(RegExp.$2);
		if (eoRel.is('.disabled') || eoRel.closest('.disabled')[0]) {
			e.preventDefault();
			return that;
		}
		if (eoRel.is('select,:checkbox') && e.type != 'change') {
			return that;
		}
		if (eoRel.is('input:text') && (e.type != 'keyup' || e.keyCode != 13)) {
			return that;
		}
		if (eoRel.is(':not(input:text,textarea,:checkbox)') && e.type != 'click') {
			// prevent triggering when move focus to button/a by pressing tab
			return that;
		}
		if (!eo.is('label,[type="radio"],[type="checkbox"]') && !eoRel.is('label,[type="radio"],[type="checkbox"]')) {
			e.preventDefault();
		}
		if (_.isFunction(that[method])) {
			that[method].apply(that, [e, options]);
		} else if (_.isFunction(window[method])) {
			window[method].apply(that, [e, options]);
		}
	}

	// prevent focus to radio when clicking form element inside .radio label
	var eoRadioParent = eo.parents('label').parents('.radio');
	if (!eo.is(':radio') && eoRadioParent[0]) {
		e.preventDefault();
		eoRadioParent.find(':radio').prop('checked', true);
	}

	return that;
}

function test(e, opt) {
	console.info('test()');
	console.info(arguments);
}

function go(e, opt) {
	location = opt.href;
}



