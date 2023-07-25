package com.mojang.realmsclient.gui;

import com.mojang.realmsclient.dto.RealmsNews;
import com.mojang.realmsclient.util.RealmsPersistence;

public class RealmsNewsManager {
   private final RealmsPersistence newsLocalStorage;
   private boolean hasUnreadNews;
   private String newsLink;

   public RealmsNewsManager(RealmsPersistence realmspersistence) {
      this.newsLocalStorage = realmspersistence;
      RealmsPersistence.RealmsPersistenceData realmspersistence_realmspersistencedata = realmspersistence.read();
      this.hasUnreadNews = realmspersistence_realmspersistencedata.hasUnreadNews;
      this.newsLink = realmspersistence_realmspersistencedata.newsLink;
   }

   public boolean hasUnreadNews() {
      return this.hasUnreadNews;
   }

   public String newsLink() {
      return this.newsLink;
   }

   public void updateUnreadNews(RealmsNews realmsnews) {
      RealmsPersistence.RealmsPersistenceData realmspersistence_realmspersistencedata = this.updateNewsStorage(realmsnews);
      this.hasUnreadNews = realmspersistence_realmspersistencedata.hasUnreadNews;
      this.newsLink = realmspersistence_realmspersistencedata.newsLink;
   }

   private RealmsPersistence.RealmsPersistenceData updateNewsStorage(RealmsNews realmsnews) {
      RealmsPersistence.RealmsPersistenceData realmspersistence_realmspersistencedata = new RealmsPersistence.RealmsPersistenceData();
      realmspersistence_realmspersistencedata.newsLink = realmsnews.newsLink;
      RealmsPersistence.RealmsPersistenceData realmspersistence_realmspersistencedata1 = this.newsLocalStorage.read();
      boolean flag = realmspersistence_realmspersistencedata.newsLink == null || realmspersistence_realmspersistencedata.newsLink.equals(realmspersistence_realmspersistencedata1.newsLink);
      if (flag) {
         return realmspersistence_realmspersistencedata1;
      } else {
         realmspersistence_realmspersistencedata.hasUnreadNews = true;
         this.newsLocalStorage.save(realmspersistence_realmspersistencedata);
         return realmspersistence_realmspersistencedata;
      }
   }
}
