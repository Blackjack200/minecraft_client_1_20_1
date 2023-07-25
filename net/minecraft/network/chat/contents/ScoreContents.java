package net.minecraft.network.chat.contents;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;

public class ScoreContents implements ComponentContents {
   private static final String SCORER_PLACEHOLDER = "*";
   private final String name;
   @Nullable
   private final EntitySelector selector;
   private final String objective;

   @Nullable
   private static EntitySelector parseSelector(String s) {
      try {
         return (new EntitySelectorParser(new StringReader(s))).parse();
      } catch (CommandSyntaxException var2) {
         return null;
      }
   }

   public ScoreContents(String s, String s1) {
      this.name = s;
      this.selector = parseSelector(s);
      this.objective = s1;
   }

   public String getName() {
      return this.name;
   }

   @Nullable
   public EntitySelector getSelector() {
      return this.selector;
   }

   public String getObjective() {
      return this.objective;
   }

   private String findTargetName(CommandSourceStack commandsourcestack) throws CommandSyntaxException {
      if (this.selector != null) {
         List<? extends Entity> list = this.selector.findEntities(commandsourcestack);
         if (!list.isEmpty()) {
            if (list.size() != 1) {
               throw EntityArgument.ERROR_NOT_SINGLE_ENTITY.create();
            }

            return list.get(0).getScoreboardName();
         }
      }

      return this.name;
   }

   private String getScore(String s, CommandSourceStack commandsourcestack) {
      MinecraftServer minecraftserver = commandsourcestack.getServer();
      if (minecraftserver != null) {
         Scoreboard scoreboard = minecraftserver.getScoreboard();
         Objective objective = scoreboard.getObjective(this.objective);
         if (scoreboard.hasPlayerScore(s, objective)) {
            Score score = scoreboard.getOrCreatePlayerScore(s, objective);
            return Integer.toString(score.getScore());
         }
      }

      return "";
   }

   public MutableComponent resolve(@Nullable CommandSourceStack commandsourcestack, @Nullable Entity entity, int i) throws CommandSyntaxException {
      if (commandsourcestack == null) {
         return Component.empty();
      } else {
         String s = this.findTargetName(commandsourcestack);
         String s1 = entity != null && s.equals("*") ? entity.getScoreboardName() : s;
         return Component.literal(this.getScore(s1, commandsourcestack));
      }
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else {
         if (object instanceof ScoreContents) {
            ScoreContents scorecontents = (ScoreContents)object;
            if (this.name.equals(scorecontents.name) && this.objective.equals(scorecontents.objective)) {
               return true;
            }
         }

         return false;
      }
   }

   public int hashCode() {
      int i = this.name.hashCode();
      return 31 * i + this.objective.hashCode();
   }

   public String toString() {
      return "score{name='" + this.name + "', objective='" + this.objective + "'}";
   }
}
