package com.calebleavell.jatui.modules;

/**
 * Not only does this class improve modularity by housing other modules,
 * but it also provides a template for extending TUIGenericModule.
 */
public class TUIContainerModule extends TUIGenericModule {
    public TUIContainerModule(TUIGenericModule.Builder<?> builder) {
        super(builder);
    }

    public static class Builder extends TUIGenericModule.Builder<Builder> {
        public Builder(String name) {
            super(Builder.class, name);
        }

        @Override
        public TUIContainerModule build() {
            return new TUIContainerModule(self());
        }
    }
}
