package org.apache.commons.collections.bidimap;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.OrderedBidiMap;
import org.apache.commons.collections.OrderedMap;
import org.apache.commons.collections.OrderedMapIterator;
import org.apache.commons.collections.ResettableIterator;
import org.apache.commons.collections.SortedBidiMap;
import org.apache.commons.collections.map.AbstractSortedMapDecorator;

/* loaded from: classes12.dex */
public class DualTreeBidiMap extends AbstractDualBidiMap implements SortedBidiMap, Serializable {
    private static final long serialVersionUID = 721969328361809L;
    protected final Comparator comparator;

    public DualTreeBidiMap() {
        super(new TreeMap(), new TreeMap());
        this.comparator = null;
    }

    public DualTreeBidiMap(Map map) {
        super(new TreeMap(), new TreeMap());
        putAll(map);
        this.comparator = null;
    }

    public DualTreeBidiMap(Comparator comparator) {
        super(new TreeMap(comparator), new TreeMap(comparator));
        this.comparator = comparator;
    }

    protected DualTreeBidiMap(Map normalMap, Map reverseMap, BidiMap inverseBidiMap) {
        super(normalMap, reverseMap, inverseBidiMap);
        this.comparator = ((SortedMap) normalMap).comparator();
    }

    @Override // org.apache.commons.collections.bidimap.AbstractDualBidiMap
    protected BidiMap createBidiMap(Map normalMap, Map reverseMap, BidiMap inverseMap) {
        return new DualTreeBidiMap(normalMap, reverseMap, inverseMap);
    }

    @Override // java.util.SortedMap
    public Comparator comparator() {
        return ((SortedMap) this.maps[0]).comparator();
    }

    @Override // org.apache.commons.collections.OrderedMap
    public Object firstKey() {
        return ((SortedMap) this.maps[0]).firstKey();
    }

    @Override // org.apache.commons.collections.OrderedMap
    public Object lastKey() {
        return ((SortedMap) this.maps[0]).lastKey();
    }

    @Override // org.apache.commons.collections.OrderedMap
    public Object nextKey(Object key) {
        if (isEmpty()) {
            return null;
        }
        if (this.maps[0] instanceof OrderedMap) {
            return ((OrderedMap) this.maps[0]).nextKey(key);
        }
        SortedMap sm = (SortedMap) this.maps[0];
        Iterator it = sm.tailMap(key).keySet().iterator();
        it.next();
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    @Override // org.apache.commons.collections.OrderedMap
    public Object previousKey(Object key) {
        if (isEmpty()) {
            return null;
        }
        if (this.maps[0] instanceof OrderedMap) {
            return ((OrderedMap) this.maps[0]).previousKey(key);
        }
        SortedMap sm = (SortedMap) this.maps[0];
        SortedMap hm = sm.headMap(key);
        if (hm.isEmpty()) {
            return null;
        }
        return hm.lastKey();
    }

    @Override // org.apache.commons.collections.OrderedMap
    public OrderedMapIterator orderedMapIterator() {
        return new BidiOrderedMapIterator(this);
    }

    @Override // org.apache.commons.collections.SortedBidiMap
    public SortedBidiMap inverseSortedBidiMap() {
        return (SortedBidiMap) inverseBidiMap();
    }

    @Override // org.apache.commons.collections.OrderedBidiMap
    public OrderedBidiMap inverseOrderedBidiMap() {
        return (OrderedBidiMap) inverseBidiMap();
    }

    @Override // java.util.SortedMap
    public SortedMap headMap(Object toKey) {
        SortedMap sub = ((SortedMap) this.maps[0]).headMap(toKey);
        return new ViewMap(this, sub);
    }

    @Override // java.util.SortedMap
    public SortedMap tailMap(Object fromKey) {
        SortedMap sub = ((SortedMap) this.maps[0]).tailMap(fromKey);
        return new ViewMap(this, sub);
    }

    @Override // java.util.SortedMap
    public SortedMap subMap(Object fromKey, Object toKey) {
        SortedMap sub = ((SortedMap) this.maps[0]).subMap(fromKey, toKey);
        return new ViewMap(this, sub);
    }

    /* loaded from: classes12.dex */
    protected static class ViewMap extends AbstractSortedMapDecorator {
        final DualTreeBidiMap bidi;

        protected ViewMap(DualTreeBidiMap bidi, SortedMap sm) {
            super((SortedMap) bidi.createBidiMap(sm, bidi.maps[1], bidi.inverseBidiMap));
            this.bidi = (DualTreeBidiMap) this.map;
        }

        @Override // org.apache.commons.collections.map.AbstractMapDecorator, java.util.Map
        public boolean containsValue(Object value) {
            return this.bidi.maps[0].containsValue(value);
        }

        @Override // org.apache.commons.collections.map.AbstractMapDecorator, java.util.Map
        public void clear() {
            Iterator it = keySet().iterator();
            while (it.hasNext()) {
                it.next();
                it.remove();
            }
        }

        @Override // org.apache.commons.collections.map.AbstractSortedMapDecorator, java.util.SortedMap
        public SortedMap headMap(Object toKey) {
            return new ViewMap(this.bidi, super.headMap(toKey));
        }

        @Override // org.apache.commons.collections.map.AbstractSortedMapDecorator, java.util.SortedMap
        public SortedMap tailMap(Object fromKey) {
            return new ViewMap(this.bidi, super.tailMap(fromKey));
        }

        @Override // org.apache.commons.collections.map.AbstractSortedMapDecorator, java.util.SortedMap
        public SortedMap subMap(Object fromKey, Object toKey) {
            return new ViewMap(this.bidi, super.subMap(fromKey, toKey));
        }
    }

    /* loaded from: classes12.dex */
    protected static class BidiOrderedMapIterator implements OrderedMapIterator, ResettableIterator {
        protected ListIterator iterator;
        private Map.Entry last = null;
        protected final AbstractDualBidiMap parent;

        protected BidiOrderedMapIterator(AbstractDualBidiMap parent) {
            this.parent = parent;
            this.iterator = new ArrayList(parent.entrySet()).listIterator();
        }

        @Override // org.apache.commons.collections.MapIterator, java.util.Iterator
        public boolean hasNext() {
            return this.iterator.hasNext();
        }

        @Override // org.apache.commons.collections.MapIterator, java.util.Iterator
        public Object next() {
            this.last = (Map.Entry) this.iterator.next();
            return this.last.getKey();
        }

        @Override // org.apache.commons.collections.OrderedMapIterator, org.apache.commons.collections.OrderedIterator
        public boolean hasPrevious() {
            return this.iterator.hasPrevious();
        }

        @Override // org.apache.commons.collections.OrderedMapIterator, org.apache.commons.collections.OrderedIterator
        public Object previous() {
            this.last = (Map.Entry) this.iterator.previous();
            return this.last.getKey();
        }

        @Override // org.apache.commons.collections.MapIterator, java.util.Iterator
        public void remove() {
            this.iterator.remove();
            this.parent.remove(this.last.getKey());
            this.last = null;
        }

        @Override // org.apache.commons.collections.MapIterator
        public Object getKey() {
            if (this.last == null) {
                throw new IllegalStateException("Iterator getKey() can only be called after next() and before remove()");
            }
            return this.last.getKey();
        }

        @Override // org.apache.commons.collections.MapIterator
        public Object getValue() {
            if (this.last == null) {
                throw new IllegalStateException("Iterator getValue() can only be called after next() and before remove()");
            }
            return this.last.getValue();
        }

        @Override // org.apache.commons.collections.MapIterator
        public Object setValue(Object value) {
            if (this.last == null) {
                throw new IllegalStateException("Iterator setValue() can only be called after next() and before remove()");
            }
            if (this.parent.maps[1].containsKey(value) && this.parent.maps[1].get(value) != this.last.getKey()) {
                throw new IllegalArgumentException("Cannot use setValue() when the object being set is already in the map");
            }
            return this.parent.put(this.last.getKey(), value);
        }

        @Override // org.apache.commons.collections.ResettableIterator
        public void reset() {
            this.iterator = new ArrayList(this.parent.entrySet()).listIterator();
            this.last = null;
        }

        public String toString() {
            if (this.last != null) {
                return new StringBuffer().append("MapIterator[").append(getKey()).append("=").append(getValue()).append("]").toString();
            }
            return "MapIterator[]";
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(this.maps[0]);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.maps[0] = new TreeMap(this.comparator);
        this.maps[1] = new TreeMap(this.comparator);
        Map map = (Map) in.readObject();
        putAll(map);
    }
}
