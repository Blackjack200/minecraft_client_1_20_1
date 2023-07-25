package net.minecraft.server.commands.data;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class EntityDataAccessor implements DataAccessor {
   private static final SimpleCommandExceptionType ERROR_NO_PLAYERS = new SimpleCommandExceptionType(Component.translatable("commands.data.entity.invalid"));
   public static final Function<String, DataCommands.DataProvider> PROVIDER = (s) -> new DataCommands.DataProvider() {
         public DataAccessor access(CommandContext<CommandSourceStack> commandcontext) throws CommandSyntaxException {
            return new EntityDataAccessor(EntityArgument.getEntity(commandcontext, s));
         }

         public ArgumentBuilder<CommandSourceStack, ?> wrap(ArgumentBuilder<CommandSourceStack, ?> argumentbuilder, Function<ArgumentBuilder<CommandSourceStack, ?>, ArgumentBuilder<CommandSourceStack, ?>> function) {
            return argumentbuilder.then(Commands.literal("entity").then(function.apply(Commands.argument(s, EntityArgument.entity()))));
         }
      };
   private final Entity entity;

   public EntityDataAccessor(Entity entity) {
      this.entity = entity;
   }

   public void setData(CompoundTag compoundtag) throws CommandSyntaxException {
      if (this.entity instanceof Player) {
         throw ERROR_NO_PLAYERS.create();
      } else {
         UUID uuid = this.entity.getUUID();
         this.entity.load(compoundtag);
         this.entity.setUUID(uuid);
      }
   }

   public CompoundTag getData() {
      return NbtPredicate.getEntityTagToCompare(this.entity);
   }

   public Component getModifiedSuccess() {
      return Component.translatable("commands.data.entity.modified", this.entity.getDisplayName());
   }

   public Component getPrintSuccess(Tag tag) {
      return Component.translatable("commands.data.entity.query", this.entity.getDisplayName(), NbtUtils.toPrettyComponent(tag));
   }

   public Component getPrintSuccess(NbtPathArgument.NbtPath nbtpathargument_nbtpath, double d0, int i) {
      return Component.translatable("commands.data.entity.get", nbtpathargument_nbtpath, this.entity.getDisplayName(), String.format(Locale.ROOT, "%.2f", d0), i);
   }
}
