package com.calebleavell.jatui.modules;

import java.util.function.Supplier;

public class TUIFunctionModule extends TUIModule {
    private final Supplier<?> function;


    @Override
    public void run() {
        this.terminated = false;
        Object output = function.get();
        if(getApplication() != null) getApplication().updateInput(this, output);
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
        Supplier<?> function; // this isn't checked in a .equalTo so structural equality can actually return true ;)

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
            this.function = function;
            return self();
        }

        public Builder setFunction(Runnable function) {
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
            return new TUIFunctionModule(self());
        }
    }

}
