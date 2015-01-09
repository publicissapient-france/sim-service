#!/usr/local/bin/node

var fs = require('fs');
var config = JSON.parse(fs.readFileSync(__dirname + '/terraform.tfstate', 'utf8'));
var resources = config.modules[0].resources;
var ansible = {};
var attributes = ['public_ip', 'private_ip','type'];

for (var key in resources) {
    var resource = resources[key];
    if (resource.type == 'aws_instance') {
        var host = resource.primary.attributes;
        var myRegexp = /aws_instance\.([^.]*)/g;
        var match = myRegexp.exec(key);
        var type = match[1];
        if (match) {
            host.type = type;
            if (ansible[type] == null) {
                ansible[type] = [];
            }
            ansible[type].push(host);
        }
    }
}

var stream = fs.createWriteStream("private/inventory");
stream.once('open', function (fd) {
    function writeHost(host) {
        stream.write(host.public_ip);
        for (var key in host) {
            if (attributes.indexOf(key) != -1) {
                stream.write(" " + key + "=\"" + host[key] + "\"");
            }
        }
        stream.write("\n");
    }

    for (var key in ansible) {
        stream.write("[" + key + "]\n");
        ansible[key].forEach(function (host) {
            writeHost(host);
        });
    }

    stream.end();
});
