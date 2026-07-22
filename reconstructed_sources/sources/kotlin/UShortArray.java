package kotlin;

import io.ktor.http.ContentDisposition;
import io.ktor.http.LinkHeader;
import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;
import kotlin.collections.ArraysKt;
import kotlin.jvm.JvmInline;
import kotlin.jvm.internal.CollectionToArray;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.markers.KMappedMarker;
import org.apache.commons.beanutils.FluentPropertyBeanIntrospector;
import org.apache.commons.beanutils.PropertyUtils;

/* compiled from: UShortArray.kt */
@Metadata(d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0010\u001e\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0017\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\b\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0010(\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\f\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0087@\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001:\u0001-B\u0011\b\u0001\u0012\u0006\u0010\u0003\u001a\u00020\u0004¢\u0006\u0004\b\u0005\u0010\u0006B\u0011\b\u0016\u0012\u0006\u0010\u0007\u001a\u00020\b¢\u0006\u0004\b\u0005\u0010\tJ\u0018\u0010\f\u001a\u00020\u00022\u0006\u0010\r\u001a\u00020\bH\u0086\u0002¢\u0006\u0004\b\u000e\u0010\u000fJ \u0010\u0010\u001a\u00020\u00112\u0006\u0010\r\u001a\u00020\b2\u0006\u0010\u0012\u001a\u00020\u0002H\u0086\u0002¢\u0006\u0004\b\u0013\u0010\u0014J\u0016\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00020\u0018H\u0096\u0002¢\u0006\u0004\b\u0019\u0010\u001aJ\u0018\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\u0002H\u0096\u0002¢\u0006\u0004\b\u001e\u0010\u001fJ\u001d\u0010 \u001a\u00020\u001c2\f\u0010!\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001H\u0016¢\u0006\u0004\b\"\u0010#J\u000f\u0010$\u001a\u00020\u001cH\u0016¢\u0006\u0004\b%\u0010&J\u0013\u0010'\u001a\u00020\u001c2\b\u0010(\u001a\u0004\u0018\u00010)HÖ\u0003J\t\u0010*\u001a\u00020\bHÖ\u0001J\t\u0010+\u001a\u00020,HÖ\u0001R\u0016\u0010\u0003\u001a\u00020\u00048\u0000X\u0081\u0004¢\u0006\b\n\u0000\u0012\u0004\b\n\u0010\u000bR\u0014\u0010\u0007\u001a\u00020\b8VX\u0096\u0004¢\u0006\u0006\u001a\u0004\b\u0015\u0010\u0016\u0088\u0001\u0003\u0092\u0001\u00020\u0004¨\u0006."}, d2 = {"Lkotlin/UShortArray;", "", "Lkotlin/UShort;", "storage", "", "constructor-impl", "([S)[S", ContentDisposition.Parameters.Size, "", "(I)[S", "getStorage$annotations", "()V", "get", "index", "get-Mh2AYeg", "([SI)S", FluentPropertyBeanIntrospector.DEFAULT_WRITE_METHOD_PREFIX, "", "value", "set-01HTLdE", "([SIS)V", "getSize-impl", "([S)I", "iterator", "", "iterator-impl", "([S)Ljava/util/Iterator;", "contains", "", "element", "contains-xj2QHRw", "([SS)Z", "containsAll", "elements", "containsAll-impl", "([SLjava/util/Collection;)Z", "isEmpty", "isEmpty-impl", "([S)Z", "equals", "other", "", "hashCode", "toString", "", "Iterator", "kotlin-stdlib"}, k = 1, mv = {2, 2, 0}, xi = 48)
@JvmInline
/* loaded from: classes11.dex */
public final class UShortArray implements Collection<UShort>, KMappedMarker {
    private final short[] storage;

    /* renamed from: box-impl, reason: not valid java name */
    public static final /* synthetic */ UShortArray m7654boximpl(short[] sArr) {
        return new UShortArray(sArr);
    }

    /* renamed from: constructor-impl, reason: not valid java name */
    public static short[] m7656constructorimpl(short[] storage) {
        Intrinsics.checkNotNullParameter(storage, "storage");
        return storage;
    }

    /* renamed from: equals-impl, reason: not valid java name */
    public static boolean m7659equalsimpl(short[] sArr, Object obj) {
        return (obj instanceof UShortArray) && Intrinsics.areEqual(sArr, ((UShortArray) obj).getStorage());
    }

    /* renamed from: equals-impl0, reason: not valid java name */
    public static final boolean m7660equalsimpl0(short[] sArr, short[] sArr2) {
        return Intrinsics.areEqual(sArr, sArr2);
    }

    public static /* synthetic */ void getStorage$annotations() {
    }

    /* renamed from: hashCode-impl, reason: not valid java name */
    public static int m7663hashCodeimpl(short[] sArr) {
        return Arrays.hashCode(sArr);
    }

    /* renamed from: toString-impl, reason: not valid java name */
    public static String m7667toStringimpl(short[] sArr) {
        return "UShortArray(storage=" + Arrays.toString(sArr) + PropertyUtils.MAPPED_DELIM2;
    }

    @Override // java.util.Collection
    public /* bridge */ /* synthetic */ boolean add(UShort uShort) {
        throw new UnsupportedOperationException("Operation is not supported for read-only collection");
    }

    /* renamed from: add-xj2QHRw, reason: not valid java name */
    public boolean m7668addxj2QHRw(short s) {
        throw new UnsupportedOperationException("Operation is not supported for read-only collection");
    }

    @Override // java.util.Collection
    public boolean addAll(Collection<? extends UShort> collection) {
        throw new UnsupportedOperationException("Operation is not supported for read-only collection");
    }

    @Override // java.util.Collection
    public void clear() {
        throw new UnsupportedOperationException("Operation is not supported for read-only collection");
    }

    @Override // java.util.Collection
    public boolean equals(Object other) {
        return m7659equalsimpl(this.storage, other);
    }

    @Override // java.util.Collection
    public int hashCode() {
        return m7663hashCodeimpl(this.storage);
    }

    @Override // java.util.Collection
    public boolean remove(Object obj) {
        throw new UnsupportedOperationException("Operation is not supported for read-only collection");
    }

    @Override // java.util.Collection
    public boolean removeAll(Collection<?> collection) {
        throw new UnsupportedOperationException("Operation is not supported for read-only collection");
    }

    @Override // java.util.Collection
    public boolean retainAll(Collection<?> collection) {
        throw new UnsupportedOperationException("Operation is not supported for read-only collection");
    }

    @Override // java.util.Collection
    public Object[] toArray() {
        return CollectionToArray.toArray(this);
    }

    @Override // java.util.Collection
    public <T> T[] toArray(T[] array) {
        Intrinsics.checkNotNullParameter(array, "array");
        return (T[]) CollectionToArray.toArray(this, array);
    }

    public String toString() {
        return m7667toStringimpl(this.storage);
    }

    /* renamed from: unbox-impl, reason: not valid java name and from getter */
    public final /* synthetic */ short[] getStorage() {
        return this.storage;
    }

    @Override // java.util.Collection
    public final /* bridge */ boolean contains(Object element) {
        if (element instanceof UShort) {
            return m7669containsxj2QHRw(((UShort) element).getData());
        }
        return false;
    }

    private /* synthetic */ UShortArray(short[] storage) {
        this.storage = storage;
    }

    /* renamed from: constructor-impl, reason: not valid java name */
    public static short[] m7655constructorimpl(int size) {
        return m7656constructorimpl(new short[size]);
    }

    /* renamed from: get-Mh2AYeg, reason: not valid java name */
    public static final short m7661getMh2AYeg(short[] arg0, int index) {
        return UShort.m7603constructorimpl(arg0[index]);
    }

    /* renamed from: set-01HTLdE, reason: not valid java name */
    public static final void m7666set01HTLdE(short[] arg0, int index, short value) {
        arg0[index] = value;
    }

    /* renamed from: getSize-impl, reason: not valid java name */
    public static int m7662getSizeimpl(short[] arg0) {
        return arg0.length;
    }

    @Override // java.util.Collection
    /* renamed from: getSize, reason: merged with bridge method [inline-methods] */
    public int size() {
        return m7662getSizeimpl(this.storage);
    }

    /* renamed from: iterator-impl, reason: not valid java name */
    public static java.util.Iterator<UShort> m7665iteratorimpl(short[] arg0) {
        return new Iterator(arg0);
    }

    @Override // java.util.Collection, java.lang.Iterable
    public java.util.Iterator<UShort> iterator() {
        return m7665iteratorimpl(this.storage);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* compiled from: UShortArray.kt */
    @Metadata(d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010(\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0017\n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\u000f\u0012\u0006\u0010\u0003\u001a\u00020\u0004¢\u0006\u0004\b\u0005\u0010\u0006J\t\u0010\t\u001a\u00020\nH\u0096\u0002J\u0010\u0010\u000b\u001a\u00020\u0002H\u0096\u0002¢\u0006\u0004\b\f\u0010\rR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u000e¢\u0006\u0002\n\u0000¨\u0006\u000e"}, d2 = {"Lkotlin/UShortArray$Iterator;", "", "Lkotlin/UShort;", "array", "", "<init>", "([S)V", "index", "", "hasNext", "", LinkHeader.Rel.Next, "next-Mh2AYeg", "()S", "kotlin-stdlib"}, k = 1, mv = {2, 2, 0}, xi = 48)
    /* loaded from: classes11.dex */
    public static final class Iterator implements java.util.Iterator<UShort>, KMappedMarker {
        private final short[] array;
        private int index;

        @Override // java.util.Iterator
        public void remove() {
            throw new UnsupportedOperationException("Operation is not supported for read-only collection");
        }

        public Iterator(short[] array) {
            Intrinsics.checkNotNullParameter(array, "array");
            this.array = array;
        }

        @Override // java.util.Iterator
        public /* bridge */ /* synthetic */ UShort next() {
            return UShort.m7597boximpl(m7671nextMh2AYeg());
        }

        @Override // java.util.Iterator
        public boolean hasNext() {
            return this.index < this.array.length;
        }

        /* renamed from: next-Mh2AYeg, reason: not valid java name */
        public short m7671nextMh2AYeg() {
            if (this.index >= this.array.length) {
                throw new NoSuchElementException(String.valueOf(this.index));
            }
            short[] sArr = this.array;
            int i = this.index;
            this.index = i + 1;
            return UShort.m7603constructorimpl(sArr[i]);
        }
    }

    /* renamed from: contains-xj2QHRw, reason: not valid java name */
    public boolean m7669containsxj2QHRw(short element) {
        return m7657containsxj2QHRw(this.storage, element);
    }

    /* renamed from: contains-xj2QHRw, reason: not valid java name */
    public static boolean m7657containsxj2QHRw(short[] arg0, short element) {
        return ArraysKt.contains(arg0, element);
    }

    @Override // java.util.Collection
    public boolean containsAll(Collection<?> elements) {
        Intrinsics.checkNotNullParameter(elements, "elements");
        return m7658containsAllimpl(this.storage, elements);
    }

    /* renamed from: containsAll-impl, reason: not valid java name */
    public static boolean m7658containsAllimpl(short[] arg0, Collection<UShort> elements) {
        Intrinsics.checkNotNullParameter(elements, "elements");
        Collection<UShort> $this$all$iv = elements;
        if ($this$all$iv.isEmpty()) {
            return true;
        }
        for (Object element$iv : $this$all$iv) {
            Object it = ((element$iv instanceof UShort) && ArraysKt.contains(arg0, ((UShort) element$iv).getData())) ? 1 : null;
            if (it == null) {
                return false;
            }
        }
        return true;
    }

    /* renamed from: isEmpty-impl, reason: not valid java name */
    public static boolean m7664isEmptyimpl(short[] arg0) {
        return arg0.length == 0;
    }

    @Override // java.util.Collection
    public boolean isEmpty() {
        return m7664isEmptyimpl(this.storage);
    }
}
