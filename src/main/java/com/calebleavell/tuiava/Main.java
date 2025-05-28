package com.calebleavell.tuiava;

import com.calebleavell.tuiava.modules.*;

import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {

        var app = new TUIApplicationModule.Builder("app").build();

        var home = new TUIContainerModule.Builder("home")
                .children(
                        new TUITextModule.Builder("home-title", "Hello There!").build(),
                        new TUITextInputModule.Builder("home-input", "your input: ").build(),
                        new TUIFunctionModule.Builder("home-function", () ->
                            {
                                String input = app.getInput("home-input", String.class);
                                int num = Integer.parseInt(input);
                                return "" + (num + 1);
                            }).build()
                ).build();

        TUIContainerModule numberedList = new TUIContainerModule.Builder("numbered-list")
                .children(
                        TUIModuleFactory.Counter(app, "numbered-list-counter").build(),
                        new TUITextModule.Builder("one", "numbered-list-counter")
                                .outputType(TUITextModule.OutputType.INPUT)
                                .printNewLine(false).build(),
                        new TUITextModule.Builder("list-item-one", ". Item One").build(),
                        TUIModuleFactory.Run("numbered-list-counter", app, "run-numbered-list-counter-1").build(),
                        new TUITextModule.Builder("two", "numbered-list-counter")
                                .outputType(TUITextModule.OutputType.INPUT)
                                .printNewLine(false).build(),
                        new TUITextModule.Builder("list-item-two", ". Item Two").build(),
                        TUIModuleFactory.Run("numbered-list-counter", app, "run-numbered-list-counter-2").build()
                ).build();

        TUITextModule text1 = new TUITextModule.Builder("scn-1", "scene 1").build();
        TUITextModule text2 = new TUITextModule.Builder("scn-2", "scene 2").build();
        TUITextModule text3 = new TUITextModule.Builder("scn-3", "scene 3").build();

        TUIContainerModule scnList2 = new TUIModuleFactory.NumberedModuleSelector("scn-selector2", app,
                text1)
                .addScene("scn-2")
                .addScene(text3)
                .listText("scene 1", "scene 2", "scene 3")
                .build();

        TUIContainerModule home2 = new TUIContainerModule.Builder("home2").children(
                scnList2, TUIModuleFactory.Terminate(app, "app-terminate").build(), text1, text2, text3
        ).build();

        TUIContainerModule helloThere = new TUIModuleFactory.NumberedList("myList",
                "item1", "item2", "item3", "item4")
                .start(5)
                .step(2)
                .collectInput("Your favorite item: ", "fav-item")
                .collectInput("Your second favorite item: ", "fav-item-2")
                .children(
                        new TUIFunctionModule.Builder("display-fav-item", () -> {
                            String item = app.getInput("fav-item", String.class);
                            if(item != null) System.out.println("You chose " + item);
                            else System.out.println("input was null");
                        })
                )
                .build();

        app.setHome(home2);
        app.run();
        System.out.println(app.getInput("scn-selector2-goto-module"));
    }
}