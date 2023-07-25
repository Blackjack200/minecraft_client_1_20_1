package net.minecraft.world.entity.vehicle;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class MinecartSpawner extends AbstractMinecart {
   private final BaseSpawner spawner = new BaseSpawner() {
      public void broadcastEvent(Level level, BlockPos blockpos, int i) {
         level.broadcastEntityEvent(MinecartSpawner.this, (byte)i);
      }
   };
   private final Runnable ticker;

   public MinecartSpawner(EntityType<? extends MinecartSpawner> entitytype, Level level) {
      super(entitytype, level);
      this.ticker = this.createTicker(level);
   }

   public MinecartSpawner(Level level, double d0, double d1, double d2) {
      super(EntityType.SPAWNER_MINECART, level, d0, d1, d2);
      this.ticker = this.createTicker(level);
   }

   protected Item getDropItem() {
      return Items.MINECART;
   }

   private Runnable createTicker(Level level) {
      return level instanceof ServerLevel ? () -> this.spawner.serverTick((ServerLevel)level, this.blockPosition()) : () -> this.spawner.clientTick(level, this.blockPosition());
   }

   public AbstractMinecart.Type getMinecartType() {
      return AbstractMinecart.Type.SPAWNER;
   }

   public BlockState getDefaultDisplayBlockState() {
      return Blocks.SPAWNER.defaultBlockState();
   }

   protected void readAdditionalSaveData(CompoundTag compoundtag) {
      super.readAdditionalSaveData(compoundtag);
      this.spawner.load(this.level(), this.blockPosition(), compoundtag);
   }

   protected void addAdditionalSaveData(CompoundTag compoundtag) {
      super.addAdditionalSaveData(compoundtag);
      this.spawner.save(compoundtag);
   }

   public void handleEntityEvent(byte b0) {
      this.spawner.onEventTriggered(this.level(), b0);
   }

   public void tick() {
      super.tick();
      this.ticker.run();
   }

   public BaseSpawner getSpawner() {
      return this.spawner;
   }

   public boolean onlyOpCanSetNbt() {
      return true;
   }
}
