package com.calebleavell.jatui;

import com.calebleavell.jatui.modules.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
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

    public static void main(String[] args) {

        var app = new TUIApplicationModule.Builder("app").build();

        var exit = new TUITextModule.Builder("exit", "Exiting...");

        var randomNumberGenerator = new TUIContainerModule.Builder("random-number-generator")
                .alterChildNames(true)
                .children(
                        new TUITextInputModule.Builder("input", "Maximum Number (or -1 to quit): ")
                                .inputConverter(s -> {
                                    int max = Integer.parseInt(s);

                                    if(max < 0) {
                                        app.terminate();
                                        exit.build().run();
                                        return null;
                                    }

                                    int generated = new java.util.Random().nextInt(max);

                                    return Integer.toString(generated);
                                }),
                        new TUITextModule.Builder("generated-number-label", "Generated Number: ").printNewLine(false),
                        new TUITextModule.Builder("generated-number-display", "random-number-generator-input")
                                .outputType(TUITextModule.OutputType.OUTPUT_OF_MODULE_NAME),
                        new TUIModuleFactory.NumberedModuleSelector("selector", app)
                                .addScene("random-number-generator")
                                .addScene(exit)
                                .listText("Generate another number", "Exit")
                );

        app.setHome(randomNumberGenerator);
        app.run();
    }
}