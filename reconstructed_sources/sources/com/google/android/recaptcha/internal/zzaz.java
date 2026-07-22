package com.google.android.recaptcha.internal;

import android.app.Application;
import kotlin.jvm.functions.Function0;

/* compiled from: com.google.android.recaptcha:recaptcha@@18.7.1 */
/* loaded from: classes11.dex */
public final class zzaz implements Function0 {
    public static final zzaz zza = new zzaz();

    @Override // kotlin.jvm.functions.Function0
    public final Object invoke() {
        int i = zzby.zza;
        Object zzb = zzbx.zza().zzb(Application.class.getName().hashCode());
        if (zzb == null) {
            throw new zzcg(zzce.zzb, zzcd.zzaA, null, null, 12, null);
        }
        return (Application) zzb;
    }
}
