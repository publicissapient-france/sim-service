function BuildingTypeLoader(types, onready) {
    var imgCount = 0;
    for (var name in types) {
        imgCount++;
        var type = types[name];
        type.image.onload = function () {
            imgCount--;
            if (imgCount == 0) {
                onready.call();
            }
        };
        type.image.src = "img/" + name + ".png";
    }
}

function BuildingType(name, width, height) {
    this.name = name;
    this.image = new Image();
    this.width = width;
    this.height = height;
}

function Point(x, y) {
    this.x = x;
    this.y = y;
}

function Team(name) {
    this.name = name;
    this.factories = [];
    this.color = null;
    this.getId = function () {
        return this.name.replace(" ", "");
    };
}

function Factory(id) {
    this.id = id;
    this.score = 0;
}

/**
 * City
 * @param context
 * @param size
 * @constructor
 */
function City(size) {
    this.colors = ['red', 'green', 'blue', 'yellow', 'aquamarine', 'orange'];
    var that = this;
    this.context = null;
    this.tileUnit = 100;
    this.size = size;
    this.types = {
        farm: new BuildingType('farm', 2, 3),
        factory: new BuildingType('factory', 2, 2),
        store: new BuildingType('store', 1, 1),
        grass: new BuildingType('grass', 1, 1)
    };

    this.buildings = [];
    this.events = [];
    this.teams = [];
    this.matrix = new Array(this.size);
    for (var i = 0; i < this.size; i++) {
        this.matrix[i] = new Array(this.size);
    }

    this.onReady = null;
    this.onUpdateLadder = null;
    this.onTeamCreated = null;

    this.drawGrass = function () {
        // Draw grass
        for (var i = 0; i < city.size; i++) {
            for (var j = 0; j < city.size; j++) {
                this.drawBuilding(new Point(i, j), this.types.grass, null);
            }
        }

        var p0 = this.translate(0, this.size);
        var p1 = this.translate(this.size, this.size);
        var p2 = this.translate(this.size, 0);

        // side 1
        this.context.beginPath();
        this.context.fillStyle = "#492D00";
        this.context.moveTo(p0.x, p0.y);
        this.context.lineTo(p1.x, p1.y);
        this.context.lineTo(p1.x, p1.y + this.tileUnit / 2);
        this.context.lineTo(p0.x, p0.y + this.tileUnit / 2);
        this.context.lineTo(p0.x, p0.y);
        this.context.fill();
        this.context.closePath();

        //side 2
        this.context.beginPath();
        this.context.fillStyle = "#1E1200";
        this.context.moveTo(p2.x, p2.y);
        this.context.lineTo(p1.x, p1.y);
        this.context.lineTo(p1.x, p1.y + this.tileUnit / 2);
        this.context.lineTo(p2.x, p2.y + this.tileUnit / 2);
        this.context.lineTo(p2.x, p2.y);
        this.context.fill();
        this.context.closePath();

    };
    new BuildingTypeLoader(this.types, function () {
        that.drawGrass();
        that.onReady();
    });

    /**
     * Check buildings collision
     * @param buildingType
     * @param x
     * @param y
     * @returns {boolean}
     */
    this.hasCollision = function (buildingType, x, y) {
        for (var w = 0; w < buildingType.width; w++) {
            for (var h = 0; h < buildingType.height; h++) {
                if (w + x >= this.size) {
                    return true;
                }

                if (h + y >= this.size || this.matrix[w + x][h + y] != null) {
                    return true;
                }
            }
        }
        return false;
    };

    this.getTeamsCount = function () {
        var count = 0;
        for (x in this.teams) {
            count++;
        }
        return count;
    };

    this.initModel = function (services) {
        for (var i in services) {
            this.addBuilding(services[i]);
        }
    };

    this.showEvent = function(message){

    };

    /**
     * Add building to the city
     * @param data
     */
    this.addBuilding = function (data) {
        var type = this.types[data.type];
        if (type) {
            data.location = this.findRandomLocation(type);
            if (data.id) {
                this.buildings[data.id] = data;
            }
            this.fillMatrix(data.location, type);
            if (type.name == 'factory') {
                var team = this.teams[data.team];
                if (team == null) {
                    team = new Team(data.team);
                    team.color = this.colors[this.getTeamsCount()];
                    this.teams[data.team] = team;
                    this.onTeamCreated(team);
                }
                var factory = new Factory(data.id);
                if (data.score) {
                    factory.score = data.score;
                }
                team.factories[data.id] = factory;
                this.drawBuilding(data.location, type, team.color);
                this.onUpdateLadder(this.teams);
            } else {
                this.drawBuilding(data.location, type, null);
            }
        }


    };

    /**
     *
     * @param buildingType
     * @returns {*}
     */
    this.findRandomLocation = function (buildingType) {
        var x, y, maxTry = 10;
        while (maxTry > 0) {
            maxTry--;
            x = Math.floor(Math.random() * this.size);
            y = Math.floor(Math.random() * this.size);
            if (!this.hasCollision(buildingType, x, y)) {
                return new Point(x, y);
            }
        }
        return null;
    };

    /**
     *
     * @param location
     * @param building
     * @param background
     */
    this.drawBuilding = function (location, building, background) {
        var p0 = this.translate(location.x, location.y);
        var p1 = this.translate(location.x + building.width, location.y);
        var p2 = this.translate(location.x + building.width, location.y + building.height);
        var p3 = this.translate(location.x, location.y + building.height);
        if (background) {
            this.context.beginPath();
            this.context.fillStyle = background;
            this.context.moveTo(p0.x, p0.y);
            this.context.lineTo(p1.x, p1.y);
            this.context.lineTo(p2.x, p2.y);
            this.context.lineTo(p3.x, p3.y);
            this.context.lineTo(p0.x, p0.y);
            this.context.fill();
            this.context.closePath();
        }
        var scaleRatio = (p1.x - p3.x) / building.image.width;
        var scaledWidth = building.image.width * scaleRatio;
        var scaledHeight = building.image.height * scaleRatio;
        this.context.drawImage(building.image, p0.x - (p0.x - p3.x), p0.y - scaledHeight - (p0.y - p2.y), scaledWidth, scaledHeight);
    };

    this.fillMatrix = function (location, type) {
        for (var i = location.x; i < location.x + type.width; i++) {
            for (var j = location.y; j < location.y + type.height; j++) {
                this.matrix[i][j] = true;
            }
        }
    };

    /**
     * Translate coordinates
     * @param oriX
     * @param oriY
     * @returns {Point}
     */
    this.translate = function (oriX, oriY) {
        var angle = Math.PI / 4;
        var cityWidth = (this.size * this.tileUnit) / Math.cos(angle);
        var offset = cityWidth / 2;
        var x = offset + (Math.cos(angle) * (oriX * this.tileUnit) - Math.sin(angle) * (oriY * this.tileUnit));
        var y = (Math.sin(angle) * (oriX * this.tileUnit) + Math.cos(angle) * (oriY * this.tileUnit)) / 2;
        return new Point(x, y);
    };

    /*
     Handlers
     */
    this.handleScore = function (data) {

        var factory = this.getFactoryById(data.factoryId);
        factory.score += data.score;

        var building = this.buildings[data.factoryId];
        var event = {
            type: 'money',
            message: (data.score >= 0 ? "+" : "" ) + " " + data.score,
            color: data.score >= 0 ? '#64FE2E' : '#FF4000',
            location: this.translate(building.location.x, building.location.y),
            alpha: 2
        };
        this.events.push(event);

        this.onUpdateLadder(this.teams);

    };

    var delta = 0.1;

    this.redraw = function () {
        var event;
        for (var i in this.events) {
            event = this.events[i];
            var fontHeight = this.tileUnit * 0.5;
            this.animationContext.font = fontHeight + "px Verdana";
            var dim = this.animationContext.measureText(event.message);
            this.animationContext.clearRect(event.location.x, event.location.y - fontHeight + 5, dim.width, fontHeight);
            var alpha = event.alpha;
            if (alpha > 1) {
                alpha = 1;
            }

            if (alpha <= 0) {
                this.events.shift();
            } else {
                this.animationContext.globalAlpha = alpha;
                this.animationContext.fillStyle = event.color ? event.color : "black";
                this.animationContext.fillText(event.message, event.location.x, event.location.y);
            }
            event.alpha -= delta;
        }
    };

    this.getFactoryById = function (factoryId) {
        console.log(factoryId);
        for (var t in this.teams) {
            var team = this.teams[t];
            for (var f in team.factories) {
                var factory = team.factories[f];
                if (factory.id == factoryId) {
                    return factory;
                }
            }
        }
        return null;
    };

}


