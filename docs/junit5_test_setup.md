# Setting Up JUnit 5 Test Suites for Hospital Management System

## Overview

This guide explains how to set up JUnit 5 test suites for our minimal Hospital Management System in IntelliJ IDEA.

## Dependencies

Add these dependencies to your `pom.xml`:

```xml
<!-- JUnit Jupiter API for writing tests -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <version>5.10.2</version>
    <scope>test</scope>
</dependency>

<!-- JUnit Jupiter Engine for running tests -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-engine</artifactId>
    <version>5.10.2</version>
    <scope>test</scope>
</dependency>

<!-- JUnit Platform Suite for creating test suites -->
<dependency>
    <groupId>org.junit.platform</groupId>
    <artifactId>junit-platform-suite-api</artifactId>
    <version>1.10.2</version>
    <scope>test</scope>
</dependency>

<!-- JUnit Platform Suite Engine -->
<dependency>
    <groupId>org.junit.platform</groupId>
    <artifactId>junit-platform-suite-engine</artifactId>
    <version>1.10.2</version>
    <scope>test</scope>
</dependency>
```

## Creating Test Suites

Create these directories and files for test organization:

```
src/test/java/com/example/hospitalsystemsimpletesting/suites/
```

### Model Test Suite

```java
package com.example.hospitalsystemsimpletesting.suites;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages("com.example.hospitalsystemsimpletesting.model")
public class ModelTestSuite {
    // Suite for all model tests
}
```

### Service Test Suite

```java
package com.example.hospitalsystemsimpletesting.suites;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages("com.example.hospitalsystemsimpletesting.service")
public class ServiceTestSuite {
    // Suite for all service tests
}
```

### All Tests Suite

```java
package com.example.hospitalsystemsimpletesting.suites;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({
    "com.example.hospitalsystemsimpletesting.model",
    "com.example.hospitalsystemsimpletesting.service",
    "com.example.hospitalsystemsimpletesting.controller",
    "com.example.hospitalsystemsimpletesting.integration"
})
public class AllTestsSuite {
    // Suite for all tests
}
```

## Running Test Suites in IntelliJ

1. Navigate to any test suite class
2. Right-click on the class file
3. Select "Run 'ModelTestSuite'" (or other suite name)
4. View results in the run tool window

## Configuring Maven to Run Suites

Add this to your `pom.xml` in the `build` section:

```xml
<plugins>
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>3.2.5</version>
        <configuration>
            <includes>
                <include>**/*Suite.java</include>
            </includes>
        </configuration>
    </plugin>
</plugins>
```

To run from command line:
```
mvn test
```

## Best Practices

1. Keep test suites simple - they're just organizational tools
2. Use package selection rather than listing every class when possible
3. Run individual tests during development, suites during integration
4. Use meaningful names for your test suites 