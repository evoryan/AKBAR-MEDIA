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

/* compiled from: UByteArray.kt */
@Metadata(d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0010\u001e\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0012\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\b\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0010(\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\f\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0087@\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001:\u0001-B\u0011\b\u0001\u0012\u0006\u0010\u0003\u001a\u00020\u0004¢\u0006\u0004\b\u0005\u0010\u0006B\u0011\b\u0016\u0012\u0006\u0010\u0007\u001a\u00020\b¢\u0006\u0004\b\u0005\u0010\tJ\u0018\u0010\f\u001a\u00020\u00022\u0006\u0010\r\u001a\u00020\bH\u0086\u0002¢\u0006\u0004\b\u000e\u0010\u000fJ \u0010\u0010\u001a\u00020\u00112\u0006\u0010\r\u001a\u00020\b2\u0006\u0010\u0012\u001a\u00020\u0002H\u0086\u0002¢\u0006\u0004\b\u0013\u0010\u0014J\u0016\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00020\u0018H\u0096\u0002¢\u0006\u0004\b\u0019\u0010\u001aJ\u0018\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\u0002H\u0096\u0002¢\u0006\u0004\b\u001e\u0010\u001fJ\u001d\u0010 \u001a\u00020\u001c2\f\u0010!\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001H\u0016¢\u0006\u0004\b\"\u0010#J\u000f\u0010$\u001a\u00020\u001cH\u0016¢\u0006\u0004\b%\u0010&J\u0013\u0010'\u001a\u00020\u001c2\b\u0010(\u001a\u0004\u0018\u00010)HÖ\u0003J\t\u0010*\u001a\u00020\bHÖ\u0001J\t\u0010+\u001a\u00020,HÖ\u0001R\u0016\u0010\u0003\u001a\u00020\u00048\u0000X\u0081\u0004¢\u0006\b\n\u0000\u0012\u0004\b\n\u0010\u000bR\u0014\u0010\u0007\u001a\u00020\b8VX\u0096\u0004¢\u0006\u0006\u001a\u0004\b\u0015\u0010\u0016\u0088\u0001\u0003\u0092\u0001\u00020\u0004¨\u0006."}, d2 = {"Lkotlin/UByteArray;", "", "Lkotlin/UByte;", "storage", "", "constructor-impl", "([B)[B", ContentDisposition.Parameters.Size, "", "(I)[B", "getStorage$annotations", "()V", "get", "index", "get-w2LRezQ", "([BI)B", FluentPropertyBeanIntrospector.DEFAULT_WRITE_METHOD_PREFIX, "", "value", "set-VurrAj0", "([BIB)V", "getSize-impl", "([B)I", "iterator", "", "iterator-impl", "([B)Ljava/util/Iterator;", "contains", "", "element", "contains-7apg3OU", "([BB)Z", "containsAll", "elements", "containsAll-impl", "([BLjava/util/Collection;)Z", "isEmpty", "isEmpty-impl", "([B)Z", "equals", "other", "", "hashCode", "toString", "", "Iterator", "kotlin-stdlib"}, k = 1, mv = {2, 2, 0}, xi = 48)
@JvmInline
/* loaded from: classes11.dex */
public final class UByteArray implements Collection<UByte>, KMappedMarker {
    private final byte[] storage;

    /* renamed from: box-impl, reason: not valid java name */
    public static final /* synthetic */ UByteArray m7391boximpl(byte[] bArr) {
        return new UByteArray(bArr);
    }

    /* renamed from: constructor-impl, reason: not valid java name */
    public static byte[] m7393constructorimpl(byte[] storage) {
        Intrinsics.checkNotNullParameter(storage, "storage");
        return storage;
    }

    /* renamed from: equals-impl, reason: not valid java name */
    public static boolean m7396equalsimpl(byte[] bArr, Object obj) {
        return (obj instanceof UByteArray) && Intrinsics.areEqual(bArr, ((UByteArray) obj).getStorage());
    }

    /* renamed from: equals-impl0, reason: not valid java name */
    public static final boolean m7397equalsimpl0(byte[] bArr, byte[] bArr2) {
        return Intrinsics.areEqual(bArr, bArr2);
    }

    public static /* synthetic */ void getStorage$annotations() {
    }

    /* renamed from: hashCode-impl, reason: not valid java name */
    public static int m7400hashCodeimpl(byte[] bArr) {
        return Arrays.hashCode(bArr);
    }

    /* renamed from: toString-impl, reason: not valid java name */
    public static String m7404toStringimpl(byte[] bArr) {
        return "UByteArray(storage=" + Arrays.toString(bArr) + PropertyUtils.MAPPED_DELIM2;
    }

    @Override // java.util.Collection
    public /* bridge */ /* synthetic */ boolean add(UByte uByte) {
        throw new UnsupportedOperationException("Operation is not supported for read-only collection");
    }

    /* renamed from: add-7apg3OU, reason: not valid java name */
    public boolean m7405add7apg3OU(byte b) {
        throw new UnsupportedOperationException("Operation is not supported for read-only collection");
    }

    @Override // java.util.Collection
    public boolean addAll(Collection<? extends UByte> collection) {
        throw new UnsupportedOperationException("Operation is not supported for read-only collection");
    }

    @Override // java.util.Collection
    public void clear() {
        throw new UnsupportedOperationException("Operation is not supported for read-only collection");
    }

    @Override // java.util.Collection
    public boolean equals(Object other) {
        return m7396equalsimpl(this.storage, other);
    }

    @Override // java.util.Collection
    public int hashCode() {
        return m7400hashCodeimpl(this.storage);
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
        return m7404toStringimpl(this.storage);
    }

    /* renamed from: unbox-impl, reason: not valid java name and from getter */
    public final /* synthetic */ byte[] getStorage() {
        return this.storage;
    }

    @Override // java.util.Collection
    public final /* bridge */ boolean contains(Object element) {
        if (element instanceof UByte) {
            return m7406contains7apg3OU(((UByte) element).getData());
        }
        return false;
    }

    private /* synthetic */ UByteArray(byte[] storage) {
        this.storage = storage;
    }

    /* renamed from: constructor-impl, reason: not valid java name */
    public static byte[] m7392constructorimpl(int size) {
        return m7393constructorimpl(new byte[size]);
    }

    /* renamed from: get-w2LRezQ, reason: not valid java name */
    public static final byte m7398getw2LRezQ(byte[] arg0, int index) {
        return UByte.m7340constructorimpl(arg0[index]);
    }

    /* renamed from: set-VurrAj0, reason: not valid java name */
    public static final void m7403setVurrAj0(byte[] arg0, int index, byte value) {
        arg0[index] = value;
    }

    /* renamed from: getSize-impl, reason: not valid java name */
    public static int m7399getSizeimpl(byte[] arg0) {
        return arg0.length;
    }

    @Override // java.util.Collection
    /* renamed from: getSize, reason: merged with bridge method [inline-methods] */
    public int size() {
        return m7399getSizeimpl(this.storage);
    }

    /* renamed from: iterator-impl, reason: not valid java name */
    public static java.util.Iterator<UByte> m7402iteratorimpl(byte[] arg0) {
        return new Iterator(arg0);
    }

    @Override // java.util.Collection, java.lang.Iterable
    public java.util.Iterator<UByte> iterator() {
        return m7402iteratorimpl(this.storage);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* compiled from: UByteArray.kt */
    @Metadata(d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010(\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0012\n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\u000f\u0012\u0006\u0010\u0003\u001a\u00020\u0004¢\u0006\u0004\b\u0005\u0010\u0006J\t\u0010\t\u001a\u00020\nH\u0096\u0002J\u0010\u0010\u000b\u001a\u00020\u0002H\u0096\u0002¢\u0006\u0004\b\f\u0010\rR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u000e¢\u0006\u0002\n\u0000¨\u0006\u000e"}, d2 = {"Lkotlin/UByteArray$Iterator;", "", "Lkotlin/UByte;", "array", "", "<init>", "([B)V", "index", "", "hasNext", "", LinkHeader.Rel.Next, "next-w2LRezQ", "()B", "kotlin-stdlib"}, k = 1, mv = {2, 2, 0}, xi = 48)
    /* loaded from: classes11.dex */
    public static final class Iterator implements java.util.Iterator<UByte>, KMappedMarker {
        private final byte[] array;
        private int index;

        @Override // java.util.Iterator
        public void remove() {
            throw new UnsupportedOperationException("Operation is not supported for read-only collection");
        }

        public Iterator(byte[] array) {
            Intrinsics.checkNotNullParameter(array, "array");
            this.array = array;
        }

        @Override // java.util.Iterator
        public /* bridge */ /* synthetic */ UByte next() {
            return UByte.m7334boximpl(m7408nextw2LRezQ());
        }

        @Override // java.util.Iterator
        public boolean hasNext() {
            return this.index < this.array.length;
        }

        /* renamed from: next-w2LRezQ, reason: not valid java name */
        public byte m7408nextw2LRezQ() {
            if (this.index >= this.array.length) {
                throw new NoSuchElementException(String.valueOf(this.index));
            }
            byte[] bArr = this.array;
            int i = this.index;
            this.index = i + 1;
            return UByte.m7340constructorimpl(bArr[i]);
        }
    }

    /* renamed from: contains-7apg3OU, reason: not valid java name */
    public boolean m7406contains7apg3OU(byte element) {
        return m7394contains7apg3OU(this.storage, element);
    }

    /* renamed from: contains-7apg3OU, reason: not valid java name */
    public static boolean m7394contains7apg3OU(byte[] arg0, byte element) {
        return ArraysKt.contains(arg0, element);
    }

    @Override // java.util.Collection
    public boolean containsAll(Collection<?> elements) {
        Intrinsics.checkNotNullParameter(elements, "elements");
        return m7395containsAllimpl(this.storage, elements);
    }

    /* renamed from: containsAll-impl, reason: not valid java name */
    public static boolean m7395containsAllimpl(byte[] arg0, Collection<UByte> elements) {
        Intrinsics.checkNotNullParameter(elements, "elements");
        Collection<UByte> $this$all$iv = elements;
        if ($this$all$iv.isEmpty()) {
            return true;
        }
        for (Object element$iv : $this$all$iv) {
            Object it = ((element$iv instanceof UByte) && ArraysKt.contains(arg0, ((UByte) element$iv).getData())) ? 1 : null;
            if (it == null) {
                return false;
            }
        }
        return true;
    }

    /* renamed from: isEmpty-impl, reason: not valid java name */
    public static boolean m7401isEmptyimpl(byte[] arg0) {
        return arg0.length == 0;
    }

    @Override // java.util.Collection
    public boolean isEmpty() {
        return m7401isEmptyimpl(this.storage);
    }
}
