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

## Hello

Every factory service announces itself to /city

```json
{
  "action": "hello"
  "team": "choose your team name and stick to it",
  "id": "unique instance id",
  "type": "factory",
  "version": "instanceVersion"
}
```

Factory emits cereal request to some type of farm (ex: /city/farm/V1)

```json
{
   "from": "factory id",
   "action": "request",
   "quantity": 10
}
```

Farm responses to the factory

```json
{
    "from": "farm id",
    "action": "response",
    "quantity": 10,
    "cost": 100
}
``` 

Factory acquittement to the farm

```json
{
    "from": "factory id",
    "action": "acquittement",
    "quantity": 9
}
``` 

Farm send bill to the bank

```json
{
    "action": "bill",
    "from": "farm id",
    "charge": "factory id",
    "cost": 1234,
    "quantity": 9
}
```

