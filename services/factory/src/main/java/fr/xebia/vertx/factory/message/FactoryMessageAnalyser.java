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
 * @author jerdct
 */
public class FactoryMessageAnalyser {

    /**
     * Validate an order
     *
     * @param order
     * @return
     */
    public boolean validateOrder(JsonObject order) {
        return order.getString("action") != null
                && order.getString("action").equals("request")
                && order.getString("from") != null
                && order.getNumber("quantity") != null
                && order.getNumber("cost") != null;
    }

    /**
     * validate an ack
     *
     * @param ack
     * @return
     */
    public boolean validateAck(JsonObject ack) {
        return ack.getString("action") != null
                && ack.getString("action").equals("acquittement")
                && ack.getNumber("quantity") != null;
    }

    /**
     * Determine if the message comes from a farm
     *
     * @param message
     * @return
     */
    public boolean isFromFarm(JsonObject message) {
        return message.getString("action") != null
                && message.getString("action").equals("response")
                && message.getNumber("quantity") != null
                && message.getNumber("cost") != null;
    }

    /**
     * Determine if the message comme from the bank.
     *
     * @param message
     * @return
     */
    public boolean isInfoFromBank(JsonObject message) {
        return message.getString("action") != null
                && (message.getString("action").equals("purchase")
                || message.getString("action").equals("sale")
                || message.getString("action").equals("cost"))
                && message.getString("from") != null
                && message.getNumber("quantity") != null
                && message.getNumber("cost") != null;
    }

    public boolean isJsonBody(Message message) {
        return message.body() != null && message.body() instanceof JsonObject;
    }

}
