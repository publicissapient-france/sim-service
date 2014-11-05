var vertx = require('vertx');
var console = require('vertx/console');

var httpServer = vertx.createHttpServer();
httpServer.requestHandler(function (req) {
    var file = '';
    if (req.path() == '/') {
        file = 'index.html';
    } else if (req.path().indexOf('..') == -1) {
        file = req.path();
    }
    req.response.sendFile('www/' + file);
});

var sockJSServer = vertx.createSockJSServer(httpServer);

sockJSServer.bridge({prefix: '/eventbus'}, [{}], [{}]);

httpServer.listen(8080);

