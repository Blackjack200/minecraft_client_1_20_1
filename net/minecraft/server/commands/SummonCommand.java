package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SummonCommand {
   private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.summon.failed"));
   private static final SimpleCommandExceptionType ERROR_DUPLICATE_UUID = new SimpleCommandExceptionType(Component.translatable("commands.summon.failed.uuid"));
   private static final SimpleCommandExceptionType INVALID_POSITION = new SimpleCommandExceptionType(Component.translatable("commands.summon.invalidPosition"));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher, CommandBuildContext commandbuildcontext) {
      commanddispatcher.register(Commands.literal("summon").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).then(Commands.argument("entity", ResourceArgument.resource(commandbuildcontext, Registries.ENTITY_TYPE)).suggests(SuggestionProviders.SUMMONABLE_ENTITIES).executes((commandcontext2) -> spawnEntity(commandcontext2.getSource(), ResourceArgument.getSummonableEntityType(commandcontext2, "entity"), commandcontext2.getSource().getPosition(), new CompoundTag(), true)).then(Commands.argument("pos", Vec3Argument.vec3()).executes((commandcontext1) -> spawnEntity(commandcontext1.getSource(), ResourceArgument.getSummonableEntityType(commandcontext1, "entity"), Vec3Argument.getVec3(commandcontext1, "pos"), new CompoundTag(), true)).then(Commands.argument("nbt", CompoundTagArgument.compoundTag()).executes((commandcontext) -> spawnEntity(commandcontext.getSource(), ResourceArgument.getSummonableEntityType(commandcontext, "entity"), Vec3Argument.getVec3(commandcontext, "pos"), CompoundTagArgument.getCompoundTag(commandcontext, "nbt"), false))))));
   }

   public static Entity createEntity(CommandSourceStack commandsourcestack, Holder.Reference<EntityType<?>> holder_reference, Vec3 vec3, CompoundTag compoundtag, boolean flag) throws CommandSyntaxException {
      BlockPos blockpos = BlockPos.containing(vec3);
      if (!Level.isInSpawnableBounds(blockpos)) {
         throw INVALID_POSITION.create();
      } else {
         CompoundTag compoundtag1 = compoundtag.copy();
         compoundtag1.putString("id", holder_reference.key().location().toString());
         ServerLevel serverlevel = commandsourcestack.getLevel();
         Entity entity = EntityType.loadEntityRecursive(compoundtag1, serverlevel, (entity1) -> {
            entity1.moveTo(vec3.x, vec3.y, vec3.z, entity1.getYRot(), entity1.getXRot());
            return entity1;
         });
         if (entity == null) {
            throw ERROR_FAILED.create();
         } else {
            if (flag && entity instanceof Mob) {
               ((Mob)entity).finalizeSpawn(commandsourcestack.getLevel(), commandsourcestack.getLevel().getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.COMMAND, (SpawnGroupData)null, (CompoundTag)null);
            }

            if (!serverlevel.tryAddFreshEntityWithPassengers(entity)) {
               throw ERROR_DUPLICATE_UUID.create();
            } else {
               return entity;
            }
         }
      }
   }

   private static int spawnEntity(CommandSourceStack commandsourcestack, Holder.Reference<EntityType<?>> holder_reference, Vec3 vec3, CompoundTag compoundtag, boolean flag) throws CommandSyntaxException {
      Entity entity = createEntity(commandsourcestack, holder_reference, vec3, compoundtag, flag);
      commandsourcestack.sendSuccess(() -> Component.translatable("commands.summon.success", entity.getDisplayName()), true);
      return 1;
   }
}
