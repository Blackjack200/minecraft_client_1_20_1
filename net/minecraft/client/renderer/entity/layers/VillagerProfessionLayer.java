package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.IOException;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.VillagerHeadModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.resources.metadata.animation.VillagerMetaDataSection;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;

public class VillagerProfessionLayer<T extends LivingEntity & VillagerDataHolder, M extends EntityModel<T> & VillagerHeadModel> extends RenderLayer<T, M> {
   private static final Int2ObjectMap<ResourceLocation> LEVEL_LOCATIONS = Util.make(new Int2ObjectOpenHashMap<>(), (int2objectopenhashmap) -> {
      int2objectopenhashmap.put(1, new ResourceLocation("stone"));
      int2objectopenhashmap.put(2, new ResourceLocation("iron"));
      int2objectopenhashmap.put(3, new ResourceLocation("gold"));
      int2objectopenhashmap.put(4, new ResourceLocation("emerald"));
      int2objectopenhashmap.put(5, new ResourceLocation("diamond"));
   });
   private final Object2ObjectMap<VillagerType, VillagerMetaDataSection.Hat> typeHatCache = new Object2ObjectOpenHashMap<>();
   private final Object2ObjectMap<VillagerProfession, VillagerMetaDataSection.Hat> professionHatCache = new Object2ObjectOpenHashMap<>();
   private final ResourceManager resourceManager;
   private final String path;

   public VillagerProfessionLayer(RenderLayerParent<T, M> renderlayerparent, ResourceManager resourcemanager, String s) {
      super(renderlayerparent);
      this.resourceManager = resourcemanager;
      this.path = s;
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, int i, T livingentity, float f, float f1, float f2, float f3, float f4, float f5) {
      if (!livingentity.isInvisible()) {
         VillagerData villagerdata = livingentity.getVillagerData();
         VillagerType villagertype = villagerdata.getType();
         VillagerProfession villagerprofession = villagerdata.getProfession();
         VillagerMetaDataSection.Hat villagermetadatasection_hat = this.getHatData(this.typeHatCache, "type", BuiltInRegistries.VILLAGER_TYPE, villagertype);
         VillagerMetaDataSection.Hat villagermetadatasection_hat1 = this.getHatData(this.professionHatCache, "profession", BuiltInRegistries.VILLAGER_PROFESSION, villagerprofession);
         M entitymodel = this.getParentModel();
         entitymodel.hatVisible(villagermetadatasection_hat1 == VillagerMetaDataSection.Hat.NONE || villagermetadatasection_hat1 == VillagerMetaDataSection.Hat.PARTIAL && villagermetadatasection_hat != VillagerMetaDataSection.Hat.FULL);
         ResourceLocation resourcelocation = this.getResourceLocation("type", BuiltInRegistries.VILLAGER_TYPE.getKey(villagertype));
         renderColoredCutoutModel(entitymodel, resourcelocation, posestack, multibuffersource, i, livingentity, 1.0F, 1.0F, 1.0F);
         entitymodel.hatVisible(true);
         if (villagerprofession != VillagerProfession.NONE && !livingentity.isBaby()) {
            ResourceLocation resourcelocation1 = this.getResourceLocation("profession", BuiltInRegistries.VILLAGER_PROFESSION.getKey(villagerprofession));
            renderColoredCutoutModel(entitymodel, resourcelocation1, posestack, multibuffersource, i, livingentity, 1.0F, 1.0F, 1.0F);
            if (villagerprofession != VillagerProfession.NITWIT) {
               ResourceLocation resourcelocation2 = this.getResourceLocation("profession_level", LEVEL_LOCATIONS.get(Mth.clamp(villagerdata.getLevel(), 1, LEVEL_LOCATIONS.size())));
               renderColoredCutoutModel(entitymodel, resourcelocation2, posestack, multibuffersource, i, livingentity, 1.0F, 1.0F, 1.0F);
            }
         }

      }
   }

   private ResourceLocation getResourceLocation(String s, ResourceLocation resourcelocation) {
      return resourcelocation.withPath((s2) -> "textures/entity/" + this.path + "/" + s + "/" + s2 + ".png");
   }

   public <K> VillagerMetaDataSection.Hat getHatData(Object2ObjectMap<K, VillagerMetaDataSection.Hat> object2objectmap, String s, DefaultedRegistry<K> defaultedregistry, K object) {
      return object2objectmap.computeIfAbsent(object, (object2) -> this.resourceManager.getResource(this.getResourceLocation(s, defaultedregistry.getKey(object))).flatMap((resource) -> {
            try {
               return resource.metadata().getSection(VillagerMetaDataSection.SERIALIZER).map(VillagerMetaDataSection::getHat);
            } catch (IOException var2) {
               return Optional.empty();
            }
         }).orElse(VillagerMetaDataSection.Hat.NONE));
   }
}
