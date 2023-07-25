package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List;
import com.mojang.datafixers.types.templates.TaggedChoice;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import org.slf4j.Logger;

public class TrappedChestBlockEntityFix extends DataFix {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int SIZE = 4096;
   private static final short SIZE_BITS = 12;

   public TrappedChestBlockEntityFix(Schema schema, boolean flag) {
      super(schema, flag);
   }

   public TypeRewriteRule makeRule() {
      Type<?> type = this.getOutputSchema().getType(References.CHUNK);
      Type<?> type1 = type.findFieldType("Level");
      Type<?> type2 = type1.findFieldType("TileEntities");
      if (!(type2 instanceof List.ListType<?> list_listtype)) {
         throw new IllegalStateException("Tile entity type is not a list type.");
      } else {
         OpticFinder<? extends java.util.List<?>> opticfinder = DSL.fieldFinder("TileEntities", list_listtype);
         Type<?> type3 = this.getInputSchema().getType(References.CHUNK);
         OpticFinder<?> opticfinder1 = type3.findField("Level");
         OpticFinder<?> opticfinder2 = opticfinder1.type().findField("Sections");
         Type<?> type4 = opticfinder2.type();
         if (!(type4 instanceof List.ListType)) {
            throw new IllegalStateException("Expecting sections to be a list.");
         } else {
            Type<?> type5 = ((List.ListType)type4).getElement();
            OpticFinder<?> opticfinder3 = DSL.typeFinder(type5);
            return TypeRewriteRule.seq((new AddNewChoices(this.getOutputSchema(), "AddTrappedChestFix", References.BLOCK_ENTITY)).makeRule(), this.fixTypeEverywhereTyped("Trapped Chest fix", type3, (typed) -> typed.updateTyped(opticfinder1, (typed1) -> {
                  Optional<? extends Typed<?>> optional = typed1.getOptionalTyped(opticfinder2);
                  if (!optional.isPresent()) {
                     return typed1;
                  } else {
                     java.util.List<? extends Typed<?>> list = optional.get().getAllTyped(opticfinder3);
                     IntSet intset = new IntOpenHashSet();

                     for(Typed<?> typed2 : list) {
                        TrappedChestBlockEntityFix.TrappedChestSection trappedchestblockentityfix_trappedchestsection = new TrappedChestBlockEntityFix.TrappedChestSection(typed2, this.getInputSchema());
                        if (!trappedchestblockentityfix_trappedchestsection.isSkippable()) {
                           for(int i = 0; i < 4096; ++i) {
                              int j = trappedchestblockentityfix_trappedchestsection.getBlock(i);
                              if (trappedchestblockentityfix_trappedchestsection.isTrappedChest(j)) {
                                 intset.add(trappedchestblockentityfix_trappedchestsection.getIndex() << 12 | i);
                              }
                           }
                        }
                     }

                     Dynamic<?> dynamic = typed1.get(DSL.remainderFinder());
                     int k = dynamic.get("xPos").asInt(0);
                     int l = dynamic.get("zPos").asInt(0);
                     TaggedChoice.TaggedChoiceType<String> taggedchoice_taggedchoicetype = this.getInputSchema().findChoiceType(References.BLOCK_ENTITY);
                     return typed1.updateTyped(opticfinder, (typed3) -> typed3.updateTyped(taggedchoice_taggedchoicetype.finder(), (typed4) -> {
                           Dynamic<?> dynamic1 = typed4.getOrCreate(DSL.remainderFinder());
                           int i2 = dynamic1.get("x").asInt(0) - (k << 4);
                           int j2 = dynamic1.get("y").asInt(0);
                           int k2 = dynamic1.get("z").asInt(0) - (l << 4);
                           return intset.contains(LeavesFix.getIndex(i2, j2, k2)) ? typed4.update(taggedchoice_taggedchoicetype.finder(), (pair) -> pair.mapFirst((s) -> {
                                 if (!Objects.equals(s, "minecraft:chest")) {
                                    LOGGER.warn("Block Entity was expected to be a chest");
                                 }

                                 return "minecraft:trapped_chest";
                              })) : typed4;
                        }));
                  }
               })));
         }
      }
   }

   public static final class TrappedChestSection extends LeavesFix.Section {
      @Nullable
      private IntSet chestIds;

      public TrappedChestSection(Typed<?> typed, Schema schema) {
         super(typed, schema);
      }

      protected boolean skippable() {
         this.chestIds = new IntOpenHashSet();

         for(int i = 0; i < this.palette.size(); ++i) {
            Dynamic<?> dynamic = this.palette.get(i);
            String s = dynamic.get("Name").asString("");
            if (Objects.equals(s, "minecraft:trapped_chest")) {
               this.chestIds.add(i);
            }
         }

         return this.chestIds.isEmpty();
      }

      public boolean isTrappedChest(int i) {
         return this.chestIds.contains(i);
      }
   }
}
