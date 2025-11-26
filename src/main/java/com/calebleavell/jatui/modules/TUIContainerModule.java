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

/**
 * Not only does this class improve modularity by housing other modules,
 * but it also provides a template for extending TUIGenericModule.
 */
public class TUIContainerModule extends TUIModule {

    /**
     * Simply logs the run and calls the super-method provided by {@link TUIModule#run}.
     */
    @Override
    public void run() {
        logger.info("Running TUIContainerModule \"{}\"", getName());
        super.run();
    }
    /**
     * Builds a TUIContainerModule based on the state of {@code builder}
     * @param builder The {@link TUIContainerModule.Builder} that is building the application module.
     */
    public TUIContainerModule(TUIModule.Builder<?> builder) {
        super(builder);
    }

    /** Builder for {@link TUIContainerModule}
     * <br><br>
     *  Required Fields: {@code name}
     * **/
    public static class Builder extends TUIModule.Builder<Builder> {
        public Builder(String name) {
            super(Builder.class, name);
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
         * Builds a new {@link TUIApplicationModule}.
         * @return The built TUIApplicationModule
         */
        @Override
        public TUIContainerModule build() {
            logger.trace("Building TUIContainerModule \"{}\"", getName());
            return new TUIContainerModule(self());
        }
    }
}
