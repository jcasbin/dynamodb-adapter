DynamoDB Adapter
====
[![Build Status](https://github.com/jcasbin/dynamodb-adapter/workflows/build/badge.svg)](https://github.com/jcasbin/dynamodb-adapter/actions)
[![codecov](https://codecov.io/gh/jcasbin/dynamodb-adapter/branch/master/graph/badge.svg?token=QU2JM5GNII)](https://codecov.io/gh/jcasbin/dynamodb-adapter)
[![javadoc](https://javadoc.io/badge2/org.casbin/dynamodb-adapter/javadoc.svg)](https://javadoc.io/doc/org.casbin/dynamodb-adapter)
[![Maven Central](https://img.shields.io/maven-central/v/org.casbin/dynamodb-adapter.svg)](https://mvnrepository.com/artifact/org.casbin/dynamodb-adapter/latest)
[![Discord](https://img.shields.io/discord/1022748306096537660?logo=discord&label=discord&color=5865F2)](https://discord.gg/S5UjpzGZjN)

DynamoDB Adapter is the [Amazon DynamoDB](https://en.wikipedia.org/wiki/Amazon_DynamoDB) adapter for [jCasbin](https://github.com/casbin/jcasbin), which provides interfaces for loading policies from DynamoDB and saving policies to it. 

**This adapter has been modernized to use AWS SDK v2 and supports zero-dependency testing with Testcontainers + LocalStack.**

## Features

- ✅ **AWS SDK v2**: Uses the modern AWS SDK v2 for DynamoDB operations
- ✅ **Zero-dependency Testing**: Integration tests run with Testcontainers + LocalStack (no AWS credentials required)
- ✅ **Java 8+**: Compatible with Java 8 and higher

Currently we support the following interfaces:
- `loadPolicy`
- `savePolicy`

## Installation
```xml
<dependency>
    <groupId>org.casbin</groupId>
    <artifactId>dynamodb-adapter</artifactId>
    <version>0.0.1</version>
</dependency>
```

## Example
```java
package com.company.example;

import org.casbin.jcasbin.main.Enforcer;
import org.casbin.adapter.DynamoDBAdapter;

public class Example {
    
    public static void main(String[] args) {
        Enforcer e = new Enforcer("examples/rbac_model.conf", "examples/rbac_policy.csv");

        String endpoint = "http://localhost:8000";
        String region = "cn-north-1";
        DynamoDBAdapter a = new DynamoDBAdapter(endpoint, region);

        // Create table (only needed once)
        a.createTable();

        // Save policy to DB
        a.savePolicy(e.getModel());

        // Load policy from DB
        a.loadPolicy(e.getModel());
    }
}
```

## Testing

This adapter uses Testcontainers with LocalStack for integration testing. To run tests:

```bash
mvn test
```

**No AWS credentials or configuration required!** Tests automatically start a LocalStack container with DynamoDB support.

## Getting Help

- [jCasbin](https://github.com/casbin/jcasbin)

## License

This project is under Apache 2.0 License. See the [LICENSE](LICENSE) file for the full license text.
