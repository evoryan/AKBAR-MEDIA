package org.apache.commons.collections.map;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.apache.commons.collections.iterators.UnmodifiableIterator;
import org.apache.commons.collections.iterators.UnmodifiableListIterator;
import org.apache.commons.collections.list.UnmodifiableList;
import org.apache.commons.collections.map.AbstractLinkedMap;

/* loaded from: classes12.dex */
public class LinkedMap extends AbstractLinkedMap implements Serializable, Cloneable {
    private static final long serialVersionUID = 9077234323521161066L;

    public LinkedMap() {
        super(16, 0.75f, 12);
    }

    public LinkedMap(int initialCapacity) {
        super(initialCapacity);
    }

    public LinkedMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public LinkedMap(Map map) {
        super(map);
    }

    @Override // org.apache.commons.collections.map.AbstractHashedMap, java.util.AbstractMap
    public Object clone() {
        return super.clone();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        doWriteObject(out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        doReadObject(in);
    }

    public Object get(int index) {
        return getEntry(index).getKey();
    }

    public Object getValue(int index) {
        return getEntry(index).getValue();
    }

    public int indexOf(Object key) {
        Object key2 = convertKey(key);
        int i = 0;
        AbstractLinkedMap.LinkEntry entry = this.header.after;
        while (entry != this.header) {
            if (!isEqualKey(key2, entry.key)) {
                entry = entry.after;
                i++;
            } else {
                return i;
            }
        }
        return -1;
    }

    public Object remove(int index) {
        return remove(get(index));
    }

    public List asList() {
        return new LinkedMapList(this);
    }

    /* loaded from: classes12.dex */
    static class LinkedMapList extends AbstractList {
        final LinkedMap parent;

        LinkedMapList(LinkedMap parent) {
            this.parent = parent;
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.util.List
        public int size() {
            return this.parent.size();
        }

        @Override // java.util.AbstractList, java.util.List
        public Object get(int index) {
            return this.parent.get(index);
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.util.List
        public boolean contains(Object obj) {
            return this.parent.containsKey(obj);
        }

        @Override // java.util.AbstractList, java.util.List
        public int indexOf(Object obj) {
            return this.parent.indexOf(obj);
        }

        @Override // java.util.AbstractList, java.util.List
        public int lastIndexOf(Object obj) {
            return this.parent.indexOf(obj);
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.util.List
        public boolean containsAll(Collection coll) {
            return this.parent.keySet().containsAll(coll);
        }

        @Override // java.util.AbstractList, java.util.List
        public Object remove(int index) {
            throw new UnsupportedOperationException();
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.util.List
        public boolean remove(Object obj) {
            throw new UnsupportedOperationException();
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.util.List
        public boolean removeAll(Collection coll) {
            throw new UnsupportedOperationException();
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.util.List
        public boolean retainAll(Collection coll) {
            throw new UnsupportedOperationException();
        }

        @Override // java.util.AbstractList, java.util.AbstractCollection, java.util.Collection, java.util.List
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.util.List
        public Object[] toArray() {
            return this.parent.keySet().toArray();
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.util.List
        public Object[] toArray(Object[] array) {
            return this.parent.keySet().toArray(array);
        }

        @Override // java.util.AbstractList, java.util.AbstractCollection, java.util.Collection, java.lang.Iterable, java.util.List
        public Iterator iterator() {
            return UnmodifiableIterator.decorate(this.parent.keySet().iterator());
        }

        @Override // java.util.AbstractList, java.util.List
        public ListIterator listIterator() {
            return UnmodifiableListIterator.decorate(super.listIterator());
        }

        @Override // java.util.AbstractList, java.util.List
        public ListIterator listIterator(int fromIndex) {
            return UnmodifiableListIterator.decorate(super.listIterator(fromIndex));
        }

        @Override // java.util.AbstractList, java.util.List
        public List subList(int fromIndexInclusive, int toIndexExclusive) {
            return UnmodifiableList.decorate(super.subList(fromIndexInclusive, toIndexExclusive));
        }
    }
}
