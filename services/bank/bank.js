var vertx = require('vertx');
var container = require('vertx/container');
var console = require('vertx/console');

var conf = {
    id: 'bank',
    type: 'bank',
    version: 'evil bank',
    team: 'masters',
    delay: container.config.delay || 5000,
    downDelay: container.config.downDelay || 15000,
    stockDebitThreshold: container.config.stockDebitThreshold || -10,
    stockDebitCost: container.config.stockDebitCost || 5,
    stockCreditThreshold: container.config.stockCreditThreshold || 100,
    stockCreditCost: container.config.stockCreditCost || 2
};

/*
    Structure : services['farm|factory|store']['1234...'] {
      alive:true,
      lastAlive=2351312...,
      team:'masters',
      purchases:10,
      sales:20,
      costs:4,
      stocks:5,
      overStocks:5,
      underStocks:5
 }
 */
var services = {};

// listen for service hello messages
vertx.eventBus.registerHandler('/city', function (message) {

    var action = message.action;
    var serviceTeam = message.team;
    var serviceType = message.type;
    var serviceId = message.from;

container.logger.info(message.action + " "+message.team+" "+message.type);

    if ('hello' == action && serviceTeam && serviceType && serviceId) {
        var now = new Date().getTime();

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
                stocks: 0
            };


        }
        services[serviceType][serviceId].lastAlive = now;
    } else if ('inventoryRequest' == action) {
        var inventory = [];
        for (var type in services) {
            for (var id in services[type]) {
                var service = services[type][id];
                if (service.alive) {
                    service.id = id;
                    service.type = type;
                    inventory.push(service);
                }
            }
        }
        vertx.eventBus.send('/city/monitor/' + serviceId, {
            action: 'inventoryResponse',
            from: conf.id,
            services: inventory
        });
    }
});

// periodically watch for services as they become up or down
vertx.setPeriodic(conf.delay, function (timerID) {

    var minLastAlive = new Date().getTime() - conf.downDelay;

    for (var type in services) {
        for (var id in services[type]) {

            var service = services[type][id];
            var aliveNow = service.lastAlive > minLastAlive;

            if (aliveNow && !service.alive) {
                service.alive = true;
                // send up
                vertx.eventBus.send('/city/monitor', {
                    action: 'up',
                    from: conf.id,
                    service: service.id,
                    type:service.type,
                    team:service.team
                });
            } else if (!aliveNow && service.alive) {
                service.alive = false;
                // send down
                vertx.eventBus.send('/city/monitor', {
                    action: 'down',
                    from: conf.id,
                    service: service.id
                });
            }
        }
    }
});

// listen for factory purchase and store sale bills
vertx.eventBus.registerHandler('/city/bank', function (message) {

    var action = message.action;
    var factoryId = message.charge;
    var quantity = message.quantity;
    var cost = message.cost;

    if (factoryId && quantity && cost) {

        var factory = services.factory[factoryId];
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

    for (var id in services.factory) {

        var service = services.factory[id];

        // update costs as service has credit or debit stocks
        var stockCosts = 0;
        if (service.stocks > conf.stockCreditThreshold) {
            stockCosts = conf.stockCreditCost * Math.abs(service.stocks);
        } else if (service.stocks < conf.stockDebitThreshold) {
            stockCosts = conf.stockDebitCost * Math.abs(service.stocks);
        }
        service.costs += stockCosts;

        if (stockCosts !== 0) {
            // send stock cost
            vertx.eventBus.send('/city/factory/' + id, {
                action: 'cost',
                from: conf.id,
                quantity: service.stocks,
                cost: stockCosts
            });
        }

        // send data
        vertx.eventBus.send('/city/monitor', {
            action: 'data',
            from: conf.id,
            service: id,
            purchases: service.purchases,
            sales: service.sales,
            costs: service.costs,
            stocks: service.stocks
        });
    }
});

vertx.setPeriodic(conf.delay, function (timerID) {

    var minLastAlive = new Date().getTime() - conf.downDelay;
    for (var type in services) {
        for (var id in services[type]) {
            var service = services[type][id];
            var aliveNow = service.lastAlive > minLastAlive;

            // send status
            vertx.eventBus.send('/city/monitor', {
                action: aliveNow ? 'up' : 'down',
                from: conf.id,
                service: id
            });
        }
    }
});
