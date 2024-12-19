package land.chipmunk.chipmunkmod.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.tree.CommandNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CommandDispatcher.class)
public class CommandDispatcherMixin<S> {
    @Inject(method = "parseNodes", at = @At("HEAD"), cancellable = true, /* important --> */ remap = false)
    private void parseNodes (CommandNode<S> node, StringReader originalReader, CommandContextBuilder<S> contextSoFar, CallbackInfoReturnable<ParseResults<S>> cir) {
        // correct way to patch this?
//        if (node.getRelevantNodes(originalReader).size() > 127) {
//            cir.setReturnValue(new ParseResults<>(contextSoFar));
//
//            cir.cancel();
//        }
    }
}
