package net.minecraft.world.scores;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public abstract class Team {
   public boolean isAlliedTo(@Nullable Team team) {
      if (team == null) {
         return false;
      } else {
         return this == team;
      }
   }

   public abstract String getName();

   public abstract MutableComponent getFormattedName(Component component);

   public abstract boolean canSeeFriendlyInvisibles();

   public abstract boolean isAllowFriendlyFire();

   public abstract Team.Visibility getNameTagVisibility();

   public abstract ChatFormatting getColor();

   public abstract Collection<String> getPlayers();

   public abstract Team.Visibility getDeathMessageVisibility();

   public abstract Team.CollisionRule getCollisionRule();

   public static enum CollisionRule {
      ALWAYS("always", 0),
      NEVER("never", 1),
      PUSH_OTHER_TEAMS("pushOtherTeams", 2),
      PUSH_OWN_TEAM("pushOwnTeam", 3);

      private static final Map<String, Team.CollisionRule> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap((team_collisionrule) -> team_collisionrule.name, (team_collisionrule) -> team_collisionrule));
      public final String name;
      public final int id;

      @Nullable
      public static Team.CollisionRule byName(String s) {
         return BY_NAME.get(s);
      }

      private CollisionRule(String s, int i) {
         this.name = s;
         this.id = i;
      }

      public Component getDisplayName() {
         return Component.translatable("team.collision." + this.name);
      }
   }

   public static enum Visibility {
      ALWAYS("always", 0),
      NEVER("never", 1),
      HIDE_FOR_OTHER_TEAMS("hideForOtherTeams", 2),
      HIDE_FOR_OWN_TEAM("hideForOwnTeam", 3);

      private static final Map<String, Team.Visibility> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap((team_visibility) -> team_visibility.name, (team_visibility) -> team_visibility));
      public final String name;
      public final int id;

      public static String[] getAllNames() {
         return BY_NAME.keySet().toArray(new String[0]);
      }

      @Nullable
      public static Team.Visibility byName(String s) {
         return BY_NAME.get(s);
      }

      private Visibility(String s, int i) {
         this.name = s;
         this.id = i;
      }

      public Component getDisplayName() {
         return Component.translatable("team.visibility." + this.name);
      }
   }
}
