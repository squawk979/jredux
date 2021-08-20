import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;

import java.lang.reflect.Field;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import net.jmeze.jredux.Action;
import net.jmeze.jredux.BaseStore;
import net.jmeze.jredux.Reducer;
import net.jmeze.jredux.Subscriber;

public class BaseStoreTest {

    @Before
    public void cleanup() throws NoSuchFieldException, IllegalAccessException {
        // need to cleanup threadlocal state.  as it's private use reflection to get to it
        Field field = BaseStore.class.getDeclaredField("isDispatching");
        field.setAccessible(true);
        ThreadLocal<Boolean> isDispatching = (ThreadLocal<Boolean>) field.get(null);
        isDispatching.set(false);
    }

    @Test
    public void dispatch() {

        BaseStore<Integer> baseStore = new BaseStore<Integer>(new Integer(0), new Reducer<Integer>() {
            @Override
            public Integer reduce(Integer state, Action action) {
                return state += 1;
            }
        });

        baseStore.dispatch(new Action(){
            // empty action
        });

        assertEquals(1, baseStore.getState().intValue());

    }

    @Test
    public void subscription() {

        final BaseStore<Integer> baseStore = new BaseStore<Integer>(new Integer(0), new Reducer<Integer>() {
            @Override
            public Integer reduce(Integer state, Action action) {
                return state += 1;
            }
        });

        baseStore.subscribe(new Subscriber<Integer>() {
            @Override
            public void onStateChange(Integer state) {
                assertEquals(1, state.intValue());
            }
        });

        baseStore.dispatch(new Action(){
            // empty action
        });

    }

    @Test(expected = IllegalStateException.class)
    public void reducerDispatch() {

        final String BASE_STORE_NAME = "baseStore";
        final HashMap<String, Object> trivialContext = new HashMap<>();

        final BaseStore<Integer> baseStore = new BaseStore<Integer>(new Integer(0), new Reducer<Integer>() {
            @Override
            public Integer reduce(Integer state, Action action) {
                BaseStore baseStore = (BaseStore) trivialContext.get(BASE_STORE_NAME);
                baseStore.dispatch(new Action(){
                    // empty action
                });
                return null;
            }
        });

        trivialContext.put(BASE_STORE_NAME, baseStore);

        baseStore.dispatch(new Action(){
            // empty action
        });

    }

    @Test
    public void nullState() {

        BaseStore<Integer> baseStore = new BaseStore<Integer>(null, new Reducer<Integer>() {
            @Override
            public Integer reduce(Integer state, Action action) {
                return null;
            }
        });

        baseStore.dispatch(new Action(){
            // empty action
        });

        assertNull(baseStore.getState());

    }

    @Test(expected = IllegalArgumentException.class)
    public void nullReducer() {

        BaseStore<Integer> baseStore = new BaseStore<Integer>(new Integer(0), null);

        baseStore.dispatch(new Action() {
            // empty action
        });

    }

    @Test(expected = RuntimeException.class)
    public void donNotCatchRuntimeExceptionInReducer() {

        BaseStore<Integer> baseStore = new BaseStore<Integer>(null, new Reducer<Integer>() {
            @Override
            public Integer reduce(Integer state, Action action) {
                throw new RuntimeException();
            }
        });

        try {
            baseStore.dispatch(new Action() {
                // empty action
            });
        } catch (Throwable e) {
            // let's assume we somehow fix the problem
        }

        // second call to dispatch
        baseStore.dispatch(new Action(){
            // empty action
        });

        assertNull(baseStore.getState());

    }

}
