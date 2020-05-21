DynamoDB Adapter
====

[![Build Status](https://travis-ci.org/jcasbin/dynamodb-adapter.svg?branch=master)](https://travis-ci.org/jcasbin/dynamodb-adapter)

DynamoDB Adapter is the [Amazon DynamoDB](https://en.wikipedia.org/wiki/Amazon_DynamoDB) adapter for [jCasbin](https://github.com/casbin/jcasbin), which provides interfaces for loading policies from DynamoDB and saving policies to it. 

Currently we only support fot the following interfaces:
- `loadPolicy`
- `savePolicy`

## Installation
TODO

## Example
```java
package com.company.example;

import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.util.Util;
import org.casbin.adapter.DynamoDBAdapter;

public class Example {
    
    Enforcer e = new Enforcer("examples/rbac_model.conf", "examples/rbac_policy.csv");

    String endpoint = "http://localhost:8000";
    String region = "cn-north-1";
    DynamoDBAdapter a = new DynamoDBAdapter(endpoint, region);

    // Save policy to DB
    a.savePolicy(e.getModel());

    // Load policy from DB
    a.loadPolicy(e.getModel());
}
```

## Getting Help

- [jCasbin](https://github.com/casbin/jcasbin)

## License

This project is under Apache 2.0 License. See the [LICENSE](LICENSE) file for the full license text.