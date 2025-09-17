import com.calebleavell.jatui.modules.*;

import java.util.Random;

import static com.calebleavell.jatui.modules.TUITextModule.OutputType.*;

import static org.fusesource.jansi.Ansi.*;

public class Main {

    public static void main(String[] args) {
        // This demo app will get a maximum number from the user, generate a random number between 1-maximum,
        // and repeat until the user decides to exit.

        // Application object
        TUIApplicationModule app = new TUIApplicationModule.Builder("app").build();

        // Builder for a TUITextModule that displays the output of another module, and is bold with gold text.
        // Since we've declared the builder here, we can copy it anywhere we want and update what we need.
        // It's generally a good idea to start abstracting modules away like this when they have complex
        // information attached to them (e.g. here it has the output type and ansi)
        var moduleOutput = new TUITextModule.Builder("module-output-template", "template")
                .setOutputType(DISPLAY_MODULE_OUTPUT)
                // We set the ansi to a nice gold color.
                // Setting the ansi automatically locks it from being set again,
                // but you can unlock it with .unlockProperty(SET_ANSI)
                .setAnsi(ansi().bold().fgRgb(220, 180, 0));

        // Front-end
        // We declare the "scene" in a ContainerModule so that it's nicely compartmentalized and reusable if needed.
        var randomNumberGenerator = new TUIContainerModule.Builder("random-number-generator")
                .addChildren(
                        // Input Module that gets the maximum number
                        new TUITextInputModule.Builder("input", "Maximum Number (or -1 to exit): ")
                                // We declare a safe handler to check for negative input.
                                // Since it's a safe handler, the input will rerun if the handler throws an exception.
                                // (we can define custom exception behavior if we wish in an overloaded method)
                                // If we want to only catch certain exceptions, that must be done in a try-catch in a regular handler.
                                .addSafeHandler("exit-if-negative", s -> {
                                    // If it's negative we exit.
                                    if(Integer.parseInt(s) < 0) {
                                        app.terminate();
                                    }
                                    return null;
                                }, "Error: input integer (your input might be too large)")
                                // We add another safe handler that references the logic for generating a random integer
                                // The input module will provide getRandomInt with the input it collected
                                .addSafeHandler("generated-number", Main::getRandomInt),
                        // Text Modules that display the generated number
                        // This can be done with TUITextModule.Builder, but TextBuilder facilitates chaining text modules.
                        new TUIModuleFactory.LineBuilder("generated-number-display")
                                .addText("Generated Number: ")
                                // We create a copy of moduleOutput, declared above
                                // We update the name and set the text as the name of module "generated-number",
                                // which was also declared above.
                                // So this module will display whatever "generated-number" outputs.
                                .addText(moduleOutput.getCopy()
                                        .setName("display-generated-number")
                                        .setText("generated-number"))
                                .newLine(),
                        // TUIModuleFactory provides NumberedModuleSelector, which displays a numbered list of
                        // text, asks for user input, and runs the module corresponding to the choice of the user.
                        new TUIModuleFactory.NumberedModuleSelector("selector", app)
                                // Runs random-number-generator, which effectively restarts the app
                                .addModule("Generate another number", "random-number-generator")
                                // TUIModuleFactory also provides Terminate, which returns a TUIFunctionModule builder
                                // that, when run, simply terminates the module that was inputted into Terminate().
                                // So here, it's terminating app.
                                .addModule("Exit", TUIModuleFactory.terminate("terminate-app", app)));


        // Set the application home and run
        app.setHome(randomNumberGenerator);
        app.run();
    }

    // best practice to declare a single Random instance statically
    static Random rand = new Random();

    // "back-end" logic
    public static int getRandomInt(String input) {
        // Since we declared a safe handler above, and we're fine
        // with the default exception handling of rerunning the input,
        // we don't have to worry about the case where "input" cannot
        // be parsed as an integer.
        int max = Integer.parseInt(input.trim());
        return rand.nextInt(max);
    }

    /**
     * Example of a template. This template creates a rectangle when run.
     * (Note, this template isn't used in the demo app. Further documentation on templating will come in the future.)
     */
    public static class Rect extends TUIModule.Template<Rect> {
        int x;
        int y;

        String cell = ".";

        public Rect(String name, int x, int y) {
            super(Rect.class, name);

            this.x = x;
            this.y = y;

            main.addChild(new TUIFunctionModule.Builder("func", () -> logger.info("test")));
        }

        protected Rect() {
            super(Rect.class);
        }

        @Override
        protected Rect createInstance() {
            return new Rect();
        }

        @Override
        public void shallowCopy(Rect original) {
            this.x = original.x;
            this.y = original.y;
            cell = original.cell;
            super.shallowCopy(original);
        }

        public Rect cell(String cell) {
            this.cell = cell;
            return self();
        }

        @Override
        public TUIContainerModule build() {
            main.clearChildren(); // this prevents the children from duplicating every time.

            for(int i = 0; i < y; i ++) {
                TUITextModule.Builder row = new TUITextModule.Builder(name + "-" + y, "");

                for(int j = 0; j < x; j ++) {
                    row.append(cell);
                }

                main.addChild(row);
            }

            return super.build();
        }

    }
}