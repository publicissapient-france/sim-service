# Requirements

$ brew install gradle (optionnal)  
$ brew install groovy (optionnal)  
$ brew install vert.x  

# Getting started

## Start services
$ ./services.sh

## Start server
$ ./server.sh

# Streams

Every factory service announces itself to /city

* Hello :

```json
{
  "action": "hello",
  "team": "choose your team name and stick to it",
  "id": "unique instance id",
  "type": "factory",
  "version": "instanceVersion"
}
```

Factory emits cereal request to some type of farm (ex: /city/farm/V1)

* Request :

```json
{
   "from": "factory id",
   "action": "request",
   "quantity": 10
}
```

Farm reply to the factory

* Response

```json
{
    "from": "farm id",
    "action": "response",
    "quantity": 10,
    "cost": 100
}
``` 

Factory reply Acquittement to the farm
Store reply Acquittement to the factory

* Acquittement :

```json
{
    "from": "factory id",
    "action": "acquittement",
    "quantity": 9,
    "cost": 90
}
``` 

Farm send Bill to /city/bank
Store send Bill to the /city/bank
* Bill

```json
{
    "from": "farm id",
    "action": "bill",
    "charge": "factory id",
    "cost": 100,
    "quantity": 9
}
```




