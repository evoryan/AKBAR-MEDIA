package io.ktor.http.content;

import kotlin.Metadata;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.flow.Flow;
import kotlinx.coroutines.flow.FlowKt;

/* compiled from: Multipart.kt */
@Metadata(d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\b\u0003\u001a\u0017\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001*\u00020\u0000¢\u0006\u0004\b\u0003\u0010\u0004\u001a8\u0010\n\u001a\u00020\u0007*\u00020\u00002\"\u0010\t\u001a\u001e\b\u0001\u0012\u0004\u0012\u00020\u0002\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\u0006\u0012\u0006\u0012\u0004\u0018\u00010\b0\u0005H\u0086@¢\u0006\u0004\b\n\u0010\u000b\u001a\u001a\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00020\f*\u00020\u0000H\u0087@¢\u0006\u0004\b\r\u0010\u000e¨\u0006\u000f"}, d2 = {"Lio/ktor/http/content/MultiPartData;", "Lkotlinx/coroutines/flow/Flow;", "Lio/ktor/http/content/PartData;", "asFlow", "(Lio/ktor/http/content/MultiPartData;)Lkotlinx/coroutines/flow/Flow;", "Lkotlin/Function2;", "Lkotlin/coroutines/Continuation;", "", "", "partHandler", "forEachPart", "(Lio/ktor/http/content/MultiPartData;Lkotlin/jvm/functions/Function2;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "", "readAllParts", "(Lio/ktor/http/content/MultiPartData;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "ktor-http"}, k = 2, mv = {2, 0, 0}, xi = 48)
/* loaded from: classes11.dex */
public final class MultipartKt {
    public static final Flow<PartData> asFlow(MultiPartData $this$asFlow) {
        Intrinsics.checkNotNullParameter($this$asFlow, "<this>");
        return FlowKt.flow(new MultipartKt$asFlow$1($this$asFlow, null));
    }

    public static final Object forEachPart(MultiPartData $this$forEachPart, Function2<? super PartData, ? super Continuation<? super Unit>, ? extends Object> function2, Continuation<? super Unit> continuation) {
        Object collect = asFlow($this$forEachPart).collect(new MultipartKt$sam$kotlinx_coroutines_flow_FlowCollector$0(function2), continuation);
        return collect == IntrinsicsKt.getCOROUTINE_SUSPENDED() ? collect : Unit.INSTANCE;
    }

    /* JADX WARN: Failed to find 'out' block for switch in B:7:0x0021. Please report as an issue. */
    /* JADX WARN: Removed duplicated region for block: B:11:0x002c  */
    /* JADX WARN: Removed duplicated region for block: B:14:0x0075 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:15:0x0076  */
    /* JADX WARN: Removed duplicated region for block: B:18:0x0070 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:19:0x0038  */
    /* JADX WARN: Removed duplicated region for block: B:22:0x0053  */
    /* JADX WARN: Removed duplicated region for block: B:24:0x0058  */
    /* JADX WARN: Removed duplicated region for block: B:25:0x0040  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0024  */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:17:0x006e -> B:12:0x0071). Please report as a decompilation issue!!! */
    @kotlin.Deprecated(level = kotlin.DeprecationLevel.ERROR, message = "This method can deadlock on large requests. Use `forEachPart` instead.")
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static final java.lang.Object readAllParts(io.ktor.http.content.MultiPartData r4, kotlin.coroutines.Continuation<? super java.util.List<? extends io.ktor.http.content.PartData>> r5) {
        /*
            boolean r0 = r5 instanceof io.ktor.http.content.MultipartKt$readAllParts$1
            if (r0 == 0) goto L14
            r0 = r5
            io.ktor.http.content.MultipartKt$readAllParts$1 r0 = (io.ktor.http.content.MultipartKt$readAllParts$1) r0
            int r1 = r0.label
            r2 = -2147483648(0xffffffff80000000, float:-0.0)
            r1 = r1 & r2
            if (r1 == 0) goto L14
            int r5 = r0.label
            int r5 = r5 - r2
            r0.label = r5
            goto L19
        L14:
            io.ktor.http.content.MultipartKt$readAllParts$1 r0 = new io.ktor.http.content.MultipartKt$readAllParts$1
            r0.<init>(r5)
        L19:
            java.lang.Object r5 = r0.result
            java.lang.Object r1 = kotlin.coroutines.intrinsics.IntrinsicsKt.getCOROUTINE_SUSPENDED()
            int r2 = r0.label
            switch(r2) {
                case 0: goto L40;
                case 1: goto L38;
                case 2: goto L2c;
                default: goto L24;
            }
        L24:
            java.lang.IllegalStateException r4 = new java.lang.IllegalStateException
            java.lang.String r5 = "call to 'resume' before 'invoke' with coroutine"
            r4.<init>(r5)
            throw r4
        L2c:
            java.lang.Object r4 = r0.L$1
            java.util.ArrayList r4 = (java.util.ArrayList) r4
            java.lang.Object r2 = r0.L$0
            io.ktor.http.content.MultiPartData r2 = (io.ktor.http.content.MultiPartData) r2
            kotlin.ResultKt.throwOnFailure(r5)
            goto L71
        L38:
            java.lang.Object r4 = r0.L$0
            io.ktor.http.content.MultiPartData r4 = (io.ktor.http.content.MultiPartData) r4
            kotlin.ResultKt.throwOnFailure(r5)
            goto L4f
        L40:
            kotlin.ResultKt.throwOnFailure(r5)
            r0.L$0 = r4
            r5 = 1
            r0.label = r5
            java.lang.Object r5 = r4.readPart(r0)
            if (r5 != r1) goto L4f
            return r1
        L4f:
            io.ktor.http.content.PartData r5 = (io.ktor.http.content.PartData) r5
            if (r5 != 0) goto L58
            java.util.List r4 = kotlin.collections.CollectionsKt.emptyList()
            return r4
        L58:
            java.util.ArrayList r2 = new java.util.ArrayList
            r2.<init>()
            r2.add(r5)
            r3 = r2
            r2 = r4
            r4 = r3
        L63:
            r0.L$0 = r2
            r0.L$1 = r4
            r5 = 2
            r0.label = r5
            java.lang.Object r5 = r2.readPart(r0)
            if (r5 != r1) goto L71
            return r1
        L71:
            io.ktor.http.content.PartData r5 = (io.ktor.http.content.PartData) r5
            if (r5 != 0) goto L76
            return r4
        L76:
            r4.add(r5)
            goto L63
        */
        throw new UnsupportedOperationException("Method not decompiled: io.ktor.http.content.MultipartKt.readAllParts(io.ktor.http.content.MultiPartData, kotlin.coroutines.Continuation):java.lang.Object");
    }
}
