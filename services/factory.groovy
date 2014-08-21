import org.vertx.groovy.core.Vertx
import org.vertx.groovy.platform.Container

Vertx vx = vertx
Container dock = container

def me = [
        id  : UUID.randomUUID().toString(),
        type: 'factory',
        name: 'Silly Factory'
]

def channels = [
        factory   : 'service.factory',
        powerPlant: 'service.powerPlant'
]

def conf = [
        hbDelay : (dock.config.hbDelay ?: 5000).toLong(),
        askDelay: (dock.config[me.type]?.askDelay ?: 5000).toInteger(),
        askLevel: (dock.config[me.type]?.askLevel ?: 5).toInteger()
]

def powerPlant

// announce me every 5 seconds
vx.setPeriodic(conf.hbDelay) { timerID -> vx.eventBus.publish(channels.factory, me) }

// Silly, only save last power plant announced
vx.eventBus.registerHandler(channels.powerPlant) { message ->

    if (message.body.id && message.body.type == 'powerPlant') {
        powerPlant = message.body
    }
}

// Silly, ask for power every x seconds
vx.setPeriodic(conf.askDelay) { timerID ->

    if (powerPlant) {
        def request = [powerRequest: conf.askLevel, factory: me.id]
        vx.eventBus.publish(channels.powerPlant + '.' + powerPlant.id, request)
    }
}

// log power responses
vx.eventBus.registerHandler(channels.factory + '.' + me.id) { message ->

    if (message.body.powerResponse && message.body.powerPlant) {

        println "$me.type|$me.id|powerResponse $message.body.powerResponse from $message.body.powerPlant"

        // todo do something with powerResponse ?
    }
}
