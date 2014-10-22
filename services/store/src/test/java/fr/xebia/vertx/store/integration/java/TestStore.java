/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.xebia.vertx.store.integration.java;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;
import org.vertx.testtools.VertxAssert;
import static org.vertx.testtools.VertxAssert.fail;
import static org.vertx.testtools.VertxAssert.testComplete;

/**
 *
 * @author xebia
 */
public class TestStore extends TestVerticle {

    /*
     * Build the conf of the store.
     */
    private JsonObject buildConf() {
        JsonObject conf = new JsonObject();
        conf.putNumber("orderRate", 1000);
        conf.putNumber("quantity", 10);
        return new JsonObject();
    }

    /**
     * TEst that a store will send one orders to one listening factory
     */
    @Test
    public void testSendOrder() {
        /*
         *Expected object.
         */
        JsonObject expected = new JsonObject();
        expected.putString("action", "order");
        expected.putString("from", "randomID");
        expected.putNumber("quantity", 10);

        /*
         * Set a time out for the test => 10 secs
         */
        long timeOutTask = vertx.setPeriodic(10000, l -> {
            container.logger().error("Time out reache for test : testSendOrder");
            fail();
        });
        vertx.eventBus().registerHandler("city.factory", m -> {
            container.logger().info("receive a message from somebody");
            /*
             * reset the timer
             */
            vertx.cancelTimer(timeOutTask);
            /*
             * check the JsonObject received
             */
            if (m.body() != null && m.body() instanceof JsonObject) {
                JsonObject received = (JsonObject) m.body();
                VertxAssert.assertEquals(received.getString("action"), expected.getString("action"));
                VertxAssert.assertTrue(received.getString("from").startsWith("store"));
                VertxAssert.assertEquals(received.getNumber("quantity"), expected.getNumber("quantity"));
            } else {
                fail();
            }
            testComplete();

        });
        container.deployVerticle("fr.xebia.vertx.store.StoreVerticle", buildConf(), 1, (res) -> {

        });
    }

}
