# Requirements

$ brew install gradle (optionnal)  
$ brew install groovy (optionnal)  
$ brew install vert.x  

# Getting started

## Start services
$ ./run.sh

## Start web-server
$ vertx runmod io.vertx~mod-web-server~2.0.0-final -conf vertx.webserver.conf

# Streams
All stream addresses are composed like this : "target_service_id"."source_service_id"

## PowerPlant
PowerPlant receives resource request from Factory "factory_id"  
power_plant_id.factory_id <= {}

PowerPlant receives service "www" creation from Bank "bank_id"
power_plant_id.bank_id <= {"service_id":"www"}

## Factory
Factory receives resource "xxx" from PowerPlant "power_plant_id"  
factory_id.power_plant_id <= {"resourceId":"xxx"}  
  
Factory consumes credit "zzz" from Bank "bank_id" for resource "xxx" consumption  
factory_id.bank_id <= {"resourceId":"xxx", "creditId":"zzz"}  
  
Factory receives credit "zzz" from Bank "bank_id" for product "yyy" production    
factory_id.bank_id => {"productId":"yyy", "creditId":"zzz"}  

## Bank
Bank "bank_id" receives a service creation request and identifies the new service "www"  
bank_id <= {"message":"Hello Georges"}  
(reply) => {"serviceId":"www", "message":"Hi there www"}  
