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

## Hello :

Every factory service announces itself to /city

```json
{
  "action": "hello",
  "team": "choose your team name and stick to it",
  "id": "unique instance id",
  "type": "factory",
  "version": "instanceVersion"
}
```

# Request :

* Factory emits cereal request to some type of farm (ex: /city/farm/V1)

```json
{
   "from": "factory id",
   "action": "request",
   "quantity": 10
}
```

## Response :

* Farm reply to the factory

```json
{
    "from": "farm id",
    "action": "response",
    "quantity": 10,
    "cost": 100
}
``` 

## Acquittement :

* Factory reply Acquittement to the farm
* Store reply Acquittement to the factory



```json
{
    "from": "factory id",
    "action": "acquittement",
    "quantity": 9,
    "cost": 90
}
``` 

## Bill

* Farm send Bill to /city/bank
* Store send Bill to the /city/bank


```json
{
    "from": "farm id",
    "action": "bill",
    "charge": "factory id",
    "cost": 100,
    "quantity": 9
}
```




