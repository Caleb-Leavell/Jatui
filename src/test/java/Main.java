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

public class Main {

    public static void main(String[] args) {
        ApplicationModule app = ApplicationModule.builder("app").build();

        TextModule.Builder moduleOutput = TextModule.builder("module-output-template", "template")
                .outputType(DISPLAY_APP_STATE)
                .style(ansi().bold().fgRgb(220, 180, 0));

        ConfirmationPrompt confirmExit = ConfirmationPrompt.builder("confirm-exit",
                "Are you sure you want to exit (y/n)? ")
                .application(app)
                .addOnConfirm(app::terminate)
                .addOnDeny(app::restart);

        ContainerModule.Builder randomNumberGenerator = ContainerModule.builder("random-number-generator")
            .addChildren(
                TextModule.builder("title", "=== Random Number Generator ===")
                        .style(ansi().bold().fgRgb(200, 255, 255)),
                TextInputModule.builder("get-max-number", "Maximum Number (or -1 to exit): ")
                        .addSafeHandler("generated-number", s -> {
                            int max = Integer.parseInt(s);
                            if(max < 0) {
                                app.navigateTo(confirmExit);
                                return -1;
                            }
                            else return getRandomInt(max);
                        }, "Error: input integer (your input might be too large)"),
                TextChain.builder("generated-number-display")
                        .addText("Generated Number: ")
                        .addText(moduleOutput.getCopy()
                                .name("display-generated-number")
                                .text("generated-number"))
                        .newLine(),
                NumberedModuleSelector.builder("selector", app)
                        .addModule("Generate another number", ModuleFactory.restart("restart", app))
                        .addModule("Exit", confirmExit));

        app.setHome(randomNumberGenerator);
        app.start();
    }

    static Random rand = new Random();

    public static int getRandomInt(int max) {
        return rand.nextInt(max) + 1;
    }
}