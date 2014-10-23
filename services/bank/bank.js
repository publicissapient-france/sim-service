var vertx = require('vertx');
var container = require('vertx/container');
var console = require('vertx/console');
var utils = require('./utils.js');

var conf = {
    id: 'bank',
    type: 'bank',
    version: 'evil bank',
    team: 'masters',
    delay: container.config.delay || 5000,
    downDelay: container.config.downDelay || 15000,
    stockThreshold: container.config.stockThreshold || 100,
    stockDebitCost: container.config.stockDebitCost || 5,
    stockCreditCost: container.config.stockCreditCost || 2,
};

/* Structure : services['farm|factory|store']['1234...'] {
    alive:true,
    lastAlive=10h10...,
    team:'masters',
    purchases:10,
    sales:20,
    costs:4,
    stocks:5,
    lastStocks:0
}*/
var services = {};

// listen for service hello messages
vertx.eventBus.registerHandler('/city', function(message) {

    var action = message.action;
    var serviceTeam = message.team;
    var serviceType = message.type;
    var serviceId = message.from;

    if ('hello' == action && serviceTeam && serviceType && serviceId) {

        if (!services[serviceType]) {
            // initialize object for type of service
            services[serviceType] = {};
        }

        if (!services[serviceType][serviceId]) {
            // initialize object for service
            services[serviceType][serviceId] = {
                team: serviceTeam,
                purchases: 0,
                sales: 0,
                costs: 0,
                stocks: 0,
                lastStocks: 0
            };
        }

        services[serviceType][serviceId].lastAlive = new Date();
    }
});

// periodically watch for services as they become up or down
vertx.setPeriodic(conf.delay, function (timerID) {

    var minLastAlive = new Date() - conf.downDelay;

    for (var type in services) {
        for (var id in services[type]) {

            var service = services[type][id];
            var aliveNow = service.lastAlive < minLastAlive;

            if (aliveNow && !service.alive) {

                service.alive = true;

                // send up
                vertx.eventBus.send('/city/monitor', {
                    action  : 'up',
                    from    : me.id,
                    service : id
                });

            } else if (!aliveNow && service.alive) {

                service.alive = false;

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

// listen for factory purchase and store sale bills
vertx.eventBus.registerHandler('/city/bank', function(message) {

    var action = message.action;
    var factoryId = message.charge;
    var quantity = message.quantity;
    var cost = message.cost;

    if (factoryId && quantity && cost) {

        var factory = services['factory'][factoryId];
        if (!factory) {
            console.warn('Unknown factory received for bill: ' + factoryId);
            return;
        }

        if ('purchase' == action) {
            factory.stocks += quantity;
            factory.purchases += cost;
        } else if ('sale' == action) {
            factory.stocks -= quantity;
            factory.sales += cost;
        } else {
            console.warn('Unknown action received for bill: ' + action);
            return;
        }

        // send purchase or sale infos
        vertx.eventBus.send('/city/factory/' + factoryId, {
            action: action,
            from: conf.id,
            quantity: quantity,
            cost: cost
        });
    }
});

// periodically update and send data to monitor service
vertx.setPeriodic(conf.delay, function (timerID) {

    for (var type in services) {
        for (var id in services[type]) {

            var service = services[type][id];

            // update costs as service has credit or debit stocks
            var deltaStocks = service.lastStocks - service.stocks;
            var stockCosts = 0;
            if (deltaStocks > conf.stockThreshold) {
                stockCosts = conf.stockCreditCost * deltaStocks;
            } else if (deltaStocks < -conf.stockThreshold) {
                stockCosts = conf.stockDebitCost * deltaStocks;
            }
            service.costs += stockCosts;
            service.lastStocks = service.stocks;

            if (stockCosts != 0) {
                // send stock cost
                vertx.eventBus.send('/city/factory/' + id, {
                    action: 'cost',
                    from: me.id,
                    quantity: deltaStocks,
                    cost: stockCosts
                });
            }

            // send data
            vertx.eventBus.send('/city/monitor', {
                action: 'data',
                from: me.id,
                service: id,
                purchases: service.purchases,
                sales: service.sales,
                costs: service.costs,
                stocks: service.stocks
            });
        }
    }
});
