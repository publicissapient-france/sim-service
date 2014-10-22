var container = require('vertx/container');

container.deployVerticle('services/bank.js', container.config);
container.deployVerticle('service/farm/build/classes/Farm', container.config);
container.deployVerticle('services/factory.js', container.config);
container.deployVerticle('services/store.js', container.config);
container.deployVerticle('services/bridge.js', container.config);
