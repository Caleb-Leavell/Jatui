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

import java.util.function.Supplier;

public class TUIFunctionModule extends TUIModule {

    /**
     * The logic that this module will execute when it is run.
     */
    private final Supplier<?> function;


    /**
     *
     */
    @Override
    public void run() {
        logger.info("Running TUIFunctionModule \"{}\"", getName());
        Object output = function.get();
        if(getApplication() != null)
            getApplication().updateInput(this, output);
        else if(output != null)
            logger.warn("Output \"{}\" produced by TUIFunctionModule \"{}\" but no application exists to store it", output, getName());
        else
            logger.debug("No output produced by TUIFunctionModule \"{}\" and no application exists", getName());

        super.run();
    }

    public Supplier<?> getFunction() {
        return function;
    }

    public TUIFunctionModule(Builder builder) {
        super(builder);
        this.function = builder.function;
    }

    public static class Builder extends TUIModule.Builder<Builder> {
        Supplier<?> function; // this isn't checked in a .equalTo so structural equality can actually return true

        public Builder(String name, Supplier<?> function) {
            super(Builder.class, name);
            this.function = function;
        }

        public Builder(String name, Runnable function) {
            super(Builder.class, name);
            this.setFunction(function);
        }

        public Supplier<?> getFunction() {
            return function;
        }

        public Builder setFunction(Supplier<?> function) {
            logger.debug("setting function for TUIFUnctionModule builder \"{}\"", getName());
            this.function = function;
            return self();
        }

        public Builder setFunction(Runnable function) {
            logger.debug("setting function for TUIFunctionModule builder \"{}\" based on a Runnable", getName());
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

        @Override
        public void shallowCopy(Builder original) {
            this.function = original.function;
            super.shallowCopy(original);
        }

        @Override
        public TUIFunctionModule build() {
            logger.trace("Building TUIFunctionModule \"{}\"", getName());
            return new TUIFunctionModule(self());
        }
    }

}
