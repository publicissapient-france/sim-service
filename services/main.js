var container = require('vertx/container');

container.deployVerticle('services/bank.js', container.config)
container.deployVerticle('services/farm.js', container.config)
container.deployVerticle('services/factory.js', container.config)
container.deployVerticle('services/store.js', container.config)
