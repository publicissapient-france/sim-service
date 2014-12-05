var vertx = require('vertx');
var container = require('vertx/container');
var console = require('vertx/console');

var conf = {
    id: 'bank',
    type: 'bank',
    version: 'evil bank',
    team: 'masters',
    delay: container.config.delay || 5000,
    inventoryDelay: container.config.delay || 2000,
    downDelay: container.config.downDelay || 15000,
    stockDebitThreshold: container.config.stockDebitThreshold || -10,
    stockDebitCost: container.config.stockDebitCost || 5,
    stockCreditThreshold: container.config.stockCreditThreshold || 100,
    stockCreditCost: container.config.stockCreditCost || 2
};

/*
 Structure :
 services['farm|factory|store']['1234...'] {
    id:'1234...',
    type:'farm|factory|store',
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
    var team = message.team;
    var type = message.type;
    var id = message.from;

    container.logger.info(JSON.stringify(message));

    if ('hello' == action && team && type && id) {

        if (!services[type]) {
            // initialize object for type of service
            services[type] = {};
        }

        if (!services[type][id]) {
            // initialize object for service
            services[type][id] = {
                id: id,
                type: type,
                alive: true,
                team: team,
                purchases: 0,
                sales: 0,
                costs: 0,
                stocks: 0
            };
        }

        // update lastAlive time flag
        services[type][id].lastAlive = new Date().getTime();

    } else if ('inventory' == action && id) {

        // on demand send inventory to a specific monitor
        vertx.eventBus.send('/city/monitor/' + id, {
            action: action,
            from: conf.id,
            services: services
        });

    } else {
        console.warn('Unknown message received @/city ' + message);
    }
});

// periodically send inventory to all monitors
vertx.setPeriodic(conf.inventoryDelay, function (timerID) {

    vertx.eventBus.send('/city/monitor', {
        action: 'inventory',
        from: conf.id,
        services: services
    });
});

// periodically flag services as they become alive or not
vertx.setPeriodic(conf.delay, function (timerID) {

    var minLastAlive = new Date().getTime() - conf.downDelay;

    for (var type in services) {
        for (var id in services[type]) {
            var service = services[type][id].alive = service.lastAlive > minLastAlive;
        }
    }
});

// listen for factory purchase and store sale bills
vertx.eventBus.registerHandler('/city/bank', function (message) {

    var action = message.action;
    var farmOrStoreId = message.from;
    var factoryId = message.charge;
    var quantity = message.quantity;
    var cost = message.cost;

    if (factoryId && farmOrStoreId && quantity && cost) {

        var factory = services.factory[factoryId];
        if (!factory) {
            console.warn('Unknown factory received @/city/bank ' + factoryId);
            return;
        }

        var farmOrStore;
        if ('purchase' == action && (farmOrStore = services.farm[farmOrStoreId])) {
            factory.stocks += quantity;
            factory.purchases += cost;
            farmOrStore.quantity -= quantity;
            farmOrStore.sales += cost;
        } else if ('sale' == action && (farmOrStore = services.store[farmOrStoreId])) {
            factory.stocks -= quantity;
            factory.sales += cost;
            farmOrStore.quantity += quantity;
            farmOrStore.purchases += cost;
        } else {
            console.warn('Invalid message received @/city/bank ' + message);
            return;
        }

        // send purchase or sale infos to factory
        vertx.eventBus.send('/city/factory/' + factoryId, {
            action: action,
            from: conf.id,
            quantity: quantity,
            cost: cost
        });

    } else {
        console.warn('Invalid message received @/city/bank' + message);
    }
});

// periodically update costs data and send, if necessary, costs related infos to factories
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
            // send stocks cost
            vertx.eventBus.send('/city/factory/' + id, {
                action: 'cost',
                from: conf.id,
                quantity: service.stocks,
                cost: stockCosts
            });
        }
    }
});
