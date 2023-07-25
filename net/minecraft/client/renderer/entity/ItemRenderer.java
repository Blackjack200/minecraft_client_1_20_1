package net.minecraft.client.renderer.entity;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.math.MatrixUtil;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ItemRenderer implements ResourceManagerReloadListener {
   public static final ResourceLocation ENCHANTED_GLINT_ENTITY = new ResourceLocation("textures/misc/enchanted_glint_entity.png");
   public static final ResourceLocation ENCHANTED_GLINT_ITEM = new ResourceLocation("textures/misc/enchanted_glint_item.png");
   private static final Set<Item> IGNORED = Sets.newHashSet(Items.AIR);
   public static final int GUI_SLOT_CENTER_X = 8;
   public static final int GUI_SLOT_CENTER_Y = 8;
   public static final int ITEM_COUNT_BLIT_OFFSET = 200;
   public static final float COMPASS_FOIL_UI_SCALE = 0.5F;
   public static final float COMPASS_FOIL_FIRST_PERSON_SCALE = 0.75F;
   public static final float COMPASS_FOIL_TEXTURE_SCALE = 0.0078125F;
   private static final ModelResourceLocation TRIDENT_MODEL = ModelResourceLocation.vanilla("trident", "inventory");
   public static final ModelResourceLocation TRIDENT_IN_HAND_MODEL = ModelResourceLocation.vanilla("trident_in_hand", "inventory");
   private static final ModelResourceLocation SPYGLASS_MODEL = ModelResourceLocation.vanilla("spyglass", "inventory");
   public static final ModelResourceLocation SPYGLASS_IN_HAND_MODEL = ModelResourceLocation.vanilla("spyglass_in_hand", "inventory");
   private final Minecraft minecraft;
   private final ItemModelShaper itemModelShaper;
   private final TextureManager textureManager;
   private final ItemColors itemColors;
   private final BlockEntityWithoutLevelRenderer blockEntityRenderer;

   public ItemRenderer(Minecraft minecraft, TextureManager texturemanager, ModelManager modelmanager, ItemColors itemcolors, BlockEntityWithoutLevelRenderer blockentitywithoutlevelrenderer) {
      this.minecraft = minecraft;
      this.textureManager = texturemanager;
      this.itemModelShaper = new ItemModelShaper(modelmanager);
      this.blockEntityRenderer = blockentitywithoutlevelrenderer;

      for(Item item : BuiltInRegistries.ITEM) {
         if (!IGNORED.contains(item)) {
            this.itemModelShaper.register(item, new ModelResourceLocation(BuiltInRegistries.ITEM.getKey(item), "inventory"));
         }
      }

      this.itemColors = itemcolors;
   }

   public ItemModelShaper getItemModelShaper() {
      return this.itemModelShaper;
   }

   private void renderModelLists(BakedModel bakedmodel, ItemStack itemstack, int i, int j, PoseStack posestack, VertexConsumer vertexconsumer) {
      RandomSource randomsource = RandomSource.create();
      long k = 42L;

      for(Direction direction : Direction.values()) {
         randomsource.setSeed(42L);
         this.renderQuadList(posestack, vertexconsumer, bakedmodel.getQuads((BlockState)null, direction, randomsource), itemstack, i, j);
      }

      randomsource.setSeed(42L);
      this.renderQuadList(posestack, vertexconsumer, bakedmodel.getQuads((BlockState)null, (Direction)null, randomsource), itemstack, i, j);
   }

   public void render(ItemStack itemstack, ItemDisplayContext itemdisplaycontext, boolean flag, PoseStack posestack, MultiBufferSource multibuffersource, int i, int j, BakedModel bakedmodel) {
      if (!itemstack.isEmpty()) {
         posestack.pushPose();
         boolean flag1 = itemdisplaycontext == ItemDisplayContext.GUI || itemdisplaycontext == ItemDisplayContext.GROUND || itemdisplaycontext == ItemDisplayContext.FIXED;
         if (flag1) {
            if (itemstack.is(Items.TRIDENT)) {
               bakedmodel = this.itemModelShaper.getModelManager().getModel(TRIDENT_MODEL);
            } else if (itemstack.is(Items.SPYGLASS)) {
               bakedmodel = this.itemModelShaper.getModelManager().getModel(SPYGLASS_MODEL);
            }
         }

         bakedmodel.getTransforms().getTransform(itemdisplaycontext).apply(flag, posestack);
         posestack.translate(-0.5F, -0.5F, -0.5F);
         if (!bakedmodel.isCustomRenderer() && (!itemstack.is(Items.TRIDENT) || flag1)) {
            boolean flag2;
            if (itemdisplaycontext != ItemDisplayContext.GUI && !itemdisplaycontext.firstPerson() && itemstack.getItem() instanceof BlockItem) {
               Block block = ((BlockItem)itemstack.getItem()).getBlock();
               flag2 = !(block instanceof HalfTransparentBlock) && !(block instanceof StainedGlassPaneBlock);
            } else {
               flag2 = true;
            }

            RenderType rendertype = ItemBlockRenderTypes.getRenderType(itemstack, flag2);
            VertexConsumer vertexconsumer;
            if (hasAnimatedTexture(itemstack) && itemstack.hasFoil()) {
               posestack.pushPose();
               PoseStack.Pose posestack_pose = posestack.last();
               if (itemdisplaycontext == ItemDisplayContext.GUI) {
                  MatrixUtil.mulComponentWise(posestack_pose.pose(), 0.5F);
               } else if (itemdisplaycontext.firstPerson()) {
                  MatrixUtil.mulComponentWise(posestack_pose.pose(), 0.75F);
               }

               if (flag2) {
                  vertexconsumer = getCompassFoilBufferDirect(multibuffersource, rendertype, posestack_pose);
               } else {
                  vertexconsumer = getCompassFoilBuffer(multibuffersource, rendertype, posestack_pose);
               }

               posestack.popPose();
            } else if (flag2) {
               vertexconsumer = getFoilBufferDirect(multibuffersource, rendertype, true, itemstack.hasFoil());
            } else {
               vertexconsumer = getFoilBuffer(multibuffersource, rendertype, true, itemstack.hasFoil());
            }

            this.renderModelLists(bakedmodel, itemstack, i, j, posestack, vertexconsumer);
         } else {
            this.blockEntityRenderer.renderByItem(itemstack, itemdisplaycontext, posestack, multibuffersource, i, j);
         }

         posestack.popPose();
      }
   }

   private static boolean hasAnimatedTexture(ItemStack itemstack) {
      return itemstack.is(ItemTags.COMPASSES) || itemstack.is(Items.CLOCK);
   }

   public static VertexConsumer getArmorFoilBuffer(MultiBufferSource multibuffersource, RenderType rendertype, boolean flag, boolean flag1) {
      return flag1 ? VertexMultiConsumer.create(multibuffersource.getBuffer(flag ? RenderType.armorGlint() : RenderType.armorEntityGlint()), multibuffersource.getBuffer(rendertype)) : multibuffersource.getBuffer(rendertype);
   }

   public static VertexConsumer getCompassFoilBuffer(MultiBufferSource multibuffersource, RenderType rendertype, PoseStack.Pose posestack_pose) {
      return VertexMultiConsumer.create(new SheetedDecalTextureGenerator(multibuffersource.getBuffer(RenderType.glint()), posestack_pose.pose(), posestack_pose.normal(), 0.0078125F), multibuffersource.getBuffer(rendertype));
   }

   public static VertexConsumer getCompassFoilBufferDirect(MultiBufferSource multibuffersource, RenderType rendertype, PoseStack.Pose posestack_pose) {
      return VertexMultiConsumer.create(new SheetedDecalTextureGenerator(multibuffersource.getBuffer(RenderType.glintDirect()), posestack_pose.pose(), posestack_pose.normal(), 0.0078125F), multibuffersource.getBuffer(rendertype));
   }

   public static VertexConsumer getFoilBuffer(MultiBufferSource multibuffersource, RenderType rendertype, boolean flag, boolean flag1) {
      if (flag1) {
         return Minecraft.useShaderTransparency() && rendertype == Sheets.translucentItemSheet() ? VertexMultiConsumer.create(multibuffersource.getBuffer(RenderType.glintTranslucent()), multibuffersource.getBuffer(rendertype)) : VertexMultiConsumer.create(multibuffersource.getBuffer(flag ? RenderType.glint() : RenderType.entityGlint()), multibuffersource.getBuffer(rendertype));
      } else {
         return multibuffersource.getBuffer(rendertype);
      }
   }

   public static VertexConsumer getFoilBufferDirect(MultiBufferSource multibuffersource, RenderType rendertype, boolean flag, boolean flag1) {
      return flag1 ? VertexMultiConsumer.create(multibuffersource.getBuffer(flag ? RenderType.glintDirect() : RenderType.entityGlintDirect()), multibuffersource.getBuffer(rendertype)) : multibuffersource.getBuffer(rendertype);
   }

   private void renderQuadList(PoseStack posestack, VertexConsumer vertexconsumer, List<BakedQuad> list, ItemStack itemstack, int i, int j) {
      boolean flag = !itemstack.isEmpty();
      PoseStack.Pose posestack_pose = posestack.last();

      for(BakedQuad bakedquad : list) {
         int k = -1;
         if (flag && bakedquad.isTinted()) {
            k = this.itemColors.getColor(itemstack, bakedquad.getTintIndex());
         }

         float f = (float)(k >> 16 & 255) / 255.0F;
         float f1 = (float)(k >> 8 & 255) / 255.0F;
         float f2 = (float)(k & 255) / 255.0F;
         vertexconsumer.putBulkData(posestack_pose, bakedquad, f, f1, f2, i, j);
      }

   }

   public BakedModel getModel(ItemStack itemstack, @Nullable Level level, @Nullable LivingEntity livingentity, int i) {
      BakedModel bakedmodel;
      if (itemstack.is(Items.TRIDENT)) {
         bakedmodel = this.itemModelShaper.getModelManager().getModel(TRIDENT_IN_HAND_MODEL);
      } else if (itemstack.is(Items.SPYGLASS)) {
         bakedmodel = this.itemModelShaper.getModelManager().getModel(SPYGLASS_IN_HAND_MODEL);
      } else {
         bakedmodel = this.itemModelShaper.getItemModel(itemstack);
      }

      ClientLevel clientlevel = level instanceof ClientLevel ? (ClientLevel)level : null;
      BakedModel bakedmodel3 = bakedmodel.getOverrides().resolve(bakedmodel, itemstack, clientlevel, livingentity, i);
      return bakedmodel3 == null ? this.itemModelShaper.getModelManager().getMissingModel() : bakedmodel3;
   }

   public void renderStatic(ItemStack itemstack, ItemDisplayContext itemdisplaycontext, int i, int j, PoseStack posestack, MultiBufferSource multibuffersource, @Nullable Level level, int k) {
      this.renderStatic((LivingEntity)null, itemstack, itemdisplaycontext, false, posestack, multibuffersource, level, i, j, k);
   }

   public void renderStatic(@Nullable LivingEntity livingentity, ItemStack itemstack, ItemDisplayContext itemdisplaycontext, boolean flag, PoseStack posestack, MultiBufferSource multibuffersource, @Nullable Level level, int i, int j, int k) {
      if (!itemstack.isEmpty()) {
         BakedModel bakedmodel = this.getModel(itemstack, level, livingentity, k);
         this.render(itemstack, itemdisplaycontext, flag, posestack, multibuffersource, i, j, bakedmodel);
      }
   }

   public void onResourceManagerReload(ResourceManager resourcemanager) {
      this.itemModelShaper.rebuildCache();
   }
}
