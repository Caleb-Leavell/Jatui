/*
 * Copyright (c) 2025 Caleb Leavell
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import com.calebleavell.jatui.modules.*;
import com.calebleavell.jatui.templates.ConfirmationPrompt;
import com.calebleavell.jatui.templates.LineBuilder;
import com.calebleavell.jatui.templates.NumberedModuleSelector;

import java.util.Random;
import java.util.Scanner;

import static com.calebleavell.jatui.modules.TextModule.OutputType.*;

import static org.fusesource.jansi.Ansi.*;

public class Main {

    public static void main(String[] args) {
        // This demo app will get a maximum number from the user, generate a random number between 1-maximum,
        // and repeat until the user decides to exit.

        // Application object
        ApplicationModule app = new ApplicationModule.Builder("app").build();

        // Builder for a TextModule that displays the output of another module, and is bold with gold text.
        // Since we've declared the builder here, we can copy it anywhere we want and update what we need.
        // It's generally a good idea to start abstracting modules away like this when they have complex
        // information attached to them (e.g. here it has the output type and ansi)
        var moduleOutput = new TextModule.Builder("module-output-template", "template")
                .setOutputType(DISPLAY_APP_STATE)
                // We set the ansi to a nice gold color.
                // Setting the ansi automatically locks it from being set again,
                // but you can unlock it with .unlockProperty(SET_ANSI)
                .setAnsi(ansi().bold().fgRgb(220, 180, 0));

        // This is a module that confirms if the user wants to exit.
        // It will be called later in the actual app.
        // If the user confirms they want to exit, the app terminates;
        // otherwise, the app restarts
        var confirmExit = new ConfirmationPrompt("confirm-exit",
                "Are you sure you want to exit (y/n)? ")
                .setApplication(app)
                .addOnConfirm(app::terminate)
                .addOnDeny(app::restart);

        // We declare the "scene" in a ContainerModule so that it's nicely compartmentalized and reusable if needed.
        var randomNumberGenerator = new ContainerModule.Builder("random-number-generator");
        randomNumberGenerator.addChildren(
            // Input Module that gets the maximum number
            new TextInputModule.Builder("get-random-number", "Maximum Number (or -1 to exit): ")
                    // We declare a safe handler to check for negative input.
                    // Since it's a safe handler, the input will rerun if the handler throws an exception.
                    // (we can define custom exception behavior if we wish in an overloaded method)
                    // If we want to only catch certain exceptions, that must be done in a try-catch in a regular handler.
                    .addSafeHandler("exit-if-negative", s -> {
                        // If it's negative we exit.
                        if(Integer.parseInt(s) < 0) {
                            app.runModuleAsChild(confirmExit);
                        }
                        return null;
                    }, "Error: input integer (your input might be too large)")
                    // We add another safe handler that references the logic for generating a random integer
                    // The input module will provide getRandomInt with the input it collected
                    .addSafeHandler("generated-number", Main::getRandomInt),
            // Text Modules that display the generated number
            // This can be done with TextModule.Builder, but TextBuilder facilitates chaining text modules.
            new LineBuilder("generated-number-display")
                    .addText("Generated Number: ")
                    // We create a copy of moduleOutput, declared above
                    // We update the name and set the text as the name of module "generated-number",
                    // which was also declared above.
                    // So this module will display whatever "generated-number" outputs.
                    .addText(moduleOutput.getCopy()
                            .setName("display-generated-number")
                            .setText("generated-number"))
                    .newLine(),
            // The templates package provides NumberedModuleSelector, which displays a numbered list of
            // text, asks for user input, and runs the module corresponding to the choice of the user.
            new NumberedModuleSelector("selector", app)
                    // This choice restarts the app
                    .addModule("Generate another number", ModuleFactory.restart("restart", app))
                    // The templates package also provides Terminate, which returns a FunctionModule builder
                    // that, when run, simply terminates the module that was inputted into Terminate().
                    // So here, it's terminating app.
                    .addModule("Exit", confirmExit));

        Scanner myScanner = new Scanner(System.in);
        myScanner.close();

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
        return rand.nextInt(max) + 1;
    }

    /**
     * Example of a template. This template creates a rectangle when run.
     * (Note, this template isn't used in the demo app. Further documentation on templating will come in the future.)
     */
    public static class Rect extends ModuleTemplate<Rect> {
        int x;
        int y;

        String cell = ".";

        public Rect(String name, int x, int y) {
            super(Rect.class, name);

            this.x = x;
            this.y = y;

            main.addChild(new FunctionModule.Builder("func", () -> logger.info("test")));
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
        public ContainerModule build() {
            main.clearChildren(); // this prevents the children from duplicating every time.

            for(int i = 0; i < y; i ++) {
                TextModule.Builder row = new TextModule.Builder(name + "-" + y, "");

                for(int j = 0; j < x; j ++) {
                    row.append(cell);
                }

                main.addChild(row);
            }

            return super.build();
        }

    }
}