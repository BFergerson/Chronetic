# Chronetic v1.0 (Alpha)
: Experimental Java Time Pattern Analysis Library

## Overview

[Chronetic](http://chronetic.io/) is an open-source time pattern analysis library built to describe time-series data. Written in Java, using [Jenetics](http://jenetics.io/), an advanced genetic algorithm; Chronetic is able to locate the most prevalent patterns occuring in a given time-series dataset. Patterns are aggregated into a **Chronotype** and can be translated into a human-readable format with a **ChronoDescriptor**.

## Example

```java
ChronoSeries chronoSeries = ChronoSeries.of(
        Instant.parse("2011-11-04T08:48:11Z"),
        Instant.parse("2012-11-02T09:23:16Z"),
        Instant.parse("2013-11-01T09:51:49Z"),
        Instant.parse("2014-11-07T08:43:00Z"),
        Instant.parse("2015-11-06T08:22:25Z")
);

//static access, default engine
String description = Chronetic.defaultEngine()
        .analyze(chronoSeries).withHourPrecision()
        .describe().humanReadable();

//Once a year from 2011 to 2015 on the first Friday of November between 8AM - 10AM
System.out.println(description);


//custom engine
Chronetic chronetic = Chronetic.configure()
        .populationSize(500)
        .survivorsSize(250).offspringSize(250)
        .maxGeneration(15).build();

description = chronetic.analyze(chronoSeries)
        .withHourPrecision()
        .describe().humanReadable();

//Once a year from 2011 to 2015 on the first Friday of November between 8AM - 10AM
System.out.println(description);
```

## Installation

### Gradle

```
repositories {
     jcenter()
     maven { url "https://jitpack.io" }
}

dependencies {
      compile 'com.github.codebrig:chronetic:v1.0-alpha'
}
```

### Maven

```
<repositories>
	<repository>
		<id>jitpack.io</id>
		<url>https://jitpack.io</url>
	</repository>
</repositories>

<dependency>
	<groupId>com.github.codebrig</groupId>
	<artifactId>chronetic</artifactId>
	<version>v1.0-alpha</version>
</dependency>
```

## API Reference

Visit http://chronetic.io/javadoc/ for the latest and most up-to-date JavaDoc documentation.

## Building/Testing

Build Chronetic:
```
./gradlew build
```

Run Chronetic JUnit tests:
```
./gradlew test
```

## Contributors

Any and all contributions are welcome. Bring on the pull requests.

## License

The library is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

        Copyright 2017 CodeBrig, LLC.

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
