package net.minecraft.world.scores;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class PlayerTeam extends Team {
   private static final int BIT_FRIENDLY_FIRE = 0;
   private static final int BIT_SEE_INVISIBLES = 1;
   private final Scoreboard scoreboard;
   private final String name;
   private final Set<String> players = Sets.newHashSet();
   private Component displayName;
   private Component playerPrefix = CommonComponents.EMPTY;
   private Component playerSuffix = CommonComponents.EMPTY;
   private boolean allowFriendlyFire = true;
   private boolean seeFriendlyInvisibles = true;
   private Team.Visibility nameTagVisibility = Team.Visibility.ALWAYS;
   private Team.Visibility deathMessageVisibility = Team.Visibility.ALWAYS;
   private ChatFormatting color = ChatFormatting.RESET;
   private Team.CollisionRule collisionRule = Team.CollisionRule.ALWAYS;
   private final Style displayNameStyle;

   public PlayerTeam(Scoreboard scoreboard, String s) {
      this.scoreboard = scoreboard;
      this.name = s;
      this.displayName = Component.literal(s);
      this.displayNameStyle = Style.EMPTY.withInsertion(s).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(s)));
   }

   public Scoreboard getScoreboard() {
      return this.scoreboard;
   }

   public String getName() {
      return this.name;
   }

   public Component getDisplayName() {
      return this.displayName;
   }

   public MutableComponent getFormattedDisplayName() {
      MutableComponent mutablecomponent = ComponentUtils.wrapInSquareBrackets(this.displayName.copy().withStyle(this.displayNameStyle));
      ChatFormatting chatformatting = this.getColor();
      if (chatformatting != ChatFormatting.RESET) {
         mutablecomponent.withStyle(chatformatting);
      }

      return mutablecomponent;
   }

   public void setDisplayName(Component component) {
      if (component == null) {
         throw new IllegalArgumentException("Name cannot be null");
      } else {
         this.displayName = component;
         this.scoreboard.onTeamChanged(this);
      }
   }

   public void setPlayerPrefix(@Nullable Component component) {
      this.playerPrefix = component == null ? CommonComponents.EMPTY : component;
      this.scoreboard.onTeamChanged(this);
   }

   public Component getPlayerPrefix() {
      return this.playerPrefix;
   }

   public void setPlayerSuffix(@Nullable Component component) {
      this.playerSuffix = component == null ? CommonComponents.EMPTY : component;
      this.scoreboard.onTeamChanged(this);
   }

   public Component getPlayerSuffix() {
      return this.playerSuffix;
   }

   public Collection<String> getPlayers() {
      return this.players;
   }

   public MutableComponent getFormattedName(Component component) {
      MutableComponent mutablecomponent = Component.empty().append(this.playerPrefix).append(component).append(this.playerSuffix);
      ChatFormatting chatformatting = this.getColor();
      if (chatformatting != ChatFormatting.RESET) {
         mutablecomponent.withStyle(chatformatting);
      }

      return mutablecomponent;
   }

   public static MutableComponent formatNameForTeam(@Nullable Team team, Component component) {
      return team == null ? component.copy() : team.getFormattedName(component);
   }

   public boolean isAllowFriendlyFire() {
      return this.allowFriendlyFire;
   }

   public void setAllowFriendlyFire(boolean flag) {
      this.allowFriendlyFire = flag;
      this.scoreboard.onTeamChanged(this);
   }

   public boolean canSeeFriendlyInvisibles() {
      return this.seeFriendlyInvisibles;
   }

   public void setSeeFriendlyInvisibles(boolean flag) {
      this.seeFriendlyInvisibles = flag;
      this.scoreboard.onTeamChanged(this);
   }

   public Team.Visibility getNameTagVisibility() {
      return this.nameTagVisibility;
   }

   public Team.Visibility getDeathMessageVisibility() {
      return this.deathMessageVisibility;
   }

   public void setNameTagVisibility(Team.Visibility team_visibility) {
      this.nameTagVisibility = team_visibility;
      this.scoreboard.onTeamChanged(this);
   }

   public void setDeathMessageVisibility(Team.Visibility team_visibility) {
      this.deathMessageVisibility = team_visibility;
      this.scoreboard.onTeamChanged(this);
   }

   public Team.CollisionRule getCollisionRule() {
      return this.collisionRule;
   }

   public void setCollisionRule(Team.CollisionRule team_collisionrule) {
      this.collisionRule = team_collisionrule;
      this.scoreboard.onTeamChanged(this);
   }

   public int packOptions() {
      int i = 0;
      if (this.isAllowFriendlyFire()) {
         i |= 1;
      }

      if (this.canSeeFriendlyInvisibles()) {
         i |= 2;
      }

      return i;
   }

   public void unpackOptions(int i) {
      this.setAllowFriendlyFire((i & 1) > 0);
      this.setSeeFriendlyInvisibles((i & 2) > 0);
   }

   public void setColor(ChatFormatting chatformatting) {
      this.color = chatformatting;
      this.scoreboard.onTeamChanged(this);
   }

   public ChatFormatting getColor() {
      return this.color;
   }
}
