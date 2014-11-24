var city;
var conf = {
    id: 'monitor-' + UUID(),
    type: 'monitor',
    version: '1.0',
    team: 'masters'
};

function createContext(element, city) {
    var context = element.getContext('2d');
//    context.canvas.width = window.innerWidth;
    context.canvas.width = (city.tileUnit * city.size);
    context.canvas.height = window.innerHeight + city.tileUnit;
    var scaleRatio = window.innerHeight / (0.707106 * city.tileUnit * city.size);
    context.scale(scaleRatio, scaleRatio);
    return context;
}
var host = location.host;
if (location.hostname == 'localhost') {
    host = "54.77.63.200:8080";
}

var eb = new vertx.EventBus('http://' + host + '/eventbus');

function log(message) {
    eventContainer.prepend('<div>' + message + '</div>');
    if (eventContainer.children().size() > 10) {
        eventContainer.children().last().remove();
    }
}
eb.onopen = function () {
    eb.registerHandler('/city/monitor', function (message) {
        city.addBuilding(message);
    });

    eb.registerHandler('/city/monitor/' + conf.id, function (message) {
        console.log(message);
        city.initModel(message.services);
    });

    eb.registerHandler('/city/factory', function (message) {
        city.onEvent("Store " + message.from + " requested " + message.quantity, message);
    });
};

setInterval(sendHello, 2000);

function getHour() {
    var date = new Date();
    return date.getHours() + ":" + padLeft(date.getMinutes()) + ":" + padLeft(date.getSeconds());
}
function padLeft(number) {
    return ( number < 10 ? "0" : "" ) + number;
}
window.onload = function () {


    var ladder = $('#ladder').find('#teams');
    eventContainer = $('#events');
    city = new City(30);

    var element = document.getElementById('city_canvas');
    city.context = createContext(element, city);
    element = document.getElementById('animation_canvas');
    city.animationContext = createContext(element, city);

    city.onReady = function () {
        updateUi();
        eb.send("/city", {
            action: 'inventoryRequest',
            from: conf.id
        });
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
        ladder.append(t);
    };

    city.onEvent = function (message, event) {

        eventContainer.append('<div><span style="display:inline-block;width: 80px">' + getHour() + "</span> " + message + '</div>');
        if (eventContainer.children().size() > 8) {
            eventContainer.children().first().remove();
        }
    }
};

function updateUi() {
    requestAnimationFrame(updateUi);
    city.redraw();
}

function sendHello() {
    var message = {
        action: 'hello',
        from: conf.id,
        team: conf.team,
        type: conf.type,
        version: conf.version
    };
    eb.send("/city", message);
}

