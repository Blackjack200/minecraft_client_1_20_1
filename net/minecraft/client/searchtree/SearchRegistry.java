package net.minecraft.client.searchtree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.ItemStack;

public class SearchRegistry implements ResourceManagerReloadListener {
   public static final SearchRegistry.Key<ItemStack> CREATIVE_NAMES = new SearchRegistry.Key<>();
   public static final SearchRegistry.Key<ItemStack> CREATIVE_TAGS = new SearchRegistry.Key<>();
   public static final SearchRegistry.Key<RecipeCollection> RECIPE_COLLECTIONS = new SearchRegistry.Key<>();
   private final Map<SearchRegistry.Key<?>, SearchRegistry.TreeEntry<?>> searchTrees = new HashMap<>();

   public void onResourceManagerReload(ResourceManager resourcemanager) {
      for(SearchRegistry.TreeEntry<?> searchregistry_treeentry : this.searchTrees.values()) {
         searchregistry_treeentry.refresh();
      }

   }

   public <T> void register(SearchRegistry.Key<T> searchregistry_key, SearchRegistry.TreeBuilderSupplier<T> searchregistry_treebuildersupplier) {
      this.searchTrees.put(searchregistry_key, new SearchRegistry.TreeEntry<>(searchregistry_treebuildersupplier));
   }

   private <T> SearchRegistry.TreeEntry<T> getSupplier(SearchRegistry.Key<T> searchregistry_key) {
      SearchRegistry.TreeEntry<T> searchregistry_treeentry = this.searchTrees.get(searchregistry_key);
      if (searchregistry_treeentry == null) {
         throw new IllegalStateException("Tree builder not registered");
      } else {
         return searchregistry_treeentry;
      }
   }

   public <T> void populate(SearchRegistry.Key<T> searchregistry_key, List<T> list) {
      this.getSupplier(searchregistry_key).populate(list);
   }

   public <T> SearchTree<T> getTree(SearchRegistry.Key<T> searchregistry_key) {
      return this.getSupplier(searchregistry_key).tree;
   }

   public static class Key<T> {
   }

   public interface TreeBuilderSupplier<T> extends Function<List<T>, RefreshableSearchTree<T>> {
   }

   static class TreeEntry<T> {
      private final SearchRegistry.TreeBuilderSupplier<T> factory;
      RefreshableSearchTree<T> tree = RefreshableSearchTree.empty();

      TreeEntry(SearchRegistry.TreeBuilderSupplier<T> searchregistry_treebuildersupplier) {
         this.factory = searchregistry_treebuildersupplier;
      }

      void populate(List<T> list) {
         this.tree = this.factory.apply((T)list);
         this.tree.refresh();
      }

      void refresh() {
         this.tree.refresh();
      }
   }
}
