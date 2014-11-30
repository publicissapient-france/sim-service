/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.xebia.vertx.factory.message;

import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonObject;

/**
 *
 * A class that is responsible for building Json message for the factory.
 *
 * @author jerdct
 */
public class FactoryMessageBuilder {

    EventBus eventBus;
    String factorId;

    public FactoryMessageBuilder(EventBus eventBus, String factorId) {
        this.eventBus = eventBus;
        this.factorId = factorId;
    }

    /**
     * Build a response to an order
     *
     * @param request
     * @return
     */
    public JsonObject buildOrderResponse(JsonObject request) {
        return new JsonObject().putString("action", "response")
                .putString("from", factorId)
                .putNumber("quantity", request.getNumber("quantity"))
                .putNumber("cost", request.getNumber("cost"));
    }

    /**
     *
     * Build a Ack message
     *
     * @param messageData
     * @return
     */
    public JsonObject buildAck(JsonObject messageData) {
        return new JsonObject().putString("action", "acquittement")
                .putString("from", factorId)
                .putNumber("quantity", messageData.getNumber("quantity"));
    }
    
    public JsonObject buildHelloMessage(String id, String version){
        return new JsonObject()
                .putString("action", "hello")
                .putString("team", "master")
                .putString("from", id)
                .putString("type", "factory")
                .putString("version", version);
    }
    
    public JsonObject buildFarmRequest(String id, Number quantity){
        return new JsonObject()
                .putString("action", "request")
                .putString("from", id)
                .putNumber("quantity",quantity);
    }

}
