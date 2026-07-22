package org.apache.commons.collections4.bidimap;

import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.Unmodifiable;
import org.apache.commons.collections4.iterators.UnmodifiableMapIterator;
import org.apache.commons.collections4.map.UnmodifiableEntrySet;
import org.apache.commons.collections4.set.UnmodifiableSet;

/* loaded from: classes12.dex */
public final class UnmodifiableBidiMap<K, V> extends AbstractBidiMapDecorator<K, V> implements Unmodifiable {
    private UnmodifiableBidiMap<V, K> inverse;

    /* JADX WARN: Multi-variable type inference failed */
    public static <K, V> BidiMap<K, V> unmodifiableBidiMap(BidiMap<? extends K, ? extends V> bidiMap) {
        if (bidiMap instanceof Unmodifiable) {
            return bidiMap;
        }
        return new UnmodifiableBidiMap(bidiMap);
    }

    private UnmodifiableBidiMap(BidiMap<? extends K, ? extends V> map) {
        super(map);
    }

    @Override // org.apache.commons.collections4.map.AbstractMapDecorator, java.util.Map, org.apache.commons.collections4.Put
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override // org.apache.commons.collections4.map.AbstractMapDecorator, java.util.Map, org.apache.commons.collections4.Put
    public V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override // org.apache.commons.collections4.map.AbstractMapDecorator, java.util.Map, org.apache.commons.collections4.Put
    public void putAll(Map<? extends K, ? extends V> mapToCopy) {
        throw new UnsupportedOperationException();
    }

    @Override // org.apache.commons.collections4.map.AbstractMapDecorator, java.util.Map, org.apache.commons.collections4.Get
    public V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override // org.apache.commons.collections4.map.AbstractMapDecorator, java.util.Map, org.apache.commons.collections4.Get
    public Set<Map.Entry<K, V>> entrySet() {
        Set<Map.Entry<K, V>> set = super.entrySet();
        return UnmodifiableEntrySet.unmodifiableEntrySet(set);
    }

    @Override // org.apache.commons.collections4.map.AbstractMapDecorator, java.util.Map, org.apache.commons.collections4.Get
    public Set<K> keySet() {
        Set<K> set = super.keySet();
        return UnmodifiableSet.unmodifiableSet(set);
    }

    @Override // org.apache.commons.collections4.bidimap.AbstractBidiMapDecorator, org.apache.commons.collections4.map.AbstractMapDecorator, java.util.Map, org.apache.commons.collections4.Get
    public Set<V> values() {
        Set<V> set = super.values();
        return UnmodifiableSet.unmodifiableSet(set);
    }

    @Override // org.apache.commons.collections4.bidimap.AbstractBidiMapDecorator, org.apache.commons.collections4.BidiMap
    public K removeValue(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override // org.apache.commons.collections4.bidimap.AbstractBidiMapDecorator, org.apache.commons.collections4.map.AbstractIterableMap, org.apache.commons.collections4.IterableGet
    public MapIterator<K, V> mapIterator() {
        MapIterator<K, V> it = decorated().mapIterator();
        return UnmodifiableMapIterator.unmodifiableMapIterator(it);
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // org.apache.commons.collections4.bidimap.AbstractBidiMapDecorator, org.apache.commons.collections4.BidiMap
    public synchronized BidiMap<V, K> inverseBidiMap() {
        if (this.inverse == null) {
            this.inverse = new UnmodifiableBidiMap<>(decorated().inverseBidiMap());
            this.inverse.inverse = this;
        }
        return this.inverse;
    }
}
