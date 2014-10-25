package fr.xebia.vertx.integration.java;

import java.util.concurrent.atomic.AtomicInteger;
import org.bouncycastle.util.Strings;
import org.junit.Test;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;
import org.vertx.testtools.VertxAssert;
import static org.vertx.testtools.VertxAssert.fail;
import static org.vertx.testtools.VertxAssert.testComplete;

/**
 *
 * test the store service.
 *
 * @author xebia
 */
public class StoreVerticleTest extends TestVerticle {

    /*
     * load the conf
     */
    private JsonObject loadConf() {
        Buffer buff = vertx.fileSystem().readFileSync("store.json");
        return new JsonObject(Strings.fromUTF8ByteArray(buff.getBytes()));
    }

    /**
     * Test the hello
     */
    @Test
    public void helloTest() {
        // given
        JsonObject conf = loadConf();
        long cancelId = vertx.setPeriodic(conf.getLong("helloRate", 5000) * 3, l -> {
            fail();
        });
        AtomicInteger count = new AtomicInteger();
        vertx.eventBus().registerHandler("/city", h -> {
            // then
            if (h.body() != null & h.body() instanceof JsonObject) {
                JsonObject hello = (JsonObject) h.body();
                VertxAssert.assertEquals(hello.getString("action"), "hello");
                VertxAssert.assertEquals(hello.getString("team"), "master");
                VertxAssert.assertNotNull(hello.getString("from"));
                VertxAssert.assertEquals(hello.getString("type"), "store");
                VertxAssert.assertNotNull(hello.getString("version"));
                if (count.incrementAndGet() > 1) {
                    vertx.cancelTimer(cancelId);
                    testComplete();
                }

            } else {
                fail();
            }
        });
        // when
        container.deployVerticle("fr.xebia.vertx.store.StoreVerticle", conf, 1, r -> {
            // no interaction with the store here.
        });
    }

    @Test
    public void testOrder() {
        // given
        JsonObject conf = loadConf();
        long cancelId = vertx.setPeriodic(conf.getLong("orderRate", 5000) * 4, l -> {
            fail();
        });
        AtomicInteger count = new AtomicInteger();
        vertx.eventBus().registerHandler("/city/factory", h -> {
            // then
            if (h.body() != null & h.body() instanceof JsonObject) {
                JsonObject order = (JsonObject) h.body();
                VertxAssert.assertEquals(order.getString("action"), "request");
                VertxAssert.assertNotNull(order.getString("from"));
                Long quantity = order.getLong("quantity");
                VertxAssert.assertTrue(quantity != null && quantity >= conf.getLong("quantity"));
                Long cost = order.getLong("cost");
                VertxAssert.assertTrue(cost != null && cost >= conf.getLong("cost"));
                if (count.incrementAndGet() > 1) {
                    vertx.cancelTimer(cancelId);
                    testComplete();
                }

            } else {
                fail();
            }
        });
        // when
        container.deployVerticle("fr.xebia.vertx.store.StoreVerticle", loadConf(), 1, r -> {
        });
    }

    @Test
    public void testAck() {
        // given
        JsonObject conf = loadConf();
        long cancelId = vertx.setPeriodic(conf.getLong("helloRate", 5000) * 2, l -> {
            fail();
        });
        container.deployVerticle("fr.xebia.vertx.store.StoreVerticle", loadConf(), 1, r -> {
            vertx.eventBus().registerHandler("/city/factory", h -> {
                JsonObject order = (JsonObject) h.body();
                JsonObject delivery = new JsonObject();
                delivery.putString("action", "response");
                delivery.putString("from", "factoryxxx");
                delivery.putNumber("quantity", 10);
                delivery.putNumber("cost", 1000);
                // when
                vertx.eventBus().sendWithTimeout("/city/store/" + order.getString("from"), delivery, 10000, res -> {
                    // then                    
                    if (res.result().body() != null && res.result().body() instanceof JsonObject) {
                        JsonObject reply = (JsonObject) res.result().body();
                        VertxAssert.assertEquals(reply.getString("action"), "acquittement");
                        VertxAssert.assertEquals(reply.getString("from"), order.getString("from"));
                        VertxAssert.assertEquals(reply.getNumber("quantity"), Long.valueOf(10));
                        vertx.cancelTimer(cancelId);
                        testComplete();
                    } else {
                        fail();
                    }
                });
            });
        });
    }

    @Test
    public void testBill() {
        // given
        JsonObject conf = loadConf();
        long cancelId = vertx.setPeriodic(conf.getLong("helloRate", 5000) * 2, l -> {
            fail();
        });
        container.deployVerticle("fr.xebia.vertx.store.StoreVerticle", loadConf(), 1, r -> {
            vertx.eventBus().registerHandler("/city/factory", h -> {
                JsonObject order = (JsonObject) h.body();
                JsonObject delivery = new JsonObject();
                delivery.putString("action", "response");
                delivery.putString("from", "factoryxxx");
                delivery.putNumber("quantity", 10);
                delivery.putNumber("cost", 1000);
                
                vertx.eventBus().registerHandler("/city/bank", b -> {
                    // then
                    if (b.body() != null & b.body() instanceof JsonObject) {
                        JsonObject sale = (JsonObject) b.body();
                        VertxAssert.assertEquals(sale.getString("action"), "sale");
                        VertxAssert.assertEquals(sale.getString("from"), order.getString("from"));
                        VertxAssert.assertEquals(sale.getString("charge"), "factoryxxx");
                        VertxAssert.assertEquals(sale.getNumber("quantity"), Long.valueOf(10));
                        VertxAssert.assertEquals(sale.getNumber("cost"), Long.valueOf(1000));
                        vertx.cancelTimer(cancelId);
                        testComplete();
                    } else {
                        fail();
                    }

                });
                // when
                vertx.eventBus().send("/city/store/" + order.getString("from"), delivery);
            });
        });
    }

}
