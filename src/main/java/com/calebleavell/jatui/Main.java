package com.calebleavell.jatui;

import com.calebleavell.jatui.modules.*;
import static com.calebleavell.jatui.modules.TUITextModule.OutputType.*;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import static org.fusesource.jansi.Ansi.*;
import static org.fusesource.jansi.Ansi.Color.*;

import java.util.ArrayList;
import java.util.List;

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

    static TUIApplicationModule app = new TUIApplicationModule.Builder("app").build();

    public static void main(String[] args) {

        var exit = new TUITextModule.Builder("exit", "Exiting...")
                .hardSetAnsi(ansi().fgRgb(150, 100, 100))
                .addChild(new TUIContainerModule.Builder("erase-screen").setAnsi(ansi().eraseScreen()));

        var randomNumberGenerator = new TUIContainerModule.Builder("random-number-generator")
                .children(
                        new TUITextInputModule.Builder("input", "Maximum Number: ")
                                .addSafeHandler("generated-number", Main::getRandomInt),
                        new TUIModuleFactory.TextBuilder("generated-number-display")
                                .addText("Generated Number: ")
                                .addText(new TUITextModule.Builder("display-generated-number", "generated-number")
                                        .outputType(DISPLAY_MODULE_OUTPUT)
                                        .hardSetAnsi(ansi().bold().fgRgb(255, 255, 0))),
                        new TUIModuleFactory.NumberedModuleSelector("selector", app)
                                .addScene("Generate another number", "random-number-generator")
                                .addScene("Exit", exit)
                );

        Rect myRect = new Rect("a", 3, 4).cell("#");

        app.setHome(myRect);
        app.run();
    }

    public static int getRandomInt(String input) {
        int max = Integer.parseInt(input);
        return new java.util.Random().nextInt(max);
    }
}