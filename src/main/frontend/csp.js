// Override the Function constructor with a version that uses inlined code if available
const originalFunction = window.Function;
window.Function = function(...args) {
  const key = args.join(",");
  
  if (functions.has(key)) {
    return functions.get(key);
  }

  if (key.startsWith("return window.Vaadin.Flow.loadOnDemand(")) {
    const chunk = key.split("return window.Vaadin.Flow.loadOnDemand('").pop().split("');").pop();
    return function (chunk) {return window.Vaadin.Flow.loadOnDemand(chunk);};
  }

  // In "training" mode, log expression to add to the map
  const code = args[args.length - 1];
  // Ignore the stats gatherer which isn't used in production mode
  if (code.indexOf("var StatisticsGatherer") == -1) {
    const snippet = `functions.set("${key}",\n  function(${args.slice(0, -1).join(',')}) {${code}});`
    console.warn(snippet);
  }

  // Fall back to the original constructor (seem to be fine not use it as a constructor)
  return originalFunction.apply(null, args)
}

// Override eval(..) with a version that uses inlined code if available
const originalEval = window.eval;
window.eval = function(script) {
  const originalArg = script;
  // Removes parenthesis used by Vaadin Charts
  if (script.length > 1 && script.substring(0, 1) === "(" && script.substring(script.length - 1) === ")") {
    script = script.substring(1, script.length - 1);
  }
  if (evalCalls.has(script)) {
    return evalCalls.get(script);
  } else {
    let snippet = "";
    if (script.startsWith("function")) {
      snippet = `evalCalls.set("${script}",\n  ${script});`
    } else {
      snippet = `evalCalls.set("${script}",\n  function() {return ${script}});`
    }
    console.warn(snippet);

    // Fall back to original eval function
    originalEval.apply(null, originalArg);
  }
}

// Inlined versions of functions corresponding to eval calls
const evalCalls = new Map();
// Examples from Tooltip formatter in DashboardView (with and without function wrapper)
evalCalls.set("'The value for <b>' + this.x + '</b> is <b>' + this.y + '</b>'",
    function() {return 'The value for <b>' + this.x + '</b> is <b>' + this.y + '</b>'});
evalCalls.set("function() {return 'The value is <b>' + this.y + '</b>'}",
    function() {return 'The value is <b>' + this.y + '</b>'});

// Inlined versions of all the functions that would otherwise have to be evaled
const functions = new Map();
functions.set("$0,$0.addEventListener('items-changed', function(){ this.$server.updateSelectedTab(true); });",
  function($0) {$0.addEventListener('items-changed', function(){ this.$server.updateSelectedTab(true); });});
functions.set("$0,return (function() { this.validate = function () {return this.checkValidity();};}).apply($0)",
  function($0) {return (function() { this.validate = function () {return this.checkValidity();};}).apply($0)});
functions.set("$0,$1,$2,return (function() { this.$server['}p']($0, true, $1)}).apply($2)",
  function($0,$1,$2) {return (function() { this.$server['}p']($0, true, $1)}).apply($2)});
functions.set("event,element,return (event.shiftKey)",
  function(event,element) {return (event.shiftKey)});
functions.set("event,element,return (event.metaKey)",
  function(event,element) {return (event.metaKey)});
functions.set("event,element,return (event.detail)",
  function(event,element) {return (event.detail)});
functions.set("event,element,return (event.ctrlKey)",
  function(event,element) {return (event.ctrlKey)});
functions.set("event,element,return (event.clientX)",
  function(event,element) {return (event.clientX)});
functions.set("event,element,return (event.clientY)",
  function(event,element) {return (event.clientY)});
functions.set("event,element,return (event.altKey)",
  function(event,element) {return (event.altKey)});
functions.set("event,element,return (event.button)",
  function(event,element) {return (event.button)});
functions.set("event,element,return (event.screenY)",
  function(event,element) {return (event.screenY)});
functions.set("event,element,return (event.screenX)",
  function(event,element) {return (event.screenX)});
functions.set("$0,this.scrollPositionHandlerAfterServerNavigation($0);",
  function($0) {this.scrollPositionHandlerAfterServerNavigation($0);});
functions.set("$0,document.title = $0",
  function($0) {document.title = $0});
functions.set("$0,    document.title = $0;\n    if(window?.Vaadin?.documentTitleSignal) {\n        window.Vaadin.documentTitleSignal.value = $0;\n    }\n",
  function($0) {    document.title = $0;
    if(window?.Vaadin?.documentTitleSignal) {
      window.Vaadin.documentTitleSignal.value = $0;
    }
  });
functions.set("$0,return (async function() { this._shouldSetInvalid = function (invalid) { return false };}).apply($0)",
    function($0) {return (async function() { this._shouldSetInvalid = function (invalid) { return false };}).apply($0)});
functions.set("$0,$1,return (async function() { this.serverConnected($0)}).apply($1)",
    function($0,$1) {return (async function() { this.serverConnected($0)}).apply($1)});
functions.set("$0,$1,$2,return (async function() { this.$server['}p']($0, true, $1)}).apply($2)",
    function($0,$1,$2) {return (async function() { this.$server['}p']($0, true, $1)}).apply($2)});
functions.set("$0,return $0.requestContentUpdate()",
    function($0) {return $0.requestContentUpdate()});
functions.set("$0,return (async function() { Vaadin.FlowComponentHost.patchVirtualContainer(this)}).apply($0)",
    function($0) {return (async function() { Vaadin.FlowComponentHost.patchVirtualContainer(this)}).apply($0)});
functions.set("$0,$1,return (async function() { this.renderer = (root) => {  if (this.text) {    root.textContent = this.text;  } else {    Vaadin.FlowComponentHost.setChildNodes($0, this.virtualChildNodeIds, root)  }}}).apply($1)",
    function($0,$1) {return (async function() { this.renderer = (root) => {  if (this.text) {    root.textContent = this.text;  } else {    Vaadin.FlowComponentHost.setChildNodes($0, this.virtualChildNodeIds, root)  }}}).apply($1)});
functions.set("$0,return $0.requestContentUpdate()",
    function($0) {return $0.requestContentUpdate()});
functions.set("$0,if($0.$connector) $0.$connector.reset();",
    function($0) {if($0.$connector) $0.$connector.reset();});
functions.set("$0,$1,$2,$3,$4,$5,return (async function() { window.Vaadin.setLitRenderer(this, $0, $1, $2, $3, $4)}).apply($5)",
    function($0,$1,$2,$3,$4,$5) {return (async function() { window.Vaadin.setLitRenderer(this, $0, $1, $2, $3, $4)}).apply($5)});
functions.set("$0,$1,return (async function() { if (this.$connector) { this.$connector.setSelectionMode($0) }}).apply($1)",
    function($0,$1) {return (async function() { if (this.$connector) { this.$connector.setSelectionMode($0) }}).apply($1)});
functions.set("$0,$1,return $0.$connector.updateSize($1)",
    function($0,$1) {return $0.$connector.updateSize($1)});
functions.set("$0,$1,$2,return $0.$connector.set($1,$2)",
    function($0,$1,$2) {return $0.$connector.set($1,$2)});
functions.set("$0,$1,return $0.$connector.confirm($1)",
    function($0,$1) {return $0.$connector.confirm($1)});
functions.set("$0,$1,$2,return (async function() { this.$connector.setFooterRenderer($0, { content: $1 })}).apply($2)",
    function($0,$1,$2) {return (async function() { this.$connector.setFooterRenderer($0, { content: $1 })}).apply($2)});
functions.set("$0,$1,$2,$3,$4,return (async function() { this.$connector.setHeaderRenderer($0, { content: $1, showSorter: $2, sorterPath: $3 })}).apply($4)",
    function($0,$1,$2,$3,$4) {return (async function() { this.$connector.setHeaderRenderer($0, { content: $1, showSorter: $2, sorterPath: $3 })}).apply($4)});
functions.set("$0,window.Vaadin.Flow.datepickerConnector.initLazy($0)",
    function($0) {window.Vaadin.Flow.datepickerConnector.initLazy($0)});
functions.set("$0,return (async function() { window.Vaadin.Flow.comboBoxConnector.initLazy(this)}).apply($0)",
    function($0) {return (async function() { window.Vaadin.Flow.comboBoxConnector.initLazy(this)}).apply($0)});
functions.set("$0,window.Vaadin.Flow.gridProConnector.patchEditModeRenderer($0)",
    function($0) {window.Vaadin.Flow.gridProConnector.patchEditModeRenderer($0)});
functions.set("$0,$1,return (async function() { window.Vaadin.Flow.gridProConnector.initCellEditableProvider($0)}).apply($1)",
    function($0,$1) {return (async function() { window.Vaadin.Flow.gridProConnector.initCellEditableProvider($0)}).apply($1)});
functions.set("$0,window.Vaadin.Flow.gridConnector.initLazy($0)",
    function($0) {window.Vaadin.Flow.gridConnector.initLazy($0)});
functions.set("$0,return (async function() { if (this.$connector) { this.$connector.reset() }}).apply($0)",
    function($0) {return (async function() { if (this.$connector) { this.$connector.reset() }}).apply($0)});
functions.set("$0,$1,return (async function() { if (this.$connector) { this.$connector.setSorterDirections($0) }}).apply($1)",
    function($0,$1) {return (async function() { if (this.$connector) { this.$connector.setSorterDirections($0) }}).apply($1)});
functions.set("$0,$1,$2,return $0.$connector.updateI18n($1,$2)",
    function($0,$1,$2) {return $0.$connector.updateI18n($1,$2)});
functions.set("$0,$1,$2,$3,return $0.$connector.set($1,$2,$3)",
    function($0,$1,$2,$3) {return $0.$connector.set($1,$2,$3)});
functions.set("$0,$1,$2,return $0.$connector.confirm($1,$2)",
    function($0,$1,$2) {return $0.$connector.confirm($1,$2)});
functions.set("$0,window.Vaadin.Flow.selectConnector.initLazy($0)",
    function($0) {window.Vaadin.Flow.selectConnector.initLazy($0)});
functions.set("$0,$1,$2,return $0.updateConfiguration($1,$2)",
    function($0,$1,$2) {return $0.updateConfiguration($1,$2)});
functions.set("event,element,return (element.querySelector(':scope > [slot=\"primary\"]').style.flexBasis)",
    function(event,element) {return (element.querySelector(':scope > [slot="primary"]').style.flexBasis)});
functions.set("event,element,return (element.querySelector(':scope > [slot=\"secondary\"]').style.flexBasis)",
    function(event,element) {return (element.querySelector(':scope > [slot="secondary"]').style.flexBasis)});
functions.set("$0,$1,$2,return $0.$connector.clear($1,$2)",
    function($0,$1,$2) {return $0.$connector.clear($1,$2)});
functions.set("$0,$1,window.dispatchEvent(new CustomEvent('vaadin-navigate', { detail: { state: $0, url: $1, replace: false } }));",
    function($0,$1) {window.dispatchEvent(new CustomEvent('vaadin-navigate', { detail: { state: $0, url: $1, replace: false } }));});
functions.set("$0,$1,window.dispatchEvent(new CustomEvent('vaadin-navigate', { detail: { state: $0, url: $1, replace: true } }));",
    function($0,$1) {window.dispatchEvent(new CustomEvent('vaadin-navigate', { detail: { state: $0, url: $1, replace: true } }));});
functions.set("event,element,return (event.code)",
    function(event,element) {return (event.code)});
functions.set("$0,$1,$2,try{Promise.resolve((async function(){return (async function() { window.Vaadin.Flow.comboBoxConnector.initLazy(this)}).apply($0)})()).then($1,function(error){$2(''+error)})}catch(error){$2(''+error)}",
    function($0,$1,$2) {try{Promise.resolve((async function(){return (async function() { window.Vaadin.Flow.comboBoxConnector.initLazy(this)}).apply($0)})()).then($1,function(error){$2(''+error)})}catch(error){$2(''+error)}});
functions.set("$0,$1,$2,try{Promise.resolve((async function(){return (async function() { this._shouldSetInvalid = function (invalid) { return false };}).apply($0)})()).then($1,function(error){$2(''+error)})}catch(error){$2(''+error)}",
    function($0,$1,$2) {try{Promise.resolve((async function(){return (async function() { this._shouldSetInvalid = function (invalid) { return false };}).apply($0)})()).then($1,function(error){$2(''+error)})}catch(error){$2(''+error)}});
functions.set("$0,$1,$2,$3,try{Promise.resolve((async function(){return $0.$connector.updateSize($1)})()).then($2,function(error){$3(''+error)})}catch(error){$3(''+error)}",
    function($0,$1,$2,$3) {try{Promise.resolve((async function(){return $0.$connector.updateSize($1)})()).then($2,function(error){$3(''+error)})}catch(error){$3(''+error)}});
functions.set("$0,$1,$2,$3,$4,$5,try{Promise.resolve((async function(){return $0.$connector.set($1,$2,$3)})()).then($4,function(error){$5(''+error)})}catch(error){$5(''+error)}",
    function($0,$1,$2,$3,$4,$5) {try{Promise.resolve((async function(){return $0.$connector.set($1,$2,$3)})()).then($4,function(error){$5(''+error)})}catch(error){$5(''+error)}});
functions.set("$0,$1,$2,$3,$4,try{Promise.resolve((async function(){return $0.$connector.confirm($1,$2)})()).then($3,function(error){$4(''+error)})}catch(error){$4(''+error)}",
    function($0,$1,$2,$3,$4) {try{Promise.resolve((async function(){return $0.$connector.confirm($1,$2)})()).then($3,function(error){$4(''+error)})}catch(error){$4(''+error)}});
functions.set("$0,$1,return (async function() { this._inputElementValue = $0}).apply($1)",
    function($0,$1) {return (async function() { this._inputElementValue = $0}).apply($1)});
functions.set("$0,$1,return (async function() { this.disabled = $0}).apply($1)",
    function($0,$1) {return (async function() { this.disabled = $0}).apply($1)});
functions.set("event,element,return (event.key)",
    function(event,element) {return (event.key)});
functions.set("event,element,return (1)",
    function(event,element) {return (1)});
functions.set("$0,$1,if ($1 == '_self') this.stopApplication(); window.open($0, $1)",
    function($0,$1) {if ($1 == '_self') this.stopApplication(); window.open($0, $1)});
functions.set("event,element,return (event.detail.username)",
    function(event,element) {return (event.detail.username)});
functions.set("event,element,return (event.detail.password)",
    function(event,element) {return (event.detail.password)});
functions.set("$0,$1,$2,return $0.$connector.doSelection($1,$2)",
    function($0,$1,$2) {return $0.$connector.doSelection($1,$2)});
functions.set("event,element,return (event.isComposing)",
    function(event,element) {return (event.isComposing)});
functions.set("$0,$1,$2,$3,try{Promise.resolve((async function(){return (async function() { this.disabled = $0}).apply($1)})()).then($2,function(error){$3(''+error)})}catch(error){$3(''+error)}",
    function($0,$1,$2,$3) {try{Promise.resolve((async function(){return (async function() { this.disabled = $0}).apply($1)})()).then($2,function(error){$3(''+error)})}catch(error){$3(''+error)}});
functions.set("$0,$1,$2,$3,try{Promise.resolve((async function(){return (async function() { this._inputElementValue = $0}).apply($1)})()).then($2,function(error){$3(''+error)})}catch(error){$3(''+error)}",
    function($0,$1,$2,$3) {try{Promise.resolve((async function(){return (async function() { this._inputElementValue = $0}).apply($1)})()).then($2,function(error){$3(''+error)})}catch(error){$3(''+error)}});
functions.set("event,element,return ((['Enter'].indexOf(event.code) !== -1 || ['Enter'].indexOf(event.key) !== -1) && !event.getModifierState('Shift') && !event.getModifierState('Control') && !event.getModifierState('Alt') && !event.getModifierState('AltGraph') && !event.getModifierState('Meta') && (event.stopPropagation() || true))",
    function(event,element) {return ((['Enter'].indexOf(event.code) !== -1 || ['Enter'].indexOf(event.key) !== -1) && !event.getModifierState('Shift') && !event.getModifierState('Control') && !event.getModifierState('Alt') && !event.getModifierState('AltGraph') && !event.getModifierState('Meta') && (event.stopPropagation() || true))});
functions.set("event,element,return (event.repeat)",
    function(event,element) {return (event.repeat)});
functions.set("event,element,return (event.location)",
    function(event,element) {return (event.location)});
functions.set("event,element,return ((['Escape','Esc'].indexOf(event.code) !== -1 || ['Escape','Esc'].indexOf(event.key) !== -1) && !event.getModifierState('Shift') && !event.getModifierState('Control') && !event.getModifierState('Alt') && !event.getModifierState('AltGraph') && !event.getModifierState('Meta') && (event.stopPropagation() || true))",
    function(event,element) {return ((['Escape','Esc'].indexOf(event.code) !== -1 || ['Escape','Esc'].indexOf(event.key) !== -1) && !event.getModifierState('Shift') && !event.getModifierState('Control') && !event.getModifierState('Alt') && !event.getModifierState('AltGraph') && !event.getModifierState('Meta') && (event.stopPropagation() || true))});
functions.set("$0,if(window.Vaadin.Flow.navigation) { window.addEventListener('vaadin-navigated', function(event) {    document.title = $0;\n    if(window?.Vaadin?.documentTitleSignal) {\n        window.Vaadin.documentTitleSignal.value = $0;\n    }\n}, {once:true}); }  else {     document.title = $0;\n    if(window?.Vaadin?.documentTitleSignal) {\n        window.Vaadin.documentTitleSignal.value = $0;\n    }\n }",
function($0) {if(window.Vaadin.Flow.navigation) { window.addEventListener('vaadin-navigated', function(event) {    document.title = $0;
  if(window?.Vaadin?.documentTitleSignal) {
    window.Vaadin.documentTitleSignal.value = $0;
  }
}, {once:true}); }  else {     document.title = $0;
  if(window?.Vaadin?.documentTitleSignal) {
    window.Vaadin.documentTitleSignal.value = $0;
  }
}});

