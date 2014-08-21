import org.vertx.groovy.core.Vertx
import org.vertx.groovy.platform.Container

Vertx vx = vertx
Container dock = container

def me = [
        id  : UUID.randomUUID().toString(),
        type: 'bank',
        name: 'Evil Bank'
]

def channels = [
        bank      : 'service.bank',
        factory   : 'service.factory',
        powerPlant: 'service.powerPlant'
]

def conf = [
        hbDelay: (dock.config.hbDelay ?: 5000).toInteger()
]

// announce me every 5 seconds
vx.setPeriodic(conf.hbDelay) { timerID -> vx.eventBus.publish(channels.bank, me) }

def powerPlantIds = []

def factoryIds = []

// Listen for power plants as they announce themselves
vx.eventBus.registerHandler(channels.powerPlant) { message ->

    if (message.body.id && message.body.type == 'powerPlant' && !powerPlantIds.contains(message.body.id)) {
        powerPlantIds.add(message.body.id)
        vx.eventBus.registerHandler(channels.powerPlant + '.' + message.body.id) { powerPlantMessage -> println "$me.type|$me.id|Intercept $powerPlantMessage.body" }
    }
}

// Listen for factories as they announce themselves
vx.eventBus.registerHandler(channels.factory) { message ->

    if (message.body.id && message.body.type == 'factory' && !factoryIds.contains(message.body.id)) {
        factoryIds.add(message.body.id)
        vx.eventBus.registerHandler(channels.factory + '.' + message.body.id) { factoryMessage -> println "$me.type|$me.id|Intercept $factoryMessage.body" }
    }
}
