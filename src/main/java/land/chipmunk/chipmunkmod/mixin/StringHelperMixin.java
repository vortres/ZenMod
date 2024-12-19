package land.chipmunk.chipmunkmod.mixin;

import net.minecraft.util.StringHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StringHelper.class)
public class StringHelperMixin {
    @Inject(method = "truncateChat", at = @At("HEAD"), cancellable = true)
    private static void truncateChat (String text, CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(text);
        cir.cancel();
    }

    @Inject(method = "stripTextFormat", at = @At("HEAD"), cancellable = true)
    private static void stripTextFormat(String text, CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(text);
        cir.cancel();
    }

    @Inject(method = "isValidChar", at = @At("HEAD"), cancellable = true)
    private static void isValidChar (char chr, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(chr >= ' ' && chr != '\u007f');
        cir.cancel();
    }
}
