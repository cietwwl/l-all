package org.mmocore.gameserver.utils;


import org.apache.commons.lang3.ArrayUtils;
import org.mmocore.gameserver.data.xml.holder.ItemHolder;
import org.mmocore.gameserver.idfactory.IdFactory;
import org.mmocore.gameserver.instancemanager.CursedWeaponsManager;
import org.mmocore.gameserver.model.Playable;
import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.model.base.Element;
import org.mmocore.gameserver.model.base.Race;
import org.mmocore.gameserver.model.instances.PetInstance;
import org.mmocore.gameserver.model.items.Inventory;
import org.mmocore.gameserver.model.items.ItemInstance;
import org.mmocore.gameserver.model.items.ItemInstance.ItemLocation;
import org.mmocore.gameserver.model.items.attachment.PickableAttachment;
import org.mmocore.gameserver.model.pledge.Clan;
import org.mmocore.gameserver.model.pledge.Rank;
import org.mmocore.gameserver.network.l2.components.IBroadcastPacket;
import org.mmocore.gameserver.network.l2.components.SystemMsg;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.tables.PetDataTable;
import org.mmocore.gameserver.templates.item.ArmorTemplate.ArmorType;
import org.mmocore.gameserver.templates.item.ItemTemplate;
import org.mmocore.gameserver.templates.item.WeaponTemplate.WeaponType;

public final class ItemFunctions
{
	private ItemFunctions()
	{
	}

	public static ItemInstance createItem(int itemId)
	{
		ItemInstance item = new ItemInstance(IdFactory.getInstance().getNextId(), itemId);
		item.setLocation(ItemLocation.VOID);
		item.setCount(1L);

		return item;
	}

	public static boolean isArrow(int itemId)
	{
		return itemId == 17 || (itemId >= 1341 && itemId <= 1345) || (itemId >= 22067 && itemId <= 22071);
	}

	/**
	 * Добавляет предмет в инвентарь игрока, корректно обрабатывает нестыкуемые вещи
	 *
	 * @param playable Владелец инвентаря
	 * @param itemId   ID предмета
	 * @param count	количество
	 */
	public static void addItem(Playable playable, int itemId, long count)
	{
		addItem(playable, itemId, count, true);
	}

	/**
	 * Добавляет предмет в инвентарь игрока, корректно обрабатывает нестыкуемые вещи
	 *
	 * @param playable Владелец инвентаря
	 * @param itemId   ID предмета
	 * @param count	количество
	 */
	public static void addItem(Playable playable, int itemId, long count, boolean notify)
	{
		if(playable == null || count < 1)
			return;

		Playable player;
		if(playable.isSummon())
			player = playable.getPlayer();
		else
			player = playable;

		ItemTemplate t = ItemHolder.getInstance().getTemplate(itemId);
		if(t.isStackable())
			player.getInventory().addItem(itemId, count);
		else
			for(long i = 0; i < count; i++)
				player.getInventory().addItem(itemId, 1);

		if(notify)
			player.sendPacket(SystemMessage.obtainItems(itemId, count, 0));
	}

	/**
	 * Возвращает количество предметов в инвентаре игрока
	 *
	 * @param playable Владелец инвентаря
	 * @param itemId   ID предмета
	 * @return количество
	 */
	public static long getItemCount(Playable playable, int itemId)
	{
		if(playable == null)
			return 0;
		Playable player = playable.getPlayer();
		return player.getInventory().getCountOf(itemId);
	}

	/**
	 * Удаляет предметы из инвентаря игрока, корректно обрабатывает нестыкуемые предметы
	 *
	 * @param playable Владелец инвентаря
	 * @param itemId   ID предмета
	 * @param count	количество
	 * @return true, если вещь удалена
	 */
	public static boolean deleteItem(Playable playable, int itemId, long count)
	{
		return deleteItem(playable, itemId, count, true);
	}

	/**
	 * Удаляет предметы из инвентаря игрока, корректно обрабатывает нестыкуемые предметы
	 *
	 * @param playable Владелец инвентаря
	 * @param itemId   ID предмета
	 * @param count	количество
	 * @param notify оповестить игрока системным сообщением
	 * @return true, если вещь удалена
	 */
	public static boolean deleteItem(Playable playable, int itemId, long count, boolean notify)
	{
		if(playable == null || count < 1)
			return false;

		Player player = playable.getPlayer();

		player.getInventory().writeLock();
		try
		{
			ItemTemplate t = ItemHolder.getInstance().getTemplate(itemId);
			if (t == null)
				return false;
			if(t.isStackable())
			{
				if(!player.getInventory().destroyItemByItemId(itemId, count))
					//TODO audit
					return false;
			}
			else
			{
				if(player.getInventory().getCountOf(itemId) < count)
					return false;

				for(long i = 0; i < count; i++)
					if(!player.getInventory().destroyItemByItemId(itemId, 1L))
						//TODO audit
						return false;
			}
		}
		finally
		{
			player.getInventory().writeUnlock();
		}

		if(notify)
			player.sendPacket(SystemMessage.removeItems(itemId, count));

		return true;
	}

	public final static boolean isClanApellaItem(int itemId)
	{
		return itemId >= 7860 && itemId <= 7879 || itemId >= 9830 && itemId <= 9839;
	}

	public final static IBroadcastPacket checkIfCanEquip(PetInstance pet, ItemInstance item)
	{
		if(!item.isEquipable())
			return SystemMsg.YOUR_PET_CANNOT_CARRY_THIS_ITEM;

		int petId = pet.getNpcId();

		if(item.getTemplate().isPendant() //
				|| PetDataTable.isWolf(petId) && item.getTemplate().isForWolf() //
				|| PetDataTable.isHatchling(petId) && item.getTemplate().isForHatchling() //
				|| PetDataTable.isStrider(petId) && item.getTemplate().isForStrider() //
				|| PetDataTable.isGWolf(petId) && item.getTemplate().isForGWolf() //
				|| PetDataTable.isBabyPet(petId) && item.getTemplate().isForPetBaby() //
				|| PetDataTable.isImprovedBabyPet(petId) && item.getTemplate().isForPetBaby() //
				)
			return null;

		return SystemMsg.YOUR_PET_CANNOT_CARRY_THIS_ITEM;
	}

	/**
	 * Проверяет возможность носить эту вещь.
	 *
	 * @return null, если вещь носить можно, либо SystemMessage, который можно показать игроку
	 */
	public final static IBroadcastPacket checkIfCanEquip(Player player, ItemInstance item)
	{
		//FIXME [G1ta0] черезмерный хардкод, переделать на условия
		int itemId = item.getItemId();
		int targetSlot = item.getTemplate().getBodyPart();
		Clan clan = player.getClan();

		// камаэли и хеви/робы/щиты/сигилы
		if(player.getRace() == Race.kamael && (item.getItemType() == ArmorType.HEAVY || item.getItemType() == ArmorType.MAGIC || item.getItemType() == ArmorType.SIGIL || item.getItemType() == WeaponType.NONE))
			return SystemMsg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;

		// не камаэли и рапиры/арбалеты/древние мечи
		if(player.getRace() != Race.kamael && (item.getItemType() == WeaponType.CROSSBOW || item.getItemType() == WeaponType.RAPIER || item.getItemType() == WeaponType.ANCIENTSWORD))
			return SystemMsg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;

		if(itemId >= 7850 && itemId <= 7859 && player.getLvlJoinedAcademy() == 0) // Clan Oath Armor
			return SystemMsg.THIS_ITEM_CAN_ONLY_BE_WORN_BY_A_MEMBER_OF_THE_CLAN_ACADEMY;

		if(isClanApellaItem(itemId) && player.getPledgeClass().ordinal() < Rank.ELDER.ordinal())
			return SystemMsg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;

		// Замковые короны, доступные для всех членов клана
		if(ArrayUtils.contains(ItemTemplate.ITEM_ID_CASTLE_CIRCLET, itemId) && (clan == null || itemId != ItemTemplate.ITEM_ID_CASTLE_CIRCLET[clan.getCastle()]))
			return SystemMsg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;

		// Корона лидера клана, владеющего замком
		if(itemId == 6841 && (clan == null || !player.isClanLeader() || clan.getCastle() == 0))
			return SystemMsg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;

		// Нельзя одевать оружие, если уже одето проклятое оружие. Проверка двумя способами, для надежности.
		if(targetSlot == ItemTemplate.SLOT_LR_HAND || targetSlot == ItemTemplate.SLOT_L_HAND || targetSlot == ItemTemplate.SLOT_R_HAND)
		{
			if(itemId != player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RHAND) && CursedWeaponsManager.getInstance().isCursed(player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RHAND)))
				return SystemMsg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;
			if(player.isCursedWeaponEquipped() && itemId != player.getCursedWeaponEquippedId())
				return SystemMsg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;
		}

		// Плащи
		if(item.getTemplate().isCloak())
		{
			// Can be worn by Knights or higher ranks who own castle
			if(item.getName().contains("Knight") && (player.getPledgeClass().ordinal() < Rank.KNIGHT.ordinal() || player.getCastle() == null))
				return SystemMsg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;

			// Плащи для камаэлей
			if(item.getName().contains("Kamael") && player.getRace() != Race.kamael)
				return SystemMsg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;

			// Плащи можно носить только с S80 или S84 сетом
			if(!player.getOpenCloak())
				return SystemMsg.THE_CLOAK_CANNOT_BE_EQUIPPED_BECAUSE_YOUR_ARMOR_SET_IS_NOT_COMPLETE;
		}

		if(targetSlot == ItemTemplate.SLOT_DECO)
		{
			int count = player.getTalismanCount();
			if(count <= 0)
				return new SystemMessage(SystemMsg.YOU_CANNOT_WEAR_S1_BECAUSE_YOU_ARE_NOT_WEARING_A_BRACELET).addItemName(itemId);

			ItemInstance deco;
			for(int slot = Inventory.PAPERDOLL_DECO1; slot <= Inventory.PAPERDOLL_DECO6; slot++)
			{
				deco = player.getInventory().getPaperdollItem(slot);
				if(deco != null)
				{
					if(deco == item)
						return null; // талисман уже одет и количество слотов больше нуля
					// Проверяем на количество слотов и одинаковые талисманы
					if(--count <= 0 || deco.getItemId() == itemId)
						return new SystemMessage(SystemMsg.YOU_CANNOT_EQUIP_S1_BECAUSE_YOU_DO_NOT_HAVE_ANY_AVAILABLE_SLOTS).addItemName(itemId);
				}
			}
		}
		return null;
	}

	public static boolean checkIfCanPickup(Playable playable, ItemInstance item)
	{
		Player player = playable.getPlayer();
		if (player.isInvisible())
			return false;
		return item.getDropTimeOwner() <= System.currentTimeMillis() || item.getDropPlayers().contains(player.getObjectId());
	}

	public static boolean canAddItem(Player player, ItemInstance item)
	{
		if(!player.getInventory().validateWeight(item))
		{
			player.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
			return false;
		}

		if(!player.getInventory().validateCapacity(item))
		{
			player.sendPacket(SystemMsg.YOUR_INVENTORY_IS_FULL);
			return false;
		}

		if(!item.getTemplate().getHandler().pickupItem(player, item))
			return false;

		PickableAttachment attachment = item.getAttachment() instanceof PickableAttachment ? (PickableAttachment) item.getAttachment() : null;
		if(attachment != null && !attachment.canPickUp(player))
			return false;

		return true;
	}

	/**
	 * Проверяет возможность передачи вещи
	 *
	 * @param player
	 * @param item
	 * @return
	 */
	public final static boolean checkIfCanDiscard(Player player, ItemInstance item)
	{
		if(PetDataTable.isPetControlItem(item) && player.isMounted())
			return false;

		if(player.getPetControlItem() == item)
			return false;

		if(player.getEnchantScroll() == item)
			return false;

		if(item.isCursed())
			return false;

		if(item.getTemplate().isQuest())
			return false;

		return true;
	}

	/**
	 * Enchant
	 */

	/**
	 * При фейле эти свитки не ломают вещь, но сбрасывают заточку
	 */
	public final static boolean isBlessedEnchantScroll(int itemId)
	{
		switch(itemId)
		{
			case 6575: // Wpn D
			case 6576: // Arm D
			case 6573: // Wpn C
			case 6574: // Arm C
			case 6571: // Wpn B
			case 6572: // Arm B
			case 6569: // Wpn A
			case 6570: // Arm A
			case 6577: // Wpn S
			case 6578: // Arm S
			case 21582: // Blessed Enchant Scroll T'Shirt
				return true;
		}
		return false;
	}

	/**
	 * При фейле эти свитки не имеют побочных эффектов
	 */
	public final static boolean isAncientEnchantScroll(int itemId)
	{
		switch(itemId)
		{
			case 22014: // Wpn B
			case 22016: // Arm B
			case 22015: // Wpn A
			case 22017: // Arm A
			case 20519: // Wpn S
			case 20520: // Arm S
				return true;
		}
		return false;
	}

	/**
	 * HF5: При неудачной модификации предмет не исчезает и сохраняет уровень модификации за счет силы Богини Разрушения. Не действует на предметы выше +15.
	 */
	public final static boolean isDestructionWpnEnchantScroll(int itemId)
	{
		switch(itemId)
		{
			case 22221:
			case 22223:
			case 22225:
			case 22227:
			case 22229:
				return true;
		}
		return false;
	}

	/**
	 * HF5: При неудачной модификации предмет не исчезает и сохраняет уровень модификации за счет силы Богини Разрушения. Не действует на предметы выше +6.
	 */
	public final static boolean isDestructionArmEnchantScroll(int itemId)
	{
		switch(itemId)
		{
			case 22222:
			case 22224:
			case 22226:
			case 22228:
			case 22230:
				return true;
		}
		return false;
	}

	/**
	 * Эти свитки имеют 10% бонус шанса заточки
	 */
	public final static boolean isItemMallEnchantScroll(int itemId)
	{
		switch(itemId)
		{
			case 22006: // Wpn D
			case 22010: // Arm D
			case 22007: // Wpn C
			case 22011: // Arm C
			case 22008: // Wpn B
			case 22012: // Arm B
			case 22009: // Wpn A
			case 22013: // Arm A
			case 20517: // Wpn S
			case 20518: // Arm S
				return true;
			default:
				return isAncientEnchantScroll(itemId);
		}
	}

	/**
	 * Эти свитки имеют 100% шанс
	 */
	public final static boolean isDivineEnchantScroll(int itemId)
	{
		switch(itemId)
		{
			case 22018: // Wpn B
			case 22020: // Arm B
			case 22019: // Wpn A
			case 22021: // Arm A
			case 20521: // Wpn S
			case 20522: // Arm S
				return true;
		}
		return false;
	}

	/**
	 * Они не используются официальным серером, но могут быть задействованы альтами
	 */
	public final static boolean isCrystallEnchantScroll(int itemId)
	{
		switch(itemId)
		{
			case 957: // Wpn D
			case 958: // Arm D
			case 953: // Wpn C
			case 954: // Arm C
			case 949: // Wpn B
			case 950: // Arm B
			case 731: // Wpn A
			case 732: // Arm A
			case 961: // Wpn S
			case 962: // Arm S
				return true;
		}
		return false;
	}

	/**
	 * Проверка соответствия свитка и катализатора грейду вещи.
	 *
	 * @return id кристалла для соответствующих и 0 для несоответствующих.
	 */
	public final static int getEnchantCrystalId(ItemInstance item, ItemInstance scroll, ItemInstance catalyst)
	{
		boolean scrollValid = false, catalystValid = false;

		for(int scrollId : getEnchantScrollId(item))
			if(scroll.getItemId() == scrollId)
			{
				scrollValid = true;
				break;
			}

		if(catalyst == null)
			catalystValid = true;
		else
			for(int catalystId : getEnchantCatalystId(item))
				if(catalystId == catalyst.getItemId())
				{
					catalystValid = true;
					break;
				}

		if(scrollValid && catalystValid)
			switch(item.getCrystalType().cry)
			{
				case ItemTemplate.CRYSTAL_NONE:
					return 0;
				case ItemTemplate.CRYSTAL_D:
					return 1458;
				case ItemTemplate.CRYSTAL_C:
					return 1459;
				case ItemTemplate.CRYSTAL_B:
					return 1460;
				case ItemTemplate.CRYSTAL_A:
					return 1461;
				case ItemTemplate.CRYSTAL_S:
					return 1462;
			}

		return -1;
	}

	/**
	 * Возвращает список свитков, которые подходят для вещи.
	 */
	public final static int[] getEnchantScrollId(ItemInstance item)
	{
		if(item.getTemplate().getType2() == ItemTemplate.TYPE2_WEAPON)
			switch(item.getCrystalType().cry)
			{
				case ItemTemplate.CRYSTAL_NONE:
					return new int[]{13540};
				case ItemTemplate.CRYSTAL_D:
					return new int[]{955, 6575, 957, 22006, 22229};
				case ItemTemplate.CRYSTAL_C:
					return new int[]{951, 6573, 953, 22007, 22227};
				case ItemTemplate.CRYSTAL_B:
					return new int[]{947, 6571, 949, 22008, 22014, 22018, 22225};
				case ItemTemplate.CRYSTAL_A:
					return new int[]{729, 6569, 731, 22009, 22015, 22019, 22223};
				case ItemTemplate.CRYSTAL_S:
					return new int[]{959, 6577, 961, 20517, 20519, 20521, 22221};
			}
		else if(item.getTemplate().getType2() == ItemTemplate.TYPE2_SHIELD_ARMOR || item.getTemplate().getType2() == ItemTemplate.TYPE2_ACCESSORY)
			switch(item.getCrystalType().cry)
			{
				case ItemTemplate.CRYSTAL_NONE:
					return new int[]{21581, 21582};
				case ItemTemplate.CRYSTAL_D:
					return new int[]{956, 6576, 958, 22010, 22230};
				case ItemTemplate.CRYSTAL_C:
					return new int[]{952, 6574, 954, 22011, 22228};
				case ItemTemplate.CRYSTAL_B:
					return new int[]{948, 6572, 950, 22012, 22016, 22020, 22226};
				case ItemTemplate.CRYSTAL_A:
					return new int[]{730, 6570, 732, 22013, 22017, 22021, 22224};
				case ItemTemplate.CRYSTAL_S:
					return new int[]{960, 6578, 962, 20518, 20520, 20522, 22222};
			}
		return new int[0];
	}

	public static final int[][] catalyst = {
			// enchant catalyst list
			{12362, 14078, 14702}, // 0 - W D
			{12363, 14079, 14703}, // 1 - W C
			{12364, 14080, 14704}, // 2 - W B
			{12365, 14081, 14705}, // 3 - W A
			{12366, 14082, 14706}, // 4 - W S
			{12367, 14083, 14707}, // 5 - A D
			{12368, 14084, 14708}, // 6 - A C
			{12369, 14085, 14709}, // 7 - A B
			{12370, 14086, 14710}, // 8 - A A
			{12371, 14087, 14711}, // 9 - A S
	};

	public final static int[] getEnchantCatalystId(ItemInstance item)
	{
		if(item.getTemplate().getType2() == ItemTemplate.TYPE2_WEAPON)
			switch(item.getCrystalType().cry)
			{
				case ItemTemplate.CRYSTAL_A:
					return catalyst[3];
				case ItemTemplate.CRYSTAL_B:
					return catalyst[2];
				case ItemTemplate.CRYSTAL_C:
					return catalyst[1];
				case ItemTemplate.CRYSTAL_D:
					return catalyst[0];
				case ItemTemplate.CRYSTAL_S:
					return catalyst[4];
			}
		else if(item.getTemplate().getType2() == ItemTemplate.TYPE2_SHIELD_ARMOR || item.getTemplate().getType2() == ItemTemplate.TYPE2_ACCESSORY)
			switch(item.getCrystalType().cry)
			{
				case ItemTemplate.CRYSTAL_A:
					return catalyst[8];
				case ItemTemplate.CRYSTAL_B:
					return catalyst[7];
				case ItemTemplate.CRYSTAL_C:
					return catalyst[6];
				case ItemTemplate.CRYSTAL_D:
					return catalyst[5];
				case ItemTemplate.CRYSTAL_S:
					return catalyst[9];
			}
		return new int[]{0, 0, 0};
	}

	public final static int getCatalystPower(int itemId)
	{
		/*
		14702	Agathion Auxiliary Stone: Enchant Weapon (D-Grade)	The Agathion Auxilary Stone raises the ability to enchant a D-Grade weapon by 20%
		14703	Agathion Auxiliary Stone: Enchant Weapon (C-Grade)	The Agathion Auxilary Stone raises the ability to enchant a C-Grade weapon by 18%
		14704	Agathion Auxiliary Stone: Enchant Weapon (B-Grade)	The Agathion Auxilary Stone raises the ability to enchant a B-Grade weapon by 15%
		14705	Agathion Auxiliary Stone: Enchant Weapon (A-Grade)	The Agathion Auxilary Stone raises the ability to enchant a A-Grade weapon by 12%
		14706	Agathion Auxiliary Stone: Enchant Weapon (S-Grade)	The Agathion Auxilary Stone raises the ability to enchant a S-Grade weapon by 10%
		14707	Agathion Auxiliary Stone: Enchant Armor (D-Grade)		The Agathion Auxilary Stone raises the ability to enchant a D-Grade armor by 35%
		14708	Agathion Auxiliary Stone: Enchant Armor (C-Grade)		The Agathion Auxilary Stone raises the ability to enchant a C-Grade armor by 27%
		14709	Agathion Auxiliary Stone: Enchant Armor (B-Grade)		The Agathion Auxilary Stone raises the ability to enchant a B-Grade armor by 23%
		14710	Agathion Auxiliary Stone: Enchant Armor (A-Grade)		The Agathion Auxilary Stone raises the ability to enchant a A-Grade armor by 18%
		14711	Agathion Auxiliary Stone: Enchant Armor (S-Grade)		The Agathion Auxilary Stone raises the ability to enchant a S-Grade armor by 15%
		 */
		for(int i = 0; i < catalyst.length; i++)
			for(int id : catalyst[i])
				if(id == itemId)
					switch(i)
					{
						case 0:
							return 20;
						case 1:
							return 18;
						case 2:
							return 15;
						case 3:
							return 12;
						case 4:
							return 10;
						case 5:
							return 35;
						case 6:
							return 27;
						case 7:
							return 23;
						case 8:
							return 18;
						case 9:
							return 15;
					}

		return 0;
		/*
		switch(_itemId)
		{
			case 14702:
			case 14078:
			case 12362:
				return 20;
			case 14703:
			case 14079:
			case 12363:
				return 18;
			case 14704:
			case 14080:
			case 12364:
				return 15;
			case 14705:
			case 14081:
			case 12365:
				return 12;
			case 14706:
			case 14082:
			case 12366:
				return 10;
			case 14707:
			case 14083:
			case 12367:
				return 35;
			case 14708:
			case 14084:
			case 12368:
				return 27;
			case 14709:
			case 14085:
			case 12369:
				return 23;
			case 14710:
			case 14086:
			case 12370:
				return 18;
			case 14711:
			case 14087:
			case 12371:
				return 15;
			default:
				return 0;
		}
		 */
	}

	/**
	 * Проверяет соответствие уровня заточки и вообще катализатор ли это или левый итем
	 *
	 * @param item
	 * @param catalyst
	 * @return true если катализатор соответствует
	 */
	public static final boolean checkCatalyst(ItemInstance item, ItemInstance catalyst)
	{
		if(item == null || catalyst == null)
			return false;

		int current = item.getEnchantLevel();
		if(current < (item.getTemplate().getBodyPart() == ItemTemplate.SLOT_FULL_ARMOR ? 4 : 3) || current > 8)
			return false;

		for(int catalystRequired : getEnchantCatalystId(item))
			if(catalystRequired == catalyst.getItemId())
				return true;

		return false;
	}


	/**
	 * Возвращает тип элемента для камня атрибуции
	 *
	 * @return значение элемента
	 */
	public static Element getEnchantAttributeStoneElement(int itemId, boolean isArmor)
	{
		Element element = Element.NONE;
		switch(itemId)
		{
			case 9546:
			case 9552:
			case 10521:
				element = Element.FIRE;
				break;
			case 9547:
			case 9553:
			case 10522:
				element = Element.WATER;
				break;
			case 9548:
			case 9554:
			case 10523:
				element = Element.EARTH;
				break;
			case 9549:
			case 9555:
			case 10524:
				element = Element.WIND;
				break;
			case 9550:
			case 9556:
			case 10525:
				element = Element.UNHOLY;
				break;
			case 9551:
			case 9557:
			case 10526:
				element = Element.HOLY;
				break;
		}

		if(isArmor)
			return Element.getReverseElement(element);

		return element;
	}
}
