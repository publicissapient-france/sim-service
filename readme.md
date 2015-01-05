# Requirements

$ brew install gradle (optionnal)  
$ brew install groovy (optionnal)  
$ brew install vert.x  

# Streams

## Hello :

* Service periodically announces itself to "/city"

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

* Factory emits Request to "/city/farm"
* Store emits Request to "/city/factory"

```json
{
   "action": "request",
   "from": "factory id",
   "quantity": 10,
   "cost" : 1000 // for stores
}
```

## Response :

* Farm sends Response with timeout to "/city/factory/id"
* Factory sends Response with timeout to "/city/store/id"

```json
{
    "action": "response",
    "from": "farm id",
    "quantity": 10,
    "cost": 100
}
``` 

## Acquittement :

* Factory replies Acquittement to the farm
* Store replies Acquittement to the factory



```json
{
    "action": "acquittement",
    "from": "factory id",
    "quantity": 9
}
``` 

## Purchase and Sale bills :

* Farm sends Purchase bill to "/city/bank"
* Store sends Sale bill to the "/city/bank"


```json
{
    "action": "purchase|sale",
    "from": "farm id",
    "charge": "factory id",
    "quantity": 9,
    "cost": 100
}
```

## Purchase and Sale receipts :

* Bank sends Purchase info to "/city/factory/id"
* Bank sends Sale info to "/city/factory/id"


```json
{
    "action": "purchase|sale|cost",
    "from": "bank",
    "quantity": 9,
    "cost": 100
}
```

## Status metrics :

* Bank sends status to "/city/factory/id"

```json
{
    "action": "status",
    "from": "bank",
    "purchases": 100,
    "sales": 100,
    "costs": 100,
    "stocks": 100
}
```
