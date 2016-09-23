package net.jmeze.jredux;

public interface Reducer<S> {
    S reduce(S state, Action action);
}
