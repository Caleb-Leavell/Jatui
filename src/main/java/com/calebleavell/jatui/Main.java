package com.calebleavell.jatui;

import com.calebleavell.jatui.modules.*;

import java.util.ArrayList;
import java.util.List;

// TODO: Ansi
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

        var exit = new TUITextModule.Builder("exit", "Exiting...");

        var randomNumberGenerator = new TUIContainerModule.Builder("random-number-generator")
                .children(
                        new TUITextInputModule.Builder("input","Maximum Number: ")
                                .addSafeHandler("generated-number", Main::getRandomInt),
                        new TUIModuleFactory.TextBuilder("generated-number-display")
                                .addText("Generated Number: ", false)
                                .addModuleOutputDisplay("generated-number"),
                        new TUIModuleFactory.NumberedModuleSelector("selector", app)
                                .addScene("random-number-generator")
                                .addScene(exit)
                                .listText( "Generate another number","Exit")
                );

        app.setHome(randomNumberGenerator);
        System.out.println(app);
        app.run();

    }

    public static int getRandomInt(String input) {
        int max = Integer.parseInt(input);
        return new java.util.Random().nextInt(max);
    }
}