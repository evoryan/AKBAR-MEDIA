package androidx.compose.material3;

import androidx.compose.ui.geometry.Offset;
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection;
import com.google.firebase.messaging.Constants;
import kotlin.Metadata;

/* compiled from: AppBar.kt */
@Metadata(d1 = {"\u0000!\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006*\u0001\u0000\b\n\u0018\u00002\u00020\u0001J#\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00032\u0006\u0010\u0005\u001a\u00020\u0003H\u0096@ø\u0001\u0000¢\u0006\u0004\b\u0006\u0010\u0007J*\u0010\b\u001a\u00020\t2\u0006\u0010\u0004\u001a\u00020\t2\u0006\u0010\u0005\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000bH\u0016ø\u0001\u0000¢\u0006\u0004\b\f\u0010\rJ\"\u0010\u000e\u001a\u00020\t2\u0006\u0010\u0005\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000bH\u0016ø\u0001\u0000¢\u0006\u0004\b\u000f\u0010\u0010\u0082\u0002\u0007\n\u0005\b¡\u001e0\u0001¨\u0006\u0011"}, d2 = {"androidx/compose/material3/EnterAlwaysScrollBehavior$nestedScrollConnection$1", "Landroidx/compose/ui/input/nestedscroll/NestedScrollConnection;", "onPostFling", "Landroidx/compose/ui/unit/Velocity;", "consumed", "available", "onPostFling-RZ2iAVY", "(JJLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "onPostScroll", "Landroidx/compose/ui/geometry/Offset;", Constants.ScionAnalytics.PARAM_SOURCE, "Landroidx/compose/ui/input/nestedscroll/NestedScrollSource;", "onPostScroll-DzOQY0M", "(JJI)J", "onPreScroll", "onPreScroll-OzD1aCk", "(JI)J", "material3_release"}, k = 1, mv = {1, 8, 0}, xi = 48)
/* loaded from: classes.dex */
public final class EnterAlwaysScrollBehavior$nestedScrollConnection$1 implements NestedScrollConnection {
    final /* synthetic */ EnterAlwaysScrollBehavior this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    public EnterAlwaysScrollBehavior$nestedScrollConnection$1(EnterAlwaysScrollBehavior $receiver) {
        this.this$0 = $receiver;
    }

    @Override // androidx.compose.ui.input.nestedscroll.NestedScrollConnection
    /* renamed from: onPreScroll-OzD1aCk */
    public long mo746onPreScrollOzD1aCk(long available, int source) {
        if (!this.this$0.getCanScroll().invoke().booleanValue()) {
            return Offset.INSTANCE.m3934getZeroF1C5BW0();
        }
        float prevHeightOffset = this.this$0.getState().getHeightOffset();
        this.this$0.getState().setHeightOffset(this.this$0.getState().getHeightOffset() + Offset.m3919getYimpl(available));
        return !((prevHeightOffset > this.this$0.getState().getHeightOffset() ? 1 : (prevHeightOffset == this.this$0.getState().getHeightOffset() ? 0 : -1)) == 0) ? Offset.m3912copydBAh8RU$default(available, 0.0f, 0.0f, 2, null) : Offset.INSTANCE.m3934getZeroF1C5BW0();
    }

    /* JADX WARN: Code restructure failed: missing block: B:13:0x005c, code lost:
    
        if ((r5.this$0.getState().getHeightOffset() == r5.this$0.getState().getHeightOffsetLimit()) != false) goto L16;
     */
    @Override // androidx.compose.ui.input.nestedscroll.NestedScrollConnection
    /* renamed from: onPostScroll-DzOQY0M */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public long mo472onPostScrollDzOQY0M(long r6, long r8, int r10) {
        /*
            r5 = this;
            androidx.compose.material3.EnterAlwaysScrollBehavior r0 = r5.this$0
            kotlin.jvm.functions.Function0 r0 = r0.getCanScroll()
            java.lang.Object r0 = r0.invoke()
            java.lang.Boolean r0 = (java.lang.Boolean) r0
            boolean r0 = r0.booleanValue()
            if (r0 != 0) goto L19
            androidx.compose.ui.geometry.Offset$Companion r0 = androidx.compose.ui.geometry.Offset.INSTANCE
            long r0 = r0.m3934getZeroF1C5BW0()
            return r0
        L19:
            androidx.compose.material3.EnterAlwaysScrollBehavior r0 = r5.this$0
            androidx.compose.material3.TopAppBarState r0 = r0.getState()
            float r1 = r0.getContentOffset()
            float r2 = androidx.compose.ui.geometry.Offset.m3919getYimpl(r6)
            float r1 = r1 + r2
            r0.setContentOffset(r1)
            androidx.compose.material3.EnterAlwaysScrollBehavior r0 = r5.this$0
            androidx.compose.material3.TopAppBarState r0 = r0.getState()
            float r0 = r0.getHeightOffset()
            r1 = 0
            int r0 = (r0 > r1 ? 1 : (r0 == r1 ? 0 : -1))
            r2 = 1
            r3 = 0
            if (r0 != 0) goto L3e
            r0 = r2
            goto L3f
        L3e:
            r0 = r3
        L3f:
            if (r0 != 0) goto L5e
            androidx.compose.material3.EnterAlwaysScrollBehavior r0 = r5.this$0
            androidx.compose.material3.TopAppBarState r0 = r0.getState()
            float r0 = r0.getHeightOffset()
            androidx.compose.material3.EnterAlwaysScrollBehavior r4 = r5.this$0
            androidx.compose.material3.TopAppBarState r4 = r4.getState()
            float r4 = r4.getHeightOffsetLimit()
            int r0 = (r0 > r4 ? 1 : (r0 == r4 ? 0 : -1))
            if (r0 != 0) goto L5b
            r0 = r2
            goto L5c
        L5b:
            r0 = r3
        L5c:
            if (r0 == 0) goto L7b
        L5e:
            float r0 = androidx.compose.ui.geometry.Offset.m3919getYimpl(r6)
            int r0 = (r0 > r1 ? 1 : (r0 == r1 ? 0 : -1))
            if (r0 != 0) goto L67
            goto L68
        L67:
            r2 = r3
        L68:
            if (r2 == 0) goto L7b
            float r0 = androidx.compose.ui.geometry.Offset.m3919getYimpl(r8)
            int r0 = (r0 > r1 ? 1 : (r0 == r1 ? 0 : -1))
            if (r0 <= 0) goto L7b
            androidx.compose.material3.EnterAlwaysScrollBehavior r0 = r5.this$0
            androidx.compose.material3.TopAppBarState r0 = r0.getState()
            r0.setContentOffset(r1)
        L7b:
            androidx.compose.material3.EnterAlwaysScrollBehavior r0 = r5.this$0
            androidx.compose.material3.TopAppBarState r0 = r0.getState()
            androidx.compose.material3.EnterAlwaysScrollBehavior r1 = r5.this$0
            androidx.compose.material3.TopAppBarState r1 = r1.getState()
            float r1 = r1.getHeightOffset()
            float r2 = androidx.compose.ui.geometry.Offset.m3919getYimpl(r6)
            float r1 = r1 + r2
            r0.setHeightOffset(r1)
            androidx.compose.ui.geometry.Offset$Companion r0 = androidx.compose.ui.geometry.Offset.INSTANCE
            long r0 = r0.m3934getZeroF1C5BW0()
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.compose.material3.EnterAlwaysScrollBehavior$nestedScrollConnection$1.mo472onPostScrollDzOQY0M(long, long, int):long");
    }

    /* JADX WARN: Failed to find 'out' block for switch in B:7:0x0022. Please report as an issue. */
    /* JADX WARN: Removed duplicated region for block: B:11:0x002e  */
    /* JADX WARN: Removed duplicated region for block: B:14:0x0035  */
    /* JADX WARN: Removed duplicated region for block: B:17:0x0081 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:18:0x0041  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0025  */
    @Override // androidx.compose.ui.input.nestedscroll.NestedScrollConnection
    /* renamed from: onPostFling-RZ2iAVY */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public java.lang.Object mo471onPostFlingRZ2iAVY(long r9, long r11, kotlin.coroutines.Continuation<? super androidx.compose.ui.unit.Velocity> r13) {
        /*
            r8 = this;
            boolean r0 = r13 instanceof androidx.compose.material3.EnterAlwaysScrollBehavior$nestedScrollConnection$1$onPostFling$1
            if (r0 == 0) goto L14
            r0 = r13
            androidx.compose.material3.EnterAlwaysScrollBehavior$nestedScrollConnection$1$onPostFling$1 r0 = (androidx.compose.material3.EnterAlwaysScrollBehavior$nestedScrollConnection$1$onPostFling$1) r0
            int r1 = r0.label
            r2 = -2147483648(0xffffffff80000000, float:-0.0)
            r1 = r1 & r2
            if (r1 == 0) goto L14
            int r13 = r0.label
            int r13 = r13 - r2
            r0.label = r13
            goto L19
        L14:
            androidx.compose.material3.EnterAlwaysScrollBehavior$nestedScrollConnection$1$onPostFling$1 r0 = new androidx.compose.material3.EnterAlwaysScrollBehavior$nestedScrollConnection$1$onPostFling$1
            r0.<init>(r8, r13)
        L19:
            r6 = r0
            java.lang.Object r13 = r6.result
            java.lang.Object r0 = kotlin.coroutines.intrinsics.IntrinsicsKt.getCOROUTINE_SUSPENDED()
            int r1 = r6.label
            switch(r1) {
                case 0: goto L41;
                case 1: goto L35;
                case 2: goto L2e;
                default: goto L25;
            }
        L25:
            java.lang.IllegalStateException r9 = new java.lang.IllegalStateException
            java.lang.String r10 = "call to 'resume' before 'invoke' with coroutine"
            r9.<init>(r10)
            throw r9
        L2e:
            long r9 = r6.J$0
            kotlin.ResultKt.throwOnFailure(r13)
            r11 = r13
            goto L82
        L35:
            long r9 = r6.J$0
            java.lang.Object r11 = r6.L$0
            androidx.compose.material3.EnterAlwaysScrollBehavior$nestedScrollConnection$1 r11 = (androidx.compose.material3.EnterAlwaysScrollBehavior$nestedScrollConnection$1) r11
            kotlin.ResultKt.throwOnFailure(r13)
            r4 = r9
            r9 = r13
            goto L56
        L41:
            kotlin.ResultKt.throwOnFailure(r13)
            r1 = r8
            r2 = r9
            r4 = r11
            r6.L$0 = r1
            r6.J$0 = r4
            r9 = 1
            r6.label = r9
            java.lang.Object r9 = super.mo471onPostFlingRZ2iAVY(r2, r4, r6)
            if (r9 != r0) goto L55
            return r0
        L55:
            r11 = r1
        L56:
            androidx.compose.ui.unit.Velocity r9 = (androidx.compose.ui.unit.Velocity) r9
            long r9 = r9.getPackedValue()
            androidx.compose.material3.EnterAlwaysScrollBehavior r12 = r11.this$0
            androidx.compose.material3.TopAppBarState r12 = r12.getState()
            float r1 = androidx.compose.ui.unit.Velocity.m6860getYimpl(r4)
            androidx.compose.material3.EnterAlwaysScrollBehavior r2 = r11.this$0
            androidx.compose.animation.core.DecayAnimationSpec r2 = r2.getFlingAnimationSpec()
            androidx.compose.material3.EnterAlwaysScrollBehavior r3 = r11.this$0
            androidx.compose.animation.core.AnimationSpec r3 = r3.getSnapAnimationSpec()
            r7 = 0
            r6.L$0 = r7
            r6.J$0 = r9
            r7 = 2
            r6.label = r7
            java.lang.Object r11 = androidx.compose.material3.AppBarKt.access$settleAppBar(r12, r1, r2, r3, r6)
            if (r11 != r0) goto L82
            return r0
        L82:
            androidx.compose.ui.unit.Velocity r11 = (androidx.compose.ui.unit.Velocity) r11
            long r11 = r11.getPackedValue()
            long r9 = androidx.compose.ui.unit.Velocity.m6863plusAH228Gc(r9, r11)
            androidx.compose.ui.unit.Velocity r9 = androidx.compose.ui.unit.Velocity.m6850boximpl(r9)
            return r9
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.compose.material3.EnterAlwaysScrollBehavior$nestedScrollConnection$1.mo471onPostFlingRZ2iAVY(long, long, kotlin.coroutines.Continuation):java.lang.Object");
    }
}
