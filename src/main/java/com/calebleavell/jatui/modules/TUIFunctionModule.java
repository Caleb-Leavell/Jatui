package com.calebleavell.jatui.modules;

import java.util.function.Supplier;

public class TUIFunctionModule extends TUIGenericModule {
    private Supplier<Object> function;


    @Override
    public void run() {
        Object output = function.get();
        if(getApplication() != null) getApplication().updateInput(this, output);
        super.run();
    }

    public Supplier<Object> getFunction() {
        return function;
    }

    public void setFunction(Supplier<Object> function) {
        this.function = function;
    }

    public TUIFunctionModule(Builder builder) {
        super(builder);
        this.function = builder.function;
    }

    public static class Builder extends TUIGenericModule.Builder<Builder> {
        Supplier<Object> function;

        public Builder(String name, Supplier<Object> function) {
            super(Builder.class, name);
            this.function = function;
        }

        public Builder(String name, Runnable function) {
            super(Builder.class, name);
            this.function(function);
        }

        public Builder function(Supplier<Object> function) {
            this.function = function;
            return self();
        }

        public Builder function(Runnable function) {
            if(function == null) this.function = null;
            else {
                this.function = () -> {
                    function.run();
                    return null;
                };
            }

            return self();
        }

        protected Builder(Builder original) {
            super(original);

            this.function = original.function;
        }

        @Override
        public Builder getCopy() {
            return new Builder(this);
        }

        @Override
        public TUIFunctionModule build() {
            return new TUIFunctionModule(self());
        }
    }

}
