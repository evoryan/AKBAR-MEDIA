package androidx.collection;

import androidx.collection.internal.RuntimeHelpersKt;
import com.google.firebase.messaging.Constants;
import kotlin.Metadata;
import kotlin.ULong;
import kotlin.collections.ArraysKt;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.apache.commons.beanutils.FluentPropertyBeanIntrospector;

/* compiled from: LongLongMap.kt */
@Metadata(d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0016\n\u0002\b\u0007\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\b\u0018\u00002\u00020\u0001B\u000f\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003¢\u0006\u0002\u0010\u0004J\b\u0010\u0006\u001a\u00020\u0007H\u0002J\u0006\u0010\b\u001a\u00020\u0007J\b\u0010\t\u001a\u00020\u0007H\u0002J\u0010\u0010\n\u001a\u00020\u00032\u0006\u0010\u000b\u001a\u00020\u0003H\u0002J\u0010\u0010\f\u001a\u00020\u00032\u0006\u0010\r\u001a\u00020\u000eH\u0002J\"\u0010\u000f\u001a\u00020\u000e2\u0006\u0010\r\u001a\u00020\u000e2\f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u000e0\u0011H\u0086\bø\u0001\u0000J\b\u0010\u0012\u001a\u00020\u0007H\u0002J\u0010\u0010\u0013\u001a\u00020\u00072\u0006\u0010\u0014\u001a\u00020\u0003H\u0002J\u0010\u0010\u0015\u001a\u00020\u00072\u0006\u0010\u0002\u001a\u00020\u0003H\u0002J\u0011\u0010\u0016\u001a\u00020\u00072\u0006\u0010\u0017\u001a\u00020\u0018H\u0086\nJ\u0011\u0010\u0016\u001a\u00020\u00072\u0006\u0010\u0017\u001a\u00020\u0019H\u0086\nJ\u0011\u0010\u0016\u001a\u00020\u00072\u0006\u0010\r\u001a\u00020\u000eH\u0086\nJ\u0011\u0010\u0016\u001a\u00020\u00072\u0006\u0010\u0017\u001a\u00020\u001aH\u0086\nJ\u0011\u0010\u001b\u001a\u00020\u00072\u0006\u0010\u001c\u001a\u00020\u0001H\u0086\nJ\u0016\u0010\u001d\u001a\u00020\u00072\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u001e\u001a\u00020\u000eJ\u001e\u0010\u001d\u001a\u00020\u000e2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u001e\u001a\u00020\u000e2\u0006\u0010\u001f\u001a\u00020\u000eJ\u000e\u0010 \u001a\u00020\u00072\u0006\u0010\u001c\u001a\u00020\u0001J\u000e\u0010!\u001a\u00020\u00072\u0006\u0010\r\u001a\u00020\u000eJ\u0016\u0010!\u001a\u00020\"2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u001e\u001a\u00020\u000eJ&\u0010#\u001a\u00020\u00072\u0018\u0010$\u001a\u0014\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\"0%H\u0086\bø\u0001\u0000J\u0010\u0010&\u001a\u00020\u00072\u0006\u0010'\u001a\u00020\u0003H\u0001J\u0010\u0010(\u001a\u00020\u00072\u0006\u0010)\u001a\u00020\u0003H\u0002J\u0019\u0010*\u001a\u00020\u00072\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u001e\u001a\u00020\u000eH\u0086\u0002J\u0006\u0010+\u001a\u00020\u0003J\u0019\u0010,\u001a\u00020\u00072\u0006\u0010'\u001a\u00020\u00032\u0006\u0010\u001e\u001a\u00020\u000eH\u0082\bR\u000e\u0010\u0005\u001a\u00020\u0003X\u0082\u000e¢\u0006\u0002\n\u0000\u0082\u0002\u0007\n\u0005\b\u009920\u0001¨\u0006-"}, d2 = {"Landroidx/collection/MutableLongLongMap;", "Landroidx/collection/LongLongMap;", "initialCapacity", "", "(I)V", "growthLimit", "adjustStorage", "", "clear", "dropDeletes", "findFirstAvailableSlot", "hash1", "findInsertIndex", "key", "", "getOrPut", "defaultValue", "Lkotlin/Function0;", "initializeGrowth", "initializeMetadata", "capacity", "initializeStorage", "minusAssign", "keys", "Landroidx/collection/LongList;", "Landroidx/collection/LongSet;", "", "plusAssign", Constants.MessagePayloadKeys.FROM, "put", "value", "default", "putAll", "remove", "", "removeIf", "predicate", "Lkotlin/Function2;", "removeValueAt", "index", "resizeStorage", "newCapacity", FluentPropertyBeanIntrospector.DEFAULT_WRITE_METHOD_PREFIX, "trim", "writeMetadata", "collection"}, k = 1, mv = {1, 8, 0}, xi = 48)
/* loaded from: classes.dex */
public final class MutableLongLongMap extends LongLongMap {
    private int growthLimit;

    public MutableLongLongMap() {
        this(0, 1, null);
    }

    public /* synthetic */ MutableLongLongMap(int i, int i2, DefaultConstructorMarker defaultConstructorMarker) {
        this((i2 & 1) != 0 ? 6 : i);
    }

    public MutableLongLongMap(int initialCapacity) {
        super(null);
        boolean value$iv = initialCapacity >= 0;
        if (!value$iv) {
            RuntimeHelpersKt.throwIllegalArgumentException("Capacity must be a positive value.");
        }
        initializeStorage(ScatterMapKt.unloadedCapacity(initialCapacity));
    }

    private final void initializeStorage(int initialCapacity) {
        int newCapacity;
        if (initialCapacity > 0) {
            newCapacity = Math.max(7, ScatterMapKt.normalizeCapacity(initialCapacity));
        } else {
            newCapacity = 0;
        }
        this._capacity = newCapacity;
        initializeMetadata(newCapacity);
        this.keys = new long[newCapacity];
        this.values = new long[newCapacity];
    }

    private final void initializeMetadata(int capacity) {
        long[] jArr;
        if (capacity == 0) {
            jArr = ScatterMapKt.EmptyGroup;
        } else {
            int size = ((((capacity + 1) + 7) + 7) & (-8)) >> 3;
            long[] $this$initializeMetadata_u24lambda_u241 = new long[size];
            ArraysKt.fill$default($this$initializeMetadata_u24lambda_u241, -9187201950435737472L, 0, 0, 6, (Object) null);
            jArr = $this$initializeMetadata_u24lambda_u241;
        }
        this.metadata = jArr;
        long[] data$iv = this.metadata;
        int i$iv = capacity >> 3;
        int b$iv = (capacity & 7) << 3;
        data$iv[i$iv] = (data$iv[i$iv] & (~(255 << b$iv))) | (255 << b$iv);
        initializeGrowth();
    }

    private final void initializeGrowth() {
        this.growthLimit = ScatterMapKt.loadedCapacity(get_capacity()) - this._size;
    }

    public final long getOrPut(long key, Function0<Long> defaultValue) {
        Intrinsics.checkNotNullParameter(defaultValue, "defaultValue");
        int index = findKeyIndex(key);
        if (index < 0) {
            long defValue = defaultValue.invoke().longValue();
            put(key, defValue);
            return defValue;
        }
        return this.values[index];
    }

    public final void set(long key, long value) {
        int index = findInsertIndex(key);
        if (index < 0) {
            index = ~index;
        }
        this.keys[index] = key;
        this.values[index] = value;
    }

    public final void put(long key, long value) {
        set(key, value);
    }

    public final long put(long key, long value, long r9) {
        int index = findInsertIndex(key);
        long previous = r9;
        if (index < 0) {
            index = ~index;
        } else {
            previous = this.values[index];
        }
        this.keys[index] = key;
        this.values[index] = value;
        return previous;
    }

    public final void putAll(LongLongMap from) {
        int $i$f$forEach;
        long[] k$iv;
        long[] v$iv;
        LongLongMap this_$iv$iv;
        int i;
        int $i$f$forEach2;
        long[] k$iv2;
        long[] v$iv2;
        LongLongMap this_$iv$iv2;
        Intrinsics.checkNotNullParameter(from, "from");
        LongLongMap this_$iv = from;
        int $i$f$forEach3 = 0;
        long[] k$iv3 = this_$iv.keys;
        long[] v$iv3 = this_$iv.values;
        LongLongMap this_$iv$iv3 = this_$iv;
        long[] m$iv$iv = this_$iv$iv3.metadata;
        int lastIndex$iv$iv = m$iv$iv.length - 2;
        int i$iv$iv = 0;
        if (0 > lastIndex$iv$iv) {
            return;
        }
        while (true) {
            long slot$iv$iv = m$iv$iv[i$iv$iv];
            LongLongMap this_$iv2 = this_$iv;
            if ((((~slot$iv$iv) << 7) & slot$iv$iv & (-9187201950435737472L)) == -9187201950435737472L) {
                $i$f$forEach = $i$f$forEach3;
                k$iv = k$iv3;
                v$iv = v$iv3;
                this_$iv$iv = this_$iv$iv3;
            } else {
                int i2 = 8;
                int bitCount$iv$iv = 8 - ((~(i$iv$iv - lastIndex$iv$iv)) >>> 31);
                int j$iv$iv = 0;
                while (j$iv$iv < bitCount$iv$iv) {
                    long value$iv$iv$iv = 255 & slot$iv$iv;
                    if (!(value$iv$iv$iv < 128)) {
                        i = i2;
                        $i$f$forEach2 = $i$f$forEach3;
                        k$iv2 = k$iv3;
                        v$iv2 = v$iv3;
                        this_$iv$iv2 = this_$iv$iv3;
                    } else {
                        int index$iv$iv = (i$iv$iv << 3) + j$iv$iv;
                        i = i2;
                        $i$f$forEach2 = $i$f$forEach3;
                        long key = k$iv3[index$iv$iv];
                        k$iv2 = k$iv3;
                        v$iv2 = v$iv3;
                        long value = v$iv2[index$iv$iv];
                        this_$iv$iv2 = this_$iv$iv3;
                        set(key, value);
                    }
                    slot$iv$iv >>= i;
                    j$iv$iv++;
                    $i$f$forEach3 = $i$f$forEach2;
                    i2 = i;
                    k$iv3 = k$iv2;
                    v$iv3 = v$iv2;
                    this_$iv$iv3 = this_$iv$iv2;
                }
                $i$f$forEach = $i$f$forEach3;
                k$iv = k$iv3;
                v$iv = v$iv3;
                this_$iv$iv = this_$iv$iv3;
                if (bitCount$iv$iv != i2) {
                    return;
                }
            }
            if (i$iv$iv == lastIndex$iv$iv) {
                return;
            }
            i$iv$iv++;
            this_$iv = this_$iv2;
            $i$f$forEach3 = $i$f$forEach;
            k$iv3 = k$iv;
            v$iv3 = v$iv;
            this_$iv$iv3 = this_$iv$iv;
        }
    }

    public final void plusAssign(LongLongMap from) {
        Intrinsics.checkNotNullParameter(from, "from");
        putAll(from);
    }

    public final void remove(long key) {
        int index = findKeyIndex(key);
        if (index >= 0) {
            removeValueAt(index);
        }
    }

    public final boolean remove(long key, long value) {
        int index = findKeyIndex(key);
        if (index >= 0 && this.values[index] == value) {
            removeValueAt(index);
            return true;
        }
        return false;
    }

    public final void removeIf(Function2<? super Long, ? super Long, Boolean> predicate) {
        int $i$f$removeIf;
        int $i$f$removeIf2;
        int i;
        Intrinsics.checkNotNullParameter(predicate, "predicate");
        int $i$f$removeIf3 = 0;
        MutableLongLongMap this_$iv = this;
        long[] m$iv = this_$iv.metadata;
        int lastIndex$iv = m$iv.length - 2;
        int i$iv = 0;
        if (0 > lastIndex$iv) {
            return;
        }
        while (true) {
            long slot$iv = m$iv[i$iv];
            long $this$maskEmptyOrDeleted$iv$iv = ((~slot$iv) << 7) & slot$iv & (-9187201950435737472L);
            if ($this$maskEmptyOrDeleted$iv$iv == -9187201950435737472L) {
                $i$f$removeIf = $i$f$removeIf3;
            } else {
                int i2 = 8;
                int bitCount$iv = 8 - ((~(i$iv - lastIndex$iv)) >>> 31);
                int j$iv = 0;
                while (j$iv < bitCount$iv) {
                    long value$iv$iv = 255 & slot$iv;
                    if (!(value$iv$iv < 128)) {
                        $i$f$removeIf2 = $i$f$removeIf3;
                        i = i2;
                    } else {
                        int index$iv = (i$iv << 3) + j$iv;
                        i = i2;
                        $i$f$removeIf2 = $i$f$removeIf3;
                        if (predicate.invoke(Long.valueOf(this.keys[index$iv]), Long.valueOf(this.values[index$iv])).booleanValue()) {
                            removeValueAt(index$iv);
                        }
                    }
                    slot$iv >>= i;
                    j$iv++;
                    i2 = i;
                    $i$f$removeIf3 = $i$f$removeIf2;
                }
                $i$f$removeIf = $i$f$removeIf3;
                if (bitCount$iv != i2) {
                    return;
                }
            }
            if (i$iv == lastIndex$iv) {
                return;
            }
            i$iv++;
            $i$f$removeIf3 = $i$f$removeIf;
        }
    }

    public final void minusAssign(long key) {
        remove(key);
    }

    public final void minusAssign(long[] keys) {
        Intrinsics.checkNotNullParameter(keys, "keys");
        for (long key : keys) {
            remove(key);
        }
    }

    public final void minusAssign(LongSet keys) {
        LongSet this_$iv;
        int $i$f$forEach;
        int i;
        LongSet this_$iv2;
        int $i$f$forEach2;
        Intrinsics.checkNotNullParameter(keys, "keys");
        int $i$f$minusAssign = 0;
        LongSet this_$iv3 = keys;
        int $i$f$forEach3 = 0;
        long[] k$iv = this_$iv3.elements;
        long[] m$iv$iv = this_$iv3.metadata;
        int lastIndex$iv$iv = m$iv$iv.length - 2;
        int i$iv$iv = 0;
        if (0 > lastIndex$iv$iv) {
            return;
        }
        while (true) {
            long slot$iv$iv = m$iv$iv[i$iv$iv];
            int $i$f$minusAssign2 = $i$f$minusAssign;
            if ((((~slot$iv$iv) << 7) & slot$iv$iv & (-9187201950435737472L)) == -9187201950435737472L) {
                this_$iv = this_$iv3;
                $i$f$forEach = $i$f$forEach3;
            } else {
                int i2 = 8;
                int bitCount$iv$iv = 8 - ((~(i$iv$iv - lastIndex$iv$iv)) >>> 31);
                int j$iv$iv = 0;
                while (j$iv$iv < bitCount$iv$iv) {
                    long value$iv$iv$iv = 255 & slot$iv$iv;
                    if (!(value$iv$iv$iv < 128)) {
                        i = i2;
                        this_$iv2 = this_$iv3;
                        $i$f$forEach2 = $i$f$forEach3;
                    } else {
                        int index$iv$iv = (i$iv$iv << 3) + j$iv$iv;
                        i = i2;
                        this_$iv2 = this_$iv3;
                        long key = k$iv[index$iv$iv];
                        $i$f$forEach2 = $i$f$forEach3;
                        remove(key);
                    }
                    slot$iv$iv >>= i;
                    j$iv$iv++;
                    this_$iv3 = this_$iv2;
                    i2 = i;
                    $i$f$forEach3 = $i$f$forEach2;
                }
                this_$iv = this_$iv3;
                $i$f$forEach = $i$f$forEach3;
                if (bitCount$iv$iv != i2) {
                    return;
                }
            }
            if (i$iv$iv == lastIndex$iv$iv) {
                return;
            }
            i$iv$iv++;
            $i$f$minusAssign = $i$f$minusAssign2;
            this_$iv3 = this_$iv;
            $i$f$forEach3 = $i$f$forEach;
        }
    }

    public final void minusAssign(LongList keys) {
        Intrinsics.checkNotNullParameter(keys, "keys");
        long[] content$iv = keys.content;
        int i = keys._size;
        for (int i$iv = 0; i$iv < i; i$iv++) {
            long key = content$iv[i$iv];
            remove(key);
        }
    }

    public final void removeValueAt(int index) {
        this._size--;
        long[] data$iv = this.metadata;
        int capacity$iv = this._capacity;
        int i$iv$iv = index >> 3;
        int b$iv$iv = (index & 7) << 3;
        data$iv[i$iv$iv] = (data$iv[i$iv$iv] & (~(255 << b$iv$iv))) | (254 << b$iv$iv);
        int $i$f$writeRawMetadata = index - 7;
        int cloneIndex$iv = ($i$f$writeRawMetadata & capacity$iv) + (capacity$iv & 7);
        data$iv[cloneIndex$iv >> 3] = data$iv[index >> 3];
    }

    public final void clear() {
        this._size = 0;
        if (this.metadata != ScatterMapKt.EmptyGroup) {
            ArraysKt.fill$default(this.metadata, -9187201950435737472L, 0, 0, 6, (Object) null);
            long[] data$iv = this.metadata;
            int offset$iv = this._capacity;
            int i$iv = offset$iv >> 3;
            int b$iv = (offset$iv & 7) << 3;
            data$iv[i$iv] = (data$iv[i$iv] & (~(255 << b$iv))) | (255 << b$iv);
        }
        initializeGrowth();
    }

    /* JADX WARN: Code restructure failed: missing block: B:14:0x0088, code lost:
    
        r7 = (((~r4) << 6) & r4) & (-9187201950435737472L);
     */
    /* JADX WARN: Code restructure failed: missing block: B:15:0x0095, code lost:
    
        if (r7 == 0) goto L28;
     */
    /* JADX WARN: Code restructure failed: missing block: B:18:0x0098, code lost:
    
        r4 = findFirstAvailableSlot(r2);
     */
    /* JADX WARN: Code restructure failed: missing block: B:19:0x00a0, code lost:
    
        if (r23.growthLimit != 0) goto L23;
     */
    /* JADX WARN: Code restructure failed: missing block: B:21:0x00b5, code lost:
    
        if (((r23.metadata[r4 >> 3] >> ((r4 & 7) << 3)) & 255) != 254) goto L20;
     */
    /* JADX WARN: Code restructure failed: missing block: B:22:0x00b7, code lost:
    
        r5 = 1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:23:0x00bc, code lost:
    
        if (r5 != null) goto L23;
     */
    /* JADX WARN: Code restructure failed: missing block: B:24:0x00be, code lost:
    
        adjustStorage();
        r4 = findFirstAvailableSlot(r2);
     */
    /* JADX WARN: Code restructure failed: missing block: B:25:0x00ba, code lost:
    
        r5 = null;
     */
    /* JADX WARN: Code restructure failed: missing block: B:26:0x00c5, code lost:
    
        r23._size++;
        r5 = r23.growthLimit;
     */
    /* JADX WARN: Code restructure failed: missing block: B:27:0x00e0, code lost:
    
        if (((r23.metadata[r4 >> 3] >> ((r4 & 7) << 3)) & 255) != 128) goto L26;
     */
    /* JADX WARN: Code restructure failed: missing block: B:28:0x00e2, code lost:
    
        r19 = 1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:29:0x00e4, code lost:
    
        r23.growthLimit = r5 - r19;
        r5 = r23.metadata;
        r9 = r23._capacity;
        r10 = r3;
        r14 = r4 >> 3;
        r17 = (r4 & 7) << 3;
        r5[r14] = (r5[r14] & (~(255 << r17))) | (r10 << r17);
        r7 = ((r4 - 7) & r9) + (r9 & 7);
        r5[r7 >> 3] = r5[r4 >> 3];
     */
    /* JADX WARN: Code restructure failed: missing block: B:30:0x0114, code lost:
    
        return ~r4;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private final int findInsertIndex(long r24) {
        /*
            Method dump skipped, instructions count: 287
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.collection.MutableLongLongMap.findInsertIndex(long):int");
    }

    private final int findFirstAvailableSlot(int hash1) {
        int probeMask = this._capacity;
        int probeOffset = hash1 & probeMask;
        int probeIndex = 0;
        while (true) {
            long[] metadata$iv = this.metadata;
            int i$iv = probeOffset >> 3;
            int b$iv = (probeOffset & 7) << 3;
            long g = (metadata$iv[i$iv] >>> b$iv) | ((metadata$iv[i$iv + 1] << (64 - b$iv)) & ((-b$iv) >> 63));
            long $this$maskEmptyOrDeleted$iv = ((~g) << 7) & g & (-9187201950435737472L);
            if ($this$maskEmptyOrDeleted$iv != 0) {
                return ((Long.numberOfTrailingZeros($this$maskEmptyOrDeleted$iv) >> 3) + probeOffset) & probeMask;
            }
            probeIndex += 8;
            probeOffset = (probeOffset + probeIndex) & probeMask;
        }
    }

    public final int trim() {
        int previousCapacity = this._capacity;
        int newCapacity = ScatterMapKt.normalizeCapacity(ScatterMapKt.unloadedCapacity(this._size));
        if (newCapacity < previousCapacity) {
            resizeStorage(newCapacity);
            return previousCapacity - this._capacity;
        }
        return 0;
    }

    private final void adjustStorage() {
        int compare;
        if (this._capacity > 8) {
            compare = Long.compare(ULong.m7496constructorimpl(ULong.m7496constructorimpl(this._size) * 32) ^ Long.MIN_VALUE, ULong.m7496constructorimpl(ULong.m7496constructorimpl(this._capacity) * 25) ^ Long.MIN_VALUE);
            if (compare <= 0) {
                dropDeletes();
                return;
            }
        }
        resizeStorage(ScatterMapKt.nextCapacity(this._capacity));
    }

    private final void dropDeletes() {
        long[] metadata = this.metadata;
        int capacity = this._capacity;
        long[] keys = this.keys;
        long[] values = this.values;
        ScatterMapKt.convertMetadataForCleanup(metadata, capacity);
        int swapIndex = -1;
        int index = 0;
        while (index != capacity) {
            long m = (metadata[index >> 3] >> ((index & 7) << 3)) & 255;
            if (m == 128) {
                swapIndex = index;
                index++;
            } else if (m != 254) {
                index++;
            } else {
                long k$iv = keys[index];
                int hash$iv = Long.hashCode(k$iv) * ScatterMapKt.MurmurHashC1;
                int $i$f$hash = hash$iv ^ (hash$iv << 16);
                int $i$f$h1 = $i$f$hash >>> 7;
                int targetIndex = findFirstAvailableSlot($i$f$h1);
                int probeOffset = $i$f$h1 & capacity;
                int newProbeIndex = ((targetIndex - probeOffset) & capacity) / 8;
                int oldProbeIndex = ((index - probeOffset) & capacity) / 8;
                if (newProbeIndex != oldProbeIndex) {
                    long[] keys2 = keys;
                    long[] values2 = values;
                    if (((metadata[targetIndex >> 3] >> ((targetIndex & 7) << 3)) & 255) == 128) {
                        int $i$f$h2 = $i$f$hash & 127;
                        long value$iv = $i$f$h2;
                        int i$iv = targetIndex >> 3;
                        int b$iv = (targetIndex & 7) << 3;
                        metadata[i$iv] = (metadata[i$iv] & (~(255 << b$iv))) | (value$iv << b$iv);
                        int i$iv2 = index >> 3;
                        int b$iv2 = (index & 7) << 3;
                        long value$iv2 = 255 << b$iv2;
                        metadata[i$iv2] = (metadata[i$iv2] & (~value$iv2)) | (128 << b$iv2);
                        keys2[targetIndex] = keys2[index];
                        keys2[index] = 0;
                        values2[targetIndex] = values2[index];
                        values2[index] = 0;
                        swapIndex = index;
                    } else {
                        int $i$f$h22 = $i$f$hash & 127;
                        long value$iv3 = $i$f$h22;
                        int i$iv3 = targetIndex >> 3;
                        int b$iv3 = (targetIndex & 7) << 3;
                        metadata[i$iv3] = (metadata[i$iv3] & (~(255 << b$iv3))) | (value$iv3 << b$iv3);
                        if (swapIndex == -1) {
                            swapIndex = ScatterMapKt.findEmptySlot(metadata, index + 1, capacity);
                        }
                        keys2[swapIndex] = keys2[targetIndex];
                        keys2[targetIndex] = keys2[index];
                        keys2[index] = keys2[swapIndex];
                        values2[swapIndex] = values2[targetIndex];
                        values2[targetIndex] = values2[index];
                        values2[index] = values2[swapIndex];
                        index--;
                    }
                    metadata[ArraysKt.getLastIndex(metadata)] = (metadata[0] & 72057594037927935L) | Long.MIN_VALUE;
                    index++;
                    keys = keys2;
                    values = values2;
                } else {
                    int $i$f$h23 = $i$f$hash & 127;
                    long[] keys3 = keys;
                    long value$iv4 = $i$f$h23;
                    int i$iv4 = index >> 3;
                    int b$iv4 = (index & 7) << 3;
                    long value$iv5 = 255 << b$iv4;
                    metadata[i$iv4] = (metadata[i$iv4] & (~value$iv5)) | (value$iv4 << b$iv4);
                    metadata[ArraysKt.getLastIndex(metadata)] = (metadata[0] & 72057594037927935L) | Long.MIN_VALUE;
                    index++;
                    keys = keys3;
                    values = values;
                }
            }
        }
        initializeGrowth();
    }

    private final void resizeStorage(int newCapacity) {
        long[] previousMetadata;
        MutableLongLongMap mutableLongLongMap = this;
        long[] previousMetadata2 = mutableLongLongMap.metadata;
        long[] previousKeys = mutableLongLongMap.keys;
        long[] previousValues = mutableLongLongMap.values;
        int previousCapacity = mutableLongLongMap._capacity;
        initializeStorage(newCapacity);
        long[] newMetadata = mutableLongLongMap.metadata;
        long[] newKeys = mutableLongLongMap.keys;
        long[] newValues = mutableLongLongMap.values;
        int capacity = mutableLongLongMap._capacity;
        int i = 0;
        while (i < previousCapacity) {
            if (!(((previousMetadata2[i >> 3] >> ((i & 7) << 3)) & 255) < 128)) {
                previousMetadata = previousMetadata2;
            } else {
                long previousKey = previousKeys[i];
                int hash$iv = Long.hashCode(previousKey) * ScatterMapKt.MurmurHashC1;
                int $i$f$hash = hash$iv ^ (hash$iv << 16);
                int $i$f$h1 = $i$f$hash >>> 7;
                int index = mutableLongLongMap.findFirstAvailableSlot($i$f$h1);
                long value$iv = $i$f$hash & 127;
                int i$iv$iv = index >> 3;
                int b$iv$iv = (index & 7) << 3;
                previousMetadata = previousMetadata2;
                newMetadata[i$iv$iv] = (newMetadata[i$iv$iv] & (~(255 << b$iv$iv))) | (value$iv << b$iv$iv);
                int cloneIndex$iv = ((index - 7) & capacity) + (capacity & 7);
                newMetadata[cloneIndex$iv >> 3] = newMetadata[index >> 3];
                newKeys[index] = previousKey;
                newValues[index] = previousValues[i];
            }
            i++;
            mutableLongLongMap = this;
            previousMetadata2 = previousMetadata;
        }
    }

    private final void writeMetadata(int index, long value) {
        long[] m = this.metadata;
        int i$iv = index >> 3;
        int b$iv = (index & 7) << 3;
        m[i$iv] = (m[i$iv] & (~(255 << b$iv))) | (value << b$iv);
        int $i$f$writeRawMetadata = this._capacity;
        int cloneIndex = ((index - 7) & $i$f$writeRawMetadata) + ($i$f$writeRawMetadata & 7);
        int i$iv2 = cloneIndex >> 3;
        int b$iv2 = (cloneIndex & 7) << 3;
        m[i$iv2] = ((~(255 << b$iv2)) & m[i$iv2]) | (value << b$iv2);
    }
}
