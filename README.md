# Jatui - A Java Text User Interface Library

Existing TUI libraries generally target *raw* terminals (handle user input every time a key is pressed). However, there are many applications that are simple enough to live in the default *cooked* terminal (handle user input when the user presses enter). Examples of these applications are:
- **CLI Wizards** (installation, configuration, setup flows)
- **Menu-Driven Tools** (interactive scripts, database helpers, git interfaces)
- **Logic Prototyping** (quickly testing algorithms or workflows)
- **Simple REPLs** (read–eval–print loops that don’t need per-keystroke input)
- **Text Adventures & Games** (turn-based input works well in cooked mode)

Jatui is a Java library that provides a framework for building TUIs that are meant to run in a cooked-terminal environment by implementing a modularized, declarative system that allows for reusable, customizable, and analyzable application units.


## Get Started

This library is currently in **beta**, and while it's stable and tested, it's missing the last few features (Password Input, toString cleanup, thorough documentation). However, it is still usable and useful, so feel free to use it if you'd like! (note, this library will be added to Maven Central once it's fully released)

**Note:** This library requires both Jansi and slf4j as a dependency

Here's a simple "Hello, World!" app to get started:

```Java
// declare a ApplicationModule to house our app
ApplicationModule app = new ApplicationModule("app").build();

// define the actual application structure
TextModule.Builder helloWorld = new TextModule.Builder("hello-world", "Hello, World!");

// set the app home and run
app.setHome(helloWorld);
app.run();
```

## Motivation

In native Java, building a simple TUI can be very verbose. For example, here's how you would make a simple random number generator that collects a maximum number, generates and displays the number in gold (using Jansi to simplify Ansi), and prompts the user if they want to generate another number or exit:

```Java
        Scanner scnr = new Scanner(System.in);
        Random rand = new Random();

        APP: while(true) {
            System.out.print("Maximum Number (or -1 to exit): ");
            String input = scnr.nextLine().trim();
            int max = -1;
            try {
                max = Integer.parseInt(input);
            }
            catch(NumberFormatException ex) {
                System.out.println("Error: input integer (your input might be too large)");
                continue;
            }

            if(max == -1) break;
            if(max <= 0) {
                System.out.println("Error: input integer must be greater than 0");
                break;
            }

            int randomNum = rand.nextInt(max);

            System.out.println("Generated Number: " + ansi().bold().fgRgb(220, 180, 0).a(randomNum).reset());

            while(true) {
                System.out.println(ansi().bold().a("[1]").reset() + " Generate another number");
                System.out.println(ansi().bold().a("[2]").reset() + " Exit");
                System.out.print("Your choice: ");

                int choice = -1;

                try {
                    choice = Integer.parseInt(scnr.nextLine());
                }
                catch(NumberFormatException e) {
                    System.out.println("Error: input must be integer");
                    continue;
                }

                switch(choice) {
                    case 1: continue APP;
                    case 2: break APP;
                    default: System.out.println("Error: input 1 or 2");
                }
            }
        }

        System.out.println(ansi().fgRgb(200, 100, 100).a("Exiting...").reset());
```

While functional, this approach becomes difficult to maintain or extend for larger projects. You could potentially abstract out pieces into their own methods, but if you wanted to resuse these components while customizing ansi styling, exact text, I/O locations, etc., verbosity increases dramatically.
Jatui aims to solve this problem by providing a declarative modularization framework. Here's how you would write the same program in Jatui:

```Java
public static void main(String[] args) {
        // Application object
        ApplicationModule app = new ApplicationModule.Builder("app").build();

        // Builder for a TextModule that displays the output of another module, and is bold with gold text.
        var moduleOutput = new TextModule.Builder("module-output-template", "template")
                .setOutputType(DISPLAY_MODULE_OUTPUT)
                .setAnsi(ansi().bold().fgRgb(220, 180, 0));

        // We declare the "scene" in a ContainerModule so that it's nicely compartmentalized and reusable if needed.
        var randomNumberGenerator = new ContainerModule.Builder("random-number-generator")
                .addChildren(
                        // Input Module that gets the maximum number
                        new TextInputModule.Builder("input", "Maximum Number (or -1 to exit): ")
                                // safe handler to check for negative input.
                                .addSafeHandler("exit-if-negative", s -> {
                                    // If it's negative we exit.
                                    if(Integer.parseInt(s) < 0) {
                                        app.terminate();
                                    }
                                    return null;
                                }, "Error: input integer (your input might be too large)")
                                // safe handler that references the logic for generating a random integer
                                .addSafeHandler("generated-number", Main::getRandomInt),
                        // Text Modules that display the generated number
                        new LineBuilder("generated-number-display")
                                .addText("Generated Number: ")
                                .addText(moduleOutput.getCopy()
                                        .setName("display-generated-number")
                                        .setText("generated-number"))
                                .newLine(),
                        // prompt the user to generate another number or exit
                        new NumberedModuleSelector("selector", app)
                                .addScene("Generate another number", "random-number-generator")
                                .addScene("Exit", terminate("terminate-app", app)));


        // Set the application home and run
        app.setHome(randomNumberGenerator);
        app.run();
    }

    static Random rand = new Random();

    // "back-end" logic
    public static int getRandomInt(String input) {
        int max = Integer.parseInt(input.trim());
        return rand.nextInt(max);
    }
```

Here's the output of the application:

<img width="445" height="289" alt="image" src="https://github.com/user-attachments/assets/ea4afdfa-db5a-43fa-a1b2-67a6af5fcf5d" />

For a fully commented version of this application that explains how everything works, visit the [Demo App](https://github.com/Caleb-Leavell/Jatui/blob/main/src/main/java/com/calebleavell/jatui/Main.java)

This library is an evolution of my previous [Java Text Interface Library](https://github.com/Caleb-Leavell/TextInterface). It's been rewritten from the ground up to be less verbose and more powerful.
