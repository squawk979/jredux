package net.jmeze.jredux;

public interface Store<S> {
    S getState();
    void dispatch(Action action);
    Subscription subscribe(Subscriber<S> subscriber);
}