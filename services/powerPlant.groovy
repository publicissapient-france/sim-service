import org.vertx.groovy.core.Vertx
import org.vertx.groovy.platform.Container

import static java.lang.Math.min
import static java.util.UUID.randomUUID

Vertx vx = vertx
Container dock = container

def me = [
        id  : randomUUID().toString(),
        type: 'powerPlant',
        name: 'Coal Power Plant'
]

def channels = [
        factory   : 'service.factory',
        powerPlant: 'service.powerPlant'
]

def conf = [
        hbDelay : (dock.config.hbDelay ?: 5000).toInteger(),
        newDelay: (dock.config[me.type]?.genDelay ?: 1000).toInteger(),
        newLevel: (dock.config[me.type]?.genLevel ?: 1).toInteger(),
        minLevel: (dock.config[me.type]?.minLevel ?: 0).toInteger(),
        maxLevel: (dock.config[me.type]?.maxLevel ?: 100).toInteger()
]

// announce me every 5 seconds
vx.setPeriodic(conf.hbDelay) { timerID -> vx.eventBus.publish(channels.powerPlant, me) }

// power level
int powerLevel = conf.minLevel

// generate 1 power unit every second
vx.setPeriodic(conf.newDelay) { timerID ->

    if (powerLevel < conf.maxLevel) {
        powerLevel += conf.newLevel
    }

    println "$me.type|$me.id|powerLevel $powerLevel"
}

// handle power requests
vx.eventBus.registerHandler(channels.powerPlant + '.' + me.id) { message ->

    if (message.body.powerRequest && message.body.factory) {

        println "$me.type|$me.id|powerRequest $message.body.powerRequest from $message.body.factory"

        // compute how much power request can be satisfied
        int powerRequest = message.body.powerRequest
        int powerResponse = min(powerRequest, powerLevel - conf.minLevel)
        powerLevel -= powerResponse

        // send power response to factory
        def response = [powerResponse: powerResponse, powerPlant: me.id]
        vx.eventBus.publish(channels.factory + '.' + message.body.factory, response)
    }
}
