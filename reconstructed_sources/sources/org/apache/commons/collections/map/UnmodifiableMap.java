package org.apache.commons.collections.map;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.IterableMap;
import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.Unmodifiable;
import org.apache.commons.collections.collection.UnmodifiableCollection;
import org.apache.commons.collections.iterators.EntrySetMapIterator;
import org.apache.commons.collections.iterators.UnmodifiableMapIterator;
import org.apache.commons.collections.set.UnmodifiableSet;

/* loaded from: classes12.dex */
public final class UnmodifiableMap extends AbstractMapDecorator implements IterableMap, Unmodifiable, Serializable {
    private static final long serialVersionUID = 2737023427269031941L;

    public static Map decorate(Map map) {
        if (map instanceof Unmodifiable) {
            return map;
        }
        return new UnmodifiableMap(map);
    }

    private UnmodifiableMap(Map map) {
        super(map);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(this.map);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.map = (Map) in.readObject();
    }

    @Override // org.apache.commons.collections.map.AbstractMapDecorator, java.util.Map
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override // org.apache.commons.collections.map.AbstractMapDecorator, java.util.Map, org.apache.commons.collections.BidiMap
    public Object put(Object key, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override // org.apache.commons.collections.map.AbstractMapDecorator, java.util.Map
    public void putAll(Map mapToCopy) {
        throw new UnsupportedOperationException();
    }

    @Override // org.apache.commons.collections.map.AbstractMapDecorator, java.util.Map
    public Object remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override // org.apache.commons.collections.IterableMap
    public MapIterator mapIterator() {
        if (this.map instanceof IterableMap) {
            MapIterator it = ((IterableMap) this.map).mapIterator();
            return UnmodifiableMapIterator.decorate(it);
        }
        MapIterator it2 = new EntrySetMapIterator(this.map);
        return UnmodifiableMapIterator.decorate(it2);
    }

    @Override // org.apache.commons.collections.map.AbstractMapDecorator, java.util.Map
    public Set entrySet() {
        Set set = super.entrySet();
        return UnmodifiableEntrySet.decorate(set);
    }

    @Override // org.apache.commons.collections.map.AbstractMapDecorator, java.util.Map
    public Set keySet() {
        Set set = super.keySet();
        return UnmodifiableSet.decorate(set);
    }

    @Override // org.apache.commons.collections.map.AbstractMapDecorator, java.util.Map
    public Collection values() {
        Collection coll = super.values();
        return UnmodifiableCollection.decorate(coll);
    }
}
