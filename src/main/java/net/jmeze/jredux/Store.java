package net.jmeze.jredux;

public interface Store<S> {
    S getState();
    S dispatch(Action action);
    Subscription subscribe(Subscriber<S> subscriber);
}