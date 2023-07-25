package net.minecraft.client.searchtree;

import com.google.common.collect.ImmutableList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;

public class FullTextSearchTree<T> extends IdSearchTree<T> {
   private final List<T> contents;
   private final Function<T, Stream<String>> filler;
   private PlainTextSearchTree<T> plainTextSearchTree = PlainTextSearchTree.empty();

   public FullTextSearchTree(Function<T, Stream<String>> function, Function<T, Stream<ResourceLocation>> function1, List<T> list) {
      super(function1, list);
      this.contents = list;
      this.filler = function;
   }

   public void refresh() {
      super.refresh();
      this.plainTextSearchTree = PlainTextSearchTree.create(this.contents, this.filler);
   }

   protected List<T> searchPlainText(String s) {
      return this.plainTextSearchTree.search(s);
   }

   protected List<T> searchResourceLocation(String s, String s1) {
      List<T> list = this.resourceLocationSearchTree.searchNamespace(s);
      List<T> list1 = this.resourceLocationSearchTree.searchPath(s1);
      List<T> list2 = this.plainTextSearchTree.search(s1);
      Iterator<T> iterator = new MergingUniqueIterator<>(list1.iterator(), list2.iterator(), this.additionOrder);
      return ImmutableList.copyOf(new IntersectionIterator<>(list.iterator(), iterator, this.additionOrder));
   }
}
