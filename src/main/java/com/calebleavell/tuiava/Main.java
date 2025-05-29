package com.calebleavell.tuiava;

import com.calebleavell.tuiava.modules.*;

import java.util.List;

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
                TUITextModule.Builder row = new TUITextModule.Builder(name + y, "");

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

        var home = new TUIContainerModule.Builder("home")
                .children(
                        new TUITextModule.Builder("home-title", "Hello There!"),
                        new TUITextInputModule.Builder("home-input", "your input: "),
                        new TUIFunctionModule.Builder("home-function", () ->
                            {
                                String input = app.getInput("home-input", String.class);
                                int num = Integer.parseInt(input);
                                return "" + (num + 1);
                            }),
                        new TUITextModule.Builder("display-home-function", "home-function").outputType(TUITextModule.OutputType.INPUT)
                );

        TUIContainerModule.Builder numberedList = new TUIContainerModule.Builder("numbered-list")
                .children(
                        TUIModuleFactory.Counter(app, "numbered-list-counter"),
                        new TUITextModule.Builder("one", "numbered-list-counter")
                                .outputType(TUITextModule.OutputType.INPUT)
                                .printNewLine(false),
                        new TUITextModule.Builder("list-item-one", ". Item One"),
                        TUIModuleFactory.Run("numbered-list-counter", app, "run-numbered-list-counter-1"),
                        new TUITextModule.Builder("two", "numbered-list-counter")
                                .outputType(TUITextModule.OutputType.INPUT)
                                .printNewLine(false),
                        new TUITextModule.Builder("list-item-two", ". Item Two"),
                        TUIModuleFactory.Run("numbered-list-counter", app, "run-numbered-list-counter-2")
                );

        TUITextModule.Builder text1 = new TUITextModule.Builder("scn-1", "scene 1");
        TUITextModule.Builder text2 = new TUITextModule.Builder("scn-2", "scene 2");
        TUITextModule.Builder text3 = new TUITextModule.Builder("scn-3", "scene 3");

        TUIModuleFactory.NumberedModuleSelector scnList2 = new TUIModuleFactory.NumberedModuleSelector("scn-selector2", app,
                text1)
                .addScene("scn-2")
                .addScene(text3)
                .listText("scene 1", "scene 2", "scene 3")
                .children(TUIModuleFactory.Terminate(app, "terminate"), text2);

        TUIContainerModule.Builder home2 = new TUIContainerModule.Builder("home2").children(
                scnList2, TUIModuleFactory.Terminate(app, "app-terminate"), text1, text2, text3
        );

        TUIModuleFactory.NumberedList helloThere = new TUIModuleFactory.NumberedList("my-list",
                "item1", "item2", "item3", "item4")
                .start(5)
                .step(2)
                .collectInput("fav-item", "Your favorite item: ")
                .collectInput("fav-item-2", "Your second favorite item: ")
                .children(
                        new TUIFunctionModule.Builder("display-fav-item", () -> {
                            String item = app.getInput("fav-item", String.class);
                            if(item != null) System.out.println("You chose " + item);
                            else System.out.println("input was null");
                        })
                );

        TUIContainerModule.Builder rectBuilder = new TUIContainerModule.Builder("rect-builder")
                .children(
                        new TUITextInputModule.Builder("rect-builder-input", "Size of Rect (integer): ")
                                .inputConverter(Integer::parseInt)
                                .inputVerifier("Error: input must be an integer"),
                        new TUIFunctionModule.Builder("rect-builder-function", () -> {
                            Integer size = app.getInput("rect-builder-input", Integer.class);
                            if(size == null) {
                                System.out.println("Error: input was not converted to integer");
                                return;
                            }
                            Rect rect = app.getChild("rect", Rect.class);
                            rect.x = size;
                            rect.y = size;
                        }),
                        new Rect("rect", 0, 0).cell("#")
                );

        app.setHome(rectBuilder);
        app.run();
    }
}