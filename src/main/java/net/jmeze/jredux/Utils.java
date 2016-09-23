package net.jmeze.jredux;

public class Utils {
    @SafeVarargs
    public static <S> Reducer<S> combineReducers(final Reducer<S>... reducers) {
        return new Reducer<S>() {
            @Override
            public S reduce(S state, Action action) {
                for (Reducer<S> reducer : reducers) {
                    state = reducer.reduce(state, action);
                }

                return state;
            }
        };
    }
}
