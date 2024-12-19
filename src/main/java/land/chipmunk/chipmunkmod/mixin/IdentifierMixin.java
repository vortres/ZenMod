package land.chipmunk.chipmunkmod.mixin;

import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Identifier.class)
public class IdentifierMixin {
    @Inject(method = "isNamespaceCharacterValid", at = @At("HEAD"), cancellable = true)
    private static void isNamespaceCharacterValid (char character, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);

        cir.cancel();
    }

    @Inject(method = "isNamespaceValid", at = @At("HEAD"), cancellable = true)
    private static void isNamespaceValid (String namespace, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);

        cir.cancel();
    }

    @Inject(method = "validateNamespace", at = @At("HEAD"), cancellable = true)
    private static void validateNamespace(String namespace, String path, CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(namespace);

        cir.cancel();
    }
}
