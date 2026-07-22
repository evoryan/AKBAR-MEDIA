package io.ktor.util.cio;

import io.ktor.utils.io.ByteReadChannel;
import io.ktor.utils.io.ByteWriteChannel;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: Readers.kt */
@Metadata(d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0012\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\u001a\u001e\u0010\u0004\u001a\u00020\u0003*\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u0001H\u0086@¢\u0006\u0004\b\u0004\u0010\u0005\u001a=\u0010\u000b\u001a\u00020\b*\u00020\u00062\u0017\u0010\n\u001a\u0013\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\b0\u0007¢\u0006\u0002\b\tH\u0086\bø\u0001\u0000\u0082\u0002\n\n\b\b\u0001\u0012\u0002\u0010\u0001 \u0001¢\u0006\u0004\b\u000b\u0010\f\u0082\u0002\u0007\n\u0005\b\u009920\u0001¨\u0006\r"}, d2 = {"Lio/ktor/utils/io/ByteReadChannel;", "", "limit", "", "toByteArray", "(Lio/ktor/utils/io/ByteReadChannel;ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Lio/ktor/utils/io/ByteWriteChannel;", "Lkotlin/Function1;", "", "Lkotlin/ExtensionFunctionType;", "block", "use", "(Lio/ktor/utils/io/ByteWriteChannel;Lkotlin/jvm/functions/Function1;)V", "ktor-utils"}, k = 2, mv = {2, 0, 0}, xi = 48)
/* loaded from: classes11.dex */
public final class ReadersKt {
    /* JADX WARN: Removed duplicated region for block: B:11:0x002c  */
    /* JADX WARN: Removed duplicated region for block: B:14:0x0031  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0024  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static final java.lang.Object toByteArray(io.ktor.utils.io.ByteReadChannel r6, int r7, kotlin.coroutines.Continuation<? super byte[]> r8) {
        /*
            boolean r0 = r8 instanceof io.ktor.util.cio.ReadersKt$toByteArray$1
            if (r0 == 0) goto L14
            r0 = r8
            io.ktor.util.cio.ReadersKt$toByteArray$1 r0 = (io.ktor.util.cio.ReadersKt$toByteArray$1) r0
            int r1 = r0.label
            r2 = -2147483648(0xffffffff80000000, float:-0.0)
            r1 = r1 & r2
            if (r1 == 0) goto L14
            int r1 = r0.label
            int r1 = r1 - r2
            r0.label = r1
            goto L19
        L14:
            io.ktor.util.cio.ReadersKt$toByteArray$1 r0 = new io.ktor.util.cio.ReadersKt$toByteArray$1
            r0.<init>(r8)
        L19:
            java.lang.Object r1 = r0.result
            java.lang.Object r2 = kotlin.coroutines.intrinsics.IntrinsicsKt.getCOROUTINE_SUSPENDED()
            int r3 = r0.label
            switch(r3) {
                case 0: goto L31;
                case 1: goto L2c;
                default: goto L24;
            }
        L24:
            java.lang.IllegalStateException r6 = new java.lang.IllegalStateException
            java.lang.String r7 = "call to 'resume' before 'invoke' with coroutine"
            r6.<init>(r7)
            throw r6
        L2c:
            kotlin.ResultKt.throwOnFailure(r1)
            r6 = r1
            goto L3f
        L31:
            kotlin.ResultKt.throwOnFailure(r1)
            long r3 = (long) r7
            r5 = 1
            r0.label = r5
            java.lang.Object r6 = io.ktor.utils.io.ByteReadChannelOperationsKt.readRemaining(r6, r3, r0)
            if (r6 != r2) goto L3f
            return r2
        L3f:
            kotlinx.io.Source r6 = (kotlinx.io.Source) r6
            byte[] r6 = kotlinx.io.SourcesKt.readByteArray(r6)
            return r6
        */
        throw new UnsupportedOperationException("Method not decompiled: io.ktor.util.cio.ReadersKt.toByteArray(io.ktor.utils.io.ByteReadChannel, int, kotlin.coroutines.Continuation):java.lang.Object");
    }

    public static /* synthetic */ Object toByteArray$default(ByteReadChannel byteReadChannel, int i, Continuation continuation, int i2, Object obj) {
        if ((i2 & 1) != 0) {
            i = Integer.MAX_VALUE;
        }
        return toByteArray(byteReadChannel, i, continuation);
    }

    public static final void use(ByteWriteChannel $this$use, Function1<? super ByteWriteChannel, Unit> block) {
        Intrinsics.checkNotNullParameter($this$use, "<this>");
        Intrinsics.checkNotNullParameter(block, "block");
        try {
            block.invoke($this$use);
        } finally {
        }
    }
}
