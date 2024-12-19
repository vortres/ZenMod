package land.chipmunk.chipmunkmod.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Type;

@Mixin(Text.Serializer.class)
public class TextSerializerMixin {
    @Unique private static final int LIMIT = 128;
    @Unique private int i;

    @Unique
    private boolean checkDepth (JsonElement element) {
        if (element.isJsonPrimitive()) return false;
        else if (i >= LIMIT) return true;

        if (element.isJsonArray()) {
            i++;

            for (JsonElement item : element.getAsJsonArray()) if (checkDepth(item)) return true;
        } else if (element.isJsonObject()) {
            final JsonObject object = element.getAsJsonObject();

            JsonArray array;

            if (object.has("extra")) array = object.get("extra").getAsJsonArray();
            else if (object.has("with")) array = object.get("with").getAsJsonArray();
            else return false;

            i++;

            for (JsonElement member : array) if (checkDepth(member)) return true;
        }

        return false;
    }

    @Inject(method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/text/MutableText;", at = @At("HEAD"), cancellable = true)
    private void deserialize (JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext, CallbackInfoReturnable<MutableText> cir) {
        i = 0; // better way to do this?

        final boolean overLimit = checkDepth(jsonElement);
        if (!overLimit) return;

        cir.setReturnValue(Text.empty()); // just ignores it
        cir.cancel();
    }
}
