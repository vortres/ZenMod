package land.chipmunk.chipmunkmod.mixin;

import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextFieldWidget.class)
public class TextFieldWidgetMixin {
    @Shadow private int maxLength;

    @Inject(method = "setMaxLength", at = @At("HEAD"), cancellable = true)
    private void setMaxLength (int length, CallbackInfo ci) {
        this.maxLength = Integer.MAX_VALUE;

        ci.cancel();
    }
}
