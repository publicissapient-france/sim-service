var vertx     = require('vertx');
var container = require('vertx/container');
var console   = require('vertx/console');
var utils     = require('./utils.js');

var me = {
    id: utils.uuid(),
    type: 'farm',
    version: 'simple farm',
    team: 'masters'
};

var conf = {
    hbDelay : container.config.hbDelay || 5000,
    genDelay: container.config[me.type].genDelay || 1000,
    minLevel: container.config[me.type].minLevel || 0,
    maxLevel: container.config[me.type].maxLevel || 100
};

var price = 2;
var stock = 0;
var offers = 0;

// announce me every x seconds
vertx.setPeriodic(conf.hbDelay, function (timerID) {
    vertx.eventBus.publish('city', {
        action : 'hello',
        from   : me.id,
        type   : me.type,
        version: me.version,
        team   : me.team
    });
});

// generate 1 resource unit every x second
vertx.setPeriodic(conf.genDelay, function(timerID) {

    if (stock < conf.maxLevel) {
        stock += 1;
    }
});

// handle factories' resource requests
vertx.eventBus.registerHandler('city.farm', function(message) {

    // compute how much resource can be satisfied
    var factoryId = message.from
    var resourceDemand = message.quantity
    var offer = Math.min(resourceDemand, stock - (conf.minLevel + offers))

    if (offer <= 0) {
        return;
    }

    // lock resource
    offers += offer;

    // send response to factory
    vertx.eventBus.sendWithTimeout('city.factory.' + factoryId, {
        action  : 'response',
        from    : me.id,
        quantity: offer,
        cost    : offer * price
    }, 1000, function (error, response) {

        if (!error) {
            stock -= response.quantity;
            console.log("Farm stock (-) => " + stock);
        } else {
            console.log("No reply for offer");
        }

        // free resource
        offers -= offer
    });
});
