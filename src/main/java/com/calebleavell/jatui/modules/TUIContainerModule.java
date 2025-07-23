package com.calebleavell.jatui.modules;

/**
 * Not only does this class improve modularity by housing other modules,
 * but it also provides a template for extending TUIGenericModule.
 */
public class TUIContainerModule extends TUIModule {

    @Override
    public void run() {
        this.terminated = false;
        super.run();
    }
    
    public TUIContainerModule(TUIModule.Builder<?> builder) {
        super(builder);
    }

    public static class Builder extends TUIModule.Builder<Builder> {
        public Builder(String name) {
            super(Builder.class, name);
        }

        protected Builder(Builder original) {
            super(original);
        }

        @Override
        public Builder getCopy() {
            return new Builder(this);
        }

        @Override
        public TUIContainerModule build() {
            return new TUIContainerModule(self());
        }
    }
}
