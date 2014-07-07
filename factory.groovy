import org.vertx.groovy.platform.Container

import java.util.concurrent.atomic.AtomicInteger

import org.vertx.groovy.core.Vertx

Vertx vx = vertx
Container dock = container

/**
 * Configuration
 */
def me = [
        id  : UUID.randomUUID().toString(),
        type: 'factory',
]

def channels = [
        output: [
                status : me.type + '.status',
                consume: me.type + '.consume',
                consume: me.type + '.production'
        ]
]

def conf = [
        powerPerUnit: (dock.config[me.type]?.maxLoad ?: 5).toInteger(),
        startChannel: 'monitoring.service.start'
]

/**
 * Debug listeners
 */
vx.eventBus.registerHandler(channels.output.status) { message ->
    println "OUTPUT ${channels.output.status} => ${message.body}"
}

vx.eventBus.registerHandler(conf.startChannel) { message ->
    println "OUTPUT ${conf.startChannel} => ${message.body}"
}

vx.eventBus.registerHandler(me.type + 'consume.' + me.id) { message ->
    println "OUTPUT ${channels.output.status} => ${message.body}"
}

/**
 * Service itself
 */
def emit = { String busAddress, Map message ->
    vx.eventBus.publish(busAddress, me + message)
}

emit(conf.startChannel, channels)

vx.setPeriodic(750) { timerID ->
    emit('power.plant.consume', [replyTo: me.type + 'consume.' + me.id, need: 4])
}
