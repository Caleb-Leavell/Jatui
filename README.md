# Jatui - A Java Text User Interface Library
[![Maven Central](https://img.shields.io/maven-central/v/io.github.calebleavell/jatui?color=007ec6)](https://central.sonatype.com/artifact/io.github.calebleavell/jatui)
[![Javadoc](https://img.shields.io/badge/javadoc-reference-007ec6?logo=openjdk&logoColor=white)](https://caleb-leavell.github.io/Jatui/)
[![Java Version](https://img.shields.io/badge/Java-21%2B-007ec6?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Changelog](https://img.shields.io/badge/changelog-Release%20Notes-007ec6)](https://github.com/Caleb-Leavell/Jatui/releases)
[![GitHub License](https://img.shields.io/github/license/Caleb-Leavell/Jatui?color=007ec6)](https://github.com/Caleb-Leavell/Jatui/blob/main/LICENSE)

<br>

Jatui is a library for making Text User Interface applications that **don't require per-keystroke input handling.** Since it targets a cooked (canonical) terminal, it allows for a much simpler overall system. If you *do* need functionality like per-keystroke input, then a library like [Ratatui](https://github.com/ratatui/ratatui) (or for java, [TamboUI](https://github.com/tamboui/tamboui)), might be a better fit.

Jatui is specifically aimed at easing several pain-points that you might find when building TUIs in native Java; primarily the fact that as the scope of the application grows, the amount of boilerplate increases dramatically (you can find a concrete comparison [here](https://github.com/Caleb-Leavell/Jatui/wiki#motivation)). Jatui provides a system that lets you define specific pieces of your TUI as reusable **modules** and put them together in a parameterizable structure. It's great for things like:
- **CLI Wizards** (e.g., config tools)
- **REPLs** (e.g., programming language interpreters)
- **Logic Prototyping** (e.g., testing a highly parameterizable algorithm)
- **Choice-based Games** (e.g., text adventures)
- And more!

Here's a simple example of the sort of control flow you can achieve with Jatui:

<img width="323" height="379" alt="image" src="https://github.com/user-attachments/assets/6fef49e7-88ed-4273-aff5-3fbddc4089b9" />

See the implementation [here](https://github.com/Caleb-Leavell/Jatui/blob/main/src/test/java/RandomNumber.java).

## Installation and Getting Started
**Prerequisites**: Java **21** or higher.

This library is on Maven Central! Add the following dependencies to your pom.xml:

```xml
<dependencies>
    <dependency>
        <groupId>io.github.calebleavell</groupId>
        <artifactId>jatui</artifactId>
        <version>1.0.2</version>
    </dependency>

    <dependency>
        <groupId>ch.qos.logback</groupId>
        <artifactId>logback-classic</artifactId>
        <version>1.5.20</version>
    </dependency>
</dependencies>
```

Jatui brings in [slf4j](https://github.com/qos-ch/slf4j) and [Jansi](https://github.com/fusesource/jansi). You'll also need an slf4j-compatible logging implementation (the above brings in logbback-classic; feel free to change it!).

Here's a simple "Hello, World!" app to get started:

```Java
import com.calebleavell.jatui.modules.ApplicationModule;
import com.calebleavell.jatui.modules.TextModule;

public class HelloWorld {
    public static void main(String[] args) {
        ApplicationModule app = ApplicationModule.builder("app").build();

        TextModule.Builder helloWorld =
                TextModule.builder("hello-world", "Hello, World!");

        app.setHome(helloWorld);
        app.start();
    }
}
```

Other demo apps can be viewed [here](https://github.com/Caleb-Leavell/Jatui/tree/main/src/test/java). Additionally, the javadoc can be viewed [here](https://caleb-leavell.github.io/Jatui/).

## Logging

The library uses [slf4j](https://github.com/qos-ch/slf4j) to log various information and errors. This means you will need an slf4j-compatible logback library (see above). If using [logback-classic](https://mvnrepository.com/artifact/ch.qos.logback/logback-classic), you will need a `logback.xml` file in the `resources` directory. Here's an example `logback.xml`:

```xml
<configuration>
    <appender name="FILE" class="ch.qos.logback.core.FileAppender">
        <file>test.log</file>
        <append>false</append>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="info">
        <appender-ref ref="FILE" />
    </root>
</configuration>
```

The logger will trace various actions performed by the library, as well as give warnings/errors for things like duplicate/nonexistent names.

