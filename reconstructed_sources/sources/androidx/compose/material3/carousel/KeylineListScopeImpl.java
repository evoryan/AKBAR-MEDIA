package androidx.compose.material3.carousel;

import io.ktor.http.ContentDisposition;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import kotlin.Metadata;
import kotlin.collections.CollectionsKt;
import kotlin.collections.IntIterator;
import kotlin.ranges.RangesKt;
import org.apache.commons.beanutils.PropertyUtils;

/* compiled from: KeylineList.kt */
@Metadata(d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0007\n\u0002\b\u0003\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\t\b\u0002\u0018\u00002\u00020\u0001:\u0001#B\u0005¢\u0006\u0002\u0010\u0002J\u0018\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u00062\u0006\u0010\u000f\u001a\u00020\u0010H\u0016JT\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00130\u00122\u0006\u0010\u0007\u001a\u00020\u00042\u0006\u0010\b\u001a\u00020\u00062\u0006\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0014\u001a\u00020\u00042\u0006\u0010\u0015\u001a\u00020\u00062\u0006\u0010\u0016\u001a\u00020\u00062\u0006\u0010\u0017\u001a\u00020\u00062\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0012H\u0002J(\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u0016\u001a\u00020\u00062\u0006\u0010\u0017\u001a\u00020\u00062\u0006\u0010\u001a\u001a\u00020\u001bø\u0001\u0000¢\u0006\u0004\b\u001c\u0010\u001dJ&\u0010\u001e\u001a\u00020\u00192\u0006\u0010\u0016\u001a\u00020\u00062\u0006\u0010\u0017\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\u00042\u0006\u0010\b\u001a\u00020\u0006J\b\u0010\u001f\u001a\u00020\u0004H\u0002J\u0018\u0010 \u001a\u00020\u00102\u0006\u0010\u000e\u001a\u00020\u00062\u0006\u0010!\u001a\u00020\u0006H\u0002J \u0010\"\u001a\u00020\u00102\u0006\u0010\u000e\u001a\u00020\u00062\u0006\u0010!\u001a\u00020\u00062\u0006\u0010\u0016\u001a\u00020\u0006H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0006X\u0082\u000e¢\u0006\u0002\n\u0000R\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\nX\u0082\u0004¢\u0006\u0002\n\u0000\u0082\u0002\u0007\n\u0005\b¡\u001e0\u0001¨\u0006$"}, d2 = {"Landroidx/compose/material3/carousel/KeylineListScopeImpl;", "Landroidx/compose/material3/carousel/KeylineListScope;", "()V", "firstFocalIndex", "", "focalItemSize", "", "pivotIndex", "pivotOffset", "tmpKeylines", "", "Landroidx/compose/material3/carousel/KeylineListScopeImpl$TmpKeyline;", "add", "", ContentDisposition.Parameters.Size, "isAnchor", "", "createKeylinesWithPivot", "", "Landroidx/compose/material3/carousel/Keyline;", "lastFocalIndex", "itemMainAxisSize", "carouselMainAxisSize", "itemSpacing", "createWithAlignment", "Landroidx/compose/material3/carousel/KeylineList;", "carouselAlignment", "Landroidx/compose/material3/carousel/CarouselAlignment;", "createWithAlignment-waks0t8", "(FFI)Landroidx/compose/material3/carousel/KeylineList;", "createWithPivot", "findLastFocalIndex", "isCutoffLeft", "offset", "isCutoffRight", "TmpKeyline", "material3_release"}, k = 1, mv = {1, 8, 0}, xi = 48)
/* loaded from: classes.dex */
final class KeylineListScopeImpl implements KeylineListScope {
    private float focalItemSize;
    private float pivotOffset;
    private int firstFocalIndex = -1;
    private int pivotIndex = -1;
    private final List<TmpKeyline> tmpKeylines = new ArrayList();

    /* JADX INFO: Access modifiers changed from: private */
    /* compiled from: KeylineList.kt */
    @Metadata(d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0007\n\u0000\n\u0002\u0010\u000b\n\u0002\b\n\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0082\b\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0002\u0010\u0006J\t\u0010\n\u001a\u00020\u0003HÆ\u0003J\t\u0010\u000b\u001a\u00020\u0005HÆ\u0003J\u001d\u0010\f\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0005HÆ\u0001J\u0013\u0010\r\u001a\u00020\u00052\b\u0010\u000e\u001a\u0004\u0018\u00010\u0001HÖ\u0003J\t\u0010\u000f\u001a\u00020\u0010HÖ\u0001J\t\u0010\u0011\u001a\u00020\u0012HÖ\u0001R\u0011\u0010\u0004\u001a\u00020\u0005¢\u0006\b\n\u0000\u001a\u0004\b\u0004\u0010\u0007R\u0011\u0010\u0002\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\b\u0010\t¨\u0006\u0013"}, d2 = {"Landroidx/compose/material3/carousel/KeylineListScopeImpl$TmpKeyline;", "", ContentDisposition.Parameters.Size, "", "isAnchor", "", "(FZ)V", "()Z", "getSize", "()F", "component1", "component2", "copy", "equals", "other", "hashCode", "", "toString", "", "material3_release"}, k = 1, mv = {1, 8, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* data */ class TmpKeyline {
        private final boolean isAnchor;
        private final float size;

        public static /* synthetic */ TmpKeyline copy$default(TmpKeyline tmpKeyline, float f, boolean z, int i, Object obj) {
            if ((i & 1) != 0) {
                f = tmpKeyline.size;
            }
            if ((i & 2) != 0) {
                z = tmpKeyline.isAnchor;
            }
            return tmpKeyline.copy(f, z);
        }

        /* renamed from: component1, reason: from getter */
        public final float getSize() {
            return this.size;
        }

        /* renamed from: component2, reason: from getter */
        public final boolean getIsAnchor() {
            return this.isAnchor;
        }

        public final TmpKeyline copy(float size, boolean isAnchor) {
            return new TmpKeyline(size, isAnchor);
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof TmpKeyline)) {
                return false;
            }
            TmpKeyline tmpKeyline = (TmpKeyline) other;
            return Float.compare(this.size, tmpKeyline.size) == 0 && this.isAnchor == tmpKeyline.isAnchor;
        }

        public int hashCode() {
            return (Float.hashCode(this.size) * 31) + Boolean.hashCode(this.isAnchor);
        }

        public String toString() {
            return "TmpKeyline(size=" + this.size + ", isAnchor=" + this.isAnchor + PropertyUtils.MAPPED_DELIM2;
        }

        public TmpKeyline(float size, boolean isAnchor) {
            this.size = size;
            this.isAnchor = isAnchor;
        }

        public final float getSize() {
            return this.size;
        }

        public final boolean isAnchor() {
            return this.isAnchor;
        }
    }

    @Override // androidx.compose.material3.carousel.KeylineListScope
    public void add(float size, boolean isAnchor) {
        this.tmpKeylines.add(new TmpKeyline(size, isAnchor));
        if (size > this.focalItemSize) {
            this.firstFocalIndex = CollectionsKt.getLastIndex(this.tmpKeylines);
            this.focalItemSize = size;
        }
    }

    public final KeylineList createWithPivot(float carouselMainAxisSize, float itemSpacing, int pivotIndex, float pivotOffset) {
        List keylines = createKeylinesWithPivot(pivotIndex, pivotOffset, this.firstFocalIndex, findLastFocalIndex(), this.focalItemSize, carouselMainAxisSize, itemSpacing, this.tmpKeylines);
        return new KeylineList(keylines);
    }

    /* renamed from: createWithAlignment-waks0t8, reason: not valid java name */
    public final KeylineList m2888createWithAlignmentwaks0t8(float carouselMainAxisSize, float itemSpacing, int carouselAlignment) {
        float f;
        int lastFocalIndex = findLastFocalIndex();
        int focalItemCount = lastFocalIndex - this.firstFocalIndex;
        this.pivotIndex = this.firstFocalIndex;
        if (CarouselAlignment.m2872equalsimpl0(carouselAlignment, CarouselAlignment.INSTANCE.m2876getCenterNUL3oTo())) {
            float itemSpacingSplit = 0.0f;
            if (!(itemSpacing == 0.0f)) {
                int i = focalItemCount % 2;
                if (i + ((((i ^ 2) & ((-i) | i)) >> 31) & 2) != 0) {
                    itemSpacingSplit = itemSpacing / 2.0f;
                }
            }
            f = ((carouselMainAxisSize / 2.0f) - ((this.focalItemSize / 2.0f) * focalItemCount)) - itemSpacingSplit;
        } else {
            boolean m2872equalsimpl0 = CarouselAlignment.m2872equalsimpl0(carouselAlignment, CarouselAlignment.INSTANCE.m2877getEndNUL3oTo());
            float f2 = this.focalItemSize;
            f = m2872equalsimpl0 ? carouselMainAxisSize - (f2 / 2.0f) : f2 / 2.0f;
        }
        this.pivotOffset = f;
        List keylines = createKeylinesWithPivot(this.pivotIndex, this.pivotOffset, this.firstFocalIndex, lastFocalIndex, this.focalItemSize, carouselMainAxisSize, itemSpacing, this.tmpKeylines);
        return new KeylineList(keylines);
    }

    private final int findLastFocalIndex() {
        int lastFocalIndex = this.firstFocalIndex;
        while (lastFocalIndex < CollectionsKt.getLastIndex(this.tmpKeylines)) {
            if (!(this.tmpKeylines.get(lastFocalIndex + 1).getSize() == this.focalItemSize)) {
                break;
            }
            lastFocalIndex++;
        }
        return lastFocalIndex;
    }

    private final List<Keyline> createKeylinesWithPivot(int pivotIndex, float pivotOffset, int firstFocalIndex, int lastFocalIndex, float itemMainAxisSize, float carouselMainAxisSize, float itemSpacing, List<TmpKeyline> tmpKeylines) {
        float pivotCutoff;
        float cutoff;
        TmpKeyline pivot = tmpKeylines.get(pivotIndex);
        List keylines = new ArrayList();
        if (isCutoffLeft(pivot.getSize(), pivotOffset)) {
            pivotCutoff = pivotOffset - (pivot.getSize() / 2.0f);
        } else if (isCutoffRight(pivot.getSize(), pivotOffset, carouselMainAxisSize)) {
            pivotCutoff = ((pivot.getSize() / 2.0f) + pivotOffset) - carouselMainAxisSize;
        } else {
            pivotCutoff = 0.0f;
        }
        Iterable iterable = null;
        keylines.add(new Keyline(pivot.getSize(), pivotOffset, pivotOffset, firstFocalIndex <= pivotIndex && pivotIndex <= lastFocalIndex, pivot.isAnchor(), true, pivotCutoff));
        float offset = (pivotOffset - (itemMainAxisSize / 2.0f)) - itemSpacing;
        float unadjustedOffset = (pivotOffset - (itemMainAxisSize / 2.0f)) - itemSpacing;
        Iterable $this$forEach$iv = RangesKt.downTo(pivotIndex - 1, 0);
        Iterator<Integer> it = $this$forEach$iv.iterator();
        while (it.hasNext()) {
            int element$iv = ((IntIterator) it).nextInt();
            TmpKeyline tmp = tmpKeylines.get(element$iv);
            float tmpOffset = offset - (tmp.getSize() / 2.0f);
            float tmpUnadjustedOffset = unadjustedOffset - (itemMainAxisSize / 2.0f);
            Iterable $this$forEach$iv2 = $this$forEach$iv;
            float cutoff2 = isCutoffLeft(tmp.getSize(), tmpOffset) ? Math.abs(tmpOffset - (tmp.getSize() / 2.0f)) : 0.0f;
            keylines.add(0, new Keyline(tmp.getSize(), tmpOffset, tmpUnadjustedOffset, firstFocalIndex <= element$iv && element$iv <= lastFocalIndex, tmp.isAnchor(), false, cutoff2));
            offset -= tmp.getSize() + itemSpacing;
            unadjustedOffset -= itemMainAxisSize + itemSpacing;
            iterable = null;
            $this$forEach$iv = $this$forEach$iv2;
        }
        float offset2 = pivotOffset + (itemMainAxisSize / 2.0f) + itemSpacing;
        float offset3 = itemMainAxisSize / 2.0f;
        float unadjustedOffset2 = pivotOffset + offset3 + itemSpacing;
        Iterator<Integer> it2 = RangesKt.until(pivotIndex + 1, tmpKeylines.size()).iterator();
        while (it2.hasNext()) {
            int element$iv2 = ((IntIterator) it2).nextInt();
            TmpKeyline tmp2 = tmpKeylines.get(element$iv2);
            float tmpOffset2 = offset2 + (tmp2.getSize() / 2.0f);
            float tmpUnadjustedOffset2 = unadjustedOffset2 + (itemMainAxisSize / 2.0f);
            float offset4 = offset2;
            float offset5 = tmp2.getSize();
            if (isCutoffRight(offset5, tmpOffset2, carouselMainAxisSize)) {
                cutoff = ((tmp2.getSize() / 2.0f) + tmpOffset2) - carouselMainAxisSize;
            } else {
                cutoff = 0.0f;
            }
            keylines.add(new Keyline(tmp2.getSize(), tmpOffset2, tmpUnadjustedOffset2, firstFocalIndex <= element$iv2 && element$iv2 <= lastFocalIndex, tmp2.isAnchor(), false, cutoff));
            offset2 = offset4 + tmp2.getSize() + itemSpacing;
            unadjustedOffset2 += itemMainAxisSize + itemSpacing;
        }
        return keylines;
    }

    private final boolean isCutoffLeft(float size, float offset) {
        return offset - (size / 2.0f) < 0.0f && (size / 2.0f) + offset > 0.0f;
    }

    private final boolean isCutoffRight(float size, float offset, float carouselMainAxisSize) {
        return offset - (size / 2.0f) < carouselMainAxisSize && (size / 2.0f) + offset > carouselMainAxisSize;
    }
}
