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

import static org.fusesource.jansi.Ansi.ansi;

public class RectToy {
    public static void main(String[] args) {
        // Simple rectangle toy application that takes width, height, and the cell string,
        // and displays the corresponding rectangle

        // The core application
        ApplicationModule app = ApplicationModule.builder("app").build();

        // pre-define a couple strings to display for organization
        String title = "Rectangle Builder";
        String hashTagLine = "#".repeat(title.length() + 4);

        // the rectangle that will be configured by the user
        Rect rectToDisplay = Rect.builder("display-rect", 0, 0)
                .style(ansi().bold());

        ContainerModule.Builder rectBuilder = ContainerModule.builder("rect-builder");
        // separate line so we can reference rectBuilder within the children
        rectBuilder.addChildren(
                    // collect the relevant fields
                    TextInputModule.builder("width-input", "Width: "),
                    TextInputModule.builder("height-input", "Height: "),
                    TextInputModule.builder("cell-input", "Cell character (e.g., \"#\"): ")
                            // It's easier to handle the inputs in a safe handler for the last
                            // input so we get the built-in exception handling.
                            .addSafeHandler("config-rect", cell -> {
                                // we don't bother handling null-case or invalid input since the
                                // safe handler automatically reruns on a runtime error

                                int width = Integer.parseInt(app.getInput("width-input", String.class));
                                int height = Integer.parseInt(app.getInput("height-input", String.class));

                                rectToDisplay.cell(cell);
                                rectToDisplay.width(width);
                                rectToDisplay.height(height);

                                // we have to explicitly navigate to the updated version instead
                                // of adding it as a child after this, otherwise the scheduler
                                // will build it before the updates apply
                                app.navigateTo(rectToDisplay);

                                return null;
                            }),
                    ConfirmationPrompt.builder("confirm-rerun", "Create another Rectangle (y/n)? ")
                            .addOnDeny(app::terminate),

                    // It's best practice to explicitly restart modules instead of creating cycles,
                    // since the latter grows the Heap (very slowly if it waits on user input)
                    ModuleFactory.restart("restart-rect-builder", app, "rect-builder")
                );

        ContainerModule.Builder home = ContainerModule.builder("hom")
                .addChildren(
                        TextChain.builder("title")
                                .addText(hashTagLine).newLine()
                                .addText("# " + title + " #").newLine()
                                .addText(hashTagLine).newLine()
                                .style(ansi().bold().fgRgb(200, 200, 220)),
                        rectBuilder
                );

        app.setHome(home);
        app.start();
    }

    // Templates are useful when we need complex/dynamic behavior,
    // or optional configuration
    // They simply abstract building a ContainerModule with the relevant
    // modules for the purpose.
    // This is a more advanced feature, and not required for most
    // simple applications.
    public static class Rect extends ModuleTemplate<Rect> {
        int width;
        int height;

        // cell has a default value since it's optional
        String cell = ".";

        protected Rect(String name, int width, int height) {
            super(Rect.class, name);

            this.width = width;
            this.height = height;
        }

        // making the constructor static to align with API syntax
        public static Rect builder(String name, int width, int height) {
            return new Rect(name, width, height);
        }

        // The next 3 methods are necessary for implementing deep copy behavior
        // While best practice, it isn't strictly necessary unless
        // this template is planned to be copied in the application

        // get an empty object
        protected Rect() {
            super(Rect.class);
        }

        // wrap the constructor
        @Override
        protected Rect createInstance() {
            return new Rect();
        }

        // copy the fields specific to this module
        @Override
        public void shallowCopy(Rect original) {
            this.width = original.width;
            this.height = original.height;
            cell = original.cell;
            super.shallowCopy(original);
        }

        // setters
        // "width" and "height" aren't strictly required to have setters
        // unless support for dynamic configuration is needed (which it
        // is for our application).
        // "cell" is required since it's optional and doesn't appear in
        // the constructor.

        public Rect width(int width) {
            this.width = width;
            return self();
        }

        public Rect height(int height) {
            this.height= height;
            return self();
        }

        // while the
        public Rect cell(String cell) {
            this.cell = cell;
            return self();
        }

        // Every template builds a ContainerModule
        // For some templates, it works best to build as you go.
        // For this template, it's easier to build everything at once.
        @Override
        public ContainerModule build() {
            main.clearChildren(); // this prevents the children from duplicating every time.

            for(int i = 0; i < height; i ++) {
                TextModule.Builder row = TextModule.builder(name + "-" + i, "");

                for(int j = 0; j < width; j ++) {
                    row.append(cell);
                }

                // Since they are being added to main, their properties will be automatically updated via
                // super.build()
                main.addChild(row);
            }

            return super.build();
        }

    }
}
