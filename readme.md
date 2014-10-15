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
  "from": "unique instance id",
  "type": "factory",
  "version": "version"
}
```

# Request :

* Factory emits cereal request to some type of farm (ex: /city/farm)

```json
{
   "action": "request",
   "from": "factory id",
   "quantity": 10
}
```

## Response :

* Farm reply to the factory

```json
{
    "action": "response",
    "from": "farm id",
    "quantity": 10,
    "cost": 100
}
``` 

## Acquittement :

* Factory reply Acquittement to the farm
* Store reply Acquittement to the factory



```json
{
    "action": "acquittement",
    "from": "factory id",
    "quantity": 9
}
``` 

## Bill

* Farm send Bill to /city/bank
* Store send Bill to the /city/bank


```json
{
    "action": "bill",
    "from": "farm id",
    "charge": "factory id",
    "quantity": 9,
    "cost": 100
}
```




