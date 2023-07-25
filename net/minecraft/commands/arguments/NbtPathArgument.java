package net.minecraft.commands.arguments;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class NbtPathArgument implements ArgumentType<NbtPathArgument.NbtPath> {
   private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo.bar", "foo[0]", "[0]", "[]", "{foo=bar}");
   public static final SimpleCommandExceptionType ERROR_INVALID_NODE = new SimpleCommandExceptionType(Component.translatable("arguments.nbtpath.node.invalid"));
   public static final SimpleCommandExceptionType ERROR_DATA_TOO_DEEP = new SimpleCommandExceptionType(Component.translatable("arguments.nbtpath.too_deep"));
   public static final DynamicCommandExceptionType ERROR_NOTHING_FOUND = new DynamicCommandExceptionType((object) -> Component.translatable("arguments.nbtpath.nothing_found", object));
   static final DynamicCommandExceptionType ERROR_EXPECTED_LIST = new DynamicCommandExceptionType((object) -> Component.translatable("commands.data.modify.expected_list", object));
   static final DynamicCommandExceptionType ERROR_INVALID_INDEX = new DynamicCommandExceptionType((object) -> Component.translatable("commands.data.modify.invalid_index", object));
   private static final char INDEX_MATCH_START = '[';
   private static final char INDEX_MATCH_END = ']';
   private static final char KEY_MATCH_START = '{';
   private static final char KEY_MATCH_END = '}';
   private static final char QUOTED_KEY_START = '"';
   private static final char SINGLE_QUOTED_KEY_START = '\'';

   public static NbtPathArgument nbtPath() {
      return new NbtPathArgument();
   }

   public static NbtPathArgument.NbtPath getPath(CommandContext<CommandSourceStack> commandcontext, String s) {
      return commandcontext.getArgument(s, NbtPathArgument.NbtPath.class);
   }

   public NbtPathArgument.NbtPath parse(StringReader stringreader) throws CommandSyntaxException {
      List<NbtPathArgument.Node> list = Lists.newArrayList();
      int i = stringreader.getCursor();
      Object2IntMap<NbtPathArgument.Node> object2intmap = new Object2IntOpenHashMap<>();
      boolean flag = true;

      while(stringreader.canRead() && stringreader.peek() != ' ') {
         NbtPathArgument.Node nbtpathargument_node = parseNode(stringreader, flag);
         list.add(nbtpathargument_node);
         object2intmap.put(nbtpathargument_node, stringreader.getCursor() - i);
         flag = false;
         if (stringreader.canRead()) {
            char c0 = stringreader.peek();
            if (c0 != ' ' && c0 != '[' && c0 != '{') {
               stringreader.expect('.');
            }
         }
      }

      return new NbtPathArgument.NbtPath(stringreader.getString().substring(i, stringreader.getCursor()), list.toArray(new NbtPathArgument.Node[0]), object2intmap);
   }

   private static NbtPathArgument.Node parseNode(StringReader stringreader, boolean flag) throws CommandSyntaxException {
      Object var10000;
      switch (stringreader.peek()) {
         case '"':
         case '\'':
            var10000 = readObjectNode(stringreader, stringreader.readString());
            break;
         case '[':
            stringreader.skip();
            int i = stringreader.peek();
            if (i == 123) {
               CompoundTag compoundtag1 = (new TagParser(stringreader)).readStruct();
               stringreader.expect(']');
               var10000 = new NbtPathArgument.MatchElementNode(compoundtag1);
            } else if (i == 93) {
               stringreader.skip();
               var10000 = NbtPathArgument.AllElementsNode.INSTANCE;
            } else {
               int j = stringreader.readInt();
               stringreader.expect(']');
               var10000 = new NbtPathArgument.IndexedElementNode(j);
            }
            break;
         case '{':
            if (!flag) {
               throw ERROR_INVALID_NODE.createWithContext(stringreader);
            }

            CompoundTag compoundtag = (new TagParser(stringreader)).readStruct();
            var10000 = new NbtPathArgument.MatchRootObjectNode(compoundtag);
            break;
         default:
            var10000 = readObjectNode(stringreader, readUnquotedName(stringreader));
      }

      return (NbtPathArgument.Node)var10000;
   }

   private static NbtPathArgument.Node readObjectNode(StringReader stringreader, String s) throws CommandSyntaxException {
      if (stringreader.canRead() && stringreader.peek() == '{') {
         CompoundTag compoundtag = (new TagParser(stringreader)).readStruct();
         return new NbtPathArgument.MatchObjectNode(s, compoundtag);
      } else {
         return new NbtPathArgument.CompoundChildNode(s);
      }
   }

   private static String readUnquotedName(StringReader stringreader) throws CommandSyntaxException {
      int i = stringreader.getCursor();

      while(stringreader.canRead() && isAllowedInUnquotedName(stringreader.peek())) {
         stringreader.skip();
      }

      if (stringreader.getCursor() == i) {
         throw ERROR_INVALID_NODE.createWithContext(stringreader);
      } else {
         return stringreader.getString().substring(i, stringreader.getCursor());
      }
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   private static boolean isAllowedInUnquotedName(char c0) {
      return c0 != ' ' && c0 != '"' && c0 != '\'' && c0 != '[' && c0 != ']' && c0 != '.' && c0 != '{' && c0 != '}';
   }

   static Predicate<Tag> createTagPredicate(CompoundTag compoundtag) {
      return (tag) -> NbtUtils.compareNbt(compoundtag, tag, true);
   }

   static class AllElementsNode implements NbtPathArgument.Node {
      public static final NbtPathArgument.AllElementsNode INSTANCE = new NbtPathArgument.AllElementsNode();

      private AllElementsNode() {
      }

      public void getTag(Tag tag, List<Tag> list) {
         if (tag instanceof CollectionTag) {
            list.addAll((CollectionTag)tag);
         }

      }

      public void getOrCreateTag(Tag tag, Supplier<Tag> supplier, List<Tag> list) {
         if (tag instanceof CollectionTag<?> collectiontag) {
            if (collectiontag.isEmpty()) {
               Tag tag1 = supplier.get();
               if (collectiontag.addTag(0, tag1)) {
                  list.add(tag1);
               }
            } else {
               list.addAll(collectiontag);
            }
         }

      }

      public Tag createPreferredParentTag() {
         return new ListTag();
      }

      public int setTag(Tag tag, Supplier<Tag> supplier) {
         if (!(tag instanceof CollectionTag<?> collectiontag)) {
            return 0;
         } else {
            int i = collectiontag.size();
            if (i == 0) {
               collectiontag.addTag(0, supplier.get());
               return 1;
            } else {
               Tag tag1 = supplier.get();
               int j = i - (int)collectiontag.stream().filter(tag1::equals).count();
               if (j == 0) {
                  return 0;
               } else {
                  collectiontag.clear();
                  if (!collectiontag.addTag(0, tag1)) {
                     return 0;
                  } else {
                     for(int k = 1; k < i; ++k) {
                        collectiontag.addTag(k, supplier.get());
                     }

                     return j;
                  }
               }
            }
         }
      }

      public int removeTag(Tag tag) {
         if (tag instanceof CollectionTag<?> collectiontag) {
            int i = collectiontag.size();
            if (i > 0) {
               collectiontag.clear();
               return i;
            }
         }

         return 0;
      }
   }

   static class CompoundChildNode implements NbtPathArgument.Node {
      private final String name;

      public CompoundChildNode(String s) {
         this.name = s;
      }

      public void getTag(Tag tag, List<Tag> list) {
         if (tag instanceof CompoundTag) {
            Tag tag1 = ((CompoundTag)tag).get(this.name);
            if (tag1 != null) {
               list.add(tag1);
            }
         }

      }

      public void getOrCreateTag(Tag tag, Supplier<Tag> supplier, List<Tag> list) {
         if (tag instanceof CompoundTag compoundtag) {
            Tag tag1;
            if (compoundtag.contains(this.name)) {
               tag1 = compoundtag.get(this.name);
            } else {
               tag1 = supplier.get();
               compoundtag.put(this.name, tag1);
            }

            list.add(tag1);
         }

      }

      public Tag createPreferredParentTag() {
         return new CompoundTag();
      }

      public int setTag(Tag tag, Supplier<Tag> supplier) {
         if (tag instanceof CompoundTag compoundtag) {
            Tag tag1 = supplier.get();
            Tag tag2 = compoundtag.put(this.name, tag1);
            if (!tag1.equals(tag2)) {
               return 1;
            }
         }

         return 0;
      }

      public int removeTag(Tag tag) {
         if (tag instanceof CompoundTag compoundtag) {
            if (compoundtag.contains(this.name)) {
               compoundtag.remove(this.name);
               return 1;
            }
         }

         return 0;
      }
   }

   static class IndexedElementNode implements NbtPathArgument.Node {
      private final int index;

      public IndexedElementNode(int i) {
         this.index = i;
      }

      public void getTag(Tag tag, List<Tag> list) {
         if (tag instanceof CollectionTag<?> collectiontag) {
            int i = collectiontag.size();
            int j = this.index < 0 ? i + this.index : this.index;
            if (0 <= j && j < i) {
               list.add(collectiontag.get(j));
            }
         }

      }

      public void getOrCreateTag(Tag tag, Supplier<Tag> supplier, List<Tag> list) {
         this.getTag(tag, list);
      }

      public Tag createPreferredParentTag() {
         return new ListTag();
      }

      public int setTag(Tag tag, Supplier<Tag> supplier) {
         if (tag instanceof CollectionTag<?> collectiontag) {
            int i = collectiontag.size();
            int j = this.index < 0 ? i + this.index : this.index;
            if (0 <= j && j < i) {
               Tag tag1 = collectiontag.get(j);
               Tag tag2 = supplier.get();
               if (!tag2.equals(tag1) && collectiontag.setTag(j, tag2)) {
                  return 1;
               }
            }
         }

         return 0;
      }

      public int removeTag(Tag tag) {
         if (tag instanceof CollectionTag<?> collectiontag) {
            int i = collectiontag.size();
            int j = this.index < 0 ? i + this.index : this.index;
            if (0 <= j && j < i) {
               collectiontag.remove(j);
               return 1;
            }
         }

         return 0;
      }
   }

   static class MatchElementNode implements NbtPathArgument.Node {
      private final CompoundTag pattern;
      private final Predicate<Tag> predicate;

      public MatchElementNode(CompoundTag compoundtag) {
         this.pattern = compoundtag;
         this.predicate = NbtPathArgument.createTagPredicate(compoundtag);
      }

      public void getTag(Tag tag, List<Tag> list) {
         if (tag instanceof ListTag listtag) {
            listtag.stream().filter(this.predicate).forEach(list::add);
         }

      }

      public void getOrCreateTag(Tag tag, Supplier<Tag> supplier, List<Tag> list) {
         MutableBoolean mutableboolean = new MutableBoolean();
         if (tag instanceof ListTag listtag) {
            listtag.stream().filter(this.predicate).forEach((tag1) -> {
               list.add(tag1);
               mutableboolean.setTrue();
            });
            if (mutableboolean.isFalse()) {
               CompoundTag compoundtag = this.pattern.copy();
               listtag.add(compoundtag);
               list.add(compoundtag);
            }
         }

      }

      public Tag createPreferredParentTag() {
         return new ListTag();
      }

      public int setTag(Tag tag, Supplier<Tag> supplier) {
         int i = 0;
         if (tag instanceof ListTag listtag) {
            int j = listtag.size();
            if (j == 0) {
               listtag.add(supplier.get());
               ++i;
            } else {
               for(int k = 0; k < j; ++k) {
                  Tag tag1 = listtag.get(k);
                  if (this.predicate.test(tag1)) {
                     Tag tag2 = supplier.get();
                     if (!tag2.equals(tag1) && listtag.setTag(k, tag2)) {
                        ++i;
                     }
                  }
               }
            }
         }

         return i;
      }

      public int removeTag(Tag tag) {
         int i = 0;
         if (tag instanceof ListTag listtag) {
            for(int j = listtag.size() - 1; j >= 0; --j) {
               if (this.predicate.test(listtag.get(j))) {
                  listtag.remove(j);
                  ++i;
               }
            }
         }

         return i;
      }
   }

   static class MatchObjectNode implements NbtPathArgument.Node {
      private final String name;
      private final CompoundTag pattern;
      private final Predicate<Tag> predicate;

      public MatchObjectNode(String s, CompoundTag compoundtag) {
         this.name = s;
         this.pattern = compoundtag;
         this.predicate = NbtPathArgument.createTagPredicate(compoundtag);
      }

      public void getTag(Tag tag, List<Tag> list) {
         if (tag instanceof CompoundTag) {
            Tag tag1 = ((CompoundTag)tag).get(this.name);
            if (this.predicate.test(tag1)) {
               list.add(tag1);
            }
         }

      }

      public void getOrCreateTag(Tag tag, Supplier<Tag> supplier, List<Tag> list) {
         if (tag instanceof CompoundTag compoundtag) {
            Tag tag1 = compoundtag.get(this.name);
            if (tag1 == null) {
               Tag var6 = this.pattern.copy();
               compoundtag.put(this.name, var6);
               list.add(var6);
            } else if (this.predicate.test(tag1)) {
               list.add(tag1);
            }
         }

      }

      public Tag createPreferredParentTag() {
         return new CompoundTag();
      }

      public int setTag(Tag tag, Supplier<Tag> supplier) {
         if (tag instanceof CompoundTag compoundtag) {
            Tag tag1 = compoundtag.get(this.name);
            if (this.predicate.test(tag1)) {
               Tag tag2 = supplier.get();
               if (!tag2.equals(tag1)) {
                  compoundtag.put(this.name, tag2);
                  return 1;
               }
            }
         }

         return 0;
      }

      public int removeTag(Tag tag) {
         if (tag instanceof CompoundTag compoundtag) {
            Tag tag1 = compoundtag.get(this.name);
            if (this.predicate.test(tag1)) {
               compoundtag.remove(this.name);
               return 1;
            }
         }

         return 0;
      }
   }

   static class MatchRootObjectNode implements NbtPathArgument.Node {
      private final Predicate<Tag> predicate;

      public MatchRootObjectNode(CompoundTag compoundtag) {
         this.predicate = NbtPathArgument.createTagPredicate(compoundtag);
      }

      public void getTag(Tag tag, List<Tag> list) {
         if (tag instanceof CompoundTag && this.predicate.test(tag)) {
            list.add(tag);
         }

      }

      public void getOrCreateTag(Tag tag, Supplier<Tag> supplier, List<Tag> list) {
         this.getTag(tag, list);
      }

      public Tag createPreferredParentTag() {
         return new CompoundTag();
      }

      public int setTag(Tag tag, Supplier<Tag> supplier) {
         return 0;
      }

      public int removeTag(Tag tag) {
         return 0;
      }
   }

   public static class NbtPath {
      private final String original;
      private final Object2IntMap<NbtPathArgument.Node> nodeToOriginalPosition;
      private final NbtPathArgument.Node[] nodes;

      public NbtPath(String s, NbtPathArgument.Node[] anbtpathargument_node, Object2IntMap<NbtPathArgument.Node> object2intmap) {
         this.original = s;
         this.nodes = anbtpathargument_node;
         this.nodeToOriginalPosition = object2intmap;
      }

      public List<Tag> get(Tag tag) throws CommandSyntaxException {
         List<Tag> list = Collections.singletonList(tag);

         for(NbtPathArgument.Node nbtpathargument_node : this.nodes) {
            list = nbtpathargument_node.get(list);
            if (list.isEmpty()) {
               throw this.createNotFoundException(nbtpathargument_node);
            }
         }

         return list;
      }

      public int countMatching(Tag tag) {
         List<Tag> list = Collections.singletonList(tag);

         for(NbtPathArgument.Node nbtpathargument_node : this.nodes) {
            list = nbtpathargument_node.get(list);
            if (list.isEmpty()) {
               return 0;
            }
         }

         return list.size();
      }

      private List<Tag> getOrCreateParents(Tag tag) throws CommandSyntaxException {
         List<Tag> list = Collections.singletonList(tag);

         for(int i = 0; i < this.nodes.length - 1; ++i) {
            NbtPathArgument.Node nbtpathargument_node = this.nodes[i];
            int j = i + 1;
            list = nbtpathargument_node.getOrCreate(list, this.nodes[j]::createPreferredParentTag);
            if (list.isEmpty()) {
               throw this.createNotFoundException(nbtpathargument_node);
            }
         }

         return list;
      }

      public List<Tag> getOrCreate(Tag tag, Supplier<Tag> supplier) throws CommandSyntaxException {
         List<Tag> list = this.getOrCreateParents(tag);
         NbtPathArgument.Node nbtpathargument_node = this.nodes[this.nodes.length - 1];
         return nbtpathargument_node.getOrCreate(list, supplier);
      }

      private static int apply(List<Tag> list, Function<Tag, Integer> function) {
         return list.stream().map(function).reduce(0, (integer, integer1) -> integer + integer1);
      }

      public static boolean isTooDeep(Tag tag, int i) {
         if (i >= 512) {
            return true;
         } else {
            if (tag instanceof CompoundTag) {
               CompoundTag compoundtag = (CompoundTag)tag;

               for(String s : compoundtag.getAllKeys()) {
                  Tag tag1 = compoundtag.get(s);
                  if (tag1 != null && isTooDeep(tag1, i + 1)) {
                     return true;
                  }
               }
            } else if (tag instanceof ListTag) {
               for(Tag tag2 : (ListTag)tag) {
                  if (isTooDeep(tag2, i + 1)) {
                     return true;
                  }
               }
            }

            return false;
         }
      }

      public int set(Tag tag, Tag tag1) throws CommandSyntaxException {
         if (isTooDeep(tag1, this.estimatePathDepth())) {
            throw NbtPathArgument.ERROR_DATA_TOO_DEEP.create();
         } else {
            Tag tag2 = tag1.copy();
            List<Tag> list = this.getOrCreateParents(tag);
            if (list.isEmpty()) {
               return 0;
            } else {
               NbtPathArgument.Node nbtpathargument_node = this.nodes[this.nodes.length - 1];
               MutableBoolean mutableboolean = new MutableBoolean(false);
               return apply(list, (tag4) -> nbtpathargument_node.setTag(tag4, () -> {
                     if (mutableboolean.isFalse()) {
                        mutableboolean.setTrue();
                        return tag2;
                     } else {
                        return tag2.copy();
                     }
                  }));
            }
         }
      }

      private int estimatePathDepth() {
         return this.nodes.length;
      }

      public int insert(int i, CompoundTag compoundtag, List<Tag> list) throws CommandSyntaxException {
         List<Tag> list1 = new ArrayList<>(list.size());

         for(Tag tag : list) {
            Tag tag1 = tag.copy();
            list1.add(tag1);
            if (isTooDeep(tag1, this.estimatePathDepth())) {
               throw NbtPathArgument.ERROR_DATA_TOO_DEEP.create();
            }
         }

         Collection<Tag> collection = this.getOrCreate(compoundtag, ListTag::new);
         int j = 0;
         boolean flag = false;

         for(Tag tag2 : collection) {
            if (!(tag2 instanceof CollectionTag)) {
               throw NbtPathArgument.ERROR_EXPECTED_LIST.create(tag2);
            }

            CollectionTag<?> collectiontag = (CollectionTag)tag2;
            boolean flag1 = false;
            int k = i < 0 ? collectiontag.size() + i + 1 : i;

            for(Tag tag3 : list1) {
               try {
                  if (collectiontag.addTag(k, flag ? tag3.copy() : tag3)) {
                     ++k;
                     flag1 = true;
                  }
               } catch (IndexOutOfBoundsException var16) {
                  throw NbtPathArgument.ERROR_INVALID_INDEX.create(k);
               }
            }

            flag = true;
            j += flag1 ? 1 : 0;
         }

         return j;
      }

      public int remove(Tag tag) {
         List<Tag> list = Collections.singletonList(tag);

         for(int i = 0; i < this.nodes.length - 1; ++i) {
            list = this.nodes[i].get(list);
         }

         NbtPathArgument.Node nbtpathargument_node = this.nodes[this.nodes.length - 1];
         return apply(list, nbtpathargument_node::removeTag);
      }

      private CommandSyntaxException createNotFoundException(NbtPathArgument.Node nbtpathargument_node) {
         int i = this.nodeToOriginalPosition.getInt(nbtpathargument_node);
         return NbtPathArgument.ERROR_NOTHING_FOUND.create(this.original.substring(0, i));
      }

      public String toString() {
         return this.original;
      }
   }

   interface Node {
      void getTag(Tag tag, List<Tag> list);

      void getOrCreateTag(Tag tag, Supplier<Tag> supplier, List<Tag> list);

      Tag createPreferredParentTag();

      int setTag(Tag tag, Supplier<Tag> supplier);

      int removeTag(Tag tag);

      default List<Tag> get(List<Tag> list) {
         return this.collect(list, this::getTag);
      }

      default List<Tag> getOrCreate(List<Tag> list, Supplier<Tag> supplier) {
         return this.collect(list, (tag, list1) -> this.getOrCreateTag(tag, supplier, list1));
      }

      default List<Tag> collect(List<Tag> list, BiConsumer<Tag, List<Tag>> biconsumer) {
         List<Tag> list1 = Lists.newArrayList();

         for(Tag tag : list) {
            biconsumer.accept(tag, list1);
         }

         return list1;
      }
   }
}
