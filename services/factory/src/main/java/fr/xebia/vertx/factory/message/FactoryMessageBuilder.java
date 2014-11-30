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
 * @author xebia
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
        return new JsonObject().putString(MessageField.ACTION.getFieldName(), 
                        MessageFieldValue.RESPONSE.getFieldValue())
                .putString(MessageField.FROM.getFieldName(), factorId)
                .putNumber(MessageField.QUANTITY.getFieldName(),
                        request.getNumber(MessageField.QUANTITY.getFieldName()))
                .putNumber(MessageField.COST.getFieldName(),
                        request.getNumber(MessageField.COST.getFieldName()));
    }

    /**
     *
     * Build a Ack message
     *
     * @param messageData
     * @return
     */
    public JsonObject buildAck(JsonObject messageData) {
        return new JsonObject().putString(MessageField.ACTION.getFieldName(), 
                        MessageFieldValue.ACQUITTEMENT.getFieldValue())
                .putString(MessageField.FROM.getFieldName(), factorId)
                .putNumber(MessageField.QUANTITY.getFieldName(),
                        messageData.getNumber(MessageField.QUANTITY.getFieldName()));
    }
    
    public JsonObject buildHelloMessage(String id, String version){
        return new JsonObject()
                .putString(MessageField.ACTION.getFieldName(), 
                        MessageFieldValue.HELLO.getFieldValue())
                .putString(MessageField.TEAM.getFieldName(), "master")
                .putString(MessageField.FROM.getFieldName(), id)
                .putString(MessageField.TYPE.getFieldName(), "factory")
                .putString(MessageField.VERSION.getFieldName(), version);
    }
    
    public JsonObject buildFarmRequest(String id, Number quantity){
        return new JsonObject()
                .putString(MessageField.ACTION.getFieldName(), 
                        MessageFieldValue.REQUEST.getFieldValue())
                .putString(MessageField.FROM.getFieldName(), id)
                .putNumber(MessageField.QUANTITY.getFieldName(),quantity);
    }

}
