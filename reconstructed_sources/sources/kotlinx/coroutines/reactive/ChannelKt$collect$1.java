package kotlinx.coroutines.reactive;

import kotlin.Metadata;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: Channel.kt */
@Metadata(k = 3, mv = {2, 1, 0}, xi = 176)
@DebugMetadata(c = "kotlinx.coroutines.reactive.ChannelKt", f = "Channel.kt", i = {0, 0}, l = {112}, m = "collect", n = {"action", "$this$consume$iv$iv"}, s = {"L$0", "L$1"})
/* loaded from: classes11.dex */
public final class ChannelKt$collect$1<T> extends ContinuationImpl {
    Object L$0;
    Object L$1;
    Object L$2;
    int label;
    /* synthetic */ Object result;

    /* JADX INFO: Access modifiers changed from: package-private */
    public ChannelKt$collect$1(Continuation<? super ChannelKt$collect$1> continuation) {
        super(continuation);
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Object invokeSuspend(Object obj) {
        this.result = obj;
        this.label |= Integer.MIN_VALUE;
        return ChannelKt.collect(null, null, this);
    }
}
