package kotlin.reflect.jvm.internal.impl.descriptors.deserialization;

import kotlin.jvm.internal.Intrinsics;
import kotlin.reflect.jvm.internal.impl.name.ClassId;
import kotlin.reflect.jvm.internal.impl.types.SimpleType;

/* compiled from: PlatformDependentTypeTransformer.kt */
/* loaded from: classes11.dex */
public interface PlatformDependentTypeTransformer {
    SimpleType transformPlatformType(ClassId classId, SimpleType simpleType);

    /* compiled from: PlatformDependentTypeTransformer.kt */
    /* loaded from: classes11.dex */
    public static final class None implements PlatformDependentTypeTransformer {
        public static final None INSTANCE = new None();

        private None() {
        }

        @Override // kotlin.reflect.jvm.internal.impl.descriptors.deserialization.PlatformDependentTypeTransformer
        public SimpleType transformPlatformType(ClassId classId, SimpleType computedType) {
            Intrinsics.checkNotNullParameter(classId, "classId");
            Intrinsics.checkNotNullParameter(computedType, "computedType");
            return computedType;
        }
    }
}
