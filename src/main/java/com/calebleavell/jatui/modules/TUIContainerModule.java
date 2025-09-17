package com.calebleavell.jatui.modules;

/**
 * Not only does this class improve modularity by housing other modules,
 * but it also provides a template for extending TUIGenericModule.
 */
public class TUIContainerModule extends TUIModule {

    @Override
    public void run() {
        logger.info("Running TUIContainerModule \"{}\"", getName());
        super.run();
    }

    public TUIContainerModule(TUIModule.Builder<?> builder) {
        super(builder);
    }

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

        @Override
        public TUIContainerModule build() {
            logger.trace("Building TUIContainerModule \"{}\"", getName());
            return new TUIContainerModule(self());
        }
    }
}
