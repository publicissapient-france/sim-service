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

var services = {};

// listen for service hello messages
vertx.eventBus.registerHandler('/city', function(message) {

    if (message.type && message.from && message.team) {

        if (!services[message.type]) {
            // initialize object for type of service
            services[message.type] = {};
        }

        if (!services[message.type].[message.from]) {
            // initialize object for service
            services[message.type].[message.from] = {};
        }

        services[message.type].[message.from].alive = new Date();
        services[message.type].[message.from].team = message.team;
    }
});

// watch for services up or down
vertx.setPeriodic(conf.hbDelay, function (timerID) {

    var minAlive = new Date() - (5 * conf.hbDelay);

    for (var type in services) {
        for (var id in services[type]) {

            var service = services[type][id];
            var alive = service.alive < minAlive;

            if (alive && !service.status) {

                service.status = true;

                // send up
                vertx.eventBus.send('/city/monitor', {
                    action  : 'up',
                    from    : me.id,
                    service : id
                });
            }

            if (!alive && !service.down) {

                service.status = false;

                // send down
                vertx.eventBus.send('/city/monitor', {
                    action  : 'down',
                    from    : me.id,
                    service : id
                });
            }
        }
    }
});

// listen for bills
vertx.eventBus.registerHandler('/city/bank', function(message) {

    // handle bills
    var factoryId = message.charge;

    /*factories[factoryId]["purchases"] += message.quantity
    factories[factoryId]["sales"] += message.cost
    factories[factoryId]["stocks"] += message.quantity*/
});
