package land.chipmunk.chipmunkmod.util.misc;

import com.google.common.base.Suppliers;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class TextUtil {
    public static MutableText fromJson (String json) {
        return Text.Serialization.fromJson(
                json,
                Suppliers.ofInstance(DynamicRegistryManager.of(Registries.REGISTRIES)).get()
        );
    }
}
