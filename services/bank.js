var vertx = require('vertx');
var container = require('vertx/container');
var console = require('vertx/console');
var utils = require('./utils.js');

var me = {
    id: utils.uuid(),
    type: 'bank',
    version: 'evil bank',
    team: 'masters'
};

var conf = {
    hbDelay: container.config.hbDelay || 5000
};

var farms = [];
var factories = [];
var stores = [];

// listen for service annoucements
vertx.eventBus.registerHandler('city', function(message) {

});

// listen for bills
vertx.eventBus.registerHandler('city.bank', function(message) {

    // handle bills
    var factoryId = message.charge;

    /*factories[factoryId]["purchases"] += message.quantity
    factories[factoryId]["sales"] += message.cost
    factories[factoryId]["stocks"] += message.quantity*/
});
