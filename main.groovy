import org.vertx.groovy.platform.Container

Container dock = container

dock.deployVerticle('services/bank.groovy', dock.config)

dock.deployVerticle('services/factory.groovy', dock.config)

dock.deployVerticle('services/powerPlant.groovy', dock.config)
