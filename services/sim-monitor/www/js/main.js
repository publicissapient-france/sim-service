var city;
var serviceLocation = "ws://0.0.0.0:8090/";
var wsocket;
function createContext(element, city) {
    var context = element.getContext('2d');
//    context.canvas.width = window.innerWidth;
    context.canvas.width = (city.tileUnit * city.size);
    context.canvas.height = window.innerHeight + city.tileUnit;
    var scaleRatio = window.innerHeight / (0.707106 * city.tileUnit * city.size);
    context.scale(scaleRatio, scaleRatio);
    return context;
}
var eb = new vertx.EventBus('http://54.77.63.200:8080/eventbus');

eb.onopen = function () {
    console.log("open");
    eb.registerHandler('/city/monitor', function (message) {
        console.log('received a message: ' + JSON.stringify(message));
    });
};

window.onload = function () {


    var ladder = $('#ladder').find('#teams');

    city = new City(30);

    var element = document.getElementById('city_canvas');

    console.log(element);
    city.context = createContext(element, city);
    element = document.getElementById('animation_canvas');
    city.animationContext = createContext(element, city);

    city.onReady = function () {

//        wsocket = new WebSocket(serviceLocation);
//        wsocket.onmessage = function (message) {
//            var data = JSON.parse(message.data);
//            var event = data.event;
//            console.log("Received " + event.type);
//
//            switch (event.type) {
//                case "init":
//                    console.log(event);
//                    city.initModel(event);
//                    break;
//            }
//        };

        updateUi();
    };

    city.onUpdateLadder = function (teams) {
        console.log('update ladder');
        var score, team;
        for (var name in teams) {
            team = city.teams[name];
            score = 0;
            for (var factoryId in team.factories) {
                score += team.factories[factoryId].score;
            }
            $("#team_" + team.getId() + " .score").html(score);
        }
    };

    city.onTeamCreated = function (team) {
        var t = $("<div id='team_" + team.getId() + "' class='team'>");
        t.append('<div class="logo" style="background-color: ' + team.color + '">');
        t.append('<div class="label" >' + team.name + '</div>');
        t.append('<div class="score" >0</div>');
        ladder.append(t)
    };


};

function updateUi() {
    requestAnimationFrame(updateUi);
    city.redraw();
}

