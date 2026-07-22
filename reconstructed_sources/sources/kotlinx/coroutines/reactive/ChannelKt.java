package kotlinx.coroutines.reactive;

import kotlin.Deprecated;
import kotlin.DeprecationLevel;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.jvm.functions.Function1;
import kotlinx.coroutines.channels.ChannelIterator;
import kotlinx.coroutines.channels.ChannelsKt;
import kotlinx.coroutines.channels.ReceiveChannel;
import org.reactivestreams.Publisher;

/* compiled from: Channel.kt */
@Metadata(d1 = {"\u0000$\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\u001a2\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u0002*\b\u0012\u0004\u0012\u0002H\u00020\u00032\u0012\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u0002H\u0002\u0012\u0004\u0012\u00020\u00010\u0005H\u0086H¢\u0006\u0002\u0010\u0006\u001a(\u0010\u0007\u001a\b\u0012\u0004\u0012\u0002H\u00020\b\"\u0004\b\u0000\u0010\u0002*\b\u0012\u0004\u0012\u0002H\u00020\u00032\b\b\u0002\u0010\t\u001a\u00020\nH\u0001\u001a(\u0010\u000b\u001a\b\u0012\u0004\u0012\u0002H\u00020\b\"\u0004\b\u0000\u0010\u0002*\b\u0012\u0004\u0012\u0002H\u00020\u00032\b\b\u0002\u0010\t\u001a\u00020\nH\u0007¨\u0006\f"}, d2 = {"collect", "", "T", "Lorg/reactivestreams/Publisher;", "action", "Lkotlin/Function1;", "(Lorg/reactivestreams/Publisher;Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "toChannel", "Lkotlinx/coroutines/channels/ReceiveChannel;", "request", "", "openSubscription", "kotlinx-coroutines-reactive"}, k = 2, mv = {2, 1, 0}, xi = 48)
/* loaded from: classes11.dex */
public final class ChannelKt {
    /* JADX WARN: Failed to find 'out' block for switch in B:7:0x0022. Please report as an issue. */
    /* JADX WARN: Removed duplicated region for block: B:11:0x002d  */
    /* JADX WARN: Removed duplicated region for block: B:18:0x008b A[Catch: all -> 0x00a7, TryCatch #1 {all -> 0x00a7, blocks: (B:16:0x0083, B:18:0x008b, B:24:0x009b), top: B:15:0x0083 }] */
    /* JADX WARN: Removed duplicated region for block: B:21:0x0078 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:22:0x0079  */
    /* JADX WARN: Removed duplicated region for block: B:24:0x009b A[Catch: all -> 0x00a7, TRY_LEAVE, TryCatch #1 {all -> 0x00a7, blocks: (B:16:0x0083, B:18:0x008b, B:24:0x009b), top: B:15:0x0083 }] */
    /* JADX WARN: Removed duplicated region for block: B:38:0x004d  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0025  */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:22:0x0079 -> B:15:0x0083). Please report as a decompilation issue!!! */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static final <T> java.lang.Object collect(org.reactivestreams.Publisher<T> r12, kotlin.jvm.functions.Function1<? super T, kotlin.Unit> r13, kotlin.coroutines.Continuation<? super kotlin.Unit> r14) {
        /*
            Method dump skipped, instructions count: 196
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: kotlinx.coroutines.reactive.ChannelKt.collect(org.reactivestreams.Publisher, kotlin.jvm.functions.Function1, kotlin.coroutines.Continuation):java.lang.Object");
    }

    private static final <T> Object collect$$forInline(Publisher<T> publisher, Function1<? super T, Unit> function1, Continuation<? super Unit> continuation) {
        ReceiveChannel $this$consumeEach$iv = toChannel$default(publisher, 0, 1, null);
        try {
            ReceiveChannel $this$consumeEach_u24lambda_u240$iv = $this$consumeEach$iv;
            ChannelIterator it = $this$consumeEach_u24lambda_u240$iv.iterator();
            while (((Boolean) it.hasNext(null)).booleanValue()) {
                Object e$iv = it.next();
                function1.invoke(e$iv);
            }
            Unit unit = Unit.INSTANCE;
            ChannelsKt.cancelConsumed($this$consumeEach$iv, null);
            Unit unit2 = Unit.INSTANCE;
            return Unit.INSTANCE;
        } finally {
        }
    }

    public static /* synthetic */ ReceiveChannel toChannel$default(Publisher publisher, int i, int i2, Object obj) {
        if ((i2 & 1) != 0) {
            i = 1;
        }
        return toChannel(publisher, i);
    }

    public static final <T> ReceiveChannel<T> toChannel(Publisher<T> publisher, int request) {
        SubscriptionChannel channel = new SubscriptionChannel(request);
        publisher.subscribe(channel);
        return channel;
    }

    public static /* synthetic */ ReceiveChannel openSubscription$default(Publisher publisher, int i, int i2, Object obj) {
        if ((i2 & 1) != 0) {
            i = 1;
        }
        return openSubscription(publisher, i);
    }

    @Deprecated(level = DeprecationLevel.HIDDEN, message = "Transforming publisher to channel is deprecated, use asFlow() instead")
    public static final /* synthetic */ ReceiveChannel openSubscription(Publisher $this$openSubscription, int request) {
        SubscriptionChannel channel = new SubscriptionChannel(request);
        $this$openSubscription.subscribe(channel);
        return channel;
    }
}
