package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketListener;

public interface ClientGamePacketListener extends PacketListener {
   void handleAddEntity(ClientboundAddEntityPacket clientboundaddentitypacket);

   void handleAddExperienceOrb(ClientboundAddExperienceOrbPacket clientboundaddexperienceorbpacket);

   void handleAddObjective(ClientboundSetObjectivePacket clientboundsetobjectivepacket);

   void handleAddPlayer(ClientboundAddPlayerPacket clientboundaddplayerpacket);

   void handleAnimate(ClientboundAnimatePacket clientboundanimatepacket);

   void handleHurtAnimation(ClientboundHurtAnimationPacket clientboundhurtanimationpacket);

   void handleAwardStats(ClientboundAwardStatsPacket clientboundawardstatspacket);

   void handleAddOrRemoveRecipes(ClientboundRecipePacket clientboundrecipepacket);

   void handleBlockDestruction(ClientboundBlockDestructionPacket clientboundblockdestructionpacket);

   void handleOpenSignEditor(ClientboundOpenSignEditorPacket clientboundopensigneditorpacket);

   void handleBlockEntityData(ClientboundBlockEntityDataPacket clientboundblockentitydatapacket);

   void handleBlockEvent(ClientboundBlockEventPacket clientboundblockeventpacket);

   void handleBlockUpdate(ClientboundBlockUpdatePacket clientboundblockupdatepacket);

   void handleSystemChat(ClientboundSystemChatPacket clientboundsystemchatpacket);

   void handlePlayerChat(ClientboundPlayerChatPacket clientboundplayerchatpacket);

   void handleDisguisedChat(ClientboundDisguisedChatPacket clientbounddisguisedchatpacket);

   void handleDeleteChat(ClientboundDeleteChatPacket clientbounddeletechatpacket);

   void handleChunkBlocksUpdate(ClientboundSectionBlocksUpdatePacket clientboundsectionblocksupdatepacket);

   void handleMapItemData(ClientboundMapItemDataPacket clientboundmapitemdatapacket);

   void handleContainerClose(ClientboundContainerClosePacket clientboundcontainerclosepacket);

   void handleContainerContent(ClientboundContainerSetContentPacket clientboundcontainersetcontentpacket);

   void handleHorseScreenOpen(ClientboundHorseScreenOpenPacket clientboundhorsescreenopenpacket);

   void handleContainerSetData(ClientboundContainerSetDataPacket clientboundcontainersetdatapacket);

   void handleContainerSetSlot(ClientboundContainerSetSlotPacket clientboundcontainersetslotpacket);

   void handleCustomPayload(ClientboundCustomPayloadPacket clientboundcustompayloadpacket);

   void handleDisconnect(ClientboundDisconnectPacket clientbounddisconnectpacket);

   void handleEntityEvent(ClientboundEntityEventPacket clientboundentityeventpacket);

   void handleEntityLinkPacket(ClientboundSetEntityLinkPacket clientboundsetentitylinkpacket);

   void handleSetEntityPassengersPacket(ClientboundSetPassengersPacket clientboundsetpassengerspacket);

   void handleExplosion(ClientboundExplodePacket clientboundexplodepacket);

   void handleGameEvent(ClientboundGameEventPacket clientboundgameeventpacket);

   void handleKeepAlive(ClientboundKeepAlivePacket clientboundkeepalivepacket);

   void handleLevelChunkWithLight(ClientboundLevelChunkWithLightPacket clientboundlevelchunkwithlightpacket);

   void handleChunksBiomes(ClientboundChunksBiomesPacket clientboundchunksbiomespacket);

   void handleForgetLevelChunk(ClientboundForgetLevelChunkPacket clientboundforgetlevelchunkpacket);

   void handleLevelEvent(ClientboundLevelEventPacket clientboundleveleventpacket);

   void handleLogin(ClientboundLoginPacket clientboundloginpacket);

   void handleMoveEntity(ClientboundMoveEntityPacket clientboundmoveentitypacket);

   void handleMovePlayer(ClientboundPlayerPositionPacket clientboundplayerpositionpacket);

   void handleParticleEvent(ClientboundLevelParticlesPacket clientboundlevelparticlespacket);

   void handlePing(ClientboundPingPacket clientboundpingpacket);

   void handlePlayerAbilities(ClientboundPlayerAbilitiesPacket clientboundplayerabilitiespacket);

   void handlePlayerInfoRemove(ClientboundPlayerInfoRemovePacket clientboundplayerinforemovepacket);

   void handlePlayerInfoUpdate(ClientboundPlayerInfoUpdatePacket clientboundplayerinfoupdatepacket);

   void handleRemoveEntities(ClientboundRemoveEntitiesPacket clientboundremoveentitiespacket);

   void handleRemoveMobEffect(ClientboundRemoveMobEffectPacket clientboundremovemobeffectpacket);

   void handleRespawn(ClientboundRespawnPacket clientboundrespawnpacket);

   void handleRotateMob(ClientboundRotateHeadPacket clientboundrotateheadpacket);

   void handleSetCarriedItem(ClientboundSetCarriedItemPacket clientboundsetcarrieditempacket);

   void handleSetDisplayObjective(ClientboundSetDisplayObjectivePacket clientboundsetdisplayobjectivepacket);

   void handleSetEntityData(ClientboundSetEntityDataPacket clientboundsetentitydatapacket);

   void handleSetEntityMotion(ClientboundSetEntityMotionPacket clientboundsetentitymotionpacket);

   void handleSetEquipment(ClientboundSetEquipmentPacket clientboundsetequipmentpacket);

   void handleSetExperience(ClientboundSetExperiencePacket clientboundsetexperiencepacket);

   void handleSetHealth(ClientboundSetHealthPacket clientboundsethealthpacket);

   void handleSetPlayerTeamPacket(ClientboundSetPlayerTeamPacket clientboundsetplayerteampacket);

   void handleSetScore(ClientboundSetScorePacket clientboundsetscorepacket);

   void handleSetSpawn(ClientboundSetDefaultSpawnPositionPacket clientboundsetdefaultspawnpositionpacket);

   void handleSetTime(ClientboundSetTimePacket clientboundsettimepacket);

   void handleSoundEvent(ClientboundSoundPacket clientboundsoundpacket);

   void handleSoundEntityEvent(ClientboundSoundEntityPacket clientboundsoundentitypacket);

   void handleTakeItemEntity(ClientboundTakeItemEntityPacket clientboundtakeitementitypacket);

   void handleTeleportEntity(ClientboundTeleportEntityPacket clientboundteleportentitypacket);

   void handleUpdateAttributes(ClientboundUpdateAttributesPacket clientboundupdateattributespacket);

   void handleUpdateMobEffect(ClientboundUpdateMobEffectPacket clientboundupdatemobeffectpacket);

   void handleUpdateTags(ClientboundUpdateTagsPacket clientboundupdatetagspacket);

   void handlePlayerCombatEnd(ClientboundPlayerCombatEndPacket clientboundplayercombatendpacket);

   void handlePlayerCombatEnter(ClientboundPlayerCombatEnterPacket clientboundplayercombatenterpacket);

   void handlePlayerCombatKill(ClientboundPlayerCombatKillPacket clientboundplayercombatkillpacket);

   void handleChangeDifficulty(ClientboundChangeDifficultyPacket clientboundchangedifficultypacket);

   void handleSetCamera(ClientboundSetCameraPacket clientboundsetcamerapacket);

   void handleInitializeBorder(ClientboundInitializeBorderPacket clientboundinitializeborderpacket);

   void handleSetBorderLerpSize(ClientboundSetBorderLerpSizePacket clientboundsetborderlerpsizepacket);

   void handleSetBorderSize(ClientboundSetBorderSizePacket clientboundsetbordersizepacket);

   void handleSetBorderWarningDelay(ClientboundSetBorderWarningDelayPacket clientboundsetborderwarningdelaypacket);

   void handleSetBorderWarningDistance(ClientboundSetBorderWarningDistancePacket clientboundsetborderwarningdistancepacket);

   void handleSetBorderCenter(ClientboundSetBorderCenterPacket clientboundsetbordercenterpacket);

   void handleTabListCustomisation(ClientboundTabListPacket clientboundtablistpacket);

   void handleResourcePack(ClientboundResourcePackPacket clientboundresourcepackpacket);

   void handleBossUpdate(ClientboundBossEventPacket clientboundbosseventpacket);

   void handleItemCooldown(ClientboundCooldownPacket clientboundcooldownpacket);

   void handleMoveVehicle(ClientboundMoveVehiclePacket clientboundmovevehiclepacket);

   void handleUpdateAdvancementsPacket(ClientboundUpdateAdvancementsPacket clientboundupdateadvancementspacket);

   void handleSelectAdvancementsTab(ClientboundSelectAdvancementsTabPacket clientboundselectadvancementstabpacket);

   void handlePlaceRecipe(ClientboundPlaceGhostRecipePacket clientboundplaceghostrecipepacket);

   void handleCommands(ClientboundCommandsPacket clientboundcommandspacket);

   void handleStopSoundEvent(ClientboundStopSoundPacket clientboundstopsoundpacket);

   void handleCommandSuggestions(ClientboundCommandSuggestionsPacket clientboundcommandsuggestionspacket);

   void handleUpdateRecipes(ClientboundUpdateRecipesPacket clientboundupdaterecipespacket);

   void handleLookAt(ClientboundPlayerLookAtPacket clientboundplayerlookatpacket);

   void handleTagQueryPacket(ClientboundTagQueryPacket clientboundtagquerypacket);

   void handleLightUpdatePacket(ClientboundLightUpdatePacket clientboundlightupdatepacket);

   void handleOpenBook(ClientboundOpenBookPacket clientboundopenbookpacket);

   void handleOpenScreen(ClientboundOpenScreenPacket clientboundopenscreenpacket);

   void handleMerchantOffers(ClientboundMerchantOffersPacket clientboundmerchantofferspacket);

   void handleSetChunkCacheRadius(ClientboundSetChunkCacheRadiusPacket clientboundsetchunkcacheradiuspacket);

   void handleSetSimulationDistance(ClientboundSetSimulationDistancePacket clientboundsetsimulationdistancepacket);

   void handleSetChunkCacheCenter(ClientboundSetChunkCacheCenterPacket clientboundsetchunkcachecenterpacket);

   void handleBlockChangedAck(ClientboundBlockChangedAckPacket clientboundblockchangedackpacket);

   void setActionBarText(ClientboundSetActionBarTextPacket clientboundsetactionbartextpacket);

   void setSubtitleText(ClientboundSetSubtitleTextPacket clientboundsetsubtitletextpacket);

   void setTitleText(ClientboundSetTitleTextPacket clientboundsettitletextpacket);

   void setTitlesAnimation(ClientboundSetTitlesAnimationPacket clientboundsettitlesanimationpacket);

   void handleTitlesClear(ClientboundClearTitlesPacket clientboundcleartitlespacket);

   void handleServerData(ClientboundServerDataPacket clientboundserverdatapacket);

   void handleCustomChatCompletions(ClientboundCustomChatCompletionsPacket clientboundcustomchatcompletionspacket);

   void handleEnabledFeatures(ClientboundUpdateEnabledFeaturesPacket clientboundupdateenabledfeaturespacket);

   void handleBundlePacket(ClientboundBundlePacket clientboundbundlepacket);

   void handleDamageEvent(ClientboundDamageEventPacket clientbounddamageeventpacket);
}
