package net.minecraft.core.dispenser;

import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.WitherSkullBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import org.slf4j.Logger;

public interface DispenseItemBehavior {
   Logger LOGGER = LogUtils.getLogger();
   DispenseItemBehavior NOOP = (blocksource, itemstack) -> itemstack;

   ItemStack dispense(BlockSource blocksource, ItemStack itemstack);

   static void bootStrap() {
      DispenserBlock.registerBehavior(Items.ARROW, new AbstractProjectileDispenseBehavior() {
         protected Projectile getProjectile(Level level, Position position, ItemStack itemstack) {
            Arrow arrow = new Arrow(level, position.x(), position.y(), position.z());
            arrow.pickup = AbstractArrow.Pickup.ALLOWED;
            return arrow;
         }
      });
      DispenserBlock.registerBehavior(Items.TIPPED_ARROW, new AbstractProjectileDispenseBehavior() {
         protected Projectile getProjectile(Level level, Position position, ItemStack itemstack) {
            Arrow arrow = new Arrow(level, position.x(), position.y(), position.z());
            arrow.setEffectsFromItem(itemstack);
            arrow.pickup = AbstractArrow.Pickup.ALLOWED;
            return arrow;
         }
      });
      DispenserBlock.registerBehavior(Items.SPECTRAL_ARROW, new AbstractProjectileDispenseBehavior() {
         protected Projectile getProjectile(Level level, Position position, ItemStack itemstack) {
            AbstractArrow abstractarrow = new SpectralArrow(level, position.x(), position.y(), position.z());
            abstractarrow.pickup = AbstractArrow.Pickup.ALLOWED;
            return abstractarrow;
         }
      });
      DispenserBlock.registerBehavior(Items.EGG, new AbstractProjectileDispenseBehavior() {
         protected Projectile getProjectile(Level level, Position position, ItemStack itemstack) {
            return Util.make(new ThrownEgg(level, position.x(), position.y(), position.z()), (thrownegg) -> thrownegg.setItem(itemstack));
         }
      });
      DispenserBlock.registerBehavior(Items.SNOWBALL, new AbstractProjectileDispenseBehavior() {
         protected Projectile getProjectile(Level level, Position position, ItemStack itemstack) {
            return Util.make(new Snowball(level, position.x(), position.y(), position.z()), (snowball) -> snowball.setItem(itemstack));
         }
      });
      DispenserBlock.registerBehavior(Items.EXPERIENCE_BOTTLE, new AbstractProjectileDispenseBehavior() {
         protected Projectile getProjectile(Level level, Position position, ItemStack itemstack) {
            return Util.make(new ThrownExperienceBottle(level, position.x(), position.y(), position.z()), (thrownexperiencebottle) -> thrownexperiencebottle.setItem(itemstack));
         }

         protected float getUncertainty() {
            return super.getUncertainty() * 0.5F;
         }

         protected float getPower() {
            return super.getPower() * 1.25F;
         }
      });
      DispenserBlock.registerBehavior(Items.SPLASH_POTION, new DispenseItemBehavior() {
         public ItemStack dispense(BlockSource blocksource, ItemStack itemstack) {
            return (new AbstractProjectileDispenseBehavior() {
               protected Projectile getProjectile(Level level, Position position, ItemStack itemstack) {
                  return Util.make(new ThrownPotion(level, position.x(), position.y(), position.z()), (thrownpotion) -> thrownpotion.setItem(itemstack));
               }

               protected float getUncertainty() {
                  return super.getUncertainty() * 0.5F;
               }

               protected float getPower() {
                  return super.getPower() * 1.25F;
               }
            }).dispense(blocksource, itemstack);
         }
      });
      DispenserBlock.registerBehavior(Items.LINGERING_POTION, new DispenseItemBehavior() {
         public ItemStack dispense(BlockSource blocksource, ItemStack itemstack) {
            return (new AbstractProjectileDispenseBehavior() {
               protected Projectile getProjectile(Level level, Position position, ItemStack itemstack) {
                  return Util.make(new ThrownPotion(level, position.x(), position.y(), position.z()), (thrownpotion) -> thrownpotion.setItem(itemstack));
               }

               protected float getUncertainty() {
                  return super.getUncertainty() * 0.5F;
               }

               protected float getPower() {
                  return super.getPower() * 1.25F;
               }
            }).dispense(blocksource, itemstack);
         }
      });
      DefaultDispenseItemBehavior defaultdispenseitembehavior = new DefaultDispenseItemBehavior() {
         public ItemStack execute(BlockSource blocksource, ItemStack itemstack) {
            Direction direction = blocksource.getBlockState().getValue(DispenserBlock.FACING);
            EntityType<?> entitytype = ((SpawnEggItem)itemstack.getItem()).getType(itemstack.getTag());

            try {
               entitytype.spawn(blocksource.getLevel(), itemstack, (Player)null, blocksource.getPos().relative(direction), MobSpawnType.DISPENSER, direction != Direction.UP, false);
            } catch (Exception var6) {
               LOGGER.error("Error while dispensing spawn egg from dispenser at {}", blocksource.getPos(), var6);
               return ItemStack.EMPTY;
            }

            itemstack.shrink(1);
            blocksource.getLevel().gameEvent((Entity)null, GameEvent.ENTITY_PLACE, blocksource.getPos());
            return itemstack;
         }
      };

      for(SpawnEggItem spawneggitem : SpawnEggItem.eggs()) {
         DispenserBlock.registerBehavior(spawneggitem, defaultdispenseitembehavior);
      }

      DispenserBlock.registerBehavior(Items.ARMOR_STAND, new DefaultDispenseItemBehavior() {
         public ItemStack execute(BlockSource blocksource, ItemStack itemstack) {
            Direction direction = blocksource.getBlockState().getValue(DispenserBlock.FACING);
            BlockPos blockpos = blocksource.getPos().relative(direction);
            ServerLevel serverlevel = blocksource.getLevel();
            Consumer<ArmorStand> consumer = EntityType.appendDefaultStackConfig((armorstand1) -> armorstand1.setYRot(direction.toYRot()), serverlevel, itemstack, (Player)null);
            ArmorStand armorstand = EntityType.ARMOR_STAND.spawn(serverlevel, itemstack.getTag(), consumer, blockpos, MobSpawnType.DISPENSER, false, false);
            if (armorstand != null) {
               itemstack.shrink(1);
            }

            return itemstack;
         }
      });
      DispenserBlock.registerBehavior(Items.SADDLE, new OptionalDispenseItemBehavior() {
         public ItemStack execute(BlockSource blocksource, ItemStack itemstack) {
            BlockPos blockpos = blocksource.getPos().relative(blocksource.getBlockState().getValue(DispenserBlock.FACING));
            List<LivingEntity> list = blocksource.getLevel().getEntitiesOfClass(LivingEntity.class, new AABB(blockpos), (livingentity) -> {
               if (!(livingentity instanceof Saddleable saddleable)) {
                  return false;
               } else {
                  return !saddleable.isSaddled() && saddleable.isSaddleable();
               }
            });
            if (!list.isEmpty()) {
               ((Saddleable)list.get(0)).equipSaddle(SoundSource.BLOCKS);
               itemstack.shrink(1);
               this.setSuccess(true);
               return itemstack;
            } else {
               return super.execute(blocksource, itemstack);
            }
         }
      });
      DefaultDispenseItemBehavior defaultdispenseitembehavior1 = new OptionalDispenseItemBehavior() {
         protected ItemStack execute(BlockSource blocksource, ItemStack itemstack) {
            BlockPos blockpos = blocksource.getPos().relative(blocksource.getBlockState().getValue(DispenserBlock.FACING));

            for(AbstractHorse abstracthorse : blocksource.getLevel().getEntitiesOfClass(AbstractHorse.class, new AABB(blockpos), (abstracthorse1) -> abstracthorse1.isAlive() && abstracthorse1.canWearArmor())) {
               if (abstracthorse.isArmor(itemstack) && !abstracthorse.isWearingArmor() && abstracthorse.isTamed()) {
                  abstracthorse.getSlot(401).set(itemstack.split(1));
                  this.setSuccess(true);
                  return itemstack;
               }
            }

            return super.execute(blocksource, itemstack);
         }
      };
      DispenserBlock.registerBehavior(Items.LEATHER_HORSE_ARMOR, defaultdispenseitembehavior1);
      DispenserBlock.registerBehavior(Items.IRON_HORSE_ARMOR, defaultdispenseitembehavior1);
      DispenserBlock.registerBehavior(Items.GOLDEN_HORSE_ARMOR, defaultdispenseitembehavior1);
      DispenserBlock.registerBehavior(Items.DIAMOND_HORSE_ARMOR, defaultdispenseitembehavior1);
      DispenserBlock.registerBehavior(Items.WHITE_CARPET, defaultdispenseitembehavior1);
      DispenserBlock.registerBehavior(Items.ORANGE_CARPET, defaultdispenseitembehavior1);
      DispenserBlock.registerBehavior(Items.CYAN_CARPET, defaultdispenseitembehavior1);
      DispenserBlock.registerBehavior(Items.BLUE_CARPET, defaultdispenseitembehavior1);
      DispenserBlock.registerBehavior(Items.BROWN_CARPET, defaultdispenseitembehavior1);
      DispenserBlock.registerBehavior(Items.BLACK_CARPET, defaultdispenseitembehavior1);
      DispenserBlock.registerBehavior(Items.GRAY_CARPET, defaultdispenseitembehavior1);
      DispenserBlock.registerBehavior(Items.GREEN_CARPET, defaultdispenseitembehavior1);
      DispenserBlock.registerBehavior(Items.LIGHT_BLUE_CARPET, defaultdispenseitembehavior1);
      DispenserBlock.registerBehavior(Items.LIGHT_GRAY_CARPET, defaultdispenseitembehavior1);
      DispenserBlock.registerBehavior(Items.LIME_CARPET, defaultdispenseitembehavior1);
      DispenserBlock.registerBehavior(Items.MAGENTA_CARPET, defaultdispenseitembehavior1);
      DispenserBlock.registerBehavior(Items.PINK_CARPET, defaultdispenseitembehavior1);
      DispenserBlock.registerBehavior(Items.PURPLE_CARPET, defaultdispenseitembehavior1);
      DispenserBlock.registerBehavior(Items.RED_CARPET, defaultdispenseitembehavior1);
      DispenserBlock.registerBehavior(Items.YELLOW_CARPET, defaultdispenseitembehavior1);
      DispenserBlock.registerBehavior(Items.CHEST, new OptionalDispenseItemBehavior() {
         public ItemStack execute(BlockSource blocksource, ItemStack itemstack) {
            BlockPos blockpos = blocksource.getPos().relative(blocksource.getBlockState().getValue(DispenserBlock.FACING));

            for(AbstractChestedHorse abstractchestedhorse : blocksource.getLevel().getEntitiesOfClass(AbstractChestedHorse.class, new AABB(blockpos), (abstractchestedhorse1) -> abstractchestedhorse1.isAlive() && !abstractchestedhorse1.hasChest())) {
               if (abstractchestedhorse.isTamed() && abstractchestedhorse.getSlot(499).set(itemstack)) {
                  itemstack.shrink(1);
                  this.setSuccess(true);
                  return itemstack;
               }
            }

            return super.execute(blocksource, itemstack);
         }
      });
      DispenserBlock.registerBehavior(Items.FIREWORK_ROCKET, new DefaultDispenseItemBehavior() {
         public ItemStack execute(BlockSource blocksource, ItemStack itemstack) {
            Direction direction = blocksource.getBlockState().getValue(DispenserBlock.FACING);
            FireworkRocketEntity fireworkrocketentity = new FireworkRocketEntity(blocksource.getLevel(), itemstack, blocksource.x(), blocksource.y(), blocksource.x(), true);
            DispenseItemBehavior.setEntityPokingOutOfBlock(blocksource, fireworkrocketentity, direction);
            fireworkrocketentity.shoot((double)direction.getStepX(), (double)direction.getStepY(), (double)direction.getStepZ(), 0.5F, 1.0F);
            blocksource.getLevel().addFreshEntity(fireworkrocketentity);
            itemstack.shrink(1);
            return itemstack;
         }

         protected void playSound(BlockSource blocksource) {
            blocksource.getLevel().levelEvent(1004, blocksource.getPos(), 0);
         }
      });
      DispenserBlock.registerBehavior(Items.FIRE_CHARGE, new DefaultDispenseItemBehavior() {
         public ItemStack execute(BlockSource blocksource, ItemStack itemstack) {
            Direction direction = blocksource.getBlockState().getValue(DispenserBlock.FACING);
            Position position = DispenserBlock.getDispensePosition(blocksource);
            double d0 = position.x() + (double)((float)direction.getStepX() * 0.3F);
            double d1 = position.y() + (double)((float)direction.getStepY() * 0.3F);
            double d2 = position.z() + (double)((float)direction.getStepZ() * 0.3F);
            Level level = blocksource.getLevel();
            RandomSource randomsource = level.random;
            double d3 = randomsource.triangle((double)direction.getStepX(), 0.11485000000000001D);
            double d4 = randomsource.triangle((double)direction.getStepY(), 0.11485000000000001D);
            double d5 = randomsource.triangle((double)direction.getStepZ(), 0.11485000000000001D);
            SmallFireball smallfireball = new SmallFireball(level, d0, d1, d2, d3, d4, d5);
            level.addFreshEntity(Util.make(smallfireball, (smallfireball1) -> smallfireball1.setItem(itemstack)));
            itemstack.shrink(1);
            return itemstack;
         }

         protected void playSound(BlockSource blocksource) {
            blocksource.getLevel().levelEvent(1018, blocksource.getPos(), 0);
         }
      });
      DispenserBlock.registerBehavior(Items.OAK_BOAT, new BoatDispenseItemBehavior(Boat.Type.OAK));
      DispenserBlock.registerBehavior(Items.SPRUCE_BOAT, new BoatDispenseItemBehavior(Boat.Type.SPRUCE));
      DispenserBlock.registerBehavior(Items.BIRCH_BOAT, new BoatDispenseItemBehavior(Boat.Type.BIRCH));
      DispenserBlock.registerBehavior(Items.JUNGLE_BOAT, new BoatDispenseItemBehavior(Boat.Type.JUNGLE));
      DispenserBlock.registerBehavior(Items.DARK_OAK_BOAT, new BoatDispenseItemBehavior(Boat.Type.DARK_OAK));
      DispenserBlock.registerBehavior(Items.ACACIA_BOAT, new BoatDispenseItemBehavior(Boat.Type.ACACIA));
      DispenserBlock.registerBehavior(Items.CHERRY_BOAT, new BoatDispenseItemBehavior(Boat.Type.CHERRY));
      DispenserBlock.registerBehavior(Items.MANGROVE_BOAT, new BoatDispenseItemBehavior(Boat.Type.MANGROVE));
      DispenserBlock.registerBehavior(Items.BAMBOO_RAFT, new BoatDispenseItemBehavior(Boat.Type.BAMBOO));
      DispenserBlock.registerBehavior(Items.OAK_CHEST_BOAT, new BoatDispenseItemBehavior(Boat.Type.OAK, true));
      DispenserBlock.registerBehavior(Items.SPRUCE_CHEST_BOAT, new BoatDispenseItemBehavior(Boat.Type.SPRUCE, true));
      DispenserBlock.registerBehavior(Items.BIRCH_CHEST_BOAT, new BoatDispenseItemBehavior(Boat.Type.BIRCH, true));
      DispenserBlock.registerBehavior(Items.JUNGLE_CHEST_BOAT, new BoatDispenseItemBehavior(Boat.Type.JUNGLE, true));
      DispenserBlock.registerBehavior(Items.DARK_OAK_CHEST_BOAT, new BoatDispenseItemBehavior(Boat.Type.DARK_OAK, true));
      DispenserBlock.registerBehavior(Items.ACACIA_CHEST_BOAT, new BoatDispenseItemBehavior(Boat.Type.ACACIA, true));
      DispenserBlock.registerBehavior(Items.CHERRY_CHEST_BOAT, new BoatDispenseItemBehavior(Boat.Type.CHERRY, true));
      DispenserBlock.registerBehavior(Items.MANGROVE_CHEST_BOAT, new BoatDispenseItemBehavior(Boat.Type.MANGROVE, true));
      DispenserBlock.registerBehavior(Items.BAMBOO_CHEST_RAFT, new BoatDispenseItemBehavior(Boat.Type.BAMBOO, true));
      DispenseItemBehavior dispenseitembehavior = new DefaultDispenseItemBehavior() {
         private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

         public ItemStack execute(BlockSource blocksource, ItemStack itemstack) {
            DispensibleContainerItem dispensiblecontaineritem = (DispensibleContainerItem)itemstack.getItem();
            BlockPos blockpos = blocksource.getPos().relative(blocksource.getBlockState().getValue(DispenserBlock.FACING));
            Level level = blocksource.getLevel();
            if (dispensiblecontaineritem.emptyContents((Player)null, level, blockpos, (BlockHitResult)null)) {
               dispensiblecontaineritem.checkExtraContent((Player)null, level, itemstack, blockpos);
               return new ItemStack(Items.BUCKET);
            } else {
               return this.defaultDispenseItemBehavior.dispense(blocksource, itemstack);
            }
         }
      };
      DispenserBlock.registerBehavior(Items.LAVA_BUCKET, dispenseitembehavior);
      DispenserBlock.registerBehavior(Items.WATER_BUCKET, dispenseitembehavior);
      DispenserBlock.registerBehavior(Items.POWDER_SNOW_BUCKET, dispenseitembehavior);
      DispenserBlock.registerBehavior(Items.SALMON_BUCKET, dispenseitembehavior);
      DispenserBlock.registerBehavior(Items.COD_BUCKET, dispenseitembehavior);
      DispenserBlock.registerBehavior(Items.PUFFERFISH_BUCKET, dispenseitembehavior);
      DispenserBlock.registerBehavior(Items.TROPICAL_FISH_BUCKET, dispenseitembehavior);
      DispenserBlock.registerBehavior(Items.AXOLOTL_BUCKET, dispenseitembehavior);
      DispenserBlock.registerBehavior(Items.TADPOLE_BUCKET, dispenseitembehavior);
      DispenserBlock.registerBehavior(Items.BUCKET, new DefaultDispenseItemBehavior() {
         private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

         public ItemStack execute(BlockSource blocksource, ItemStack itemstack) {
            LevelAccessor levelaccessor = blocksource.getLevel();
            BlockPos blockpos = blocksource.getPos().relative(blocksource.getBlockState().getValue(DispenserBlock.FACING));
            BlockState blockstate = levelaccessor.getBlockState(blockpos);
            Block block = blockstate.getBlock();
            if (block instanceof BucketPickup) {
               ItemStack itemstack1 = ((BucketPickup)block).pickupBlock(levelaccessor, blockpos, blockstate);
               if (itemstack1.isEmpty()) {
                  return super.execute(blocksource, itemstack);
               } else {
                  levelaccessor.gameEvent((Entity)null, GameEvent.FLUID_PICKUP, blockpos);
                  Item item = itemstack1.getItem();
                  itemstack.shrink(1);
                  if (itemstack.isEmpty()) {
                     return new ItemStack(item);
                  } else {
                     if (blocksource.<DispenserBlockEntity>getEntity().addItem(new ItemStack(item)) < 0) {
                        this.defaultDispenseItemBehavior.dispense(blocksource, new ItemStack(item));
                     }

                     return itemstack;
                  }
               }
            } else {
               return super.execute(blocksource, itemstack);
            }
         }
      });
      DispenserBlock.registerBehavior(Items.FLINT_AND_STEEL, new OptionalDispenseItemBehavior() {
         protected ItemStack execute(BlockSource blocksource, ItemStack itemstack) {
            Level level = blocksource.getLevel();
            this.setSuccess(true);
            Direction direction = blocksource.getBlockState().getValue(DispenserBlock.FACING);
            BlockPos blockpos = blocksource.getPos().relative(direction);
            BlockState blockstate = level.getBlockState(blockpos);
            if (BaseFireBlock.canBePlacedAt(level, blockpos, direction)) {
               level.setBlockAndUpdate(blockpos, BaseFireBlock.getState(level, blockpos));
               level.gameEvent((Entity)null, GameEvent.BLOCK_PLACE, blockpos);
            } else if (!CampfireBlock.canLight(blockstate) && !CandleBlock.canLight(blockstate) && !CandleCakeBlock.canLight(blockstate)) {
               if (blockstate.getBlock() instanceof TntBlock) {
                  TntBlock.explode(level, blockpos);
                  level.removeBlock(blockpos, false);
               } else {
                  this.setSuccess(false);
               }
            } else {
               level.setBlockAndUpdate(blockpos, blockstate.setValue(BlockStateProperties.LIT, Boolean.valueOf(true)));
               level.gameEvent((Entity)null, GameEvent.BLOCK_CHANGE, blockpos);
            }

            if (this.isSuccess() && itemstack.hurt(1, level.random, (ServerPlayer)null)) {
               itemstack.setCount(0);
            }

            return itemstack;
         }
      });
      DispenserBlock.registerBehavior(Items.BONE_MEAL, new OptionalDispenseItemBehavior() {
         protected ItemStack execute(BlockSource blocksource, ItemStack itemstack) {
            this.setSuccess(true);
            Level level = blocksource.getLevel();
            BlockPos blockpos = blocksource.getPos().relative(blocksource.getBlockState().getValue(DispenserBlock.FACING));
            if (!BoneMealItem.growCrop(itemstack, level, blockpos) && !BoneMealItem.growWaterPlant(itemstack, level, blockpos, (Direction)null)) {
               this.setSuccess(false);
            } else if (!level.isClientSide) {
               level.levelEvent(1505, blockpos, 0);
            }

            return itemstack;
         }
      });
      DispenserBlock.registerBehavior(Blocks.TNT, new DefaultDispenseItemBehavior() {
         protected ItemStack execute(BlockSource blocksource, ItemStack itemstack) {
            Level level = blocksource.getLevel();
            BlockPos blockpos = blocksource.getPos().relative(blocksource.getBlockState().getValue(DispenserBlock.FACING));
            PrimedTnt primedtnt = new PrimedTnt(level, (double)blockpos.getX() + 0.5D, (double)blockpos.getY(), (double)blockpos.getZ() + 0.5D, (LivingEntity)null);
            level.addFreshEntity(primedtnt);
            level.playSound((Player)null, primedtnt.getX(), primedtnt.getY(), primedtnt.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent((Entity)null, GameEvent.ENTITY_PLACE, blockpos);
            itemstack.shrink(1);
            return itemstack;
         }
      });
      DispenseItemBehavior dispenseitembehavior1 = new OptionalDispenseItemBehavior() {
         protected ItemStack execute(BlockSource blocksource, ItemStack itemstack) {
            this.setSuccess(ArmorItem.dispenseArmor(blocksource, itemstack));
            return itemstack;
         }
      };
      DispenserBlock.registerBehavior(Items.CREEPER_HEAD, dispenseitembehavior1);
      DispenserBlock.registerBehavior(Items.ZOMBIE_HEAD, dispenseitembehavior1);
      DispenserBlock.registerBehavior(Items.DRAGON_HEAD, dispenseitembehavior1);
      DispenserBlock.registerBehavior(Items.SKELETON_SKULL, dispenseitembehavior1);
      DispenserBlock.registerBehavior(Items.PIGLIN_HEAD, dispenseitembehavior1);
      DispenserBlock.registerBehavior(Items.PLAYER_HEAD, dispenseitembehavior1);
      DispenserBlock.registerBehavior(Items.WITHER_SKELETON_SKULL, new OptionalDispenseItemBehavior() {
         protected ItemStack execute(BlockSource blocksource, ItemStack itemstack) {
            Level level = blocksource.getLevel();
            Direction direction = blocksource.getBlockState().getValue(DispenserBlock.FACING);
            BlockPos blockpos = blocksource.getPos().relative(direction);
            if (level.isEmptyBlock(blockpos) && WitherSkullBlock.canSpawnMob(level, blockpos, itemstack)) {
               level.setBlock(blockpos, Blocks.WITHER_SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, Integer.valueOf(RotationSegment.convertToSegment(direction))), 3);
               level.gameEvent((Entity)null, GameEvent.BLOCK_PLACE, blockpos);
               BlockEntity blockentity = level.getBlockEntity(blockpos);
               if (blockentity instanceof SkullBlockEntity) {
                  WitherSkullBlock.checkSpawn(level, blockpos, (SkullBlockEntity)blockentity);
               }

               itemstack.shrink(1);
               this.setSuccess(true);
            } else {
               this.setSuccess(ArmorItem.dispenseArmor(blocksource, itemstack));
            }

            return itemstack;
         }
      });
      DispenserBlock.registerBehavior(Blocks.CARVED_PUMPKIN, new OptionalDispenseItemBehavior() {
         protected ItemStack execute(BlockSource blocksource, ItemStack itemstack) {
            Level level = blocksource.getLevel();
            BlockPos blockpos = blocksource.getPos().relative(blocksource.getBlockState().getValue(DispenserBlock.FACING));
            CarvedPumpkinBlock carvedpumpkinblock = (CarvedPumpkinBlock)Blocks.CARVED_PUMPKIN;
            if (level.isEmptyBlock(blockpos) && carvedpumpkinblock.canSpawnGolem(level, blockpos)) {
               if (!level.isClientSide) {
                  level.setBlock(blockpos, carvedpumpkinblock.defaultBlockState(), 3);
                  level.gameEvent((Entity)null, GameEvent.BLOCK_PLACE, blockpos);
               }

               itemstack.shrink(1);
               this.setSuccess(true);
            } else {
               this.setSuccess(ArmorItem.dispenseArmor(blocksource, itemstack));
            }

            return itemstack;
         }
      });
      DispenserBlock.registerBehavior(Blocks.SHULKER_BOX.asItem(), new ShulkerBoxDispenseBehavior());

      for(DyeColor dyecolor : DyeColor.values()) {
         DispenserBlock.registerBehavior(ShulkerBoxBlock.getBlockByColor(dyecolor).asItem(), new ShulkerBoxDispenseBehavior());
      }

      DispenserBlock.registerBehavior(Items.GLASS_BOTTLE.asItem(), new OptionalDispenseItemBehavior() {
         private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

         private ItemStack takeLiquid(BlockSource blocksource, ItemStack itemstack, ItemStack itemstack1) {
            itemstack.shrink(1);
            if (itemstack.isEmpty()) {
               blocksource.getLevel().gameEvent((Entity)null, GameEvent.FLUID_PICKUP, blocksource.getPos());
               return itemstack1.copy();
            } else {
               if (blocksource.<DispenserBlockEntity>getEntity().addItem(itemstack1.copy()) < 0) {
                  this.defaultDispenseItemBehavior.dispense(blocksource, itemstack1.copy());
               }

               return itemstack;
            }
         }

         public ItemStack execute(BlockSource blocksource, ItemStack itemstack) {
            this.setSuccess(false);
            ServerLevel serverlevel = blocksource.getLevel();
            BlockPos blockpos = blocksource.getPos().relative(blocksource.getBlockState().getValue(DispenserBlock.FACING));
            BlockState blockstate = serverlevel.getBlockState(blockpos);
            if (blockstate.is(BlockTags.BEEHIVES, (blockbehaviour_blockstatebase) -> blockbehaviour_blockstatebase.hasProperty(BeehiveBlock.HONEY_LEVEL) && blockbehaviour_blockstatebase.getBlock() instanceof BeehiveBlock) && blockstate.getValue(BeehiveBlock.HONEY_LEVEL) >= 5) {
               ((BeehiveBlock)blockstate.getBlock()).releaseBeesAndResetHoneyLevel(serverlevel, blockstate, blockpos, (Player)null, BeehiveBlockEntity.BeeReleaseStatus.BEE_RELEASED);
               this.setSuccess(true);
               return this.takeLiquid(blocksource, itemstack, new ItemStack(Items.HONEY_BOTTLE));
            } else if (serverlevel.getFluidState(blockpos).is(FluidTags.WATER)) {
               this.setSuccess(true);
               return this.takeLiquid(blocksource, itemstack, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER));
            } else {
               return super.execute(blocksource, itemstack);
            }
         }
      });
      DispenserBlock.registerBehavior(Items.GLOWSTONE, new OptionalDispenseItemBehavior() {
         public ItemStack execute(BlockSource blocksource, ItemStack itemstack) {
            Direction direction = blocksource.getBlockState().getValue(DispenserBlock.FACING);
            BlockPos blockpos = blocksource.getPos().relative(direction);
            Level level = blocksource.getLevel();
            BlockState blockstate = level.getBlockState(blockpos);
            this.setSuccess(true);
            if (blockstate.is(Blocks.RESPAWN_ANCHOR)) {
               if (blockstate.getValue(RespawnAnchorBlock.CHARGE) != 4) {
                  RespawnAnchorBlock.charge((Entity)null, level, blockpos, blockstate);
                  itemstack.shrink(1);
               } else {
                  this.setSuccess(false);
               }

               return itemstack;
            } else {
               return super.execute(blocksource, itemstack);
            }
         }
      });
      DispenserBlock.registerBehavior(Items.SHEARS.asItem(), new ShearsDispenseItemBehavior());
      DispenserBlock.registerBehavior(Items.HONEYCOMB, new OptionalDispenseItemBehavior() {
         public ItemStack execute(BlockSource blocksource, ItemStack itemstack) {
            BlockPos blockpos = blocksource.getPos().relative(blocksource.getBlockState().getValue(DispenserBlock.FACING));
            Level level = blocksource.getLevel();
            BlockState blockstate = level.getBlockState(blockpos);
            Optional<BlockState> optional = HoneycombItem.getWaxed(blockstate);
            if (optional.isPresent()) {
               level.setBlockAndUpdate(blockpos, optional.get());
               level.levelEvent(3003, blockpos, 0);
               itemstack.shrink(1);
               this.setSuccess(true);
               return itemstack;
            } else {
               return super.execute(blocksource, itemstack);
            }
         }
      });
      DispenserBlock.registerBehavior(Items.POTION, new DefaultDispenseItemBehavior() {
         private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

         public ItemStack execute(BlockSource blocksource, ItemStack itemstack) {
            if (PotionUtils.getPotion(itemstack) != Potions.WATER) {
               return this.defaultDispenseItemBehavior.dispense(blocksource, itemstack);
            } else {
               ServerLevel serverlevel = blocksource.getLevel();
               BlockPos blockpos = blocksource.getPos();
               BlockPos blockpos1 = blocksource.getPos().relative(blocksource.getBlockState().getValue(DispenserBlock.FACING));
               if (!serverlevel.getBlockState(blockpos1).is(BlockTags.CONVERTABLE_TO_MUD)) {
                  return this.defaultDispenseItemBehavior.dispense(blocksource, itemstack);
               } else {
                  if (!serverlevel.isClientSide) {
                     for(int i = 0; i < 5; ++i) {
                        serverlevel.sendParticles(ParticleTypes.SPLASH, (double)blockpos.getX() + serverlevel.random.nextDouble(), (double)(blockpos.getY() + 1), (double)blockpos.getZ() + serverlevel.random.nextDouble(), 1, 0.0D, 0.0D, 0.0D, 1.0D);
                     }
                  }

                  serverlevel.playSound((Player)null, blockpos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                  serverlevel.gameEvent((Entity)null, GameEvent.FLUID_PLACE, blockpos);
                  serverlevel.setBlockAndUpdate(blockpos1, Blocks.MUD.defaultBlockState());
                  return new ItemStack(Items.GLASS_BOTTLE);
               }
            }
         }
      });
   }

   static void setEntityPokingOutOfBlock(BlockSource blocksource, Entity entity, Direction direction) {
      entity.setPos(blocksource.x() + (double)direction.getStepX() * (0.5000099999997474D - (double)entity.getBbWidth() / 2.0D), blocksource.y() + (double)direction.getStepY() * (0.5000099999997474D - (double)entity.getBbHeight() / 2.0D) - (double)entity.getBbHeight() / 2.0D, blocksource.z() + (double)direction.getStepZ() * (0.5000099999997474D - (double)entity.getBbWidth() / 2.0D));
   }
}
