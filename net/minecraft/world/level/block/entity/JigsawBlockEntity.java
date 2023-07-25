package net.minecraft.world.level.block.entity;

import java.util.Arrays;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class JigsawBlockEntity extends BlockEntity {
   public static final String TARGET = "target";
   public static final String POOL = "pool";
   public static final String JOINT = "joint";
   public static final String NAME = "name";
   public static final String FINAL_STATE = "final_state";
   private ResourceLocation name = new ResourceLocation("empty");
   private ResourceLocation target = new ResourceLocation("empty");
   private ResourceKey<StructureTemplatePool> pool = ResourceKey.create(Registries.TEMPLATE_POOL, new ResourceLocation("empty"));
   private JigsawBlockEntity.JointType joint = JigsawBlockEntity.JointType.ROLLABLE;
   private String finalState = "minecraft:air";

   public JigsawBlockEntity(BlockPos blockpos, BlockState blockstate) {
      super(BlockEntityType.JIGSAW, blockpos, blockstate);
   }

   public ResourceLocation getName() {
      return this.name;
   }

   public ResourceLocation getTarget() {
      return this.target;
   }

   public ResourceKey<StructureTemplatePool> getPool() {
      return this.pool;
   }

   public String getFinalState() {
      return this.finalState;
   }

   public JigsawBlockEntity.JointType getJoint() {
      return this.joint;
   }

   public void setName(ResourceLocation resourcelocation) {
      this.name = resourcelocation;
   }

   public void setTarget(ResourceLocation resourcelocation) {
      this.target = resourcelocation;
   }

   public void setPool(ResourceKey<StructureTemplatePool> resourcekey) {
      this.pool = resourcekey;
   }

   public void setFinalState(String s) {
      this.finalState = s;
   }

   public void setJoint(JigsawBlockEntity.JointType jigsawblockentity_jointtype) {
      this.joint = jigsawblockentity_jointtype;
   }

   protected void saveAdditional(CompoundTag compoundtag) {
      super.saveAdditional(compoundtag);
      compoundtag.putString("name", this.name.toString());
      compoundtag.putString("target", this.target.toString());
      compoundtag.putString("pool", this.pool.location().toString());
      compoundtag.putString("final_state", this.finalState);
      compoundtag.putString("joint", this.joint.getSerializedName());
   }

   public void load(CompoundTag compoundtag) {
      super.load(compoundtag);
      this.name = new ResourceLocation(compoundtag.getString("name"));
      this.target = new ResourceLocation(compoundtag.getString("target"));
      this.pool = ResourceKey.create(Registries.TEMPLATE_POOL, new ResourceLocation(compoundtag.getString("pool")));
      this.finalState = compoundtag.getString("final_state");
      this.joint = JigsawBlockEntity.JointType.byName(compoundtag.getString("joint")).orElseGet(() -> JigsawBlock.getFrontFacing(this.getBlockState()).getAxis().isHorizontal() ? JigsawBlockEntity.JointType.ALIGNED : JigsawBlockEntity.JointType.ROLLABLE);
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public CompoundTag getUpdateTag() {
      return this.saveWithoutMetadata();
   }

   public void generate(ServerLevel serverlevel, int i, boolean flag) {
      BlockPos blockpos = this.getBlockPos().relative(this.getBlockState().getValue(JigsawBlock.ORIENTATION).front());
      Registry<StructureTemplatePool> registry = serverlevel.registryAccess().registryOrThrow(Registries.TEMPLATE_POOL);
      Holder<StructureTemplatePool> holder = registry.getHolderOrThrow(this.pool);
      JigsawPlacement.generateJigsaw(serverlevel, holder, this.target, i, blockpos, flag);
   }

   public static enum JointType implements StringRepresentable {
      ROLLABLE("rollable"),
      ALIGNED("aligned");

      private final String name;

      private JointType(String s) {
         this.name = s;
      }

      public String getSerializedName() {
         return this.name;
      }

      public static Optional<JigsawBlockEntity.JointType> byName(String s) {
         return Arrays.stream(values()).filter((jigsawblockentity_jointtype) -> jigsawblockentity_jointtype.getSerializedName().equals(s)).findFirst();
      }

      public Component getTranslatedName() {
         return Component.translatable("jigsaw_block.joint." + this.name);
      }
   }
}
