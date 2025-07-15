package com.calebleavell.jatui.modules;

import static org.fusesource.jansi.Ansi.ansi;

public class TUITextModule extends TUIGenericModule {
    private String text;
    private final boolean printNewLine;
    private final OutputType outputType;
    private final boolean enableAnsi;

    public enum OutputType {
            DISPLAY_TEXT,
            DISPLAY_MODULE_OUTPUT
    }

    @Override
    public void run() {

        if(enableAnsi)
            getPrintStream().print(getAnsi());

        switch(outputType) {
            case DISPLAY_TEXT:
                getPrintStream().print(text);
                break;
            case DISPLAY_MODULE_OUTPUT:
                if (getApplication() != null)
                    getPrintStream().print(getApplication().getInput(text));
                break;
            default:
                getPrintStream().println("ERROR: TUITextModule has not implemented functionality for outputType " + outputType);
                break;
        }

        if(enableAnsi)
            getPrintStream().print(ansi().reset());

        if(printNewLine) getPrintStream().println();
        super.run();
    }

    String getText() {
        return text;
    }

    void setText(String text) {
        this.text = text;
    }

    public TUITextModule(Builder builder) {
        super(builder);
        this.text = builder.text;
        this.printNewLine = builder.printNewLine;
        this.outputType = builder.outputType;
        this.enableAnsi = builder.enableAnsi;
    }

    public static class Builder extends TUIGenericModule.Builder<Builder> {
        protected String text;
        protected boolean printNewLine = true;
        protected OutputType outputType = OutputType.DISPLAY_TEXT;

        public Builder(String name, String text) {
            super(Builder.class, name);
            this.text = text;
        }

        protected Builder(Builder original) {
            super(original);
            this.text = original.text;
            this.printNewLine = original.printNewLine;
            this.outputType = original.outputType;
        }

        @Override
        public Builder getCopy() {
            return new Builder(this);
        }

        public Builder printNewLine(boolean printNewLine) {
            this.printNewLine = printNewLine;
            return self();
        }

        public Builder outputType(OutputType type) {
            this.outputType = type;
            return self();
        }

        public Builder text(String text) {
            this.text = text;
            return self();
        }

        public Builder append(String text) {
            this.text += text;
            return self();
        }

        @Override
        public TUITextModule build() {
            return new TUITextModule(self());
        }
    }
}
