package org.apache.commons.lang3.event;

import com.google.android.gms.common.internal.ServiceSpecificExtraArgs;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

/* loaded from: classes12.dex */
public class EventListenerSupport<L> implements Serializable {
    private static final long serialVersionUID = 3593265990380473632L;
    private List<L> listeners;
    private transient L[] prototypeArray;
    private transient L proxy;

    public static <T> EventListenerSupport<T> create(Class<T> listenerInterface) {
        return new EventListenerSupport<>(listenerInterface);
    }

    public EventListenerSupport(Class<L> listenerInterface) {
        this(listenerInterface, Thread.currentThread().getContextClassLoader());
    }

    public EventListenerSupport(Class<L> listenerInterface, ClassLoader classLoader) {
        this();
        Objects.requireNonNull(listenerInterface, "listenerInterface");
        Objects.requireNonNull(classLoader, "classLoader");
        Validate.isTrue(listenerInterface.isInterface(), "Class %s is not an interface", listenerInterface.getName());
        initializeTransientFields(listenerInterface, classLoader);
    }

    private EventListenerSupport() {
        this.listeners = new CopyOnWriteArrayList();
    }

    public L fire() {
        return this.proxy;
    }

    public void addListener(L listener) {
        addListener(listener, true);
    }

    public void addListener(L listener, boolean allowDuplicate) {
        Objects.requireNonNull(listener, ServiceSpecificExtraArgs.CastExtraArgs.LISTENER);
        if (allowDuplicate || !this.listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

    int getListenerCount() {
        return this.listeners.size();
    }

    public void removeListener(L listener) {
        Objects.requireNonNull(listener, ServiceSpecificExtraArgs.CastExtraArgs.LISTENER);
        this.listeners.remove(listener);
    }

    public L[] getListeners() {
        return (L[]) this.listeners.toArray(this.prototypeArray);
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        ArrayList<L> serializableListeners = new ArrayList<>();
        ObjectOutputStream testObjectOutputStream = new ObjectOutputStream(new ByteArrayOutputStream());
        for (L listener : this.listeners) {
            try {
                testObjectOutputStream.writeObject(listener);
                serializableListeners.add(listener);
            } catch (IOException e) {
                testObjectOutputStream = new ObjectOutputStream(new ByteArrayOutputStream());
            }
        }
        objectOutputStream.writeObject(serializableListeners.toArray(this.prototypeArray));
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        Object[] objArr = (Object[]) objectInputStream.readObject();
        this.listeners = new CopyOnWriteArrayList(objArr);
        Class<L> listenerInterface = ArrayUtils.getComponentType(objArr);
        initializeTransientFields(listenerInterface, Thread.currentThread().getContextClassLoader());
    }

    private void initializeTransientFields(Class<L> cls, ClassLoader classLoader) {
        this.prototypeArray = (L[]) ArrayUtils.newInstance(cls, 0);
        createProxy(cls, classLoader);
    }

    private void createProxy(Class<L> listenerInterface, ClassLoader classLoader) {
        this.proxy = listenerInterface.cast(Proxy.newProxyInstance(classLoader, new Class[]{listenerInterface}, createInvocationHandler()));
    }

    protected InvocationHandler createInvocationHandler() {
        return new ProxyInvocationHandler();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* loaded from: classes12.dex */
    public class ProxyInvocationHandler implements InvocationHandler {
        protected ProxyInvocationHandler() {
        }

        @Override // java.lang.reflect.InvocationHandler
        public Object invoke(Object unusedProxy, Method method, Object[] args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            Iterator it = EventListenerSupport.this.listeners.iterator();
            while (it.hasNext()) {
                method.invoke(it.next(), args);
            }
            return null;
        }
    }
}
