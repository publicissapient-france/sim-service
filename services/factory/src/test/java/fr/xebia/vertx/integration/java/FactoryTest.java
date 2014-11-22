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
 *
 *
 * @author xebia
 */
public class FactoryTest extends TestVerticle {

    /*
     * load the conf
     */
    private JsonObject loadConf() {
        Buffer buff = vertx.fileSystem().readFileSync("factory.json");
        return new JsonObject(Strings.fromUTF8ByteArray(buff.getBytes()));
    }

    @Test
    public void whenStartedThenSendHello() {
        // given
        JsonObject conf = loadConf();
        long cancelId = vertx.setPeriodic(conf.getLong("helloRate", 5000) * 3, l -> {
            fail();
        });
        AtomicInteger count = new AtomicInteger();
        vertx.eventBus().registerHandler("/city", h -> {
            // then
            if (h.body() != null & h.body() instanceof JsonObject) {
                vertx.cancelTimer(cancelId);
                JsonObject hello = (JsonObject) h.body();
                VertxAssert.assertEquals(hello.getString("action"), "hello");
                VertxAssert.assertEquals(hello.getString("team"), "master");
                VertxAssert.assertNotNull(hello.getString("from"));
                VertxAssert.assertEquals(hello.getString("type"), "factory");
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
        container.deployVerticle("fr.xebia.vertx.factory.FactoryVerticle", conf, 1, r -> {
            // no interaction with the store here.
        });
    }

    @Test
    public void whenReceiveOrderThenResponseWithExistantStock() {
        // given
        JsonObject conf = loadConf();
        String id = "TestStore";
        long cancelId = vertx.setPeriodic(15000, l -> {
            //fail();
        });
        container.deployVerticle("fr.xebia.vertx.factory.FactoryVerticle", conf, 1, r -> {
            vertx.eventBus().registerHandler("/city/farm", stockRequest -> {
                if (stockRequest.body() != null && stockRequest.body() instanceof JsonObject) {
                    JsonObject request = (JsonObject) stockRequest.body();
                    VertxAssert.assertEquals(request.getString("action"), "request");
                    VertxAssert.assertNotNull(request.getString("from"));
                    VertxAssert.assertNotNull(request.getNumber("quantity"));
                    JsonObject stockResponse = new JsonObject().putString("action", "response")
                            .putString("from", id)
                            .putNumber("quantity", request.getNumber("quantity"))
                            .putNumber("cost", 1000);

                    vertx.eventBus().sendWithTimeout("/city/factory/" + request.getString("from"), stockResponse, 5000,
                            factoryAck -> {
                                if (factoryAck.failed() 
                                        || factoryAck.result().body() == null 
                                        || !(factoryAck.result().body() instanceof JsonObject)) {
                                    fail();
                                }
                            });

                    // then
                    vertx.eventBus().registerHandler("/city/store/" + id, response -> {
                        vertx.cancelTimer(cancelId);
                        if (response.body() != null && response.body() instanceof JsonObject) {
                            JsonObject hello = (JsonObject) response.body();
                            VertxAssert.assertEquals(hello.getString("action"), "response");
                            VertxAssert.assertNotNull(hello.getString("from"));
                            VertxAssert.assertEquals(hello.getNumber("quantity"), 10);
                            VertxAssert.assertEquals(hello.getNumber("cost"), 100);
                            testComplete();
                        } else {
                            fail();
                        }

                    });
                    JsonObject order = new JsonObject().putString("action", "request").putString("from", id).putNumber(
                            "quantity", 10).putNumber("cost", 100);
                    // when
                    vertx.eventBus().publish("/city/factory", order);
                }
            });
        });
    }
}
