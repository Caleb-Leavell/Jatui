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

import java.util.Objects;

import static org.fusesource.jansi.Ansi.ansi;

public class TUITextModule extends TUIModule {
    private final String text;
    private final boolean printNewLine;
    private final OutputType outputType;

    public enum OutputType {
            DISPLAY_TEXT,
            DISPLAY_MODULE_OUTPUT
    }

    @Override
    public void run() {
        logger.info("Running TUITextModule {}", getName());
        if(getAnsiEnabled()) {
            logger.debug("printing ansi for {}", getName());
            getPrintStream().print(getAnsi());
        }
        else
            logger.trace("ansi disabled for {}", getName());

        switch(outputType) {
            case DISPLAY_TEXT:
                logger.info("displaying text for \"{}\": \"{}\"", getName(), text);
                getPrintStream().print(text);
                break;
            case DISPLAY_MODULE_OUTPUT:
                if (getApplication() != null) {
                    logger.info("displaying output of module \"{}\" for \"{}\": \"{}\"", text, getName(), getApplication().getInput(text));
                    getPrintStream().print(getApplication().getInput(text));
                }
                else logger.warn("tried to display output of module \"{}\" but application was null", text);
                break;
            default:
                logger.error("TUITextModule has not implemented functionality for outputType \"{}\"", outputType);
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

        super.run();
    }

    String getText() {
        return text;
    }

    OutputType getOutputType() {return outputType;}

    boolean getPrintNewLine() {return printNewLine;}

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
     *
     * @implNote
     * This method intentionally does not override {@link Object#equals(Object)} so that things like HashMaps still check by method reference.
     *  This method is merely for checking structural equality, which is generally only necessary for manual testing.
     */
    public boolean equals(TUITextModule other) {
        if(this == other) return true;
        if(other == null) return false;

        return Objects.equals(text, other.text)
                && Objects.equals(printNewLine, other.printNewLine)
                && Objects.equals(outputType, other.outputType)
                && super.structuralEquals(other);
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

        public boolean getPrintNewLine() {
            return printNewLine;
        }

        public Builder setOutputType(OutputType type) {
            this.outputType = type;
            return self();
        }

        public OutputType getOutputType() {
            return outputType;
        }

        public Builder setText(String text) {
            this.text = text;
            return self();
        }

        public Builder append(String text) {
            this.text += text;
            return self();
        }

        public String getText() {
            return text;
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
         *
         * @implNote
         * This is the {@code Function<TUIModule<?>, TUIModule.Builder<?>, Boolean>} that is passed into {@link DirectedGraphNode#structuralEquals(DirectedGraphNode)}
         */
        public boolean shallowStructuralEquals(TUITextModule.Builder first, TUITextModule.Builder second) {
            if(first == second) return true;
            if(first == null || second == null) return false;

            return Objects.equals(first.text, second.text)
                    && Objects.equals(first.printNewLine, second.printNewLine)
                    && Objects.equals(first.outputType, second.outputType)
                    && super.shallowStructuralEquals(first, second);
        }


        @Override
        public TUITextModule build() {
            logger.trace("Building TUITextModule {}", getName());
            return new TUITextModule(self());
        }
    }
}
