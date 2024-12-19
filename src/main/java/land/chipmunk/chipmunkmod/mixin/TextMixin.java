package land.chipmunk.chipmunkmod.mixin;

import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.Optional;

@Mixin(Text.class)
public interface TextMixin {
    @Inject(method = "visit(Lnet/minecraft/text/StringVisitable$StyledVisitor;Lnet/minecraft/text/Style;)Ljava/util/Optional;", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Text;visit(Lnet/minecraft/text/StringVisitable$StyledVisitor;Lnet/minecraft/text/Style;)Ljava/util/Optional;"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private <T> void visit (StringVisitable.StyledVisitor<T> styledVisitor, Style style, CallbackInfoReturnable<Optional<T>> cir, Style style2, Optional optional, Iterator var5, Text text) {
        if (text == null) cir.setReturnValue(Optional.empty());
    }

    @Inject(method = "visit(Lnet/minecraft/text/StringVisitable$Visitor;)Ljava/util/Optional;", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Text;visit(Lnet/minecraft/text/StringVisitable$Visitor;)Ljava/util/Optional;"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private <T> void visit (StringVisitable.Visitor<T> visitor, CallbackInfoReturnable<Optional<T>> cir, Optional optional, Iterator var3, Text text) {
        if (text == null) cir.setReturnValue(Optional.empty());
    }
}
