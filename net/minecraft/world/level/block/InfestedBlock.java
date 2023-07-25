package net.minecraft.world.level.block;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class InfestedBlock extends Block {
   private final Block hostBlock;
   private static final Map<Block, Block> BLOCK_BY_HOST_BLOCK = Maps.newIdentityHashMap();
   private static final Map<BlockState, BlockState> HOST_TO_INFESTED_STATES = Maps.newIdentityHashMap();
   private static final Map<BlockState, BlockState> INFESTED_TO_HOST_STATES = Maps.newIdentityHashMap();

   public InfestedBlock(Block block, BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties.destroyTime(block.defaultDestroyTime() / 2.0F).explosionResistance(0.75F));
      this.hostBlock = block;
      BLOCK_BY_HOST_BLOCK.put(block, this);
   }

   public Block getHostBlock() {
      return this.hostBlock;
   }

   public static boolean isCompatibleHostBlock(BlockState blockstate) {
      return BLOCK_BY_HOST_BLOCK.containsKey(blockstate.getBlock());
   }

   private void spawnInfestation(ServerLevel serverlevel, BlockPos blockpos) {
      Silverfish silverfish = EntityType.SILVERFISH.create(serverlevel);
      if (silverfish != null) {
         silverfish.moveTo((double)blockpos.getX() + 0.5D, (double)blockpos.getY(), (double)blockpos.getZ() + 0.5D, 0.0F, 0.0F);
         serverlevel.addFreshEntity(silverfish);
         silverfish.spawnAnim();
      }

   }

   public void spawnAfterBreak(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, ItemStack itemstack, boolean flag) {
      super.spawnAfterBreak(blockstate, serverlevel, blockpos, itemstack, flag);
      if (serverlevel.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS) && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, itemstack) == 0) {
         this.spawnInfestation(serverlevel, blockpos);
      }

   }

   public static BlockState infestedStateByHost(BlockState blockstate) {
      return getNewStateWithProperties(HOST_TO_INFESTED_STATES, blockstate, () -> BLOCK_BY_HOST_BLOCK.get(blockstate.getBlock()).defaultBlockState());
   }

   public BlockState hostStateByInfested(BlockState blockstate) {
      return getNewStateWithProperties(INFESTED_TO_HOST_STATES, blockstate, () -> this.getHostBlock().defaultBlockState());
   }

   private static BlockState getNewStateWithProperties(Map<BlockState, BlockState> map, BlockState blockstate, Supplier<BlockState> supplier) {
      return map.computeIfAbsent(blockstate, (blockstate1) -> {
         BlockState blockstate2 = supplier.get();

         for(Property property : blockstate1.getProperties()) {
            blockstate2 = blockstate2.hasProperty(property) ? blockstate2.setValue(property, blockstate1.getValue(property)) : blockstate2;
         }

         return blockstate2;
      });
   }
}
