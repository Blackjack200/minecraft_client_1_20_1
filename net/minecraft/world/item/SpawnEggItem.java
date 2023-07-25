package net.minecraft.world.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class SpawnEggItem extends Item {
   private static final Map<EntityType<? extends Mob>, SpawnEggItem> BY_ID = Maps.newIdentityHashMap();
   private final int backgroundColor;
   private final int highlightColor;
   private final EntityType<?> defaultType;

   public SpawnEggItem(EntityType<? extends Mob> entitytype, int i, int j, Item.Properties item_properties) {
      super(item_properties);
      this.defaultType = entitytype;
      this.backgroundColor = i;
      this.highlightColor = j;
      BY_ID.put(entitytype, this);
   }

   public InteractionResult useOn(UseOnContext useoncontext) {
      Level level = useoncontext.getLevel();
      if (!(level instanceof ServerLevel)) {
         return InteractionResult.SUCCESS;
      } else {
         ItemStack itemstack = useoncontext.getItemInHand();
         BlockPos blockpos = useoncontext.getClickedPos();
         Direction direction = useoncontext.getClickedFace();
         BlockState blockstate = level.getBlockState(blockpos);
         if (blockstate.is(Blocks.SPAWNER)) {
            BlockEntity blockentity = level.getBlockEntity(blockpos);
            if (blockentity instanceof SpawnerBlockEntity) {
               SpawnerBlockEntity spawnerblockentity = (SpawnerBlockEntity)blockentity;
               EntityType<?> entitytype = this.getType(itemstack.getTag());
               spawnerblockentity.setEntityId(entitytype, level.getRandom());
               blockentity.setChanged();
               level.sendBlockUpdated(blockpos, blockstate, blockstate, 3);
               level.gameEvent(useoncontext.getPlayer(), GameEvent.BLOCK_CHANGE, blockpos);
               itemstack.shrink(1);
               return InteractionResult.CONSUME;
            }
         }

         BlockPos blockpos1;
         if (blockstate.getCollisionShape(level, blockpos).isEmpty()) {
            blockpos1 = blockpos;
         } else {
            blockpos1 = blockpos.relative(direction);
         }

         EntityType<?> entitytype1 = this.getType(itemstack.getTag());
         if (entitytype1.spawn((ServerLevel)level, itemstack, useoncontext.getPlayer(), blockpos1, MobSpawnType.SPAWN_EGG, true, !Objects.equals(blockpos, blockpos1) && direction == Direction.UP) != null) {
            itemstack.shrink(1);
            level.gameEvent(useoncontext.getPlayer(), GameEvent.ENTITY_PLACE, blockpos);
         }

         return InteractionResult.CONSUME;
      }
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionhand) {
      ItemStack itemstack = player.getItemInHand(interactionhand);
      BlockHitResult blockhitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
      if (blockhitresult.getType() != HitResult.Type.BLOCK) {
         return InteractionResultHolder.pass(itemstack);
      } else if (!(level instanceof ServerLevel)) {
         return InteractionResultHolder.success(itemstack);
      } else {
         BlockPos blockpos = blockhitresult.getBlockPos();
         if (!(level.getBlockState(blockpos).getBlock() instanceof LiquidBlock)) {
            return InteractionResultHolder.pass(itemstack);
         } else if (level.mayInteract(player, blockpos) && player.mayUseItemAt(blockpos, blockhitresult.getDirection(), itemstack)) {
            EntityType<?> entitytype = this.getType(itemstack.getTag());
            Entity entity = entitytype.spawn((ServerLevel)level, itemstack, player, blockpos, MobSpawnType.SPAWN_EGG, false, false);
            if (entity == null) {
               return InteractionResultHolder.pass(itemstack);
            } else {
               if (!player.getAbilities().instabuild) {
                  itemstack.shrink(1);
               }

               player.awardStat(Stats.ITEM_USED.get(this));
               level.gameEvent(player, GameEvent.ENTITY_PLACE, entity.position());
               return InteractionResultHolder.consume(itemstack);
            }
         } else {
            return InteractionResultHolder.fail(itemstack);
         }
      }
   }

   public boolean spawnsEntity(@Nullable CompoundTag compoundtag, EntityType<?> entitytype) {
      return Objects.equals(this.getType(compoundtag), entitytype);
   }

   public int getColor(int i) {
      return i == 0 ? this.backgroundColor : this.highlightColor;
   }

   @Nullable
   public static SpawnEggItem byId(@Nullable EntityType<?> entitytype) {
      return BY_ID.get(entitytype);
   }

   public static Iterable<SpawnEggItem> eggs() {
      return Iterables.unmodifiableIterable(BY_ID.values());
   }

   public EntityType<?> getType(@Nullable CompoundTag compoundtag) {
      if (compoundtag != null && compoundtag.contains("EntityTag", 10)) {
         CompoundTag compoundtag1 = compoundtag.getCompound("EntityTag");
         if (compoundtag1.contains("id", 8)) {
            return EntityType.byString(compoundtag1.getString("id")).orElse(this.defaultType);
         }
      }

      return this.defaultType;
   }

   public FeatureFlagSet requiredFeatures() {
      return this.defaultType.requiredFeatures();
   }

   public Optional<Mob> spawnOffspringFromSpawnEgg(Player player, Mob mob, EntityType<? extends Mob> entitytype, ServerLevel serverlevel, Vec3 vec3, ItemStack itemstack) {
      if (!this.spawnsEntity(itemstack.getTag(), entitytype)) {
         return Optional.empty();
      } else {
         Mob mob1;
         if (mob instanceof AgeableMob) {
            mob1 = ((AgeableMob)mob).getBreedOffspring(serverlevel, (AgeableMob)mob);
         } else {
            mob1 = entitytype.create(serverlevel);
         }

         if (mob1 == null) {
            return Optional.empty();
         } else {
            mob1.setBaby(true);
            if (!mob1.isBaby()) {
               return Optional.empty();
            } else {
               mob1.moveTo(vec3.x(), vec3.y(), vec3.z(), 0.0F, 0.0F);
               serverlevel.addFreshEntityWithPassengers(mob1);
               if (itemstack.hasCustomHoverName()) {
                  mob1.setCustomName(itemstack.getHoverName());
               }

               if (!player.getAbilities().instabuild) {
                  itemstack.shrink(1);
               }

               return Optional.of(mob1);
            }
         }
      }
   }
}
