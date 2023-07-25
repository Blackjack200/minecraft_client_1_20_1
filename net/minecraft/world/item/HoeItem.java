package net.minecraft.world.item;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class HoeItem extends DiggerItem {
   protected static final Map<Block, Pair<Predicate<UseOnContext>, Consumer<UseOnContext>>> TILLABLES = Maps.newHashMap(ImmutableMap.of(Blocks.GRASS_BLOCK, Pair.of(HoeItem::onlyIfAirAbove, changeIntoState(Blocks.FARMLAND.defaultBlockState())), Blocks.DIRT_PATH, Pair.of(HoeItem::onlyIfAirAbove, changeIntoState(Blocks.FARMLAND.defaultBlockState())), Blocks.DIRT, Pair.of(HoeItem::onlyIfAirAbove, changeIntoState(Blocks.FARMLAND.defaultBlockState())), Blocks.COARSE_DIRT, Pair.of(HoeItem::onlyIfAirAbove, changeIntoState(Blocks.DIRT.defaultBlockState())), Blocks.ROOTED_DIRT, Pair.of((useoncontext) -> true, changeIntoStateAndDropItem(Blocks.DIRT.defaultBlockState(), Items.HANGING_ROOTS))));

   protected HoeItem(Tier tier, int i, float f, Item.Properties item_properties) {
      super((float)i, f, tier, BlockTags.MINEABLE_WITH_HOE, item_properties);
   }

   public InteractionResult useOn(UseOnContext useoncontext) {
      Level level = useoncontext.getLevel();
      BlockPos blockpos = useoncontext.getClickedPos();
      Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> pair = TILLABLES.get(level.getBlockState(blockpos).getBlock());
      if (pair == null) {
         return InteractionResult.PASS;
      } else {
         Predicate<UseOnContext> predicate = pair.getFirst();
         Consumer<UseOnContext> consumer = pair.getSecond();
         if (predicate.test(useoncontext)) {
            Player player = useoncontext.getPlayer();
            level.playSound(player, blockpos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!level.isClientSide) {
               consumer.accept(useoncontext);
               if (player != null) {
                  useoncontext.getItemInHand().hurtAndBreak(1, player, (player1) -> player1.broadcastBreakEvent(useoncontext.getHand()));
               }
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
         } else {
            return InteractionResult.PASS;
         }
      }
   }

   public static Consumer<UseOnContext> changeIntoState(BlockState blockstate) {
      return (useoncontext) -> {
         useoncontext.getLevel().setBlock(useoncontext.getClickedPos(), blockstate, 11);
         useoncontext.getLevel().gameEvent(GameEvent.BLOCK_CHANGE, useoncontext.getClickedPos(), GameEvent.Context.of(useoncontext.getPlayer(), blockstate));
      };
   }

   public static Consumer<UseOnContext> changeIntoStateAndDropItem(BlockState blockstate, ItemLike itemlike) {
      return (useoncontext) -> {
         useoncontext.getLevel().setBlock(useoncontext.getClickedPos(), blockstate, 11);
         useoncontext.getLevel().gameEvent(GameEvent.BLOCK_CHANGE, useoncontext.getClickedPos(), GameEvent.Context.of(useoncontext.getPlayer(), blockstate));
         Block.popResourceFromFace(useoncontext.getLevel(), useoncontext.getClickedPos(), useoncontext.getClickedFace(), new ItemStack(itemlike));
      };
   }

   public static boolean onlyIfAirAbove(UseOnContext useoncontext) {
      return useoncontext.getClickedFace() != Direction.DOWN && useoncontext.getLevel().getBlockState(useoncontext.getClickedPos().above()).isAir();
   }
}
