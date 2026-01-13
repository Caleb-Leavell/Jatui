# Jatui - A Java Text User Interface Library

Existing TUI libraries generally target *raw* terminals (handle user input every time a key is pressed). However, there are many applications that are simple enough to live in the default *cooked* terminal (handle user input when the user presses enter). Examples of these applications are:
- **CLI Wizards** (installation, configuration, setup flows)
- **Menu-Driven Tools** (interactive scripts, database helpers, git interfaces)
- **Logic Prototyping** (quickly testing algorithms or workflows)
- **Simple REPLs** (read–eval–print loops that don’t need per-keystroke input)
- **Text Adventures & Games** (turn-based input works well in cooked mode)

Jatui is a Java library that provides a framework for building TUIs that are meant to run in a cooked-terminal environment by implementing a modularized, declarative system that allows for reusable, customizable, and analyzable application units.


## Get Started

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
```

You may replace logback-classic with any logback library compatible with slf4j.

Here's a simple "Hello, World!" app to get started:

```Java
// declare a ApplicationModule to house our app
ApplicationModule app = new ApplicationModule("app").build();

// define the actual application structure
TextModule.Builder helloWorld = TextModule.builder("hello-world", "Hello, World!");

// set the app home and run
app.setHome(helloWorld);
app.run();
```

Other demo apps can be viewed [here](https://github.com/Caleb-Leavell/Jatui/tree/main/src/test/java).
