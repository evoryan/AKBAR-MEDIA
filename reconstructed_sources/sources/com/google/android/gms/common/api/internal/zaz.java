package com.google.android.gms.common.api.internal;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import java.util.Objects;

/* compiled from: com.google.android.gms:play-services-base@@18.9.0 */
/* loaded from: classes10.dex */
final class zaz implements OnCompleteListener {
    final /* synthetic */ TaskCompletionSource zaa;
    final /* synthetic */ zaaa zab;

    /* JADX INFO: Access modifiers changed from: package-private */
    public zaz(zaaa zaaaVar, TaskCompletionSource taskCompletionSource) {
        this.zaa = taskCompletionSource;
        Objects.requireNonNull(zaaaVar);
        this.zab = zaaaVar;
    }

    @Override // com.google.android.gms.tasks.OnCompleteListener
    public final void onComplete(Task task) {
        this.zab.zag().remove(this.zaa);
    }
}
