package com.calebleavell.jatui.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Function;

public class TUITextInputModule extends TUIGenericModule {
    private String input;
    private TUITextModule displayText;

    private final static Scanner scnr = new Scanner(System.in);

    public final static String INVALID = "Error: input was invalid";

    @Override
    public void run() {
        displayText.run();

        input = scnr.nextLine();

        TUIApplicationModule app = getApplication();
        if(app != null) app.updateInput(this, input);

        super.run();
    }

    public String getInput() {
        return input;
    }

    public TUITextModule getDisplayText() {
        return displayText;
    }

    public TUITextInputModule(Builder builder) {
        super(builder);
        this.displayText = builder.displayText.build();
    }

    public static class Builder extends TUIGenericModule.Builder<Builder> {
        protected TUITextModule.Builder displayText;
        protected InputHandlers handlers;

        public Builder(String name, String displayText) {
            super(Builder.class, name);
            this.displayText = new TUITextModule.Builder(name + "-display-text", displayText)
                    .printNewLine(false);
            handlers = new InputHandlers("handlers", this);
        }

        public Builder addHandler(TUIFunctionModule.Builder handler) {
            handlers.addHandler(handler);
            return self();
        }

        public Builder addHandler(String name, Function<String, ?> logic) {
            handlers.addHandler(name, logic);
            return self();
        }

        public <T> Builder addSafeHandler(String name, Function<String, T> logic, Consumer<String> exceptionHandler) {
            handlers.addSafeHandler(name, logic, exceptionHandler);
            return self();
        }

        public Builder addSafeHandler(String name, Function<String, ?> logic, String exceptionMessage) {
            handlers.addSafeHandler(name, logic, o -> {
                TUIApplicationModule app = this.getApplication();
                if(app == null) return;
                System.out.println(exceptionMessage);
                app.terminateChild(this.name);

                this.build().run();
            });

            return self();
        }

        public Builder addSafeHandler(String name, Function<String, ?> logic) {
            this.addSafeHandler(name, logic, "Error: Invalid Input");
            return self();
        }

        @Override
        public TUITextInputModule build() {
            if(children.isEmpty() || children.getFirst() != handlers) this.addChild(0, handlers);
            this.application(application);
            return new TUITextInputModule(self());
        }
    }

    protected static class InputHandlers extends TUIModule.Template<InputHandlers> {

        protected List<TUIFunctionModule.Builder> handlers = new ArrayList<>();
        protected Builder inputModule;

        public InputHandlers(String name, Builder inputModule) {
            super(InputHandlers.class, name);
            this.inputModule = inputModule;
        }

        public InputHandlers addHandler(TUIFunctionModule.Builder handler) {
            handlers.add(handler);
            return self();
        }

        public <T> InputHandlers addHandler(String name, Function<String, T> logic) {
            handlers.add(new TUIFunctionModule.Builder(name, () -> {
                TUIApplicationModule app = this.getApplication();
                if(app == null) return null;
                String input = app.getInput(inputModule.getName(), String.class);
                T converted = logic.apply(input);
                app.updateInput(name, converted);
                return converted;
            }));
            return self();
        }

        public <T> InputHandlers addSafeHandler(String name, Function<String, T> logic, Consumer<String> exceptionHandler) {
            handlers.add(new TUIFunctionModule.Builder(name, () -> {
                TUIApplicationModule app = this.getApplication();
                if(app == null) return null;
                String input = app.getInput(inputModule.getName(), String.class);
                T converted;
                try {
                    converted = logic.apply(input);
                }
                catch(Exception e) {
                    exceptionHandler.accept(input);
                    return app.getInput(name);
                }
                app.updateInput(name, converted);
                return converted;
            }));
            return self();
        }

        @Override
        public TUIContainerModule build() {
            handlers.forEach(h -> main.addChild(h)); //method is ambiguous so we can't do a method reference
            return super.build();
        }
    }
}
