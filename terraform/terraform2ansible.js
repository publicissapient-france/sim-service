#!/usr/local/bin/node

var fs = require('fs');
var config = JSON.parse(fs.readFileSync(__dirname + '/terraform.tfstate', 'utf8'));
var resources = config.modules[0].resources;
var ansible = {
    cores: [],
    factories: []
};
for (var key in resources) {
    var resource = resources[key];
    if (resource.type == 'aws_instance') {
        var host = {
            public_ip: resource.primary.attributes.public_ip,
            private_ip: resource.primary.attributes.private_ip
        };
        if (key.indexOf('aws_instance.core') != -1) {
            host.type = 'core';
            ansible.cores.push(host);
        }
        if (key.indexOf('aws_instance.factory') != -1) {
            host.type = 'factory';
            ansible.factories.push(host)
        }
    }
}

var stream = fs.createWriteStream("private/hosts");
stream.once('open', function (fd) {
    function writeHost(host) {

        stream.write(host.public_ip);
        for (var key in host) {
            stream.write(" " + key + "=" + host[key]);
        }
        stream.write("\n");
    }

    stream.write("[cores]\n");
    ansible.cores.forEach(function (host) {
        writeHost(host);
    });
    stream.write("\n");

    stream.write("[factories]\n");
    ansible.factories.forEach(function (host) {
        writeHost(host);
    });
    stream.end();
});
