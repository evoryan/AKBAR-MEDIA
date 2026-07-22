package kotlin.reflect.jvm.internal.impl.builtins;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kotlin.collections.CollectionsKt;
import kotlin.collections.SetsKt;
import kotlin.jvm.JvmStatic;
import kotlin.jvm.internal.Intrinsics;
import kotlin.reflect.jvm.internal.impl.builtins.functions.FunctionClassKind;
import kotlin.reflect.jvm.internal.impl.name.ClassId;
import kotlin.reflect.jvm.internal.impl.name.FqName;
import kotlin.reflect.jvm.internal.impl.name.FqNameUnsafe;
import kotlin.reflect.jvm.internal.impl.name.Name;

/* compiled from: StandardNames.kt */
/* loaded from: classes11.dex */
public final class StandardNames {
    public static final FqName ANNOTATION_PACKAGE_FQ_NAME;
    public static final Name BACKING_FIELD;
    public static final FqName BUILT_INS_PACKAGE_FQ_NAME;
    public static final Set<FqName> BUILT_INS_PACKAGE_FQ_NAMES;
    public static final Name BUILT_INS_PACKAGE_NAME;
    public static final Name CHAR_CODE;
    public static final FqName COLLECTIONS_PACKAGE_FQ_NAME;
    public static final Name CONTEXT_FUNCTION_TYPE_PARAMETER_COUNT_NAME;
    public static final FqName CONTINUATION_INTERFACE_FQ_NAME;
    public static final FqName COROUTINES_INTRINSICS_PACKAGE_FQ_NAME;
    public static final FqName COROUTINES_JVM_INTERNAL_PACKAGE_FQ_NAME;
    public static final FqName COROUTINES_PACKAGE_FQ_NAME;
    public static final String DATA_CLASS_COMPONENT_PREFIX;
    public static final Name DATA_CLASS_COPY;
    public static final Name DEFAULT_VALUE_PARAMETER;
    public static final FqName DYNAMIC_FQ_NAME;
    public static final Name ENUM_ENTRIES;
    public static final Name ENUM_VALUES;
    public static final Name ENUM_VALUE_OF;
    public static final Name HASHCODE_NAME;
    public static final StandardNames INSTANCE = new StandardNames();
    public static final FqName KOTLIN_INTERNAL_FQ_NAME;
    public static final FqName KOTLIN_REFLECT_FQ_NAME;
    public static final Name NEXT_CHAR;
    private static final FqName NON_EXISTENT_CLASS;
    public static final List<String> PREFIXES;
    public static final FqName RANGES_PACKAGE_FQ_NAME;
    public static final FqName RESULT_FQ_NAME;
    public static final FqName TEXT_PACKAGE_FQ_NAME;

    private StandardNames() {
    }

    static {
        Name identifier = Name.identifier("field");
        Intrinsics.checkNotNullExpressionValue(identifier, "identifier(\"field\")");
        BACKING_FIELD = identifier;
        Name identifier2 = Name.identifier("value");
        Intrinsics.checkNotNullExpressionValue(identifier2, "identifier(\"value\")");
        DEFAULT_VALUE_PARAMETER = identifier2;
        Name identifier3 = Name.identifier("values");
        Intrinsics.checkNotNullExpressionValue(identifier3, "identifier(\"values\")");
        ENUM_VALUES = identifier3;
        Name identifier4 = Name.identifier("entries");
        Intrinsics.checkNotNullExpressionValue(identifier4, "identifier(\"entries\")");
        ENUM_ENTRIES = identifier4;
        Name identifier5 = Name.identifier("valueOf");
        Intrinsics.checkNotNullExpressionValue(identifier5, "identifier(\"valueOf\")");
        ENUM_VALUE_OF = identifier5;
        Name identifier6 = Name.identifier("copy");
        Intrinsics.checkNotNullExpressionValue(identifier6, "identifier(\"copy\")");
        DATA_CLASS_COPY = identifier6;
        DATA_CLASS_COMPONENT_PREFIX = "component";
        Name identifier7 = Name.identifier("hashCode");
        Intrinsics.checkNotNullExpressionValue(identifier7, "identifier(\"hashCode\")");
        HASHCODE_NAME = identifier7;
        Name identifier8 = Name.identifier("code");
        Intrinsics.checkNotNullExpressionValue(identifier8, "identifier(\"code\")");
        CHAR_CODE = identifier8;
        Name identifier9 = Name.identifier("nextChar");
        Intrinsics.checkNotNullExpressionValue(identifier9, "identifier(\"nextChar\")");
        NEXT_CHAR = identifier9;
        Name identifier10 = Name.identifier("count");
        Intrinsics.checkNotNullExpressionValue(identifier10, "identifier(\"count\")");
        CONTEXT_FUNCTION_TYPE_PARAMETER_COUNT_NAME = identifier10;
        DYNAMIC_FQ_NAME = new FqName("<dynamic>");
        COROUTINES_PACKAGE_FQ_NAME = new FqName("kotlin.coroutines");
        COROUTINES_JVM_INTERNAL_PACKAGE_FQ_NAME = new FqName("kotlin.coroutines.jvm.internal");
        COROUTINES_INTRINSICS_PACKAGE_FQ_NAME = new FqName("kotlin.coroutines.intrinsics");
        FqName child = COROUTINES_PACKAGE_FQ_NAME.child(Name.identifier("Continuation"));
        Intrinsics.checkNotNullExpressionValue(child, "COROUTINES_PACKAGE_FQ_NA…entifier(\"Continuation\"))");
        CONTINUATION_INTERFACE_FQ_NAME = child;
        RESULT_FQ_NAME = new FqName("kotlin.Result");
        KOTLIN_REFLECT_FQ_NAME = new FqName("kotlin.reflect");
        PREFIXES = CollectionsKt.listOf((Object[]) new String[]{"KProperty", "KMutableProperty", "KFunction", "KSuspendFunction"});
        Name identifier11 = Name.identifier("kotlin");
        Intrinsics.checkNotNullExpressionValue(identifier11, "identifier(\"kotlin\")");
        BUILT_INS_PACKAGE_NAME = identifier11;
        FqName fqName = FqName.topLevel(BUILT_INS_PACKAGE_NAME);
        Intrinsics.checkNotNullExpressionValue(fqName, "topLevel(BUILT_INS_PACKAGE_NAME)");
        BUILT_INS_PACKAGE_FQ_NAME = fqName;
        FqName child2 = BUILT_INS_PACKAGE_FQ_NAME.child(Name.identifier("annotation"));
        Intrinsics.checkNotNullExpressionValue(child2, "BUILT_INS_PACKAGE_FQ_NAM…identifier(\"annotation\"))");
        ANNOTATION_PACKAGE_FQ_NAME = child2;
        FqName child3 = BUILT_INS_PACKAGE_FQ_NAME.child(Name.identifier("collections"));
        Intrinsics.checkNotNullExpressionValue(child3, "BUILT_INS_PACKAGE_FQ_NAM…dentifier(\"collections\"))");
        COLLECTIONS_PACKAGE_FQ_NAME = child3;
        FqName child4 = BUILT_INS_PACKAGE_FQ_NAME.child(Name.identifier("ranges"));
        Intrinsics.checkNotNullExpressionValue(child4, "BUILT_INS_PACKAGE_FQ_NAM…ame.identifier(\"ranges\"))");
        RANGES_PACKAGE_FQ_NAME = child4;
        FqName child5 = BUILT_INS_PACKAGE_FQ_NAME.child(Name.identifier("text"));
        Intrinsics.checkNotNullExpressionValue(child5, "BUILT_INS_PACKAGE_FQ_NAM…(Name.identifier(\"text\"))");
        TEXT_PACKAGE_FQ_NAME = child5;
        FqName child6 = BUILT_INS_PACKAGE_FQ_NAME.child(Name.identifier("internal"));
        Intrinsics.checkNotNullExpressionValue(child6, "BUILT_INS_PACKAGE_FQ_NAM…e.identifier(\"internal\"))");
        KOTLIN_INTERNAL_FQ_NAME = child6;
        NON_EXISTENT_CLASS = new FqName("error.NonExistentClass");
        BUILT_INS_PACKAGE_FQ_NAMES = SetsKt.setOf((Object[]) new FqName[]{BUILT_INS_PACKAGE_FQ_NAME, COLLECTIONS_PACKAGE_FQ_NAME, RANGES_PACKAGE_FQ_NAME, ANNOTATION_PACKAGE_FQ_NAME, KOTLIN_REFLECT_FQ_NAME, KOTLIN_INTERNAL_FQ_NAME, COROUTINES_PACKAGE_FQ_NAME});
    }

    /* compiled from: StandardNames.kt */
    /* loaded from: classes11.dex */
    public static final class FqNames {
        public static final FqName accessibleLateinitPropertyLiteral;
        public static final FqName annotation;
        public static final FqName annotationRetention;
        public static final FqName annotationTarget;
        public static final Map<FqNameUnsafe, PrimitiveType> arrayClassFqNameToPrimitiveType;
        public static final FqName collection;
        public static final Map<FqNameUnsafe, PrimitiveType> fqNameToPrimitiveType;
        public static final FqName iterable;
        public static final FqName iterator;
        public static final FqNameUnsafe kCallable;
        public static final FqNameUnsafe kClass;
        public static final FqNameUnsafe kDeclarationContainer;
        public static final FqNameUnsafe kMutableProperty0;
        public static final FqNameUnsafe kMutableProperty1;
        public static final FqNameUnsafe kMutableProperty2;
        public static final FqNameUnsafe kMutablePropertyFqName;
        public static final ClassId kProperty;
        public static final FqNameUnsafe kProperty0;
        public static final FqNameUnsafe kProperty1;
        public static final FqNameUnsafe kProperty2;
        public static final FqNameUnsafe kPropertyFqName;
        public static final FqName list;
        public static final FqName listIterator;
        public static final FqName map;
        public static final FqName mapEntry;
        public static final FqName mustBeDocumented;
        public static final FqName mutableCollection;
        public static final FqName mutableIterable;
        public static final FqName mutableIterator;
        public static final FqName mutableList;
        public static final FqName mutableListIterator;
        public static final FqName mutableMap;
        public static final FqName mutableMapEntry;
        public static final FqName mutableSet;
        public static final ClassId parameterNameClassId;
        public static final Set<Name> primitiveArrayTypeShortNames;
        public static final Set<Name> primitiveTypeShortNames;
        public static final FqName publishedApi;
        public static final FqName repeatable;
        public static final ClassId repeatableClassId;
        public static final FqName retention;
        public static final ClassId retentionClassId;
        public static final FqName set;
        public static final FqName target;
        public static final ClassId targetClassId;
        public static final ClassId uByte;
        public static final FqName uByteArrayFqName;
        public static final FqName uByteFqName;
        public static final ClassId uInt;
        public static final FqName uIntArrayFqName;
        public static final FqName uIntFqName;
        public static final ClassId uLong;
        public static final FqName uLongArrayFqName;
        public static final FqName uLongFqName;
        public static final ClassId uShort;
        public static final FqName uShortArrayFqName;
        public static final FqName uShortFqName;
        public static final FqName unsafeVariance;
        public static final FqNames INSTANCE = new FqNames();
        public static final FqNameUnsafe any = INSTANCE.fqNameUnsafe("Any");
        public static final FqNameUnsafe nothing = INSTANCE.fqNameUnsafe("Nothing");
        public static final FqNameUnsafe cloneable = INSTANCE.fqNameUnsafe("Cloneable");
        public static final FqName suppress = INSTANCE.fqName("Suppress");
        public static final FqNameUnsafe unit = INSTANCE.fqNameUnsafe("Unit");
        public static final FqNameUnsafe charSequence = INSTANCE.fqNameUnsafe("CharSequence");
        public static final FqNameUnsafe string = INSTANCE.fqNameUnsafe("String");
        public static final FqNameUnsafe array = INSTANCE.fqNameUnsafe("Array");
        public static final FqNameUnsafe _boolean = INSTANCE.fqNameUnsafe("Boolean");
        public static final FqNameUnsafe _char = INSTANCE.fqNameUnsafe("Char");
        public static final FqNameUnsafe _byte = INSTANCE.fqNameUnsafe("Byte");
        public static final FqNameUnsafe _short = INSTANCE.fqNameUnsafe("Short");
        public static final FqNameUnsafe _int = INSTANCE.fqNameUnsafe("Int");
        public static final FqNameUnsafe _long = INSTANCE.fqNameUnsafe("Long");
        public static final FqNameUnsafe _float = INSTANCE.fqNameUnsafe("Float");
        public static final FqNameUnsafe _double = INSTANCE.fqNameUnsafe("Double");
        public static final FqNameUnsafe number = INSTANCE.fqNameUnsafe("Number");
        public static final FqNameUnsafe _enum = INSTANCE.fqNameUnsafe("Enum");
        public static final FqNameUnsafe functionSupertype = INSTANCE.fqNameUnsafe("Function");
        public static final FqName throwable = INSTANCE.fqName("Throwable");
        public static final FqName comparable = INSTANCE.fqName("Comparable");
        public static final FqNameUnsafe intRange = INSTANCE.rangesFqName("IntRange");
        public static final FqNameUnsafe longRange = INSTANCE.rangesFqName("LongRange");
        public static final FqName deprecated = INSTANCE.fqName("Deprecated");
        public static final FqName deprecatedSinceKotlin = INSTANCE.fqName("DeprecatedSinceKotlin");
        public static final FqName deprecationLevel = INSTANCE.fqName("DeprecationLevel");
        public static final FqName replaceWith = INSTANCE.fqName("ReplaceWith");
        public static final FqName extensionFunctionType = INSTANCE.fqName("ExtensionFunctionType");
        public static final FqName contextFunctionTypeParams = INSTANCE.fqName("ContextFunctionTypeParams");
        public static final FqName parameterName = INSTANCE.fqName("ParameterName");

        private FqNames() {
        }

        static {
            ClassId classId = ClassId.topLevel(parameterName);
            Intrinsics.checkNotNullExpressionValue(classId, "topLevel(parameterName)");
            parameterNameClassId = classId;
            annotation = INSTANCE.fqName("Annotation");
            target = INSTANCE.annotationName("Target");
            ClassId classId2 = ClassId.topLevel(target);
            Intrinsics.checkNotNullExpressionValue(classId2, "topLevel(target)");
            targetClassId = classId2;
            annotationTarget = INSTANCE.annotationName("AnnotationTarget");
            annotationRetention = INSTANCE.annotationName("AnnotationRetention");
            retention = INSTANCE.annotationName("Retention");
            ClassId classId3 = ClassId.topLevel(retention);
            Intrinsics.checkNotNullExpressionValue(classId3, "topLevel(retention)");
            retentionClassId = classId3;
            repeatable = INSTANCE.annotationName("Repeatable");
            ClassId classId4 = ClassId.topLevel(repeatable);
            Intrinsics.checkNotNullExpressionValue(classId4, "topLevel(repeatable)");
            repeatableClassId = classId4;
            mustBeDocumented = INSTANCE.annotationName("MustBeDocumented");
            unsafeVariance = INSTANCE.fqName("UnsafeVariance");
            publishedApi = INSTANCE.fqName("PublishedApi");
            accessibleLateinitPropertyLiteral = INSTANCE.internalName("AccessibleLateinitPropertyLiteral");
            iterator = INSTANCE.collectionsFqName("Iterator");
            iterable = INSTANCE.collectionsFqName("Iterable");
            collection = INSTANCE.collectionsFqName("Collection");
            list = INSTANCE.collectionsFqName("List");
            listIterator = INSTANCE.collectionsFqName("ListIterator");
            set = INSTANCE.collectionsFqName("Set");
            map = INSTANCE.collectionsFqName("Map");
            FqName child = map.child(Name.identifier("Entry"));
            Intrinsics.checkNotNullExpressionValue(child, "map.child(Name.identifier(\"Entry\"))");
            mapEntry = child;
            mutableIterator = INSTANCE.collectionsFqName("MutableIterator");
            mutableIterable = INSTANCE.collectionsFqName("MutableIterable");
            mutableCollection = INSTANCE.collectionsFqName("MutableCollection");
            mutableList = INSTANCE.collectionsFqName("MutableList");
            mutableListIterator = INSTANCE.collectionsFqName("MutableListIterator");
            mutableSet = INSTANCE.collectionsFqName("MutableSet");
            mutableMap = INSTANCE.collectionsFqName("MutableMap");
            FqName child2 = mutableMap.child(Name.identifier("MutableEntry"));
            Intrinsics.checkNotNullExpressionValue(child2, "mutableMap.child(Name.identifier(\"MutableEntry\"))");
            mutableMapEntry = child2;
            kClass = reflect("KClass");
            kCallable = reflect("KCallable");
            kProperty0 = reflect("KProperty0");
            kProperty1 = reflect("KProperty1");
            kProperty2 = reflect("KProperty2");
            kMutableProperty0 = reflect("KMutableProperty0");
            kMutableProperty1 = reflect("KMutableProperty1");
            kMutableProperty2 = reflect("KMutableProperty2");
            kPropertyFqName = reflect("KProperty");
            kMutablePropertyFqName = reflect("KMutableProperty");
            ClassId classId5 = ClassId.topLevel(kPropertyFqName.toSafe());
            Intrinsics.checkNotNullExpressionValue(classId5, "topLevel(kPropertyFqName.toSafe())");
            kProperty = classId5;
            kDeclarationContainer = reflect("KDeclarationContainer");
            uByteFqName = INSTANCE.fqName("UByte");
            uShortFqName = INSTANCE.fqName("UShort");
            uIntFqName = INSTANCE.fqName("UInt");
            uLongFqName = INSTANCE.fqName("ULong");
            ClassId classId6 = ClassId.topLevel(uByteFqName);
            Intrinsics.checkNotNullExpressionValue(classId6, "topLevel(uByteFqName)");
            uByte = classId6;
            ClassId classId7 = ClassId.topLevel(uShortFqName);
            Intrinsics.checkNotNullExpressionValue(classId7, "topLevel(uShortFqName)");
            uShort = classId7;
            ClassId classId8 = ClassId.topLevel(uIntFqName);
            Intrinsics.checkNotNullExpressionValue(classId8, "topLevel(uIntFqName)");
            uInt = classId8;
            ClassId classId9 = ClassId.topLevel(uLongFqName);
            Intrinsics.checkNotNullExpressionValue(classId9, "topLevel(uLongFqName)");
            uLong = classId9;
            uByteArrayFqName = INSTANCE.fqName("UByteArray");
            uShortArrayFqName = INSTANCE.fqName("UShortArray");
            uIntArrayFqName = INSTANCE.fqName("UIntArray");
            uLongArrayFqName = INSTANCE.fqName("ULongArray");
            HashSet $this$primitiveTypeShortNames_u24lambda_u241 = kotlin.reflect.jvm.internal.impl.utils.CollectionsKt.newHashSetWithExpectedSize(PrimitiveType.values().length);
            for (PrimitiveType primitiveType : PrimitiveType.values()) {
                $this$primitiveTypeShortNames_u24lambda_u241.add(primitiveType.getTypeName());
            }
            primitiveTypeShortNames = $this$primitiveTypeShortNames_u24lambda_u241;
            HashSet $this$primitiveArrayTypeShortNames_u24lambda_u243 = kotlin.reflect.jvm.internal.impl.utils.CollectionsKt.newHashSetWithExpectedSize(PrimitiveType.values().length);
            for (PrimitiveType primitiveType2 : PrimitiveType.values()) {
                $this$primitiveArrayTypeShortNames_u24lambda_u243.add(primitiveType2.getArrayTypeName());
            }
            primitiveArrayTypeShortNames = $this$primitiveArrayTypeShortNames_u24lambda_u243;
            HashMap $this$fqNameToPrimitiveType_u24lambda_u244 = kotlin.reflect.jvm.internal.impl.utils.CollectionsKt.newHashMapWithExpectedSize(PrimitiveType.values().length);
            for (PrimitiveType primitiveType3 : PrimitiveType.values()) {
                FqNames fqNames = INSTANCE;
                String asString = primitiveType3.getTypeName().asString();
                Intrinsics.checkNotNullExpressionValue(asString, "primitiveType.typeName.asString()");
                $this$fqNameToPrimitiveType_u24lambda_u244.put(fqNames.fqNameUnsafe(asString), primitiveType3);
            }
            fqNameToPrimitiveType = $this$fqNameToPrimitiveType_u24lambda_u244;
            HashMap $this$arrayClassFqNameToPrimitiveType_u24lambda_u245 = kotlin.reflect.jvm.internal.impl.utils.CollectionsKt.newHashMapWithExpectedSize(PrimitiveType.values().length);
            for (PrimitiveType primitiveType4 : PrimitiveType.values()) {
                FqNames fqNames2 = INSTANCE;
                String asString2 = primitiveType4.getArrayTypeName().asString();
                Intrinsics.checkNotNullExpressionValue(asString2, "primitiveType.arrayTypeName.asString()");
                $this$arrayClassFqNameToPrimitiveType_u24lambda_u245.put(fqNames2.fqNameUnsafe(asString2), primitiveType4);
            }
            arrayClassFqNameToPrimitiveType = $this$arrayClassFqNameToPrimitiveType_u24lambda_u245;
        }

        private final FqNameUnsafe fqNameUnsafe(String simpleName) {
            FqNameUnsafe unsafe = fqName(simpleName).toUnsafe();
            Intrinsics.checkNotNullExpressionValue(unsafe, "fqName(simpleName).toUnsafe()");
            return unsafe;
        }

        private final FqName fqName(String simpleName) {
            FqName child = StandardNames.BUILT_INS_PACKAGE_FQ_NAME.child(Name.identifier(simpleName));
            Intrinsics.checkNotNullExpressionValue(child, "BUILT_INS_PACKAGE_FQ_NAM…e.identifier(simpleName))");
            return child;
        }

        private final FqName collectionsFqName(String simpleName) {
            FqName child = StandardNames.COLLECTIONS_PACKAGE_FQ_NAME.child(Name.identifier(simpleName));
            Intrinsics.checkNotNullExpressionValue(child, "COLLECTIONS_PACKAGE_FQ_N…e.identifier(simpleName))");
            return child;
        }

        private final FqNameUnsafe rangesFqName(String simpleName) {
            FqNameUnsafe unsafe = StandardNames.RANGES_PACKAGE_FQ_NAME.child(Name.identifier(simpleName)).toUnsafe();
            Intrinsics.checkNotNullExpressionValue(unsafe, "RANGES_PACKAGE_FQ_NAME.c…r(simpleName)).toUnsafe()");
            return unsafe;
        }

        @JvmStatic
        public static final FqNameUnsafe reflect(String simpleName) {
            Intrinsics.checkNotNullParameter(simpleName, "simpleName");
            FqNameUnsafe unsafe = StandardNames.KOTLIN_REFLECT_FQ_NAME.child(Name.identifier(simpleName)).toUnsafe();
            Intrinsics.checkNotNullExpressionValue(unsafe, "KOTLIN_REFLECT_FQ_NAME.c…r(simpleName)).toUnsafe()");
            return unsafe;
        }

        private final FqName annotationName(String simpleName) {
            FqName child = StandardNames.ANNOTATION_PACKAGE_FQ_NAME.child(Name.identifier(simpleName));
            Intrinsics.checkNotNullExpressionValue(child, "ANNOTATION_PACKAGE_FQ_NA…e.identifier(simpleName))");
            return child;
        }

        private final FqName internalName(String simpleName) {
            FqName child = StandardNames.KOTLIN_INTERNAL_FQ_NAME.child(Name.identifier(simpleName));
            Intrinsics.checkNotNullExpressionValue(child, "KOTLIN_INTERNAL_FQ_NAME.…e.identifier(simpleName))");
            return child;
        }
    }

    @JvmStatic
    public static final String getFunctionName(int parameterCount) {
        return "Function" + parameterCount;
    }

    @JvmStatic
    public static final ClassId getFunctionClassId(int parameterCount) {
        return new ClassId(BUILT_INS_PACKAGE_FQ_NAME, Name.identifier(getFunctionName(parameterCount)));
    }

    @JvmStatic
    public static final String getSuspendFunctionName(int parameterCount) {
        return FunctionClassKind.SuspendFunction.getClassNamePrefix() + parameterCount;
    }

    @JvmStatic
    public static final boolean isPrimitiveArray(FqNameUnsafe arrayFqName) {
        Intrinsics.checkNotNullParameter(arrayFqName, "arrayFqName");
        return FqNames.arrayClassFqNameToPrimitiveType.get(arrayFqName) != null;
    }

    @JvmStatic
    public static final FqName getPrimitiveFqName(PrimitiveType primitiveType) {
        Intrinsics.checkNotNullParameter(primitiveType, "primitiveType");
        FqName child = BUILT_INS_PACKAGE_FQ_NAME.child(primitiveType.getTypeName());
        Intrinsics.checkNotNullExpressionValue(child, "BUILT_INS_PACKAGE_FQ_NAM…d(primitiveType.typeName)");
        return child;
    }
}
