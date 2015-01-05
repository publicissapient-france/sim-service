var vertx = require('vertx');
var container = require('vertx/container');
var console = require('vertx/console');

var conf = {
    id: 'bank',
    type: 'bank',
    version: 'evil bank',
    team: 'masters',
    delay: container.config.delay || 5000,
    downDelay: container.config.downDelay || 5000,
    inventoryDelay: container.config.inventoryDelay || 5000,
    debitStockThreshold: container.config.debitStockThreshold || -10,
    debitStockCost: container.config.debitStockCost || 5,
    creditStockThreshold: container.config.creditStockThreshold || 100,
    creditStockCost: container.config.creditStockCost || 2
};

/**
 services.farm|factory|store.serviceId = {
 id:'serviceId',
 type:'farm|factory|store',
 alive:true,
 lastAlive=2351312...,
 team:'masters',
 purchases:10,
 sales:20,
 costs:4,
 stocks:5,
 maxStocks:5,
 minStocks:5
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
                stocks: 0,
                maxStocks: 0,
                minStocks: 0
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

// periodically update services' alive status
vertx.setPeriodic(conf.delay, function (timerID) {

    var minLastAlive = new Date().getTime() - conf.downDelay;

    for (var type in services) {
        for (var id in services[type]) {
            services[type][id].alive = services[type][id].lastAlive > minLastAlive;
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

    if (!(factoryId && farmOrStoreId && quantity && cost && action)) {
        console.warn('Invalid message received @/city/bank ' + message);
        return;
    }

    var factory = services.factory[factoryId];
    if (!factory) {
        console.warn('Unknown factory received @/city/bank ' + factoryId);
        return;
    }

    var farmOrStore;
    if ('purchase' == action && (farmOrStore = services.farm[farmOrStoreId])) {
        factory.stocks += quantity;
        factory.purchases += cost;
        factory.maxStocks = Math.max(factory.maxStocks, factory.stocks);
        farmOrStore.quantity -= quantity;
        farmOrStore.sales += cost;
    } else if ('sale' == action && (farmOrStore = services.store[farmOrStoreId])) {
        factory.stocks -= quantity;
        factory.sales += cost;
        factory.minStocks = Math.min(factory.minStocks, factory.stocks);
        farmOrStore.quantity += quantity;
        farmOrStore.purchases += cost;
    } else {
        console.warn('Unknown action, farm or store received @/city/bank ' + message);
        return;
    }

    // send purchase or sale infos to factory
    vertx.eventBus.send('/city/factory/' + factoryId, {
        action: action,
        from: conf.id,
        quantity: quantity,
        cost: cost
    });
});

// periodically send status data to factory
vertx.setPeriodic(conf.delay, function (timerID) {

    for (var id in services.factory) {

        var factory = services.factory[id];
        if (!factory.alive) {
            // do nothing if factory is not alive
            continue;
        }

        var creditStocks = factory.maxStocks - conf.creditStockThreshold;
        if (creditStocks > 0) {
            factory.costs += conf.creditStockCost * creditStocks;
        }
        factory.maxStocks = 0;

        var debitStocks = conf.debitStockThreshold - factory.minStocks;
        if (debitStocks > 0) {
            factory.costs += conf.debitStockCost * debitStocks;
        }
        factory.minStocks = 0;

        // send status information to factory
        vertx.eventBus.send('/city/factory/' + id, {
            action: 'status',
            from: conf.id,
            purchases: factory.purchases,
            sales: factory.sales,
            costs: factory.costs,
            stocks: factory.stocks
        });
    }
});
