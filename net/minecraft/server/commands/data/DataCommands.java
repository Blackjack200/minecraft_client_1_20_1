package net.minecraft.server.commands.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class DataCommands {
   private static final SimpleCommandExceptionType ERROR_MERGE_UNCHANGED = new SimpleCommandExceptionType(Component.translatable("commands.data.merge.failed"));
   private static final DynamicCommandExceptionType ERROR_GET_NOT_NUMBER = new DynamicCommandExceptionType((object) -> Component.translatable("commands.data.get.invalid", object));
   private static final DynamicCommandExceptionType ERROR_GET_NON_EXISTENT = new DynamicCommandExceptionType((object) -> Component.translatable("commands.data.get.unknown", object));
   private static final SimpleCommandExceptionType ERROR_MULTIPLE_TAGS = new SimpleCommandExceptionType(Component.translatable("commands.data.get.multiple"));
   private static final DynamicCommandExceptionType ERROR_EXPECTED_OBJECT = new DynamicCommandExceptionType((object) -> Component.translatable("commands.data.modify.expected_object", object));
   private static final DynamicCommandExceptionType ERROR_EXPECTED_VALUE = new DynamicCommandExceptionType((object) -> Component.translatable("commands.data.modify.expected_value", object));
   private static final Dynamic2CommandExceptionType ERROR_INVALID_SUBSTRING = new Dynamic2CommandExceptionType((object, object1) -> Component.translatable("commands.data.modify.invalid_substring", object, object1));
   public static final List<Function<String, DataCommands.DataProvider>> ALL_PROVIDERS = ImmutableList.of(EntityDataAccessor.PROVIDER, BlockDataAccessor.PROVIDER, StorageDataAccessor.PROVIDER);
   public static final List<DataCommands.DataProvider> TARGET_PROVIDERS = ALL_PROVIDERS.stream().map((function) -> function.apply("target")).collect(ImmutableList.toImmutableList());
   public static final List<DataCommands.DataProvider> SOURCE_PROVIDERS = ALL_PROVIDERS.stream().map((function) -> function.apply("source")).collect(ImmutableList.toImmutableList());

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      LiteralArgumentBuilder<CommandSourceStack> literalargumentbuilder = Commands.literal("data").requires((commandsourcestack) -> commandsourcestack.hasPermission(2));

      for(DataCommands.DataProvider datacommands_dataprovider : TARGET_PROVIDERS) {
         literalargumentbuilder.then(datacommands_dataprovider.wrap(Commands.literal("merge"), (argumentbuilder3) -> argumentbuilder3.then(Commands.argument("nbt", CompoundTagArgument.compoundTag()).executes((commandcontext9) -> mergeData(commandcontext9.getSource(), datacommands_dataprovider.access(commandcontext9), CompoundTagArgument.getCompoundTag(commandcontext9, "nbt")))))).then(datacommands_dataprovider.wrap(Commands.literal("get"), (argumentbuilder2) -> argumentbuilder2.executes((commandcontext8) -> getData(commandcontext8.getSource(), datacommands_dataprovider.access(commandcontext8))).then(Commands.argument("path", NbtPathArgument.nbtPath()).executes((commandcontext7) -> getData(commandcontext7.getSource(), datacommands_dataprovider.access(commandcontext7), NbtPathArgument.getPath(commandcontext7, "path"))).then(Commands.argument("scale", DoubleArgumentType.doubleArg()).executes((commandcontext6) -> getNumeric(commandcontext6.getSource(), datacommands_dataprovider.access(commandcontext6), NbtPathArgument.getPath(commandcontext6, "path"), DoubleArgumentType.getDouble(commandcontext6, "scale"))))))).then(datacommands_dataprovider.wrap(Commands.literal("remove"), (argumentbuilder1) -> argumentbuilder1.then(Commands.argument("path", NbtPathArgument.nbtPath()).executes((commandcontext5) -> removeData(commandcontext5.getSource(), datacommands_dataprovider.access(commandcontext5), NbtPathArgument.getPath(commandcontext5, "path")))))).then(decorateModification((argumentbuilder, datacommands_datamanipulatordecorator) -> argumentbuilder.then(Commands.literal("insert").then(Commands.argument("index", IntegerArgumentType.integer()).then(datacommands_datamanipulatordecorator.create((commandcontext4, compoundtag9, nbtpathargument_nbtpath4, list4) -> nbtpathargument_nbtpath4.insert(IntegerArgumentType.getInteger(commandcontext4, "index"), compoundtag9, list4))))).then(Commands.literal("prepend").then(datacommands_datamanipulatordecorator.create((commandcontext3, compoundtag8, nbtpathargument_nbtpath3, list3) -> nbtpathargument_nbtpath3.insert(0, compoundtag8, list3)))).then(Commands.literal("append").then(datacommands_datamanipulatordecorator.create((commandcontext2, compoundtag7, nbtpathargument_nbtpath2, list2) -> nbtpathargument_nbtpath2.insert(-1, compoundtag7, list2)))).then(Commands.literal("set").then(datacommands_datamanipulatordecorator.create((commandcontext1, compoundtag6, nbtpathargument_nbtpath1, list1) -> nbtpathargument_nbtpath1.set(compoundtag6, Iterables.getLast(list1))))).then(Commands.literal("merge").then(datacommands_datamanipulatordecorator.create((commandcontext, compoundtag, nbtpathargument_nbtpath, list) -> {
               CompoundTag compoundtag1 = new CompoundTag();

               for(Tag tag : list) {
                  if (NbtPathArgument.NbtPath.isTooDeep(tag, 0)) {
                     throw NbtPathArgument.ERROR_DATA_TOO_DEEP.create();
                  }

                  if (!(tag instanceof CompoundTag)) {
                     throw ERROR_EXPECTED_OBJECT.create(tag);
                  }

                  CompoundTag compoundtag2 = (CompoundTag)tag;
                  compoundtag1.merge(compoundtag2);
               }

               Collection<Tag> collection = nbtpathargument_nbtpath.getOrCreate(compoundtag, CompoundTag::new);
               int i = 0;

               for(Tag tag1 : collection) {
                  if (!(tag1 instanceof CompoundTag)) {
                     throw ERROR_EXPECTED_OBJECT.create(tag1);
                  }

                  CompoundTag compoundtag3 = (CompoundTag)tag1;
                  CompoundTag compoundtag5 = compoundtag3.copy();
                  compoundtag3.merge(compoundtag1);
                  i += compoundtag5.equals(compoundtag3) ? 0 : 1;
               }

               return i;
            })))));
      }

      commanddispatcher.register(literalargumentbuilder);
   }

   private static String getAsText(Tag tag) throws CommandSyntaxException {
      if (tag.getType().isValue()) {
         return tag.getAsString();
      } else {
         throw ERROR_EXPECTED_VALUE.create(tag);
      }
   }

   private static List<Tag> stringifyTagList(List<Tag> list, DataCommands.StringProcessor datacommands_stringprocessor) throws CommandSyntaxException {
      List<Tag> list1 = new ArrayList<>(list.size());

      for(Tag tag : list) {
         String s = getAsText(tag);
         list1.add(StringTag.valueOf(datacommands_stringprocessor.process(s)));
      }

      return list1;
   }

   private static ArgumentBuilder<CommandSourceStack, ?> decorateModification(BiConsumer<ArgumentBuilder<CommandSourceStack, ?>, DataCommands.DataManipulatorDecorator> biconsumer) {
      LiteralArgumentBuilder<CommandSourceStack> literalargumentbuilder = Commands.literal("modify");

      for(DataCommands.DataProvider datacommands_dataprovider : TARGET_PROVIDERS) {
         datacommands_dataprovider.wrap(literalargumentbuilder, (argumentbuilder) -> {
            ArgumentBuilder<CommandSourceStack, ?> argumentbuilder1 = Commands.argument("targetPath", NbtPathArgument.nbtPath());

            for(DataCommands.DataProvider datacommands_dataprovider2 : SOURCE_PROVIDERS) {
               biconsumer.accept(argumentbuilder1, (datacommands_datamanipulator8) -> datacommands_dataprovider2.wrap(Commands.literal("from"), (argumentbuilder3) -> argumentbuilder3.executes((commandcontext8) -> manipulateData(commandcontext8, datacommands_dataprovider, datacommands_datamanipulator8, getSingletonSource(commandcontext8, datacommands_dataprovider2))).then(Commands.argument("sourcePath", NbtPathArgument.nbtPath()).executes((commandcontext7) -> manipulateData(commandcontext7, datacommands_dataprovider, datacommands_datamanipulator8, resolveSourcePath(commandcontext7, datacommands_dataprovider2))))));
               biconsumer.accept(argumentbuilder1, (datacommands_datamanipulator2) -> datacommands_dataprovider2.wrap(Commands.literal("string"), (argumentbuilder2) -> argumentbuilder2.executes((commandcontext6) -> manipulateData(commandcontext6, datacommands_dataprovider, datacommands_datamanipulator2, stringifyTagList(getSingletonSource(commandcontext6, datacommands_dataprovider2), (s3) -> s3))).then(Commands.argument("sourcePath", NbtPathArgument.nbtPath()).executes((commandcontext5) -> manipulateData(commandcontext5, datacommands_dataprovider, datacommands_datamanipulator2, stringifyTagList(resolveSourcePath(commandcontext5, datacommands_dataprovider2), (s2) -> s2))).then(Commands.argument("start", IntegerArgumentType.integer()).executes((commandcontext3) -> manipulateData(commandcontext3, datacommands_dataprovider, datacommands_datamanipulator2, stringifyTagList(resolveSourcePath(commandcontext3, datacommands_dataprovider2), (s1) -> substring(s1, IntegerArgumentType.getInteger(commandcontext3, "start"))))).then(Commands.argument("end", IntegerArgumentType.integer()).executes((commandcontext1) -> manipulateData(commandcontext1, datacommands_dataprovider, datacommands_datamanipulator2, stringifyTagList(resolveSourcePath(commandcontext1, datacommands_dataprovider2), (s) -> substring(s, IntegerArgumentType.getInteger(commandcontext1, "start"), IntegerArgumentType.getInteger(commandcontext1, "end"))))))))));
            }

            biconsumer.accept(argumentbuilder1, (datacommands_datamanipulator) -> Commands.literal("value").then(Commands.argument("value", NbtTagArgument.nbtTag()).executes((commandcontext) -> {
                  List<Tag> list = Collections.singletonList(NbtTagArgument.getNbtTag(commandcontext, "value"));
                  return manipulateData(commandcontext, datacommands_dataprovider, datacommands_datamanipulator, list);
               })));
            return argumentbuilder.then(argumentbuilder1);
         });
      }

      return literalargumentbuilder;
   }

   private static String validatedSubstring(String s, int i, int j) throws CommandSyntaxException {
      if (i >= 0 && j <= s.length() && i <= j) {
         return s.substring(i, j);
      } else {
         throw ERROR_INVALID_SUBSTRING.create(i, j);
      }
   }

   private static String substring(String s, int i, int j) throws CommandSyntaxException {
      int k = s.length();
      int l = getOffset(i, k);
      int i1 = getOffset(j, k);
      return validatedSubstring(s, l, i1);
   }

   private static String substring(String s, int i) throws CommandSyntaxException {
      int j = s.length();
      return validatedSubstring(s, getOffset(i, j), j);
   }

   private static int getOffset(int i, int j) {
      return i >= 0 ? i : j + i;
   }

   private static List<Tag> getSingletonSource(CommandContext<CommandSourceStack> commandcontext, DataCommands.DataProvider datacommands_dataprovider) throws CommandSyntaxException {
      DataAccessor dataaccessor = datacommands_dataprovider.access(commandcontext);
      return Collections.singletonList(dataaccessor.getData());
   }

   private static List<Tag> resolveSourcePath(CommandContext<CommandSourceStack> commandcontext, DataCommands.DataProvider datacommands_dataprovider) throws CommandSyntaxException {
      DataAccessor dataaccessor = datacommands_dataprovider.access(commandcontext);
      NbtPathArgument.NbtPath nbtpathargument_nbtpath = NbtPathArgument.getPath(commandcontext, "sourcePath");
      return nbtpathargument_nbtpath.get(dataaccessor.getData());
   }

   private static int manipulateData(CommandContext<CommandSourceStack> commandcontext, DataCommands.DataProvider datacommands_dataprovider, DataCommands.DataManipulator datacommands_datamanipulator, List<Tag> list) throws CommandSyntaxException {
      DataAccessor dataaccessor = datacommands_dataprovider.access(commandcontext);
      NbtPathArgument.NbtPath nbtpathargument_nbtpath = NbtPathArgument.getPath(commandcontext, "targetPath");
      CompoundTag compoundtag = dataaccessor.getData();
      int i = datacommands_datamanipulator.modify(commandcontext, compoundtag, nbtpathargument_nbtpath, list);
      if (i == 0) {
         throw ERROR_MERGE_UNCHANGED.create();
      } else {
         dataaccessor.setData(compoundtag);
         commandcontext.getSource().sendSuccess(() -> dataaccessor.getModifiedSuccess(), true);
         return i;
      }
   }

   private static int removeData(CommandSourceStack commandsourcestack, DataAccessor dataaccessor, NbtPathArgument.NbtPath nbtpathargument_nbtpath) throws CommandSyntaxException {
      CompoundTag compoundtag = dataaccessor.getData();
      int i = nbtpathargument_nbtpath.remove(compoundtag);
      if (i == 0) {
         throw ERROR_MERGE_UNCHANGED.create();
      } else {
         dataaccessor.setData(compoundtag);
         commandsourcestack.sendSuccess(() -> dataaccessor.getModifiedSuccess(), true);
         return i;
      }
   }

   private static Tag getSingleTag(NbtPathArgument.NbtPath nbtpathargument_nbtpath, DataAccessor dataaccessor) throws CommandSyntaxException {
      Collection<Tag> collection = nbtpathargument_nbtpath.get(dataaccessor.getData());
      Iterator<Tag> iterator = collection.iterator();
      Tag tag = iterator.next();
      if (iterator.hasNext()) {
         throw ERROR_MULTIPLE_TAGS.create();
      } else {
         return tag;
      }
   }

   private static int getData(CommandSourceStack commandsourcestack, DataAccessor dataaccessor, NbtPathArgument.NbtPath nbtpathargument_nbtpath) throws CommandSyntaxException {
      Tag tag = getSingleTag(nbtpathargument_nbtpath, dataaccessor);
      int i;
      if (tag instanceof NumericTag) {
         i = Mth.floor(((NumericTag)tag).getAsDouble());
      } else if (tag instanceof CollectionTag) {
         i = ((CollectionTag)tag).size();
      } else if (tag instanceof CompoundTag) {
         i = ((CompoundTag)tag).size();
      } else {
         if (!(tag instanceof StringTag)) {
            throw ERROR_GET_NON_EXISTENT.create(nbtpathargument_nbtpath.toString());
         }

         i = tag.getAsString().length();
      }

      commandsourcestack.sendSuccess(() -> dataaccessor.getPrintSuccess(tag), false);
      return i;
   }

   private static int getNumeric(CommandSourceStack commandsourcestack, DataAccessor dataaccessor, NbtPathArgument.NbtPath nbtpathargument_nbtpath, double d0) throws CommandSyntaxException {
      Tag tag = getSingleTag(nbtpathargument_nbtpath, dataaccessor);
      if (!(tag instanceof NumericTag)) {
         throw ERROR_GET_NOT_NUMBER.create(nbtpathargument_nbtpath.toString());
      } else {
         int i = Mth.floor(((NumericTag)tag).getAsDouble() * d0);
         commandsourcestack.sendSuccess(() -> dataaccessor.getPrintSuccess(nbtpathargument_nbtpath, d0, i), false);
         return i;
      }
   }

   private static int getData(CommandSourceStack commandsourcestack, DataAccessor dataaccessor) throws CommandSyntaxException {
      CompoundTag compoundtag = dataaccessor.getData();
      commandsourcestack.sendSuccess(() -> dataaccessor.getPrintSuccess(compoundtag), false);
      return 1;
   }

   private static int mergeData(CommandSourceStack commandsourcestack, DataAccessor dataaccessor, CompoundTag compoundtag) throws CommandSyntaxException {
      CompoundTag compoundtag1 = dataaccessor.getData();
      if (NbtPathArgument.NbtPath.isTooDeep(compoundtag, 0)) {
         throw NbtPathArgument.ERROR_DATA_TOO_DEEP.create();
      } else {
         CompoundTag compoundtag2 = compoundtag1.copy().merge(compoundtag);
         if (compoundtag1.equals(compoundtag2)) {
            throw ERROR_MERGE_UNCHANGED.create();
         } else {
            dataaccessor.setData(compoundtag2);
            commandsourcestack.sendSuccess(() -> dataaccessor.getModifiedSuccess(), true);
            return 1;
         }
      }
   }

   @FunctionalInterface
   interface DataManipulator {
      int modify(CommandContext<CommandSourceStack> commandcontext, CompoundTag compoundtag, NbtPathArgument.NbtPath nbtpathargument_nbtpath, List<Tag> list) throws CommandSyntaxException;
   }

   @FunctionalInterface
   interface DataManipulatorDecorator {
      ArgumentBuilder<CommandSourceStack, ?> create(DataCommands.DataManipulator datacommands_datamanipulator);
   }

   public interface DataProvider {
      DataAccessor access(CommandContext<CommandSourceStack> commandcontext) throws CommandSyntaxException;

      ArgumentBuilder<CommandSourceStack, ?> wrap(ArgumentBuilder<CommandSourceStack, ?> argumentbuilder, Function<ArgumentBuilder<CommandSourceStack, ?>, ArgumentBuilder<CommandSourceStack, ?>> function);
   }

   @FunctionalInterface
   interface StringProcessor {
      String process(String s) throws CommandSyntaxException;
   }
}
