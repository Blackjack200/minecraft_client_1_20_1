package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.raid.Raids;
import net.minecraft.world.phys.Vec3;

public class RaidCommand {
   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("raid").requires((commandsourcestack) -> commandsourcestack.hasPermission(3)).then(Commands.literal("start").then(Commands.argument("omenlvl", IntegerArgumentType.integer(0)).executes((commandcontext6) -> start(commandcontext6.getSource(), IntegerArgumentType.getInteger(commandcontext6, "omenlvl"))))).then(Commands.literal("stop").executes((commandcontext5) -> stop(commandcontext5.getSource()))).then(Commands.literal("check").executes((commandcontext4) -> check(commandcontext4.getSource()))).then(Commands.literal("sound").then(Commands.argument("type", ComponentArgument.textComponent()).executes((commandcontext3) -> playSound(commandcontext3.getSource(), ComponentArgument.getComponent(commandcontext3, "type"))))).then(Commands.literal("spawnleader").executes((commandcontext2) -> spawnLeader(commandcontext2.getSource()))).then(Commands.literal("setomen").then(Commands.argument("level", IntegerArgumentType.integer(0)).executes((commandcontext1) -> setBadOmenLevel(commandcontext1.getSource(), IntegerArgumentType.getInteger(commandcontext1, "level"))))).then(Commands.literal("glow").executes((commandcontext) -> glow(commandcontext.getSource()))));
   }

   private static int glow(CommandSourceStack commandsourcestack) throws CommandSyntaxException {
      Raid raid = getRaid(commandsourcestack.getPlayerOrException());
      if (raid != null) {
         for(Raider raider : raid.getAllRaiders()) {
            raider.addEffect(new MobEffectInstance(MobEffects.GLOWING, 1000, 1));
         }
      }

      return 1;
   }

   private static int setBadOmenLevel(CommandSourceStack commandsourcestack, int i) throws CommandSyntaxException {
      Raid raid = getRaid(commandsourcestack.getPlayerOrException());
      if (raid != null) {
         int j = raid.getMaxBadOmenLevel();
         if (i > j) {
            commandsourcestack.sendFailure(Component.literal("Sorry, the max bad omen level you can set is " + j));
         } else {
            int k = raid.getBadOmenLevel();
            raid.setBadOmenLevel(i);
            commandsourcestack.sendSuccess(() -> Component.literal("Changed village's bad omen level from " + k + " to " + i), false);
         }
      } else {
         commandsourcestack.sendFailure(Component.literal("No raid found here"));
      }

      return 1;
   }

   private static int spawnLeader(CommandSourceStack commandsourcestack) {
      commandsourcestack.sendSuccess(() -> Component.literal("Spawned a raid captain"), false);
      Raider raider = EntityType.PILLAGER.create(commandsourcestack.getLevel());
      if (raider == null) {
         commandsourcestack.sendFailure(Component.literal("Pillager failed to spawn"));
         return 0;
      } else {
         raider.setPatrolLeader(true);
         raider.setItemSlot(EquipmentSlot.HEAD, Raid.getLeaderBannerInstance());
         raider.setPos(commandsourcestack.getPosition().x, commandsourcestack.getPosition().y, commandsourcestack.getPosition().z);
         raider.finalizeSpawn(commandsourcestack.getLevel(), commandsourcestack.getLevel().getCurrentDifficultyAt(BlockPos.containing(commandsourcestack.getPosition())), MobSpawnType.COMMAND, (SpawnGroupData)null, (CompoundTag)null);
         commandsourcestack.getLevel().addFreshEntityWithPassengers(raider);
         return 1;
      }
   }

   private static int playSound(CommandSourceStack commandsourcestack, @Nullable Component component) {
      if (component != null && component.getString().equals("local")) {
         ServerLevel serverlevel = commandsourcestack.getLevel();
         Vec3 vec3 = commandsourcestack.getPosition().add(5.0D, 0.0D, 0.0D);
         serverlevel.playSeededSound((Player)null, vec3.x, vec3.y, vec3.z, SoundEvents.RAID_HORN, SoundSource.NEUTRAL, 2.0F, 1.0F, serverlevel.random.nextLong());
      }

      return 1;
   }

   private static int start(CommandSourceStack commandsourcestack, int i) throws CommandSyntaxException {
      ServerPlayer serverplayer = commandsourcestack.getPlayerOrException();
      BlockPos blockpos = serverplayer.blockPosition();
      if (serverplayer.serverLevel().isRaided(blockpos)) {
         commandsourcestack.sendFailure(Component.literal("Raid already started close by"));
         return -1;
      } else {
         Raids raids = serverplayer.serverLevel().getRaids();
         Raid raid = raids.createOrExtendRaid(serverplayer);
         if (raid != null) {
            raid.setBadOmenLevel(i);
            raids.setDirty();
            commandsourcestack.sendSuccess(() -> Component.literal("Created a raid in your local village"), false);
         } else {
            commandsourcestack.sendFailure(Component.literal("Failed to create a raid in your local village"));
         }

         return 1;
      }
   }

   private static int stop(CommandSourceStack commandsourcestack) throws CommandSyntaxException {
      ServerPlayer serverplayer = commandsourcestack.getPlayerOrException();
      BlockPos blockpos = serverplayer.blockPosition();
      Raid raid = serverplayer.serverLevel().getRaidAt(blockpos);
      if (raid != null) {
         raid.stop();
         commandsourcestack.sendSuccess(() -> Component.literal("Stopped raid"), false);
         return 1;
      } else {
         commandsourcestack.sendFailure(Component.literal("No raid here"));
         return -1;
      }
   }

   private static int check(CommandSourceStack commandsourcestack) throws CommandSyntaxException {
      Raid raid = getRaid(commandsourcestack.getPlayerOrException());
      if (raid != null) {
         StringBuilder stringbuilder = new StringBuilder();
         stringbuilder.append("Found a started raid! ");
         commandsourcestack.sendSuccess(() -> Component.literal(stringbuilder.toString()), false);
         StringBuilder stringbuilder1 = new StringBuilder();
         stringbuilder1.append("Num groups spawned: ");
         stringbuilder1.append(raid.getGroupsSpawned());
         stringbuilder1.append(" Bad omen level: ");
         stringbuilder1.append(raid.getBadOmenLevel());
         stringbuilder1.append(" Num mobs: ");
         stringbuilder1.append(raid.getTotalRaidersAlive());
         stringbuilder1.append(" Raid health: ");
         stringbuilder1.append(raid.getHealthOfLivingRaiders());
         stringbuilder1.append(" / ");
         stringbuilder1.append(raid.getTotalHealth());
         commandsourcestack.sendSuccess(() -> Component.literal(stringbuilder1.toString()), false);
         return 1;
      } else {
         commandsourcestack.sendFailure(Component.literal("Found no started raids"));
         return 0;
      }
   }

   @Nullable
   private static Raid getRaid(ServerPlayer serverplayer) {
      return serverplayer.serverLevel().getRaidAt(serverplayer.blockPosition());
   }
}
