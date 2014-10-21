var city;
function assert(condition, message) {
    if (!condition) {
        message = message || "Assertion failed";
        if (typeof Error !== "undefined") {
            throw new Error(message);
        }
        throw message; // Fallback
    }
}
window.onload = function () {

    var canvas = document.getElementById('city_canvas');
    if (!canvas) {
        alert("Impossible de récupérer le canvas");
        return;
    }
    var context = canvas.getContext('2d');

    if (!context) {
        alert("Impossible de récupérer le context du canvas");
        return;
    }

    context.canvas.width = window.innerWidth;
    context.canvas.height = window.innerHeight;
    canvas.style.marginLeft = window.innerHeight / 2

    city = new City( 10);
    var scaleRatio = window.innerHeight / (0.707106 * city.tileUnit * city.size);
    context.scale(scaleRatio, scaleRatio);
    city.onReady = function () {
        console.log("Start test");
        var type = {
            width: 2,
            height: 2
        };
        assert(!city.hasCollision(type, 0, 0), "test01");
        assert(!city.hasCollision(type, 8, 8), "test02");
        assert(city.hasCollision(type, 9, 8), "should_have_collision_x");
        assert(city.hasCollision(type, 8, 9), "should_have_collision_y");
    }
}

