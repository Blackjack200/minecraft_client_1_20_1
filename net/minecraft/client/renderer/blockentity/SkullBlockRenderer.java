package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PiglinHeadModel;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.dragon.DragonHeadModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;

public class SkullBlockRenderer implements BlockEntityRenderer<SkullBlockEntity> {
   private final Map<SkullBlock.Type, SkullModelBase> modelByType;
   private static final Map<SkullBlock.Type, ResourceLocation> SKIN_BY_TYPE = Util.make(Maps.newHashMap(), (hashmap) -> {
      hashmap.put(SkullBlock.Types.SKELETON, new ResourceLocation("textures/entity/skeleton/skeleton.png"));
      hashmap.put(SkullBlock.Types.WITHER_SKELETON, new ResourceLocation("textures/entity/skeleton/wither_skeleton.png"));
      hashmap.put(SkullBlock.Types.ZOMBIE, new ResourceLocation("textures/entity/zombie/zombie.png"));
      hashmap.put(SkullBlock.Types.CREEPER, new ResourceLocation("textures/entity/creeper/creeper.png"));
      hashmap.put(SkullBlock.Types.DRAGON, new ResourceLocation("textures/entity/enderdragon/dragon.png"));
      hashmap.put(SkullBlock.Types.PIGLIN, new ResourceLocation("textures/entity/piglin/piglin.png"));
      hashmap.put(SkullBlock.Types.PLAYER, DefaultPlayerSkin.getDefaultSkin());
   });

   public static Map<SkullBlock.Type, SkullModelBase> createSkullRenderers(EntityModelSet entitymodelset) {
      ImmutableMap.Builder<SkullBlock.Type, SkullModelBase> immutablemap_builder = ImmutableMap.builder();
      immutablemap_builder.put(SkullBlock.Types.SKELETON, new SkullModel(entitymodelset.bakeLayer(ModelLayers.SKELETON_SKULL)));
      immutablemap_builder.put(SkullBlock.Types.WITHER_SKELETON, new SkullModel(entitymodelset.bakeLayer(ModelLayers.WITHER_SKELETON_SKULL)));
      immutablemap_builder.put(SkullBlock.Types.PLAYER, new SkullModel(entitymodelset.bakeLayer(ModelLayers.PLAYER_HEAD)));
      immutablemap_builder.put(SkullBlock.Types.ZOMBIE, new SkullModel(entitymodelset.bakeLayer(ModelLayers.ZOMBIE_HEAD)));
      immutablemap_builder.put(SkullBlock.Types.CREEPER, new SkullModel(entitymodelset.bakeLayer(ModelLayers.CREEPER_HEAD)));
      immutablemap_builder.put(SkullBlock.Types.DRAGON, new DragonHeadModel(entitymodelset.bakeLayer(ModelLayers.DRAGON_SKULL)));
      immutablemap_builder.put(SkullBlock.Types.PIGLIN, new PiglinHeadModel(entitymodelset.bakeLayer(ModelLayers.PIGLIN_HEAD)));
      return immutablemap_builder.build();
   }

   public SkullBlockRenderer(BlockEntityRendererProvider.Context blockentityrendererprovider_context) {
      this.modelByType = createSkullRenderers(blockentityrendererprovider_context.getModelSet());
   }

   public void render(SkullBlockEntity skullblockentity, float f, PoseStack posestack, MultiBufferSource multibuffersource, int i, int j) {
      float f1 = skullblockentity.getAnimation(f);
      BlockState blockstate = skullblockentity.getBlockState();
      boolean flag = blockstate.getBlock() instanceof WallSkullBlock;
      Direction direction = flag ? blockstate.getValue(WallSkullBlock.FACING) : null;
      int k = flag ? RotationSegment.convertToSegment(direction.getOpposite()) : blockstate.getValue(SkullBlock.ROTATION);
      float f2 = RotationSegment.convertToDegrees(k);
      SkullBlock.Type skullblock_type = ((AbstractSkullBlock)blockstate.getBlock()).getType();
      SkullModelBase skullmodelbase = this.modelByType.get(skullblock_type);
      RenderType rendertype = getRenderType(skullblock_type, skullblockentity.getOwnerProfile());
      renderSkull(direction, f2, f1, posestack, multibuffersource, i, skullmodelbase, rendertype);
   }

   public static void renderSkull(@Nullable Direction direction, float f, float f1, PoseStack posestack, MultiBufferSource multibuffersource, int i, SkullModelBase skullmodelbase, RenderType rendertype) {
      posestack.pushPose();
      if (direction == null) {
         posestack.translate(0.5F, 0.0F, 0.5F);
      } else {
         float f2 = 0.25F;
         posestack.translate(0.5F - (float)direction.getStepX() * 0.25F, 0.25F, 0.5F - (float)direction.getStepZ() * 0.25F);
      }

      posestack.scale(-1.0F, -1.0F, 1.0F);
      VertexConsumer vertexconsumer = multibuffersource.getBuffer(rendertype);
      skullmodelbase.setupAnim(f1, f, 0.0F);
      skullmodelbase.renderToBuffer(posestack, vertexconsumer, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      posestack.popPose();
   }

   public static RenderType getRenderType(SkullBlock.Type skullblock_type, @Nullable GameProfile gameprofile) {
      ResourceLocation resourcelocation = SKIN_BY_TYPE.get(skullblock_type);
      if (skullblock_type == SkullBlock.Types.PLAYER && gameprofile != null) {
         Minecraft minecraft = Minecraft.getInstance();
         Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = minecraft.getSkinManager().getInsecureSkinInformation(gameprofile);
         return map.containsKey(Type.SKIN) ? RenderType.entityTranslucent(minecraft.getSkinManager().registerTexture(map.get(Type.SKIN), Type.SKIN)) : RenderType.entityCutoutNoCull(DefaultPlayerSkin.getDefaultSkin(UUIDUtil.getOrCreatePlayerUUID(gameprofile)));
      } else {
         return RenderType.entityCutoutNoCullZOffset(resourcelocation);
      }
   }
}
