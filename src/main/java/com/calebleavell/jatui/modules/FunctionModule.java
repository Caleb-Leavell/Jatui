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

import com.calebleavell.jatui.core.DirectedGraphNode;

import java.util.function.Supplier;

/**
 * Handles arbitrary logic execution for the application by executing a {@link Supplier} when the module is run.
 * If tied to an {@link ApplicationModule}, whatever the supplier returns is updated in the app's inputMap and can
 * be accessed via {@link ApplicationModule#getInput(String)}.
 */
public class FunctionModule extends TUIModule {

    /**
     * The logic that this module will execute when it is run.
     */
    private final Supplier<?> function;


    /**
     *  Runs the stored function, attempts to update the application input
     *  based on what the function returned, then runs children as
     *  provided by {@link TUIModule#start()}
     */
    @Override
    public void doRunLogic() {
        logger.info("Running FunctionModule \"{}\"", getName());
        Object output = function.get();
        if(getApplication() != null)
            getApplication().updateInput(this, output);
        else if(output != null)
            logger.warn("Output \"{}\" produced by FunctionModule \"{}\" but no application exists to store it", output, getName());
        else
            logger.debug("No output produced by FunctionModule \"{}\" and no application exists", getName());
    }

    /**
     * The function is the {@link Supplier} logic that this module will execute when it is run.
     * @return The function stored by this module.
     */
    public Supplier<?> getFunction() {
        return function;
    }


    /**
     * Builds a {@link FunctionModule} based on the state of {@code builder}
     * @param builder The {@link FunctionModule.Builder} that is building the module.
     */
    public FunctionModule(Builder builder) {
        super(builder);
        this.function = builder.function;
    }

    /**
     * Constructs a new {@link FunctionModule} builder.
     *
     * @param name The name of the builder.
     * @param function The Supplier that will be executed when this module is built and run.
     * @return The new builder.
     */
    public static Builder builder(String name, Supplier<?> function) {
        return new Builder(name, function);
    }

    /**
     * Constructs a new {@link FunctionModule} builder.
     *
     * @param name The name of the builder.
     * @param function The Runnable that will be executed when this module is built and run.
     * @return The new builder.
     */
    public static Builder builder(String name, Runnable function) {
        return new Builder(name, function);
    }

    /**
     * Builder for {@link FunctionModule}.
     * <br><br>
     * Required fields: {@code name}, {@code function}
     */
    public static class Builder extends TUIModule.Builder<Builder> {

        /**
         * The logic that this module will execute when it is run.
         *
         * @implNote
         * This isn't checked in any equivalent to {@link DirectedGraphNode#structuralEquals(DirectedGraphNode)}
         * in order to allow the structural equality check to return true (since separately declared lambdas and method
         * references return false on {@link Object#equals}).
         */
        Supplier<?> function;

        /**
         * Constructs a builder based on a provided name and Supplier
         * @param name The unique name of the module
         * @param function The Supplier that will be executed when this module is built and run.
         */
        protected Builder(String name, Supplier<?> function) {
            super(Builder.class, name);
            this.function = function;
        }

        /**
         * Constructs a builder based on a provided name and Runnable.
         * Note that the Runnable will get wrapped in a Supplier that returns {@code null}
         * @param name The unique name of the module
         * @param function The Runnable that will be executed when this module is built and run.
         */
        protected Builder(String name, Runnable function) {
            super(Builder.class, name);
            this.function(function);
        }

        /**
         * The function is the {@link Supplier} logic that this module will execute when it is run.
         * @return The function stored by this module.
         */
        public Supplier<?> getFunction() {
            return function;
        }

        /**
         * The function is the {@link Supplier} logic that this module will execute when it is run.
         * @param function The Supplier to be run when this module is run
         * @return self
         */
        public Builder function(Supplier<?> function) {
            logger.debug("setting function for FunctionModule builder \"{}\"", getName());
            this.function = function;
            return self();
        }

        /**
         * The function is the {@link Supplier} logic that this module will execute when it is run.
         * @param function The Runnable to be run when this module is run (this will be wrapped
         *                 in a Supplier that returns null.)
         * @return self
         */
        public Builder function(Runnable function) {
            logger.debug("setting function for FunctionModule builder \"{}\" based on a Runnable", getName());
            if(function == null) this.function = null;
            else {
                this.function = () -> {
                    function.run();
                    return null;
                };
            }

            return self();
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
         * Creates a copy of {@code original} by mutating this instance.
         * Children are not copied.
         * This is a utility method for {@link TUIModule.Builder#getCopy()}
         * @param original The builder to copy from
         */
        @Override
        public void shallowCopy(Builder original) {
            this.function = original.function;
            super.shallowCopy(original);
        }

        /**
         * Builds a new {@link FunctionModule} based on this builder.
         * @return The built {@link FunctionModule}
         */
        @Override
        public FunctionModule build() {
            logger.trace("Building FunctionModule \"{}\"", getName());
            return new FunctionModule(self());
        }
    }

}
