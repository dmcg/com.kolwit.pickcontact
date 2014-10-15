/**
 * @constructor
 */
var PickContact = function(){};


PickContact.prototype.chooseContact = function(success, failure, type){
    var args = type ? [type] : [];
    cordova.exec(success, failure, "PickContact", "chooseContact", args);
};

// Plug in to Cordova
cordova.addConstructor(function() {

    if (!window.Cordova) {
        window.Cordova = cordova;
    };


    if(!window.plugins) window.plugins = {};
    window.plugins.PickContact = new PickContact();
});
