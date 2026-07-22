package com.google.android.datatransport.runtime.scheduling.jobscheduling;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import com.google.android.datatransport.runtime.TransportContext;
import com.google.android.datatransport.runtime.TransportRuntime;
import com.google.android.datatransport.runtime.util.PriorityMapping;
import org.apache.commons.logging.LogFactory;

/* loaded from: classes10.dex */
public class AlarmManagerSchedulerBroadcastReceiver extends BroadcastReceiver {
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        String backendName = intent.getData().getQueryParameter("backendName");
        String extras = intent.getData().getQueryParameter("extras");
        int priority = Integer.valueOf(intent.getData().getQueryParameter(LogFactory.PRIORITY_KEY)).intValue();
        int attemptNumber = intent.getExtras().getInt("attemptNumber");
        TransportRuntime.initialize(context);
        TransportContext.Builder transportContext = TransportContext.builder().setBackendName(backendName).setPriority(PriorityMapping.valueOf(priority));
        if (extras != null) {
            transportContext.setExtras(Base64.decode(extras, 0));
        }
        TransportRuntime.getInstance().getUploader().upload(transportContext.build(), attemptNumber, new Runnable() { // from class: com.google.android.datatransport.runtime.scheduling.jobscheduling.AlarmManagerSchedulerBroadcastReceiver$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                AlarmManagerSchedulerBroadcastReceiver.lambda$onReceive$0();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ void lambda$onReceive$0() {
    }
}
