package com.calebleavell.tuiava.modules;

import java.util.List;
import java.util.Scanner;
import java.util.function.Function;

public class TUITextInputModule extends TUIGenericModule {
    private String input;
    private TUITextModule displayText;
    private TUIFunctionModule inputConverter;
    private TUIFunctionModule inputVerifier;

    private final static Scanner scnr = new Scanner(System.in);

    public final static String INVALID = "Error: input was invalid";

    @Override
    public void run() {
        displayText.run();
        input = scnr.nextLine();

        TUIApplicationModule app = getApplication();
        if(app != null) {
            app.updateInput(this, input);
            inputConverter.run();
            app.updateInput(inputConverter.getName(), app.getInput(inputConverter.getName()));
            inputVerifier.run();
            app.updateInput(this, app.getInput(inputConverter.getName()));
        }
        super.run();
    }

    public String getInput() {
        return input;
    }

    public TUITextModule getDisplayText() {
        return displayText;
    }

    public void setDisplayText(String text) {
        this.displayText.setText(text);
    }

    public void setDisplayText(TUITextModule text) {
        this.displayText = text;
    }

    public TUITextInputModule(Builder builder) {
        super(builder);
        this.displayText = builder.displayText.build();
        this.inputConverter = builder.inputConverter.build();
        this.inputVerifier = builder.inputVerifier.build();
    }

    public static class Builder extends TUIGenericModule.Builder<Builder> {
        protected TUITextModule.Builder displayText;
        protected TUIFunctionModule.Builder inputConverter;
        protected TUIFunctionModule.Builder inputVerifier;

        public Builder(String name, String displayText) {
            super(Builder.class, name);
            this.displayText = new TUITextModule.Builder(name + "-display-text", displayText)
                    .printNewLine(false);

            inputConverter = new TUIFunctionModule.Builder(name + "-converter", () -> {
                TUIApplicationModule app = this.getApplication();
                if(app != null) return app.getInput(name);
                else return null;
            });

            inputVerifier = new TUIFunctionModule.Builder(name + "-verifier", () -> {
                TUIApplicationModule app = this.getApplication();
                if(app == null) return;
                Object converted = app.getInput(inputConverter.getName());
                if(TUITextInputModule.INVALID.equals(converted)) {
                    System.out.println("Error: Invalid Input");
                    app.terminateChild(name);
                    this.build().run();
                }
            });
        }

        public Builder inputConverter(Function<String, ?> converter) {
            inputConverter.function(() -> {
                TUIApplicationModule app = this.getApplication();
                if(app == null) return null;
                String input = app.getInput(name, String.class);
                try {
                    Object converted = converter.apply(input);
                    app.updateInput(inputConverter.build(), converted);
                    return converted;
                }
                catch(Exception e) {
                    return TUITextInputModule.INVALID;
                }
            });
            return self();
        }

        public Builder inputVerifier(Runnable onInvalidInput, Runnable onValidInput) {
            inputVerifier.function(() -> {
                TUIApplicationModule app = this.getApplication();
                if(app == null) return;
                Object converted = app.getInput(inputConverter.getName());
                if(TUITextInputModule.INVALID.equals(converted)) {
                    onInvalidInput.run();
                }
                else {
                    onValidInput.run();
                }
            });
            return self();
        }

        public Builder inputVerifier(Runnable onInvalidInput) {
            inputVerifier.function(() -> {
                TUIApplicationModule app = this.getApplication();
                if(app == null) return;
                Object converted = app.getInput(inputConverter.getName());
                if(TUITextInputModule.INVALID.equals(converted)) {
                    onInvalidInput.run();
                }
            });
            return self();
        }

        public Builder inputVerifier(String invalidInputMessage) {
            inputVerifier = new TUIFunctionModule.Builder(name + "-verifier", () -> {
                TUIApplicationModule app = this.getApplication();
                if(app == null) return;
                Object converted = app.getInput(inputConverter.getName());
                if(TUITextInputModule.INVALID.equals(converted)) {
                    System.out.println(invalidInputMessage);
                    app.terminateChild(name);
                    this.build().run();
                }
            });

            return self();
        }

        @Override
        public List<TUIModule.Builder<?>> applicationHelper(TUIApplicationModule application, List<TUIModule.Builder<?>> visited) {
            super.applicationHelper(application, visited);

            displayText.application(application);
            inputConverter.application(application);
            inputVerifier.application(application);

            return super.applicationHelper(application, visited);
        }

        @Override
        public TUITextInputModule build() {
            return new TUITextInputModule(self());
        }
    }
}
