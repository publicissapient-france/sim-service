import org.vertx.groovy.platform.Container

import java.util.concurrent.atomic.AtomicInteger

import org.vertx.groovy.core.Vertx

Vertx vx = vertx
Container dock = container

def me = [
        id: UUID.randomUUID().toString(),
        type: 'power.plant'
]

def conf = [
        maxLoad: (dock.config[me.type]?.maxLoad ?: 10).toInteger(),
        initLoad: (dock.config[me.type]?.initLoad ?: 0).toInteger()
]



def emit = { String busAddress, Map message ->
    vx.eventBus.publish(busAddress, me + message)
}

AtomicInteger load = new AtomicInteger(conf.initLoad)

def timerID = vx.setPeriodic(1000) { timerID ->
    def newLoad = load.incrementAndGet()

    if (newLoad > conf.maxLoad) {
        load.set(conf.maxLoad)
        newLoad = conf.maxLoad
    }

    emit("${me.type}.status", [load: newLoad])
}


vx.eventBus.registerHandler('power.plant.status') { message ->
    println message.body
}