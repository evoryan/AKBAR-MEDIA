package com.google.android.gms.tasks;

import java.util.concurrent.ExecutionException;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: com.google.android.gms:play-services-tasks@@18.4.0 */
/* loaded from: classes10.dex */
public final class zzae<T> implements zzab<T> {
    private final Object zza = new Object();
    private final int zzb;
    private final zzw zzc;
    private int zzd;
    private int zze;
    private int zzf;
    private Exception zzg;
    private boolean zzh;

    public zzae(int i, zzw zzwVar) {
        this.zzb = i;
        this.zzc = zzwVar;
    }

    private final void zza() {
        int i = this.zzd + this.zze + this.zzf;
        int i2 = this.zzb;
        if (i == i2) {
            if (this.zzg == null) {
                boolean z = this.zzh;
                zzw zzwVar = this.zzc;
                if (z) {
                    zzwVar.zze();
                    return;
                } else {
                    zzwVar.zza(null);
                    return;
                }
            }
            zzw zzwVar2 = this.zzc;
            int i3 = this.zze;
            StringBuilder sb = new StringBuilder(String.valueOf(i3).length() + 8 + String.valueOf(i2).length() + 24);
            sb.append(i3);
            sb.append(" out of ");
            sb.append(i2);
            sb.append(" underlying tasks failed");
            zzwVar2.zzc(new ExecutionException(sb.toString(), this.zzg));
        }
    }

    @Override // com.google.android.gms.tasks.OnCanceledListener
    public final void onCanceled() {
        synchronized (this.zza) {
            this.zzf++;
            this.zzh = true;
            zza();
        }
    }

    @Override // com.google.android.gms.tasks.OnFailureListener
    public final void onFailure(Exception exc) {
        synchronized (this.zza) {
            this.zze++;
            this.zzg = exc;
            zza();
        }
    }

    @Override // com.google.android.gms.tasks.OnSuccessListener
    public final void onSuccess(T t) {
        synchronized (this.zza) {
            this.zzd++;
            zza();
        }
    }
}
