package net.minecraft.client.resources.metadata.animation;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class VillagerMetaDataSection {
   public static final VillagerMetadataSectionSerializer SERIALIZER = new VillagerMetadataSectionSerializer();
   public static final String SECTION_NAME = "villager";
   private final VillagerMetaDataSection.Hat hat;

   public VillagerMetaDataSection(VillagerMetaDataSection.Hat villagermetadatasection_hat) {
      this.hat = villagermetadatasection_hat;
   }

   public VillagerMetaDataSection.Hat getHat() {
      return this.hat;
   }

   public static enum Hat {
      NONE("none"),
      PARTIAL("partial"),
      FULL("full");

      private static final Map<String, VillagerMetaDataSection.Hat> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(VillagerMetaDataSection.Hat::getName, (villagermetadatasection_hat) -> villagermetadatasection_hat));
      private final String name;

      private Hat(String s) {
         this.name = s;
      }

      public String getName() {
         return this.name;
      }

      public static VillagerMetaDataSection.Hat getByName(String s) {
         return BY_NAME.getOrDefault(s, NONE);
      }
   }
}
