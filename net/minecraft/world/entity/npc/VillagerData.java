package net.minecraft.world.entity.npc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;

public class VillagerData {
   public static final int MIN_VILLAGER_LEVEL = 1;
   public static final int MAX_VILLAGER_LEVEL = 5;
   private static final int[] NEXT_LEVEL_XP_THRESHOLDS = new int[]{0, 10, 70, 150, 250};
   public static final Codec<VillagerData> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(BuiltInRegistries.VILLAGER_TYPE.byNameCodec().fieldOf("type").orElseGet(() -> VillagerType.PLAINS).forGetter((villagerdata2) -> villagerdata2.type), BuiltInRegistries.VILLAGER_PROFESSION.byNameCodec().fieldOf("profession").orElseGet(() -> VillagerProfession.NONE).forGetter((villagerdata1) -> villagerdata1.profession), Codec.INT.fieldOf("level").orElse(1).forGetter((villagerdata) -> villagerdata.level)).apply(recordcodecbuilder_instance, VillagerData::new));
   private final VillagerType type;
   private final VillagerProfession profession;
   private final int level;

   public VillagerData(VillagerType villagertype, VillagerProfession villagerprofession, int i) {
      this.type = villagertype;
      this.profession = villagerprofession;
      this.level = Math.max(1, i);
   }

   public VillagerType getType() {
      return this.type;
   }

   public VillagerProfession getProfession() {
      return this.profession;
   }

   public int getLevel() {
      return this.level;
   }

   public VillagerData setType(VillagerType villagertype) {
      return new VillagerData(villagertype, this.profession, this.level);
   }

   public VillagerData setProfession(VillagerProfession villagerprofession) {
      return new VillagerData(this.type, villagerprofession, this.level);
   }

   public VillagerData setLevel(int i) {
      return new VillagerData(this.type, this.profession, i);
   }

   public static int getMinXpPerLevel(int i) {
      return canLevelUp(i) ? NEXT_LEVEL_XP_THRESHOLDS[i - 1] : 0;
   }

   public static int getMaxXpPerLevel(int i) {
      return canLevelUp(i) ? NEXT_LEVEL_XP_THRESHOLDS[i] : 0;
   }

   public static boolean canLevelUp(int i) {
      return i >= 1 && i < 5;
   }
}
