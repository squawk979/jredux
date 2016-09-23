package net.jmeze.jredux;

public interface Subscriber<S> {
    void onStateChange(S state);
}
