package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class BlockEntityCustomNameToComponentFix extends DataFix {
   public BlockEntityCustomNameToComponentFix(Schema schema, boolean flag) {
      super(schema, flag);
   }

   public TypeRewriteRule makeRule() {
      OpticFinder<String> opticfinder = DSL.fieldFinder("id", NamespacedSchema.namespacedString());
      return this.fixTypeEverywhereTyped("BlockEntityCustomNameToComponentFix", this.getInputSchema().getType(References.BLOCK_ENTITY), (typed) -> typed.update(DSL.remainderFinder(), (dynamic) -> {
            Optional<String> optional = typed.getOptional(opticfinder);
            return optional.isPresent() && Objects.equals(optional.get(), "minecraft:command_block") ? dynamic : EntityCustomNameToComponentFix.fixTagCustomName(dynamic);
         }));
   }
}
