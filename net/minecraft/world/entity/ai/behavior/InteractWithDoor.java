package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.OptionalBox;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;

public class InteractWithDoor {
   private static final int COOLDOWN_BEFORE_RERUNNING_IN_SAME_NODE = 20;
   private static final double SKIP_CLOSING_DOOR_IF_FURTHER_AWAY_THAN = 3.0D;
   private static final double MAX_DISTANCE_TO_HOLD_DOOR_OPEN_FOR_OTHER_MOBS = 2.0D;

   public static BehaviorControl<LivingEntity> create() {
      MutableObject<Node> mutableobject = new MutableObject<>((Node)null);
      MutableInt mutableint = new MutableInt(0);
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.present(MemoryModuleType.PATH), behaviorbuilder_instance.registered(MemoryModuleType.DOORS_TO_CLOSE), behaviorbuilder_instance.registered(MemoryModuleType.NEAREST_LIVING_ENTITIES)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1, memoryaccessor2) -> (serverlevel, livingentity, i) -> {
               Path path = behaviorbuilder_instance.get(memoryaccessor);
               Optional<Set<GlobalPos>> optional = behaviorbuilder_instance.tryGet(memoryaccessor1);
               if (!path.notStarted() && !path.isDone()) {
                  if (Objects.equals(mutableobject.getValue(), path.getNextNode())) {
                     mutableint.setValue(20);
                  } else if (mutableint.decrementAndGet() > 0) {
                     return false;
                  }

                  mutableobject.setValue(path.getNextNode());
                  Node node = path.getPreviousNode();
                  Node node1 = path.getNextNode();
                  BlockPos blockpos = node.asBlockPos();
                  BlockState blockstate = serverlevel.getBlockState(blockpos);
                  if (blockstate.is(BlockTags.WOODEN_DOORS, (blockbehaviour_blockstatebase1) -> blockbehaviour_blockstatebase1.getBlock() instanceof DoorBlock)) {
                     DoorBlock doorblock = (DoorBlock)blockstate.getBlock();
                     if (!doorblock.isOpen(blockstate)) {
                        doorblock.setOpen(livingentity, serverlevel, blockstate, blockpos, true);
                     }

                     optional = rememberDoorToClose(memoryaccessor1, optional, serverlevel, blockpos);
                  }

                  BlockPos blockpos1 = node1.asBlockPos();
                  BlockState blockstate1 = serverlevel.getBlockState(blockpos1);
                  if (blockstate1.is(BlockTags.WOODEN_DOORS, (blockbehaviour_blockstatebase) -> blockbehaviour_blockstatebase.getBlock() instanceof DoorBlock)) {
                     DoorBlock doorblock1 = (DoorBlock)blockstate1.getBlock();
                     if (!doorblock1.isOpen(blockstate1)) {
                        doorblock1.setOpen(livingentity, serverlevel, blockstate1, blockpos1, true);
                        optional = rememberDoorToClose(memoryaccessor1, optional, serverlevel, blockpos1);
                     }
                  }

                  optional.ifPresent((set) -> closeDoorsThatIHaveOpenedOrPassedThrough(serverlevel, livingentity, node, node1, set, behaviorbuilder_instance.tryGet(memoryaccessor2)));
                  return true;
               } else {
                  return false;
               }
            }));
   }

   public static void closeDoorsThatIHaveOpenedOrPassedThrough(ServerLevel serverlevel, LivingEntity livingentity, @Nullable Node node, @Nullable Node node1, Set<GlobalPos> set, Optional<List<LivingEntity>> optional) {
      Iterator<GlobalPos> iterator = set.iterator();

      while(iterator.hasNext()) {
         GlobalPos globalpos = iterator.next();
         BlockPos blockpos = globalpos.pos();
         if ((node == null || !node.asBlockPos().equals(blockpos)) && (node1 == null || !node1.asBlockPos().equals(blockpos))) {
            if (isDoorTooFarAway(serverlevel, livingentity, globalpos)) {
               iterator.remove();
            } else {
               BlockState blockstate = serverlevel.getBlockState(blockpos);
               if (!blockstate.is(BlockTags.WOODEN_DOORS, (blockbehaviour_blockstatebase) -> blockbehaviour_blockstatebase.getBlock() instanceof DoorBlock)) {
                  iterator.remove();
               } else {
                  DoorBlock doorblock = (DoorBlock)blockstate.getBlock();
                  if (!doorblock.isOpen(blockstate)) {
                     iterator.remove();
                  } else if (areOtherMobsComingThroughDoor(livingentity, blockpos, optional)) {
                     iterator.remove();
                  } else {
                     doorblock.setOpen(livingentity, serverlevel, blockstate, blockpos, false);
                     iterator.remove();
                  }
               }
            }
         }
      }

   }

   private static boolean areOtherMobsComingThroughDoor(LivingEntity livingentity, BlockPos blockpos, Optional<List<LivingEntity>> optional) {
      return optional.isEmpty() ? false : optional.get().stream().filter((livingentity4) -> livingentity4.getType() == livingentity.getType()).filter((livingentity2) -> blockpos.closerToCenterThan(livingentity2.position(), 2.0D)).anyMatch((livingentity1) -> isMobComingThroughDoor(livingentity1.getBrain(), blockpos));
   }

   private static boolean isMobComingThroughDoor(Brain<?> brain, BlockPos blockpos) {
      if (!brain.hasMemoryValue(MemoryModuleType.PATH)) {
         return false;
      } else {
         Path path = brain.getMemory(MemoryModuleType.PATH).get();
         if (path.isDone()) {
            return false;
         } else {
            Node node = path.getPreviousNode();
            if (node == null) {
               return false;
            } else {
               Node node1 = path.getNextNode();
               return blockpos.equals(node.asBlockPos()) || blockpos.equals(node1.asBlockPos());
            }
         }
      }
   }

   private static boolean isDoorTooFarAway(ServerLevel serverlevel, LivingEntity livingentity, GlobalPos globalpos) {
      return globalpos.dimension() != serverlevel.dimension() || !globalpos.pos().closerToCenterThan(livingentity.position(), 3.0D);
   }

   private static Optional<Set<GlobalPos>> rememberDoorToClose(MemoryAccessor<OptionalBox.Mu, Set<GlobalPos>> memoryaccessor, Optional<Set<GlobalPos>> optional, ServerLevel serverlevel, BlockPos blockpos) {
      GlobalPos globalpos = GlobalPos.of(serverlevel.dimension(), blockpos);
      return Optional.of(optional.map((set1) -> {
         set1.add(globalpos);
         return set1;
      }).orElseGet(() -> {
         Set<GlobalPos> set = Sets.newHashSet(globalpos);
         memoryaccessor.set(set);
         return set;
      }));
   }
}
