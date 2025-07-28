package com.calebleavell.jatui.modules;

import java.util.Objects;
import java.util.function.Supplier;

public class TUIFunctionModule extends TUIModule {
    private Supplier<Object> function;


    @Override
    public void run() {
        this.terminated = false;
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

    /**
     * <p>Checks equality for properties given by the builder.</p>
     *
     * <p>For TUIFunctionModule, this includes: </p>
     * <strong><ul>
     *     <li>function </strong><i>(Note: this will cause this method to return false in most cases.)</i><strong></li>
     *     <li>name</li>
     *     <li>application</li>
     *     <li>children</li>
     *     <li>ansi</li>
     *     <li>scanner</li>
     *     <li>printStream</li>
     *     <li>enableAnsi</li>
     * </ul></strong>
     * <p>Note: Runtime properties (e.g., inputMap, currentRunningChild, terminated), are not considered.</p>
     * @param o The object to compare (must be a TUIModule object)
     * @return Whether this object equals o
     */
    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null) return false;
        if(getClass() != o.getClass()) return false;

        TUIFunctionModule other = (TUIFunctionModule) o;

        return Objects.equals(function, other.function) && super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(function, super.hashCode());
    }

    public TUIFunctionModule(Builder builder) {
        super(builder);
        this.function = builder.function;
    }

    public static class Builder extends TUIModule.Builder<Builder> {
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
