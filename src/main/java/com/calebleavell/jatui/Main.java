package com.calebleavell.jatui;

import com.calebleavell.jatui.modules.*;
import static com.calebleavell.jatui.modules.TUITextModule.OutputType.*;


import static org.fusesource.jansi.Ansi.*;


// TODO: Debug features
// TODO: documentation
// TODO: unit testing

public class Main {
    public static class Rect extends TUIModule.Template<Rect> {
        int x;
        int y;

        String cell = ".";

        public Rect(String name, int x, int y) {
            super(Rect.class, name);

            this.x = x;
            this.y = y;
        }

        protected Rect(Rect original) {
            super(original);
            this.x = original.x;
            this.y = original.y;
            this.cell = original.cell;
        }

        @Override
        public Rect getCopy() {
            return new Rect(this);
        }

        public Rect cell(String cell) {
            this.cell = cell;
            return self();
        }

        @Override
        public TUIContainerModule build() {
            main.clearChildren();

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


    public static void main(String[] args) {

        // Application object
        TUIApplicationModule app = new TUIApplicationModule.Builder("app").build();

        var moduleOutput = new TUITextModule.Builder("module-output-template", "template")
                .outputType(DISPLAY_MODULE_OUTPUT)
                .hardSetAnsi(ansi().bold().fgRgb(220, 180, 0));

        // Front-end
        var randomNumberGenerator = new TUIContainerModule.Builder("random-number-generator")
                .children(
                        new TUITextInputModule.Builder("input", "Maximum Number: ")
                                .addSafeHandler("exit-if-negative", s -> {
                                    if(Integer.parseInt(s) < 0) app.terminate();
                                    return null;
                                })
                                .addSafeHandler("generated-number", Main::getRandomInt),
                        new TUIModuleFactory.TextBuilder("generated-number-display")
                                .addText("Generated Number: ")
                                .addText(moduleOutput.getCopy()
                                        .setName("display-generated-number")
                                        .text("generated-number")),
                        new TUIModuleFactory.NumberedModuleSelector("selector", app)
                                .addScene("Generate another number", "random-number-generator")
                                .addScene("Exit", TUIModuleFactory.Terminate(app, "terminate-app")));

        // Run Application
        app.setChildren(randomNumberGenerator);
        app.run();
    }

    // "back-end" logic
    public static int getRandomInt(String input) {
        int max = Integer.parseInt(input);
        return new java.util.Random().nextInt(max);
    }
}