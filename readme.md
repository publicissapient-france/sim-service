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


# Webstream

## City buildings

```json
{
    "event": {
        "action": "init",
        "buildings": [
            {
                "type": "factory",
                "team": "team 1",
                "id": "f01",
                "version": "1.0",
                "score": 420
            },
            {
                "type": "factory",
                "team": "team 2",
                "id": "f11",
                "version": "1.0",
                "score": 333
            },
            {
                "type": "factory",
                "team": "team 2",
                "id": "a8da7965-6eb4-4e1c-825d-181b15c688c5",
                "version": "1.0",
                "score": 777
            },
            {
                "type": "farm",
                "id": "893a6820-f010-46f2-9084-a26596dd7daf",
                "version": "1.0",
            },
            {
                "type": "farm",
                "id": "ad20e4d2-e156-45c1-be46-f5b69b408828",
                "version": "1.0",
            }
        ]
    }
}
```




