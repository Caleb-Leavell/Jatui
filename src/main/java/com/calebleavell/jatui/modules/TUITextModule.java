package com.calebleavell.jatui.modules;

public class TUITextModule extends TUIGenericModule {
    private String text;
    private final boolean printNewLine;
    private final OutputType outputType;

    public enum OutputType {
            TEXT,
            OUTPUT_OF_MODULE_NAME
    }

    @Override
    public void run() {
        switch(outputType) {
            case TEXT:
                System.out.print(text);
                break;
            case OUTPUT_OF_MODULE_NAME:
                if (getApplication() != null)
                    System.out.print(getApplication().getInput(text));
                break;
            default:
                System.out.println("ERROR: TUITextModule has not implemented functionality for outputType " + outputType);
                break;
        }
        if(printNewLine) System.out.println();
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
    }

    public static class Builder extends TUIGenericModule.Builder<Builder> {
        protected String text;
        protected boolean printNewLine = true;
        protected OutputType outputType = OutputType.TEXT;

        public Builder(String name, String text) {
            super(Builder.class, name);
            this.text = text;
        }

        public Builder printNewLine(boolean printNewLine) {
            this.printNewLine = printNewLine;
            return self();
        }

        public Builder outputType(OutputType type) {
            this.outputType = type;
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
