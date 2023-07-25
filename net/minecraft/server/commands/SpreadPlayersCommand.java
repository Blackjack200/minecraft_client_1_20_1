package net.minecraft.server.commands;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic4CommandExceptionType;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.scores.Team;

public class SpreadPlayersCommand {
   private static final int MAX_ITERATION_COUNT = 10000;
   private static final Dynamic4CommandExceptionType ERROR_FAILED_TO_SPREAD_TEAMS = new Dynamic4CommandExceptionType((object, object1, object2, object3) -> Component.translatable("commands.spreadplayers.failed.teams", object, object1, object2, object3));
   private static final Dynamic4CommandExceptionType ERROR_FAILED_TO_SPREAD_ENTITIES = new Dynamic4CommandExceptionType((object, object1, object2, object3) -> Component.translatable("commands.spreadplayers.failed.entities", object, object1, object2, object3));
   private static final Dynamic2CommandExceptionType ERROR_INVALID_MAX_HEIGHT = new Dynamic2CommandExceptionType((object, object1) -> Component.translatable("commands.spreadplayers.failed.invalid.height", object, object1));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("spreadplayers").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).then(Commands.argument("center", Vec2Argument.vec2()).then(Commands.argument("spreadDistance", FloatArgumentType.floatArg(0.0F)).then(Commands.argument("maxRange", FloatArgumentType.floatArg(1.0F)).then(Commands.argument("respectTeams", BoolArgumentType.bool()).then(Commands.argument("targets", EntityArgument.entities()).executes((commandcontext1) -> spreadPlayers(commandcontext1.getSource(), Vec2Argument.getVec2(commandcontext1, "center"), FloatArgumentType.getFloat(commandcontext1, "spreadDistance"), FloatArgumentType.getFloat(commandcontext1, "maxRange"), commandcontext1.getSource().getLevel().getMaxBuildHeight(), BoolArgumentType.getBool(commandcontext1, "respectTeams"), EntityArgument.getEntities(commandcontext1, "targets"))))).then(Commands.literal("under").then(Commands.argument("maxHeight", IntegerArgumentType.integer()).then(Commands.argument("respectTeams", BoolArgumentType.bool()).then(Commands.argument("targets", EntityArgument.entities()).executes((commandcontext) -> spreadPlayers(commandcontext.getSource(), Vec2Argument.getVec2(commandcontext, "center"), FloatArgumentType.getFloat(commandcontext, "spreadDistance"), FloatArgumentType.getFloat(commandcontext, "maxRange"), IntegerArgumentType.getInteger(commandcontext, "maxHeight"), BoolArgumentType.getBool(commandcontext, "respectTeams"), EntityArgument.getEntities(commandcontext, "targets")))))))))));
   }

   private static int spreadPlayers(CommandSourceStack commandsourcestack, Vec2 vec2, float f, float f1, int i, boolean flag, Collection<? extends Entity> collection) throws CommandSyntaxException {
      ServerLevel serverlevel = commandsourcestack.getLevel();
      int j = serverlevel.getMinBuildHeight();
      if (i < j) {
         throw ERROR_INVALID_MAX_HEIGHT.create(i, j);
      } else {
         RandomSource randomsource = RandomSource.create();
         double d0 = (double)(vec2.x - f1);
         double d1 = (double)(vec2.y - f1);
         double d2 = (double)(vec2.x + f1);
         double d3 = (double)(vec2.y + f1);
         SpreadPlayersCommand.Position[] aspreadplayerscommand_position = createInitialPositions(randomsource, flag ? getNumberOfTeams(collection) : collection.size(), d0, d1, d2, d3);
         spreadPositions(vec2, (double)f, serverlevel, randomsource, d0, d1, d2, d3, i, aspreadplayerscommand_position, flag);
         double d4 = setPlayerPositions(collection, serverlevel, aspreadplayerscommand_position, i, flag);
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.spreadplayers.success." + (flag ? "teams" : "entities"), aspreadplayerscommand_position.length, vec2.x, vec2.y, String.format(Locale.ROOT, "%.2f", d4)), true);
         return aspreadplayerscommand_position.length;
      }
   }

   private static int getNumberOfTeams(Collection<? extends Entity> collection) {
      Set<Team> set = Sets.newHashSet();

      for(Entity entity : collection) {
         if (entity instanceof Player) {
            set.add(entity.getTeam());
         } else {
            set.add((Team)null);
         }
      }

      return set.size();
   }

   private static void spreadPositions(Vec2 vec2, double d0, ServerLevel serverlevel, RandomSource randomsource, double d1, double d2, double d3, double d4, int i, SpreadPlayersCommand.Position[] aspreadplayerscommand_position, boolean flag) throws CommandSyntaxException {
      boolean flag1 = true;
      double d5 = (double)Float.MAX_VALUE;

      int j;
      for(j = 0; j < 10000 && flag1; ++j) {
         flag1 = false;
         d5 = (double)Float.MAX_VALUE;

         for(int k = 0; k < aspreadplayerscommand_position.length; ++k) {
            SpreadPlayersCommand.Position spreadplayerscommand_position = aspreadplayerscommand_position[k];
            int l = 0;
            SpreadPlayersCommand.Position spreadplayerscommand_position1 = new SpreadPlayersCommand.Position();

            for(int i1 = 0; i1 < aspreadplayerscommand_position.length; ++i1) {
               if (k != i1) {
                  SpreadPlayersCommand.Position spreadplayerscommand_position2 = aspreadplayerscommand_position[i1];
                  double d6 = spreadplayerscommand_position.dist(spreadplayerscommand_position2);
                  d5 = Math.min(d6, d5);
                  if (d6 < d0) {
                     ++l;
                     spreadplayerscommand_position1.x += spreadplayerscommand_position2.x - spreadplayerscommand_position.x;
                     spreadplayerscommand_position1.z += spreadplayerscommand_position2.z - spreadplayerscommand_position.z;
                  }
               }
            }

            if (l > 0) {
               spreadplayerscommand_position1.x /= (double)l;
               spreadplayerscommand_position1.z /= (double)l;
               double d7 = spreadplayerscommand_position1.getLength();
               if (d7 > 0.0D) {
                  spreadplayerscommand_position1.normalize();
                  spreadplayerscommand_position.moveAway(spreadplayerscommand_position1);
               } else {
                  spreadplayerscommand_position.randomize(randomsource, d1, d2, d3, d4);
               }

               flag1 = true;
            }

            if (spreadplayerscommand_position.clamp(d1, d2, d3, d4)) {
               flag1 = true;
            }
         }

         if (!flag1) {
            for(SpreadPlayersCommand.Position spreadplayerscommand_position3 : aspreadplayerscommand_position) {
               if (!spreadplayerscommand_position3.isSafe(serverlevel, i)) {
                  spreadplayerscommand_position3.randomize(randomsource, d1, d2, d3, d4);
                  flag1 = true;
               }
            }
         }
      }

      if (d5 == (double)Float.MAX_VALUE) {
         d5 = 0.0D;
      }

      if (j >= 10000) {
         if (flag) {
            throw ERROR_FAILED_TO_SPREAD_TEAMS.create(aspreadplayerscommand_position.length, vec2.x, vec2.y, String.format(Locale.ROOT, "%.2f", d5));
         } else {
            throw ERROR_FAILED_TO_SPREAD_ENTITIES.create(aspreadplayerscommand_position.length, vec2.x, vec2.y, String.format(Locale.ROOT, "%.2f", d5));
         }
      }
   }

   private static double setPlayerPositions(Collection<? extends Entity> collection, ServerLevel serverlevel, SpreadPlayersCommand.Position[] aspreadplayerscommand_position, int i, boolean flag) {
      double d0 = 0.0D;
      int j = 0;
      Map<Team, SpreadPlayersCommand.Position> map = Maps.newHashMap();

      for(Entity entity : collection) {
         SpreadPlayersCommand.Position spreadplayerscommand_position;
         if (flag) {
            Team team = entity instanceof Player ? entity.getTeam() : null;
            if (!map.containsKey(team)) {
               map.put(team, aspreadplayerscommand_position[j++]);
            }

            spreadplayerscommand_position = map.get(team);
         } else {
            spreadplayerscommand_position = aspreadplayerscommand_position[j++];
         }

         entity.teleportTo(serverlevel, (double)Mth.floor(spreadplayerscommand_position.x) + 0.5D, (double)spreadplayerscommand_position.getSpawnY(serverlevel, i), (double)Mth.floor(spreadplayerscommand_position.z) + 0.5D, Set.of(), entity.getYRot(), entity.getXRot());
         double d1 = Double.MAX_VALUE;

         for(SpreadPlayersCommand.Position spreadplayerscommand_position2 : aspreadplayerscommand_position) {
            if (spreadplayerscommand_position != spreadplayerscommand_position2) {
               double d2 = spreadplayerscommand_position.dist(spreadplayerscommand_position2);
               d1 = Math.min(d2, d1);
            }
         }

         d0 += d1;
      }

      return collection.size() < 2 ? 0.0D : d0 / (double)collection.size();
   }

   private static SpreadPlayersCommand.Position[] createInitialPositions(RandomSource randomsource, int i, double d0, double d1, double d2, double d3) {
      SpreadPlayersCommand.Position[] aspreadplayerscommand_position = new SpreadPlayersCommand.Position[i];

      for(int j = 0; j < aspreadplayerscommand_position.length; ++j) {
         SpreadPlayersCommand.Position spreadplayerscommand_position = new SpreadPlayersCommand.Position();
         spreadplayerscommand_position.randomize(randomsource, d0, d1, d2, d3);
         aspreadplayerscommand_position[j] = spreadplayerscommand_position;
      }

      return aspreadplayerscommand_position;
   }

   static class Position {
      double x;
      double z;

      double dist(SpreadPlayersCommand.Position spreadplayerscommand_position) {
         double d0 = this.x - spreadplayerscommand_position.x;
         double d1 = this.z - spreadplayerscommand_position.z;
         return Math.sqrt(d0 * d0 + d1 * d1);
      }

      void normalize() {
         double d0 = this.getLength();
         this.x /= d0;
         this.z /= d0;
      }

      double getLength() {
         return Math.sqrt(this.x * this.x + this.z * this.z);
      }

      public void moveAway(SpreadPlayersCommand.Position spreadplayerscommand_position) {
         this.x -= spreadplayerscommand_position.x;
         this.z -= spreadplayerscommand_position.z;
      }

      public boolean clamp(double d0, double d1, double d2, double d3) {
         boolean flag = false;
         if (this.x < d0) {
            this.x = d0;
            flag = true;
         } else if (this.x > d2) {
            this.x = d2;
            flag = true;
         }

         if (this.z < d1) {
            this.z = d1;
            flag = true;
         } else if (this.z > d3) {
            this.z = d3;
            flag = true;
         }

         return flag;
      }

      public int getSpawnY(BlockGetter blockgetter, int i) {
         BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos(this.x, (double)(i + 1), this.z);
         boolean flag = blockgetter.getBlockState(blockpos_mutableblockpos).isAir();
         blockpos_mutableblockpos.move(Direction.DOWN);

         boolean flag2;
         for(boolean flag1 = blockgetter.getBlockState(blockpos_mutableblockpos).isAir(); blockpos_mutableblockpos.getY() > blockgetter.getMinBuildHeight(); flag1 = flag2) {
            blockpos_mutableblockpos.move(Direction.DOWN);
            flag2 = blockgetter.getBlockState(blockpos_mutableblockpos).isAir();
            if (!flag2 && flag1 && flag) {
               return blockpos_mutableblockpos.getY() + 1;
            }

            flag = flag1;
         }

         return i + 1;
      }

      public boolean isSafe(BlockGetter blockgetter, int i) {
         BlockPos blockpos = BlockPos.containing(this.x, (double)(this.getSpawnY(blockgetter, i) - 1), this.z);
         BlockState blockstate = blockgetter.getBlockState(blockpos);
         return blockpos.getY() < i && !blockstate.liquid() && !blockstate.is(BlockTags.FIRE);
      }

      public void randomize(RandomSource randomsource, double d0, double d1, double d2, double d3) {
         this.x = Mth.nextDouble(randomsource, d0, d2);
         this.z = Mth.nextDouble(randomsource, d1, d3);
      }
   }
}
