package com.calebleavell.tuiava.modules;

import java.util.Scanner;

public class TUITextInputModule extends TUIGenericModule {
    private String input;
    private TUITextModule displayText;

    private final static Scanner scnr = new Scanner(System.in);

    @Override
    public void run() {
        displayText.run();
        input = scnr.nextLine();
        if(getApplication() != null) getApplication().updateInput(this, input);
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
        this.displayText = builder.displayText;
    }

    public static class Builder extends TUIGenericModule.Builder<Builder> {
        protected TUITextModule displayText;

        public Builder(String displayText, String name) {
            super(Builder.class, name);
            this.displayText = new TUITextModule.Builder(name + "-display-text", displayText)
                    .printNewLine(false)
                    .build();
        }

        @Override
        public TUITextInputModule build() {
            return new TUITextInputModule(self());
        }
    }
}
