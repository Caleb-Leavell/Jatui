/*
 * Copyright (c) 2025 Caleb Leavell
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.calebleavell.jatui.modules;

import org.fusesource.jansi.Ansi;

import java.io.PrintStream;
import java.util.Objects;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * Handles the displaying of text. Usually this means displaying to console ({@link System#out}), but
 * {@link TUIModule.Builder#printStream(PrintStream)} can be used to output to other places as well.
 */
public class TextModule extends TUIModule {

    /** The text to either display directly or the input identifier fetch the input from for the {@link ApplicationModule}. **/
    private final String text;

    /** Whether to print a new line after the text is outputted. **/
    private final boolean printNewLine;

    /** The {@link OutputType} of the text to display, which includes pure output or fetching application state. **/
    private final OutputType outputType;

    /**
     * Defines the behavior of the {@link TextModule}, specifically whether
     * it displays raw text or fetches application state.
     */
    public enum OutputType {
        /** Displays the text provided by {@link TextModule.Builder#text(String)} **/
        DISPLAY_TEXT,
        /** Displays the application state, where the input identifier is provided by {@link TextModule.Builder#text(String)} **/
        DISPLAY_APP_STATE
    }

    /**
     * Outputs text via the behavior given by {@link OutputType}.
     * If {@code application} is null and {@link OutputType} is
     * {@link OutputType#DISPLAY_APP_STATE}, nothing is outputted and a warning
     * is logged.
     * <br>
     * Also displays the ansi provided by {@link TUIModule.Builder#style(Ansi)} unless
     * disabled via {@link TUIModule.Builder#enableAnsi(boolean)}.
     * The ansi gets reset at the end of the run.
     * <br>
     * Prints a new line unless disabled via {@link TextModule.Builder#printNewLine(boolean)}.
     */
    @Override
    public void doRunLogic() {
        logger.info("Running TextModule {}", getName());
        if(getAnsiEnabled()) {
            logger.debug("printing ansi for {}", getName());
            getPrintStream().print(getAnsi());
        }
        else
            logger.trace("ansi disabled for {}", getName());

        switch(outputType) {
            case DISPLAY_TEXT:
                logger.debug("displaying text for \"{}\": \"{}\"", getName(), text);
                getPrintStream().print(text);
                break;
            case DISPLAY_APP_STATE:
                if (getApplication() != null) {
                    logger.debug("displaying output of module \"{}\" for \"{}\": \"{}\"", text, getName(), getApplication().getInput(text));
                    getPrintStream().print(getApplication().getInput(text));
                }
                else logger.warn("tried to display output of module \"{}\" but application was null", text);
                break;
            default:
                logger.error("TextModule has not implemented functionality for outputType \"{}\"", outputType);
                break;
        }

        if(getAnsiEnabled()) {
            logger.trace("resetting ansi for {}", getName());
            getPrintStream().print(ansi().reset());
        }

        if(printNewLine) {
            logger.trace("newline for {}", getName());
            getPrintStream().println();
        }
    }

    /**
     * Get the text that defines how this module outputs.
     * @return text
     */
    String getText() {
        return text;
    }

    /**
     * Get the {@link OutputType} that defines the behavior of this module.
     * @return the output type of this module.
     */
    OutputType getOutputType() {return outputType;}

    /**
     * If {@code printNewLine} is true, this module prints a new line after outputting.
     * @return printNewLine
     */
    boolean getPrintNewLine() {return printNewLine;}

    /**
     * Checks equality for properties given by the builder. For {@link TextModule}, this includes
     * {@code text}, {@code printNewLine}, and {@code outputType},
     * as well as other requirements provided by {@link TUIModule#structuralEquals(TUIModule)}.
     */
    public boolean equals(TextModule other) {
        if(this == other) return true;
        if(other == null) return false;

        return Objects.equals(text, other.text)
                && Objects.equals(printNewLine, other.printNewLine)
                && Objects.equals(outputType, other.outputType)
                && super.structuralEquals(other);
    }

    /**
     * Constructs a new {@link TextModule} based on the configuration of the {@link TextModule.Builder}.
     * Copies {@code text}, {@code printNewLine}, and {@code outputType} from the builder.
     *
     * @param builder The builder to construct the new module from.
     */
    public TextModule(Builder builder) {
        super(builder);
        this.text = builder.text;
        this.printNewLine = builder.printNewLine;
        this.outputType = builder.outputType;
    }

    /**
     * Constructs a new {@link TextModule} builder.
     *
     * @param name The name of the builder.
     * @param text The text to either display directly,
     *             or the input identifier fetch the input from for the {@link ApplicationModule} if {@link Builder#outputType(OutputType)}
     *             is set to {@link TextModule.OutputType#DISPLAY_APP_STATE}.
     * @return The new builder.
     */
    public static Builder builder(String name, String text) {
        return new Builder(name, text);
    }


    /**
     * Builder for {@link TextInputModule}.
     * <br><br>
     * Required fields: {@code name}, {@code text} <br>
     * Optional fields (with default values): {@code printNewLine}, {@code outputType}
     **/
    public static class Builder extends TUIModule.Builder<Builder> {
        /** The text to either display directly or the input identifier fetch the input from for the {@link ApplicationModule}. **/
        protected String text;

        /** Whether to print a new line after the text is outputted. **/
        protected boolean printNewLine = true;

        /** The {@link OutputType} of the text to display, which includes pure output or fetching application state. **/
        protected OutputType outputType = OutputType.DISPLAY_TEXT;

        protected Builder(String name, String text) {
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


        /**
         * Copies {@code text}, {@code printNewLine}, and {@code outputType},
         * and delegates to {@link TUIModule.Builder#shallowCopy(TUIModule.Builder)}.
         * @param original The builder to copy from.
         */
        @Override
        public void shallowCopy(Builder original) {
            this.text = original.text;
            this.printNewLine = original.printNewLine;
            this.outputType = original.outputType;
            super.shallowCopy(original);
        }

        /**
         * If true, this module prints a new line after outputting.
         * @param printNewLine Whether to print a new line after outputting.
         * @return self
         */
        public Builder printNewLine(boolean printNewLine) {
            this.printNewLine = printNewLine;
            return self();
        }

        /**
         * If {@code printNewLine} is true, this module prints a new line after outputting.
         * @return printNewLine
         */
        public boolean getPrintNewLine() {
            return printNewLine;
        }

        /**
         * Set the {@link OutputType} that defines the behavior of this module.
         * @param type the output type of this module.
         * @return self
         */
        public Builder outputType(OutputType type) {
            this.outputType = type;
            return self();
        }

        /**
         * Get the {@link OutputType} that defines the behavior of this module.
         * @return the output type of this module.
         */
        public OutputType getOutputType() {
            return outputType;
        }

        /**
         * Set the text that defines how this module outputs.
         * @param text What to output.
         * @return self.
         */
        public Builder text(String text) {
            this.text = text;
            return self();
        }

        /**
         * Append to the text that's already been configured for this builder.
         * @param text What to append to the existing text.
         * @return self
         */
        public Builder append(String text) {
            this.text += text;
            return self();
        }

        /**
         * Get the text that defines how this module outputs.
         * @return What to output.
         */
        public String getText() {
            return text;
        }

        /**
         * Checks equality for properties given by the builder. For {@link TextModule}, this includes
         * {@code text}, {@code printNewLine}, and {@code outputType},
         * as well as other requirements provided by {@link TUIModule#structuralEquals(TUIModule)}.
         */
        public boolean shallowStructuralEquals(TextModule.Builder first, TextModule.Builder second) {
            if(first == second) return true;
            if(first == null || second == null) return false;

            return Objects.equals(first.text, second.text)
                    && Objects.equals(first.printNewLine, second.printNewLine)
                    && Objects.equals(first.outputType, second.outputType)
                    && super.shallowStructuralEquals(first, second);
        }

        /**
         * Builds a new {@link TextModule} based on the configuration of this builder.
         * @return The new {@link TextModule}.
         */
        @Override
        public TextModule build() {
            logger.trace("Building TextModule {}", getName());
            return new TextModule(self());
        }
    }
}
