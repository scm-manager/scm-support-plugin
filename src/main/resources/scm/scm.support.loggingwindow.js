/* *
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * http://bitbucket.org/sdorra/scm-manager
 * 
 */

Ext.ns('Sonia.support');

Sonia.support.LoggingWindow = Ext.extend(Ext.Window, {
  
  loggingEnabled: false,
  
  initComponent: function(){
    var config = {
      closable: true,
      resizable: false,
      plain: true,
      border: false,
      modal: true,
      constrain: true,
      constrainHeader: true,
      minimizable: false,
      maximizable: false,
      stateful: false,
      shim: true,
      buttonAlign:"center",
      width: 400,
      height: 100,
      minHeight: 80,
      footer: true,
      title: 'Trace Logging',
      items: [{
        xtype: 'label',
        text: 'Enable or disable trace logging'
      }],
      fbar: new Ext.Toolbar({
        items: [{
          text: 'Enable',
          hideMode: 'offsets',
          disabled: this.loggingEnabled,
          scope: this,
          handler: this.enableTraceLogging
        },{
          text: 'Disable',
          hideMode: 'offsets',
          disabled: ! this.loggingEnabled,
          scope: this,
          handler: this.disableTraceLogging
        }],
        enableOverflow: false
      })
    };
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.support.LoggingWindow.superclass.initComponent.apply(this, arguments);
  },
  
  enableTraceLogging: function(){
    var el = this.el;
    var tid = setTimeout( function(){el.mask('Loading ...');}, 100);
    
    Ext.Ajax.request({
      url: restUrl + 'plugins/support/logging/enable',
      method: 'POST',
      scope: this,
      success: function(response){
        clearTimeout(tid);
        el.unmask();
        this.loggingEnabled = true;
        this.close();
      },
      failure: function(result){
        clearTimeout(tid);
        el.unmask();
        main.handleFailure(
          result.status, 
          'Error', 
          'Could not enable trace logging'
        );
      }
    });
  },
  
  disableTraceLogging: function(){
    this.loggingEnabled = false;
    this.close();
    window.open(restUrl + 'plugins/support/logging/disable');
  }
  
});