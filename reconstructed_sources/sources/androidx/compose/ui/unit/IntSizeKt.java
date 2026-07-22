package androidx.compose.ui.unit;

import androidx.compose.ui.geometry.Size;
import androidx.compose.ui.geometry.SizeKt;
import io.ktor.http.ContentDisposition;
import kotlin.Metadata;

/* compiled from: IntSize.kt */
@Metadata(d1 = {"\u0000&\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0007\u001a\u001d\u0010\u0007\u001a\u00020\u00022\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\tH\u0007¢\u0006\u0002\u0010\u000b\u001a\u0016\u0010\f\u001a\u00020\u0002*\u00020\rH\u0007ø\u0001\u0000¢\u0006\u0004\b\u000e\u0010\u0006\u001a\u001f\u0010\u000f\u001a\u00020\u0002*\u00020\t2\u0006\u0010\u0010\u001a\u00020\u0002H\u0087\nø\u0001\u0000¢\u0006\u0004\b\u0011\u0010\u0012\u001a\u0016\u0010\u0013\u001a\u00020\u0014*\u00020\u0002H\u0007ø\u0001\u0000¢\u0006\u0004\b\u0015\u0010\u0016\u001a\u0016\u0010\u0017\u001a\u00020\u0002*\u00020\rH\u0007ø\u0001\u0000¢\u0006\u0004\b\u0018\u0010\u0006\u001a\u0016\u0010\u0019\u001a\u00020\r*\u00020\u0002H\u0007ø\u0001\u0000¢\u0006\u0004\b\u001a\u0010\u0006\"\u001e\u0010\u0000\u001a\u00020\u0001*\u00020\u00028FX\u0087\u0004¢\u0006\f\u0012\u0004\b\u0003\u0010\u0004\u001a\u0004\b\u0005\u0010\u0006\u0082\u0002\u0007\n\u0005\b¡\u001e0\u0001¨\u0006\u001b"}, d2 = {"center", "Landroidx/compose/ui/unit/IntOffset;", "Landroidx/compose/ui/unit/IntSize;", "getCenter-ozmzZPI$annotations", "(J)V", "getCenter-ozmzZPI", "(J)J", "IntSize", "width", "", "height", "(II)J", "roundToIntSize", "Landroidx/compose/ui/geometry/Size;", "roundToIntSize-uvyYCjk", "times", ContentDisposition.Parameters.Size, "times-O0kMr_c", "(IJ)J", "toIntRect", "Landroidx/compose/ui/unit/IntRect;", "toIntRect-ozmzZPI", "(J)Landroidx/compose/ui/unit/IntRect;", "toIntSize", "toIntSize-uvyYCjk", "toSize", "toSize-ozmzZPI", "ui-unit_release"}, k = 2, mv = {1, 8, 0}, xi = 48)
/* loaded from: classes10.dex */
public final class IntSizeKt {
    /* renamed from: getCenter-ozmzZPI$annotations, reason: not valid java name */
    public static /* synthetic */ void m6799getCenterozmzZPI$annotations(long j) {
    }

    public static final long IntSize(int width, int height) {
        return IntSize.m6787constructorimpl((width << 32) | (height & 4294967295L));
    }

    /* renamed from: times-O0kMr_c, reason: not valid java name */
    public static final long m6801timesO0kMr_c(int $this$times_u2dO0kMr_c, long size) {
        return IntSize.m6794timesYEO4UFw(size, $this$times_u2dO0kMr_c);
    }

    /* renamed from: toIntRect-ozmzZPI, reason: not valid java name */
    public static final IntRect m6802toIntRectozmzZPI(long $this$toIntRect_u2dozmzZPI) {
        return IntRectKt.m6782IntRectVbeCjmY(IntOffset.INSTANCE.m6760getZeronOccac(), $this$toIntRect_u2dozmzZPI);
    }

    /* renamed from: getCenter-ozmzZPI, reason: not valid java name */
    public static final long m6798getCenterozmzZPI(long $this$center) {
        return IntOffset.m6744constructorimpl((($this$center >> 33) << 32) | ((($this$center << 32) >> 33) & 4294967295L));
    }

    /* renamed from: toSize-ozmzZPI, reason: not valid java name */
    public static final long m6804toSizeozmzZPI(long $this$toSize_u2dozmzZPI) {
        return SizeKt.Size(IntSize.m6792getWidthimpl($this$toSize_u2dozmzZPI), IntSize.m6791getHeightimpl($this$toSize_u2dozmzZPI));
    }

    /* renamed from: toIntSize-uvyYCjk, reason: not valid java name */
    public static final long m6803toIntSizeuvyYCjk(long $this$toIntSize_u2duvyYCjk) {
        int val1$iv = (int) Size.m3987getWidthimpl($this$toIntSize_u2duvyYCjk);
        int val2$iv = (int) Size.m3984getHeightimpl($this$toIntSize_u2duvyYCjk);
        return IntSize.m6787constructorimpl((val1$iv << 32) | (val2$iv & 4294967295L));
    }

    /* renamed from: roundToIntSize-uvyYCjk, reason: not valid java name */
    public static final long m6800roundToIntSizeuvyYCjk(long $this$roundToIntSize_u2duvyYCjk) {
        float $this$fastRoundToInt$iv = Size.m3987getWidthimpl($this$roundToIntSize_u2duvyYCjk);
        int val1$iv = Math.round($this$fastRoundToInt$iv);
        float $this$fastRoundToInt$iv2 = Size.m3984getHeightimpl($this$roundToIntSize_u2duvyYCjk);
        int val2$iv = Math.round($this$fastRoundToInt$iv2);
        return IntSize.m6787constructorimpl((val1$iv << 32) | (val2$iv & 4294967295L));
    }
}
