package net.minecraft.client.searchtree;

import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;

public class IdSearchTree<T> implements RefreshableSearchTree<T> {
   protected final Comparator<T> additionOrder;
   protected final ResourceLocationSearchTree<T> resourceLocationSearchTree;

   public IdSearchTree(Function<T, Stream<ResourceLocation>> function, List<T> list) {
      ToIntFunction<T> tointfunction = Util.createIndexLookup(list);
      this.additionOrder = Comparator.comparingInt(tointfunction);
      this.resourceLocationSearchTree = ResourceLocationSearchTree.create(list, function);
   }

   public List<T> search(String s) {
      int i = s.indexOf(58);
      return i == -1 ? this.searchPlainText(s) : this.searchResourceLocation(s.substring(0, i).trim(), s.substring(i + 1).trim());
   }

   protected List<T> searchPlainText(String s) {
      return this.resourceLocationSearchTree.searchPath(s);
   }

   protected List<T> searchResourceLocation(String s, String s1) {
      List<T> list = this.resourceLocationSearchTree.searchNamespace(s);
      List<T> list1 = this.resourceLocationSearchTree.searchPath(s1);
      return ImmutableList.copyOf(new IntersectionIterator<>(list.iterator(), list1.iterator(), this.additionOrder));
   }
}
