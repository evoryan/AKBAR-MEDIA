package com.google.firebase.ai.type;

import kotlin.Metadata;

/* compiled from: Exceptions.kt */
@Metadata(d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\u000f\u0012\u0006\u0010\u0002\u001a\u00020\u0003¢\u0006\u0004\b\u0004\u0010\u0005¨\u0006\u0006"}, d2 = {"Lcom/google/firebase/ai/type/FirebaseAIOnDeviceUnknownException;", "Lcom/google/firebase/ai/type/FirebaseAIException;", "cause", "Lcom/google/firebase/ai/ondevice/interop/FirebaseAIOnDeviceException;", "<init>", "(Lcom/google/firebase/ai/ondevice/interop/FirebaseAIOnDeviceException;)V", "com.google.firebase-ai-logic-firebase-ai"}, k = 1, mv = {2, 0, 0}, xi = 48)
/* loaded from: classes11.dex */
public final class FirebaseAIOnDeviceUnknownException extends FirebaseAIException {
    /* JADX WARN: Illegal instructions before constructor call */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public FirebaseAIOnDeviceUnknownException(com.google.firebase.ai.ondevice.interop.FirebaseAIOnDeviceException r3) {
        /*
            r2 = this;
            java.lang.String r0 = "cause"
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(r3, r0)
            java.lang.String r0 = r3.getMessage()
            if (r0 != 0) goto Ld
            java.lang.String r0 = "Unknown on-device exception"
        Ld:
            r1 = r3
            java.lang.Throwable r1 = (java.lang.Throwable) r1
            r2.<init>(r0, r1)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.firebase.ai.type.FirebaseAIOnDeviceUnknownException.<init>(com.google.firebase.ai.ondevice.interop.FirebaseAIOnDeviceException):void");
    }
}
