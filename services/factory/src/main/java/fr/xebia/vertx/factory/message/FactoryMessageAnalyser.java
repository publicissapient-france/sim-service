/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.xebia.vertx.factory.message;

import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

/**
 *
 * Class that is responsible for qualification of factory message.
 *
 * @author xebia
 */
public class FactoryMessageAnalyser {

    /**
     * Validate an order
     *
     * @param order
     * @return
     */
    public boolean validateOrder(JsonObject order) {
        return order.getString(MessageField.ACTION.getFieldName()) != null
                && order.getString(MessageField.ACTION.getFieldName())
                        .equals(MessageFieldValue.REQUEST.getFieldValue())
                && order.getString(MessageField.FROM.getFieldName()) != null
                && order.getNumber(MessageField.QUANTITY.getFieldName()) != null
                && order.getNumber(MessageField.COST.getFieldName()) != null;
    }

    /**
     * validate an ack
     *
     * @param ack
     * @return
     */
    public boolean validateAck(JsonObject ack) {
        return ack.getString(MessageField.ACTION.getFieldName()) != null
                && ack.getString(MessageField.ACTION.getFieldName())
                        .equals(MessageFieldValue.ACQUITTEMENT.getFieldValue())
                && ack.getNumber(MessageField.QUANTITY.getFieldName()) != null;
    }

    /**
     * Determine if the message comes from a farm
     *
     * @param message
     * @return
     */
    public boolean isFromFarm(JsonObject message) {
        return message.getString(MessageField.ACTION.getFieldName()) != null
                && message.getString(MessageField.ACTION.getFieldName())
                        .equals(MessageFieldValue.RESPONSE.getFieldValue())
                && message.getNumber(MessageField.QUANTITY.getFieldName()) != null
                && message.getNumber(MessageField.COST.getFieldName()) != null;
    }

    /**
     * Determine if the message comme from the bank.
     *
     * @param message
     * @return
     */
    public boolean isInfoFromBank(JsonObject message) {
        return message.getString(MessageField.ACTION.getFieldName()) != null
                && (message.getString(MessageField.ACTION.getFieldName())
                        .equals(MessageFieldValue.PURCHASE.getFieldValue())
                || message.getString(MessageField.ACTION.getFieldName())
                        .equals(MessageFieldValue.SALE.getFieldValue())
                || message.getString(MessageField.ACTION.getFieldName())
                        .equals(MessageFieldValue.COST.getFieldValue()))
                && message.getString(MessageField.FROM.getFieldName()) != null
                && message.getNumber(MessageField.QUANTITY.getFieldName()) != null
                && message.getNumber(MessageField.COST.getFieldName()) != null;
    }

    public boolean isJsonBody(Message message) {
        return message.body() != null && message.body() instanceof JsonObject;
    }

}
