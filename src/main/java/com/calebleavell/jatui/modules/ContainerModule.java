/*
    Copyright (c) 2026 Caleb Leavell

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
 */

package com.calebleavell.jatui.modules;

/**
 * Not only does this class improve modularity by housing other modules,
 * but it also provides a template for extending TUIGenericModule.
 */
public class ContainerModule extends TUIModule {

    /**
     * Simply logs the run and calls the super-method provided by {@link TUIModule#start()}.
     */
    @Override
    public void doRunLogic() {
        logger.info("Running ContainerModule \"{}\"", getName());
    }
    /**
     * Builds a ContainerModule based on the state of {@code builder}
     * @param builder The {@link ContainerModule.Builder} that is building the application module.
     */
    public ContainerModule(TUIModule.Builder<?> builder) {
        super(builder);
    }

    /**
     * Constructs a new {@link ContainerModule} builder.
     *
     * @param name The name of the builder.
     * @return The new builder.
     */
    public static Builder builder(String name) {
        return new Builder(name);
    }

    /** Builder for {@link ContainerModule}
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
         * Builds a new {@link ApplicationModule}.
         * @return The built ApplicationModule
         */
        @Override
        public ContainerModule build() {
            logger.trace("Building ContainerModule \"{}\"", getName());
            return new ContainerModule(self());
        }
    }
}
