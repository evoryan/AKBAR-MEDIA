package com.example.ui.screens;

import android.content.Context;
import androidx.compose.foundation.BackgroundKt;
import androidx.compose.foundation.ImageKt;
import androidx.compose.foundation.layout.Arrangement;
import androidx.compose.foundation.layout.BoxKt;
import androidx.compose.foundation.layout.BoxScope;
import androidx.compose.foundation.layout.BoxScopeInstance;
import androidx.compose.foundation.layout.ColumnKt;
import androidx.compose.foundation.layout.ColumnScope;
import androidx.compose.foundation.layout.ColumnScopeInstance;
import androidx.compose.foundation.layout.SizeKt;
import androidx.compose.foundation.layout.SpacerKt;
import androidx.compose.material3.MaterialTheme;
import androidx.compose.material3.TextKt;
import androidx.compose.runtime.Applier;
import androidx.compose.runtime.Composable;
import androidx.compose.runtime.ComposableTarget;
import androidx.compose.runtime.ComposablesKt;
import androidx.compose.runtime.Composer;
import androidx.compose.runtime.ComposerKt;
import androidx.compose.runtime.CompositionLocal;
import androidx.compose.runtime.CompositionLocalMap;
import androidx.compose.runtime.EffectsKt;
import androidx.compose.runtime.RecomposeScopeImplKt;
import androidx.compose.runtime.ScopeUpdateScope;
import androidx.compose.runtime.Updater;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.ComposedModifierKt;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.Color;
import androidx.compose.ui.graphics.ColorFilter;
import androidx.compose.ui.graphics.ColorKt;
import androidx.compose.ui.graphics.Shape;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.layout.MeasurePolicy;
import androidx.compose.ui.node.ComposeUiNode;
import androidx.compose.ui.platform.AndroidCompositionLocals_androidKt;
import androidx.compose.ui.res.PainterResources_androidKt;
import androidx.compose.ui.text.TextStyle;
import androidx.compose.ui.text.font.FontFamily;
import androidx.compose.ui.text.font.FontStyle;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import androidx.compose.ui.text.style.TextDecoration;
import androidx.compose.ui.unit.Dp;
import androidx.compose.ui.unit.TextUnitKt;
import com.example.R;
import com.example.ui.data.SettingsManager;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.SourceDebugExtension;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: SplashScreen.kt */
@Metadata(d1 = {"\u0000\u0010\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\u001a)\u0010\u0000\u001a\u00020\u00012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u0003H\u0007¢\u0006\u0002\u0010\u0005¨\u0006\u0006"}, d2 = {"SplashScreen", "", "onNavigateToLogin", "Lkotlin/Function0;", "onNavigateToDashboard", "(Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function0;Landroidx/compose/runtime/Composer;I)V", "app"}, k = 2, mv = {2, 2, 0}, xi = 48)
@SourceDebugExtension({"SMAP\nSplashScreen.kt\nKotlin\n*S Kotlin\n*F\n+ 1 SplashScreen.kt\ncom/example/ui/screens/SplashScreenKt\n+ 2 CompositionLocal.kt\nandroidx/compose/runtime/CompositionLocal\n+ 3 Composer.kt\nandroidx/compose/runtime/ComposerKt\n+ 4 Box.kt\nandroidx/compose/foundation/layout/BoxKt\n+ 5 Layout.kt\nandroidx/compose/ui/layout/LayoutKt\n+ 6 Composables.kt\nandroidx/compose/runtime/ComposablesKt\n+ 7 Composer.kt\nandroidx/compose/runtime/Updater\n+ 8 Column.kt\nandroidx/compose/foundation/layout/ColumnKt\n+ 9 Dp.kt\nandroidx/compose/ui/unit/DpKt\n*L\n1#1,90:1\n77#2:91\n1225#3,6:92\n71#4:98\n69#4,5:99\n74#4:132\n71#4:171\n69#4,5:172\n74#4:205\n78#4:210\n78#4:220\n79#5,6:104\n86#5,4:119\n90#5,2:129\n79#5,6:141\n86#5,4:156\n90#5,2:166\n79#5,6:177\n86#5,4:192\n90#5,2:202\n94#5:209\n94#5:215\n94#5:219\n368#6,9:110\n377#6:131\n368#6,9:147\n377#6:168\n368#6,9:183\n377#6:204\n378#6,2:207\n378#6,2:213\n378#6,2:217\n4034#7,6:123\n4034#7,6:160\n4034#7,6:196\n86#8:133\n82#8,7:134\n89#8:169\n93#8:216\n149#9:170\n149#9:206\n149#9:211\n149#9:212\n*S KotlinDebug\n*F\n+ 1 SplashScreen.kt\ncom/example/ui/screens/SplashScreenKt\n*L\n35#1:91\n36#1:92,6\n52#1:98\n52#1:99,5\n52#1:132\n62#1:171\n62#1:172,5\n62#1:205\n62#1:210\n52#1:220\n52#1:104,6\n52#1:119,4\n52#1:129,2\n58#1:141,6\n58#1:156,4\n58#1:166,2\n62#1:177,6\n62#1:192,4\n62#1:202,2\n62#1:209\n58#1:215\n52#1:219\n52#1:110,9\n52#1:131\n58#1:147,9\n58#1:168\n62#1:183,9\n62#1:204\n62#1:207,2\n58#1:213,2\n52#1:217,2\n52#1:123,6\n58#1:160,6\n62#1:196,6\n58#1:133\n58#1:134,7\n58#1:169\n58#1:216\n64#1:170\n70#1:206\n73#1:211\n80#1:212\n*E\n"})
/* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/screens/SplashScreenKt.dex */
public final class SplashScreenKt {
    /* JADX INFO: Access modifiers changed from: package-private */
    public static final Unit SplashScreen$lambda$4(Function0 function0, Function0 function02, int i, Composer composer, int i2) {
        SplashScreen(function0, function02, composer, RecomposeScopeImplKt.updateChangedFlags(i | 1));
        return Unit.INSTANCE;
    }

    /* JADX WARN: Removed duplicated region for block: B:100:0x00ed  */
    /* JADX WARN: Removed duplicated region for block: B:42:0x00e7  */
    /* JADX WARN: Removed duplicated region for block: B:45:0x0113  */
    /* JADX WARN: Removed duplicated region for block: B:48:0x0138  */
    /* JADX WARN: Removed duplicated region for block: B:51:0x01cb  */
    /* JADX WARN: Removed duplicated region for block: B:54:0x01d7  */
    /* JADX WARN: Removed duplicated region for block: B:62:0x02eb  */
    /* JADX WARN: Removed duplicated region for block: B:65:0x02f7  */
    /* JADX WARN: Removed duplicated region for block: B:68:0x0330  */
    /* JADX WARN: Removed duplicated region for block: B:73:0x0402  */
    /* JADX WARN: Removed duplicated region for block: B:76:0x040e  */
    /* JADX WARN: Removed duplicated region for block: B:79:0x0445  */
    /* JADX WARN: Removed duplicated region for block: B:84:0x0520  */
    /* JADX WARN: Removed duplicated region for block: B:87:0x05c4  */
    /* JADX WARN: Removed duplicated region for block: B:88:0x0527  */
    /* JADX WARN: Removed duplicated region for block: B:90:0x045b A[ADDED_TO_REGION] */
    /* JADX WARN: Removed duplicated region for block: B:91:0x0414  */
    /* JADX WARN: Removed duplicated region for block: B:93:0x0346 A[ADDED_TO_REGION] */
    /* JADX WARN: Removed duplicated region for block: B:94:0x02fd  */
    /* JADX WARN: Removed duplicated region for block: B:97:0x01dd  */
    /* JADX WARN: Removed duplicated region for block: B:98:0x013e  */
    /* JADX WARN: Removed duplicated region for block: B:99:0x0119  */
    @ComposableTarget(applier = "androidx.compose.ui.UiComposable")
    @Composable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static final void SplashScreen(@NotNull final Function0<Unit> function0, @NotNull final Function0<Unit> function02, @Nullable Composer $composer, final int $changed) {
        int i;
        Object obj;
        Composer $composer2;
        Function0 function03;
        Composer composer;
        Function0 function04;
        Composer composer2;
        Function0 function05;
        Composer composer3;
        Intrinsics.checkNotNullParameter(function0, "onNavigateToLogin");
        Intrinsics.checkNotNullParameter(function02, "onNavigateToDashboard");
        Composer $composer3 = $composer.startRestartGroup(-2142861020);
        ComposerKt.sourceInformation($composer3, "C(SplashScreen)P(1)34@1457L7,35@1497L256,35@1469L284,45@1819L11,47@2070L11,48@2276L11,51@2458L1396:SplashScreen.kt#2thlc2");
        int $dirty = $changed;
        if (($changed & 6) == 0) {
            $dirty |= $composer3.changedInstance(function0) ? 4 : 2;
        }
        if (($changed & 48) == 0) {
            $dirty |= $composer3.changedInstance(function02) ? 32 : 16;
        }
        if (($dirty & 19) == 18 && $composer3.getSkipping()) {
            $composer3.skipToGroupEnd();
            $composer2 = $composer3;
        } else {
            if (ComposerKt.isTraceInProgress()) {
                ComposerKt.traceEventStart(-2142861020, $dirty, -1, "com.example.ui.screens.SplashScreen (SplashScreen.kt:33)");
            }
            CompositionLocal localContext = AndroidCompositionLocals_androidKt.getLocalContext();
            ComposerKt.sourceInformationMarkerStart($composer3, 2023513938, "CC:CompositionLocal.kt#9igjgp");
            Object consume = $composer3.consume(localContext);
            ComposerKt.sourceInformationMarkerEnd($composer3);
            Context context = (Context) consume;
            ComposerKt.sourceInformationMarkerStart($composer3, -2000098876, "CC(remember):SplashScreen.kt#9igjgp");
            boolean changedInstance = (($dirty & 14) == 4) | $composer3.changedInstance(context) | (($dirty & 112) == 32);
            Object rememberedValue = $composer3.rememberedValue();
            if (changedInstance) {
                i = 32;
            } else {
                i = 32;
                if (rememberedValue != Composer.Companion.getEmpty()) {
                    obj = rememberedValue;
                    ComposerKt.sourceInformationMarkerEnd($composer3);
                    EffectsKt.LaunchedEffect(true, (Function2) obj, $composer3, 6);
                    ColorKt.Color(ColorKt.luminance-8_81llA(MaterialTheme.INSTANCE.getColorScheme($composer3, MaterialTheme.$stable).getBackground-0d7_KjU()) >= 0.5f ? 4278255615L : 4278216447L);
                    ColorKt.Color(4294967295L);
                    long bgMain = ColorKt.Color(ColorKt.luminance-8_81llA(MaterialTheme.INSTANCE.getColorScheme($composer3, MaterialTheme.$stable).getBackground-0d7_KjU()) >= 0.5f ? 4278848010L : 4294244346L);
                    ColorKt.Color(ColorKt.luminance-8_81llA(MaterialTheme.INSTANCE.getColorScheme($composer3, MaterialTheme.$stable).getBackground-0d7_KjU()) >= 0.5f ? 4280222230L : 4294962165L);
                    ColorKt.Color(4279374354L);
                    Modifier modifier = BackgroundKt.background-bw27NRU$default(SizeKt.fillMaxSize$default(Modifier.Companion, 0.0f, 1, (Object) null), bgMain, (Shape) null, 2, (Object) null);
                    Alignment center = Alignment.Companion.getCenter();
                    ComposerKt.sourceInformationMarkerStart($composer3, 733328855, "CC(Box)P(2,1,3)72@3384L130:Box.kt#2w3rfo");
                    MeasurePolicy maybeCachedBoxMeasurePolicy = BoxKt.maybeCachedBoxMeasurePolicy(center, false);
                    $composer2 = $composer3;
                    ComposerKt.sourceInformationMarkerStart($composer3, -1323940314, "CC(Layout)P(!1,2)79@3208L23,82@3359L411:Layout.kt#80mrfh");
                    int currentCompositeKeyHash = ComposablesKt.getCurrentCompositeKeyHash($composer3, 0);
                    CompositionLocalMap currentCompositionLocalMap = $composer3.getCurrentCompositionLocalMap();
                    Modifier materializeModifier = ComposedModifierKt.materializeModifier($composer3, modifier);
                    Function0 constructor = ComposeUiNode.Companion.getConstructor();
                    int i2 = ((((48 << 3) & 112) << 6) & 896) | 6;
                    ComposerKt.sourceInformationMarkerStart($composer3, -692256719, "CC(ReusableComposeNode)P(1,2)376@14062L9:Composables.kt#9igjgp");
                    if (!($composer3.getApplier() instanceof Applier)) {
                        ComposablesKt.invalidApplier();
                    }
                    $composer3.startReusableNode();
                    if (!$composer3.getInserting()) {
                        function03 = constructor;
                        $composer3.createNode(function03);
                    } else {
                        function03 = constructor;
                        $composer3.useNode();
                    }
                    composer = Updater.constructor-impl($composer3);
                    Updater.set-impl(composer, maybeCachedBoxMeasurePolicy, ComposeUiNode.Companion.getSetMeasurePolicy());
                    Updater.set-impl(composer, currentCompositionLocalMap, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
                    Function2 setCompositeKeyHash = ComposeUiNode.Companion.getSetCompositeKeyHash();
                    if (!composer.getInserting() && Intrinsics.areEqual(composer.rememberedValue(), Integer.valueOf(currentCompositeKeyHash))) {
                        Updater.set-impl(composer, materializeModifier, ComposeUiNode.Companion.getSetModifier());
                        int i3 = (i2 >> 6) & 14;
                        ComposerKt.sourceInformationMarkerStart($composer3, -2146769399, "C73@3429L9:Box.kt#2w3rfo");
                        BoxScope boxScope = BoxScopeInstance.INSTANCE;
                        int i4 = ((48 >> 6) & 112) | 6;
                        ComposerKt.sourceInformationMarkerStart($composer3, 1913196068, "C57@2611L1237:SplashScreen.kt#2thlc2");
                        Alignment.Horizontal centerHorizontally = Alignment.Companion.getCenterHorizontally();
                        Arrangement.Vertical center2 = Arrangement.INSTANCE.getCenter();
                        ComposerKt.sourceInformationMarkerStart($composer3, -483455358, "CC(Column)P(2,3,1)86@4330L61,87@4396L133:Column.kt#2w3rfo");
                        Modifier modifier2 = Modifier.Companion;
                        MeasurePolicy columnMeasurePolicy = ColumnKt.columnMeasurePolicy(center2, centerHorizontally, $composer3, ((432 >> 3) & 14) | ((432 >> 3) & 112));
                        ComposerKt.sourceInformationMarkerStart($composer3, -1323940314, "CC(Layout)P(!1,2)79@3208L23,82@3359L411:Layout.kt#80mrfh");
                        int currentCompositeKeyHash2 = ComposablesKt.getCurrentCompositeKeyHash($composer3, 0);
                        CompositionLocalMap currentCompositionLocalMap2 = $composer3.getCurrentCompositionLocalMap();
                        Modifier materializeModifier2 = ComposedModifierKt.materializeModifier($composer3, modifier2);
                        Function0 constructor2 = ComposeUiNode.Companion.getConstructor();
                        int i5 = ((((432 << 3) & 112) << 6) & 896) | 6;
                        ComposerKt.sourceInformationMarkerStart($composer3, -692256719, "CC(ReusableComposeNode)P(1,2)376@14062L9:Composables.kt#9igjgp");
                        if (!($composer3.getApplier() instanceof Applier)) {
                            ComposablesKt.invalidApplier();
                        }
                        $composer3.startReusableNode();
                        if (!$composer3.getInserting()) {
                            function04 = constructor2;
                            $composer3.createNode(function04);
                        } else {
                            function04 = constructor2;
                            $composer3.useNode();
                        }
                        composer2 = Updater.constructor-impl($composer3);
                        Updater.set-impl(composer2, columnMeasurePolicy, ComposeUiNode.Companion.getSetMeasurePolicy());
                        Updater.set-impl(composer2, currentCompositionLocalMap2, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
                        Function2 setCompositeKeyHash2 = ComposeUiNode.Companion.getSetCompositeKeyHash();
                        if (!composer2.getInserting() && Intrinsics.areEqual(composer2.rememberedValue(), Integer.valueOf(currentCompositeKeyHash2))) {
                            Updater.set-impl(composer2, materializeModifier2, ComposeUiNode.Companion.getSetModifier());
                            int i6 = (i5 >> 6) & 14;
                            ComposerKt.sourceInformationMarkerStart($composer3, -384784025, "C88@4444L9:Column.kt#2w3rfo");
                            ColumnScope columnScope = ColumnScopeInstance.INSTANCE;
                            int i7 = ((432 >> 6) & 112) | 6;
                            ComposerKt.sourceInformationMarkerStart($composer3, -455789651, "C61@2760L379,72@3152L41,77@3433L11,73@3206L376,79@3595L40,80@3648L190:SplashScreen.kt#2thlc2");
                            Modifier modifier3 = SizeKt.size-3ABfNKs(Modifier.Companion, Dp.constructor-impl(120));
                            Alignment center3 = Alignment.Companion.getCenter();
                            ComposerKt.sourceInformationMarkerStart($composer3, 733328855, "CC(Box)P(2,1,3)72@3384L130:Box.kt#2w3rfo");
                            MeasurePolicy maybeCachedBoxMeasurePolicy2 = BoxKt.maybeCachedBoxMeasurePolicy(center3, false);
                            ComposerKt.sourceInformationMarkerStart($composer3, -1323940314, "CC(Layout)P(!1,2)79@3208L23,82@3359L411:Layout.kt#80mrfh");
                            int currentCompositeKeyHash3 = ComposablesKt.getCurrentCompositeKeyHash($composer3, 0);
                            CompositionLocalMap currentCompositionLocalMap3 = $composer3.getCurrentCompositionLocalMap();
                            Modifier materializeModifier3 = ComposedModifierKt.materializeModifier($composer3, modifier3);
                            Function0 constructor3 = ComposeUiNode.Companion.getConstructor();
                            int i8 = ((((54 << 3) & 112) << 6) & 896) | 6;
                            ComposerKt.sourceInformationMarkerStart($composer3, -692256719, "CC(ReusableComposeNode)P(1,2)376@14062L9:Composables.kt#9igjgp");
                            if (!($composer3.getApplier() instanceof Applier)) {
                                ComposablesKt.invalidApplier();
                            }
                            $composer3.startReusableNode();
                            if (!$composer3.getInserting()) {
                                function05 = constructor3;
                                $composer3.createNode(function05);
                            } else {
                                function05 = constructor3;
                                $composer3.useNode();
                            }
                            composer3 = Updater.constructor-impl($composer3);
                            Updater.set-impl(composer3, maybeCachedBoxMeasurePolicy2, ComposeUiNode.Companion.getSetMeasurePolicy());
                            Updater.set-impl(composer3, currentCompositionLocalMap3, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
                            Function2 setCompositeKeyHash3 = ComposeUiNode.Companion.getSetCompositeKeyHash();
                            if (!composer3.getInserting() && Intrinsics.areEqual(composer3.rememberedValue(), Integer.valueOf(currentCompositeKeyHash3))) {
                                Updater.set-impl(composer3, materializeModifier3, ComposeUiNode.Companion.getSetModifier());
                                int i9 = (i8 >> 6) & 14;
                                ComposerKt.sourceInformationMarkerStart($composer3, -2146769399, "C73@3429L9:Box.kt#2w3rfo");
                                BoxScope boxScope2 = BoxScopeInstance.INSTANCE;
                                int i10 = ((54 >> 6) & 112) | 6;
                                ComposerKt.sourceInformationMarkerStart($composer3, 1867339824, "C67@2957L47,66@2920L205:SplashScreen.kt#2thlc2");
                                ImageKt.Image(PainterResources_androidKt.painterResource(R.drawable.ic_logo_splash, $composer3, 0), "Logo", SizeKt.size-3ABfNKs(Modifier.Companion, Dp.constructor-impl(100)), (Alignment) null, (ContentScale) null, 0.0f, (ColorFilter) null, $composer3, 432, 120);
                                ComposerKt.sourceInformationMarkerEnd($composer3);
                                ComposerKt.sourceInformationMarkerEnd($composer3);
                                $composer3.endNode();
                                ComposerKt.sourceInformationMarkerEnd($composer3);
                                ComposerKt.sourceInformationMarkerEnd($composer3);
                                ComposerKt.sourceInformationMarkerEnd($composer3);
                                SpacerKt.Spacer(SizeKt.height-3ABfNKs(Modifier.Companion, Dp.constructor-impl(24)), $composer3, 6);
                                TextKt.Text--4IGK_g(SettingsManager.INSTANCE.getCompanyName(), (Modifier) null, ColorKt.luminance-8_81llA(MaterialTheme.INSTANCE.getColorScheme($composer3, MaterialTheme.$stable).getBackground-0d7_KjU()) >= 0.5f ? Color.Companion.getWhite-0d7_KjU() : ColorKt.Color(4279900698L), TextUnitKt.getSp(i), (FontStyle) null, FontWeight.Companion.getBold(), (FontFamily) null, 0L, (TextDecoration) null, (TextAlign) null, 0L, 0, false, 0, 0, (Function1) null, (TextStyle) null, $composer3, 199680, 0, 131026);
                                SpacerKt.Spacer(SizeKt.height-3ABfNKs(Modifier.Companion, Dp.constructor-impl(8)), $composer3, 6);
                                TextKt.Text--4IGK_g("Billing System v1.2a", (Modifier) null, ColorKt.Color(4289374890L), TextUnitKt.getSp(16), (FontStyle) null, FontWeight.Companion.getMedium(), (FontFamily) null, 0L, (TextDecoration) null, (TextAlign) null, 0L, 0, false, 0, 0, (Function1) null, (TextStyle) null, $composer3, 200070, 0, 131026);
                                ComposerKt.sourceInformationMarkerEnd($composer3);
                                ComposerKt.sourceInformationMarkerEnd($composer3);
                                $composer3.endNode();
                                ComposerKt.sourceInformationMarkerEnd($composer3);
                                ComposerKt.sourceInformationMarkerEnd($composer3);
                                ComposerKt.sourceInformationMarkerEnd($composer3);
                                ComposerKt.sourceInformationMarkerEnd($composer3);
                                ComposerKt.sourceInformationMarkerEnd($composer3);
                                $composer3.endNode();
                                ComposerKt.sourceInformationMarkerEnd($composer3);
                                ComposerKt.sourceInformationMarkerEnd($composer3);
                                ComposerKt.sourceInformationMarkerEnd($composer3);
                                if (ComposerKt.isTraceInProgress()) {
                                    ComposerKt.traceEventEnd();
                                }
                            }
                            composer3.updateRememberedValue(Integer.valueOf(currentCompositeKeyHash3));
                            composer3.apply(Integer.valueOf(currentCompositeKeyHash3), setCompositeKeyHash3);
                            Updater.set-impl(composer3, materializeModifier3, ComposeUiNode.Companion.getSetModifier());
                            int i92 = (i8 >> 6) & 14;
                            ComposerKt.sourceInformationMarkerStart($composer3, -2146769399, "C73@3429L9:Box.kt#2w3rfo");
                            BoxScope boxScope22 = BoxScopeInstance.INSTANCE;
                            int i102 = ((54 >> 6) & 112) | 6;
                            ComposerKt.sourceInformationMarkerStart($composer3, 1867339824, "C67@2957L47,66@2920L205:SplashScreen.kt#2thlc2");
                            ImageKt.Image(PainterResources_androidKt.painterResource(R.drawable.ic_logo_splash, $composer3, 0), "Logo", SizeKt.size-3ABfNKs(Modifier.Companion, Dp.constructor-impl(100)), (Alignment) null, (ContentScale) null, 0.0f, (ColorFilter) null, $composer3, 432, 120);
                            ComposerKt.sourceInformationMarkerEnd($composer3);
                            ComposerKt.sourceInformationMarkerEnd($composer3);
                            $composer3.endNode();
                            ComposerKt.sourceInformationMarkerEnd($composer3);
                            ComposerKt.sourceInformationMarkerEnd($composer3);
                            ComposerKt.sourceInformationMarkerEnd($composer3);
                            SpacerKt.Spacer(SizeKt.height-3ABfNKs(Modifier.Companion, Dp.constructor-impl(24)), $composer3, 6);
                            TextKt.Text--4IGK_g(SettingsManager.INSTANCE.getCompanyName(), (Modifier) null, ColorKt.luminance-8_81llA(MaterialTheme.INSTANCE.getColorScheme($composer3, MaterialTheme.$stable).getBackground-0d7_KjU()) >= 0.5f ? Color.Companion.getWhite-0d7_KjU() : ColorKt.Color(4279900698L), TextUnitKt.getSp(i), (FontStyle) null, FontWeight.Companion.getBold(), (FontFamily) null, 0L, (TextDecoration) null, (TextAlign) null, 0L, 0, false, 0, 0, (Function1) null, (TextStyle) null, $composer3, 199680, 0, 131026);
                            SpacerKt.Spacer(SizeKt.height-3ABfNKs(Modifier.Companion, Dp.constructor-impl(8)), $composer3, 6);
                            TextKt.Text--4IGK_g("Billing System v1.2a", (Modifier) null, ColorKt.Color(4289374890L), TextUnitKt.getSp(16), (FontStyle) null, FontWeight.Companion.getMedium(), (FontFamily) null, 0L, (TextDecoration) null, (TextAlign) null, 0L, 0, false, 0, 0, (Function1) null, (TextStyle) null, $composer3, 200070, 0, 131026);
                            ComposerKt.sourceInformationMarkerEnd($composer3);
                            ComposerKt.sourceInformationMarkerEnd($composer3);
                            $composer3.endNode();
                            ComposerKt.sourceInformationMarkerEnd($composer3);
                            ComposerKt.sourceInformationMarkerEnd($composer3);
                            ComposerKt.sourceInformationMarkerEnd($composer3);
                            ComposerKt.sourceInformationMarkerEnd($composer3);
                            ComposerKt.sourceInformationMarkerEnd($composer3);
                            $composer3.endNode();
                            ComposerKt.sourceInformationMarkerEnd($composer3);
                            ComposerKt.sourceInformationMarkerEnd($composer3);
                            ComposerKt.sourceInformationMarkerEnd($composer3);
                            if (ComposerKt.isTraceInProgress()) {
                            }
                        }
                        composer2.updateRememberedValue(Integer.valueOf(currentCompositeKeyHash2));
                        composer2.apply(Integer.valueOf(currentCompositeKeyHash2), setCompositeKeyHash2);
                        Updater.set-impl(composer2, materializeModifier2, ComposeUiNode.Companion.getSetModifier());
                        int i62 = (i5 >> 6) & 14;
                        ComposerKt.sourceInformationMarkerStart($composer3, -384784025, "C88@4444L9:Column.kt#2w3rfo");
                        ColumnScope columnScope2 = ColumnScopeInstance.INSTANCE;
                        int i72 = ((432 >> 6) & 112) | 6;
                        ComposerKt.sourceInformationMarkerStart($composer3, -455789651, "C61@2760L379,72@3152L41,77@3433L11,73@3206L376,79@3595L40,80@3648L190:SplashScreen.kt#2thlc2");
                        Modifier modifier32 = SizeKt.size-3ABfNKs(Modifier.Companion, Dp.constructor-impl(120));
                        Alignment center32 = Alignment.Companion.getCenter();
                        ComposerKt.sourceInformationMarkerStart($composer3, 733328855, "CC(Box)P(2,1,3)72@3384L130:Box.kt#2w3rfo");
                        MeasurePolicy maybeCachedBoxMeasurePolicy22 = BoxKt.maybeCachedBoxMeasurePolicy(center32, false);
                        ComposerKt.sourceInformationMarkerStart($composer3, -1323940314, "CC(Layout)P(!1,2)79@3208L23,82@3359L411:Layout.kt#80mrfh");
                        int currentCompositeKeyHash32 = ComposablesKt.getCurrentCompositeKeyHash($composer3, 0);
                        CompositionLocalMap currentCompositionLocalMap32 = $composer3.getCurrentCompositionLocalMap();
                        Modifier materializeModifier32 = ComposedModifierKt.materializeModifier($composer3, modifier32);
                        Function0 constructor32 = ComposeUiNode.Companion.getConstructor();
                        int i82 = ((((54 << 3) & 112) << 6) & 896) | 6;
                        ComposerKt.sourceInformationMarkerStart($composer3, -692256719, "CC(ReusableComposeNode)P(1,2)376@14062L9:Composables.kt#9igjgp");
                        if (!($composer3.getApplier() instanceof Applier)) {
                        }
                        $composer3.startReusableNode();
                        if (!$composer3.getInserting()) {
                        }
                        composer3 = Updater.constructor-impl($composer3);
                        Updater.set-impl(composer3, maybeCachedBoxMeasurePolicy22, ComposeUiNode.Companion.getSetMeasurePolicy());
                        Updater.set-impl(composer3, currentCompositionLocalMap32, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
                        Function2 setCompositeKeyHash32 = ComposeUiNode.Companion.getSetCompositeKeyHash();
                        if (!composer3.getInserting()) {
                            Updater.set-impl(composer3, materializeModifier32, ComposeUiNode.Companion.getSetModifier());
                            int i922 = (i82 >> 6) & 14;
                            ComposerKt.sourceInformationMarkerStart($composer3, -2146769399, "C73@3429L9:Box.kt#2w3rfo");
                            BoxScope boxScope222 = BoxScopeInstance.INSTANCE;
                            int i1022 = ((54 >> 6) & 112) | 6;
                            ComposerKt.sourceInformationMarkerStart($composer3, 1867339824, "C67@2957L47,66@2920L205:SplashScreen.kt#2thlc2");
                            ImageKt.Image(PainterResources_androidKt.painterResource(R.drawable.ic_logo_splash, $composer3, 0), "Logo", SizeKt.size-3ABfNKs(Modifier.Companion, Dp.constructor-impl(100)), (Alignment) null, (ContentScale) null, 0.0f, (ColorFilter) null, $composer3, 432, 120);
                            ComposerKt.sourceInformationMarkerEnd($composer3);
                            ComposerKt.sourceInformationMarkerEnd($composer3);
                            $composer3.endNode();
                            ComposerKt.sourceInformationMarkerEnd($composer3);
                            ComposerKt.sourceInformationMarkerEnd($composer3);
                            ComposerKt.sourceInformationMarkerEnd($composer3);
                            SpacerKt.Spacer(SizeKt.height-3ABfNKs(Modifier.Companion, Dp.constructor-impl(24)), $composer3, 6);
                            TextKt.Text--4IGK_g(SettingsManager.INSTANCE.getCompanyName(), (Modifier) null, ColorKt.luminance-8_81llA(MaterialTheme.INSTANCE.getColorScheme($composer3, MaterialTheme.$stable).getBackground-0d7_KjU()) >= 0.5f ? Color.Companion.getWhite-0d7_KjU() : ColorKt.Color(4279900698L), TextUnitKt.getSp(i), (FontStyle) null, FontWeight.Companion.getBold(), (FontFamily) null, 0L, (TextDecoration) null, (TextAlign) null, 0L, 0, false, 0, 0, (Function1) null, (TextStyle) null, $composer3, 199680, 0, 131026);
                            SpacerKt.Spacer(SizeKt.height-3ABfNKs(Modifier.Companion, Dp.constructor-impl(8)), $composer3, 6);
                            TextKt.Text--4IGK_g("Billing System v1.2a", (Modifier) null, ColorKt.Color(4289374890L), TextUnitKt.getSp(16), (FontStyle) null, FontWeight.Companion.getMedium(), (FontFamily) null, 0L, (TextDecoration) null, (TextAlign) null, 0L, 0, false, 0, 0, (Function1) null, (TextStyle) null, $composer3, 200070, 0, 131026);
                            ComposerKt.sourceInformationMarkerEnd($composer3);
                            ComposerKt.sourceInformationMarkerEnd($composer3);
                            $composer3.endNode();
                            ComposerKt.sourceInformationMarkerEnd($composer3);
                            ComposerKt.sourceInformationMarkerEnd($composer3);
                            ComposerKt.sourceInformationMarkerEnd($composer3);
                            ComposerKt.sourceInformationMarkerEnd($composer3);
                            ComposerKt.sourceInformationMarkerEnd($composer3);
                            $composer3.endNode();
                            ComposerKt.sourceInformationMarkerEnd($composer3);
                            ComposerKt.sourceInformationMarkerEnd($composer3);
                            ComposerKt.sourceInformationMarkerEnd($composer3);
                            if (ComposerKt.isTraceInProgress()) {
                            }
                        }
                        composer3.updateRememberedValue(Integer.valueOf(currentCompositeKeyHash32));
                        composer3.apply(Integer.valueOf(currentCompositeKeyHash32), setCompositeKeyHash32);
                        Updater.set-impl(composer3, materializeModifier32, ComposeUiNode.Companion.getSetModifier());
                        int i9222 = (i82 >> 6) & 14;
                        ComposerKt.sourceInformationMarkerStart($composer3, -2146769399, "C73@3429L9:Box.kt#2w3rfo");
                        BoxScope boxScope2222 = BoxScopeInstance.INSTANCE;
                        int i10222 = ((54 >> 6) & 112) | 6;
                        ComposerKt.sourceInformationMarkerStart($composer3, 1867339824, "C67@2957L47,66@2920L205:SplashScreen.kt#2thlc2");
                        ImageKt.Image(PainterResources_androidKt.painterResource(R.drawable.ic_logo_splash, $composer3, 0), "Logo", SizeKt.size-3ABfNKs(Modifier.Companion, Dp.constructor-impl(100)), (Alignment) null, (ContentScale) null, 0.0f, (ColorFilter) null, $composer3, 432, 120);
                        ComposerKt.sourceInformationMarkerEnd($composer3);
                        ComposerKt.sourceInformationMarkerEnd($composer3);
                        $composer3.endNode();
                        ComposerKt.sourceInformationMarkerEnd($composer3);
                        ComposerKt.sourceInformationMarkerEnd($composer3);
                        ComposerKt.sourceInformationMarkerEnd($composer3);
                        SpacerKt.Spacer(SizeKt.height-3ABfNKs(Modifier.Companion, Dp.constructor-impl(24)), $composer3, 6);
                        TextKt.Text--4IGK_g(SettingsManager.INSTANCE.getCompanyName(), (Modifier) null, ColorKt.luminance-8_81llA(MaterialTheme.INSTANCE.getColorScheme($composer3, MaterialTheme.$stable).getBackground-0d7_KjU()) >= 0.5f ? Color.Companion.getWhite-0d7_KjU() : ColorKt.Color(4279900698L), TextUnitKt.getSp(i), (FontStyle) null, FontWeight.Companion.getBold(), (FontFamily) null, 0L, (TextDecoration) null, (TextAlign) null, 0L, 0, false, 0, 0, (Function1) null, (TextStyle) null, $composer3, 199680, 0, 131026);
                        SpacerKt.Spacer(SizeKt.height-3ABfNKs(Modifier.Companion, Dp.constructor-impl(8)), $composer3, 6);
                        TextKt.Text--4IGK_g("Billing System v1.2a", (Modifier) null, ColorKt.Color(4289374890L), TextUnitKt.getSp(16), (FontStyle) null, FontWeight.Companion.getMedium(), (FontFamily) null, 0L, (TextDecoration) null, (TextAlign) null, 0L, 0, false, 0, 0, (Function1) null, (TextStyle) null, $composer3, 200070, 0, 131026);
                        ComposerKt.sourceInformationMarkerEnd($composer3);
                        ComposerKt.sourceInformationMarkerEnd($composer3);
                        $composer3.endNode();
                        ComposerKt.sourceInformationMarkerEnd($composer3);
                        ComposerKt.sourceInformationMarkerEnd($composer3);
                        ComposerKt.sourceInformationMarkerEnd($composer3);
                        ComposerKt.sourceInformationMarkerEnd($composer3);
                        ComposerKt.sourceInformationMarkerEnd($composer3);
                        $composer3.endNode();
                        ComposerKt.sourceInformationMarkerEnd($composer3);
                        ComposerKt.sourceInformationMarkerEnd($composer3);
                        ComposerKt.sourceInformationMarkerEnd($composer3);
                        if (ComposerKt.isTraceInProgress()) {
                        }
                    }
                    composer.updateRememberedValue(Integer.valueOf(currentCompositeKeyHash));
                    composer.apply(Integer.valueOf(currentCompositeKeyHash), setCompositeKeyHash);
                    Updater.set-impl(composer, materializeModifier, ComposeUiNode.Companion.getSetModifier());
                    int i32 = (i2 >> 6) & 14;
                    ComposerKt.sourceInformationMarkerStart($composer3, -2146769399, "C73@3429L9:Box.kt#2w3rfo");
                    BoxScope boxScope3 = BoxScopeInstance.INSTANCE;
                    int i42 = ((48 >> 6) & 112) | 6;
                    ComposerKt.sourceInformationMarkerStart($composer3, 1913196068, "C57@2611L1237:SplashScreen.kt#2thlc2");
                    Alignment.Horizontal centerHorizontally2 = Alignment.Companion.getCenterHorizontally();
                    Arrangement.Vertical center22 = Arrangement.INSTANCE.getCenter();
                    ComposerKt.sourceInformationMarkerStart($composer3, -483455358, "CC(Column)P(2,3,1)86@4330L61,87@4396L133:Column.kt#2w3rfo");
                    Modifier modifier22 = Modifier.Companion;
                    MeasurePolicy columnMeasurePolicy2 = ColumnKt.columnMeasurePolicy(center22, centerHorizontally2, $composer3, ((432 >> 3) & 14) | ((432 >> 3) & 112));
                    ComposerKt.sourceInformationMarkerStart($composer3, -1323940314, "CC(Layout)P(!1,2)79@3208L23,82@3359L411:Layout.kt#80mrfh");
                    int currentCompositeKeyHash22 = ComposablesKt.getCurrentCompositeKeyHash($composer3, 0);
                    CompositionLocalMap currentCompositionLocalMap22 = $composer3.getCurrentCompositionLocalMap();
                    Modifier materializeModifier22 = ComposedModifierKt.materializeModifier($composer3, modifier22);
                    Function0 constructor22 = ComposeUiNode.Companion.getConstructor();
                    int i52 = ((((432 << 3) & 112) << 6) & 896) | 6;
                    ComposerKt.sourceInformationMarkerStart($composer3, -692256719, "CC(ReusableComposeNode)P(1,2)376@14062L9:Composables.kt#9igjgp");
                    if (!($composer3.getApplier() instanceof Applier)) {
                    }
                    $composer3.startReusableNode();
                    if (!$composer3.getInserting()) {
                    }
                    composer2 = Updater.constructor-impl($composer3);
                    Updater.set-impl(composer2, columnMeasurePolicy2, ComposeUiNode.Companion.getSetMeasurePolicy());
                    Updater.set-impl(composer2, currentCompositionLocalMap22, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
                    Function2 setCompositeKeyHash22 = ComposeUiNode.Companion.getSetCompositeKeyHash();
                    if (!composer2.getInserting()) {
                        Updater.set-impl(composer2, materializeModifier22, ComposeUiNode.Companion.getSetModifier());
                        int i622 = (i52 >> 6) & 14;
                        ComposerKt.sourceInformationMarkerStart($composer3, -384784025, "C88@4444L9:Column.kt#2w3rfo");
                        ColumnScope columnScope22 = ColumnScopeInstance.INSTANCE;
                        int i722 = ((432 >> 6) & 112) | 6;
                        ComposerKt.sourceInformationMarkerStart($composer3, -455789651, "C61@2760L379,72@3152L41,77@3433L11,73@3206L376,79@3595L40,80@3648L190:SplashScreen.kt#2thlc2");
                        Modifier modifier322 = SizeKt.size-3ABfNKs(Modifier.Companion, Dp.constructor-impl(120));
                        Alignment center322 = Alignment.Companion.getCenter();
                        ComposerKt.sourceInformationMarkerStart($composer3, 733328855, "CC(Box)P(2,1,3)72@3384L130:Box.kt#2w3rfo");
                        MeasurePolicy maybeCachedBoxMeasurePolicy222 = BoxKt.maybeCachedBoxMeasurePolicy(center322, false);
                        ComposerKt.sourceInformationMarkerStart($composer3, -1323940314, "CC(Layout)P(!1,2)79@3208L23,82@3359L411:Layout.kt#80mrfh");
                        int currentCompositeKeyHash322 = ComposablesKt.getCurrentCompositeKeyHash($composer3, 0);
                        CompositionLocalMap currentCompositionLocalMap322 = $composer3.getCurrentCompositionLocalMap();
                        Modifier materializeModifier322 = ComposedModifierKt.materializeModifier($composer3, modifier322);
                        Function0 constructor322 = ComposeUiNode.Companion.getConstructor();
                        int i822 = ((((54 << 3) & 112) << 6) & 896) | 6;
                        ComposerKt.sourceInformationMarkerStart($composer3, -692256719, "CC(ReusableComposeNode)P(1,2)376@14062L9:Composables.kt#9igjgp");
                        if (!($composer3.getApplier() instanceof Applier)) {
                        }
                        $composer3.startReusableNode();
                        if (!$composer3.getInserting()) {
                        }
                        composer3 = Updater.constructor-impl($composer3);
                        Updater.set-impl(composer3, maybeCachedBoxMeasurePolicy222, ComposeUiNode.Companion.getSetMeasurePolicy());
                        Updater.set-impl(composer3, currentCompositionLocalMap322, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
                        Function2 setCompositeKeyHash322 = ComposeUiNode.Companion.getSetCompositeKeyHash();
                        if (!composer3.getInserting()) {
                        }
                        composer3.updateRememberedValue(Integer.valueOf(currentCompositeKeyHash322));
                        composer3.apply(Integer.valueOf(currentCompositeKeyHash322), setCompositeKeyHash322);
                        Updater.set-impl(composer3, materializeModifier322, ComposeUiNode.Companion.getSetModifier());
                        int i92222 = (i822 >> 6) & 14;
                        ComposerKt.sourceInformationMarkerStart($composer3, -2146769399, "C73@3429L9:Box.kt#2w3rfo");
                        BoxScope boxScope22222 = BoxScopeInstance.INSTANCE;
                        int i102222 = ((54 >> 6) & 112) | 6;
                        ComposerKt.sourceInformationMarkerStart($composer3, 1867339824, "C67@2957L47,66@2920L205:SplashScreen.kt#2thlc2");
                        ImageKt.Image(PainterResources_androidKt.painterResource(R.drawable.ic_logo_splash, $composer3, 0), "Logo", SizeKt.size-3ABfNKs(Modifier.Companion, Dp.constructor-impl(100)), (Alignment) null, (ContentScale) null, 0.0f, (ColorFilter) null, $composer3, 432, 120);
                        ComposerKt.sourceInformationMarkerEnd($composer3);
                        ComposerKt.sourceInformationMarkerEnd($composer3);
                        $composer3.endNode();
                        ComposerKt.sourceInformationMarkerEnd($composer3);
                        ComposerKt.sourceInformationMarkerEnd($composer3);
                        ComposerKt.sourceInformationMarkerEnd($composer3);
                        SpacerKt.Spacer(SizeKt.height-3ABfNKs(Modifier.Companion, Dp.constructor-impl(24)), $composer3, 6);
                        TextKt.Text--4IGK_g(SettingsManager.INSTANCE.getCompanyName(), (Modifier) null, ColorKt.luminance-8_81llA(MaterialTheme.INSTANCE.getColorScheme($composer3, MaterialTheme.$stable).getBackground-0d7_KjU()) >= 0.5f ? Color.Companion.getWhite-0d7_KjU() : ColorKt.Color(4279900698L), TextUnitKt.getSp(i), (FontStyle) null, FontWeight.Companion.getBold(), (FontFamily) null, 0L, (TextDecoration) null, (TextAlign) null, 0L, 0, false, 0, 0, (Function1) null, (TextStyle) null, $composer3, 199680, 0, 131026);
                        SpacerKt.Spacer(SizeKt.height-3ABfNKs(Modifier.Companion, Dp.constructor-impl(8)), $composer3, 6);
                        TextKt.Text--4IGK_g("Billing System v1.2a", (Modifier) null, ColorKt.Color(4289374890L), TextUnitKt.getSp(16), (FontStyle) null, FontWeight.Companion.getMedium(), (FontFamily) null, 0L, (TextDecoration) null, (TextAlign) null, 0L, 0, false, 0, 0, (Function1) null, (TextStyle) null, $composer3, 200070, 0, 131026);
                        ComposerKt.sourceInformationMarkerEnd($composer3);
                        ComposerKt.sourceInformationMarkerEnd($composer3);
                        $composer3.endNode();
                        ComposerKt.sourceInformationMarkerEnd($composer3);
                        ComposerKt.sourceInformationMarkerEnd($composer3);
                        ComposerKt.sourceInformationMarkerEnd($composer3);
                        ComposerKt.sourceInformationMarkerEnd($composer3);
                        ComposerKt.sourceInformationMarkerEnd($composer3);
                        $composer3.endNode();
                        ComposerKt.sourceInformationMarkerEnd($composer3);
                        ComposerKt.sourceInformationMarkerEnd($composer3);
                        ComposerKt.sourceInformationMarkerEnd($composer3);
                        if (ComposerKt.isTraceInProgress()) {
                        }
                    }
                    composer2.updateRememberedValue(Integer.valueOf(currentCompositeKeyHash22));
                    composer2.apply(Integer.valueOf(currentCompositeKeyHash22), setCompositeKeyHash22);
                    Updater.set-impl(composer2, materializeModifier22, ComposeUiNode.Companion.getSetModifier());
                    int i6222 = (i52 >> 6) & 14;
                    ComposerKt.sourceInformationMarkerStart($composer3, -384784025, "C88@4444L9:Column.kt#2w3rfo");
                    ColumnScope columnScope222 = ColumnScopeInstance.INSTANCE;
                    int i7222 = ((432 >> 6) & 112) | 6;
                    ComposerKt.sourceInformationMarkerStart($composer3, -455789651, "C61@2760L379,72@3152L41,77@3433L11,73@3206L376,79@3595L40,80@3648L190:SplashScreen.kt#2thlc2");
                    Modifier modifier3222 = SizeKt.size-3ABfNKs(Modifier.Companion, Dp.constructor-impl(120));
                    Alignment center3222 = Alignment.Companion.getCenter();
                    ComposerKt.sourceInformationMarkerStart($composer3, 733328855, "CC(Box)P(2,1,3)72@3384L130:Box.kt#2w3rfo");
                    MeasurePolicy maybeCachedBoxMeasurePolicy2222 = BoxKt.maybeCachedBoxMeasurePolicy(center3222, false);
                    ComposerKt.sourceInformationMarkerStart($composer3, -1323940314, "CC(Layout)P(!1,2)79@3208L23,82@3359L411:Layout.kt#80mrfh");
                    int currentCompositeKeyHash3222 = ComposablesKt.getCurrentCompositeKeyHash($composer3, 0);
                    CompositionLocalMap currentCompositionLocalMap3222 = $composer3.getCurrentCompositionLocalMap();
                    Modifier materializeModifier3222 = ComposedModifierKt.materializeModifier($composer3, modifier3222);
                    Function0 constructor3222 = ComposeUiNode.Companion.getConstructor();
                    int i8222 = ((((54 << 3) & 112) << 6) & 896) | 6;
                    ComposerKt.sourceInformationMarkerStart($composer3, -692256719, "CC(ReusableComposeNode)P(1,2)376@14062L9:Composables.kt#9igjgp");
                    if (!($composer3.getApplier() instanceof Applier)) {
                    }
                    $composer3.startReusableNode();
                    if (!$composer3.getInserting()) {
                    }
                    composer3 = Updater.constructor-impl($composer3);
                    Updater.set-impl(composer3, maybeCachedBoxMeasurePolicy2222, ComposeUiNode.Companion.getSetMeasurePolicy());
                    Updater.set-impl(composer3, currentCompositionLocalMap3222, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
                    Function2 setCompositeKeyHash3222 = ComposeUiNode.Companion.getSetCompositeKeyHash();
                    if (!composer3.getInserting()) {
                    }
                    composer3.updateRememberedValue(Integer.valueOf(currentCompositeKeyHash3222));
                    composer3.apply(Integer.valueOf(currentCompositeKeyHash3222), setCompositeKeyHash3222);
                    Updater.set-impl(composer3, materializeModifier3222, ComposeUiNode.Companion.getSetModifier());
                    int i922222 = (i8222 >> 6) & 14;
                    ComposerKt.sourceInformationMarkerStart($composer3, -2146769399, "C73@3429L9:Box.kt#2w3rfo");
                    BoxScope boxScope222222 = BoxScopeInstance.INSTANCE;
                    int i1022222 = ((54 >> 6) & 112) | 6;
                    ComposerKt.sourceInformationMarkerStart($composer3, 1867339824, "C67@2957L47,66@2920L205:SplashScreen.kt#2thlc2");
                    ImageKt.Image(PainterResources_androidKt.painterResource(R.drawable.ic_logo_splash, $composer3, 0), "Logo", SizeKt.size-3ABfNKs(Modifier.Companion, Dp.constructor-impl(100)), (Alignment) null, (ContentScale) null, 0.0f, (ColorFilter) null, $composer3, 432, 120);
                    ComposerKt.sourceInformationMarkerEnd($composer3);
                    ComposerKt.sourceInformationMarkerEnd($composer3);
                    $composer3.endNode();
                    ComposerKt.sourceInformationMarkerEnd($composer3);
                    ComposerKt.sourceInformationMarkerEnd($composer3);
                    ComposerKt.sourceInformationMarkerEnd($composer3);
                    SpacerKt.Spacer(SizeKt.height-3ABfNKs(Modifier.Companion, Dp.constructor-impl(24)), $composer3, 6);
                    TextKt.Text--4IGK_g(SettingsManager.INSTANCE.getCompanyName(), (Modifier) null, ColorKt.luminance-8_81llA(MaterialTheme.INSTANCE.getColorScheme($composer3, MaterialTheme.$stable).getBackground-0d7_KjU()) >= 0.5f ? Color.Companion.getWhite-0d7_KjU() : ColorKt.Color(4279900698L), TextUnitKt.getSp(i), (FontStyle) null, FontWeight.Companion.getBold(), (FontFamily) null, 0L, (TextDecoration) null, (TextAlign) null, 0L, 0, false, 0, 0, (Function1) null, (TextStyle) null, $composer3, 199680, 0, 131026);
                    SpacerKt.Spacer(SizeKt.height-3ABfNKs(Modifier.Companion, Dp.constructor-impl(8)), $composer3, 6);
                    TextKt.Text--4IGK_g("Billing System v1.2a", (Modifier) null, ColorKt.Color(4289374890L), TextUnitKt.getSp(16), (FontStyle) null, FontWeight.Companion.getMedium(), (FontFamily) null, 0L, (TextDecoration) null, (TextAlign) null, 0L, 0, false, 0, 0, (Function1) null, (TextStyle) null, $composer3, 200070, 0, 131026);
                    ComposerKt.sourceInformationMarkerEnd($composer3);
                    ComposerKt.sourceInformationMarkerEnd($composer3);
                    $composer3.endNode();
                    ComposerKt.sourceInformationMarkerEnd($composer3);
                    ComposerKt.sourceInformationMarkerEnd($composer3);
                    ComposerKt.sourceInformationMarkerEnd($composer3);
                    ComposerKt.sourceInformationMarkerEnd($composer3);
                    ComposerKt.sourceInformationMarkerEnd($composer3);
                    $composer3.endNode();
                    ComposerKt.sourceInformationMarkerEnd($composer3);
                    ComposerKt.sourceInformationMarkerEnd($composer3);
                    ComposerKt.sourceInformationMarkerEnd($composer3);
                    if (ComposerKt.isTraceInProgress()) {
                    }
                }
            }
            obj = (Function2) new SplashScreen.1.1(context, function02, function0, (Continuation) null);
            $composer3.updateRememberedValue(obj);
            ComposerKt.sourceInformationMarkerEnd($composer3);
            EffectsKt.LaunchedEffect(true, (Function2) obj, $composer3, 6);
            ColorKt.Color(ColorKt.luminance-8_81llA(MaterialTheme.INSTANCE.getColorScheme($composer3, MaterialTheme.$stable).getBackground-0d7_KjU()) >= 0.5f ? 4278255615L : 4278216447L);
            ColorKt.Color(4294967295L);
            long bgMain2 = ColorKt.Color(ColorKt.luminance-8_81llA(MaterialTheme.INSTANCE.getColorScheme($composer3, MaterialTheme.$stable).getBackground-0d7_KjU()) >= 0.5f ? 4278848010L : 4294244346L);
            ColorKt.Color(ColorKt.luminance-8_81llA(MaterialTheme.INSTANCE.getColorScheme($composer3, MaterialTheme.$stable).getBackground-0d7_KjU()) >= 0.5f ? 4280222230L : 4294962165L);
            ColorKt.Color(4279374354L);
            Modifier modifier4 = BackgroundKt.background-bw27NRU$default(SizeKt.fillMaxSize$default(Modifier.Companion, 0.0f, 1, (Object) null), bgMain2, (Shape) null, 2, (Object) null);
            Alignment center4 = Alignment.Companion.getCenter();
            ComposerKt.sourceInformationMarkerStart($composer3, 733328855, "CC(Box)P(2,1,3)72@3384L130:Box.kt#2w3rfo");
            MeasurePolicy maybeCachedBoxMeasurePolicy3 = BoxKt.maybeCachedBoxMeasurePolicy(center4, false);
            $composer2 = $composer3;
            ComposerKt.sourceInformationMarkerStart($composer3, -1323940314, "CC(Layout)P(!1,2)79@3208L23,82@3359L411:Layout.kt#80mrfh");
            int currentCompositeKeyHash4 = ComposablesKt.getCurrentCompositeKeyHash($composer3, 0);
            CompositionLocalMap currentCompositionLocalMap4 = $composer3.getCurrentCompositionLocalMap();
            Modifier materializeModifier4 = ComposedModifierKt.materializeModifier($composer3, modifier4);
            Function0 constructor4 = ComposeUiNode.Companion.getConstructor();
            int i22 = ((((48 << 3) & 112) << 6) & 896) | 6;
            ComposerKt.sourceInformationMarkerStart($composer3, -692256719, "CC(ReusableComposeNode)P(1,2)376@14062L9:Composables.kt#9igjgp");
            if (!($composer3.getApplier() instanceof Applier)) {
            }
            $composer3.startReusableNode();
            if (!$composer3.getInserting()) {
            }
            composer = Updater.constructor-impl($composer3);
            Updater.set-impl(composer, maybeCachedBoxMeasurePolicy3, ComposeUiNode.Companion.getSetMeasurePolicy());
            Updater.set-impl(composer, currentCompositionLocalMap4, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
            Function2 setCompositeKeyHash4 = ComposeUiNode.Companion.getSetCompositeKeyHash();
            if (!composer.getInserting()) {
                Updater.set-impl(composer, materializeModifier4, ComposeUiNode.Companion.getSetModifier());
                int i322 = (i22 >> 6) & 14;
                ComposerKt.sourceInformationMarkerStart($composer3, -2146769399, "C73@3429L9:Box.kt#2w3rfo");
                BoxScope boxScope32 = BoxScopeInstance.INSTANCE;
                int i422 = ((48 >> 6) & 112) | 6;
                ComposerKt.sourceInformationMarkerStart($composer3, 1913196068, "C57@2611L1237:SplashScreen.kt#2thlc2");
                Alignment.Horizontal centerHorizontally22 = Alignment.Companion.getCenterHorizontally();
                Arrangement.Vertical center222 = Arrangement.INSTANCE.getCenter();
                ComposerKt.sourceInformationMarkerStart($composer3, -483455358, "CC(Column)P(2,3,1)86@4330L61,87@4396L133:Column.kt#2w3rfo");
                Modifier modifier222 = Modifier.Companion;
                MeasurePolicy columnMeasurePolicy22 = ColumnKt.columnMeasurePolicy(center222, centerHorizontally22, $composer3, ((432 >> 3) & 14) | ((432 >> 3) & 112));
                ComposerKt.sourceInformationMarkerStart($composer3, -1323940314, "CC(Layout)P(!1,2)79@3208L23,82@3359L411:Layout.kt#80mrfh");
                int currentCompositeKeyHash222 = ComposablesKt.getCurrentCompositeKeyHash($composer3, 0);
                CompositionLocalMap currentCompositionLocalMap222 = $composer3.getCurrentCompositionLocalMap();
                Modifier materializeModifier222 = ComposedModifierKt.materializeModifier($composer3, modifier222);
                Function0 constructor222 = ComposeUiNode.Companion.getConstructor();
                int i522 = ((((432 << 3) & 112) << 6) & 896) | 6;
                ComposerKt.sourceInformationMarkerStart($composer3, -692256719, "CC(ReusableComposeNode)P(1,2)376@14062L9:Composables.kt#9igjgp");
                if (!($composer3.getApplier() instanceof Applier)) {
                }
                $composer3.startReusableNode();
                if (!$composer3.getInserting()) {
                }
                composer2 = Updater.constructor-impl($composer3);
                Updater.set-impl(composer2, columnMeasurePolicy22, ComposeUiNode.Companion.getSetMeasurePolicy());
                Updater.set-impl(composer2, currentCompositionLocalMap222, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
                Function2 setCompositeKeyHash222 = ComposeUiNode.Companion.getSetCompositeKeyHash();
                if (!composer2.getInserting()) {
                }
                composer2.updateRememberedValue(Integer.valueOf(currentCompositeKeyHash222));
                composer2.apply(Integer.valueOf(currentCompositeKeyHash222), setCompositeKeyHash222);
                Updater.set-impl(composer2, materializeModifier222, ComposeUiNode.Companion.getSetModifier());
                int i62222 = (i522 >> 6) & 14;
                ComposerKt.sourceInformationMarkerStart($composer3, -384784025, "C88@4444L9:Column.kt#2w3rfo");
                ColumnScope columnScope2222 = ColumnScopeInstance.INSTANCE;
                int i72222 = ((432 >> 6) & 112) | 6;
                ComposerKt.sourceInformationMarkerStart($composer3, -455789651, "C61@2760L379,72@3152L41,77@3433L11,73@3206L376,79@3595L40,80@3648L190:SplashScreen.kt#2thlc2");
                Modifier modifier32222 = SizeKt.size-3ABfNKs(Modifier.Companion, Dp.constructor-impl(120));
                Alignment center32222 = Alignment.Companion.getCenter();
                ComposerKt.sourceInformationMarkerStart($composer3, 733328855, "CC(Box)P(2,1,3)72@3384L130:Box.kt#2w3rfo");
                MeasurePolicy maybeCachedBoxMeasurePolicy22222 = BoxKt.maybeCachedBoxMeasurePolicy(center32222, false);
                ComposerKt.sourceInformationMarkerStart($composer3, -1323940314, "CC(Layout)P(!1,2)79@3208L23,82@3359L411:Layout.kt#80mrfh");
                int currentCompositeKeyHash32222 = ComposablesKt.getCurrentCompositeKeyHash($composer3, 0);
                CompositionLocalMap currentCompositionLocalMap32222 = $composer3.getCurrentCompositionLocalMap();
                Modifier materializeModifier32222 = ComposedModifierKt.materializeModifier($composer3, modifier32222);
                Function0 constructor32222 = ComposeUiNode.Companion.getConstructor();
                int i82222 = ((((54 << 3) & 112) << 6) & 896) | 6;
                ComposerKt.sourceInformationMarkerStart($composer3, -692256719, "CC(ReusableComposeNode)P(1,2)376@14062L9:Composables.kt#9igjgp");
                if (!($composer3.getApplier() instanceof Applier)) {
                }
                $composer3.startReusableNode();
                if (!$composer3.getInserting()) {
                }
                composer3 = Updater.constructor-impl($composer3);
                Updater.set-impl(composer3, maybeCachedBoxMeasurePolicy22222, ComposeUiNode.Companion.getSetMeasurePolicy());
                Updater.set-impl(composer3, currentCompositionLocalMap32222, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
                Function2 setCompositeKeyHash32222 = ComposeUiNode.Companion.getSetCompositeKeyHash();
                if (!composer3.getInserting()) {
                }
                composer3.updateRememberedValue(Integer.valueOf(currentCompositeKeyHash32222));
                composer3.apply(Integer.valueOf(currentCompositeKeyHash32222), setCompositeKeyHash32222);
                Updater.set-impl(composer3, materializeModifier32222, ComposeUiNode.Companion.getSetModifier());
                int i9222222 = (i82222 >> 6) & 14;
                ComposerKt.sourceInformationMarkerStart($composer3, -2146769399, "C73@3429L9:Box.kt#2w3rfo");
                BoxScope boxScope2222222 = BoxScopeInstance.INSTANCE;
                int i10222222 = ((54 >> 6) & 112) | 6;
                ComposerKt.sourceInformationMarkerStart($composer3, 1867339824, "C67@2957L47,66@2920L205:SplashScreen.kt#2thlc2");
                ImageKt.Image(PainterResources_androidKt.painterResource(R.drawable.ic_logo_splash, $composer3, 0), "Logo", SizeKt.size-3ABfNKs(Modifier.Companion, Dp.constructor-impl(100)), (Alignment) null, (ContentScale) null, 0.0f, (ColorFilter) null, $composer3, 432, 120);
                ComposerKt.sourceInformationMarkerEnd($composer3);
                ComposerKt.sourceInformationMarkerEnd($composer3);
                $composer3.endNode();
                ComposerKt.sourceInformationMarkerEnd($composer3);
                ComposerKt.sourceInformationMarkerEnd($composer3);
                ComposerKt.sourceInformationMarkerEnd($composer3);
                SpacerKt.Spacer(SizeKt.height-3ABfNKs(Modifier.Companion, Dp.constructor-impl(24)), $composer3, 6);
                TextKt.Text--4IGK_g(SettingsManager.INSTANCE.getCompanyName(), (Modifier) null, ColorKt.luminance-8_81llA(MaterialTheme.INSTANCE.getColorScheme($composer3, MaterialTheme.$stable).getBackground-0d7_KjU()) >= 0.5f ? Color.Companion.getWhite-0d7_KjU() : ColorKt.Color(4279900698L), TextUnitKt.getSp(i), (FontStyle) null, FontWeight.Companion.getBold(), (FontFamily) null, 0L, (TextDecoration) null, (TextAlign) null, 0L, 0, false, 0, 0, (Function1) null, (TextStyle) null, $composer3, 199680, 0, 131026);
                SpacerKt.Spacer(SizeKt.height-3ABfNKs(Modifier.Companion, Dp.constructor-impl(8)), $composer3, 6);
                TextKt.Text--4IGK_g("Billing System v1.2a", (Modifier) null, ColorKt.Color(4289374890L), TextUnitKt.getSp(16), (FontStyle) null, FontWeight.Companion.getMedium(), (FontFamily) null, 0L, (TextDecoration) null, (TextAlign) null, 0L, 0, false, 0, 0, (Function1) null, (TextStyle) null, $composer3, 200070, 0, 131026);
                ComposerKt.sourceInformationMarkerEnd($composer3);
                ComposerKt.sourceInformationMarkerEnd($composer3);
                $composer3.endNode();
                ComposerKt.sourceInformationMarkerEnd($composer3);
                ComposerKt.sourceInformationMarkerEnd($composer3);
                ComposerKt.sourceInformationMarkerEnd($composer3);
                ComposerKt.sourceInformationMarkerEnd($composer3);
                ComposerKt.sourceInformationMarkerEnd($composer3);
                $composer3.endNode();
                ComposerKt.sourceInformationMarkerEnd($composer3);
                ComposerKt.sourceInformationMarkerEnd($composer3);
                ComposerKt.sourceInformationMarkerEnd($composer3);
                if (ComposerKt.isTraceInProgress()) {
                }
            }
            composer.updateRememberedValue(Integer.valueOf(currentCompositeKeyHash4));
            composer.apply(Integer.valueOf(currentCompositeKeyHash4), setCompositeKeyHash4);
            Updater.set-impl(composer, materializeModifier4, ComposeUiNode.Companion.getSetModifier());
            int i3222 = (i22 >> 6) & 14;
            ComposerKt.sourceInformationMarkerStart($composer3, -2146769399, "C73@3429L9:Box.kt#2w3rfo");
            BoxScope boxScope322 = BoxScopeInstance.INSTANCE;
            int i4222 = ((48 >> 6) & 112) | 6;
            ComposerKt.sourceInformationMarkerStart($composer3, 1913196068, "C57@2611L1237:SplashScreen.kt#2thlc2");
            Alignment.Horizontal centerHorizontally222 = Alignment.Companion.getCenterHorizontally();
            Arrangement.Vertical center2222 = Arrangement.INSTANCE.getCenter();
            ComposerKt.sourceInformationMarkerStart($composer3, -483455358, "CC(Column)P(2,3,1)86@4330L61,87@4396L133:Column.kt#2w3rfo");
            Modifier modifier2222 = Modifier.Companion;
            MeasurePolicy columnMeasurePolicy222 = ColumnKt.columnMeasurePolicy(center2222, centerHorizontally222, $composer3, ((432 >> 3) & 14) | ((432 >> 3) & 112));
            ComposerKt.sourceInformationMarkerStart($composer3, -1323940314, "CC(Layout)P(!1,2)79@3208L23,82@3359L411:Layout.kt#80mrfh");
            int currentCompositeKeyHash2222 = ComposablesKt.getCurrentCompositeKeyHash($composer3, 0);
            CompositionLocalMap currentCompositionLocalMap2222 = $composer3.getCurrentCompositionLocalMap();
            Modifier materializeModifier2222 = ComposedModifierKt.materializeModifier($composer3, modifier2222);
            Function0 constructor2222 = ComposeUiNode.Companion.getConstructor();
            int i5222 = ((((432 << 3) & 112) << 6) & 896) | 6;
            ComposerKt.sourceInformationMarkerStart($composer3, -692256719, "CC(ReusableComposeNode)P(1,2)376@14062L9:Composables.kt#9igjgp");
            if (!($composer3.getApplier() instanceof Applier)) {
            }
            $composer3.startReusableNode();
            if (!$composer3.getInserting()) {
            }
            composer2 = Updater.constructor-impl($composer3);
            Updater.set-impl(composer2, columnMeasurePolicy222, ComposeUiNode.Companion.getSetMeasurePolicy());
            Updater.set-impl(composer2, currentCompositionLocalMap2222, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
            Function2 setCompositeKeyHash2222 = ComposeUiNode.Companion.getSetCompositeKeyHash();
            if (!composer2.getInserting()) {
            }
            composer2.updateRememberedValue(Integer.valueOf(currentCompositeKeyHash2222));
            composer2.apply(Integer.valueOf(currentCompositeKeyHash2222), setCompositeKeyHash2222);
            Updater.set-impl(composer2, materializeModifier2222, ComposeUiNode.Companion.getSetModifier());
            int i622222 = (i5222 >> 6) & 14;
            ComposerKt.sourceInformationMarkerStart($composer3, -384784025, "C88@4444L9:Column.kt#2w3rfo");
            ColumnScope columnScope22222 = ColumnScopeInstance.INSTANCE;
            int i722222 = ((432 >> 6) & 112) | 6;
            ComposerKt.sourceInformationMarkerStart($composer3, -455789651, "C61@2760L379,72@3152L41,77@3433L11,73@3206L376,79@3595L40,80@3648L190:SplashScreen.kt#2thlc2");
            Modifier modifier322222 = SizeKt.size-3ABfNKs(Modifier.Companion, Dp.constructor-impl(120));
            Alignment center322222 = Alignment.Companion.getCenter();
            ComposerKt.sourceInformationMarkerStart($composer3, 733328855, "CC(Box)P(2,1,3)72@3384L130:Box.kt#2w3rfo");
            MeasurePolicy maybeCachedBoxMeasurePolicy222222 = BoxKt.maybeCachedBoxMeasurePolicy(center322222, false);
            ComposerKt.sourceInformationMarkerStart($composer3, -1323940314, "CC(Layout)P(!1,2)79@3208L23,82@3359L411:Layout.kt#80mrfh");
            int currentCompositeKeyHash322222 = ComposablesKt.getCurrentCompositeKeyHash($composer3, 0);
            CompositionLocalMap currentCompositionLocalMap322222 = $composer3.getCurrentCompositionLocalMap();
            Modifier materializeModifier322222 = ComposedModifierKt.materializeModifier($composer3, modifier322222);
            Function0 constructor322222 = ComposeUiNode.Companion.getConstructor();
            int i822222 = ((((54 << 3) & 112) << 6) & 896) | 6;
            ComposerKt.sourceInformationMarkerStart($composer3, -692256719, "CC(ReusableComposeNode)P(1,2)376@14062L9:Composables.kt#9igjgp");
            if (!($composer3.getApplier() instanceof Applier)) {
            }
            $composer3.startReusableNode();
            if (!$composer3.getInserting()) {
            }
            composer3 = Updater.constructor-impl($composer3);
            Updater.set-impl(composer3, maybeCachedBoxMeasurePolicy222222, ComposeUiNode.Companion.getSetMeasurePolicy());
            Updater.set-impl(composer3, currentCompositionLocalMap322222, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
            Function2 setCompositeKeyHash322222 = ComposeUiNode.Companion.getSetCompositeKeyHash();
            if (!composer3.getInserting()) {
            }
            composer3.updateRememberedValue(Integer.valueOf(currentCompositeKeyHash322222));
            composer3.apply(Integer.valueOf(currentCompositeKeyHash322222), setCompositeKeyHash322222);
            Updater.set-impl(composer3, materializeModifier322222, ComposeUiNode.Companion.getSetModifier());
            int i92222222 = (i822222 >> 6) & 14;
            ComposerKt.sourceInformationMarkerStart($composer3, -2146769399, "C73@3429L9:Box.kt#2w3rfo");
            BoxScope boxScope22222222 = BoxScopeInstance.INSTANCE;
            int i102222222 = ((54 >> 6) & 112) | 6;
            ComposerKt.sourceInformationMarkerStart($composer3, 1867339824, "C67@2957L47,66@2920L205:SplashScreen.kt#2thlc2");
            ImageKt.Image(PainterResources_androidKt.painterResource(R.drawable.ic_logo_splash, $composer3, 0), "Logo", SizeKt.size-3ABfNKs(Modifier.Companion, Dp.constructor-impl(100)), (Alignment) null, (ContentScale) null, 0.0f, (ColorFilter) null, $composer3, 432, 120);
            ComposerKt.sourceInformationMarkerEnd($composer3);
            ComposerKt.sourceInformationMarkerEnd($composer3);
            $composer3.endNode();
            ComposerKt.sourceInformationMarkerEnd($composer3);
            ComposerKt.sourceInformationMarkerEnd($composer3);
            ComposerKt.sourceInformationMarkerEnd($composer3);
            SpacerKt.Spacer(SizeKt.height-3ABfNKs(Modifier.Companion, Dp.constructor-impl(24)), $composer3, 6);
            TextKt.Text--4IGK_g(SettingsManager.INSTANCE.getCompanyName(), (Modifier) null, ColorKt.luminance-8_81llA(MaterialTheme.INSTANCE.getColorScheme($composer3, MaterialTheme.$stable).getBackground-0d7_KjU()) >= 0.5f ? Color.Companion.getWhite-0d7_KjU() : ColorKt.Color(4279900698L), TextUnitKt.getSp(i), (FontStyle) null, FontWeight.Companion.getBold(), (FontFamily) null, 0L, (TextDecoration) null, (TextAlign) null, 0L, 0, false, 0, 0, (Function1) null, (TextStyle) null, $composer3, 199680, 0, 131026);
            SpacerKt.Spacer(SizeKt.height-3ABfNKs(Modifier.Companion, Dp.constructor-impl(8)), $composer3, 6);
            TextKt.Text--4IGK_g("Billing System v1.2a", (Modifier) null, ColorKt.Color(4289374890L), TextUnitKt.getSp(16), (FontStyle) null, FontWeight.Companion.getMedium(), (FontFamily) null, 0L, (TextDecoration) null, (TextAlign) null, 0L, 0, false, 0, 0, (Function1) null, (TextStyle) null, $composer3, 200070, 0, 131026);
            ComposerKt.sourceInformationMarkerEnd($composer3);
            ComposerKt.sourceInformationMarkerEnd($composer3);
            $composer3.endNode();
            ComposerKt.sourceInformationMarkerEnd($composer3);
            ComposerKt.sourceInformationMarkerEnd($composer3);
            ComposerKt.sourceInformationMarkerEnd($composer3);
            ComposerKt.sourceInformationMarkerEnd($composer3);
            ComposerKt.sourceInformationMarkerEnd($composer3);
            $composer3.endNode();
            ComposerKt.sourceInformationMarkerEnd($composer3);
            ComposerKt.sourceInformationMarkerEnd($composer3);
            ComposerKt.sourceInformationMarkerEnd($composer3);
            if (ComposerKt.isTraceInProgress()) {
            }
        }
        ScopeUpdateScope endRestartGroup = $composer2.endRestartGroup();
        if (endRestartGroup != null) {
            endRestartGroup.updateScope(new Function2() { // from class: com.example.ui.screens.SplashScreenKt$$ExternalSyntheticLambda0
                public final Object invoke(Object obj2, Object obj3) {
                    return SplashScreenKt.SplashScreen$lambda$4(function0, function02, $changed, (Composer) obj2, ((Integer) obj3).intValue());
                }
            });
        }
    }
}
