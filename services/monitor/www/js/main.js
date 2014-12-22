var city;
var logger;
var eventBus;

var conf = {
    id: 'monitor-' + UUID(),
    type: 'monitor',
    version: '1.0',
    team: 'masters'
};

function createContext(element, city) {
    var context = element.getContext('2d');
    context.canvas.width = (city.tileUnit * city.size);
    context.canvas.height = window.innerHeight + city.tileUnit;
    var scaleRatio = window.innerHeight / (0.707106 * city.tileUnit * city.size);
    context.scale(scaleRatio, scaleRatio);
    return context;
}

$(function () {
    logger = new Logger({
        container: "#events",
        maxLog: 10
    });

    var ladder = $('#ladder').find('#teams');
    city = new City(30);

    var element = document.getElementById('city_canvas');
    city.context = createContext(element, city);
    element = document.getElementById('animation_canvas');
    city.animationContext = createContext(element, city);

    city.onReady = function () {

        city.addDecoration('bank');
        city.addDecoration('stadium');
        city.addDecoration('tree',10);

        eventBus = new vertx.EventBus('http://' + location.host + '/eventbus');

        eventBus.onopen = function () {

            eventBus.registerHandler('/city/monitor', function (message) {
                console.log(message);
                switch (message.action) {
                    case 'up':
                        logger.log("Service " + message.service + " is up");
                        city.handleUpEvent(message);
                        break;
                    case 'down':
                        break;
                    case 'inventory':
                        city.updateModel(message.services);
                        break;
                }
            });

            eventBus.registerHandler('/city/monitor/' + conf.id, function (message) {
                city.initModel(message.services);
            });

            eventBus.send("/city", {
                action: 'inventory',
                from: conf.id
            });
            updateUi();
        };

    };

    city.onUpdateLadder = function (teams) {
        for (var key in teams) {
            var team = teams[key];
            var teamContainer = $("#team_" + team.getId());
            teamContainer.find(".purchases").html(team.totalPurchases());
            teamContainer.find(".costs").html(team.totalCosts());
            teamContainer.find(".sales").html(team.totalSales());
        }
    };

    city.onTeamCreated = function (team) {
        var title = $("<div>")
            .append('<div class="logo" style="background-color: ' + team.color + '"></div>')
            .append('<div class="label" >' + team.name + '</div>');

        var scores = $('<div>')
            .append('<div class="score sales" >0</div>')
            .append('<div class="score purchases" >0</div>')
            .append('<div class="score costs" >0</div>');

        var t = $("<div id='team_" + team.getId() + "' class='team'>")
            .append(title)
            .append('<div class="spacer" ></div>')
            .append(scores);
        ladder.append(t);
    };

    city.onBuildingRemoved = function (building) {
        logger.log("Building " + building.data.id + " has been removed");
    };
});

function updateUi() {
    requestAnimationFrame(updateUi);
    city.redraw();
}
