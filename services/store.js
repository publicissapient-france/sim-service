var vertx     = require('vertx');
var container = require('vertx/container');
var console   = require('vertx/console');
var utils     = require('./utils.js');

var me = {
    id: utils.uuid(),
    type: 'store',
    version: 'simple store',
    team: 'masters'
};

var conf = {
    hbDelay : container.config.hbDelay || 5000,
    askDelay: container.config[me.type].askDelay || 1,
    askLevel: container.config[me.type].askLevel || 1
};

var price = 2;
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

// ask for products every x seconds
vertx.setPeriodic(conf.askDelay, function (timerID) {

    vertx.eventBus.publish('city.factory', {
        action  : 'request',
        from    : me.id,
        quantity: conf.askLevel,
        cost    : conf.askLevel * price
    });
});

// accept all product responses
vertx.eventBus.registerHandler("city.store." + me.id, function (message, replier) {

    var offer = message.quantity;

    replier({
        action  : 'acquittement',
        from    : me.id,
        quantity: offer
    });

    stock += offer * price;
    console.log("Store stock (+) => " + stock);
});

