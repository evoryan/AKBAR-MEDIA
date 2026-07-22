package androidx.compose.ui.platform;

import androidx.compose.ui.node.ModifierNodeElement;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: TestTag.kt */
@Metadata(d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\r\u0012\u0006\u0010\u0003\u001a\u00020\u0004¢\u0006\u0002\u0010\u0005J\b\u0010\u0006\u001a\u00020\u0002H\u0016J\u0013\u0010\u0007\u001a\u00020\b2\b\u0010\t\u001a\u0004\u0018\u00010\nH\u0096\u0002J\b\u0010\u000b\u001a\u00020\fH\u0016J\u0010\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u0002H\u0016J\f\u0010\u0010\u001a\u00020\u000e*\u00020\u0011H\u0016R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\u0012"}, d2 = {"Landroidx/compose/ui/platform/TestTagElement;", "Landroidx/compose/ui/node/ModifierNodeElement;", "Landroidx/compose/ui/platform/TestTagNode;", "tag", "", "(Ljava/lang/String;)V", "create", "equals", "", "other", "", "hashCode", "", "update", "", "node", "inspectableProperties", "Landroidx/compose/ui/platform/InspectorInfo;", "ui_release"}, k = 1, mv = {1, 8, 0}, xi = 48)
/* loaded from: classes10.dex */
final class TestTagElement extends ModifierNodeElement<TestTagNode> {
    private final String tag;

    public TestTagElement(String tag) {
        this.tag = tag;
    }

    @Override // androidx.compose.ui.node.ModifierNodeElement
    public TestTagNode create() {
        return new TestTagNode(this.tag);
    }

    @Override // androidx.compose.ui.node.ModifierNodeElement
    public void update(TestTagNode node) {
        node.setTag(this.tag);
    }

    @Override // androidx.compose.ui.node.ModifierNodeElement
    public void inspectableProperties(InspectorInfo $this$inspectableProperties) {
        $this$inspectableProperties.setName("testTag");
        $this$inspectableProperties.getProperties().set("tag", this.tag);
    }

    @Override // androidx.compose.ui.node.ModifierNodeElement
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof TestTagElement) {
            return Intrinsics.areEqual(this.tag, ((TestTagElement) other).tag);
        }
        return false;
    }

    @Override // androidx.compose.ui.node.ModifierNodeElement
    public int hashCode() {
        return this.tag.hashCode();
    }
}
