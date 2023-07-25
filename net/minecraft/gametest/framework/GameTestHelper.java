package net.minecraft.gametest.framework;

import com.mojang.authlib.GameProfile;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.LongStream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class GameTestHelper {
   private final GameTestInfo testInfo;
   private boolean finalCheckAdded;

   public GameTestHelper(GameTestInfo gametestinfo) {
      this.testInfo = gametestinfo;
   }

   public ServerLevel getLevel() {
      return this.testInfo.getLevel();
   }

   public BlockState getBlockState(BlockPos blockpos) {
      return this.getLevel().getBlockState(this.absolutePos(blockpos));
   }

   @Nullable
   public BlockEntity getBlockEntity(BlockPos blockpos) {
      return this.getLevel().getBlockEntity(this.absolutePos(blockpos));
   }

   public void killAllEntities() {
      this.killAllEntitiesOfClass(Entity.class);
   }

   public void killAllEntitiesOfClass(Class oclass) {
      AABB aabb = this.getBounds();
      List<Entity> list = this.getLevel().getEntitiesOfClass(oclass, aabb.inflate(1.0D), (entity) -> !(entity instanceof Player));
      list.forEach(Entity::kill);
   }

   public ItemEntity spawnItem(Item item, float f, float f1, float f2) {
      ServerLevel serverlevel = this.getLevel();
      Vec3 vec3 = this.absoluteVec(new Vec3((double)f, (double)f1, (double)f2));
      ItemEntity itementity = new ItemEntity(serverlevel, vec3.x, vec3.y, vec3.z, new ItemStack(item, 1));
      itementity.setDeltaMovement(0.0D, 0.0D, 0.0D);
      serverlevel.addFreshEntity(itementity);
      return itementity;
   }

   public ItemEntity spawnItem(Item item, BlockPos blockpos) {
      return this.spawnItem(item, (float)blockpos.getX(), (float)blockpos.getY(), (float)blockpos.getZ());
   }

   public <E extends Entity> E spawn(EntityType<E> entitytype, BlockPos blockpos) {
      return this.spawn(entitytype, Vec3.atBottomCenterOf(blockpos));
   }

   public <E extends Entity> E spawn(EntityType<E> entitytype, Vec3 vec3) {
      ServerLevel serverlevel = this.getLevel();
      E entity = entitytype.create(serverlevel);
      if (entity == null) {
         throw new NullPointerException("Failed to create entity " + entitytype.builtInRegistryHolder().key().location());
      } else {
         if (entity instanceof Mob) {
            Mob mob = (Mob)entity;
            mob.setPersistenceRequired();
         }

         Vec3 vec31 = this.absoluteVec(vec3);
         entity.moveTo(vec31.x, vec31.y, vec31.z, entity.getYRot(), entity.getXRot());
         serverlevel.addFreshEntity(entity);
         return entity;
      }
   }

   public <E extends Entity> E spawn(EntityType<E> entitytype, int i, int j, int k) {
      return this.spawn(entitytype, new BlockPos(i, j, k));
   }

   public <E extends Entity> E spawn(EntityType<E> entitytype, float f, float f1, float f2) {
      return this.spawn(entitytype, new Vec3((double)f, (double)f1, (double)f2));
   }

   public <E extends Mob> E spawnWithNoFreeWill(EntityType<E> entitytype, BlockPos blockpos) {
      E mob = this.spawn(entitytype, blockpos);
      mob.removeFreeWill();
      return mob;
   }

   public <E extends Mob> E spawnWithNoFreeWill(EntityType<E> entitytype, int i, int j, int k) {
      return this.spawnWithNoFreeWill(entitytype, new BlockPos(i, j, k));
   }

   public <E extends Mob> E spawnWithNoFreeWill(EntityType<E> entitytype, Vec3 vec3) {
      E mob = this.spawn(entitytype, vec3);
      mob.removeFreeWill();
      return mob;
   }

   public <E extends Mob> E spawnWithNoFreeWill(EntityType<E> entitytype, float f, float f1, float f2) {
      return this.spawnWithNoFreeWill(entitytype, new Vec3((double)f, (double)f1, (double)f2));
   }

   public GameTestSequence walkTo(Mob mob, BlockPos blockpos, float f) {
      return this.startSequence().thenExecuteAfter(2, () -> {
         Path path = mob.getNavigation().createPath(this.absolutePos(blockpos), 0);
         mob.getNavigation().moveTo(path, (double)f);
      });
   }

   public void pressButton(int i, int j, int k) {
      this.pressButton(new BlockPos(i, j, k));
   }

   public void pressButton(BlockPos blockpos) {
      this.assertBlockState(blockpos, (blockstate1) -> blockstate1.is(BlockTags.BUTTONS), () -> "Expected button");
      BlockPos blockpos1 = this.absolutePos(blockpos);
      BlockState blockstate = this.getLevel().getBlockState(blockpos1);
      ButtonBlock buttonblock = (ButtonBlock)blockstate.getBlock();
      buttonblock.press(blockstate, this.getLevel(), blockpos1);
   }

   public void useBlock(BlockPos blockpos) {
      this.useBlock(blockpos, this.makeMockPlayer());
   }

   public void useBlock(BlockPos blockpos, Player player) {
      BlockPos blockpos1 = this.absolutePos(blockpos);
      this.useBlock(blockpos, player, new BlockHitResult(Vec3.atCenterOf(blockpos1), Direction.NORTH, blockpos1, true));
   }

   public void useBlock(BlockPos blockpos, Player player, BlockHitResult blockhitresult) {
      BlockPos blockpos1 = this.absolutePos(blockpos);
      BlockState blockstate = this.getLevel().getBlockState(blockpos1);
      InteractionResult interactionresult = blockstate.use(this.getLevel(), player, InteractionHand.MAIN_HAND, blockhitresult);
      if (!interactionresult.consumesAction()) {
         UseOnContext useoncontext = new UseOnContext(player, InteractionHand.MAIN_HAND, blockhitresult);
         player.getItemInHand(InteractionHand.MAIN_HAND).useOn(useoncontext);
      }

   }

   public LivingEntity makeAboutToDrown(LivingEntity livingentity) {
      livingentity.setAirSupply(0);
      livingentity.setHealth(0.25F);
      return livingentity;
   }

   public Player makeMockSurvivalPlayer() {
      return new Player(this.getLevel(), BlockPos.ZERO, 0.0F, new GameProfile(UUID.randomUUID(), "test-mock-player")) {
         public boolean isSpectator() {
            return false;
         }

         public boolean isCreative() {
            return false;
         }
      };
   }

   public LivingEntity withLowHealth(LivingEntity livingentity) {
      livingentity.setHealth(0.25F);
      return livingentity;
   }

   public Player makeMockPlayer() {
      return new Player(this.getLevel(), BlockPos.ZERO, 0.0F, new GameProfile(UUID.randomUUID(), "test-mock-player")) {
         public boolean isSpectator() {
            return false;
         }

         public boolean isCreative() {
            return true;
         }

         public boolean isLocalPlayer() {
            return true;
         }
      };
   }

   public ServerPlayer makeMockServerPlayerInLevel() {
      ServerPlayer serverplayer = new ServerPlayer(this.getLevel().getServer(), this.getLevel(), new GameProfile(UUID.randomUUID(), "test-mock-player")) {
         public boolean isSpectator() {
            return false;
         }

         public boolean isCreative() {
            return true;
         }
      };
      this.getLevel().getServer().getPlayerList().placeNewPlayer(new Connection(PacketFlow.SERVERBOUND), serverplayer);
      return serverplayer;
   }

   public void pullLever(int i, int j, int k) {
      this.pullLever(new BlockPos(i, j, k));
   }

   public void pullLever(BlockPos blockpos) {
      this.assertBlockPresent(Blocks.LEVER, blockpos);
      BlockPos blockpos1 = this.absolutePos(blockpos);
      BlockState blockstate = this.getLevel().getBlockState(blockpos1);
      LeverBlock leverblock = (LeverBlock)blockstate.getBlock();
      leverblock.pull(blockstate, this.getLevel(), blockpos1);
   }

   public void pulseRedstone(BlockPos blockpos, long i) {
      this.setBlock(blockpos, Blocks.REDSTONE_BLOCK);
      this.runAfterDelay(i, () -> this.setBlock(blockpos, Blocks.AIR));
   }

   public void destroyBlock(BlockPos blockpos) {
      this.getLevel().destroyBlock(this.absolutePos(blockpos), false, (Entity)null);
   }

   public void setBlock(int i, int j, int k, Block block) {
      this.setBlock(new BlockPos(i, j, k), block);
   }

   public void setBlock(int i, int j, int k, BlockState blockstate) {
      this.setBlock(new BlockPos(i, j, k), blockstate);
   }

   public void setBlock(BlockPos blockpos, Block block) {
      this.setBlock(blockpos, block.defaultBlockState());
   }

   public void setBlock(BlockPos blockpos, BlockState blockstate) {
      this.getLevel().setBlock(this.absolutePos(blockpos), blockstate, 3);
   }

   public void setNight() {
      this.setDayTime(13000);
   }

   public void setDayTime(int i) {
      this.getLevel().setDayTime((long)i);
   }

   public void assertBlockPresent(Block block, int i, int j, int k) {
      this.assertBlockPresent(block, new BlockPos(i, j, k));
   }

   public void assertBlockPresent(Block block, BlockPos blockpos) {
      BlockState blockstate = this.getBlockState(blockpos);
      this.assertBlock(blockpos, (block2) -> blockstate.is(block), "Expected " + block.getName().getString() + ", got " + blockstate.getBlock().getName().getString());
   }

   public void assertBlockNotPresent(Block block, int i, int j, int k) {
      this.assertBlockNotPresent(block, new BlockPos(i, j, k));
   }

   public void assertBlockNotPresent(Block block, BlockPos blockpos) {
      this.assertBlock(blockpos, (block2) -> !this.getBlockState(blockpos).is(block), "Did not expect " + block.getName().getString());
   }

   public void succeedWhenBlockPresent(Block block, int i, int j, int k) {
      this.succeedWhenBlockPresent(block, new BlockPos(i, j, k));
   }

   public void succeedWhenBlockPresent(Block block, BlockPos blockpos) {
      this.succeedWhen(() -> this.assertBlockPresent(block, blockpos));
   }

   public void assertBlock(BlockPos blockpos, Predicate<Block> predicate, String s) {
      this.assertBlock(blockpos, predicate, () -> s);
   }

   public void assertBlock(BlockPos blockpos, Predicate<Block> predicate, Supplier<String> supplier) {
      this.assertBlockState(blockpos, (blockstate) -> predicate.test(blockstate.getBlock()), supplier);
   }

   public <T extends Comparable<T>> void assertBlockProperty(BlockPos blockpos, Property<T> property, T comparable) {
      BlockState blockstate = this.getBlockState(blockpos);
      boolean flag = blockstate.hasProperty(property);
      if (!flag || !blockstate.<T>getValue(property).equals(comparable)) {
         String s = flag ? "was " + blockstate.getValue(property) : "property " + property.getName() + " is missing";
         String s1 = String.format(Locale.ROOT, "Expected property %s to be %s, %s", property.getName(), comparable, s);
         throw new GameTestAssertPosException(s1, this.absolutePos(blockpos), blockpos, this.testInfo.getTick());
      }
   }

   public <T extends Comparable<T>> void assertBlockProperty(BlockPos blockpos, Property<T> property, Predicate<T> predicate, String s) {
      this.assertBlockState(blockpos, (blockstate) -> {
         if (!blockstate.hasProperty(property)) {
            return false;
         } else {
            T comparable = blockstate.getValue(property);
            return predicate.test(comparable);
         }
      }, () -> s);
   }

   public void assertBlockState(BlockPos blockpos, Predicate<BlockState> predicate, Supplier<String> supplier) {
      BlockState blockstate = this.getBlockState(blockpos);
      if (!predicate.test(blockstate)) {
         throw new GameTestAssertPosException(supplier.get(), this.absolutePos(blockpos), blockpos, this.testInfo.getTick());
      }
   }

   public void assertRedstoneSignal(BlockPos blockpos, Direction direction, IntPredicate intpredicate, Supplier<String> supplier) {
      BlockPos blockpos1 = this.absolutePos(blockpos);
      ServerLevel serverlevel = this.getLevel();
      BlockState blockstate = serverlevel.getBlockState(blockpos1);
      int i = blockstate.getSignal(serverlevel, blockpos1, direction);
      if (!intpredicate.test(i)) {
         throw new GameTestAssertPosException(supplier.get(), blockpos1, blockpos, this.testInfo.getTick());
      }
   }

   public void assertEntityPresent(EntityType<?> entitytype) {
      List<? extends Entity> list = this.getLevel().getEntities(entitytype, this.getBounds(), Entity::isAlive);
      if (list.isEmpty()) {
         throw new GameTestAssertException("Expected " + entitytype.toShortString() + " to exist");
      }
   }

   public void assertEntityPresent(EntityType<?> entitytype, int i, int j, int k) {
      this.assertEntityPresent(entitytype, new BlockPos(i, j, k));
   }

   public void assertEntityPresent(EntityType<?> entitytype, BlockPos blockpos) {
      BlockPos blockpos1 = this.absolutePos(blockpos);
      List<? extends Entity> list = this.getLevel().getEntities(entitytype, new AABB(blockpos1), Entity::isAlive);
      if (list.isEmpty()) {
         throw new GameTestAssertPosException("Expected " + entitytype.toShortString(), blockpos1, blockpos, this.testInfo.getTick());
      }
   }

   public void assertEntityPresent(EntityType<?> entitytype, Vec3 vec3, Vec3 vec31) {
      List<? extends Entity> list = this.getLevel().getEntities(entitytype, new AABB(vec3, vec31), Entity::isAlive);
      if (list.isEmpty()) {
         throw new GameTestAssertPosException("Expected " + entitytype.toShortString() + " between ", BlockPos.containing(vec3), BlockPos.containing(vec31), this.testInfo.getTick());
      }
   }

   public void assertEntitiesPresent(EntityType<?> entitytype, BlockPos blockpos, int i, double d0) {
      BlockPos blockpos1 = this.absolutePos(blockpos);
      List<? extends Entity> list = this.getEntities(entitytype, blockpos, d0);
      if (list.size() != i) {
         throw new GameTestAssertPosException("Expected " + i + " entities of type " + entitytype.toShortString() + ", actual number of entities found=" + list.size(), blockpos1, blockpos, this.testInfo.getTick());
      }
   }

   public void assertEntityPresent(EntityType<?> entitytype, BlockPos blockpos, double d0) {
      List<? extends Entity> list = this.getEntities(entitytype, blockpos, d0);
      if (list.isEmpty()) {
         BlockPos blockpos1 = this.absolutePos(blockpos);
         throw new GameTestAssertPosException("Expected " + entitytype.toShortString(), blockpos1, blockpos, this.testInfo.getTick());
      }
   }

   public <T extends Entity> List<T> getEntities(EntityType<T> entitytype, BlockPos blockpos, double d0) {
      BlockPos blockpos1 = this.absolutePos(blockpos);
      return this.getLevel().getEntities(entitytype, (new AABB(blockpos1)).inflate(d0), Entity::isAlive);
   }

   public void assertEntityInstancePresent(Entity entity, int i, int j, int k) {
      this.assertEntityInstancePresent(entity, new BlockPos(i, j, k));
   }

   public void assertEntityInstancePresent(Entity entity, BlockPos blockpos) {
      BlockPos blockpos1 = this.absolutePos(blockpos);
      List<? extends Entity> list = this.getLevel().getEntities(entity.getType(), new AABB(blockpos1), Entity::isAlive);
      list.stream().filter((entity3) -> entity3 == entity).findFirst().orElseThrow(() -> new GameTestAssertPosException("Expected " + entity.getType().toShortString(), blockpos1, blockpos, this.testInfo.getTick()));
   }

   public void assertItemEntityCountIs(Item item, BlockPos blockpos, double d0, int i) {
      BlockPos blockpos1 = this.absolutePos(blockpos);
      List<ItemEntity> list = this.getLevel().getEntities(EntityType.ITEM, (new AABB(blockpos1)).inflate(d0), Entity::isAlive);
      int j = 0;

      for(ItemEntity itementity : list) {
         ItemStack itemstack = itementity.getItem();
         if (itemstack.is(item)) {
            j += itemstack.getCount();
         }
      }

      if (j != i) {
         throw new GameTestAssertPosException("Expected " + i + " " + item.getDescription().getString() + " items to exist (found " + j + ")", blockpos1, blockpos, this.testInfo.getTick());
      }
   }

   public void assertItemEntityPresent(Item item, BlockPos blockpos, double d0) {
      BlockPos blockpos1 = this.absolutePos(blockpos);

      for(Entity entity : this.getLevel().getEntities(EntityType.ITEM, (new AABB(blockpos1)).inflate(d0), Entity::isAlive)) {
         ItemEntity itementity = (ItemEntity)entity;
         if (itementity.getItem().getItem().equals(item)) {
            return;
         }
      }

      throw new GameTestAssertPosException("Expected " + item.getDescription().getString() + " item", blockpos1, blockpos, this.testInfo.getTick());
   }

   public void assertItemEntityNotPresent(Item item, BlockPos blockpos, double d0) {
      BlockPos blockpos1 = this.absolutePos(blockpos);

      for(Entity entity : this.getLevel().getEntities(EntityType.ITEM, (new AABB(blockpos1)).inflate(d0), Entity::isAlive)) {
         ItemEntity itementity = (ItemEntity)entity;
         if (itementity.getItem().getItem().equals(item)) {
            throw new GameTestAssertPosException("Did not expect " + item.getDescription().getString() + " item", blockpos1, blockpos, this.testInfo.getTick());
         }
      }

   }

   public void assertEntityNotPresent(EntityType<?> entitytype) {
      List<? extends Entity> list = this.getLevel().getEntities(entitytype, this.getBounds(), Entity::isAlive);
      if (!list.isEmpty()) {
         throw new GameTestAssertException("Did not expect " + entitytype.toShortString() + " to exist");
      }
   }

   public void assertEntityNotPresent(EntityType<?> entitytype, int i, int j, int k) {
      this.assertEntityNotPresent(entitytype, new BlockPos(i, j, k));
   }

   public void assertEntityNotPresent(EntityType<?> entitytype, BlockPos blockpos) {
      BlockPos blockpos1 = this.absolutePos(blockpos);
      List<? extends Entity> list = this.getLevel().getEntities(entitytype, new AABB(blockpos1), Entity::isAlive);
      if (!list.isEmpty()) {
         throw new GameTestAssertPosException("Did not expect " + entitytype.toShortString(), blockpos1, blockpos, this.testInfo.getTick());
      }
   }

   public void assertEntityTouching(EntityType<?> entitytype, double d0, double d1, double d2) {
      Vec3 vec3 = new Vec3(d0, d1, d2);
      Vec3 vec31 = this.absoluteVec(vec3);
      Predicate<? super Entity> predicate = (entity) -> entity.getBoundingBox().intersects(vec31, vec31);
      List<? extends Entity> list = this.getLevel().getEntities(entitytype, this.getBounds(), predicate);
      if (list.isEmpty()) {
         throw new GameTestAssertException("Expected " + entitytype.toShortString() + " to touch " + vec31 + " (relative " + vec3 + ")");
      }
   }

   public void assertEntityNotTouching(EntityType<?> entitytype, double d0, double d1, double d2) {
      Vec3 vec3 = new Vec3(d0, d1, d2);
      Vec3 vec31 = this.absoluteVec(vec3);
      Predicate<? super Entity> predicate = (entity) -> !entity.getBoundingBox().intersects(vec31, vec31);
      List<? extends Entity> list = this.getLevel().getEntities(entitytype, this.getBounds(), predicate);
      if (list.isEmpty()) {
         throw new GameTestAssertException("Did not expect " + entitytype.toShortString() + " to touch " + vec31 + " (relative " + vec3 + ")");
      }
   }

   public <E extends Entity, T> void assertEntityData(BlockPos blockpos, EntityType<E> entitytype, Function<? super E, T> function, @Nullable T object) {
      BlockPos blockpos1 = this.absolutePos(blockpos);
      List<E> list = this.getLevel().getEntities(entitytype, new AABB(blockpos1), Entity::isAlive);
      if (list.isEmpty()) {
         throw new GameTestAssertPosException("Expected " + entitytype.toShortString(), blockpos1, blockpos, this.testInfo.getTick());
      } else {
         for(E entity : list) {
            T object1 = function.apply(entity);
            if (object1 == null) {
               if (object != null) {
                  throw new GameTestAssertException("Expected entity data to be: " + object + ", but was: " + object1);
               }
            } else if (!object1.equals(object)) {
               throw new GameTestAssertException("Expected entity data to be: " + object + ", but was: " + object1);
            }
         }

      }
   }

   public <E extends LivingEntity> void assertEntityIsHolding(BlockPos blockpos, EntityType<E> entitytype, Item item) {
      BlockPos blockpos1 = this.absolutePos(blockpos);
      List<E> list = this.getLevel().getEntities(entitytype, new AABB(blockpos1), Entity::isAlive);
      if (list.isEmpty()) {
         throw new GameTestAssertPosException("Expected entity of type: " + entitytype, blockpos1, blockpos, this.getTick());
      } else {
         for(E livingentity : list) {
            if (livingentity.isHolding(item)) {
               return;
            }
         }

         throw new GameTestAssertPosException("Entity should be holding: " + item, blockpos1, blockpos, this.getTick());
      }
   }

   public <E extends Entity & InventoryCarrier> void assertEntityInventoryContains(BlockPos blockpos, EntityType<E> entitytype, Item item) {
      BlockPos blockpos1 = this.absolutePos(blockpos);
      List<E> list = this.getLevel().getEntities(entitytype, new AABB(blockpos1), (object) -> object.isAlive());
      if (list.isEmpty()) {
         throw new GameTestAssertPosException("Expected " + entitytype.toShortString() + " to exist", blockpos1, blockpos, this.getTick());
      } else {
         for(E entity : list) {
            if (entity.getInventory().hasAnyMatching((itemstack) -> itemstack.is(item))) {
               return;
            }
         }

         throw new GameTestAssertPosException("Entity inventory should contain: " + item, blockpos1, blockpos, this.getTick());
      }
   }

   public void assertContainerEmpty(BlockPos blockpos) {
      BlockPos blockpos1 = this.absolutePos(blockpos);
      BlockEntity blockentity = this.getLevel().getBlockEntity(blockpos1);
      if (blockentity instanceof BaseContainerBlockEntity && !((BaseContainerBlockEntity)blockentity).isEmpty()) {
         throw new GameTestAssertException("Container should be empty");
      }
   }

   public void assertContainerContains(BlockPos blockpos, Item item) {
      BlockPos blockpos1 = this.absolutePos(blockpos);
      BlockEntity blockentity = this.getLevel().getBlockEntity(blockpos1);
      if (!(blockentity instanceof BaseContainerBlockEntity)) {
         throw new GameTestAssertException("Expected a container at " + blockpos + ", found " + BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockentity.getType()));
      } else if (((BaseContainerBlockEntity)blockentity).countItem(item) != 1) {
         throw new GameTestAssertException("Container should contain: " + item);
      }
   }

   public void assertSameBlockStates(BoundingBox boundingbox, BlockPos blockpos) {
      BlockPos.betweenClosedStream(boundingbox).forEach((blockpos2) -> {
         BlockPos blockpos3 = blockpos.offset(blockpos2.getX() - boundingbox.minX(), blockpos2.getY() - boundingbox.minY(), blockpos2.getZ() - boundingbox.minZ());
         this.assertSameBlockState(blockpos2, blockpos3);
      });
   }

   public void assertSameBlockState(BlockPos blockpos, BlockPos blockpos1) {
      BlockState blockstate = this.getBlockState(blockpos);
      BlockState blockstate1 = this.getBlockState(blockpos1);
      if (blockstate != blockstate1) {
         this.fail("Incorrect state. Expected " + blockstate1 + ", got " + blockstate, blockpos);
      }

   }

   public void assertAtTickTimeContainerContains(long i, BlockPos blockpos, Item item) {
      this.runAtTickTime(i, () -> this.assertContainerContains(blockpos, item));
   }

   public void assertAtTickTimeContainerEmpty(long i, BlockPos blockpos) {
      this.runAtTickTime(i, () -> this.assertContainerEmpty(blockpos));
   }

   public <E extends Entity, T> void succeedWhenEntityData(BlockPos blockpos, EntityType<E> entitytype, Function<E, T> function, T object) {
      this.succeedWhen(() -> this.assertEntityData(blockpos, entitytype, function, object));
   }

   public <E extends Entity> void assertEntityProperty(E entity, Predicate<E> predicate, String s) {
      if (!predicate.test(entity)) {
         throw new GameTestAssertException("Entity " + entity + " failed " + s + " test");
      }
   }

   public <E extends Entity, T> void assertEntityProperty(E entity, Function<E, T> function, String s, T object) {
      T object1 = function.apply(entity);
      if (!object1.equals(object)) {
         throw new GameTestAssertException("Entity " + entity + " value " + s + "=" + object1 + " is not equal to expected " + object);
      }
   }

   public void succeedWhenEntityPresent(EntityType<?> entitytype, int i, int j, int k) {
      this.succeedWhenEntityPresent(entitytype, new BlockPos(i, j, k));
   }

   public void succeedWhenEntityPresent(EntityType<?> entitytype, BlockPos blockpos) {
      this.succeedWhen(() -> this.assertEntityPresent(entitytype, blockpos));
   }

   public void succeedWhenEntityNotPresent(EntityType<?> entitytype, int i, int j, int k) {
      this.succeedWhenEntityNotPresent(entitytype, new BlockPos(i, j, k));
   }

   public void succeedWhenEntityNotPresent(EntityType<?> entitytype, BlockPos blockpos) {
      this.succeedWhen(() -> this.assertEntityNotPresent(entitytype, blockpos));
   }

   public void succeed() {
      this.testInfo.succeed();
   }

   private void ensureSingleFinalCheck() {
      if (this.finalCheckAdded) {
         throw new IllegalStateException("This test already has final clause");
      } else {
         this.finalCheckAdded = true;
      }
   }

   public void succeedIf(Runnable runnable) {
      this.ensureSingleFinalCheck();
      this.testInfo.createSequence().thenWaitUntil(0L, runnable).thenSucceed();
   }

   public void succeedWhen(Runnable runnable) {
      this.ensureSingleFinalCheck();
      this.testInfo.createSequence().thenWaitUntil(runnable).thenSucceed();
   }

   public void succeedOnTickWhen(int i, Runnable runnable) {
      this.ensureSingleFinalCheck();
      this.testInfo.createSequence().thenWaitUntil((long)i, runnable).thenSucceed();
   }

   public void runAtTickTime(long i, Runnable runnable) {
      this.testInfo.setRunAtTickTime(i, runnable);
   }

   public void runAfterDelay(long i, Runnable runnable) {
      this.runAtTickTime(this.testInfo.getTick() + i, runnable);
   }

   public void randomTick(BlockPos blockpos) {
      BlockPos blockpos1 = this.absolutePos(blockpos);
      ServerLevel serverlevel = this.getLevel();
      serverlevel.getBlockState(blockpos1).randomTick(serverlevel, blockpos1, serverlevel.random);
   }

   public int getHeight(Heightmap.Types heightmap_types, int i, int j) {
      BlockPos blockpos = this.absolutePos(new BlockPos(i, 0, j));
      return this.relativePos(this.getLevel().getHeightmapPos(heightmap_types, blockpos)).getY();
   }

   public void fail(String s, BlockPos blockpos) {
      throw new GameTestAssertPosException(s, this.absolutePos(blockpos), blockpos, this.getTick());
   }

   public void fail(String s, Entity entity) {
      throw new GameTestAssertPosException(s, entity.blockPosition(), this.relativePos(entity.blockPosition()), this.getTick());
   }

   public void fail(String s) {
      throw new GameTestAssertException(s);
   }

   public void failIf(Runnable runnable) {
      this.testInfo.createSequence().thenWaitUntil(runnable).thenFail(() -> new GameTestAssertException("Fail conditions met"));
   }

   public void failIfEver(Runnable runnable) {
      LongStream.range(this.testInfo.getTick(), (long)this.testInfo.getTimeoutTicks()).forEach((i) -> this.testInfo.setRunAtTickTime(i, runnable::run));
   }

   public GameTestSequence startSequence() {
      return this.testInfo.createSequence();
   }

   public BlockPos absolutePos(BlockPos blockpos) {
      BlockPos blockpos1 = this.testInfo.getStructureBlockPos();
      BlockPos blockpos2 = blockpos1.offset(blockpos);
      return StructureTemplate.transform(blockpos2, Mirror.NONE, this.testInfo.getRotation(), blockpos1);
   }

   public BlockPos relativePos(BlockPos blockpos) {
      BlockPos blockpos1 = this.testInfo.getStructureBlockPos();
      Rotation rotation = this.testInfo.getRotation().getRotated(Rotation.CLOCKWISE_180);
      BlockPos blockpos2 = StructureTemplate.transform(blockpos, Mirror.NONE, rotation, blockpos1);
      return blockpos2.subtract(blockpos1);
   }

   public Vec3 absoluteVec(Vec3 vec3) {
      Vec3 vec31 = Vec3.atLowerCornerOf(this.testInfo.getStructureBlockPos());
      return StructureTemplate.transform(vec31.add(vec3), Mirror.NONE, this.testInfo.getRotation(), this.testInfo.getStructureBlockPos());
   }

   public Vec3 relativeVec(Vec3 vec3) {
      Vec3 vec31 = Vec3.atLowerCornerOf(this.testInfo.getStructureBlockPos());
      return StructureTemplate.transform(vec3.subtract(vec31), Mirror.NONE, this.testInfo.getRotation(), this.testInfo.getStructureBlockPos());
   }

   public void assertTrue(boolean flag, String s) {
      if (!flag) {
         throw new GameTestAssertException(s);
      }
   }

   public void assertFalse(boolean flag, String s) {
      if (flag) {
         throw new GameTestAssertException(s);
      }
   }

   public long getTick() {
      return this.testInfo.getTick();
   }

   private AABB getBounds() {
      return this.testInfo.getStructureBounds();
   }

   private AABB getRelativeBounds() {
      AABB aabb = this.testInfo.getStructureBounds();
      return aabb.move(BlockPos.ZERO.subtract(this.absolutePos(BlockPos.ZERO)));
   }

   public void forEveryBlockInStructure(Consumer<BlockPos> consumer) {
      AABB aabb = this.getRelativeBounds();
      BlockPos.MutableBlockPos.betweenClosedStream(aabb.move(0.0D, 1.0D, 0.0D)).forEach(consumer);
   }

   public void onEachTick(Runnable runnable) {
      LongStream.range(this.testInfo.getTick(), (long)this.testInfo.getTimeoutTicks()).forEach((i) -> this.testInfo.setRunAtTickTime(i, runnable::run));
   }

   public void placeAt(Player player, ItemStack itemstack, BlockPos blockpos, Direction direction) {
      BlockPos blockpos1 = this.absolutePos(blockpos.relative(direction));
      BlockHitResult blockhitresult = new BlockHitResult(Vec3.atCenterOf(blockpos1), direction, blockpos1, false);
      UseOnContext useoncontext = new UseOnContext(player, InteractionHand.MAIN_HAND, blockhitresult);
      itemstack.useOn(useoncontext);
   }
}
