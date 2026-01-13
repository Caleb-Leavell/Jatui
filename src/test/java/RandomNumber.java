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
import com.calebleavell.jatui.templates.TextChain;
import com.calebleavell.jatui.templates.NumberedModuleSelector;

import java.util.Random;

import static com.calebleavell.jatui.modules.TextModule.OutputType.*;

import static org.fusesource.jansi.Ansi.*;

public class RandomNumber {

    public static void main(String[] args) {
        // This demo app will get a maximum number from the user, generate a random number between 1-maximum,
        // and repeat until the user decides to exit.

        // Application object
        ApplicationModule app = ApplicationModule.builder("app").build();

        // Builder for a TextModule that displays the output of another module, and is bold with gold text.
        // Since we've declared the builder here, we can copy it anywhere we want and update what we need.
        // It's generally a good idea to start abstracting modules away like this when they have complex
        // information attached to them (e.g. here it has the output type and ansi)
        TextModule.Builder moduleOutput = TextModule.builder("module-output-template", "template")
                .outputType(DISPLAY_APP_STATE)
                // We set the ansi to a nice gold color.
                // Setting the ansi automatically locks it from being set again,
                // but you can unlock it with .unlockProperty(SET_ANSI)
                .style(ansi().bold().fgRgb(220, 180, 0));

        // This is a module that confirms if the user wants to exit.
        // It will be called later in the actual app.
        // If the user confirms they want to exit, the app terminates;
        // otherwise, the app restarts
        ConfirmationPrompt confirmExit = ConfirmationPrompt.builder("confirm-exit",
                "Are you sure you want to exit (y/n)? ")
                .application(app)
                .addOnConfirm(app::terminate)
                .addOnDeny(app::restart);

        // We declare the "scene" in a ContainerModule so that it's nicely compartmentalized and reusable if needed.
        ContainerModule.Builder randomNumberGenerator = ContainerModule.builder("random-number-generator")
            .addChildren(
                TextModule.builder("title", "=== Random Number Generator ===")
                        .style(ansi().bold().fgRgb(200, 255, 255)),
                // Input Module that gets the maximum number
                TextInputModule.builder("get-max-number", "Maximum Number (or -1 to exit): ")
                        // We declare a safe handler to handle the input.
                        // Since it's a safe handler, the input will rerun if the handler throws an exception.
                        // (we can define custom exception behavior if we wish in an overloaded method)
                        // If we want to only catch certain exceptions, that must be done in a try-catch in a regular handler.
                        .addSafeHandler("generated-number", s -> {
                            int max = Integer.parseInt(s);
                            if(max < 0) {
                                // If it's negative we exit.
                                app.navigateTo(confirmExit);
                                return -1;
                            }
                            // generate the random number and save it
                            // in the app state
                            else return getRandomInt(max);
                        }, "Error: input integer (your input might be too large)"),
                TextChain.builder("generated-number-display")
                        .addText("Generated Number: ")
                        // We create a copy of moduleOutput, declared above
                        // We update the name and set the text as the name of module "generated-number",
                        // which was also declared above.
                        // So this module will display whatever "generated-number" outputs.
                        .addText(moduleOutput.getCopy()
                                .name("display-generated-number")
                                .text("generated-number"))
                        .newLine(),
                NumberedModuleSelector.builder("selector", app)
                        .addModule("Generate another number", ModuleFactory.restart("restart", app))
                        .addModule("Exit", confirmExit));

        // Set the application home and run
        app.setHome(randomNumberGenerator);
        app.start();
    }

    static Random rand = new Random();

    public static int getRandomInt(int max) {
        return rand.nextInt(max) + 1;
    }
}