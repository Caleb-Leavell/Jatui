package com.calebleavell.jatui.modules;

import java.util.Objects;

import static org.fusesource.jansi.Ansi.ansi;

public class TUITextModule extends TUIModule {
    private String text;
    private final boolean printNewLine;
    private final OutputType outputType;

    public enum OutputType {
            DISPLAY_TEXT,
            DISPLAY_MODULE_OUTPUT
    }

    @Override
    public void run() {
        this.terminated = false;
        if(getAnsiEnabled())
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

        if(getAnsiEnabled())
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

    /**
     * <p>Checks equality for properties given by the builder.</p>
     *
     * <p>For TUITextModule, this includes: </p>
     * <ul>
     *     <li><strong>text</strong></li>
     *     <li><strong>printNewLine</strong></li>
     *     <li><strong>outputType</strong></li>
     *     <li>name</li>
     *     <li>application</li>
     *     <li>ansi</li>
     *     <li>scanner</li>
     *     <li>printStream</li>
     *     <li>enableAnsi</li>
     * </ul>
     * @param other The TUITextModule to compare
     * @return true if this module equals {@code other} according to builder-provided properties
     * @implNote This method intentionally does not override {@link Object#equals(Object)} so that things like HashMaps still check by method reference.
     *  This method is merely for checking structural equality, which is generally only necessary for manual testing.
     */
    public boolean equals(TUITextModule other) {
        if(this == other) return true;
        if(other == null) return false;

        return Objects.equals(text, other.text)
                && Objects.equals(printNewLine, other.printNewLine)
                && Objects.equals(outputType, other.outputType)
                && super.equals(other);
    }

    public TUITextModule(Builder builder) {
        super(builder);
        this.text = builder.text;
        this.printNewLine = builder.printNewLine;
        this.outputType = builder.outputType;
    }

    public static class Builder extends TUIModule.Builder<Builder> {
        protected String text;
        protected boolean printNewLine = true;
        protected OutputType outputType = OutputType.DISPLAY_TEXT;

        public Builder(String name, String text) {
            super(Builder.class, name);
            this.text = text;
        }

        protected Builder() {
            super(Builder.class);
        }

        /**
         * Gets a fresh instance of this type of Builder.
         *  Note, this is intended only for copying utility and may have unknown consequences if used in other ways.
         * @return A fresh, empty instance.
         */
        @Override
        protected Builder createInstance() {
            return new Builder();
        }


        @Override
        public void shallowCopy(Builder original) {
            this.text = original.text;
            this.printNewLine = original.printNewLine;
            this.outputType = original.outputType;
            super.shallowCopy(original);
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

        /**
         * <p>Checks equality for properties given by the builder.</p>
         *
         * <p>For TUITextModule.Builder, this includes: </p>
         * <ul>
         *     <li><strong>text</strong></li>
         *     <li><strong>printNewLine</strong></li>
         *     <li><strong>outputType</strong></li>
         *     <li>name</li>
         *     <li>application</li>
         *     <li>children</li>
         *     <li>ansi</li>
         *     <li>scanner</li>
         *     <li>printStream</li>
         *     <li>enableAnsi</li>
         * </ul>
         * <p>Note: Runtime properties (e.g., currentRunningChild, terminated), are not considered. Children are also not considered here,
         *  but are considered in equals().
         * @param first The first TUITextModule.Builder to compare
         * @param second The second TUITextModule.Builder to compare
         * @return {@code true} if {@code first} and {@code second} are equal according to builder-provided properties
         * @implNote This is the {@code Function<TUIModule<?>, TUIModule.Builder<?>, Boolean>} that is passed into {@link DirectedGraphNode#equals(DirectedGraphNode)}
         */
        public boolean equalTo(TUITextModule.Builder first, TUITextModule.Builder second) {
            if(first == second) return true;
            if(first == null || second == null) return false;

            return Objects.equals(first.text, second.text)
                    && Objects.equals(first.printNewLine, second.printNewLine)
                    && Objects.equals(first.outputType, second.outputType)
                    && super.equalTo(first, second);
        }


        @Override
        public TUITextModule build() {
            return new TUITextModule(self());
        }
    }
}
