package net.minecraft.network.chat.contents;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.logging.LogUtils;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;

public class NbtContents implements ComponentContents {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final boolean interpreting;
   private final Optional<Component> separator;
   private final String nbtPathPattern;
   private final DataSource dataSource;
   @Nullable
   protected final NbtPathArgument.NbtPath compiledNbtPath;

   public NbtContents(String s, boolean flag, Optional<Component> optional, DataSource datasource) {
      this(s, compileNbtPath(s), flag, optional, datasource);
   }

   private NbtContents(String s, @Nullable NbtPathArgument.NbtPath nbtpathargument_nbtpath, boolean flag, Optional<Component> optional, DataSource datasource) {
      this.nbtPathPattern = s;
      this.compiledNbtPath = nbtpathargument_nbtpath;
      this.interpreting = flag;
      this.separator = optional;
      this.dataSource = datasource;
   }

   @Nullable
   private static NbtPathArgument.NbtPath compileNbtPath(String s) {
      try {
         return (new NbtPathArgument()).parse(new StringReader(s));
      } catch (CommandSyntaxException var2) {
         return null;
      }
   }

   public String getNbtPath() {
      return this.nbtPathPattern;
   }

   public boolean isInterpreting() {
      return this.interpreting;
   }

   public Optional<Component> getSeparator() {
      return this.separator;
   }

   public DataSource getDataSource() {
      return this.dataSource;
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else {
         if (object instanceof NbtContents) {
            NbtContents nbtcontents = (NbtContents)object;
            if (this.dataSource.equals(nbtcontents.dataSource) && this.separator.equals(nbtcontents.separator) && this.interpreting == nbtcontents.interpreting && this.nbtPathPattern.equals(nbtcontents.nbtPathPattern)) {
               return true;
            }
         }

         return false;
      }
   }

   public int hashCode() {
      int i = this.interpreting ? 1 : 0;
      i = 31 * i + this.separator.hashCode();
      i = 31 * i + this.nbtPathPattern.hashCode();
      return 31 * i + this.dataSource.hashCode();
   }

   public String toString() {
      return "nbt{" + this.dataSource + ", interpreting=" + this.interpreting + ", separator=" + this.separator + "}";
   }

   public MutableComponent resolve(@Nullable CommandSourceStack commandsourcestack, @Nullable Entity entity, int i) throws CommandSyntaxException {
      if (commandsourcestack != null && this.compiledNbtPath != null) {
         Stream<String> stream = this.dataSource.getData(commandsourcestack).flatMap((compoundtag) -> {
            try {
               return this.compiledNbtPath.get(compoundtag).stream();
            } catch (CommandSyntaxException var3) {
               return Stream.empty();
            }
         }).map(Tag::getAsString);
         if (this.interpreting) {
            Component component = DataFixUtils.orElse(ComponentUtils.updateForEntity(commandsourcestack, this.separator, entity, i), ComponentUtils.DEFAULT_NO_STYLE_SEPARATOR);
            return stream.flatMap((s) -> {
               try {
                  MutableComponent mutablecomponent6 = Component.Serializer.fromJson(s);
                  return Stream.of(ComponentUtils.updateForEntity(commandsourcestack, mutablecomponent6, entity, i));
               } catch (Exception var5) {
                  LOGGER.warn("Failed to parse component: {}", s, var5);
                  return Stream.of();
               }
            }).reduce((mutablecomponent4, mutablecomponent5) -> mutablecomponent4.append(component).append(mutablecomponent5)).orElseGet(Component::empty);
         } else {
            return ComponentUtils.updateForEntity(commandsourcestack, this.separator, entity, i).map((mutablecomponent) -> stream.map(Component::literal).reduce((mutablecomponent2, mutablecomponent3) -> mutablecomponent2.append(mutablecomponent).append(mutablecomponent3)).orElseGet(Component::empty)).orElseGet(() -> Component.literal(stream.collect(Collectors.joining(", "))));
         }
      } else {
         return Component.empty();
      }
   }
}
