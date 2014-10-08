var vertx     = require('vertx');
var container = require('vertx/container');
var console   = require('vertx/console');
var utils     = require('./utils.js');

var me = {
    id  : utils.uuid(),
    type: 'bank',
    name: 'Evil Bank'
};

var channels = {
    bank      : 'service.bank',
    factory   : 'service.factory',
    powerPlant: 'service.powerPlant'
};

var conf = {
    hbDelay: container.config.hbDelay || 5000
};

// announce me every 5 seconds
vertx.setPeriodic(conf.hbDelay, function (timerID) {
    vertx.eventBus.publish(channels.bank, me)
});

var powerPlantIds = [];

var factoryIds = [];

// Listen for power plants as they announce themselves
vertx.eventBus.registerHandler(channels.powerPlant, function (message) {
    if (message.id && (message.type == 'powerPlant') && (powerPlantIds.indexOf(message.id) < 0)) {
        powerPlantIds.push(message.id);
        vertx.eventBus.registerHandler(channels.powerPlant + '.' + message.id, function (powerPlantMessage) {
            console.log(me.type + "|" + me.id + "|Intercept " + JSON.stringify(powerPlantMessage));
        });
    }
});

// Listen for factories as they announce themselves
vertx.eventBus.registerHandler(channels.factory, function (message) {
    if (message.id && (message.type == 'factory') && (factoryIds.indexOf(message.id) < 0)) {
        factoryIds.push(message.id);
        vertx.eventBus.registerHandler(channels.factory + '.' + message.id, function (factoryMessage) {
            console.log(me.type + "|" + me.id + "|Intercept " + JSON.stringify(factoryMessage));
        });
    }
});
