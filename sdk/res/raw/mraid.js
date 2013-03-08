(function() {

	// Set up some variables
	var mraid = window.mraid = {};
	var mraid.util = {};
	var listeners = [];
	var listeners['ready']=[];
	var listeners['error']=[];
	var listeners['stateChange']=[];
	var listeners['viewableChange']=[];
	var state='loading'; //Can be loading, default, expanded, or hidden
	var placement_type='inline'; 
	var is_viewable=false;  
	var expand_properties={width:300, height:250, useCustomClose:false, isModal:true};

	// ----- MRAID AD API FUNCTIONS -----
	
	// getVersion() returns string '1.0'
	mraid.getVersion=function(){ return '1.0'};

	/** Adds a listener to a specific event. For example, a function onReady might be defined, and then mraid.addEventListener('ready', onReady)
	  * is called. When the ready event is fired, onReady will be called.
	  * Events 'error', 'viewableChange', 'stateChange', have parameters.
         */
	mraid.addEventListener=function(event_name, method){
		if(listeners[event_name].indexOf(method) > -1) return; // Listener is already registered
		listeners[event_name].push(method);
	};

	// Removes a listener from the registry
	mraid.removeEventListener=function(event_name, method){
		var method_index = listeners[event_name].indexOf(method);
		if(method_index > -1) //Don't try to remove unregistered listeners
			listeners[event_name].splice(method_index,1);
	};

	//returns 'loading', 'default', 'expanded', or 'hidden'
	mraid.getState=function(){
		return state;
	};

	//returns 'inline' or 'interstitial'
	mraid.getPlacementType(){
		return placement_type;
	};

	//returns true or false
	mraid.isViewable(){
		return is_viewable;
	};

	//returns a json object... {width:300, height:250, useCustomClose:false, isModal:false};
	mraid.getExpandProperties(){
		return expand_properties;
	};


	// ----- MRAID JS TO NATIVE FUNCTIONS -----

	//Closes an expanded ad or hides an ad in default state
	mraid.close=function(){
		switch(mraid.getState()){
		case 'loading':
			mraid.util.errorEvent("mraid.close() called while state is 'loading'.", "mraid.close()");
			break;
		case 'default':
			window.location="mraid://close/";
			mraid.util.stateChangeEvent('hidden');
			break;
		case 'expanded':
			window.location="mraid://close/";
			mraid.util.stateChangeEvent('default');
			break;
		case 'hidden':
			mraid.util.errorEvent("mraid.close() called while ad was already hidden". "mraid.close()");
			break;
		}
	};

	// Expands a default state ad, or unhides a hidden ad. Optionally takes a URL to load in the expanded view
	mraid.expand=function(url){
		switch(mraid.getState()){
		case 'loading':
			mraid.util.errorEvent("mraid.expand() called while state is 'loading'.", "mraid.expand()");
			break;
		case 'default':
			window.location="mraid://expand/"+(url!=null ? "?url="+url:"");
			mraid.util.stateChangeEvent('expanded');
			break;
		case 'expanded':
			mraid.util.errorEvent("mraid.expand() called while state is 'expanded'.", "mraid.expand()");
			break;
		case 'hidden':
			window.location="mraid://expand/"+(url!=null ? "?url="+url:"");
			mraid.util.stateChangeEvent('default');
			break;
		}
	};

	// Takes an object... {width:300, height:250, useCustomClose:false, isModal:false};
	mraid.setExpandProperties(properties){
		properties.isModal=true; // Read only property.
		expand_properties=properties;
		window.location="mraid://setExpandProperties/?properties="+JSON.stringify(properties);
	};

	// Takes a boolean
	mraid.useCustomClose=function(well_is_it){
		ep = mraid.getExpandProperties();
		ep.useCustomClose = well_is_it;
		mraid.setExpandProperties(ep);
	};

	// Loads a given URL
	mraid.open=function(url){
		window.location=url;//Is there any reason to make a native call here?
	};

	// ----- MRAID UTILITY FUNCTIONS -----
	// These functions are called by the native SDK to drive events and update information

	mraid.util.setPlacementType=function(type){
		placement_type=type;
	};

	mraid.util.setIsViewable=function(is_it_viewable){
		is_viewable=is_it_viewable;
	};

	mraid.util.readyEvent=function(){
		for(var i=0;i<listeners['ready'].length;i++){
			listeners['ready'][i]();
		}
	};

	mraid.util.errorEvent=function(message, what_doing){
		for(var i=0;i<listeners['error'].length;i++){
			listeners['error'][i](message, what_doing);
		}
	};

	mraid.util.stateChangeEvent=function(new_state){
		state=new_state;
		for(var i=0;i<listeners['stateChange'].length;i++){
			listeners['stateChange'][i](new_state);
		}
	};

	mraid.util.viewableChangeEvent=function(is_viewable_now){
		is_viewable = is_viewable_now;
		for(var i=0;i<listeners['viewableChange'].length;i++){
			listeners['viewableChange'][i](is_viewable_now);
		}
	};
		
}());