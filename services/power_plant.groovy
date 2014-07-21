import org.vertx.groovy.core.eventbus.Message
import org.vertx.groovy.platform.Container

import java.util.concurrent.atomic.AtomicInteger

import org.vertx.groovy.core.Vertx

Vertx vx = vertx
Container dock = container

def me = [
        id  : UUID.randomUUID().toString(),
        type: 'power.plant',
]

def channels = [
        output: [
                status: me.type + '.status'
        ],
        input : [
                consume: me.type + '.consume'
        ]
]

def conf = [
        maxLoad         : (dock.config[me.type]?.maxLoad ?: 10).toInteger(),
        initLoad        : (dock.config[me.type]?.initLoad ?: 0).toInteger(),
        productionPeriod: (dock.config[me.type]?.productionPeriod ?: 1000).toInteger(),
        startChannel    : 'monitoring.service.start'
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

/**
 * Service itself
 */
def emit = { String busAddress, Map message ->
    vx.eventBus.publish(busAddress, me + message)
}

AtomicInteger load = new AtomicInteger(conf.initLoad)

vx.setPeriodic(conf.productionPeriod) { timerID ->
    def newLoad = load.incrementAndGet()

    if (newLoad > conf.maxLoad) {
        load.set(conf.maxLoad)
        newLoad = conf.maxLoad
    }

    emit(channels.output.status, [load: newLoad])
}

vx.eventBus.registerHandler(channels.input.consume) { Message message ->
    println "Received on "+channels.input.consume



    if (message.body.need && message.body.replyTo) {
        int power = message.body.need.toInteger()
        int newLoad = load.addAndGet(-1 * power)
        if (newLoad < 0) {
            load.set(0)
            power = power + newLoad
        }
        emit(message.body.replyTo,[power: power])
    }
}

emit(conf.startChannel, channels)


