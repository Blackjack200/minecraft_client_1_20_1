package net.minecraft.network.chat.contents;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public record EntityDataSource(String selectorPattern, @Nullable EntitySelector compiledSelector) implements DataSource {
   public EntityDataSource(String s) {
      this(s, compileSelector(s));
   }

   @Nullable
   private static EntitySelector compileSelector(String s) {
      try {
         EntitySelectorParser entityselectorparser = new EntitySelectorParser(new StringReader(s));
         return entityselectorparser.parse();
      } catch (CommandSyntaxException var2) {
         return null;
      }
   }

   public Stream<CompoundTag> getData(CommandSourceStack commandsourcestack) throws CommandSyntaxException {
      if (this.compiledSelector != null) {
         List<? extends Entity> list = this.compiledSelector.findEntities(commandsourcestack);
         return list.stream().map(NbtPredicate::getEntityTagToCompare);
      } else {
         return Stream.empty();
      }
   }

   public String toString() {
      return "entity=" + this.selectorPattern;
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else {
         if (object instanceof EntityDataSource) {
            EntityDataSource entitydatasource = (EntityDataSource)object;
            if (this.selectorPattern.equals(entitydatasource.selectorPattern)) {
               return true;
            }
         }

         return false;
      }
   }

   public int hashCode() {
      return this.selectorPattern.hashCode();
   }
}
