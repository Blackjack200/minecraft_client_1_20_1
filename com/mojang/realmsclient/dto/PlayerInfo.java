package com.mojang.realmsclient.dto;

import com.google.gson.annotations.SerializedName;

public class PlayerInfo extends ValueObject implements ReflectionBasedSerialization {
   @SerializedName("name")
   private String name;
   @SerializedName("uuid")
   private String uuid;
   @SerializedName("operator")
   private boolean operator;
   @SerializedName("accepted")
   private boolean accepted;
   @SerializedName("online")
   private boolean online;

   public String getName() {
      return this.name;
   }

   public void setName(String s) {
      this.name = s;
   }

   public String getUuid() {
      return this.uuid;
   }

   public void setUuid(String s) {
      this.uuid = s;
   }

   public boolean isOperator() {
      return this.operator;
   }

   public void setOperator(boolean flag) {
      this.operator = flag;
   }

   public boolean getAccepted() {
      return this.accepted;
   }

   public void setAccepted(boolean flag) {
      this.accepted = flag;
   }

   public boolean getOnline() {
      return this.online;
   }

   public void setOnline(boolean flag) {
      this.online = flag;
   }
}
