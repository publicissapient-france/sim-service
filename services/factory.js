var vertx     = require('vertx');
var container = require('vertx/container');
var console   = require('vertx/console');
var utils     = require('./utils.js');

var me = {
    id: utils.uuid(),
    type: 'factory',
    version: 'simple factory',
    team: 'masters'
};

var conf = {
    hbDelay : container.config.hbDelay || 5000,
    askDelay: container.config[me.type].askDelay || 5000,
    askLevel: container.config[me.type].askLevel || 5
};

var stock = 0;

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

// ask for resources every x seconds
vertx.setPeriodic(conf.askDelay, function (timerID) {
    vertx.eventBus.publish('city.farm', {
            action  : 'request',
            from    : me.id,
            quantity: conf.askLevel
    });
});

// accept all resource responses
vertx.eventBus.registerHandler('city.factory.' + me.id, function (message, replier) {

    var offer = message.quantity;

    replier({
            action  : 'acquittement',
            from    : me.id,
            quantity: offer
    }); // TODO check if reply is still possible, timeout...

    stock += offer;
    console.log('Factory stock (+) => ' + stock);
});

// answer all product requests
vertx.eventBus.registerHandler('city.factory', function(message) {

    var storeId = message.from;
    var demand = message.quantity;
    var offer = Math.min(demand, stock);

    if (offer <= 0) {
        return;
    }

    // send response to store
    vertx.eventBus.send('city.store.' + storeId, {
            action  : 'response',
            from    : me.id,
            quantity: offer
    }, function (response) {
        stock -= response.quantity;
        console.log("Factory stock (-) => " + stock);
    });
});
