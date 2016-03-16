package org.mmocore.gameserver.network.l2.components;

import java.util.NoSuchElementException;

import org.mmocore.gameserver.model.Player;
import org.mmocore.gameserver.network.l2.s2c.L2GameServerPacket;
import org.mmocore.gameserver.network.l2.s2c.SystemMessage;
import org.mmocore.gameserver.utils.Log;

/**
 * @author VISTALL
 * @date  13:28/01.12.2010
 */
public enum SystemMsg implements IBroadcastPacket
{
	// Message: You have been disconnected from the server.
	YOU_HAVE_BEEN_DISCONNECTED_FROM_THE_SERVER(0),
	// Message: The server will be coming down in $s1 second(s).  Please find a safe place to log out.
	THE_SERVER_WILL_BE_COMING_DOWN_IN_S1_SECONDS__PLEASE_FIND_A_SAFE_PLACE_TO_LOG_OUT(1),
	// Message: $s1 is not currently logged in.
	S1_IS_NOT_CURRENTLY_LOGGED_IN(3),
	// Message: You cannot ask yourself to apply to a clan.
	YOU_CANNOT_ASK_YOURSELF_TO_APPLY_TO_A_CLAN(4),
	// Message: $s1 is not a clan leader.
	S1_IS_NOT_A_CLAN_LEADER(9),
	// Message: $s1 is already a member of another clan.
	S1_IS_ALREADY_A_MEMBER_OF_ANOTHER_CLAN(10),
	// Message: Unable to dissolve: your clan has requested to participate in a castle siege.
	UNABLE_TO_DISSOLVE_YOUR_CLAN_HAS_REQUESTED_TO_PARTICIPATE_IN_A_CASTLE_SIEGE(13),
	// Message: Unable to dissolve: your clan owns one or more castles or hideouts.
	UNABLE_TO_DISSOLVE_YOUR_CLAN_OWNS_ONE_OR_MORE_CASTLES_OR_HIDEOUTS(14),
	// Message: You are not in siege.
	YOU_ARE_NOT_IN_SIEGE(16),
	// Message: Your target is out of range.
	YOUR_TARGET_IS_OUT_OF_RANGE(22),
	// Message: Not enough HP.
	NOT_ENOUGH_HP(23),
	// Message: Not enough MP.
	NOT_ENOUGH_MP(24),
	// Message: Rejuvenating HP.
	REJUVENATING_HP(25),
	// Message: Your casting has been interrupted.
	YOUR_CASTING_HAS_BEEN_INTERRUPTED(27),
	// Message: You have obtained $s2 $s1.
	YOU_HAVE_OBTAINED_S2_S1(29),
	// Message: You have obtained $s1.
	YOU_HAVE_OBTAINED_S1(30),
	// Message: You cannot move while sitting.
	YOU_CANNOT_MOVE_WHILE_SITTING(31),
	// Message: Welcome to the World of Lineage II.
	WELCOME_TO_THE_WORLD_OF_LINEAGE_II(34),
	// Message: You carefully nock an arrow.
	YOU_CAREFULLY_NOCK_AN_ARROW(41),
	// Message: You have avoided $c1's attack.
	YOU_HAVE_AVOIDED_C1S_ATTACK(42),
	// Message: You have earned $s1 experience.
	YOU_HAVE_EARNED_S1_EXPERIENCE(45),
	// Message: You use $s1.
	YOU_USE_S1(46),
	// Message: You have equipped your $s1.
	YOU_HAVE_EQUIPPED_YOUR_S1(49),
	// Message: Your target cannot be found.
	YOUR_TARGET_CANNOT_BE_FOUND(50),
	// Message: You cannot use this on yourself.
	YOU_CANNOT_USE_THIS_ON_YOURSELF(51),
	// Message: You have earned $s1 adena.
	YOU_HAVE_EARNED_S1_ADENA(52),
	// Message: You have earned $s2 $s1(s).
	YOU_HAVE_EARNED_S2_S1S(53),
	// Message: You have earned $s1.
	YOU_HAVE_EARNED_S1(54),
	// Message: You have failed to pick up $s1 adena.
	YOU_HAVE_FAILED_TO_PICK_UP_S1_ADENA(55),
	// Message: You have failed to pick up $s1.
	YOU_HAVE_FAILED_TO_PICK_UP_S1(56),
	// Message: Nothing happened.
	NOTHING_HAPPENED(61),
	// Message: This name already exists.
	THIS_NAME_ALREADY_EXISTS(79),
	// Message: Your title cannot exceed 16 characters in length.пїЅ Please try again.
	YOUR_TITLE_CANNOT_EXCEED_16_CHARACTERS_IN_LENGTH(80),
	// Message: You may not attack in a peaceful zone.
	YOU_MAY_NOT_ATTACK_IN_A_PEACEFUL_ZONE(84),
	// Message: You may not attack this target in a peaceful zone.
	YOU_MAY_NOT_ATTACK_THIS_TARGET_IN_A_PEACEFUL_ZONE(85),
	// Message: $s1 has worn off.
	S1_HAS_WORN_OFF(92),
	// Message: You have earned $s1 experience and $s2 SP.
	YOU_HAVE_EARNED_S1_EXPERIENCE_AND_S2_SP(95),
	// Message: Your level has increased!
	YOUR_LEVEL_HAS_INCREASED(96),
	// Message: This item cannot be discarded.
	THIS_ITEM_CANNOT_BE_DISCARDED(98),
	// Message: This item cannot be traded or sold.
	THIS_ITEM_CANNOT_BE_TRADED_OR_SOLD(99),
	// Message: You cannot exit the game while in combat.
	YOU_CANNOT_EXIT_THE_GAME_WHILE_IN_COMBAT(101),
	// Message: You cannot restart while in combat.
	YOU_CANNOT_RESTART_WHILE_IN_COMBAT(102),
	// Message: You cannot change weapons during an attack.
	YOU_CANNOT_CHANGE_WEAPONS_DURING_AN_ATTACK(104),
	// Message: $c1 has been invited to the party.
	C1_HAS_BEEN_INVITED_TO_THE_PARTY(105),
	// Message: You have joined $s1's party.
	YOU_HAVE_JOINED_S1S_PARTY(106),
	// Message: $c1 has joined the party.
	C1_HAS_JOINED_THE_PARTY(107),
	// Message: $c1 has left the party.
	C1_HAS_LEFT_THE_PARTY(108),
	// Message: Invalid target.
	INVALID_TARGET(109),
	// Message: $s1's effect can be felt.
	S1S_EFFECT_CAN_BE_FELT(110),
	// Message: Your shield defense has succeeded.
	YOUR_SHIELD_DEFENSE_HAS_SUCCEEDED(111),
	// Message: You have run out of arrows.
	YOU_HAVE_RUN_OUT_OF_ARROWS(112),
	// Message: $s1 cannot be used due to unsuitable terms.
	S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS(113),
	// Message: You have entered the shadow of the Mother Tree.
	YOU_HAVE_ENTERED_THE_SHADOW_OF_THE_MOTHER_TREE(114),
	// Message: You have left the shadow of the Mother Tree.
	YOU_HAVE_LEFT_THE_SHADOW_OF_THE_MOTHER_TREE(115),
	// Message: You have entered a peace zone.
	YOU_HAVE_ENTERED_A_PEACE_ZONE(116),
	// Message: You have left the peace zone.
	YOU_HAVE_LEFT_THE_PEACE_ZONE(117),
	// Message: You have requested a trade with $c1.
	YOU_HAVE_REQUESTED_A_TRADE_WITH_C1(118),
	// Message: $c1 has denied your request to trade.
	C1_HAS_DENIED_YOUR_REQUEST_TO_TRADE(119),
	// Message: You begin trading with $c1.
	YOU_BEGIN_TRADING_WITH_C1(120),
	// Message: $c1 has confirmed the trade.
	C1_HAS_CONFIRMED_THE_TRADE(121),
	// Message: You may no longer adjust items in the trade because the trade has been confirmed.
	YOU_MAY_NO_LONGER_ADJUST_ITEMS_IN_THE_TRADE_BECAUSE_THE_TRADE_HAS_BEEN_CONFIRMED(122),
	// Message: Your trade was successful.
	YOUR_TRADE_WAS_SUCCESSFUL(123),
	// Message: $c1 has cancelled the trade.
	C1_HAS_CANCELLED_THE_TRADE(124),
	// Message: You have been disconnected from the server. Please login again.
	YOU_HAVE_BEEN_DISCONNECTED_FROM_THE_SERVER_PLEASE_LOGIN_AGAIN(127),
	// Message: Your inventory is full.
	YOUR_INVENTORY_IS_FULL(129),
	// Message: Your warehouse is full.
	YOUR_WAREHOUSE_IS_FULL(130),
	// Message: $s1 has been added to your friends list.
	S1_HAS_BEEN_ADDED_TO_YOUR_FRIENDS_LIST(132),
	// Message: $s1 has been removed from your friends list.
	S1_HAS_BEEN_REMOVED_FROM_YOUR_FRIENDS_LIST(133),
	// Message: There are no more items in the shortcut.
	THERE_ARE_NO_MORE_ITEMS_IN_THE_SHORTCUT(137),
	// Message: $c1 has resisted your $s2.
	C1_HAS_RESISTED_YOUR_S2(139),
	// Message: You are already trading with someone.
	YOU_ARE_ALREADY_TRADING_WITH_SOMEONE(142),
	// Message: That is an incorrect target.
	THAT_IS_AN_INCORRECT_TARGET(144),
	// Message: That player is not online.
	THAT_PLAYER_IS_NOT_ONLINE(145),
	// Message: You cannot pick up or use items while trading.
	YOU_CANNOT_PICK_UP_OR_USE_ITEMS_WHILE_TRADING(149),
	// Message: You cannot discard something that far away from you.
	YOU_CANNOT_DISCARD_SOMETHING_THAT_FAR_AWAY_FROM_YOU(151),
	// Message: You have invited the wrong target.
	YOU_HAVE_INVITED_THE_WRONG_TARGET(152),
	// Message: $c1 is on another task. Please try again later.
	C1_IS_ON_ANOTHER_TASK(153),
	// Message: Only the leader can give out invitations.
	ONLY_THE_LEADER_CAN_GIVE_OUT_INVITATIONS(154),
	// Message: The party is full.
	THE_PARTY_IS_FULL(155),
	// Message: Your attack has failed.
	YOUR_ATTACK_HAS_FAILED(158),
	// Message: $c1 is a member of another party and cannot be invited.
	C1_IS_A_MEMBER_OF_ANOTHER_PARTY_AND_CANNOT_BE_INVITED(160),
	// Message: That player is not currently online.
	THAT_PLAYER_IS_NOT_CURRENTLY_ONLINE(161),
	// Message: You have moved too far away from the warehouse to perform that action.
	YOU_HAVE_MOVED_TOO_FAR_AWAY_FROM_THE_WAREHOUSE_TO_PERFORM_THAT_ACTION(162),
	// Message: You cannot destroy it because the number is incorrect.
	YOU_CANNOT_DESTROY_IT_BECAUSE_THE_NUMBER_IS_INCORRECT(163),
	// Message: Waiting for another reply.
	WAITING_FOR_ANOTHER_REPLY(164),
	// Message: You cannot add yourself to your own friend list.
	YOU_CANNOT_ADD_YOURSELF_TO_YOUR_OWN_FRIEND_LIST(165),
	// Message: Friend list is not ready yet. Please register again later.
	FRIEND_LIST_IS_NOT_READY_YET(166),
	// Message: $c1 is already on your friend list.
	C1_IS_ALREADY_ON_YOUR_FRIEND_LIST(167),
	// Message: $c1 has sent a friend request.
	C1_HAS_SENT_A_FRIEND_REQUEST(168),
	// Message: Accept friendship 0/1 (1 to accept, 0 to deny)
	ACCEPT_FRIENDSHIP_01_1_TO_ACCEPT_0_TO_DENY(169),
	// Message: The user who requested to become friends is not found in the game.
	THE_USER_WHO_REQUESTED_TO_BECOME_FRIENDS_IS_NOT_FOUND_IN_THE_GAME(170),
	// Message: $c1 is not on your friend list.
	C1_IS_NOT_ON_YOUR_FRIEND_LIST(171),
	// Message: You lack the funds needed to pay for this transaction.
	YOU_LACK_THE_FUNDS_NEEDED_TO_PAY_FOR_THIS_TRANSACTION(172),
	// Message: That skill has been de-activated as HP was fully recovered.
	THAT_SKILL_HAS_BEEN_DEACTIVATED_AS_HP_WAS_FULLY_RECOVERED(175),
	// Message: That person is in message refusal mode.
	THAT_PERSON_IS_IN_MESSAGE_REFUSAL_MODE(176),
	// Message: Message refusal mode.
	MESSAGE_REFUSAL_MODE(177),
	// Message: Message acceptance mode.
	MESSAGE_ACCEPTANCE_MODE(178),
	// Message: You cannot discard those items here.
	YOU_CANNOT_DISCARD_THOSE_ITEMS_HERE(179),
	// Message: Cannot see target.
	CANNOT_SEE_TARGET(181),
	// Message: Your clan has been created.
	YOUR_CLAN_HAS_BEEN_CREATED(189),
	// Message: You have failed to create a clan.
	YOU_HAVE_FAILED_TO_CREATE_A_CLAN(190),
	// Message: Clan member $s1 has been expelled.
	CLAN_MEMBER_S1_HAS_BEEN_EXPELLED(191),
	// Message: Clan has dispersed.
	CLAN_HAS_DISPERSED(193),
	// Message: Entered the clan.
	ENTERED_THE_CLAN(195),
	// Message: $s1 declined your clan invitation.
	S1_DECLINED_YOUR_CLAN_INVITATION(196),
	// Message: You have recently been dismissed from a clan.  You are not allowed to join another clan for 24-hours.
	YOU_HAVE_RECENTLY_BEEN_DISMISSED_FROM_A_CLAN(199),
	// Message: You have withdrawn from the party.
	YOU_HAVE_WITHDRAWN_FROM_THE_PARTY(200),
	// Message: $c1 was expelled from the party.
	C1_WAS_EXPELLED_FROM_THE_PARTY(201),
	// Message: You have been expelled from the party.
	YOU_HAVE_BEEN_EXPELLED_FROM_THE_PARTY(202),
	// Message: Incorrect name. Please try again.
	INCORRECT_NAME(204),
	// Message: You are not a clan member and cannot perform this action.
	YOU_ARE_NOT_A_CLAN_MEMBER_AND_CANNOT_PERFORM_THIS_ACTION(212),
	// Message: Not working. Please try again later.
	NOT_WORKING_PLEASE_TRY_AGAIN_LATER(213),
	// Message: Your title has been changed.
	YOUR_TITLE_HAS_BEEN_CHANGED(214),
	// Message: $s1 has joined the clan.
	S1_HAS_JOINED_THE_CLAN(222),
	// Message: $s1 has withdrawn from the clan.
	S1_HAS_WITHDRAWN_FROM_THE_CLAN(223),
	// Message: You do not meet the criteria in order to create a clan.
	YOU_DO_NOT_MEET_THE_CRITERIA_IN_ORDER_TO_CREATE_A_CLAN(229),
	// Message: You must wait 10 days before creating a new clan.
	YOU_MUST_WAIT_10_DAYS_BEFORE_CREATING_A_NEW_CLAN(230),
	// Message: After a clan member is dismissed from a clan, the clan must wait at least a day before accepting a new member.
	AFTER_A_CLAN_MEMBER_IS_DISMISSED_FROM_A_CLAN_THE_CLAN_MUST_WAIT_AT_LEAST_A_DAY_BEFORE_ACCEPTING_A_NEW_MEMBER(231),
	// Message: After leaving or having been dismissed from a clan, you must wait at least a day before joining another clan.
	AFTER_LEAVING_OR_HAVING_BEEN_DISMISSED_FROM_A_CLAN_YOU_MUST_WAIT_AT_LEAST_A_DAY_BEFORE_JOINING_ANOTHER_CLAN(232),
	// Message: The Academy/Royal Guard/Order of Knights is full and cannot accept new members at this time.
	THE_ACADEMYROYAL_GUARDORDER_OF_KNIGHTS_IS_FULL_AND_CANNOT_ACCEPT_NEW_MEMBERS_AT_THIS_TIME(233),
	// Message: The target must be a clan member.
	THE_TARGET_MUST_BE_A_CLAN_MEMBER(234),
	// Message: Only the clan leader is enabled.
	ONLY_THE_CLAN_LEADER_IS_ENABLED(236),
	// Message: Not joined in any clan.
	NOT_JOINED_IN_ANY_CLAN(238),
	// Message: A clan leader cannot withdraw from their own clan.
	A_CLAN_LEADER_CANNOT_WITHDRAW_FROM_THEIR_OWN_CLAN(239),
	// Message: Select target.
	SELECT_TARGET(242),
	// Message: Clan name is invalid.
	CLAN_NAME_IS_INVALID(261),
	// Message: Clan name's length is incorrect.
	CLAN_NAMES_LENGTH_IS_INCORRECT(262),
	// Message: You have already requested the dissolution of your clan.
	YOU_HAVE_ALREADY_REQUESTED_THE_DISSOLUTION_OF_YOUR_CLAN(263),
	// Message: You cannot dissolve a clan while engaged in a war.
	YOU_CANNOT_DISSOLVE_A_CLAN_WHILE_ENGAGED_IN_A_WAR(264),
	// Message: There are no requests to disperse.
	THERE_ARE_NO_REQUESTS_TO_DISPERSE(267),
	// Message: A player can only be granted a title if the clan is level 3 or above.
	A_PLAYER_CAN_ONLY_BE_GRANTED_A_TITLE_IF_THE_CLAN_IS_LEVEL_3_OR_ABOVE(271),
	// Message: A clan crest can only be registered when the clan's skill level is 3 or above.
	A_CLAN_CREST_CAN_ONLY_BE_REGISTERED_WHEN_THE_CLANS_SKILL_LEVEL_IS_3_OR_ABOVE(272),
	// Message: Your clan's level has increased.
	YOUR_CLANS_LEVEL_HAS_INCREASED(274),
	// Message: The clan has failed to increase its level.
	THE_CLAN_HAS_FAILED_TO_INCREASE_ITS_LEVEL(275),
	// Message: You do not have the necessary materials or prerequisites to learn this skill.
	YOU_DO_NOT_HAVE_THE_NECESSARY_MATERIALS_OR_PREREQUISITES_TO_LEARN_THIS_SKILL(276),
	// Message: You have earned $s1.
	YOU_HAVE_EARNED_S1_SKILL(277),
	// Message: You do not have enough SP to learn this skill.
	YOU_DO_NOT_HAVE_ENOUGH_SP_TO_LEARN_THIS_SKILL(278),
	// Message: You do not have enough adena.
	YOU_DO_NOT_HAVE_ENOUGH_ADENA(279),
	// Message: You have not deposited any items in your warehouse.
	YOU_HAVE_NOT_DEPOSITED_ANY_ITEMS_IN_YOUR_WAREHOUSE(282),
	// Message: You have entered a combat zone.
	YOU_HAVE_ENTERED_A_COMBAT_ZONE(283),
	// Message: You have left a combat zone.
	YOU_HAVE_LEFT_A_COMBAT_ZONE(284),
	// Message: Clan $s1 has successfully engraved the holy artifact!
	CLAN_S1_HAS_SUCCESSFULLY_ENGRAVED_THE_HOLY_ARTIFACT(285),
	// Message: Your base is being attacked.
	YOUR_BASE_IS_BEING_ATTACKED(286),
	// Message: The opposing clan has started to engrave the holy artifact!
	THE_OPPOSING_CLAN_HAS_STARTED_TO_ENGRAVE_THE_HOLY_ARTIFACT(287),
	// Message: The castle gate has been destroyed.
	THE_CASTLE_GATE_HAS_BEEN_DESTROYED(288),
	// Message: An outpost or headquarters cannot be built because one already exists.
	AN_OUTPOST_OR_HEADQUARTERS_CANNOT_BE_BUILT_BECAUSE_ONE_ALREADY_EXISTS(289),
	// Message: You cannot set up a base here.
	YOU_CANNOT_SET_UP_A_BASE_HERE(290),
	// Message: Clan $s1 is victorious over $s2's castle siege!
	CLAN_S1_IS_VICTORIOUS_OVER_S2S_CASTLE_SIEGE(291),
	// Message: $s1 has announced the next castle siege time.
	S1_HAS_ANNOUNCED_THE_NEXT_CASTLE_SIEGE_TIME(292),
	// Message: The registration term for $s1 has ended.
	THE_REGISTRATION_TERM_FOR_S1_HAS_ENDED(293),
	// Message: You cannot summon the encampment because you are not a member of the siege clan involved in the castle / fortress / hideout siege.
	YOU_CANNOT_SUMMON_THE_ENCAMPMENT_BECAUSE_YOU_ARE_NOT_A_MEMBER_OF_THE_SIEGE_CLAN_INVOLVED_IN_THE_CASTLE__FORTRESS__HIDEOUT_SIEGE(294),
	// Message: $s1's siege was canceled because there were no clans that participated.
	S1S_SIEGE_WAS_CANCELED_BECAUSE_THERE_WERE_NO_CLANS_THAT_PARTICIPATED(295),
	// Message: You received $s1 falling damage.
	YOU_RECEIVED_S1_FALLING_DAMAGE(296),
	// Message: You have taken $s1 damage because you were unable to breathe.
	YOU_HAVE_TAKEN_S1_DAMAGE_BECAUSE_YOU_WERE_UNABLE_TO_BREATHE(297),
	// Message: You have dropped $s1.
	YOU_HAVE_DROPPED_S1(298),
	// Message: $c1 has obtained $s3 $s2.
	C1_HAS_OBTAINED_S3_S2(299),
	// Message: $c1 has obtained $s2.
	C1_HAS_OBTAINED_S2(300),
	// Message: $s2 $s1 has disappeared.
	S2_S1_HAS_DISAPPEARED(301),
	// Message: $s1 has disappeared.
	S1_HAS_DISAPPEARED(302),
	// Message: Clan member $s1 has logged into game.
	CLAN_MEMBER_S1_HAS_LOGGED_INTO_GAME(304),
	// Message: The player declined to join your party.
	THE_PLAYER_DECLINED_TO_JOIN_YOUR_PARTY(305),
	// Message: This door cannot be unlocked.
	THIS_DOOR_CANNOT_BE_UNLOCKED(319),
	// Message: You have failed to unlock the door.
	YOU_HAVE_FAILED_TO_UNLOCK_THE_DOOR(320),
	// Message: It is not locked.
	IT_IS_NOT_LOCKED(321),
	// Message: Your force has increased to level $s1.
	YOUR_FORCE_HAS_INCREASED_TO_LEVEL_S1(323),
	// Message: Your force has reached maximum capacity.
	YOUR_FORCE_HAS_REACHED_MAXIMUM_CAPACITY(324),
	// Message: You have acquired $s1 SP.
	YOU_HAVE_ACQUIRED_S1_SP(331),
	// Message: The soulshot you are attempting to use does not match the grade of your equipped weapon.
	THE_SOULSHOT_YOU_ARE_ATTEMPTING_TO_USE_DOES_NOT_MATCH_THE_GRADE_OF_YOUR_EQUIPPED_WEAPON(337),
	// Message: You do not have enough soulshots for that.
	YOU_DO_NOT_HAVE_ENOUGH_SOULSHOTS_FOR_THAT(338),
	// Message: Cannot use soulshots.
	CANNOT_USE_SOULSHOTS(339),
	// Message: You do not have enough materials to perform that action.
	YOU_DO_NOT_HAVE_ENOUGH_MATERIALS_TO_PERFORM_THAT_ACTION(341),
	// Message: Your soulshots are enabled.
	YOUR_SOULSHOTS_ARE_ENABLED(342),
	// Message: Sweeper failed, target not spoiled.
	SWEEPER_FAILED_TARGET_NOT_SPOILED(343),
	// Message: Incorrect item count.
	INCORRECT_ITEM_COUNT(351),
	// Message: Inappropriate enchant conditions.
	INAPPROPRIATE_ENCHANT_CONDITIONS(355),
	// Message: It has already been spoiled.
	IT_HAS_ALREADY_BEEN_SPOILED(357),
	// Message: $s1 hour(s) until castle siege conclusion.
	S1_HOURS_UNTIL_CASTLE_SIEGE_CONCLUSION(358),
	// Message: $s1 minute(s) until castle siege conclusion.
	S1_MINUTES_UNTIL_CASTLE_SIEGE_CONCLUSION(359),
	// Message: This castle siege will end in $s1 second(s)!
	THIS_CASTLE_SIEGE_WILL_END_IN_S1_SECONDS(360),
	// Message: Over-hit!
	OVERHIT(361),
	// Message: You have acquired $s1 bonus experience from a successful over-hit.
	YOU_HAVE_ACQUIRED_S1_BONUS_EXPERIENCE_FROM_A_SUCCESSFUL_OVERHIT(362),
	// Message: Equipped +$s1 $s2.
	EQUIPPED_S1_S2(368),
	// Message: You have obtained a +$s1 $s2.
	YOU_HAVE_OBTAINED_A_S1_S2(369),
	// Message: Acquired +$s1 $s2.
	ACQUIRED_S1_S2(371),
	// Message: You have dropped +$s1 $s2.
	YOU_HAVE_DROPPED_S1_S2(375),
	// Message: $c1 has obtained +$s2$s3.
	C1_HAS_OBTAINED_S2S3(376),
	// Message: $S1 $S2 disappeared.
	S1_S2_DISAPPEARED(377),
	// Message: $c1 purchased $s2.
	C1_PURCHASED_S2(378),
	// Message: $c1 purchased +$s2$s3.
	C1_PURCHASED_S2S3(379),
	// Message: $c1 purchased $s3 $s2(s).
	C1_PURCHASED_S3_S2S(380),
	// Message: The game client encountered an error and was unable to connect to the petition server.
	THE_GAME_CLIENT_ENCOUNTERED_AN_ERROR_AND_WAS_UNABLE_TO_CONNECT_TO_THE_PETITION_SERVER(381),
	// Message: This ends the GM petition consultation. \\nPlease give us feedback on the petition service.
	THIS_ENDS_THE_GM_PETITION_CONSULTATION_NPLEASE_GIVE_US_FEEDBACK_ON_THE_PETITION_SERVICE(387),
	// Message: Not under petition consultation.
	NOT_UNDER_PETITION_CONSULTATION(388),
	// Message: Your petition application has been accepted. \\n - Receipt No. is $s1.
	YOUR_PETITION_APPLICATION_HAS_BEEN_ACCEPTED_N__RECEIPT_NO_IS_S1(389),
	// Message: You may only submit one petition (active) at a time.
	YOU_MAY_ONLY_SUBMIT_ONE_PETITION_ACTIVE_AT_A_TIME(390),
	// Message: Receipt No. $s1: petition cancelled.
	RECEIPT_NO_S1_PETITION_CANCELLED(391),
	// Message: Failed to cancel petition. Please try again later.
	FAILED_TO_CANCEL_PETITION(393),
	// Message: Starting petition consultation with $c1.
	STARTING_PETITION_CONSULTATION_WITH_C1(394),
	// Message: Ending petition consultation with $c1.
	ENDING_PETITION_CONSULTATION_WITH_C1(395),
	// Message: System error.
	SYSTEM_ERROR(399),
	// Message: You do not possess the correct ticket to board the boat.
	YOU_DO_NOT_POSSESS_THE_CORRECT_TICKET_TO_BOARD_THE_BOAT(402),
	// Message: Your Create Item level is too low to register this recipe.
	YOUR_CREATE_ITEM_LEVEL_IS_TOO_LOW_TO_REGISTER_THIS_RECIPE(404),
	// Message: Petition application accepted.
	PETITION_APPLICATION_ACCEPTED(406),
	// Message: Your petition is being processed.
	YOUR_PETITION_IS_BEING_PROCESSED(407),
	// Message: $s1 has been disarmed.
	S1_HAS_BEEN_DISARMED(417),
	// Message: Time expired.
	TIME_EXPIRED(420),
	// Message: Another person has logged in with the same account.
	ANOTHER_PERSON_HAS_LOGGED_IN_WITH_THE_SAME_ACCOUNT(421),
	// Message: You have exceeded the weight limit.
	YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT(422),
	// Message: Does not fit strengthening conditions of the scroll.
	DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL(424),
	// Message: This feature is only available to alliance leaders.
	THIS_FEATURE_IS_ONLY_AVAILABLE_TO_ALLIANCE_LEADERS(464),
	// Message: You are not currently allied with any clans.
	YOU_ARE_NOT_CURRENTLY_ALLIED_WITH_ANY_CLANS(465),
	// Message: You have exceeded the limit.
	YOU_HAVE_EXCEEDED_THE_LIMIT(466),
	// Message: A clan that has withdrawn or been expelled cannot enter into an alliance within one day of withdrawal or expulsion.
	A_CLAN_THAT_HAS_WITHDRAWN_OR_BEEN_EXPELLED_CANNOT_ENTER_INTO_AN_ALLIANCE_WITHIN_ONE_DAY_OF_WITHDRAWAL_OR_EXPULSION(468),
	// Message: You may not ally with a clan you are currently at war with.  That would be diabolical and treacherous.
	YOU_MAY_NOT_ALLY_WITH_A_CLAN_YOU_ARE_CURRENTLY_AT_WAR_WITH(469),
	// Message: Only the clan leader may apply for withdrawal from the alliance.
	ONLY_THE_CLAN_LEADER_MAY_APPLY_FOR_WITHDRAWAL_FROM_THE_ALLIANCE(470),
	// Message: Alliance leaders cannot withdraw.
	ALLIANCE_LEADERS_CANNOT_WITHDRAW(471),
	// Message: $s1 has joined as a friend.
	S1_HAS_JOINED_AS_A_FRIEND(479),
	// Message: $s1 has been deleted from your friends list.
	S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST(481),
	// Message: This function is inaccessible right now.  Please try again later.
	THIS_FUNCTION_IS_INACCESSIBLE_RIGHT_NOW(483),
	// Message: ======<Friends List>======
	FRIENDS_LIST(487),
	// Message: $s1 (Currently: Online)
	S1_CURRENTLY_ONLINE(488),
	// Message: $s1 (Currently: Offline)
	S1_CURRENTLY_OFFLINE(489),
	// Message: ========================
	ID490(490),
	// Message: =======<Alliance Information>=======
	ALLIANCE_INFORMATION(491),
	// Message: Alliance Name: $s1
	ALLIANCE_NAME_S1(492),
	// Message: Connection: $s1 / Total $s2
	CONNECTION_S1__TOTAL_S2(493),
	// Message: Alliance Leader: $s2 of $s1
	ALLIANCE_LEADER_S2_OF_S1(494),
	// Message: Affiliated clans: Total $s1 clan(s)
	AFFILIATED_CLANS_TOTAL_S1_CLANS(495),
	// Message: =====<Clan Information>=====
	CLAN_INFORMATION(496),
	// Message: Clan Name: $s1
	CLAN_NAME_S1(497),
	// Message: Clan Leader:  $s1
	CLAN_LEADER__S1(498),
	// Message: Clan Level: $s1
	CLAN_LEVEL_S1(499),
	// Message: ------------------------
	ID500(500),
	// Message: You already belong to another alliance.
	YOU_ALREADY_BELONG_TO_ANOTHER_ALLIANCE(502),
	// Message: Your friend $s1 just logged in.
	YOUR_FRIEND_S1_JUST_LOGGED_IN(503),
	// Message: Only clan leaders may create alliances.
	ONLY_CLAN_LEADERS_MAY_CREATE_ALLIANCES(504),
	// Message: You cannot create a new alliance within 1 day of dissolution.
	YOU_CANNOT_CREATE_A_NEW_ALLIANCE_WITHIN_1_DAY_OF_DISSOLUTION(505),
	// Message: Incorrect alliance name.  Please try again.
	INCORRECT_ALLIANCE_NAME__PLEASE_TRY_AGAIN(506),
	// Message: Incorrect length for an alliance name.
	INCORRECT_LENGTH_FOR_AN_ALLIANCE_NAME(507),
	// Message: That alliance name already exists.
	THAT_ALLIANCE_NAME_ALREADY_EXISTS(508),
	// Message: You have accepted the alliance.
	YOU_HAVE_ACCEPTED_THE_ALLIANCE(517),
	// Message: You have failed to invite a clan into the alliance.
	YOU_HAVE_FAILED_TO_INVITE_A_CLAN_INTO_THE_ALLIANCE(518),
	// Message: You have withdrawn from the alliance.
	YOU_HAVE_WITHDRAWN_FROM_THE_ALLIANCE(519),
	// Message: You have failed to withdraw from the alliance.
	YOU_HAVE_FAILED_TO_WITHDRAW_FROM_THE_ALLIANCE(520),
	// Message: The alliance has been dissolved.
	THE_ALLIANCE_HAS_BEEN_DISSOLVED(523),
	// Message: You have failed to dissolve the alliance.
	YOU_HAVE_FAILED_TO_DISSOLVE_THE_ALLIANCE(524),
	// Message: That person has been successfully added to your Friend List
	THAT_PERSON_HAS_BEEN_SUCCESSFULLY_ADDED_TO_YOUR_FRIEND_LIST(525),
	// Message: You have failed to add a friend to your friends list.
	YOU_HAVE_FAILED_TO_ADD_A_FRIEND_TO_YOUR_FRIENDS_LIST(526),
	// Message: $s1 leader, $s2, has requested an alliance.
	S1_LEADER_S2_HAS_REQUESTED_AN_ALLIANCE(527),
	// Message: Your Spiritshot does not match the weapon's grade.
	YOUR_SPIRITSHOT_DOES_NOT_MATCH_THE_WEAPONS_GRADE(530),
	// Message: You do not have enough Spiritshot for that.
	YOU_DO_NOT_HAVE_ENOUGH_SPIRITSHOT_FOR_THAT(531),
	// Message: You may not use Spiritshots.
	YOU_MAY_NOT_USE_SPIRITSHOTS(532),
	// Message: Your spiritshot has been enabled.
	YOUR_SPIRITSHOT_HAS_BEEN_ENABLED(533),
	// Message: Your SP has decreased by $s1.
	YOUR_SP_HAS_DECREASED_BY_S1(538),
	// Message: You already have a pet.
	YOU_ALREADY_HAVE_A_PET(543),
	// Message: Your pet cannot carry this item.
	YOUR_PET_CANNOT_CARRY_THIS_ITEM(544),
	// Message: Your pet cannot carry any more items. Remove some, then try again.
	YOUR_PET_CANNOT_CARRY_ANY_MORE_ITEMS(545),
	// Message: Your pet cannot carry any more items.
	YOUR_PET_CANNOT_CARRY_ANY_MORE_ITEMS_(546),
	// Message: Summoning your pet.
	SUMMONING_YOUR_PET(547),
	// Message: Your pet's name can be up to 8 characters in length.
	YOUR_PETS_NAME_CAN_BE_UP_TO_8_CHARACTERS_IN_LENGTH(548),
	// Message: To create an alliance, your clan must be Level 5 or higher.
	TO_CREATE_AN_ALLIANCE_YOUR_CLAN_MUST_BE_LEVEL_5_OR_HIGHER(549),
	// Message: As you are currently schedule for clan dissolution, no alliance can be created.
	AS_YOU_ARE_CURRENTLY_SCHEDULE_FOR_CLAN_DISSOLUTION_NO_ALLIANCE_CAN_BE_CREATED(550),
	// Message: As you are currently schedule for clan dissolution, your clan level cannot be increased.
	AS_YOU_ARE_CURRENTLY_SCHEDULE_FOR_CLAN_DISSOLUTION_YOUR_CLAN_LEVEL_CANNOT_BE_INCREASED(551),
	// Message: As you are currently schedule for clan dissolution, you cannot register or delete a Clan Crest.
	AS_YOU_ARE_CURRENTLY_SCHEDULE_FOR_CLAN_DISSOLUTION_YOU_CANNOT_REGISTER_OR_DELETE_A_CLAN_CREST(552),
	// Message: You cannot disperse the clans in your alliance.
	YOU_CANNOT_DISPERSE_THE_CLANS_IN_YOUR_ALLIANCE(554),
	// Message: As your pet is currently out, its summoning item cannot be destroyed.
	AS_YOUR_PET_IS_CURRENTLY_OUT_ITS_SUMMONING_ITEM_CANNOT_BE_DESTROYED(557),
	// Message: You may not crystallize this item. Your crystallization skill level is too low.
	YOU_MAY_NOT_CRYSTALLIZE_THIS_ITEM(562),
	// Message: Cubic Summoning failed.
	CUBIC_SUMMONING_FAILED(568),
	// Message: Pets and Servitors are not available at this time.
	PETS_AND_SERVITORS_ARE_NOT_AVAILABLE_AT_THIS_TIME(574),
	// Message: You cannot summon during a trade or while using a private store.
	YOU_CANNOT_SUMMON_DURING_A_TRADE_OR_WHILE_USING_A_PRIVATE_STORE(577),
	// Message: You cannot summon during combat.
	YOU_CANNOT_SUMMON_DURING_COMBAT(578),
	// Message: A pet cannot be unsummoned during battle.
	A_PET_CANNOT_BE_UNSUMMONED_DURING_BATTLE(579),
	// Message: You may not use multiple pets or servitors at the same time.
	YOU_MAY_NOT_USE_MULTIPLE_PETS_OR_SERVITORS_AT_THE_SAME_TIME(580),
	// Message: Dead pets cannot be returned to their summoning item.
	DEAD_PETS_CANNOT_BE_RETURNED_TO_THEIR_SUMMONING_ITEM(589),
	// Message: Your pet is dead and any attempt you make to give it something goes unrecognized.
	YOUR_PET_IS_DEAD_AND_ANY_ATTEMPT_YOU_MAKE_TO_GIVE_IT_SOMETHING_GOES_UNRECOGNIZED(590),
	// Message: An invalid character is included in the pet's name.
	AN_INVALID_CHARACTER_IS_INCLUDED_IN_THE_PETS_NAME(591),
	// Message: You may not restore a hungry pet.
	YOU_MAY_NOT_RESTORE_A_HUNGRY_PET(594),
	// Message: Your pet ate a little, but is still hungry.
	YOUR_PET_ATE_A_LITTLE_BUT_IS_STILL_HUNGRY(596),
	// Message: You may not equip a pet item.
	YOU_MAY_NOT_EQUIP_A_PET_ITEM(600),
	// Message: There are $S1 petitions currently on the waiting list.
	THERE_ARE_S1_PETITIONS_CURRENTLY_ON_THE_WAITING_LIST(601),
	// Message: The petition system is currently unavailable. Please try again later.
	THE_PETITION_SYSTEM_IS_CURRENTLY_UNAVAILABLE_PLEASE_TRY_AGAIN_LATER(602),
	// Message: You may not call forth a pet or summoned creature from this location.
	YOU_MAY_NOT_CALL_FORTH_A_PET_OR_SUMMONED_CREATURE_FROM_THIS_LOCATION(604),
	// Message: You can only enter up 128 names in your friends list.
	YOU_CAN_ONLY_ENTER_UP_128_NAMES_IN_YOUR_FRIENDS_LIST(605),
	// Message: The Friend's List of the person you are trying to add is full, so registration is not possible.
	THE_FRIENDS_LIST_OF_THE_PERSON_YOU_ARE_TRYING_TO_ADD_IS_FULL_SO_REGISTRATION_IS_NOT_POSSIBLE(606),
	// Message: You do not have any further skills to learn. Come back when you have reached Level $s1.
	YOU_DO_NOT_HAVE_ANY_FURTHER_SKILLS_TO_LEARN__COME_BACK_WHEN_YOU_HAVE_REACHED_LEVEL_S1(607),
	// Message: $c1 has obtained $s3 $s2 by using sweeper.
	C1_HAS_OBTAINED_S3_S2_BY_USING_SWEEPER(608),
	// Message: $c1 has obtained $s2 by using sweeper.
	C1_HAS_OBTAINED_S2_BY_USING_SWEEPER(609),
	// Message: The Spoil condition has been activated.
	THE_SPOIL_CONDITION_HAS_BEEN_ACTIVATED(612),
	// Message: ======<Ignore List>======
	IGNORE_LIST(613),
	// Message: You have failed to register the user to your Ignore List.
	YOU_HAVE_FAILED_TO_REGISTER_THE_USER_TO_YOUR_IGNORE_LIST(615),
	// Message: You have failed to delete the character.
	YOU_HAVE_FAILED_TO_DELETE_THE_CHARACTER_FROM_YOUR_IGNORE_LIST(616),
	// Message: $s1 has been added to your Ignore List.
	S1_HAS_BEEN_ADDED_TO_YOUR_IGNORE_LIST(617),
	// Message: $s1 has been removed from your Ignore List.
	S1_HAS_BEEN_REMOVED_FROM_YOUR_IGNORE_LIST(618),
	// Message: $s1 has placed you on his/her Ignore List.
	S1_HAS_PLACED_YOU_ON_HISHER_IGNORE_LIST(619),
	// Message: You have already requested a Castle Siege.
	YOU_HAVE_ALREADY_REQUESTED_A_CASTLE_SIEGE(638),
	// Message: You are already registered to the attacker side and must cancel your registration before submitting your request.
	YOU_ARE_ALREADY_REGISTERED_TO_THE_ATTACKER_SIDE_AND_MUST_CANCEL_YOUR_REGISTRATION_BEFORE_SUBMITTING_YOUR_REQUEST(642),
	// Message: You have already registered to the defender side and must cancel your registration before submitting your request.
	YOU_HAVE_ALREADY_REGISTERED_TO_THE_DEFENDER_SIDE_AND_MUST_CANCEL_YOUR_REGISTRATION_BEFORE_SUBMITTING_YOUR_REQUEST(643),
	// Message: You are not yet registered for the castle siege.
	YOU_ARE_NOT_YET_REGISTERED_FOR_THE_CASTLE_SIEGE(644),
	// Message: Only clans of level 5 or higher may register for a castle siege.
	ONLY_CLANS_OF_LEVEL_5_OR_HIGHER_MAY_REGISTER_FOR_A_CASTLE_SIEGE(645),
	// Message: You do not have the authority to modify the castle defender list.
	YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_MODIFY_THE_CASTLE_DEFENDER_LIST(646),
	// Message: You do not have the authority to modify the siege time.
	YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_MODIFY_THE_SIEGE_TIME(647),
	// Message: No more registrations may be accepted for the attacker side.
	NO_MORE_REGISTRATIONS_MAY_BE_ACCEPTED_FOR_THE_ATTACKER_SIDE(648),
	// Message: No more registrations may be accepted for the defender side.
	NO_MORE_REGISTRATIONS_MAY_BE_ACCEPTED_FOR_THE_DEFENDER_SIDE(649),
	// Message: You may not summon from your current location.
	YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION(650),
	// Message: You do not have the authority to position mercenaries.
	YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_POSITION_MERCENARIES(653),
	// Message: You do not have the authority to cancel mercenary positioning.
	YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_CANCEL_MERCENARY_POSITIONING(654),
	// Message: Mercenaries cannot be positioned here.
	MERCENARIES_CANNOT_BE_POSITIONED_HERE(655),
	// Message: This mercenary cannot be positioned anymore.
	THIS_MERCENARY_CANNOT_BE_POSITIONED_ANYMORE(656),
	// Message: Positioning cannot be done here because the distance between mercenaries is too short.
	POSITIONING_CANNOT_BE_DONE_HERE_BECAUSE_THE_DISTANCE_BETWEEN_MERCENARIES_IS_TOO_SHORT(657),
	// Message: This is not a mercenary of a castle that you own and so you cannot cancel its positioning.
	THIS_IS_NOT_A_MERCENARY_OF_A_CASTLE_THAT_YOU_OWN_AND_SO_YOU_CANNOT_CANCEL_ITS_POSITIONING(658),
	// Message: This is not the time for siege registration and so registrations cannot be accepted or rejected.
	THIS_IS_NOT_THE_TIME_FOR_SIEGE_REGISTRATION_AND_SO_REGISTRATIONS_CANNOT_BE_ACCEPTED_OR_REJECTED(659),
	// Message: This is not the time for siege registration and so registration and cancellation cannot be done.
	THIS_IS_NOT_THE_TIME_FOR_SIEGE_REGISTRATION_AND_SO_REGISTRATION_AND_CANCELLATION_CANNOT_BE_DONE(660),
	// Message: $s1 adena disappeared.
	S1_ADENA_DISAPPEARED(672),
	// Message: Only a clan leader whose clan is of level 2 or higher is allowed to participate in a clan hall auction.
	ONLY_A_CLAN_LEADER_WHOSE_CLAN_IS_OF_LEVEL_2_OR_HIGHER_IS_ALLOWED_TO_PARTICIPATE_IN_A_CLAN_HALL_AUCTION(673),
	// Message: It has not yet been seven days since canceling an auction.
	IT_HAS_NOT_YET_BEEN_SEVEN_DAYS_SINCE_CANCELING_AN_AUCTION(674),
	// Message: There are no clan halls up for auction.
	THERE_ARE_NO_CLAN_HALLS_UP_FOR_AUCTION(675),
	// Message: Since you have already submitted a bid, you are not allowed to participate in another auction at this time.
	SINCE_YOU_HAVE_ALREADY_SUBMITTED_A_BID_YOU_ARE_NOT_ALLOWED_TO_PARTICIPATE_IN_ANOTHER_AUCTION_AT_THIS_TIME(676),
	// Message: Your bid price must be higher than the minimum price currently being bid.
	YOUR_BID_PRICE_MUST_BE_HIGHER_THAN_THE_MINIMUM_PRICE_CURRENTLY_BEING_BID(677),
	// Message: You have submitted a bid for the auction of $s1.
	YOU_HAVE_SUBMITTED_A_BID_FOR_THE_AUCTION_OF_S1(678),
	// Message: You have canceled your bid.
	YOU_HAVE_CANCELED_YOUR_BID(679),
	// Message: There are no priority rights on a sweeper.
	THERE_ARE_NO_PRIORITY_RIGHTS_ON_A_SWEEPER(683),
	// Message: You have received $s1 damage from the fire of magic.
	YOU_HAVE_RECEIVED_S1_DAMAGE_FROM_THE_FIRE_OF_MAGIC(686),
	// Message: You cannot move while frozen. Please wait.
	YOU_CANNOT_MOVE_WHILE_FROZEN(687),
	// Message: Castle-owning clans are automatically registered on the defending side.
	CASTLE_OWNING_CLANS_ARE_AUTOMATICALLY_REGISTERED_ON_THE_DEFENDING_SIDE(688),
	// Message: A clan that owns a castle cannot participate in another siege.
	A_CLAN_THAT_OWNS_A_CASTLE_CANNOT_PARTICIPATE_IN_ANOTHER_SIEGE(689),
	// Message: You cannot register as an attacker because you are in an alliance with the castle-owning clan.
	YOU_CANNOT_REGISTER_AS_AN_ATTACKER_BECAUSE_YOU_ARE_IN_AN_ALLIANCE_WITH_THE_CASTLE_OWNING_CLAN(690),
	// Message: $s1 clan is already a member of $s2 alliance.
	S1_CLAN_IS_ALREADY_A_MEMBER_OF_S2_ALLIANCE(691),
	// Message: The other party is frozen. Please wait a moment.
	THE_OTHER_PARTY_IS_FROZEN(692),
	// Message: No packages have arrived.
	NO_PACKAGES_HAVE_ARRIVED(694),
	// Message: You do not have enough required items.
	YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS(701),
	// Message: There are no GMs currently visible in the public list as they may be performing other functions at the moment.
	THERE_ARE_NO_GMS_CURRENTLY_VISIBLE_IN_THE_PUBLIC_LIST_AS_THEY_MAY_BE_PERFORMING_OTHER_FUNCTIONS_AT_THE_MOMENT(702),
	// Message: ======<GM List>======
	GM_LIST(703),
	// Message: GM : $c1
	GM__C1(704),
	// Message: You cannot teleport to a village that is in a siege.
	YOU_CANNOT_TELEPORT_TO_A_VILLAGE_THAT_IS_IN_A_SIEGE(707),
	// Message: You do not have the right to use the clan warehouse.
	YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_THE_CLAN_WAREHOUSE(709),
	// Message: Only clans of clan level 1 or higher can use a clan warehouse.
	ONLY_CLANS_OF_CLAN_LEVEL_1_OR_HIGHER_CAN_USE_A_CLAN_WAREHOUSE(710),
	// Message: If a base camp does not exist, resurrection is not possible.
	IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE(716),
	// Message: The guardian tower has been destroyed and resurrection is not possible.
	THE_GUARDIAN_TOWER_HAS_BEEN_DESTROYED_AND_RESURRECTION_IS_NOT_POSSIBLE(717),
	// Message: The purchase price is higher than the amount of money that you have and so you cannot open a personal store.
	THE_PURCHASE_PRICE_IS_HIGHER_THAN_THE_AMOUNT_OF_MONEY_THAT_YOU_HAVE_AND_SO_YOU_CANNOT_OPEN_A_PERSONAL_STORE(720),
	// Message: You cannot apply for dissolution again within seven days after a previous application for dissolution.
	YOU_CANNOT_APPLY_FOR_DISSOLUTION_AGAIN_WITHIN_SEVEN_DAYS_AFTER_A_PREVIOUS_APPLICATION_FOR_DISSOLUTION(728),
	// Message: That item cannot be discarded.
	THAT_ITEM_CANNOT_BE_DISCARDED(729),
	// Message: You have submitted $s1 petition(s). \\n - You may submit $s2 more petition(s) today.
	YOU_HAVE_SUBMITTED_S1_PETITIONS_N__YOU_MAY_SUBMIT_S2_MORE_PETITIONS_TODAY(730),
	// Message: We have received $s1 petitions from you today and that is the maximum that you can submit in one day. You cannot submit any more petitions.
	WE_HAVE_RECEIVED_S1_PETITIONS_FROM_YOU_TODAY_AND_THAT_IS_THE_MAXIMUM_THAT_YOU_CAN_SUBMIT_IN_ONE_DAY_YOU_CANNOT_SUBMIT_ANY_MORE_PETITIONS(733),
	// Message: The petition was canceled. You may submit $s1 more petition(s) today.
	THE_PETITION_WAS_CANCELED_YOU_MAY_SUBMIT_S1_MORE_PETITIONS_TODAY(736),
	// Message: You have not submitted a petition.
	YOU_HAVE_NOT_SUBMITTED_A_PETITION(738),
	// Message: You are currently not in a petition chat.
	YOU_ARE_CURRENTLY_NOT_IN_A_PETITION_CHAT(745),
	// Message: The effect of $s1 has been removed.
	THE_EFFECT_OF_S1_HAS_BEEN_REMOVED(749),
	// Message: There are no other skills to learn.
	THERE_ARE_NO_OTHER_SKILLS_TO_LEARN(750),
	// Message: You cannot position mercenaries here.
	YOU_CANNOT_POSITION_MERCENARIES_HERE(753),
	// Message: $c1 cannot join the clan because one day has not yet passed since they left another clan.
	C1_CANNOT_JOIN_THE_CLAN_BECAUSE_ONE_DAY_HAS_NOT_YET_PASSED_SINCE_THEY_LEFT_ANOTHER_CLAN(760),
	// Message: $s1 clan cannot join the alliance because one day has not yet passed since they left another alliance.
	S1_CLAN_CANNOT_JOIN_THE_ALLIANCE_BECAUSE_ONE_DAY_HAS_NOT_YET_PASSED_SINCE_THEY_LEFT_ANOTHER_ALLIANCE(761),
	// Message: You have been playing for an extended period of time. Please consider taking a break.
	YOU_HAVE_BEEN_PLAYING_FOR_AN_EXTENDED_PERIOD_OF_TIME_PLEASE_CONSIDER_TAKING_A_BREAK_S1(764),
	// Message: The clan hall which was put up for auction has been awarded to $s1 clan.
	THE_CLAN_HALL_WHICH_WAS_PUT_UP_FOR_AUCTION_HAS_BEEN_AWARDED_TO_S1_CLAN(776),
	// Message: The clan hall which had been put up for auction was not sold and therefore has been re-listed.
	THE_CLAN_HALL_WHICH_HAD_BEEN_PUT_UP_FOR_AUCTION_WAS_NOT_SOLD_AND_THEREFORE_HAS_BEEN_RELISTED(777),
	// Message: Observation is only possible during a siege.
	OBSERVATION_IS_ONLY_POSSIBLE_DURING_A_SIEGE(780),
	// Message: Observers cannot participate.
	OBSERVERS_CANNOT_PARTICIPATE(781),
	// Message: Lottery ticket sales have been temporarily suspended.
	LOTTERY_TICKET_SALES_HAVE_BEEN_TEMPORARILY_SUSPENDED(783),
	// Message: Tickets for the current lottery are no longer available.
	TICKETS_FOR_THE_CURRENT_LOTTERY_ARE_NO_LONGER_AVAILABLE(784),
	// Message: The tryouts are finished.
	THE_TRYOUTS_ARE_FINISHED(787),
	// Message: The finals are finished.
	THE_FINALS_ARE_FINISHED(788),
	// Message: The tryouts have begun.
	THE_TRYOUTS_HAVE_BEGUN(789),
	// Message: The finals have begun.
	THE_FINALS_HAVE_BEGUN(790),
	// Message: The final match is about to begin. Line up!
	THE_FINAL_MATCH_IS_ABOUT_TO_BEGIN(791),
	// Message: You are not authorized to do that.
	YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT(794),
	// Message: You may create up to 48 macros.
	YOU_MAY_CREATE_UP_TO_48_MACROS(797),
	// Message: You are too late. The registration period is over.
	YOU_ARE_TOO_LATE_THE_REGISTRATION_PERIOD_IS_OVER(800),
	// Message: The tryouts are about to begin. Line up!
	THE_TRYOUTS_ARE_ABOUT_TO_BEGIN(815),
	// Message: Tickets are now available for Monster Race $s1!
	TICKETS_ARE_NOW_AVAILABLE_FOR_MONSTER_RACE_S1(816),
	// Message: Now selling tickets for Monster Race $s1!
	NOW_SELLING_TICKETS_FOR_MONSTER_RACE_S1(817),
	// Message: Ticket sales for the Monster Race will end in $s1 minute(s).
	TICKET_SALES_FOR_THE_MONSTER_RACE_WILL_END_IN_S1_MINUTES(818),
	// Message: Tickets sales are closed for Monster Race $s1. Odds are posted.
	TICKETS_SALES_ARE_CLOSED_FOR_MONSTER_RACE_S1_ODDS_ARE_POSTED(819),
	// Message: Monster Race $s2 will begin in $s1 minute(s)!
	MONSTER_RACE_S2_WILL_BEGIN_IN_S1_MINUTES(820),
	// Message: Monster Race $s1 will begin in 30 seconds!
	MONSTER_RACE_S1_WILL_BEGIN_IN_30_SECONDS(821),
	// Message: Monster Race $s1 is about to begin! Countdown in five seconds!
	MONSTER_RACE_S1_IS_ABOUT_TO_BEGIN_COUNTDOWN_IN_FIVE_SECONDS(822),
	// Message: The race will begin in $s1 second(s)!
	THE_RACE_WILL_BEGIN_IN_S1_SECONDS(823),
	// Message: They're off!
	THEYRE_OFF(824),
	// Message: Monster Race $s1 is finished!
	MONSTER_RACE_S1_IS_FINISHED(825),
	// Message: First prize goes to the player in lane $s1. Second prize goes to the player in lane $s2.
	FIRST_PRIZE_GOES_TO_THE_PLAYER_IN_LANE_S1_SECOND_PRIZE_GOES_TO_THE_PLAYER_IN_LANE_S2(826),
	// Message: You may not impose a block on a GM.
	YOU_MAY_NOT_IMPOSE_A_BLOCK_ON_A_GM(827),
	// Message: You cannot recommend yourself.
	YOU_CANNOT_RECOMMEND_YOURSELF(829),
	// Message: You have recommended $c1. You have $s2 recommendations left.
	YOU_HAVE_RECOMMENDED_C1_YOU_HAVE_S2_RECOMMENDATIONS_LEFT(830),
	// Message: You have been recommended by $c1.
	YOU_HAVE_BEEN_RECOMMENDED_BY_C1(831),
	// Message: That character has already been recommended.
	THAT_CHARACTER_HAS_ALREADY_BEEN_RECOMMENDED(832),
	// Message: You are not authorized to make further recommendations at this time. You will receive more recommendation credits each day at 1 p.m.
	YOU_ARE_NOT_AUTHORIZED_TO_MAKE_FURTHER_RECOMMENDATIONS_AT_THIS_TIME(833),
	// Message: $c1 has rolled a $s2.
	C1_HAS_ROLLED_A_S2(834),
	// Message: You may not throw the dice at this time. Try again later.
	YOU_MAY_NOT_THROW_THE_DICE_AT_THIS_TIME_TRY_AGAIN_LATER(835),
	// Message: Macro descriptions may contain up to 32 characters.
	MACRO_DESCRIPTIONS_MAY_CONTAIN_UP_TO_32_CHARACTERS(837),
	// Message: Enter the name of the macro.
	ENTER_THE_NAME_OF_THE_MACRO(838),
	// Message: That recipe is already registered.
	THAT_RECIPE_IS_ALREADY_REGISTERED(840),
	// Message: No further recipes may be registered.
	NO_FURTHER_RECIPES_MAY_BE_REGISTERED(841),
	// Message: You are not authorized to register a recipe.
	YOU_ARE_NOT_AUTHORIZED_TO_REGISTER_A_RECIPE(842),
	// Message: The siege of $s1 is finished.
	THE_SIEGE_OF_S1_IS_FINISHED(843),
	// Message: The siege to conquer $s1 has begun.
	THE_SIEGE_TO_CONQUER_S1_HAS_BEGUN(844),
	// Message: The deadline to register for the siege of $s1 has passed.
	THE_DEADLINE_TO_REGISTER_FOR_THE_SIEGE_OF_S1_HAS_PASSED(845),
	// Message: The siege of $s1 has been canceled due to lack of interest.
	THE_SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST(846),
	// Message: A clan that owns a clan hall may not participate in a clan hall siege.
	A_CLAN_THAT_OWNS_A_CLAN_HALL_MAY_NOT_PARTICIPATE_IN_A_CLAN_HALL_SIEGE(847),
	// Message: $s1 has been added.
	S1_HAS_BEEN_ADDED(851),
	// Message: The recipe is incorrect.
	THE_RECIPE_IS_INCORRECT(852),
	// Message: $s1 clan has defeated $s2.
	S1_CLAN_HAS_DEFEATED_S2(855),
	// Message: The siege of $s1 has ended in a draw.
	THE_SIEGE_OF_S1_HAS_ENDED_IN_A_DRAW(856),
	// Message: The preliminary match of $s1 has ended in a draw.
	THE_PRELIMINARY_MATCH_OF_S1_HAS_ENDED_IN_A_DRAW(858),
	// Message: Please register a recipe.
	PLEASE_REGISTER_A_RECIPE(859),
	// Message: The seed has been sown.
	THE_SEED_HAS_BEEN_SOWN(871),
	// Message: This seed may not be sown here.
	THIS_SEED_MAY_NOT_BE_SOWN_HERE(872),
	// Message: The symbol has been added.
	THE_SYMBOL_HAS_BEEN_ADDED(877),
	// Message: The symbol has been deleted.
	THE_SYMBOL_HAS_BEEN_DELETED(878),
	// Message: The manor system is currently under maintenance.
	THE_MANOR_SYSTEM_IS_CURRENTLY_UNDER_MAINTENANCE(879),
	// Message: The transaction is complete.
	THE_TRANSACTION_IS_COMPLETE(880),
	// Message: The manor information has been updated.
	THE_MANOR_INFORMATION_HAS_BEEN_UPDATED(884),
	// Message: The seed was successfully sown.
	THE_SEED_WAS_SUCCESSFULLY_SOWN(889),
	// Message: The seed was not sown.
	THE_SEED_WAS_NOT_SOWN(890),
	// Message: You are not authorized to harvest.
	YOU_ARE_NOT_AUTHORIZED_TO_HARVEST(891),
	// Message: The harvest has failed.
	THE_HARVEST_HAS_FAILED(892),
	// Message: The harvest failed because the seed was not sown.
	THE_HARVEST_FAILED_BECAUSE_THE_SEED_WAS_NOT_SOWN(893),
	// Message: The symbol cannot be drawn.
	THE_SYMBOL_CANNOT_BE_DRAWN(899),
	// Message: No slot exists to draw the symbol.
	NO_SLOT_EXISTS_TO_DRAW_THE_SYMBOL(900),
	// Message: Current location : $s1, $s2, $s3 (Near Talking Island Village)
	CURRENT_LOCATION__S1_S2_S3_NEAR_TALKING_ISLAND_VILLAGE(910),
	// Message: Current location : $s1, $s2, $s3 (Near Gludin Village)
	CURRENT_LOCATION__S1_S2_S3_NEAR_GLUDIN_VILLAGE(911),
	// Message: Current location : $s1, $s2, $s3 (Near the Town of Gludio)
	CURRENT_LOCATION__S1_S2_S3_NEAR_THE_TOWN_OF_GLUDIO(912),
	// Message: Current location : $s1, $s2, $s3 (Near the Neutral Zone)
	CURRENT_LOCATION__S1_S2_S3_NEAR_THE_NEUTRAL_ZONE(913),
	// Message: Current location : $s1, $s2, $s3 (Near the Elven Village)
	CURRENT_LOCATION__S1_S2_S3_NEAR_THE_ELVEN_VILLAGE(914),
	// Message: Current location : $s1, $s2, $s3 (Near the Dark Elf Village)
	CURRENT_LOCATION__S1_S2_S3_NEAR_THE_DARK_ELF_VILLAGE(915),
	// Message: Current location : $s1, $s2, $s3 (Near the Town of Dion)
	CURRENT_LOCATION__S1_S2_S3_NEAR_THE_TOWN_OF_DION(916),
	// Message: Current location : $s1, $s2, $s3 (Near the Floran Village)
	CURRENT_LOCATION__S1_S2_S3_NEAR_THE_FLORAN_VILLAGE(917),
	// Message: Current location : $s1, $s2, $s3 (Near the Town of Giran)
	CURRENT_LOCATION__S1_S2_S3_NEAR_THE_TOWN_OF_GIRAN(918),
	// Message: Current location : $s1, $s2, $s3 (Near Giran Harbor)
	CURRENT_LOCATION__S1_S2_S3_NEAR_GIRAN_HARBOR(919),
	// Message: Current location : $s1, $s2, $s3 (Near the Orc Village)
	CURRENT_LOCATION__S1_S2_S3_NEAR_THE_ORC_VILLAGE(920),
	// Message: Current location : $s1, $s2, $s3 (Near the Dwarven Village)
	CURRENT_LOCATION__S1_S2_S3_NEAR_THE_DWARVEN_VILLAGE(921),
	// Message: Current location : $s1, $s2, $s3 (Near the Town of Oren)
	CURRENT_LOCATION__S1_S2_S3_NEAR_THE_TOWN_OF_OREN(922),
	// Message: Current location : $s1, $s2, $s3 (Near Hunters Village)
	CURRENT_LOCATION__S1_S2_S3_NEAR_HUNTERS_VILLAGE(923),
	// Message: Current location : $s1, $s2, $s3 (Near Aden Castle Town)
	CURRENT_LOCATION__S1_S2_S3_NEAR_ADEN_CASTLE_TOWN(924),
	// Message: Current location : $s1, $s2, $s3 (Near the Coliseum)
	CURRENT_LOCATION__S1_S2_S3_NEAR_THE_COLISEUM(925),
	// Message: Current location : $s1, $s2, $s3 (Near Heine)
	CURRENT_LOCATION__S1_S2_S3_NEAR_HEINE(926),
	// Message: The current time is $s1:$s2.
	THE_CURRENT_TIME_IS_S1S2(927),
	// Message: The current time is $s1:$s2.
	ID928_THE_CURRENT_TIME_IS_S1S2(928),
	// Message: Lottery tickets are not currently being sold.
	LOTTERY_TICKETS_ARE_NOT_CURRENTLY_BEING_SOLD(930),
	// Message: You cannot chat while in observation mode.
	YOU_CANNOT_CHAT_WHILE_IN_OBSERVATION_MODE(932),
	// Message: You do not have enough funds in the clan warehouse for the Manor to operate.
	YOU_DO_NOT_HAVE_ENOUGH_FUNDS_IN_THE_CLAN_WAREHOUSE_FOR_THE_MANOR_TO_OPERATE(935),
	// Message: The community server is currently offline.
	THE_COMMUNITY_SERVER_IS_CURRENTLY_OFFLINE(938),
	// Message: You failed to manufacture $s1.
	YOU_FAILED_TO_MANUFACTURE_S1(960),
	// Message: You are now blocking everything.
	YOU_ARE_NOW_BLOCKING_EVERYTHING(961),
	// Message: You are no longer blocking everything.
	YOU_ARE_NO_LONGER_BLOCKING_EVERYTHING(962),
	// Message: Chatting is currently prohibited. If you try to chat before the prohibition is removed, the prohibition time will increase even further.
	CHATTING_IS_CURRENTLY_PROHIBITED_IF_YOU_TRY_TO_CHAT_BEFORE_THE_PROHIBITION_IS_REMOVED_THE_PROHIBITION_TIME_WILL_INCREASE_EVEN_FURTHER(966),
	// Message: Petitions cannot exceed 255 characters.
	PETITIONS_CANNOT_EXCEED_255_CHARACTERS(971),
	// Message: The soul crystal succeeded in absorbing a soul.
	THE_SOUL_CRYSTAL_SUCCEEDED_IN_ABSORBING_A_SOUL(974),
	// Message: The soul crystal was not able to absorb the soul.
	THE_SOUL_CRYSTAL_WAS_NOT_ABLE_TO_ABSORB_THE_SOUL(975),
	// Message: The soul crystal broke because it was not able to endure the soul energy.
	THE_SOUL_CRYSTAL_BROKE_BECAUSE_IT_WAS_NOT_ABLE_TO_ENDURE_THE_SOUL_ENERGY(976),
	// Message: The soul crystal caused resonation and failed at absorbing a soul.
	THE_SOUL_CRYSTAL_CAUSED_RESONATION_AND_FAILED_AT_ABSORBING_A_SOUL(977),
	// Message: The soul crystal is refusing to absorb the soul.
	THE_SOUL_CRYSTAL_IS_REFUSING_TO_ABSORB_THE_SOUL(978),
	// Message: The ferry has arrived at Talking Island Harbor.
	THE_FERRY_HAS_ARRIVED_AT_TALKING_ISLAND_HARBOR(979),
	// Message: The ferry will leave for Gludin Harbor after anchoring for ten minutes.
	THE_FERRY_WILL_LEAVE_FOR_GLUDIN_HARBOR_AFTER_ANCHORING_FOR_TEN_MINUTES(980),
	// Message: The ferry will leave for Gludin Harbor in five minutes.
	THE_FERRY_WILL_LEAVE_FOR_GLUDIN_HARBOR_IN_FIVE_MINUTES(981),
	// Message: The ferry will leave for Gludin Harbor in one minute.
	THE_FERRY_WILL_LEAVE_FOR_GLUDIN_HARBOR_IN_ONE_MINUTE(982),
	// Message: Those wishing to ride the ferry should make haste to get on.
	THOSE_WISHING_TO_RIDE_THE_FERRY_SHOULD_MAKE_HASTE_TO_GET_ON(983),
	// Message: The ferry will be leaving soon for Gludin Harbor.
	THE_FERRY_WILL_BE_LEAVING_SOON_FOR_GLUDIN_HARBOR(984),
	// Message: The ferry is leaving for Gludin Harbor.
	THE_FERRY_IS_LEAVING_FOR_GLUDIN_HARBOR(985),
	// Message: The ferry has arrived at Gludin Harbor.
	THE_FERRY_HAS_ARRIVED_AT_GLUDIN_HARBOR(986),
	// Message: The ferry will leave for Talking Island Harbor after anchoring for ten minutes.
	THE_FERRY_WILL_LEAVE_FOR_TALKING_ISLAND_HARBOR_AFTER_ANCHORING_FOR_TEN_MINUTES(987),
	// Message: The ferry will leave for Talking Island Harbor in five minutes.
	THE_FERRY_WILL_LEAVE_FOR_TALKING_ISLAND_HARBOR_IN_FIVE_MINUTES(988),
	// Message: The ferry will leave for Talking Island Harbor in one minute.
	THE_FERRY_WILL_LEAVE_FOR_TALKING_ISLAND_HARBOR_IN_ONE_MINUTE(989),
	// Message: The ferry will be leaving soon for Talking Island Harbor.
	THE_FERRY_WILL_BE_LEAVING_SOON_FOR_TALKING_ISLAND_HARBOR(990),
	// Message: The ferry is leaving for Talking Island Harbor.
	THE_FERRY_IS_LEAVING_FOR_TALKING_ISLAND_HARBOR(991),
	// Message: The ferry has arrived at Giran Harbor.
	THE_FERRY_HAS_ARRIVED_AT_GIRAN_HARBOR(992),
	// Message: The ferry will leave for Giran Harbor after anchoring for ten minutes.
	THE_FERRY_WILL_LEAVE_FOR_GIRAN_HARBOR_AFTER_ANCHORING_FOR_TEN_MINUTES(993),
	// Message: The ferry will leave for Giran Harbor in five minutes.
	THE_FERRY_WILL_LEAVE_FOR_GIRAN_HARBOR_IN_FIVE_MINUTES(994),
	// Message: The ferry will leave for Giran Harbor in one minute.
	THE_FERRY_WILL_LEAVE_FOR_GIRAN_HARBOR_IN_ONE_MINUTE(995),
	// Message: The ferry will be leaving soon for Giran Harbor.
	THE_FERRY_WILL_BE_LEAVING_SOON_FOR_GIRAN_HARBOR(996),
	// Message: The ferry is leaving for Giran Harbor.
	THE_FERRY_IS_LEAVING_FOR_GIRAN_HARBOR(997),
	// Message: The Innadril pleasure boat has arrived. It will anchor for ten minutes.
	THE_INNADRIL_PLEASURE_BOAT_HAS_ARRIVED(998),
	// Message: The Innadril pleasure boat will leave in five minutes.
	THE_INNADRIL_PLEASURE_BOAT_WILL_LEAVE_IN_FIVE_MINUTES(999),
	// Message: The Innadril pleasure boat will leave in one minute.
	THE_INNADRIL_PLEASURE_BOAT_WILL_LEAVE_IN_ONE_MINUTE(1000),
	// Message: The Innadril pleasure boat will be leaving soon.
	THE_INNADRIL_PLEASURE_BOAT_WILL_BE_LEAVING_SOON(1001),
	// Message: The Innadril pleasure boat is leaving.
	THE_INNADRIL_PLEASURE_BOAT_IS_LEAVING(1002),
	// Message: Cannot process a monster race ticket.
	// Message: You have registered for a clan hall auction.
	YOU_HAVE_REGISTERED_FOR_A_CLAN_HALL_AUCTION(1004),
	// Message: There is not enough adena in the clan hall warehouse.
	THERE_IS_NOT_ENOUGH_ADENA_IN_THE_CLAN_HALL_WAREHOUSE(1005),
	// Message: Your bid has been successfully placed.
	YOUR_BID_HAS_BEEN_SUCCESSFULLY_PLACED(1006),
	// Message: A hungry strider cannot be mounted or dismounted.
	A_HUNGRY_STRIDER_CANNOT_BE_MOUNTED_OR_DISMOUNTED(1008),
	// Message: A strider cannot be ridden when dead.
	A_STRIDER_CANNOT_BE_RIDDEN_WHEN_DEAD(1009),
	// Message: A dead strider cannot be ridden.
	A_DEAD_STRIDER_CANNOT_BE_RIDDEN(1010),
	// Message: A strider in battle cannot be ridden.
	A_STRIDER_IN_BATTLE_CANNOT_BE_RIDDEN(1011),
	// Message: A strider cannot be ridden while in battle.
	A_STRIDER_CANNOT_BE_RIDDEN_WHILE_IN_BATTLE(1012),
	// Message: A strider can be ridden only when standing.
	A_STRIDER_CAN_BE_RIDDEN_ONLY_WHEN_STANDING(1013),
	// Message: Your pet gained $s1 experience points.
	YOUR_PET_GAINED_S1_EXPERIENCE_POINTS(1014),
	// Message: Your pet hit for $s1 damage.
	YOUR_PET_HIT_FOR_S1_DAMAGE(1015),
	// Message: Your pet received $s2 damage by $c1.
	YOUR_PET_RECEIVED_S2_DAMAGE_BY_C1(1016),
	// Message: Pet's critical hit!
	PETS_CRITICAL_HIT(1017),
	// Message: Summoned monster's critical hit!
	SUMMONED_MONSTERS_CRITICAL_HIT(1028),
	// Message: A summoned monster uses $s1.
	A_SUMMONED_MONSTER_USES_S1(1029),
	// Message: <Party Information>
	PARTY_INFORMATION(1030),
	// Message: Looting method: Finders keepers
	LOOTING_METHOD_FINDERS_KEEPERS(1031),
	// Message: Looting method: Random
	LOOTING_METHOD_RANDOM(1032),
	// Message: Looting method: Random including spoil
	LOOTING_METHOD_RANDOM_INCLUDING_SPOIL(1033),
	// Message: Looting method: By turn
	LOOTING_METHOD_BY_TURN(1034),
	// Message: Looting method: By turn including spoil
	LOOTING_METHOD_BY_TURN_INCLUDING_SPOIL(1035),
	// Message: You have exceeded the quantity that can be inputted.
	YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED(1036),
	// Message: Items left at the clan hall warehouse can only be retrieved by the clan leader. Do you want to continue?
	ITEMS_LEFT_AT_THE_CLAN_HALL_WAREHOUSE_CAN_ONLY_BE_RETRIEVED_BY_THE_CLAN_LEADER_DO_YOU_WANT_TO_CONTINUE(1039),
	// Message: Monster race payout information is not available while tickets are being sold.
	MONSTER_RACE_PAYOUT_INFORMATION_IS_NOT_AVAILABLE_WHILE_TICKETS_ARE_BEING_SOLD(1044),
	// Message: Monster race tickets are no longer available.
	MONSTER_RACE_TICKETS_ARE_NO_LONGER_AVAILABLE(1046),
	// Message: There are no communities in my clan. Clan communities are allowed for clans with skill levels of 2 and higher.
	THERE_ARE_NO_COMMUNITIES_IN_MY_CLAN_CLAN_COMMUNITIES_ARE_ALLOWED_FOR_CLANS_WITH_SKILL_LEVELS_OF_2_AND_HIGHER(1050),
	// Message: Payment for your clan hall has not been made. Please make payment to your clan warehouse by $s1 tomorrow.
	PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_ME_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW(1051),
	// Message: The clan hall fee is one week overdue; therefore the clan hall ownership has been revoked.
	THE_CLAN_HALL_FEE_IS_ONE_WEEK_OVERDUE_THEREFORE_THE_CLAN_HALL_OWNERSHIP_HAS_BEEN_REVOKED(1052),
	// Message: It is not possible to resurrect in battlefields where a siege war is taking place.
	IT_IS_NOT_POSSIBLE_TO_RESURRECT_IN_BATTLEFIELDS_WHERE_A_SIEGE_WAR_IS_TAKING_PLACE(1053),
	// Message: You have entered a mystical land.
	YOU_HAVE_ENTERED_A_MYSTICAL_LAND(1054),
	// Message: You have left a mystical land.
	YOU_HAVE_LEFT_A_MYSTICAL_LAND(1055),
	// Message: The equipment, +$s1 $s2, has been removed.
	THE_EQUIPMENT_S1_S2_HAS_BEEN_REMOVED(1064),
	// Message: While operating a private store or workshop, you cannot discard, destroy, or trade an item.
	WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM(1065),
	// Message: $s1 HP has been restored.
	S1_HP_HAS_BEEN_RESTORED(1066),
	// Message: $s2 HP has been restored by $c1.
	S2_HP_HAS_BEEN_RESTORED_BY_C1(1067),
	// Message: $s1 MP has been restored.
	S1_MP_HAS_BEEN_RESTORED(1068),
	// Message: $s2 MP has been restored by $c1.
	S2_MP_HAS_BEEN_RESTORED_BY_C1(1069),
	// Message: The bid amount must be higher than the previous bid.
	THE_BID_AMOUNT_MUST_BE_HIGHER_THAN_THE_PREVIOUS_BID(1075),
	// Message: The prize amount for the winner of Lottery #$s1 is $s2 adena.  We have $s3 first prize winners.
	THE_PRIZE_AMOUNT_FOR_THE_WINNER_OF_LOTTERY_S1_IS_S2_ADENA__WE_HAVE_S3_FIRST_PRIZE_WINNERS(1112),
	// Message: The prize amount for Lucky Lottery #$s1 is $s2 adena. There was no first prize winner in this drawing, therefore the jackpot will be added to the next drawing.
	THE_PRIZE_AMOUNT_FOR_LUCKY_LOTTERY_S1_IS_S2_ADENA_THERE_WAS_NO_FIRST_PRIZE_WINNER_IN_THIS_DRAWING_THEREFORE_THE_JACKPOT_WILL_BE_ADDED_TO_THE_NEXT_DRAWING(1113),
	// Message: You cannot leave a clan while engaged in combat.
	YOU_CANNOT_LEAVE_A_CLAN_WHILE_ENGAGED_IN_COMBAT(1116),
	// Message: A clan member may not be dismissed during combat.
	A_CLAN_MEMBER_MAY_NOT_BE_DISMISSED_DURING_COMBAT(1117),
	// Message: Unable to process this request until your inventory's weight and slot count are less than 80 percent of capacity.
	PROGRESS_IN_A_QUEST_IS_POSSIBLE_ONLY_WHEN_YOUR_INVENTORYS_WEIGHT_AND_SLOT_COUNT_ARE_LESS_THAN_80_PERCENT_OF_CAPACITY(1118),
	// Message: A private store may not be opened while using a skill.
	A_PRIVATE_STORE_MAY_NOT_BE_OPENED_WHILE_USING_A_SKILL(1128),
	// Message: You have dealt $s1 damage to your target and $s2 damage to the servitor.
	YOU_HAVE_DEALT_S1_DAMAGE_TO_YOUR_TARGET_AND_S2_DAMAGE_TO_THE_SERVITOR(1130),
	// Message: It is now midnight and the effect of $s1 can be felt.
	IT_IS_NOW_MIDNIGHT_AND_THE_EFFECT_OF_S1_CAN_BE_FELT(1131),
	// Message: It is dawn and the effect of $s1 will now disappear.
	IT_IS_DAWN_AND_THE_EFFECT_OF_S1_WILL_NOW_DISAPPEAR(1132),
	// Message: Since your HP has decreased, the effect of $s1 can be felt.
	SINCE_YOUR_HP_HAS_DECREASED_THE_EFFECT_OF_S1_CAN_BE_FELT(1133),
	// Message: Since your HP has increased, the effect of $s1 will disappear.
	SINCE_YOUR_HP_HAS_INCREASED_THE_EFFECT_OF_S1_WILL_DISAPPEAR(1134),
	// Message: While you are engaged in combat, you cannot operate a private store or private workshop.
	WHILE_YOU_ARE_ENGAGED_IN_COMBAT_YOU_CANNOT_OPERATE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP(1135),
	// Message: $c1 harvested $s3 $s2(s).
	C1_HARVESTED_S3_S2S(1137),
	// Message: The weight and volume limit of your inventory cannot be exceeded.
	THE_WEIGHT_AND_VOLUME_LIMIT_OF_YOUR_INVENTORY_CANNOT_BE_EXCEEDED(1139),
	// Message: Would you like to open the gate?
	WOULD_YOU_LIKE_TO_OPEN_THE_GATE(1140),
	// Message: Would you like to close the gate?
	WOULD_YOU_LIKE_TO_CLOSE_THE_GATE(1141),
	// Message: Since $s1 already exists nearby, you cannot summon it again.
	SINCE_S1_ALREADY_EXISTS_NEARBY_YOU_CANNOT_SUMMON_IT_AGAIN(1142),
	// Message: Since you do not have enough items to maintain the servitor's stay, the servitor has disappeared.
	SINCE_YOU_DO_NOT_HAVE_ENOUGH_ITEMS_TO_MAINTAIN_THE_SERVITORS_STAY_THE_SERVITOR_HAS_DISAPPEARED(1143),
	// Message: $c1 created $s2 after receiving $s3 adena.
	C1_CREATED_S2_AFTER_RECEIVING_S3_ADENA(1146),
	// Message: $c1 created $s2 $s3 at the price of $s4 adena.
	C1_CREATED_S2_S3_AT_THE_PRICE_OF_S4_ADENA(1148),
	// Message: Your attempt to create $s2 for $c1 at the price of $s3 adena has failed.
	YOUR_ATTEMPT_TO_CREATE_S2_FOR_C1_AT_THE_PRICE_OF_S3_ADENA_HAS_FAILED(1149),
	// Message: $c1 has failed to create $s2 at the price of $s3 adena.
	C1_HAS_FAILED_TO_CREATE_S2_AT_THE_PRICE_OF_S3_ADENA(1150),
	// Message: $s2 is sold to $c1 for the price of $s3 adena.
	S2_IS_SOLD_TO_C1_FOR_THE_PRICE_OF_S3_ADENA(1151),
	// Message: $s2 $s3 have been sold to $c1 for $s4 adena.
	S2_S3_HAVE_BEEN_SOLD_TO_C1_FOR_S4_ADENA(1152),
	// Message: $s2 has been purchased from $c1 at the price of $s3 adena.
	S2_HAS_BEEN_PURCHASED_FROM_C1_AT_THE_PRICE_OF_S3_ADENA(1153),
	// Message: $s3 $s2 has been purchased from $c1 for $s4 adena.
	S3_S2_HAS_BEEN_PURCHASED_FROM_C1_FOR_S4_ADENA(1154),
	// Message: +$s2$s3 has been sold to $c1 at the price of $s4 adena.
	S2S3_HAS_BEEN_SOLD_TO_C1_AT_THE_PRICE_OF_S4_ADENA(1155),
	// Message: +$s2$s3 has been purchased from $c1 at the price of $s4 adena.
	S2S3_HAS_BEEN_PURCHASED_FROM_C1_AT_THE_PRICE_OF_S4_ADENA(1156),
	// Message: The ferry from Talking Island will arrive at Gludin Harbor in approximately 10 minutes.
	THE_FERRY_FROM_TALKING_ISLAND_WILL_ARRIVE_AT_GLUDIN_HARBOR_IN_APPROXIMATELY_10_MINUTES(1159),
	// Message: The ferry from Talking Island will be arriving at Gludin Harbor in approximately 5 minutes.
	THE_FERRY_FROM_TALKING_ISLAND_WILL_BE_ARRIVING_AT_GLUDIN_HARBOR_IN_APPROXIMATELY_5_MINUTES(1160),
	// Message: The ferry from Talking Island will be arriving at Gludin Harbor in approximately 1 minute.
	THE_FERRY_FROM_TALKING_ISLAND_WILL_BE_ARRIVING_AT_GLUDIN_HARBOR_IN_APPROXIMATELY_1_MINUTE(1161),
	// Message: The ferry from Giran Harbor will be arriving at Talking Island in approximately 15 minutes.
	THE_FERRY_FROM_GIRAN_HARBOR_WILL_BE_ARRIVING_AT_TALKING_ISLAND_IN_APPROXIMATELY_15_MINUTES(1162),
	// Message: The ferry from Giran Harbor will be arriving at Talking Island in approximately 10 minutes.
	THE_FERRY_FROM_GIRAN_HARBOR_WILL_BE_ARRIVING_AT_TALKING_ISLAND_IN_APPROXIMATELY_10_MINUTES(1163),
	// Message: The ferry from Giran Harbor will be arriving at Talking Island in approximately 5 minutes.
	THE_FERRY_FROM_GIRAN_HARBOR_WILL_BE_ARRIVING_AT_TALKING_ISLAND_IN_APPROXIMATELY_5_MINUTES(1164),
	// Message: The ferry from Giran Harbor will be arriving at Talking Island in approximately 1 minute.
	THE_FERRY_FROM_GIRAN_HARBOR_WILL_BE_ARRIVING_AT_TALKING_ISLAND_IN_APPROXIMATELY_1_MINUTE(1165),
	// Message: The ferry from Talking Island will be arriving at Giran Harbor in approximately 20 minutes.
	THE_FERRY_FROM_TALKING_ISLAND_WILL_BE_ARRIVING_AT_GIRAN_HARBOR_IN_APPROXIMATELY_20_MINUTES(1166),
	// Message: The ferry from Talking Island will be arriving at Giran Harbor in approximately 15 minutes.
	THE_FERRY_FROM_TALKING_ISLAND_WILL_BE_ARRIVING_AT_GIRAN_HARBOR_IN_APPROXIMATELY_15_MINUTES(1167),
	// Message: The ferry from Talking Island will be arriving at Giran Harbor in approximately 10 minutes.
	THE_FERRY_FROM_TALKING_ISLAND_WILL_BE_ARRIVING_AT_GIRAN_HARBOR_IN_APPROXIMATELY_10_MINUTES(1168),
	// Message: The ferry from Talking Island will be arriving at Giran Harbor in approximately 5 minutes.
	THE_FERRY_FROM_TALKING_ISLAND_WILL_BE_ARRIVING_AT_GIRAN_HARBOR_IN_APPROXIMATELY_5_MINUTES(1169),
	// Message: The ferry from Talking Island will be arriving at Giran Harbor in approximately 1 minute.
	THE_FERRY_FROM_TALKING_ISLAND_WILL_BE_ARRIVING_AT_GIRAN_HARBOR_IN_APPROXIMATELY_1_MINUTE(1170),
	// Message: The Innadril pleasure boat will arrive in approximately 20 minutes.
	THE_INNADRIL_PLEASURE_BOAT_WILL_ARRIVE_IN_APPROXIMATELY_20_MINUTES(1171),
	// Message: The Innadril pleasure boat will arrive in approximately 15 minutes.
	THE_INNADRIL_PLEASURE_BOAT_WILL_ARRIVE_IN_APPROXIMATELY_15_MINUTES(1172),
	// Message: The Innadril pleasure boat will arrive in approximately 10 minutes.
	THE_INNADRIL_PLEASURE_BOAT_WILL_ARRIVE_IN_APPROXIMATELY_10_MINUTES(1173),
	// Message: The Innadril pleasure boat will arrive in approximately 5 minutes.
	THE_INNADRIL_PLEASURE_BOAT_WILL_ARRIVE_IN_APPROXIMATELY_5_MINUTES(1174),
	// Message: The Innadril pleasure boat will arrive in approximately 1 minute.
	THE_INNADRIL_PLEASURE_BOAT_WILL_ARRIVE_IN_APPROXIMATELY_1_MINUTE(1175),
	// Message: Your selected target can no longer receive a recommendation.
	YOUR_SELECTED_TARGET_CAN_NO_LONGER_RECEIVE_A_RECOMMENDATION(1188),
	// Message: The temporary alliance of the Castle Attacker team is in effect. It will be dissolved when the Castle Lord is replaced.
	THE_TEMPORARY_ALLIANCE_OF_THE_CASTLE_ATTACKER_TEAM_IS_IN_EFFECT(1189),
	// Message: The temporary alliance of the Castle Attacker team has been dissolved.
	THE_TEMPORARY_ALLIANCE_OF_THE_CASTLE_ATTACKER_TEAM_HAS_BEEN_DISSOLVED(1190),
	// Message: The ferry from Gludin Harbor will be arriving at Talking Island in approximately 10 minutes.
	THE_FERRY_FROM_GLUDIN_HARBOR_WILL_BE_ARRIVING_AT_TALKING_ISLAND_IN_APPROXIMATELY_10_MINUTES(1191),
	// Message: The ferry from Gludin Harbor will be arriving at Talking Island in approximately 5 minutes.
	THE_FERRY_FROM_GLUDIN_HARBOR_WILL_BE_ARRIVING_AT_TALKING_ISLAND_IN_APPROXIMATELY_5_MINUTES(1192),
	// Message: The ferry from Gludin Harbor will be arriving at Talking Island in approximately 1 minute.
	THE_FERRY_FROM_GLUDIN_HARBOR_WILL_BE_ARRIVING_AT_TALKING_ISLAND_IN_APPROXIMATELY_1_MINUTE(1193),
	// Message: A mercenary can be assigned to a position from the beginning of the Seal Validation period until the time when a siege starts.
	A_MERCENARY_CAN_BE_ASSIGNED_TO_A_POSITION_FROM_THE_BEGINNING_OF_THE_SEAL_VALIDATION_PERIOD_UNTIL_THE_TIME_WHEN_A_SIEGE_STARTS(1194),
	// Message: This mercenary cannot be assigned to a position by using the Seal of Strife.
	THIS_MERCENARY_CANNOT_BE_ASSIGNED_TO_A_POSITION_BY_USING_THE_SEAL_OF_STRIFE(1195),
	// Message: Your force has reached maximum capacity.
	YOUR_FORCE_HAS_REACHED_MAXIMUM_CAPACITY_(1196),
	// Message: The item has been successfully crystallized.
	THE_ITEM_HAS_BEEN_SUCCESSFULLY_CRYSTALLIZED(1198),
	// Message: = $s1 ($s2 Alliance)
	_S1_S2_ALLIANCE(1200),
	// Message: = $s1 (No alliance exists)
	_S1_NO_ALLIANCE_EXISTS(1202),
	// Message: The memo box is full.  There is a 100 memo limit.
	THE_MEMO_BOX_IS_FULL__THERE_IS_A_100_MEMO_LIMIT(1206),
	// Message: $c1 died and dropped $s3 $s2.
	C1_DIED_AND_DROPPED_S3_S2(1208),
	// Message: Congratulations. Your raid was successful.
	CONGRATULATIONS_RAID_WAS_SUCCESSFUL(1209),
	// Message: Seven Signs: The competition period has begun.  Visit a Priest of Dawn or Priestess of Dusk to participate in the event.
	SEVEN_SIGNS_THE_COMPETITION_PERIOD_HAS_BEGUN__VISIT_A_PRIEST_OF_DAWN_OR_PRIESTESS_OF_DUSK_TO_PARTICIPATE_IN_THE_EVENT(1210),
	// Message: Seven Signs: The competition period has ended. The next quest event will start in one week.
	SEVEN_SIGNS_THE_COMPETITION_PERIOD_HAS_ENDED_THE_NEXT_QUEST_EVENT_WILL_START_IN_ONE_WEEK(1211),
	// Message: Seven Signs: The Lords of Dawn have obtained the Seal of Avarice.
	SEVEN_SIGNS_THE_LORDS_OF_DAWN_HAVE_OBTAINED_THE_SEAL_OF_AVARICE(1212),
	// Message: Seven Signs: The Lords of Dawn have obtained the Seal of Gnosis.
	SEVEN_SIGNS_THE_LORDS_OF_DAWN_HAVE_OBTAINED_THE_SEAL_OF_GNOSIS(1213),
	// Message: Seven Signs: The Lords of Dawn have obtained the Seal of Strife.
	SEVEN_SIGNS_THE_LORDS_OF_DAWN_HAVE_OBTAINED_THE_SEAL_OF_STRIFE(1214),
	// Message: Seven Signs: The Revolutionaries of Dusk have obtained the Seal of Avarice.
	SEVEN_SIGNS_THE_REVOLUTIONARIES_OF_DUSK_HAVE_OBTAINED_THE_SEAL_OF_AVARICE(1215),
	// Message: Seven Signs: The Revolutionaries of Dusk have obtained the Seal of Gnosis.
	SEVEN_SIGNS_THE_REVOLUTIONARIES_OF_DUSK_HAVE_OBTAINED_THE_SEAL_OF_GNOSIS(1216),
	// Message: Seven Signs: The Revolutionaries of Dusk have obtained the Seal of Strife.
	SEVEN_SIGNS_THE_REVOLUTIONARIES_OF_DUSK_HAVE_OBTAINED_THE_SEAL_OF_STRIFE(1217),
	// Message: Seven Signs: The Seal Validation period has begun.
	SEVEN_SIGNS_THE_SEAL_VALIDATION_PERIOD_HAS_BEGUN(1218),
	// Message: Seven Signs: The Seal Validation period has ended.
	SEVEN_SIGNS_THE_SEAL_VALIDATION_PERIOD_HAS_ENDED(1219),
	// Message: Current Location: $s1, $s2, $s3 (GM Consultation Area)
	CURRENT_LOCATION_S1_S2_S3_GM_CONSULTATION_AREA(1222),
	// Message: We depart for Talking Island in five minutes.
	WE_DEPART_FOR_TALKING_ISLAND_IN_FIVE_MINUTES(1223),
	// Message: We depart for Talking Island in one minute.
	WE_DEPART_FOR_TALKING_ISLAND_IN_ONE_MINUTE(1224),
	// Message: All aboard for Talking Island!
	ALL_ABOARD_FOR_TALKING_ISLAND(1225),
	// Message: We are now leaving for Talking Island.
	WE_ARE_NOW_LEAVING_FOR_TALKING_ISLAND(1226),
	// Message: You have $s1 unread messages.
	YOU_HAVE_S1_UNREAD_MESSAGES(1227),
	// Message: $c1 has blocked you. You cannot send mail to $c1.
	C1_HAS_BLOCKED_YOU_YOU_CANNOT_SEND_MAIL_TO_C1(1228),
	// Message: You've sent mail.
	YOUVE_SENT_MAIL(1231),
	// Message: The message was not sent.
	THE_MESSAGE_WAS_NOT_SENT(1232),
	// Message: You've got mail.
	YOUVE_GOT_MAIL(1233),
	// Message: Seven Signs: The Revolutionaries of Dusk have won.
	SEVEN_SIGNS_THE_REVOLUTIONARIES_OF_DUSK_HAVE_WON(1240),
	// Message: Seven Signs: The Lords of Dawn have won.
	SEVEN_SIGNS_THE_LORDS_OF_DAWN_HAVE_WON(1241),
	// Message: You are out of feed. Mount status canceled.
	YOU_ARE_OUT_OF_FEED(1248),
	// Message: $c1 has died and dropped $s2 adena.
	C1_HAS_DIED_AND_DROPPED_S2_ADENA(1246),
	// Message: $s1 has been crystallized.
	S1_HAS_BEEN_CRYSTALLIZED(1258),
	// Message: Seven Signs: Preparations have begun for the next quest event.
	SEVEN_SIGNS_PREPARATIONS_HAVE_BEGUN_FOR_THE_NEXT_QUEST_EVENT(1260),
	// Message: Seven Signs: The quest event period has begun. Speak with a Priest of Dawn or Dusk Priestess if you wish to participate in the event.
	SEVEN_SIGNS_THE_QUEST_EVENT_PERIOD_HAS_BEGUN_SPEAK_WITH_A_PRIEST_OF_DAWN_OR_DUSK_PRIESTESS_IF_YOU_WISH_TO_PARTICIPATE_IN_THE_EVENT(1261),
	// Message: Seven Signs: Quest event has ended. Results are being tallied.
	SEVEN_SIGNS_QUEST_EVENT_HAS_ENDED_RESULTS_ARE_BEING_TALLIED(1262),
	// Message: Seven Signs: This is the seal validation period. A new quest event period begins next Monday.
	SEVEN_SIGNS_THIS_IS_THE_SEAL_VALIDATION_PERIOD_A_NEW_QUEST_EVENT_PERIOD_BEGINS_NEXT_MONDAY(1263),
	// Message: Your contribution score has increased by $s1.
	YOUR_CONTRIBUTION_SCORE_HAS_INCREASED_BY_S1(1267),
	// Message: The new subclass has been added.
	THE_NEW_SUBCLASS_HAS_BEEN_ADDED(1269),
	// Message: You have successfully switched to your subclass.
	YOU_HAVE_SUCCESSFULLY_SWITCHED_TO_YOUR_SUBCLASS(1270),
	// Message: You will participate in the Seven Signs as a member of the Lords of Dawn.
	YOU_WILL_PARTICIPATE_IN_THE_SEVEN_SIGNS_AS_A_MEMBER_OF_THE_LORDS_OF_DAWN(1273),
	// Message: You will participate in the Seven Signs as a member of the Revolutionaries of Dusk.
	YOU_WILL_PARTICIPATE_IN_THE_SEVEN_SIGNS_AS_A_MEMBER_OF_THE_REVOLUTIONARIES_OF_DUSK(1274),
	// Message: You've chosen to fight for the Seal of Avarice during this quest event period.
	YOUVE_CHOSEN_TO_FIGHT_FOR_THE_SEAL_OF_AVARICE_DURING_THIS_QUEST_EVENT_PERIOD(1275),
	// Message: You've chosen to fight for the Seal of Gnosis during this quest event period.
	YOUVE_CHOSEN_TO_FIGHT_FOR_THE_SEAL_OF_GNOSIS_DURING_THIS_QUEST_EVENT_PERIOD(1276),
	// Message: You've chosen to fight for the Seal of Strife during this quest event period.
	YOUVE_CHOSEN_TO_FIGHT_FOR_THE_SEAL_OF_STRIFE_DURING_THIS_QUEST_EVENT_PERIOD(1277),
	// Message: Contribution level has exceeded the limit. You may not continue.
	CONTRIBUTION_LEVEL_HAS_EXCEEDED_THE_LIMIT_YOU_MAY_NOT_CONTINUE(1279),
	// Message: Magic Critical Hit!
	MAGIC_CRITICAL_HIT(1280),
	// Message: Your excellent shield defense was a success!
	YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS(1281),
	// Message: Since the seal was owned during the previous period and 10% or more people have participated.
	SINCE_THE_SEAL_WAS_OWNED_DURING_THE_PREVIOUS_PERIOD_AND_10_OR_MORE_PEOPLE_HAVE_PARTICIPATED(1289),
	// Message: Although the seal was not owned, 35% or more people have participated.
	ALTHOUGH_THE_SEAL_WAS_NOT_OWNED_35_OR_MORE_PEOPLE_HAVE_PARTICIPATED(1290),
	// Message: Although the seal was owned during the previous period, less than 10% of people have voted.
	ALTHOUGH_THE_SEAL_WAS_OWNED_DURING_THE_PREVIOUS_PERIOD_LESS_THAN_10_OF_PEOPLE_HAVE_VOTED(1291),
	// Message: Since the seal was not owned during the previous period, and since less than 35 percent of people have voted.
	SINCE_THE_SEAL_WAS_NOT_OWNED_DURING_THE_PREVIOUS_PERIOD_AND_SINCE_LESS_THAN_35_PERCENT_OF_PEOPLE_HAVE_VOTED(1292),
	// Message: If current trends continue, it will end in a tie.
	IF_CURRENT_TRENDS_CONTINUE_IT_WILL_END_IN_A_TIE(1293),
	// Message: Subclasses may not be created or changed while a skill is in use.
	SUBCLASSES_MAY_NOT_BE_CREATED_OR_CHANGED_WHILE_A_SKILL_IS_IN_USE(1295),
	// Message: You cannot open a Private Store here.
	YOU_CANNOT_OPEN_A_PRIVATE_STORE_HERE(1296),
	// Message: You cannot open a Private Workshop here.
	YOU_CANNOT_OPEN_A_PRIVATE_WORKSHOP_HERE(1297),
	// Message: $c1's casting has been interrupted.
	C1S_CASTING_HAS_BEEN_INTERRUPTED(1299),
	// Message: This may only be used during the quest event period.
	THIS_MAY_ONLY_BE_USED_DURING_THE_QUEST_EVENT_PERIOD(1303),
	// Message: You are no longer trying on equipment.
	YOU_ARE_NO_LONGER_TRYING_ON_EQUIPMENT(1306),
	// Message: Congratulations - You've completed a class transfer!
	CONGRATULATIONS__YOUVE_COMPLETED_A_CLASS_TRANSFER(1308),
	// Message: You are currently blocked from using the Private Store and Private Workshop.
	YOU_ARE_CURRENTLY_BLOCKED_FROM_USING_THE_PRIVATE_STORE_AND_PRIVATE_WORKSHOP(1329),
	// Message: You have been blocked from chatting with that contact.
	YOU_HAVE_BEEN_BLOCKED_FROM_CHATTING_WITH_THAT_CONTACT(1357),
	// Message: You can not try those items on at the same time.
	YOU_CAN_NOT_TRY_THOSE_ITEMS_ON_AT_THE_SAME_TIME(1368),
	// Message: $c1 has become the party leader.
	C1_HAS_BECOME_THE_PARTY_LEADER(1384),
	// Message: You are not allowed to dismount in this location.
	YOU_ARE_NOT_ALLOWED_TO_DISMOUNT_IN_THIS_LOCATION(1385),
	// Message: You are not allowed to enter the party room.
	YOU_ARE_NOT_ALLOWED_TO_ENTER_THE_PARTY_ROOM(1390),
	// Message: You have exited the party room.
	YOU_HAVE_EXITED_THE_PARTY_ROOM(1391),
	// Message: $c1 has left the party room.
	C1_HAS_LEFT_THE_PARTY_ROOM(1392),
	// Message: You have been ousted from the party room.
	YOU_HAVE_BEEN_OUSTED_FROM_THE_PARTY_ROOM(1393),
	// Message: $c1 has been kicked from the party room.
	C1_HAS_BEEN_KICKED_FROM_THE_PARTY_ROOM(1394),
	// Message: The party room has been disbanded.
	THE_PARTY_ROOM_HAS_BEEN_DISBANDED(1395),
	// Message: The list of party rooms can only be viewed by a person who is not part of a party.
	THE_LIST_OF_PARTY_ROOMS_CAN_ONLY_BE_VIEWED_BY_A_PERSON_WHO_IS_NOT_PART_OF_A_PARTY(1396),
	// Message: The leader of the party room has changed.
	THE_LEADER_OF_THE_PARTY_ROOM_HAS_CHANGED(1397),
	// Message: Slow down, you are already the party leader.
	SLOW_DOWN_YOU_ARE_ALREADY_THE_PARTY_LEADER(1401),
	// Message: You may only transfer party leadership to another member of the party.
	YOU_MAY_ONLY_TRANSFER_PARTY_LEADERSHIP_TO_ANOTHER_MEMBER_OF_THE_PARTY(1402),
	// Message: $s1 CP has been restored.
	S1_CP_HAS_BEEN_RESTORED(1405),
	// Message: $s2 CP has been restored by $c1.
	S2_CP_HAS_BEEN_RESTORED_BY_C1(1406),
	// Message: You do not meet the requirements to enter that party room.
	YOU_DO_NOT_MEET_THE_REQUIREMENTS_TO_ENTER_THAT_PARTY_ROOM(1413),
	// Message: The automatic use of $s1 has been activated.
	THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_ACTIVATED(1433),
	// Message: The automatic use of $s1 has been deactivated.
	THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_DEACTIVATED(1434),
	// Message: Due to insufficient $s1, the automatic use function has been deactivated.
	DUE_TO_INSUFFICIENT_S1_THE_AUTOMATIC_USE_FUNCTION_HAS_BEEN_DEACTIVATED(1435),
	// Message: Due to insufficient $s1, the automatic use function cannot be activated.
	DUE_TO_INSUFFICIENT_S1_THE_AUTOMATIC_USE_FUNCTION_CANNOT_BE_ACTIVATED(1436),
	// Message: You do not have all of the items needed to enchant that skill.
	YOU_DO_NOT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL(1439),
	// Message: Skill enchant was successful! $s1 has been enchanted.
	SKILL_ENCHANT_WAS_SUCCESSFUL_S1_HAS_BEEN_ENCHANTED(1440),
	// Message: Skill enchant failed. The skill will be initialized.
	SKILL_ENCHANT_FAILED(1441),
	// Message: You do not have enough SP to enchant that skill.
	YOU_DO_NOT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL(1443),
	// Message: Your previous subclass will be removed and replaced with the new subclass at level 40.  Do you wish to continue?
	YOUR_PREVIOUS_SUBCLASS_WILL_BE_REMOVED_AND_REPLACED_WITH_THE_NEW_SUBCLASS_AT_LEVEL_40__DO_YOU_WISH_TO_CONTINUE(1445),
	// Message: You cannot do that while fishing.
	YOU_CANNOT_DO_THAT_WHILE_FISHING(1447),
	// Message: Only fishing skills may be used at this time.
	ONLY_FISHING_SKILLS_MAY_BE_USED_AT_THIS_TIME(1448),
	// Message: You've got a bite!
	YOUVE_GOT_A_BITE(1449),
	// Message: That fish is more determined than you are - it spit the hook!
	THAT_FISH_IS_MORE_DETERMINED_THAN_YOU_ARE__IT_SPIT_THE_HOOK(1450),
	// Message: Your bait was stolen by that fish!
	YOUR_BAIT_WAS_STOLEN_BY_THAT_FISH(1451),
	// Message: The bait has been lost because the fish got away.
	THE_BAIT_HAS_BEEN_LOST_BECAUSE_THE_FISH_GOT_AWAY(1452),
	// Message: You do not have a fishing pole equipped.
	YOU_DO_NOT_HAVE_A_FISHING_POLE_EQUIPPED(1453),
	// Message: You must put bait on your hook before you can fish.
	YOU_MUST_PUT_BAIT_ON_YOUR_HOOK_BEFORE_YOU_CAN_FISH(1454),
	// Message: You cannot fish while under water.
	YOU_CANNOT_FISH_WHILE_UNDER_WATER(1455),
	// Message: You cannot fish while riding as a passenger of a boat - it's against the rules.
	YOU_CANNOT_FISH_WHILE_RIDING_AS_A_PASSENGER_OF_A_BOAT(1456),
	// Message: You can't fish here.
	YOU_CANT_FISH_HERE(1457),
	// Message: Your attempt at fishing has been cancelled.
	YOUR_ATTEMPT_AT_FISHING_HAS_BEEN_CANCELLED(1458),
	// Message: You do not have enough bait.
	YOU_DO_NOT_HAVE_ENOUGH_BAIT(1459),
	// Message: You reel your line in and stop fishing.
	YOU_REEL_YOUR_LINE_IN_AND_STOP_FISHING(1460),
	// Message: You cast your line and start to fish.
	YOU_CAST_YOUR_LINE_AND_START_TO_FISH(1461),
	// Message: You may only use the Pumping skill while you are fishing.
	YOU_MAY_ONLY_USE_THE_PUMPING_SKILL_WHILE_YOU_ARE_FISHING(1462),
	// Message: You may only use the Reeling skill while you are fishing.
	YOU_MAY_ONLY_USE_THE_REELING_SKILL_WHILE_YOU_ARE_FISHING(1463),
	// Message: The fish has resisted your attempt to bring it in.
	THE_FISH_HAS_RESISTED_YOUR_ATTEMPT_TO_BRING_IT_IN(1464),
	// Message: Your pumping is successful, causing $s1 damage.
	YOUR_PUMPING_IS_SUCCESSFUL_CAUSING_S1_DAMAGE(1465),
	// Message: You failed to do anything with the fish and it regains $s1 HP.
	YOU_FAILED_TO_DO_ANYTHING_WITH_THE_FISH_AND_IT_REGAINS_S1_HP(1466),
	// Message: You reel that fish in closer and cause $s1 damage.
	YOU_REEL_THAT_FISH_IN_CLOSER_AND_CAUSE_S1_DAMAGE(1467),
	// Message: You failed to reel that fish in further and it regains $s1 HP.
	YOU_FAILED_TO_REEL_THAT_FISH_IN_FURTHER_AND_IT_REGAINS_S1_HP(1468),
	// Message: You caught something!
	YOU_CAUGHT_SOMETHING(1469),
	// Message: You cannot do that while fishing.
	YOU_CANNOT_DO_THAT_WHILE_FISHING_(1470),
	// Message: You cannot do that while fishing.
	YOU_CANNOT_DO_THAT_WHILE_FISHING_2(1471),
	// Message: That is the wrong grade of soulshot for that fishing pole.
	THAT_IS_THE_WRONG_GRADE_OF_SOULSHOT_FOR_THAT_FISHING_POLE(1479),
	// Message: The ferry from Talking Island to Gludin Harbor has been delayed.
	THE_FERRY_FROM_TALKING_ISLAND_TO_GLUDIN_HARBOR_HAS_BEEN_DELAYED(1485),
	// Message: The ferry from Gludin Harbor to Talking Island has been delayed.
	THE_FERRY_FROM_GLUDIN_HARBOR_TO_TALKING_ISLAND_HAS_BEEN_DELAYED(1486),
	// Message: The ferry from Giran Harbor to Talking Island has been delayed.
	THE_FERRY_FROM_GIRAN_HARBOR_TO_TALKING_ISLAND_HAS_BEEN_DELAYED(1487),
	// Message: The ferry from Talking Island to Giran Harbor has been delayed.
	THE_FERRY_FROM_TALKING_ISLAND_TO_GIRAN_HARBOR_HAS_BEEN_DELAYED(1488),
	// Message: Traded $s2 of $s1 crops.
	TRADED_S2_OF_S1_CROPS(1490),
	// Message: Failed in trading $s2 of $s1 crops.
	FAILED_IN_TRADING_S2_OF_S1_CROPS(1491),
	// Message: You will be moved to the Olympiad Stadium in $s1 second(s).
	YOU_WILL_BE_MOVED_TO_THE_OLYMPIAD_STADIUM_IN_S1_SECONDS(1492),
	// Message: Your opponent made haste with their tail between their legs; the match has been cancelled.
	YOUR_OPPONENT_MADE_HASTE_WITH_THEIR_TAIL_BETWEEN_THEIR_LEGS_THE_MATCH_HAS_BEEN_CANCELLED(1493),
	// Message: Your opponent does not meet the requirements to do battle; the match has been cancelled.
	YOUR_OPPONENT_DOES_NOT_MEET_THE_REQUIREMENTS_TO_DO_BATTLE_THE_MATCH_HAS_BEEN_CANCELLED(1494),
	// Message: The match will start in $s1 second(s).
	THE_MATCH_WILL_START_IN_S1_SECONDS(1495),
	// Message: The match has started. Fight!
	THE_MATCH_HAS_STARTED(1496),
	// Message: Congratulations, $c1! You win the match!
	CONGRATULATIONS_C1_YOU_WIN_THE_MATCH(1497),
	// Message: There is no victor; the match ends in a tie.
	THERE_IS_NO_VICTOR_THE_MATCH_ENDS_IN_A_TIE(1498),
	// Message: You will be moved back to town in $s1 second(s).
	YOU_WILL_BE_MOVED_BACK_TO_TOWN_IN_S1_SECONDS(1499),
	// Message: $c1 does not meet the participation requirements. A subclass character cannot participate in the Olympiad.
	C1_DOES_NOT_MEET_THE_PARTICIPATION_REQUIREMENTS_SUBCLASS_CHARACTER_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD(1500),
	// Message: $c1 does not meet the participation requirements. Only Noblesse characters can participate in the Olympiad.
	C1_DOES_NOT_MEET_THE_PARTICIPATION_REQUIREMENTS_ONLY_NOBLESSE_CHARACTERS_CAN_PARTICIPATE_IN_THE_OLYMPIAD(1501),
	// Message: $c1 is already registered on the match waiting list.
	C1_IS_ALREADY_REGISTERED_ON_THE_MATCH_WAITING_LIST(1502),
	// Message: You have been registered for the Grand Olympiad waiting list for a class specific match.
	YOU_HAVE_BEEN_REGISTERED_FOR_THE_GRAND_OLYMPIAD_WAITING_LIST_FOR_A_CLASS_SPECIFIC_MATCH(1503),
	// Message: You are currently registered for a 1v1 class irrelevant match.
	YOU_ARE_CURRENTLY_REGISTERED_FOR_A_1V1_CLASS_IRRELEVANT_MATCH(1504),
	// Message: You have been removed from the Grand Olympiad waiting list.
	YOU_HAVE_BEEN_REMOVED_FROM_THE_GRAND_OLYMPIAD_WAITING_LIST(1505),
	// Message: You are not currently registered for the Grand Olympiad.
	YOU_ARE_NOT_CURRENTLY_REGISTERED_FOR_THE_GRAND_OLYMPIAD(1506),
	// Message: You cannot equip that item in a Grand Olympiad match.
	YOU_CANNOT_EQUIP_THAT_ITEM_IN_A_GRAND_OLYMPIAD_MATCH(1507),
	// Message: You cannot use that item in a Grand Olympiad match.
	YOU_CANNOT_USE_THAT_ITEM_IN_A_GRAND_OLYMPIAD_MATCH(1508),
	// Message: You cannot use that skill in a Grand Olympiad match.
	YOU_CANNOT_USE_THAT_SKILL_IN_A_GRAND_OLYMPIAD_MATCH(1509),
	// Message: $c1 is making an attempt to resurrect you. If you choose this path, $s2 experience points will be returned to you. Do you want to be resurrected?
	C1_IS_MAKING_AN_ATTEMPT_TO_RESURRECT_YOU_IF_YOU_CHOOSE_THIS_PATH_S2_EXPERIENCE_WILL_BE_RETURNED_FOR_YOU(1510),
	// Message: While a pet is being resurrected, it cannot help in resurrecting its master.
	WHILE_A_PET_IS_BEING_RESURRECTED_IT_CANNOT_HELP_IN_RESURRECTING_ITS_MASTER(1511),
	// Message: Resurrection has already been proposed.
	RESURRECTION_HAS_ALREADY_BEEN_PROPOSED(1513),
	// Message: You cannot resurrect the owner of a pet while their pet is being resurrected.
	YOU_CANNOT_RESURRECT_THE_OWNER_OF_A_PET_WHILE_THEIR_PET_IS_BEING_RESURRECTED(1514),
	// Message: A pet cannot be resurrected while it's owner is in the process of resurrecting.
	A_PET_CANNOT_BE_RESURRECTED_WHILE_ITS_OWNER_IS_IN_THE_PROCESS_OF_RESURRECTING(1515),
	// Message: The target is unavailable for seeding.
	THE_TARGET_IS_UNAVAILABLE_FOR_SEEDING(1516),
	// Message: The Blessed Enchant failed. The enchant value of the item became 0.
	THE_BLESSED_ENCHANT_FAILED(1517),
	// Message: You do not meet the required condition to equip that item.
	YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM(1518),
	// Message: The pet has been killed. If you don't resurrect it within 24 hours, the pet's body will disappear along with all the pet's items.
	THE_PET_HAS_BEEN_KILLED(1519),
	// Message: Your servitor has vanished! You'll need to summon a new one.
	YOUR_SERVITOR_HAS_VANISHED_YOULL_NEED_TO_SUMMON_A_NEW_ONE(1521),
	// Message: You should release your pet or servitor so that it does not fall off of the boat and drown!
	YOU_SHOULD_RELEASE_YOUR_PET_OR_SERVITOR_SO_THAT_IT_DOES_NOT_FALL_OFF_OF_THE_BOAT_AND_DROWN(1523),
	// Message: Your pet was hungry so it ate $s1.
	YOUR_PET_WAS_HUNGRY_SO_IT_ATE_S1(1527),
	// Message: Attention: $c1 has picked up $s2.
	ATTENTION_C1_HAS_PICKED_UP_S2(1533),
	// Message: Attention: $c1 has picked up +$s2$s3.
	ATTENTION_C1_HAS_PICKED_UP_S2S3(1534),
	// Message: Attention: $c1's pet has picked up $s2.
	ATTENTION_C1S_PET_HAS_PICKED_UP_S2(1535),
	// Message: Attention: $c1's pet has picked up +$s2$s3.
	ATTENTION_C1S_PET_HAS_PICKED_UP_S2S3(1536),
	// Message: Current Location:  $s1, $s2, $s3 (near Rune Village)
	CURRENT_LOCATION__S1_S2_S3_NEAR_RUNE_VILLAGE(1537),
	// Message: Current Location: $s1, $s2, $s3 (near the Town of Goddard)
	CURRENT_LOCATION_S1_S2_S3_NEAR_THE_TOWN_OF_GODDARD(1538),
	// Message: Your clan notice has been saved.
	YOUR_CLAN_NOTICE_HAS_BEEN_SAVED(1556),
	// Message: $s1 has declared a Clan War.
	S1_HAS_DECLARED_A_CLAN_WAR(1561),
	// Message: A Clan War has been declared against the clan, $s1.  If you are killed during the Clan War by members of the opposing clan, you will only lose a quarter of the normal experience from death.
	A_CLAN_WAR_HAS_BEEN_DECLARED_AGAINST_THE_CLAN_S1__IF_YOU_ARE_KILLED_DURING_THE_CLAN_WAR_BY_MEMBERS_OF_THE_OPPOSING_CLAN_YOU_WILL_ONLY_LOSE_A_QUARTER_OF_THE_NORMAL_EXPERIENCE_FROM_DEATH(1562),
	// Message: A clan war can only be declared if the clan is level 3 or above, and the number of clan members is fifteen or greater.
	A_CLAN_WAR_CAN_ONLY_BE_DECLARED_IF_THE_CLAN_IS_LEVEL_3_OR_ABOVE_AND_THE_NUMBER_OF_CLAN_MEMBERS_IS_FIFTEEN_OR_GREATER(1564),
	// Message: A clan war cannot be declared against a clan that does not exist!
	A_CLAN_WAR_CANNOT_BE_DECLARED_AGAINST_A_CLAN_THAT_DOES_NOT_EXIST(1565),
	// Message: The clan, $s1, has decided to stop the war.
	THE_CLAN_S1_HAS_DECIDED_TO_STOP_THE_WAR(1566),
	// Message: The war against $s1 Clan has been stopped.
	THE_WAR_AGAINST_S1_CLAN_HAS_BEEN_STOPPED(1567),
	// Message: A declaration of Clan War against an allied clan can't be made.
	A_DECLARATION_OF_CLAN_WAR_AGAINST_AN_ALLIED_CLAN_CANT_BE_MADE(1569),
	// Message: A declaration of war against more than 30 Clans can't be made at the same time.
	A_DECLARATION_OF_WAR_AGAINST_MORE_THAN_30_CLANS_CANT_BE_MADE_AT_THE_SAME_TIME(1570),
	// Message: ======<Clans You've Declared War On>======
	CLANS_YOUVE_DECLARED_WAR_ON(1571),
	// Message: ======<Clans That Have Declared War On You>======
	CLANS_THAT_HAVE_DECLARED_WAR_ON_YOU(1572),
	// Message: The Command Channel has been formed.
	THE_COMMAND_CHANNEL_HAS_BEEN_FORMED(1580),
	// Message: The Command Channel has been disbanded.
	THE_COMMAND_CHANNEL_HAS_BEEN_DISBANDED(1581),
	// Message: You were dismissed from the Command Channel.
	YOU_WERE_DISMISSED_FROM_THE_COMMAND_CHANNEL(1583),
	// Message: $c1's party has been dismissed from the Command Channel.
	C1S_PARTY_HAS_BEEN_DISMISSED_FROM_THE_COMMAND_CHANNEL(1584),
	// Message: You have quit the Command Channel.
	YOU_HAVE_QUIT_THE_COMMAND_CHANNEL(1586),
	// Message: $c1's party has left the Command Channel.
	C1S_PARTY_HAS_LEFT_THE_COMMAND_CHANNEL(1587),
	// Message: Command Channel authority has been transferred to $c1.
	COMMAND_CHANNEL_AUTHORITY_HAS_BEEN_TRANSFERRED_TO_C1(1589),
	// Message: No user has been invited to the Command Channel.
	NO_USER_HAS_BEEN_INVITED_TO_THE_COMMAND_CHANNEL(1591),
	// Message: You do not have authority to invite someone to the Command Channel.
	YOU_DO_NOT_HAVE_AUTHORITY_TO_INVITE_SOMEONE_TO_THE_COMMAND_CHANNEL(1593),
	// Message: $c1's party is already a member of the Command Channel.
	C1S_PARTY_IS_ALREADY_A_MEMBER_OF_THE_COMMAND_CHANNEL(1594),
	// Message: $s1 has succeeded.
	S1_HAS_SUCCEEDED(1595),
	// Message: $s1 has failed.
	S1_HAS_FAILED(1597),
	// Message: Soulshots and spiritshots are not available for a dead pet or servitor.  Sad, isn't it?
	SOULSHOTS_AND_SPIRITSHOTS_ARE_NOT_AVAILABLE_FOR_A_DEAD_PET_OR_SERVITOR(1598),
	// Message: You cannot "observe" while you are in combat!
	YOU_CANNOT_OBSERVE_WHILE_YOU_ARE_IN_COMBAT(1599),
	// Message: Only a party leader can access the Command Channel.
	ONLY_A_PARTY_LEADER_CAN_ACCESS_THE_COMMAND_CHANNEL(1602),
	// Message: Only the Command Channel creator can use the Raid Leader text.
	ONLY_THE_COMMAND_CHANNEL_CREATOR_CAN_USE_THE_RAID_LEADER_TEXT(1603),
	// Message: * Here, you can buy only seeds of $s1 Manor.
	_HERE_YOU_CAN_BUY_ONLY_SEEDS_OF_S1_MANOR(1605),
	// Message: Congratulations - You've completed your third-class transfer quest!
	CONGRATULATIONS__YOUVE_COMPLETED_YOUR_THIRDCLASS_TRANSFER_QUEST(1606),
	// Message: $s1 adena has been withdrawn to pay for purchasing fees.
	S1_ADENA_HAS_BEEN_WITHDRAWN_TO_PAY_FOR_PURCHASING_FEES(1607),
	// Message: War has already been declared against that clan. but I'll make note that you really don't like them.
	WAR_HAS_ALREADY_BEEN_DECLARED_AGAINST_THAT_CLAN_BUT_ILL_MAKE_NOTE_THAT_YOU_REALLY_DONT_LIKE_THEM(1609),
	// Message: Fool! You cannot declare war against your own clan!
	FOOL_YOU_CANNOT_DECLARE_WAR_AGAINST_YOUR_OWN_CLAN(1610),
	// Message: Party Leader: $c1
	PARTY_LEADER_C1(1611),
	// Message: =====<War List>=====
	WAR_LIST(1612),
	// Message: You do not have the authority to use the Command Channel.
	YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_USE_THE_COMMAND_CHANNEL(1617),
	// Message: The ferry from Rune Harbor to Gludin Harbor has been delayed.
	THE_FERRY_FROM_RUNE_HARBOR_TO_GLUDIN_HARBOR_HAS_BEEN_DELAYED(1618),
	// Message: The ferry from Gludin Harbor to Rune Harbor has been delayed.
	THE_FERRY_FROM_GLUDIN_HARBOR_TO_RUNE_HARBOR_HAS_BEEN_DELAYED(1619),
	// Message: Welcome to Rune Harbor.
	WELCOME_TO_RUNE_HARBOR(1620),
	// Message: Departure for Gludin Harbor will take place in five minutes!
	DEPARTURE_FOR_GLUDIN_HARBOR_WILL_TAKE_PLACE_IN_FIVE_MINUTES(1621),
	// Message: Departure for Gludin Harbor will take place in one minute!
	DEPARTURE_FOR_GLUDIN_HARBOR_WILL_TAKE_PLACE_IN_ONE_MINUTE(1622),
	// Message: Make haste!  We will be departing for Gludin Harbor shortly…
	MAKE_HASTE__WE_WILL_BE_DEPARTING_FOR_GLUDIN_HARBOR_SHORTLY(1623),
	// Message: We are now departing for Gludin Harbor. Hold on and enjoy the ride!
	WE_ARE_NOW_DEPARTING_FOR_GLUDIN_HARBOR(1624),
	// Message: Departure for Rune Harbor will take place after anchoring for ten minutes.
	DEPARTURE_FOR_RUNE_HARBOR_WILL_TAKE_PLACE_AFTER_ANCHORING_FOR_TEN_MINUTES(1625),
	// Message: Departure for Rune Harbor will take place in five minutes!
	DEPARTURE_FOR_RUNE_HARBOR_WILL_TAKE_PLACE_IN_FIVE_MINUTES(1626),
	// Message: Departure for Rune Harbor will take place in one minute!
	DEPARTURE_FOR_RUNE_HARBOR_WILL_TAKE_PLACE_IN_ONE_MINUTE(1627),
	// Message: Make haste!  We will be departing for Gludin Harbor shortly…
	MAKE_HASTE__WE_WILL_BE_DEPARTING_FOR_GLUDIN_HARBOR_SHORTLY_(1628),
	// Message: We are now departing for Rune Harbor. Hold on and enjoy the ride!
	WE_ARE_NOW_DEPARTING_FOR_RUNE_HARBOR(1629),
	// Message: The ferry from Rune Harbor will be arriving at Gludin Harbor in approximately 15 minutes.
	THE_FERRY_FROM_RUNE_HARBOR_WILL_BE_ARRIVING_AT_GLUDIN_HARBOR_IN_APPROXIMATELY_15_MINUTES(1630),
	// Message: The ferry from Rune Harbor will be arriving at Gludin Harbor in approximately 10 minutes.
	THE_FERRY_FROM_RUNE_HARBOR_WILL_BE_ARRIVING_AT_GLUDIN_HARBOR_IN_APPROXIMATELY_10_MINUTES(1631),
	// Message: The ferry from Rune Harbor will be arriving at Gludin Harbor in approximately 5 minutes.
	THE_FERRY_FROM_RUNE_HARBOR_WILL_BE_ARRIVING_AT_GLUDIN_HARBOR_IN_APPROXIMATELY_5_MINUTES(1632),
	// Message: The ferry from Rune Harbor will be arriving at Gludin Harbor in approximately 1 minute.
	THE_FERRY_FROM_RUNE_HARBOR_WILL_BE_ARRIVING_AT_GLUDIN_HARBOR_IN_APPROXIMATELY_1_MINUTE(1633),
	// Message: The ferry from Gludin Harbor will be arriving at Rune Harbor in approximately 15 minutes.
	THE_FERRY_FROM_GLUDIN_HARBOR_WILL_BE_ARRIVING_AT_RUNE_HARBOR_IN_APPROXIMATELY_15_MINUTES(1634),
	// Message: The ferry from Gludin Harbor will be arriving at Rune Harbor in approximately 10 minutes.
	THE_FERRY_FROM_GLUDIN_HARBOR_WILL_BE_ARRIVING_AT_RUNE_HARBOR_IN_APPROXIMATELY_10_MINUTES(1635),
	// Message: The ferry from Gludin Harbor will be arriving at Rune Harbor in approximately 5 minutes.
	THE_FERRY_FROM_GLUDIN_HARBOR_WILL_BE_ARRIVING_AT_RUNE_HARBOR_IN_APPROXIMATELY_5_MINUTES(1636),
	// Message: The ferry from Gludin Harbor will be arriving at Rune Harbor in approximately 1 minute.
	THE_FERRY_FROM_GLUDIN_HARBOR_WILL_BE_ARRIVING_AT_RUNE_HARBOR_IN_APPROXIMATELY_1_MINUTE(1637),
	// Message: You cannot fish while using a recipe book, private manufacture or private store.
	YOU_CANNOT_FISH_WHILE_USING_A_RECIPE_BOOK_PRIVATE_MANUFACTURE_OR_PRIVATE_STORE(1638),
	// Message: Round $s1 of the Grand Olympiad Games has started!
	ROUND_S1_OF_THE_GRAND_OLYMPIAD_GAMES_HAS_STARTED(1639),
	// Message: Round $s1 of the Grand Olympiad Games has now ended.
	ROUND_S1_OF_THE_GRAND_OLYMPIAD_GAMES_HAS_NOW_ENDED(1640),
	// Message: Sharpen your swords, tighten the stitching in your armor, and make haste to a Grand Olympiad Manager!  Battles in the Grand Olympiad Games are now taking place!
	SHARPEN_YOUR_SWORDS_TIGHTEN_THE_STITCHING_IN_YOUR_ARMOR_AND_MAKE_HASTE_TO_A_GRAND_OLYMPIAD_MANAGER__BATTLES_IN_THE_GRAND_OLYMPIAD_GAMES_ARE_NOW_TAKING_PLACE(1641),
	// Message: Much carnage has been left for the cleanup crew of the Olympiad Stadium.  Battles in the Grand Olympiad Games are now over!
	MUCH_CARNAGE_HAS_BEEN_LEFT_FOR_THE_CLEANUP_CREW_OF_THE_OLYMPIAD_STADIUM(1642),
	// Message: Current Location: $s1, $s2, $s3 (Dimensional Gap)
	CURRENT_LOCATION_S1_S2_S3_DIMENSIONAL_GAP(1643),
	// Message: The Grand Olympiad Games are not currently in progress.
	THE_GRAND_OLYMPIAD_GAMES_ARE_NOT_CURRENTLY_IN_PROGRESS(1651),
	// Message: You caught something smelly and scary, maybe you should throw it back!?
	YOU_CAUGHT_SOMETHING_SMELLY_AND_SCARY_MAYBE_YOU_SHOULD_THROW_IT_BACK(1655),
	// Message: $c1 has earned $s2 points in the Grand Olympiad Games.
	C1_HAS_EARNED_S2_POINTS_IN_THE_GRAND_OLYMPIAD_GAMES(1657),
	// Message: $c1 has lost $s2 points in the Grand Olympiad Games.
	C1_HAS_LOST_S2_POINTS_IN_THE_GRAND_OLYMPIAD_GAMES(1658),
	// Message: Current Location: $s1, $s2, $s3 (Cemetery of the Empire).
	CURRENT_LOCATION_S1_S2_S3_CEMETERY_OF_THE_EMPIRE(1659),
	// Message: The clan crest was successfully registered.  Remember, only a clan that owns a clan hall or castle can display a crest.
	THE_CLAN_CREST_WAS_SUCCESSFULLY_REGISTERED__REMEMBER_ONLY_A_CLAN_THAT_OWNS_A_CLAN_HALL_OR_CASTLE_CAN_DISPLAY_A_CREST(1663),
	// Message: Lethal Strike!
	LETHAL_STRIKE(1667),
	// Message: Your lethal strike was successful!
	YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL(1668),
	// Message: There was nothing found inside.
	THERE_WAS_NOTHING_FOUND_INSIDE(1669),
	// Message: Due to your Reeling and/or Pumping skill being three or more levels higher than your Fishing skill, a 50 damage penalty will be applied.
	DUE_TO_YOUR_REELING_ANDOR_PUMPING_SKILL_BEING_THREE_OR_MORE_LEVELS_HIGHER_THAN_YOUR_FISHING_SKILL_A_50_DAMAGE_PENALTY_WILL_BE_APPLIED(1670),
	// Message: Your reeling was successful! (Mastery Penalty:$s1 )
	YOUR_REELING_WAS_SUCCESSFUL_MASTERY_PENALTY_S1(1671),
	// Message: Your pumping was successful! (Mastery Penalty:$s1 )
	YOUR_PUMPING_WAS_SUCCESSFUL_MASTERY_PENALTY_S1(1672),
	// Message: For the current Grand Olympiad you have participated in $s1 match(es). $s2 win(s) and $s3 defeat(s). You currently have $s4 Olympiad Point(s).
	FOR_THE_CURRENT_GRAND_OLYMPIAD_YOU_HAVE_PARTICIPATED_IN_S1_MATCHES_S2_WINS_S3_DEFEATS_YOU_CURRENTLY_HAVE_S4_OLYMPIAD_POINTS(1673),
	// Message: This command can only be used by a Noblesse.
	THIS_COMMAND_CAN_ONLY_BE_USED_BY_A_NOBLESSE(1674),
	// Message: A manor cannot be set up between 4:30 am and 8 pm.
	A_MANOR_CANNOT_BE_SET_UP_BETWEEN_430_AM_AND_8_PM(1675),
	// Message: You do not have a servitor or pet and therefore cannot use the automatic-use function.
	YOU_DO_NOT_HAVE_A_SERVITOR_OR_PET_AND_THEREFORE_CANNOT_USE_THE_AUTOMATICUSE_FUNCTION(1676),
	// Message: A cease-fire during a Clan War can not be called while members of your clan are engaged in battle.
	A_CEASEFIRE_DURING_A_CLAN_WAR_CAN_NOT_BE_CALLED_WHILE_MEMBERS_OF_YOUR_CLAN_ARE_ENGAGED_IN_BATTLE(1677),
	// Message: You have not declared a Clan War against the clan $s1.
	YOU_HAVE_NOT_DECLARED_A_CLAN_WAR_AGAINST_THE_CLAN_S1(1678),
	// Message: Only the creator of a command channel can issue a global command.
	ONLY_THE_CREATOR_OF_A_COMMAND_CHANNEL_CAN_ISSUE_A_GLOBAL_COMMAND(1679),
	// Message: $c1 has declined the channel invitation.
	C1_HAS_DECLINED_THE_CHANNEL_INVITATION(1680),
	// Message: Only the creator of a command channel can use the channel dismiss command.
	ONLY_THE_CREATOR_OF_A_COMMAND_CHANNEL_CAN_USE_THE_CHANNEL_DISMISS_COMMAND(1682),
	// Message: Only a party leader can leave a command channel.
	ONLY_A_PARTY_LEADER_CAN_LEAVE_A_COMMAND_CHANNEL(1683),
	// Message: This area cannot be entered while mounted atop of a Wyvern.  You will be dismounted from your Wyvern if you do not leave!
	THIS_AREA_CANNOT_BE_ENTERED_WHILE_MOUNTED_ATOP_OF_A_WYVERN(1687),
	// Message: You cannot enchant while operating a Private Store or Private Workshop.
	YOU_CANNOT_ENCHANT_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP(1688),
	// Message: $c1 is already registered on the class match waiting list.
	C1_IS_ALREADY_REGISTERED_ON_THE_CLASS_MATCH_WAITING_LIST(1689),
	// Message: $c1 is already registered on the waiting list for the class irrelevant individual match.
	C1_IS_ALREADY_REGISTERED_ON_THE_WAITING_LIST_FOR_THE_CLASS_IRRELEVANT_INDIVIDUAL_MATCH(1690),
	// Message: You may not observe a Grand Olympiad Games match while you are on the waiting list.
	YOU_MAY_NOT_OBSERVE_A_GRAND_OLYMPIAD_GAMES_MATCH_WHILE_YOU_ARE_ON_THE_WAITING_LIST(1693),
	// Message: Only a clan leader that is a Noblesse can view the Siege War Status window during a siege war.
	ONLY_A_CLAN_LEADER_THAT_IS_A_NOBLESSE_CAN_VIEW_THE_SIEGE_WAR_STATUS_WINDOW_DURING_A_SIEGE_WAR(1694),
	// Message: You cannot dismiss a party member by force.
	YOU_CANNOT_DISMISS_A_PARTY_MEMBER_BY_FORCE(1699),
	// Message: You don't have enough spiritshots needed for a pet/servitor.
	YOU_DONT_HAVE_ENOUGH_SPIRITSHOTS_NEEDED_FOR_A_PETSERVITOR(1700),
	// Message: You don't have enough soulshots needed for a pet/servitor.
	YOU_DONT_HAVE_ENOUGH_SOULSHOTS_NEEDED_FOR_A_PETSERVITOR(1701),
	// Message: You acquired $s1 PC Point(s).
	YOU_ACQUIRED_S1_PC_BANG_POINT(1707),
	// Message: Double points! You acquired $s1 PC Point(s).
	DOUBLE_POINTS_YOU_ACQUIRED_S1_PC_BANG_POINT(1708),
	// Message: You are using $s1 PC Point(s).
	YOU_ARE_USING_S1_POINT(1709),
	// Message: You don't have enough PC Points.
	YOU_ARE_SHORT_OF_ACCUMULATED_POINTS(1710),
	// Message: Current Location: $s1, $s2, $s3 (Near the Town of Schuttgart)
	CURRENT_LOCATION_S1_S2_S3_NEAR_THE_TOWN_OF_SCHUTTGART(1714),
	// Message: You have earned $s1 raid point(s).
	YOU_HAVE_EARNED_S1_RAID_POINTS(1725),
	// Message: You cannot join a Command Channel while teleporting.
	YOU_CANNOT_JOIN_A_COMMAND_CHANNEL_WHILE_TELEPORTING(1729),
	// Message: To join a Clan Academy, characters must be Level 40 or below, not belong another clan and not yet completed their 2nd class transfer.
	TO_JOIN_A_CLAN_ACADEMY_CHARACTERS_MUST_BE_LEVEL_40_OR_BELOW_NOT_BELONG_ANOTHER_CLAN_AND_NOT_YET_COMPLETED_THEIR_2ND_CLASS_TRANSFER(1734),
	// Message: Your clan has already established a Clan Academy.
	YOUR_CLAN_HAS_ALREADY_ESTABLISHED_A_CLAN_ACADEMY(1738),
	// Message: Clan Academy member $s1 has successfully completed the 2nd class transfer and obtained $s2 Clan Reputation points.
	CLAN_ACADEMY_MEMBER_S1_HAS_SUCCESSFULLY_COMPLETED_THE_2ND_CLASS_TRANSFER_AND_OBTAINED_S2_CLAN_REPUTATION_POINTS(1748),
	// Message: Congratulations! You will now graduate from the Clan Academy and leave your current clan. As a graduate of the academy, you can immediately join a clan as a regular member without being subject to any penalties.
	CONGRATULATIONS_YOU_WILL_NOW_GRADUATE_FROM_THE_CLAN_ACADEMY_AND_LEAVE_YOUR_CURRENT_CLAN(1749),
	// Message: $s2 has been designated as the apprentice of clan member $s1.
	S2_HAS_BEEN_DESIGNATED_AS_THE_APPRENTICE_OF_CLAN_MEMBER_S1(1755),
	// Message: Your apprentice, $s1, has logged in.
	YOUR_APPRENTICE_C1_HAS_LOGGED_IN(1756),
	// Message: Your apprentice, $c1, has logged out.
	YOUR_APPRENTICE_C1_HAS_LOGGED_OUT(1757),
	// Message: Your sponsor, $c1, has logged in.
	YOUR_SPONSOR_C1_HAS_LOGGED_IN(1758),
	// Message: Your sponsor, $c1, has logged out.
	YOUR_SPONSOR_C1_HAS_LOGGED_OUT(1759),
	// Message: $s2, clan member $c1's apprentice, has been removed.
	S2_CLAN_MEMBER_C1S_APPRENTICE_HAS_BEEN_REMOVED(1763),
	// Message: This item can only be worn by a member of the Clan Academy.
	THIS_ITEM_CAN_ONLY_BE_WORN_BY_A_MEMBER_OF_THE_CLAN_ACADEMY(1764),
	// Message: Now that your clan level is above Level 5, it can accumulate clan reputation points.
	NOW_THAT_YOUR_CLAN_LEVEL_IS_ABOVE_LEVEL_5_IT_CAN_ACCUMULATE_CLAN_REPUTATION_POINTS(1771),
	// Message: Since your clan emerged victorious from the siege, $s1 points have been added to your clan's reputation score.
	SINCE_YOUR_CLAN_EMERGED_VICTORIOUS_FROM_THE_SIEGE_S1_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLANS_REPUTATION_SCORE(1773),
	// Message: Clan member $c1 was an active member of the highest-ranked party in the Festival of Darkness. $s2 points have been added to your clan's reputation score.
	CLAN_MEMBER_C1_WAS_AN_ACTIVE_MEMBER_OF_THE_HIGHESTRANKED_PARTY_IN_THE_FESTIVAL_OF_DARKNESS_S2_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLANS_REPUTATION_SCORE(1775),
	// Message: Clan member $c1 was named a hero. $2s points have been added to your clan's reputation score.
	CLAN_MEMBER_C1_WAS_NAMED_A_HERO_S2_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLANS_REPUTATION_SCORE(1776),
	// Message: You have successfully completed a clan quest. $s1 points have been added to your clan's reputation score.
	YOU_HAVE_SUCCESSFULLY_COMPLETED_A_CLAN_QUEST_S1_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLANS_REPUTATION_SCORE(1777),
	// Message: Your clan has added $1s points to its clan reputation score.
	YOUR_CLAN_HAS_ADDED_S1_POINTS_TO_ITS_CLAN_REPUTATION_SCORE(1781),
	// Message: Your clan member, $c1, was killed. $s2 points have been deducted from your clan's reputation score and added to your opponent's clan reputation score.
	YOUR_CLAN_MEMBER_C1_WAS_KILLED_S2_POINTS_HAVE_BEEN_DEDICTED_FROM_YOUR_CLAN_REPUTATION_SCORE(1782),
	// Message: For killing an opposing clan member, $s1 points have been deducted from your opponents' clan reputation score.
	FOR_KILLING_AN_OPPOSING_CLAN_MEMBER_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_OPPONENTS_CLAN_REPUTATION_SCORE(1783),
	// Message: Your clan has failed to defend the castle. $s1 points have been deducted from your clan's reputation score and added to your opponents'.
	YOUR_CLAN_HAS_FAILED_TO_DEFEND_THE_CASTLE_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOU_CLAN_REPUTATION_SCORE_AND_ADDED_TO_YOUR_OPPONENTS(1784),
	// Message: $s1 points have been deducted from the clan's reputation score.
	S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_THE_CLANS_REPUTATION_SCORE(1787),
	// Message: The clan skill $s1 has been added.
	THE_CLAN_SKILL_S1_HAS_BEEN_ADDED(1788),
	// Message: Since the Clan Reputation Score has dropped to 0 or lower, your clan skill(s) will be de-activated.
	SINCE_THE_CLAN_REPUTATION_SCORE_HAS_DROPPED_TO_0_OR_LOWER_YOUR_CLAN_SKILLS_WILL_BE_DEACTIVATED(1789),
	// Message: The conditions necessary to create a military unit have not been met.
	THE_CONDITIONS_NECESSARY_TO_CREATE_A_MILITARY_UNIT_HAVE_NOT_BEEN_MET(1791),
	// Message: The attempt to sell has failed.
	THE_ATTEMPT_TO_SELL_HAS_FAILED(1801),
	// Message: The attempt to trade has failed.
	THE_ATTEMPT_TO_TRADE_HAS_FAILED(1802),
	// Message: $s1 has $s2 minute(s) of usage time remaining.
	S1_HAS_S2_MINUTES_OF_USAGE_TIME_REMAINING(1814),
	// Message: $s2 was dropped in the $s1 region.
	S2_WAS_DROPPED_IN_THE_S1_REGION(1815),
	// Message: The owner of $s2 has appeared in the $s1 region.
	THE_OWNER_OF_S2_HAS_APPEARED_IN_THE_S1_REGION(1816),
	// Message: $s1 has disappeared.
	ID1818_S1_HAS_DISAPPEARED(1818),
	// Message: The registration period for a clan hall war has ended.
	THE_REGISTRATION_PERIOD_FOR_A_CLAN_HALL_WAR_HAS_ENDED(1823),
	// Message: You have been registered for a clan hall war.  Please move to the left side of the clan hall's arena and get ready.
	YOU_HAVE_BEEN_REGISTERED_FOR_A_CLAN_HALL_WAR(1824),
	// Message: You have failed in your attempt to register for the clan hall war. Please try again.
	YOU_HAVE_FAILED_IN_YOUR_ATTEMPT_TO_REGISTER_FOR_THE_CLAN_HALL_WAR(1825),
	// Message: In $s1 minute(s), the game will begin. All players must hurry and move to the left side of the clan hall's arena.
	IN_S1_MINUTES_THE_GAME_WILL_BEGIN_ALL_PLAYERS_MUST_HURRY_AND_MOVE_TO_THE_LEFT_SIDE_OF_THE_CLAN_HALLS_ARENA(1826),
	// Message: In $s1 minute(s), the game will begin. All players, please enter the arena now.
	IN_S1_MINUTES_THE_GAME_WILL_BEGIN_ALL_PLAYERS_PLEASE_ENTER_THE_ARENA_NOW(1827),
	// Message: In $s1 second(s), the game will begin.
	IN_S1_SECONDS_THE_GAME_WILL_BEGIN(1828),
	// Message: $c1 is not allowed to use the party room invite command. Please update the waiting list.
	C1_IS_NOT_ALLOWED_TO_USE_THE_PARTY_ROOM_INVITE_COMMAND(1830),
	// Message: $c1 does not meet the conditions of the party room. Please update the waiting list.
	C1_DOES_NOT_MEET_THE_CONDITIONS_OF_THE_PARTY_ROOM(1831),
	// Message: Only a room leader may invite others to a party room.
	ONLY_A_ROOM_LEADER_MAY_INVITE_OTHERS_TO_A_PARTY_ROOM(1832),
	// Message: The party room is full. No more characters can be invited in.
	THE_PARTY_ROOM_IS_FULL(1834),
	// Message: $s1 is full and cannot accept additional clan members at this time.
	S1_IS_FULL_AND_CANNOT_ACCEPT_ADDITIONAL_CLAN_MEMBERS_AT_THIS_TIME(1835),
	// Message: This clan hall war has been cancelled.  Not enough clans have registered.
	THIS_CLAN_HALL_WAR_HAS_BEEN_CANCELLED(1841),
	// Message: $c1 wishes to summon you from $s2. Do you accept?
	C1_WISHES_TO_SUMMON_YOU_FROM_S2(1842),
	// Message: $c1 is engaged in combat and cannot be summoned.
	C1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED(1843),
	// Message: $c1 is dead at the moment and cannot be summoned.
	C1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED(1844),
	// Message: Hero weapons cannot be destroyed.
	HERO_WEAPONS_CANNOT_BE_DESTROYED(1845),
	// Message: Another military unit is already using that name. Please enter a different name.
	ANOTHER_MILITARY_UNIT_IS_ALREADY_USING_THAT_NAME_PLEASE_ENTER_A_DIFFERENT_NAME(1855),
	// Message: The Clan Reputation Score is too low.
	THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW(1860),
	// Message: Clan skills will now be activated since the clan's reputation score is 0 or higher.
	CLAN_SKILLS_WILL_NOW_BE_ACTIVATED_SINCE_THE_CLANS_REPUTATION_SCORE_IS_0_OR_HIGHER(1862),
	// Message: Your pet/servitor is unresponsive and will not obey any orders.
	YOUR_PETSERVITOR_IS_UNRESPONSIVE_AND_WILL_NOT_OBEY_ANY_ORDERS(1864),
	// Message: $c1 has granted the Command Channel's master party the privilege of item looting.
	C1_HAS_GRANTED_THE_COMMAND_CHANNELS_MASTER_PARTY_THE_PRIVILEGE_OF_ITEM_LOOTING(1869),
	// Message: A Command Channel with looting rights already exists.
	A_COMMAND_CHANNEL_WITH_LOOTING_RIGHTS_ALREADY_EXISTS(1870),
	// Message: The preliminary match will begin in $s1 second(s). Prepare yourself.
	THE_PRELIMINARY_MATCH_WILL_BEGIN_IN_S1_SECONDS(1881),
	// Message: There are no offerings I own or I made a bid for.
	THERE_ARE_NO_OFFERINGS_I_OWN_OR_I_MADE_A_BID_FOR(1883),
	// Message: You may not use items in a private store or private work shop.
	YOU_MAY_NOT_USE_ITEMS_IN_A_PRIVATE_STORE_OR_PRIVATE_WORK_SHOP(1891),
	// Message: A sub-class cannot be created or changed while you are over your weight limit.
	A_SUBCLASS_CANNOT_BE_CREATED_OR_CHANGED_WHILE_YOU_ARE_OVER_YOUR_WEIGHT_LIMIT(1894),
	// Message: $c1 is currently trading or operating a private store and cannot be summoned.
	C1_IS_CURRENTLY_TRADING_OR_OPERATING_A_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED(1898),
	// Message: Your target is in an area which blocks summoning.
	YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING(1899),
	// Message: $c1 has entered the party room.
	C1_HAS_ENTERED_THE_PARTY_ROOM(1900),
	// Message: $s1 has sent an invitation to room <$s2>.
	S1_HAS_SENT_AN_INVITATION_TO_ROOM_S2(1901),
	// Message: Incompatible item grade.  This item cannot be used.
	INCOMPATIBLE_ITEM_GRADE(1902),
	// Message: A sub-class may not be created or changed while a servitor or pet is summoned.
	A_SUBCLASS_MAY_NOT_BE_CREATED_OR_CHANGED_WHILE_A_SERVITOR_OR_PET_IS_SUMMONED(1904),
	// Message: You cannot summon players who are currently participating in the Grand Olympiad.
	YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_CURRENTLY_PARTICIPATING_IN_THE_GRAND_OLYMPIAD(1911),
	// Message: The game will end in $s1 second(s).
	THE_GAME_WILL_END_IN_S1_SECONDS_(1915),
	// Message: Your Death Penalty is now level $s1.
	YOUR_DEATH_PENALTY_IS_NOW_LEVEL_S1(1916),
	// Message: Your Death Penalty has been lifted.
	YOUR_DEATH_PENALTY_HAS_BEEN_LIFTED(1917),
	// Message: Your pet is too high level to control.
	YOUR_PET_IS_TOO_HIGH_LEVEL_TO_CONTROL(1918),
	// Message: Court Magician: The portal has been created!
	COURT_MAGICIAN_THE_PORTAL_HAS_BEEN_CREATED(1923),
	// Message: Current Location: $s1, $s2, $s3 (near the Primeval Isle)
	CURRENT_LOCATION_S1_S2_S3_NEAR_THE_PRIMEVAL_ISLE(1924),
	// Message: There is no opponent to receive your challenge for a duel.
	THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL(1926),
	// Message: $c1 has been challenged to a duel.
	C1_HAS_BEEN_CHALLENGED_TO_A_DUEL(1927),
	// Message: $c1's party has been challenged to a duel.
	C1S_PARTY_HAS_BEEN_CHALLENGED_TO_A_DUEL(1928),
	// Message: $c1 has accepted your challenge to a duel. The duel will begin in a few moments.
	C1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_A_DUEL(1929),
	// Message: You have accepted $c1's challenge a duel. The duel will begin in a few moments.
	YOU_HAVE_ACCEPTED_C1S_CHALLENGE_A_DUEL(1930),
	// Message: $c1 has declined your challenge to a duel.
	C1_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL(1931),
	// Message: $c1 has declined your challenge to a duel.
	C1_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL_(1932),
	// Message: You have accepted $c1's challenge to a party duel. The duel will begin in a few moments.
	YOU_HAVE_ACCEPTED_C1S_CHALLENGE_TO_A_PARTY_DUEL(1933),
	// Message: $s1 has accepted your challenge to duel against their party. The duel will begin in a few moments.
	S1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_DUEL_AGAINST_THEIR_PARTY(1934),
	// Message: $c1 has declined your challenge to a party duel.
	C1_HAS_DECLINED_YOUR_CHALLENGE_TO_A_PARTY_DUEL(1935),
	// Message: The opposing party has declined your challenge to a duel.
	THE_OPPOSING_PARTY_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL(1936),
	// Message: Since the person you challenged is not currently in a party, they cannot duel against your party.
	SINCE_THE_PERSON_YOU_CHALLENGED_IS_NOT_CURRENTLY_IN_A_PARTY_THEY_CANNOT_DUEL_AGAINST_YOUR_PARTY(1937),
	// Message: $c1 has challenged you to a duel.
	C1_HAS_CHALLENGED_YOU_TO_A_DUEL(1938),
	// Message: $c1's party has challenged your party to a duel.
	C1S_PARTY_HAS_CHALLENGED_YOUR_PARTY_TO_A_DUEL(1939),
	// Message: You are unable to request a duel at this time.
	YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME(1940),
	// Message: The opposing party is currently unable to accept a challenge to a duel.
	THE_OPPOSING_PARTY_IS_CURRENTLY_UNABLE_TO_ACCEPT_A_CHALLENGE_TO_A_DUEL(1942),
	// Message: In a moment, you will be transported to the site where the duel will take place.
	IN_A_MOMENT_YOU_WILL_BE_TRANSPORTED_TO_THE_SITE_WHERE_THE_DUEL_WILL_TAKE_PLACE(1944),
	// Message: The duel will begin in $s1 second(s).
	THE_DUEL_WILL_BEGIN_IN_S1_SECONDS(1945),
	// Message: Let the duel begin!
	LET_THE_DUEL_BEGIN(1949),
	// Message: $c1 has won the duel.
	C1_HAS_WON_THE_DUEL(1950),
	// Message: $c1's party has won the duel.
	C1S_PARTY_HAS_WON_THE_DUEL(1951),
	// Message: The duel has ended in a tie.
	THE_DUEL_HAS_ENDED_IN_A_TIE(1952),
	// Message: Select the item to be augmented.
	SELECT_THE_ITEM_TO_BE_AUGMENTED(1957),
	// Message: Select the catalyst for augmentation.
	SELECT_THE_CATALYST_FOR_AUGMENTATION(1958),
	// Message: Requires $s2 $s1.
	REQUIRES_S2_S1(1959),
	// Message: This is not a suitable item.
	THIS_IS_NOT_A_SUITABLE_ITEM(1960),
	// Message: Gemstone quantity is incorrect.
	GEMSTONE_QUANTITY_IS_INCORRECT(1961),
	// Message: The item was successfully augmented!
	THE_ITEM_WAS_SUCCESSFULLY_AUGMENTED(1962),
	// Message: Select the item from which you wish to remove augmentation.
	SELECT_THE_ITEM_FROM_WHICH_YOU_WISH_TO_REMOVE_AUGMENTATION(1963),
	// Message: Augmentation removal can only be done on an augmented item.
	AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM(1964),
	// Message: Augmentation has been successfully removed from your $s1.
	AUGMENTATION_HAS_BEEN_SUCCESSFULLY_REMOVED_FROM_YOUR_S1(1965),
	// Message: Only the clan leader may issue commands.
	ONLY_THE_CLAN_LEADER_MAY_ISSUE_COMMANDS(1966),
	// Message: Once an item is augmented, it cannot be augmented again.
	ONCE_AN_ITEM_IS_AUGMENTED_IT_CANNOT_BE_AUGMENTED_AGAIN(1970),
	// Message: You cannot augment items while a private store or private workshop is in operation.
	YOU_CANNOT_AUGMENT_ITEMS_WHILE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP_IS_IN_OPERATION(1972),
	// Message: You cannot augment items while dead.
	YOU_CANNOT_AUGMENT_ITEMS_WHILE_DEAD(1974),
	// Message: You cannot augment items while paralyzed.
	YOU_CANNOT_AUGMENT_ITEMS_WHILE_PARALYZED(1976),
	// Message: You cannot augment items while fishing.
	YOU_CANNOT_AUGMENT_ITEMS_WHILE_FISHING(1977),
	// Message: You cannot augment items while sitting down.
	YOU_CANNOT_AUGMENT_ITEMS_WHILE_SITTING_DOWN(1978),
	// Message: $s1's remaining Mana is now 10.
	S1S_REMAINING_MANA_IS_NOW_10(1979),
	// Message: $s1's remaining Mana is now 5.
	S1S_REMAINING_MANA_IS_NOW_5(1980),
	// Message: $s1's remaining Mana is now 1. It will disappear soon.
	S1S_REMAINING_MANA_IS_NOW_1(1981),
	// Message: $s1's remaining Mana is now 0, and the item has disappeared.
	S1S_REMAINING_MANA_IS_NOW_0_AND_THE_ITEM_HAS_DISAPPEARED(1982),
	// Message: $s1
	S1(1983),
	// Message: Press the Augment button to begin.
	PRESS_THE_AUGMENT_BUTTON_TO_BEGIN(1984),
	// Message: The ferry has arrived at Primeval Isle.
	THE_FERRY_HAS_ARRIVED_AT_PRIMEVAL_ISLE(1988),
	// Message: The ferry will leave for Rune Harbor after anchoring for three minutes.
	THE_FERRY_WILL_LEAVE_FOR_RUNE_HARBOR_AFTER_ANCHORING_FOR_THREE_MINUTES(1989),
	// Message: The ferry is now departing Primeval Isle for Rune Harbor.
	THE_FERRY_IS_NOW_DEPARTING_PRIMEVAL_ISLE_FOR_RUNE_HARBOR(1990),
	// Message: The ferry will leave for Primeval Isle after anchoring for three minutes.
	THE_FERRY_WILL_LEAVE_FOR_PRIMEVAL_ISLE_AFTER_ANCHORING_FOR_THREE_MINUTES(1991),
	// Message: The ferry is now departing Rune Harbor for Primeval Isle.
	THE_FERRY_IS_NOW_DEPARTING_RUNE_HARBOR_FOR_PRIMEVAL_ISLE(1992),
	// Message: The ferry from Primeval Isle to Rune Harbor has been delayed.
	THE_FERRY_FROM_PRIMEVAL_ISLE_TO_RUNE_HARBOR_HAS_BEEN_DELAYED(1993),
	// Message: The attack has been blocked.
	THE_ATTACK_HAS_BEEN_BLOCKED(1996),
	// Message: $c1 is performing a counterattack.
	C1_IS_PERFORMING_A_COUNTERATTACK(1997),
	// Message: You countered $c1's attack.
	YOU_COUNTERED_C1S_ATTACK(1998),
	// Message: $c1 dodges the attack.
	C1_DODGES_THE_ATTACK(1999),
	// Message: You have avoided $c1's attack.
	ID2000_YOU_HAVE_AVOIDED_C1S_ATTACK(2000),
	// Message: Augmentation failed due to inappropriate conditions.
	AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS(2001),
	// Message: A skill is ready to be used again.
	A_SKILL_IS_READY_TO_BE_USED_AGAIN(2015),
	// Message: A skill is ready to be used again but its re-use counter time has increased.
	A_SKILL_IS_READY_TO_BE_USED_AGAIN_BUT_ITS_REUSE_COUNTER_TIME_HAS_INCREASED(2016),
	// Message: $c1 cannot duel because $c1 is currently engaged in a private store or manufacture.
	C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_ENGAGED_IN_A_PRIVATE_STORE_OR_MANUFACTURE(2017),
	// Message: $c1 cannot duel because $c1 is currently fishing.
	C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_FISHING(2018),
	// Message: $c1 cannot duel because $c1's HP or MP is below 50%.
	C1_CANNOT_DUEL_BECAUSE_C1S_HP_OR_MP_IS_BELOW_50(2019),
	// Message: $c1 cannot make a challenge to a duel because $c1 is currently in a duel-prohibited area (Peaceful Zone / Seven Signs Zone / Near Water / Restart Prohibited Area).
	C1_CANNOT_MAKE_A_CHALLENGE_TO_A_DUEL_BECAUSE_C1_IS_CURRENTLY_IN_A_DUELPROHIBITED_AREA_PEACEFUL_ZONE__SEVEN_SIGNS_ZONE__NEAR_WATER__RESTART_PROHIBITED_AREA(2020),
	// Message: $c1 cannot duel because $c1 is currently engaged in battle.
	C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_ENGAGED_IN_BATTLE(2021),
	// Message: $c1 cannot duel because $c1 is already engaged in a duel.
	C1_CANNOT_DUEL_BECAUSE_C1_IS_ALREADY_ENGAGED_IN_A_DUEL(2022),
	// Message: $c1 cannot duel because $c1 is in a chaotic state.
	C1_CANNOT_DUEL_BECAUSE_C1_IS_IN_A_CHAOTIC_STATE(2023),
	// Message: $c1 cannot duel because $c1 is participating in the Olympiad.
	C1_CANNOT_DUEL_BECAUSE_C1_IS_PARTICIPATING_IN_THE_OLYMPIAD(2024),
	// Message: $c1 cannot duel because $c1 is participating in a clan hall war.
	C1_CANNOT_DUEL_BECAUSE_C1_IS_PARTICIPATING_IN_A_CLAN_HALL_WAR(2025),
	// Message: $c1 cannot duel because $c1 is participating in a siege war.
	C1_CANNOT_DUEL_BECAUSE_C1_IS_PARTICIPATING_IN_A_SIEGE_WAR(2026),
	// Message: $c1 cannot duel because $c1 is currently riding a boat, steed, or strider.
	C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_RIDING_A_BOAT_STEED_OR_STRIDER(2027),
	// Message: $c1 cannot receive a duel challenge because $c1 is too far away.
	C1_CANNOT_RECEIVE_A_DUEL_CHALLENGE_BECAUSE_C1_IS_TOO_FAR_AWAY(2028),
	// Message: A sub-class cannot be created or changed because you have exceeded your inventory limit.
	A_SUBCLASS_CANNOT_BE_CREATED_OR_CHANGED_BECAUSE_YOU_HAVE_EXCEEDED_YOUR_INVENTORY_LIMIT(2033),
	// Message: There are $s1 hours(s) and $s2 minute(s) remaining until the item can be purchased again.
	THERE_ARE_S1_HOURSS_AND_S2_MINUTES_REMAINING_UNTIL_THE_ITEM_CAN_BE_PURCHASED_AGAIN(2034),
	// Message: There are $s1 minute(s) remaining until the item can be purchased again.
	THERE_ARE_S1_MINUTES_REMAINING_UNTIL_THE_ITEM_CAN_BE_PURCHASED_AGAIN(2035),
	// Message: Some Lineage II features have been limited for free trials. Trial accounts aren't allowed to trade items and/or Adena.  To unlock all of the features of Lineage II, purchase the full version today.
	SOME_LINEAGE_II_FEATURES_HAVE_BEEN_LIMITED_FOR_FREE_TRIALS_(2039),
	// Message: Some Lineage II features have been limited for free trials. Trial accounts aren't allowed buy items from private stores. To unlock all of the features of Lineage II, purchase the full version today.
	SOME_LINEAGE_II_FEATURES_HAVE_BEEN_LIMITED_FOR_FREE_TRIALS_____(2046),
	// Message: $s1 clan is trying to display a flag.
	S1_CLAN_IS_TRYING_TO_DISPLAY_A_FLAG(2050),
	// Message: You have blocked $c1.
	YOU_HAVE_BLOCKED_C1(2057),
	// Message: You already polymorphed and cannot polymorph again.
	YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN(2058),
	// Message: You cannot polymorph into the desired form in water.
	YOU_CANNOT_POLYMORPH_INTO_THE_DESIRED_FORM_IN_WATER(2060),
	// Message: You cannot polymorph when you have summoned a servitor/pet.
	YOU_CANNOT_POLYMORPH_WHEN_YOU_HAVE_SUMMONED_A_SERVITORPET(2062),
	// Message: You cannot polymorph while riding a pet.
	YOU_CANNOT_POLYMORPH_WHILE_RIDING_A_PET(2063),
	// Message: You cannot polymorph while under the effect of a special skill.
	YOU_CANNOT_POLYMORPH_WHILE_UNDER_THE_EFFECT_OF_A_SPECIAL_SKILL(2064),
	// Message: That weapon cannot perform any attacks.
	THAT_WEAPON_CANNOT_PERFORM_ANY_ATTACKS(2066),
	// Message: That weapon cannot use any other skill except the weapon's skill.
	THAT_WEAPON_CANNOT_USE_ANY_OTHER_SKILL_EXCEPT_THE_WEAPONS_SKILL(2067),
	// Message: You do not have all of the items needed to untrain the enchant skill.
	YOU_DO_NOT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_UNTRAIN_THE_ENCHANT_SKILL(2068),
	// Message: Untrain of enchant skill was successful. Current level of enchant skill $s1 has been decreased by 1.
	UNTRAIN_OF_ENCHANT_SKILL_WAS_SUCCESSFUL_S1(2069),
	// Message: Untrain of enchant skill was successful. Current level of enchant skill $s1 became 0 and enchant skill will be initialized.
	UNTRAIN_OF_ENCHANT_SKILL_WAS_SUCCESSFUL_S1_(2070),
	// Message: You do not have all of the items needed to enchant skill route change.
	YOU_DO_NOT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_SKILL_ROUTE_CHANGE(2071),
	// Message: Enchant skill route change was successful. Lv of enchant skill $s1 has been decreased by $s2.
	ENCHANT_SKILL_ROUTE_CHANGE_WAS_SUCCESSFUL_S1_LEVEL_DECREASED_BY_S2(2072),
	// Message: Enchant skill route change was successful. Lv of enchant skill $s1 will remain.
	ENCHANT_SKILL_ROUTE_CHANGE_WAS_SUCCESSFUL_S1(2073),
	// Message: Skill enchant failed. Current level of enchant skill $s1 will remain unchanged.
	SKILL_ENCHANT_FAILED_S1(2074),
	// Message: It is not an auction period.
	IT_IS_NOT_AN_AUCTION_PERIOD(2075),
	// Message: Bidding is not allowed because the maximum bidding price exceeds 100 billion.
	BIDDING_IS_NOT_ALLOWED_BECAUSE_THE_MAXIMUM_BIDDING_PRICE_EXCEEDS_100_BILLION(2076),
	// Message: Your bid must be higher than the current highest bid.
	YOUR_BID_MUST_BE_HIGHER_THAN_THE_CURRENT_HIGHEST_BID(2077),
	// Message: You do not have enough adena for this bid.
	YOU_DO_NOT_HAVE_ENOUGH_ADENA_FOR_THIS_BID(2078),
	// Message: You have been outbid.
	YOU_HAVE_BEEN_OUTBID(2080),
	// Message: There are no funds presently due to you.
	THERE_ARE_NO_FUNDS_PRESENTLY_DUE_TO_YOU(2081),
	// Message: Enemy Blood Pledges have intruded into the fortress.
	ENEMY_BLOOD_PLEDGES_HAVE_INTRUDED_INTO_THE_FORTRESS(2084),
	// Message: Shout and trade chatting cannot be used while possessing a cursed weapon.
	SHOUT_AND_TRADE_CHATTING_CANNOT_BE_USED_WHILE_POSSESSING_A_CURSED_WEAPON(2085),
	// Message: Search on user $c2 for third-party program use will be completed in $s1 minute(s).
	SEARCH_ON_USER_C2_FOR_THIRDPARTY_PROGRAM_USE_WILL_BE_COMPLETED_IN_S1_MINUTES(2086),
	// Message: A fortress is under attack!
	A_FORTRESS_IS_UNDER_ATTACK(2087),
	// Message: $s1 minute(s) until the fortress battle starts.
	S1_MINUTES_UNTIL_THE_FORTRESS_BATTLE_STARTS(2088),
	// Message: $s1 second(s) until the fortress battle starts.
	S1_SECONDS_UNTIL_THE_FORTRESS_BATTLE_STARTS(2089),
	// Message: The fortress battle $s1 has begun.
	THE_FORTRESS_BATTLE_S1_HAS_BEGUN(2090),
	// Message: $c1 is in a location which cannot be entered, therefore it cannot be processed.
	C1_IS_IN_A_LOCATION_WHICH_CANNOT_BE_ENTERED_THEREFORE_IT_CANNOT_BE_PROCESSED(2096),
	// Message: $c1's level does not correspond to the requirements for entry.
	C1S_LEVEL_DOES_NOT_CORRESPOND_TO_THE_REQUIREMENTS_FOR_ENTRY(2097),
	// Message: $c1's quest requirement is not sufficient and cannot be entered.
	C1S_QUEST_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED(2098),
	// Message: $c1's item requirement is not sufficient and cannot be entered.
	C1S_ITEM_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED(2099),
	// Message: $c1 may not re-enter yet.
	C1_MAY_NOT_REENTER_YET(2100),
	// Message: You are not currently in a party, so you cannot enter.
	YOU_ARE_NOT_CURRENTLY_IN_A_PARTY_SO_YOU_CANNOT_ENTER(2101),
	// Message: You cannot enter due to the party having exceeded the limit.
	YOU_CANNOT_ENTER_DUE_TO_THE_PARTY_HAVING_EXCEEDED_THE_LIMIT(2102),
	// Message: You cannot enter because you are not associated with the current command channel.
	YOU_CANNOT_ENTER_BECAUSE_YOU_ARE_NOT_ASSOCIATED_WITH_THE_CURRENT_COMMAND_CHANNEL(2103),
	// Message: The maximum number of instance zones has been exceeded. You cannot enter.
	THE_MAXIMUM_NUMBER_OF_INSTANCE_ZONES_HAS_BEEN_EXCEEDED(2104),
	// Message: You have entered another instance zone, therefore you cannot enter corresponding dungeon.
	YOU_HAVE_ENTERED_ANOTHER_INSTANCE_ZONE_THEREFORE_YOU_CANNOT_ENTER_CORRESPONDING_DUNGEON(2105),
	// Message: This dungeon will expire in $s1 minute(s). You will be forced out of the dungeon when the time expires.
	THIS_DUNGEON_WILL_EXPIRE_IN_S1_MINUTES(2106),
	// Message: This instance zone will be terminated in $s1 minute(s). You will be forced out of the dungeon when the time expires.
	THIS_INSTANCE_ZONE_WILL_BE_TERMINATED_IN_S1_MINUTES_YOU_WILL_BE_FORCED_OUT_OF_THE_DUNGEON_WHEN_THE_TIME_EXPIRES(2107),
	// Message: You cannot convert this item.
	YOU_CANNOT_CONVERT_THIS_ITEM(2130),
	// Message: You have bid the highest price and have won the item. The item can be found in your personal warehouse.
	YOU_HAVE_BID_THE_HIGHEST_PRICE_AND_HAVE_WON_THE_ITEM_THE_ITEM_CAN_BE_FOUND_IN_YOUR_PERSONAL_WAREHOUSE(2131),
	// Message: You cannot add elemental power while operating a Private Store or Private Workshop.
	YOU_CANNOT_ADD_ELEMENTAL_POWER_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP(2143),
	// Message: Please select item to add elemental power.
	PLEASE_SELECT_ITEM_TO_ADD_ELEMENTAL_POWER(2144),
	// Message: Attribute item usage has been cancelled.
	ATTRIBUTE_ITEM_USAGE_HAS_BEEN_CANCELLED(2145),
	// Message: $s2 elemental power has been added successfully to $s1.
	S2_ELEMENTAL_POWER_HAS_BEEN_ADDED_SUCCESSFULLY_TO_S1(2147),
	// Message: $s3 elemental power has been added successfully to +$s1 $s2.
	S3_ELEMENTAL_POWER_HAS_BEEN_ADDED_SUCCESSFULLY_TO_S1_S2(2148),
	// Message: You have failed to add elemental power.
	YOU_HAVE_FAILED_TO_ADD_ELEMENTAL_POWER(2149),
	// Message: Another elemental power has already been added. This elemental power cannot be added.
	ANOTHER_ELEMENTAL_POWER_HAS_ALREADY_BEEN_ADDED_THIS_ELEMENTAL_POWER_CANNOT_BE_ADDED(2150),
	// Message: Your opponent has resistance to magic, the damage was decreased.
	YOUR_OPPONENT_HAS_RESISTANCE_TO_MAGIC_THE_DAMAGE_WAS_DECREASED(2151),
	// Message: The target is not a flagpole so a flag cannot be displayed.
	THE_TARGET_IS_NOT_A_FLAGPOLE_SO_A_FLAG_CANNOT_BE_DISPLAYED(2154),
	// Message: A flag is already being displayed, another flag cannot be displayed.
	A_FLAG_IS_ALREADY_BEING_DISPLAYED_ANOTHER_FLAG_CANNOT_BE_DISPLAYED(2155),
	// Message: There are not enough necessary items to use the skill.
	THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL(2156),
	// Message: Force attack is impossible against a temporary allied member during a siege.
	FORCE_ATTACK_IS_IMPOSSIBLE_AGAINST_A_TEMPORARY_ALLIED_MEMBER_DURING_A_SIEGE(2158),
	// Message: Bidder exists, the auction time has been extended by 5 minutes.
	BIDDER_EXISTS_THE_AUCTION_TIME_HAS_BEEN_EXTENDED_BY_5_MINUTES(2159),
	// Message: Bidder exists, auction time has been extended by 3 minutes.
	BIDDER_EXISTS_AUCTION_TIME_HAS_BEEN_EXTENDED_BY_3_MINUTES(2160),
	// Message: There is not enough space to move, the skill cannot be used.
	THERE_IS_NOT_ENOUGH_SPACE_TO_MOVE_THE_SKILL_CANNOT_BE_USED(2161),
	// Message: Your soul count has increased by $s1. It is now at $s2.
	YOUR_SOUL_COUNT_HAS_INCREASED_BY_S1_ITS_NOW_S2(2162),
	// Message: Soul cannot be increased anymore.
	SOUL_CANNOT_BE_INCREASED_ANYMORE(2163),
	// Message: The barracks have been seized.
	THE_BARRACKS_HAVE_BEEN_SEIZED(2164),
	// Message: The barracks function has been restored.
	THE_BARRACKS_FUNCTION_HAS_BEEN_RESTORED(2165),
	// Message: All barracks are occupied.
	ALL_BARRACKS_ARE_OCCUPIED(2166),
	// Message: A malicious skill cannot be used in a peace zone.
	A_MALICIOUS_SKILL_CANNOT_BE_USED_IN_A_PEACE_ZONE(2167),
	// Message: $c1 has acquired the flag.
	C1_HAS_ACQUIRED_THE_FLAG(2168),
	// Message: Your clan has been registered to $s1's fortress battle.
	YOUR_CLAN_HAS_BEEN_REGISTERED_TO_S1S_FORTRESS_BATTLE(2169),
	// Message: $s1's auction has ended.
	S1S_AUCTION_HAS_ENDED(2173),
	// Message: $c1 cannot duel because $c1 is currently polymorphed.
	C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_POLYMORPHED(2174),
	// Message: Party duel cannot be initiated due to a polymorphed party member.
	PARTY_DUEL_CANNOT_BE_INITIATED_DUE_TO_A_POLYMORPHED_PARTY_MEMBER(2175),
	// Message: You cannot polymorph while riding a boat.
	YOU_CANNOT_POLYMORPH_WHILE_RIDING_A_BOAT(2182),
	// Message: The fortress battle of $s1 has finished.
	THE_FORTRESS_BATTLE_OF_S1_HAS_FINISHED(2183),
	// Message: $s1 is victorious in the fortress battle of $s2.
	S1_IS_VICTORIOUS_IN_THE_FORTRESS_BATTLE_OF_S2(2184),
	// Message: Only a party leader can make the request to enter.
	ONLY_A_PARTY_LEADER_CAN_MAKE_THE_REQUEST_TO_ENTER(2185),
	// Message: Soul cannot be absorbed anymore.
	SOUL_CANNOT_BE_ABSORBED_ANYMORE(2186),
	// Message: Current Location : $s1, $s2, $s3 (Near Kamael Village)
	CURRENT_LOCATION__S1_S2_S3_NEAR_KAMAEL_VILLAGE(2189),
	// Message: Current Location : $s1, $s2, $s3 (Near the south end of the Wastelands)
	CURRENT_LOCATION__S1_S2_S3_NEAR_THE_SOUTH_END_OF_THE_WASTELANDS(2190),
	// Message: You are too far from the NPC for that to work.
	YOU_ARE_TOO_FAR_FROM_THE_NPC_FOR_THAT_TO_WORK(2193),
	// Message: You do not have enough souls.
	YOU_DO_NOT_HAVE_ENOUGH_SOULS(2195),
	// Message: You have entered an area where the mini map can now be used.
	YOU_HAVE_ENTERED_AN_AREA_WHERE_THE_MINI_MAP_CAN_NOW_BE_USED(2206),
	// Message: This is an area where you cannot use the mini map. The mini map cannot be opened.
	THIS_IS_AN_AREA_WHERE_YOU_CANNOT_USE_THE_MINI_MAP_THE_MINI_MAP_CANNOT_BE_OPENED(2207),
	// Message: You cannot board a ship while you are polymorphed.
	YOU_CANNOT_BOARD_A_SHIP_WHILE_YOU_ARE_POLYMORPHED(2213),
	// Message: The ballista has been successfully destroyed. The clan's reputation will be increased.
	THE_BALLISTA_HAS_BEEN_SUCCESSFULLY_DESTROYED(2217),
	// Message: This squad skill has already been acquired.
	THIS_SQUAD_SKILL_HAS_ALREADY_BEEN_ACQUIRED(2219),
	// Message: The previous level skill has not been learned.
	THE_PREVIOUS_LEVEL_SKILL_HAS_NOT_BEEN_LEARNED(2220),
	// Message: Not enough bolts.
	NOT_ENOUGH_BOLTS(2226),
	// Message: It is not possible to register for the castle siege side or castle siege of a higher castle in the contract.
	IT_IS_NOT_POSSIBLE_TO_REGISTER_FOR_THE_CASTLE_SIEGE_SIDE_OR_CASTLE_SIEGE_OF_A_HIGHER_CASTLE_IN_THE_CONTRACT(2227),
	// Message: Instance zone time limit:
	INSTANCE_ZONE_TIME_LIMIT(2228),
	// Message: There is no instance zone under a time limit.
	THERE_IS_NO_INSTANCE_ZONE_UNDER_A_TIME_LIMIT(2229),
	// Message: $s1 will be available for re-use after $s2 hour(s) $s3 minute(s).
	S1_WILL_BE_AVAILABLE_FOR_REUSE_AFTER_S2_HOURS_S3_MINUTES(2230),
	// Message: Siege registration is not possible due to your castle contract.
	SIEGE_REGISTRATION_IS_NOT_POSSIBLE_DUE_TO_YOUR_CASTLE_CONTRACT(2233),
	// Message: You are participating in the siege of $s1. This siege is scheduled for 2 hours.
	YOU_ARE_PARTICIPATING_IN_THE_SIEGE_OF_S1_THIS_SIEGE_IS_SCHEDULED_FOR_2_HOURS(2238),
	// Message: $s1 minute(s) remaining.
	S1_MINUTES_REMAINING(2244),
	// Message: $s1 second(s) remaining.
	S1_SECONDS_REMAINING(2245),
	// Message: The contest will begin in $s1 minute(s).
	THE_CONTEST_WILL_BEGIN_IN_S1_MINUTES(2246),
	// Message: You cannot board an airship while transformed.
	YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_TRANSFORMED(2247),
	// Message: You cannot board an airship while petrified.
	YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_PETRIFIED(2248),
	// Message: You cannot board an airship while dead.
	YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_DEAD(2249),
	// Message: You cannot board an airship while fishing.
	YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_FISHING(2250),
	// Message: You cannot board an airship while in battle.
	YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_IN_BATTLE(2251),
	// Message: You cannot board an airship while in a duel.
	YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_IN_A_DUEL(2252),
	// Message: You cannot board an airship while sitting.
	YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_SITTING(2253),
	// Message: You cannot board an airship while casting.
	YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_CASTING(2254),
	// Message: You cannot board an airship when a cursed weapon is equipped.
	YOU_CANNOT_BOARD_AN_AIRSHIP_WHEN_A_CURSED_WEAPON_IS_EQUIPPED(2255),
	// Message: You cannot board an airship while holding a flag.
	YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_HOLDING_A_FLAG(2256),
	// Message: You cannot board an airship while a pet or a servitor is summoned.
	YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_A_PET_OR_A_SERVITOR_IS_SUMMONED(2257),
	// Message: You have already boarded another airship.
	YOU_HAVE_ALREADY_BOARDED_ANOTHER_AIRSHIP(2258),
	// Message: Current Location: $s1, $s2, $s3 (near Fantasy Isle)
	CURRENT_LOCATION_S1_S2_S3_NEAR_FANTASY_ISLE(2259),
	// Message: $c1 has done $s3 points of damage to $c2.
	C1_HAS_DONE_S3_POINTS_OF_DAMAGE_TO_C2(2261),
	// Message: $c1 has received $s3 damage from $c2.
	C1_HAS_RECEIVED_S3_DAMAGE_FROM_C2(2262),
	// Message: $c1 has received damage of $s3 through $c2.
	C1_HAS_RECEIVED_DAMAGE_OF_S3_THROUGH_C2(2263),
	// Message: $c1 has evaded $c2's attack.
	C1_HAS_EVADED_C2S_ATTACK(2264),
	// Message: $c1's attack went astray.
	C1S_ATTACK_WENT_ASTRAY(2265),
	// Message: $c1 landed a critical hit!
	C1_LANDED_A_CRITICAL_HIT(2266),
	// Message: $c1 resisted $c2's drain.
	C1_RESISTED_C2S_DRAIN(2267),
	// Message: $c1's attack failed.
	C1S_ATTACK_FAILED(2268),
	// Message: $c1 resisted $c2's magic.
	C1_RESISTED_C2S_MAGIC(2269),
	// Message: $c1 weakly resisted $c2's magic.
	C1_WEAKLY_RESISTED_C2S_MAGIC(2271),
	// Message: This skill cannot be learned while in the sub-class state. Please try again after changing to the main class.
	THIS_SKILL_CANNOT_BE_LEARNED_WHILE_IN_THE_SUBCLASS_STATE(2273),
	// Message: Damage is decreased because $c1 resisted $c2's magic.
	DAMAGE_IS_DECREASED_BECAUSE_C1_RESISTED_C2S_MAGIC(2280),
	// Message: You cannot transform while sitting.
	YOU_CANNOT_TRANSFORM_WHILE_SITTING(2283),
	// Message: You cannot wear $s1 because you are not wearing a bracelet.
	YOU_CANNOT_WEAR_S1_BECAUSE_YOU_ARE_NOT_WEARING_A_BRACELET(2286),
	// Message: You cannot equip $s1 because you do not have any available slots.
	YOU_CANNOT_EQUIP_S1_BECAUSE_YOU_DO_NOT_HAVE_ANY_AVAILABLE_SLOTS(2287),
	// Message: Agathion skills can be used only when your Agathion is summoned.
	AGATHION_SKILLS_CAN_BE_USED_ONLY_WHEN_YOUR_AGATHION_IS_SUMMONED(2292),
	// Message: Current location: $s1, $s2, $s3 (inside the Steel Citadel)
	CURRENT_LOCATION_S1_S2_S3_INSIDE_THE_STEEL_CITADEL(2293),
	// Message: There are $s2 second(s) remaining in $s1's re-use time.
	THERE_ARE_S2_SECONDS_REMAINING_IN_S1S_REUSE_TIME(2303),
	// Message: There are $s2 minute(s), $s3 second(s) remaining in $s1's re-use time.
	THERE_ARE_S2_MINUTES_S3_SECONDS_REMAINING_IN_S1S_REUSE_TIME(2304),
	// Message: There are $s2 hour(s), $s3 minute(s), and $s4 second(s) remaining in $s1's re-use time.
	THERE_ARE_S2_HOURS_S3_MINUTES_AND_S4_SECONDS_REMAINING_IN_S1S_REUSE_TIME(2305),
	// Message: Your Charm of Courage is trying to resurrect you. Would you like to resurrect now?
	YOUR_CHARM_OF_COURAGE_IS_TRYING_TO_RESURRECT_YOU(2306),
	// Message: Your Vitality is at maximum.
	YOUR_VITALITY_IS_AT_MAXIMUM(2314),
	// Message: Your Vitality has increased.
	YOUR_VITALITY_HAS_INCREASED(2315),
	// Message: Your Vitality has decreased.
	YOUR_VITALITY_HAS_DECREASED(2316),
	// Message: Your Vitality is fully exhausted.
	YOUR_VITALITY_IS_FULLY_EXHAUSTED(2317),
	// Message: You have acquired $s1 reputation.
	YOU_HAVE_ACQUIRED_S1_REPUTATION(2319),
	// Message: Current location: Inside Kamaloka
	CURRENT_LOCATION_INSIDE_KAMALOKA(2321),
	// Message: Current location: Inside Near Kamaloka
	CURRENT_LOCATION_INSIDE_NIA_KAMALOKA(2322),
	// Message: Current location: Inside Rim Kamaloka
	CURRENT_LOCATION_INSIDE_RIM_KAMALOKA(2323),
	// Message: You have acquired 50 Clan Fame Points.
	YOU_HAVE_ACQUIRED_50_CLAN_FAME_POINTS(2326),
	// Message: You don't have enough reputation to do that.
	YOU_DONT_HAVE_ENOUGH_REPUTATION_TO_DO_THAT(2327),
	// Message: Only clans who are level 4 or above can register for battle at Devastated Castle and Fortress of the Dead.
	ONLY_CLANS_WHO_ARE_LEVEL_4_OR_ABOVE_CAN_REGISTER_FOR_BATTLE_AT_DEVASTATED_CASTLE_AND_FORTRESS_OF_THE_DEAD(2328),
	// Message: You cannot receive the Dimensional Item because you have exceed your inventory weight/quantity limit.
	YOU_CANNOT_RECEIVE_THE_DIMENSIONAL_ITEM_BECAUSE_YOU_HAVE_EXCEED_YOUR_INVENTORY_WEIGHTQUANTITY_LIMIT(2333),
	// Message: There are no more Dimensional Items to be found.
	THERE_ARE_NO_MORE_DIMENSIONAL_ITEMS_TO_BE_FOUND(2335),
	// Message: Half-Kill!
	CP_SIPHON(2336),
	// Message: Your CP was drained because you were hit with a Half-Kill skill.
	YOUR_CP_WAS_DRAINED_BECAUSE_YOU_WERE_HIT_WITH_A_CP_SIPHON_SKILL(2337),
	// Message: $s1 seconds to game end!
	S1_SECONDS_TO_GAME_END(2347),
	// Message: You cannot use My Teleports during a battle.
	YOU_CANNOT_USE_MY_TELEPORTS_DURING_A_BATTLE(2348),
	// Message: You cannot use My Teleports while participating a large-scale battle such as a castle siege, fortress siege, or hideout siege.
	YOU_CANNOT_USE_MY_TELEPORTS_WHILE_PARTICIPATING_A_LARGESCALE_BATTLE_SUCH_AS_A_CASTLE_SIEGE_FORTRESS_SIEGE_OR_HIDEOUT_SIEGE(2349),
	// Message: You cannot use My Teleports during a duel.
	YOU_CANNOT_USE_MY_TELEPORTS_DURING_A_DUEL(2350),
	// Message: You cannot use My Teleports while flying.
	YOU_CANNOT_USE_MY_TELEPORTS_WHILE_FLYING(2351),
	// Message: You cannot use My Teleports while participating in an Olympiad match.
	YOU_CANNOT_USE_MY_TELEPORTS_WHILE_PARTICIPATING_IN_AN_OLYMPIAD_MATCH(2352),
	// Message: You cannot use My Teleports while you are in a petrified or paralyzed state.
	YOU_CANNOT_USE_MY_TELEPORTS_WHILE_YOU_ARE_IN_A_PETRIFIED_OR_PARALYZED_STATE(2353),
	// Message: You cannot use My Teleports while you are dead.
	YOU_CANNOT_USE_MY_TELEPORTS_WHILE_YOU_ARE_DEAD(2354),
	// Message: You cannot use My Teleports in this area.
	YOU_CANNOT_USE_MY_TELEPORTS_IN_THIS_AREA(2355),
	// Message: You cannot use My Teleports underwater.
	YOU_CANNOT_USE_MY_TELEPORTS_UNDERWATER(2356),
	// Message: You cannot use My Teleports in an instant zone.
	YOU_CANNOT_USE_MY_TELEPORTS_IN_AN_INSTANT_ZONE(2357),
	// Message: You have no space to save the teleport location.
	YOU_HAVE_NO_SPACE_TO_SAVE_THE_TELEPORT_LOCATION(2358),
	// Message: You cannot teleport because you do not have a teleport item.
	YOU_CANNOT_TELEPORT_BECAUSE_YOU_DO_NOT_HAVE_A_TELEPORT_ITEM(2359),
	// Message: The limited-time item has disappeared because the remaining time ran out.
	THE_LIMITEDTIME_ITEM_HAS_DISAPPEARED_BECAUSE_THE_REMAINING_TIME_RAN_OUT(2366),
	// Message: Resurrection will take place in the waiting room after $s1 seconds.
	RESURRECTION_WILL_TAKE_PLACE_IN_THE_WAITING_ROOM_AFTER_S1_SECONDS(2370),
	// Message: End match!
	END_MATCH(2374),
	// Message: You cannot receive a Dimensional Item during an exchange.
	YOU_CANNOT_RECEIVE_A_DIMENSIONAL_ITEM_DURING_AN_EXCHANGE(2376),
	// Message: A party cannot be formed in this area.
	A_PARTY_CANNOT_BE_FORMED_IN_THIS_AREA(2388),
	// Message: Your number of My Teleports slots has reached its maximum limit.
	YOUR_NUMBER_OF_MY_TELEPORTS_SLOTS_HAS_REACHED_ITS_MAXIMUM_LIMIT(2390),
	// Message: You have used the Feather of Blessing to resurrect.
	YOU_HAVE_USED_THE_FEATHER_OF_BLESSING_TO_RESURRECT(2391),
	// Message: That pet/servitor skill cannot be used because it is recharging.
	THAT_PET_SERVITOR_SKILL_CANNOT_BE_USED_BECAUSE_IT_IS_RECHARGING(2396),
	// Message: Instant Zone currently in use: $s1
	INSTANT_ZONE_CURRENTLY_IN_USE_S1(2400),
	// Message: Clan lord $c2, who leads clan $s1, has been declared the lord of the $s3 territory.
	CLAN_LORD_C2_WHO_LEADS_CLAN_S1_HAS_BEEN_DECLARED_THE_LORD_OF_THE_S3_TERRITORY(2401),
	// Message: The Territory War request period has ended.
	THE_TERRITORY_WAR_REQUEST_PERIOD_HAS_ENDED(2402),
	// Message: The Territory War begins in 10 minutes!
	THE_TERRITORY_WAR_BEGINS_IN_10_MINUTES(2403),
	// Message: The Territory War begins in 5 minutes!
	THE_TERRITORY_WAR_BEGINS_IN_5_MINUTES(2404),
	// Message: The Territory War begins in 1 minute!
	THE_TERRITORY_WAR_BEGINS_IN_1_MINUTE(2405),
	// Message: You are currently registered for a 3 vs. 3 class irrelevant team match.
	YOU_ARE_CURRENTLY_REGISTERED_FOR_A_3_VS_3_CLASS_IRRELEVANT_TEAM_MATCH(2408),
	// Message: The number of My Teleports slots has been increased.
	THE_NUMBER_OF_MY_TELEPORTS_SLOTS_HAS_BEEN_INCREASED(2409),
	// Message: You cannot use My Teleports to reach this area!
	YOU_CANNOT_USE_MY_TELEPORTS_TO_REACH_THIS_AREA(2410),
	// Message: The collection has failed.
	THE_COLLECTION_HAS_FAILED(2424),
	// Message: The Red Team is victorious.
	THE_RED_TEAM_IS_VICTORIOUS(2427),
	// Message: The Blue Team is victorious.
	THE_BLUE_TEAM_IS_VICTORIOUS(2428),
	// Message: $c1 is already registered on the waiting list for the 3 vs. 3 class irrelevant team match.
	C1_IS_ALREADY_REGISTERED_ON_THE_WAITING_LIST_FOR_THE_3_VS_3_CLASS_IRRELEVANT_TEAM_MATCH(2440),
	// Message: Only a party leader can request a team match.
	ONLY_A_PARTY_LEADER_CAN_REQUEST_A_TEAM_MATCH(2441),
	// Message: The request cannot be made because the requirements have not been met. To participate in a team match, you must first form a 3-member party.
	THE_REQUEST_CANNOT_BE_MADE_BECAUSE_THE_REQUIREMENTS_HAVE_NOT_BEEN_MET(2442),
	// Message: The battlefield channel has been activated.
	THE_BATTLEFIELD_CHANNEL_HAS_BEEN_ACTIVATED(2445),
	// Message: The battlefield channel has been deactivated.
	THE_BATTLEFIELD_CHANNEL_HAS_BEEN_DEACTIVATED(2446),
	// Message: Five years have passed since this character's creation.
	FIVE_YEARS_HAVE_PASSED_SINCE_THIS_CHARACTERS_CREATION(2447),
	// Message: Happy birthday! Alegria has sent you a birthday gift.
	YOUR_BIRTHDAY_GIFT_HAS_ARRIVED(2448),
	// Message: There are $s1 days remaining until your birthday. On your birthday, you will receive a gift that Alegria has carefully prepared.
	THERE_ARE_S1_DAYS_UNTIL_YOUR_CHARACTERS_BIRTHDAY(2449),
	// Message: $c1's birthday is $s3/$s4/$s2.
	C1S_BIRTHDAY_IS_S3S4S2(2450),
	// Message: Your cloak has been unequipped because your armor set is no longer complete.
	YOUR_CLOAK_HAS_BEEN_UNEQUIPPED_BECAUSE_YOUR_ARMOR_SET_IS_NO_LONGER_COMPLETE(2451),
	// Message: The cloak cannot be equipped because your armor set is not complete.
	THE_CLOAK_CANNOT_BE_EQUIPPED_BECAUSE_YOUR_ARMOR_SET_IS_NOT_COMPLETE(2453),
	// Message: In order to acquire an airship, the clan's level must be level 5 or higher.
	IN_ORDER_TO_ACQUIRE_AN_AIRSHIP_THE_CLANS_LEVEL_MUST_BE_LEVEL_5_OR_HIGHER(2456),
	// Message: An airship cannot be summoned because either you have not registered your airship license, or the airship has not yet been summoned.
	AN_AIRSHIP_CANNOT_BE_SUMMONED_BECAUSE_EITHER_YOU_HAVE_NOT_REGISTERED_YOUR_AIRSHIP_LICENSE_OR_THE_AIRSHIP_HAS_NOT_YET_BEEN_SUMMONED(2457),
	// Message: Your clan's airship is already being used by another clan member.
	YOUR_CLANS_AIRSHIP_IS_ALREADY_BEING_USED_BY_ANOTHER_CLAN_MEMBER(2458),
	// Message: The Airship Summon License has already been acquired.
	THE_AIRSHIP_SUMMON_LICENSE_HAS_ALREADY_BEEN_ACQUIRED(2459),
	// Message: The clan owned airship already exists.
	THE_CLAN_OWNED_AIRSHIP_ALREADY_EXISTS(2460),
	// Message: An airship cannot be summoned because you don't have enough $s1.
	AN_AIRSHIP_CANNOT_BE_SUMMONED_BECAUSE_YOU_DONT_HAVE_ENOUGH_S1(2462),
	// Message: The airship's fuel (EP) will soon run out.
	THE_AIRSHIPS_FUEL_EP_WILL_SOON_RUN_OUT(2463),
	// Message: The airship's fuel (EP) has run out. The airship's speed will be greatly decreased in this condition.
	THE_AIRSHIPS_FUEL_EP_HAS_RUN_OUT(2464),
	// Message: Your ship cannot teleport because it does not have enough fuel for the trip.
	YOUR_SHIP_CANNOT_TELEPORT_BECAUSE_IT_DOES_NOT_HAVE_ENOUGH_FUEL_FOR_THE_TRIP(2491),
	// Message: The match is being prepared. Please try again later.
	THE_MATCH_IS_BEING_PREPARED_PLEASE_TRY_AGAIN_LATER(2701),
	// Message: Team members were modified because the teams were unbalanced.
	TEAM_MEMBERS_WERE_MODIFIED_BECAUSE_THE_TEAMS_WERE_UNBALANCED(2703),
	// Message: You cannot register because capacity has been exceeded.
	YOU_CANNOT_REGISTER_BECAUSE_CAPACITY_HAS_BEEN_EXCEEDED(2704),
	// Message: You cannot enter because you do not meet the requirements.
	YOU_CANNOT_ENTER_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS(2706),
	// Message: You must wait 10 seconds before attempting to register again.
	YOU_MUST_WAIT_10_SECONDS_BEFORE_ATTEMPTING_TO_REGISTER_AGAIN(2707),
	// Message: You cannot register while in possession of a cursed weapon.
	YOU_CANNOT_REGISTER_WHILE_IN_POSSESSION_OF_A_CURSED_WEAPON(2708),
	// Message: Applicants for the Olympiad, Underground Coliseum, or Kratei's Cube matches cannot register.
	APPLICANTS_FOR_THE_OLYMPIAD_UNDERGROUND_COLISEUM_OR_KRATEIS_CUBE_MATCHES_CANNOT_REGISTER(2709),
	// Message: Current location: $s1, $s2, $s3 (near the Keucereus Alliance Base)
	CURRENT_LOCATION_S1_S2_S3_NEAR_THE_KEUCEREUS_ALLIANCE_BASE(2710),
	// Message: Current location: $s1, $s2, $s3 (inside the Seed of Infinity)
	CURRENT_LOCATION_S1_S2_S3_INSIDE_THE_SEED_OF_INFINITY(2711),
	// Message: Current location: $s1, $s2, $s3 (outside the Seed of Infinity)
	CURRENT_LOCATION_S1_S2_S3_OUTSIDE_THE_SEED_OF_INFINITY(2712),
	// Message: Current location: $s1, $s2, $s3 (inside Aerial Cleft)
	CURRENT_LOCATION_S1_S2_S3_INSIDE_AERIAL_CLEFT(2716),
	// Message: Instant zone: $s1's entry has been restricted. You can check the next possible entry time by using the command "/instancezone."
	INSTANT_ZONE_S1_ENTRY_HAS_BEEN_RESTRICTED(2720),
	// Message: You cannot board because you do not meet the requirements.
	YOU_CANNOT_BOARD_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS(2727),
	// Message: You cannot control the helm while transformed.
	YOU_CANNOT_CONTROL_THE_HELM_WHILE_TRANSFORMED(2729),
	// Message: You cannot control the helm while you are petrified.
	YOU_CANNOT_CONTROL_THE_HELM_WHILE_YOU_ARE_PETRIFIED(2730),
	// Message: You cannot control the helm when you are dead.
	YOU_CANNOT_CONTROL_THE_HELM_WHEN_YOU_ARE_DEAD(2731),
	// Message: You cannot control the helm while fishing.
	YOU_CANNOT_CONTROL_THE_HELM_WHILE_FISHING(2732),
	// Message: You cannot control the helm while in a battle.
	YOU_CANNOT_CONTROL_THE_HELM_WHILE_IN_A_BATTLE(2733),
	// Message: You cannot control the helm while in a duel.
	YOU_CANNOT_CONTROL_THE_HELM_WHILE_IN_A_DUEL(2734),
	// Message: You cannot control the helm while in a sitting position.
	YOU_CANNOT_CONTROL_THE_HELM_WHILE_IN_A_SITTING_POSITION(2735),
	// Message: You cannot control the helm while using a skill.
	YOU_CANNOT_CONTROL_THE_HELM_WHILE_USING_A_SKILL(2736),
	// Message: You cannot control the helm while a cursed weapon is equipped.
	YOU_CANNOT_CONTROL_THE_HELM_WHILE_A_CURSED_WEAPON_IS_EQUIPPED(2737),
	// Message: You cannot control the helm while holding a flag.
	YOU_CANNOT_CONTROL_THE_HELM_WHILE_HOLDING_A_FLAG(2738),
	// Message: You cannot control the helm because you do not meet the requirements.
	YOU_CANNOT_CONTROL_THE_HELM_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS(2739),
	// Message: This action is prohibited while steering.
	THIS_ACTION_IS_PROHIBITED_WHILE_STEERING(2740),
	// Message: The $s1 ward has been destroyed! $c2 now has the territory ward.
	THE_S1_WARD_HAS_BEEN_DESTROYED_C2_NOW_HAS_THE_TERRITORY_WARD(2750),
	// Message: The character that acquired $s1's ward has been killed.
	THE_CHARACTER_THAT_ACQUIRED_S1S_WARD_HAS_BEEN_KILLED(2751),
	// Message: This type of attack is prohibited when allied troops are the target.
	THIS_TYPE_OF_ATTACK_IS_PROHIBITED_WHEN_ALLIED_TROOPS_ARE_THE_TARGET(2753),
	// Message: You cannot be simultaneously registered for PVP matches such as the Olympiad, Underground Coliseum, Aerial Cleft, Kratei's Cube, and Handy's Block Checkers.
	YOU_CANNOT_BE_SIMULTANEOUSLY_REGISTERED_FOR_PVP_MATCHES_SUCH_AS_THE_OLYMPIAD_UNDERGROUND_COLISEUM_AERIAL_CLEFT_KRATEIS_CUBE_AND_HANDYS_BLOCK_CHECKERS(2754),
	// Message: Another player is probably controlling the target.
	ANOTHER_PLAYER_IS_PROBABLY_CONTROLLING_THE_TARGET(2756),
	// Message: You must target the one you wish to control.
	YOU_MUST_TARGET_THE_ONE_YOU_WISH_TO_CONTROL(2761),
	// Message: You cannot control because you are too far.
	YOU_CANNOT_CONTROL_BECAUSE_YOU_ARE_TOO_FAR(2762),
	// Message: Only the alliance channel leader can attempt entry.
	ONLY_THE_ALLIANCE_CHANNEL_LEADER_CAN_ATTEMPT_ENTRY(2765),
	// Message: The effect of territory ward is disappearing.
	THE_EFFECT_OF_TERRITORY_WARD_IS_DISAPPEARING(2776),
	// Message: The airship summon license has been entered. Your clan can now summon the airship.
	THE_AIRSHIP_SUMMON_LICENSE_HAS_BEEN_ENTERED(2777),
	// Message: You cannot teleport while in possession of a ward.
	YOU_CANNOT_TELEPORT_WHILE_IN_POSSESSION_OF_A_WARD(2778),
	// Message: Mercenary participation is requested in $s1 territory.
	MERCENARY_PARTICIPATION_IS_REQUESTED_IN_S1_TERRITORY(2788),
	// Message: Mercenary participation request is cancelled in $s1 territory.
	MERCENARY_PARTICIPATION_REQUEST_IS_CANCELLED_IN_S1_TERRITORY(2789),
	// Message: Clan participation is requested in $s1 territory.
	CLAN_PARTICIPATION_IS_REQUESTED_IN_S1_TERRITORY(2790),
	// Message: Clan participation request is cancelled in $s1 territory.
	CLAN_PARTICIPATION_REQUEST_IS_CANCELLED_IN_S1_TERRITORY(2791),
	// Message: You must have a minimum of ($s1) people to enter this Instant Zone. Your request for entry is denied.
	YOU_MUST_HAVE_A_MINIMUM_OF_S1_PEOPLE_TO_ENTER_THIS_INSTANT_ZONE(2793),
	// Message: The territory war channel and functions will now be deactivated.
	THE_TERRITORY_WAR_CHANNEL_AND_FUNCTIONS_WILL_NOW_BE_DEACTIVATED(2794),
	// Message: You've already requested a territory war in another territory elsewhere.
	YOUVE_ALREADY_REQUESTED_A_TERRITORY_WAR_IN_ANOTHER_TERRITORY_ELSEWHERE(2795),
	// Message: The clan who owns the territory cannot participate in the territory war as mercenaries.
	THE_CLAN_WHO_OWNS_THE_TERRITORY_CANNOT_PARTICIPATE_IN_THE_TERRITORY_WAR_AS_MERCENARIES(2796),
	// Message: It is not a territory war registration period, so a request cannot be made at this time.
	IT_IS_NOT_A_TERRITORY_WAR_REGISTRATION_PERIOD_SO_A_REQUEST_CANNOT_BE_MADE_AT_THIS_TIME(2797),
	// Message: The territory war will end in $s1-hour(s).
	THE_TERRITORY_WAR_WILL_END_IN_S1HOURS(2798),
	// Message: The territory war will end in $s1-minute(s).
	THE_TERRITORY_WAR_WILL_END_IN_S1MINUTES(2799),
	// Message: $s1-second(s) to the end of territory war!
	S1_SECONDS_TO_THE_END_OF_TERRITORY_WAR(2900),
	// Message: You cannot force attack a member of the same territory.
	YOU_CANNOT_FORCE_ATTACK_A_MEMBER_OF_THE_SAME_TERRITORY(2901),
	// Message: You've acquired the ward. Move quickly to your forces' outpost.
	YOUVE_ACQUIRED_THE_WARD(2902),
	// Message: Territory war has begun.
	TERRITORY_WAR_HAS_BEGUN(2903),
	// Message: Territory war has ended.
	TERRITORY_WAR_HAS_ENDED(2904),
	// Message: You've requested $c1 to be on your Friends List.
	YOUVE_REQUESTED_C1_TO_BE_ON_YOUR_FRIENDS_LIST(2911),
	// Message: Clan $s1 has succeeded in capturing $s2's territory ward.
	CLAN_S1_HAS_SUCCEEDED_IN_CAPTURING_S2S_TERRITORY_WARD(2913),
	// Message: The territory war will begin in 20 minutes! Territory related functions (i.e.: battlefield channel, Disguise Scrolls, Transformations, etc...) can now be used.
	THE_TERRITORY_WAR_WILL_BEGIN_IN_20_MINUTES(2914),
	// Message: This clan member cannot withdraw or be expelled while participating in a territory war.
	THIS_CLAN_MEMBER_CANNOT_WITHDRAW_OR_BE_EXPELLED_WHILE_PARTICIPATING_IN_A_TERRITORY_WAR(2915),
	// Message: Only characters who are level 40 or above who have completed their second class transfer can register in a territory war.
	ONLY_CHARACTERS_WHO_ARE_LEVEL_40_OR_ABOVE_WHO_HAVE_COMPLETED_THEIR_SECOND_CLASS_TRANSFER_CAN_REGISTER_IN_A_TERRITORY_WAR(2918),
	// Message: Block Checker will end in 5 seconds!
	BLOCK_CHECKER_WILL_END_IN_5_SECONDS(2922),
	// Message: Block Checker will end in 4 seconds!!
	BLOCK_CHECKER_WILL_END_IN_4_SECONDS(2923),
	// Message: Block Checker will end in 3 seconds!!!
	BLOCK_CHECKER_WILL_END_IN_3_SECONDS(2925),
	// Message: Block Checker will end in 2 seconds!!!!
	BLOCK_CHECKER_WILL_END_IN_2_SECONDS(2926),
	// Message: Block Checker will end in 1 second!!!!!
	BLOCK_CHECKER_WILL_END_IN_1_SECOND(2927),
	// Message: The $c1 team has won.
	THE_C1_TEAM_HAS_WON(2928),
	// Message: The disguise scroll cannot be used because it is meant for use in a different territory.
	THE_DISGUISE_SCROLL_CANNOT_BE_USED_BECAUSE_IT_IS_MEANT_FOR_USE_IN_A_DIFFERENT_TERRITORY(2936),
	// Message: A territory owning clan member cannot use a disguise scroll.
	A_TERRITORY_OWNING_CLAN_MEMBER_CANNOT_USE_A_DISGUISE_SCROLL(2937),
	// Message: The disguise scroll cannot be used while you are engaged in a private store or manufacture workshop.
	THE_DISGUISE_SCROLL_CANNOT_BE_USED_WHILE_YOU_ARE_ENGAGED_IN_A_PRIVATE_STORE_OR_MANUFACTURE_WORKSHOP(2938),
	// Message: A disguise cannot be used when you are in a chaotic state.
	A_DISGUISE_CANNOT_BE_USED_WHEN_YOU_ARE_IN_A_CHAOTIC_STATE(2939),
	// Message: The territory war exclusive disguise and transformation can be used 20 minutes before the start of the territory war to 10 minutes after its end.
	THE_TERRITORY_WAR_EXCLUSIVE_DISGUISE_AND_TRANSFORMATION_CAN_BE_USED_20_MINUTES_BEFORE_THE_START_OF_THE_TERRITORY_WAR_TO_10_MINUTES_AFTER_ITS_END(2955),
	// Message: A character born on February 29 will receive a gift on February 28.
	A_CHARACTER_BORN_ON_FEBRUARY_29_WILL_RECEIVE_A_GIFT_ON_FEBRUARY_28(2957),
	// Message: An Agathion has already been summoned.
	AN_AGATHION_HAS_ALREADY_BEEN_SUMMONED(2958),
	// Message: The item $s1 is required.
	THE_ITEM_S1_IS_REQUIRED(2960),
	// Message: $s2 unit(s) of the item $s1 is/are required.
	S2_UNITS_OF_THE_ITEM_S1_ISARE_REQUIRED(2961),
	// Message: The previous mail was forwarded less than 1 minute ago and this cannot be forwarded.
	THE_PREVIOUS_MAIL_WAS_FORWARDED_LESS_THAN_1_MINUTE_AGO_AND_THIS_CANNOT_BE_FORWARDED(2969),
	// Message: You cannot forward in a non-peace zone location.
	YOU_CANNOT_FORWARD_IN_A_NONPEACE_ZONE_LOCATION(2970),
	// Message: You cannot forward during an exchange.
	YOU_CANNOT_FORWARD_DURING_AN_EXCHANGE(2971),
	// Message: You cannot forward because the private shop or workshop is in progress.
	YOU_CANNOT_FORWARD_BECAUSE_THE_PRIVATE_SHOP_OR_WORKSHOP_IS_IN_PROGRESS(2972),
	// Message: You cannot forward during an item enhancement or attribute enhancement.
	YOU_CANNOT_FORWARD_DURING_AN_ITEM_ENHANCEMENT_OR_ATTRIBUTE_ENHANCEMENT(2973),
	// Message: The item that you're trying to send cannot be forwarded because it isn't proper.
	THE_ITEM_THAT_YOURE_TRYING_TO_SEND_CANNOT_BE_FORWARDED_BECAUSE_IT_ISNT_PROPER(2974),
	// Message: You cannot forward because you don't have enough adena.
	YOU_CANNOT_FORWARD_BECAUSE_YOU_DONT_HAVE_ENOUGH_ADENA(2975),
	// Message: You cannot receive in a non-peace zone location.
	YOU_CANNOT_RECEIVE_IN_A_NONPEACE_ZONE_LOCATION(2976),
	// Message: You cannot receive during an exchange.
	YOU_CANNOT_RECEIVE_DURING_AN_EXCHANGE(2977),
	// Message: You cannot receive because the private shop or workshop is in progress.
	YOU_CANNOT_RECEIVE_BECAUSE_THE_PRIVATE_SHOP_OR_WORKSHOP_IS_IN_PROGRESS(2978),
	// Message: You cannot receive during an item enhancement or attribute enhancement.
	YOU_CANNOT_RECEIVE_DURING_AN_ITEM_ENHANCEMENT_OR_ATTRIBUTE_ENHANCEMENT(2979),
	// Message: You cannot receive because you don't have enough adena.
	YOU_CANNOT_RECEIVE_BECAUSE_YOU_DONT_HAVE_ENOUGH_ADENA(2980),
	// Message: You could not receive because your inventory is full.
	YOU_COULD_NOT_RECEIVE_BECAUSE_YOUR_INVENTORY_IS_FULL(2981),
	// Message: You cannot cancel in a non-peace zone location.
	YOU_CANNOT_CANCEL_IN_A_NONPEACE_ZONE_LOCATION(2982),
	// Message: You cannot cancel during an exchange.
	YOU_CANNOT_CANCEL_DURING_AN_EXCHANGE(2983),
	// Message: You cannot cancel because the private shop or workshop is in progress.
	YOU_CANNOT_CANCEL_BECAUSE_THE_PRIVATE_SHOP_OR_WORKSHOP_IS_IN_PROGRESS(2984),
	// Message: You cannot cancel during an item enhancement or attribute enhancement.
	YOU_CANNOT_CANCEL_DURING_AN_ITEM_ENHANCEMENT_OR_ATTRIBUTE_ENHANCEMENT(2985),
	// Message: You could not cancel receipt because your inventory is full.
	YOU_COULD_NOT_CANCEL_RECEIPT_BECAUSE_YOUR_INVENTORY_IS_FULL(2988),
	// Message: The Command Channel matching room was cancelled.
	THE_COMMAND_CHANNEL_MATCHING_ROOM_WAS_CANCELLED(2994),
	// Message: You cannot enter the Command Channel matching room because you do not meet the requirements.
	YOU_CANNOT_ENTER_THE_COMMAND_CHANNEL_MATCHING_ROOM_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS(2996),
	// Message: You exited from the Command Channel matching room.
	YOU_EXITED_FROM_THE_COMMAND_CHANNEL_MATCHING_ROOM(2997),
	// Message: You were expelled from the Command Channel matching room.
	YOU_WERE_EXPELLED_FROM_THE_COMMAND_CHANNEL_MATCHING_ROOM(2998),
	// Message: The Command Channel affiliated party's party member cannot use the matching screen.
	THE_COMMAND_CHANNEL_AFFILIATED_PARTYS_PARTY_MEMBER_CANNOT_USE_THE_MATCHING_SCREEN(2999),
	// Message: The Command Channel matching room was created.
	THE_COMMAND_CHANNEL_MATCHING_ROOM_WAS_CREATED(3000),
	// Message: The Command Channel matching room information was edited.
	THE_COMMAND_CHANNEL_MATCHING_ROOM_INFORMATION_WAS_EDITED(3001),
	// Message: When the recipient doesn't exist or the character has been deleted, sending mail is not possible.
	WHEN_THE_RECIPIENT_DOESNT_EXIST_OR_THE_CHARACTER_HAS_BEEN_DELETED_SENDING_MAIL_IS_NOT_POSSIBLE(3002),
	// Message: $c1 entered the Command Channel matching room.
	C1_ENTERED_THE_COMMAND_CHANNEL_MATCHING_ROOM(3003),
	// Message: Shyeed's roar filled with wrath rings throughout the Stakato Nest.
	SHYEEDS_ROAR_FILLED_WITH_WRATH_RINGS_THROUGHOUT_THE_STAKATO_NEST(3007),
	// Message: The mail has arrived.
	THE_MAIL_HAS_ARRIVED(3008),
	// Message: Mail successfully sent.
	MAIL_SUCCESSFULLY_SENT(3009),
	// Message: Mail successfully cancelled.
	MAIL_SUCCESSFULLY_CANCELLED(3011),
	// Message: Mail successfully received.
	MAIL_SUCCESSFULLY_RECEIVED(3012),
	// Message: $c1 has successfully enchanted a +$s2 $s3.
	C1_HAS_SUCCESSFULLY_ENCHANTED_A_S2_S3(3013),
	// Message: You cannot send a mail to yourself.
	YOU_CANNOT_SEND_A_MAIL_TO_YOURSELF(3019),
	// Message: The Kasha's Eye gives you a strange feeling.
	THE_KASHAS_EYE_GIVES_YOU_A_STRANGE_FEELING(3022),
	// Message: I can feel that the energy being flown in the Kasha's eye is getting stronger rapidly.
	I_CAN_FEEL_THAT_THE_ENERGY_BEING_FLOWN_IN_THE_KASHAS_EYE_IS_GETTING_STRONGER_RAPIDLY(3023),
	// Message: Kasha's eye pitches and tosses like it's about to explode.
	KASHAS_EYE_PITCHES_AND_TOSSES_LIKE_ITS_ABOUT_TO_EXPLODE(3024),
	// Message: You cannot use the skill enhancing function on this level. You can use the corresponding function on levels higher than 76Lv .
	YOU_CANNOT_USE_THE_SKILL_ENHANCING_FUNCTION_ON_THIS_LEVEL(3026),
	// Message: You cannot use the skill enhancing function in this class. You can use corresponding function when completing the third class change.
	YOU_CANNOT_USE_THE_SKILL_ENHANCING_FUNCTION_IN_THIS_CLASS(3027),
	// Message: You cannot use the skill enhancing function in this class. You can use the skill enhancing function under off-battle status, and cannot use the function while transforming, battling and on-board.
	YOU_CANNOT_USE_THE_SKILL_ENHANCING_FUNCTION_IN_THIS_CLASS_(3028),
	// Message: By using the skill of Einhasad's holy sword, defeat the evil Lilims!
	BY_USING_THE_SKILL_OF_EINHASADS_HOLY_SWORD_DEFEAT_THE_EVIL_LILIMS(3031),
	// Message: In order to help Anakim, activate the sealing device of the Emperor who is possessed by the evil magical curse! Magical curse is very powerful, so we must be careful!
	IN_ORDER_TO_HELP_ANAKIM_ACTIVATE_THE_SEALING_DEVICE_OF_THE_EMPEROR_WHO_IS_POSSESSED_BY_THE_EVIL_MAGICAL_CURSE_MAGICAL_CURSE_IS_VERY_POWERFUL_SO_WE_MUST_BE_CAREFUL(3032),
	// Message: By using the invisible skill, sneak into the Dawn's document storage!
	BY_USING_THE_INVISIBLE_SKILL_SNEAK_INTO_THE_DAWNS_DOCUMENT_STORAGE(3033),
	// Message: The door in front of us is the entrance to the Dawn's document storage! Approach to the Code Input Device!
	THE_DOOR_IN_FRONT_OF_US_IS_THE_ENTRANCE_TO_THE_DAWNS_DOCUMENT_STORAGE_APPROACH_TO_THE_CODE_INPUT_DEVICE(3034),
	// Message: Male guards can detect the concealment but the female guards cannot.
	MALE_GUARDS_CAN_DETECT_THE_CONCEALMENT_BUT_THE_FEMALE_GUARDS_CANNOT(3037),
	// Message: Female guards notice the disguises from far away better than the male guards do, so beware.
	FEMALE_GUARDS_NOTICE_THE_DISGUISES_FROM_FAR_AWAY_BETTER_THAN_THE_MALE_GUARDS_DO_SO_BEWARE(3038),
	// Message: By using the holy water of Einhasad, open the door possessed by the curse of flames.
	BY_USING_THE_HOLY_WATER_OF_EINHASAD_OPEN_THE_DOOR_POSSESSED_BY_THE_CURSE_OF_FLAMES(3039),
	// Message: By using the Court Magician's Magic Staff, open the door on which the magician's barrier is placed.
	BY_USING_THE_COURT_MAGICIANS_MAGIC_STAFF_OPEN_THE_DOOR_ON_WHICH_THE_MAGICIANS_BARRIER_IS_PLACED(3040),
	// Message: Current location: Inside the Chamber of Delusion
	CURRENT_LOCATION_INSIDE_THE_CHAMBER_OF_DELUSION(3065),
	// Message: $s1 acquired the attached item to your mail.
	S1_ACQUIRED_THE_ATTACHED_ITEM_TO_YOUR_MAIL(3072),
	// Message: You have acquired $s2 $s1.
	YOU_HAVE_ACQUIRED_S2_S1(3073),
	// Message: A user currently participating in the Olympiad cannot send party and friend invitations.
	A_USER_CURRENTLY_PARTICIPATING_IN_THE_OLYMPIAD_CANNOT_SEND_PARTY_AND_FRIEND_INVITATIONS(3094),
	// Message: You are no longer protected from aggressive monsters.
	YOU_ARE_NO_LONGER_PROTECTED_FROM_AGGRESSIVE_MONSTERS(3108),
	// Message: The couple action was denied.
	THE_COUPLE_ACTION_WAS_DENIED(3119),
	// Message: The request cannot be completed because the target does not meet location requirements.
	THE_REQUEST_CANNOT_BE_COMPLETED_BECAUSE_THE_TARGET_DOES_NOT_MEET_LOCATION_REQUIREMENTS(3120),
	// Message: The couple action was cancelled.
	THE_COUPLE_ACTION_WAS_CANCELLED(3121),
	// Message: $c1 is in Private Shop mode or in a battle and cannot be requested for a couple action.
	C1_IS_IN_PRIVATE_SHOP_MODE_OR_IN_A_BATTLE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION(3123),
	// Message: $c1 is fishing and cannot be requested for a couple action.
	C1_IS_FISHING_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION(3124),
	// Message: $c1 is in a battle and cannot be requested for a couple action.
	C1_IS_IN_A_BATTLE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION(3125),
	// Message: $c1 is already participating in a couple action and cannot be requested for another couple action.
	C1_IS_ALREADY_PARTICIPATING_IN_A_COUPLE_ACTION_AND_CANNOT_BE_REQUESTED_FOR_ANOTHER_COUPLE_ACTION(3126),
	// Message: $c1 is in a chaotic state and cannot be requested for a couple action.
	C1_IS_IN_A_CHAOTIC_STATE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION(3127),
	// Message: $c1 is participating in the Olympiad and cannot be requested for a couple action.
	C1_IS_PARTICIPATING_IN_THE_OLYMPIAD_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION(3128),
	// Message: $c1 is in a castle siege and cannot be requested for a couple action.
	C1_IS_IN_A_CASTLE_SIEGE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION(3130),
	// Message: $c1 is riding a ship, steed, or strider and cannot be requested for a couple action.
	C1_IS_RIDING_A_SHIP_STEED_OR_STRIDER_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION(3131),
	// Message: $c1 is currently teleporting and cannot be requested for a couple action.
	C1_IS_CURRENTLY_TELEPORTING_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION(3132),
	// Message: $c1 is currently transforming and cannot be requested for a couple action.
	C1_IS_CURRENTLY_TRANSFORMING_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION(3133),
	// Message: Requesting approval for changing party loot to "$s1".
	REQUESTING_APPROVAL_FOR_CHANGING_PARTY_LOOT_TO_S1(3135),
	// Message: Party loot change was cancelled.
	PARTY_LOOT_CHANGE_WAS_CANCELLED(3137),
	// Message: Party loot was changed to "$s1".
	PARTY_LOOT_WAS_CHANGED_TO_S1(3138),
	// Message: $c1 is currently dead and cannot be requested for a couple action.
	C1_IS_CURRENTLY_DEAD_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION(3139),
	// Message: You have requested a couple action with $c1.
	YOU_HAVE_REQUESTED_A_COUPLE_ACTION_WITH_C1(3150),
	// Message: $c1 is set to refuse couple actions and cannot be requested for a couple action.
	C1_IS_SET_TO_REFUSE_COUPLE_ACTIONS_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION(3164),
	// Message: $c1 is set to refuse duel requests and cannot receive a duel request.
	C1_IS_SET_TO_REFUSE_DUEL_REQUESTS_AND_CANNOT_RECEIVE_A_DUEL_REQUEST(3169),
	// Message: Current location: $s1, $s2, $s3 (outside the Seed of Annihilation)
	CURRENT_LOCATION_S1_S2_S3_OUTSIDE_THE_SEED_OF_ANNIHILATION(3170),
	// Message: You are out of Recommendations. Try again later.
	YOU_ARE_OUT_OF_RECOMMENDATIONS(3206),
	// Message: You obtained $s1 Recommendation(s).
	YOU_OBTAINED_S1_RECOMMENDATIONS(3207),
	// Message: $s1 was successfully added to your Contact List.
	S1_WAS_SUCCESSFULLY_ADDED_TO_YOUR_CONTACT_LIST(3214),
	// Message: The name $s1%  doesn't exist. Please try another name.
	THE_NAME_S1__DOESNT_EXIST(3215),
	// Message: The name already exists on the added list.
	THE_NAME_ALREADY_EXISTS_ON_THE_ADDED_LIST(3216),
	// Message: The name is not currently registered.
	THE_NAME_IS_NOT_CURRENTLY_REGISTERED(3217),
	// Message: $s1 was successfully deleted from your Contact List.
	S1_WAS_SUCCESSFULLY_DELETED_FROM_YOUR_CONTACT_LIST(3219),
	// Message: You cannot add your own name.
	YOU_CANNOT_ADD_YOUR_OWN_NAME(3221),
	// Message: The maximum number of names (100) has been reached. You cannot register any more.
	THE_MAXIMUM_NUMBER_OF_NAMES_100_HAS_BEEN_REACHED(3222),
	// Message: The maximum matches you can participate in 1 week is 70.
	THE_MAXIMUM_MATCHES_YOU_CAN_PARTICIPATE_IN_1_WEEK_IS_70(3224),
	// Message: The total number of matches that can be entered in 1 week is 60 class irrelevant individual matches, 30 specific matches, and 10 team matches.
	THE_TOTAL_NUMBER_OF_MATCHES_THAT_CAN_BE_ENTERED_IN_1_WEEK_IS_60_CLASS_IRRELEVANT_INDIVIDUAL_MATCHES_30_SPECIFIC_MATCHES_AND_10_TEAM_MATCHES(3225),
	// Message: You cannot move while speaking to an NPC. One moment please.
	YOU_CANNOT_MOVE_WHILE_SPEAKING_TO_AN_NPC(3226),
	// Message: Arcane Shield decreased your MP by $s1 instead of HP.
	DUE_TO_THE_EFFECT_OF_THE_ARCANE_SHIELD_MP_RATHER_THAN_HP_RECEIVED_S1S_DAMAGE(3255),
	// Message: MP became 0 and the Arcane Shield is disappearing.
	MP_BECAME_0_AND_THE_ARCANE_SHIELD_IS_DISAPPEARING(3256),
	// Message: You have acquired $s1 EXP (Bonus: $s2) and $s3 SP (Bonus: $s4).
	YOU_HAVE_ACQUIRED_S1_EXP_BONUS_S2_AND_S3_SP_BONUS_S4(3259),
	// Message: You cannot use the skill because the servitor has not been summoned.
	YOU_CANNOT_USE_THE_SKILL_BECAUSE_THE_SERVITOR_HAS_NOT_BEEN_SUMMONED(3260),
	// Message: You have $s1 match(es) remaining that you can participate in this week ($s2 1 vs 1 Class matches, $s3 1 vs 1 matches, & $s4 3 vs 3 Team matches).
	YOU_HAVE_S1_MATCHES_REMAINING_THAT_YOU_CAN_PARTICIPATE_IN_THIS_WEEK_S2_1_VS_1_CLASS_MATCHES_S3_1_VS_1_MATCHES__S4_3_VS_3_TEAM_MATCHES(3261),
	// Message: There are $s2 seconds remaining for $s1's re-use time. It is reset every day at 6:30 AM.
	THERE_ARE_S2_SECONDS_REMAINING_FOR_S1S_REUSE_TIME(3263),
	// Message: There are $s2 minutes $s3 seconds remaining for $s1's re-use time. It is reset every day at 6:30 AM.
	THERE_ARE_S2_MINUTES_S3_SECONDS_REMAINING_FOR_S1S_REUSE_TIME(3264),
	// Message: There are $s2 hours $s3 minutes $s4 seconds remaining for $s1's re-use time. It is reset every day at 6:30 AM.
	THERE_ARE_S2_HOURS_S3_MINUTES_S4_SECONDS_REMAINING_FOR_S1S_REUSE_TIME(3265),
	// Message: The angel Nevit has blessed you from above. You are imbued with full Vitality as well as a Vitality Replenishing effect. And should you die, you will not lose Exp!
	THE_ANGEL_NEVIT_HAS_BLESSED_YOU_FROM_ABOVE_YOU_ARE_IMBUED_WITH_FULL_VITALITY_AS_WELL_AS_A_VITALITY_REPLENISHING_EFFECT(3266),
	// Message: You are starting to feel the effects of Nevit's Advent Blessing.
	YOU_ARE_STARTING_TO_FEEL_THE_EFFECTS_OF_NEVITS_BLESSING(3267),
	// Message: You are further infused with the blessings of Nevit! Continue to battle evil wherever it may lurk.
	YOU_ARE_FURTHER_INFUSED_WITH_THE_BLESSINGS_OF_NEVIT_CONTINUE_TO_BATTLE_EVIL_WHEREVER_IT_MAY_LURK(3268),
	// Message: Nevit's Advent Blessing shines strongly from above. You can almost see his divine aura.
	NEVITS_BLESSING_SHINES_STRONGLY_FROM_ABOVE_YOU_CAN_ALMOST_SEE_HIS_DIVINE_AURA(3269),
	// Message: Nevit's Advent Blessing has ended. Continue your journey and you will surely meet his favor again sometime soon.
	NEVITS_BLESSING_HAS_ENDED_CONTINUE_YOUR_JOURNEY_AND_YOU_WILL_SURELY_MEET_HIS_FAVOR_AGAIN_SOMETIME_SOON(3275),
	// Message: You have completed the initial level. Congratulations~!
	YOU_HAVE_COMPLETED_THE_INITIAL_LEVEL(6039),
	// Message: The skill was canceled due to insufficient energy.
	THE_SKILL_HAS_BEEN_CANCELED_BECAUSE_YOU_HAVE_INSUFFICIENT_ENERGY(6042),
	// Message: You cannot replenish energy because you do not meet the requirements.
	YOUR_ENERGY_CANNOT_BE_REPLENISHED_BECAUSE_CONDITIONS_ARE_NOT_MET(6043),
	// Message: Energy was replenished by $s1.
	ENERGY_S1_REPLENISHED(6044),
	// Message: The premium item for this account was provided. If the premium account is terminated, this item will be deleted.
	THE_PREMIUM_ITEM_FOR_THIS_ACCOUNT_WAS_PROVIDED(6046),
	// Message: The premium item cannot be received because the inventory weight/quantity limit has been exceeded.
	THE_PREMIUM_ITEM_CANNOT_BE_RECEIVED_BECAUSE_THE_INVENTORY_WEIGHTQUANTITY_LIMIT_HAS_BEEN_EXCEEDED(6047),
	// Message: The remium account has been terminated. The provided premium item was deleted.
	THE_REMIUM_ACCOUNT_HAS_BEEN_TERMINATED(6048),
	// Message: You cannot bookmark this location because you do not have a My Teleport Flag.
	YOU_CANNOT_BOOKMARK_THIS_LOCATION_BECAUSE_YOU_DO_NOT_HAVE_A_MY_TELEPORT_FLAG(6501);

	private final L2GameServerPacket _message;
	private final int _id;
	private final int _size;

	SystemMsg(int id)
	{
		_id = id;

		if(name().contains("S4") || name().contains("C4"))
		{
			_size = 4;
			_message = null;
		}
		else if(name().contains("S3") || name().contains("C3"))
		{
			_size = 3;
			_message = null;
		}
		else if(name().contains("S2") || name().contains("C2"))
		{
			_size = 2;
			_message = null;
		}
		else if(name().contains("S1") || name().contains("C1"))
		{
			_size = 1;
			_message = null;
		}
		else
		{
			_size = 0;
			_message = new SystemMessage(this);
		}
	}

	public int id()
	{
		return _id;
	}

	public int size()
	{
		return _size;
	}

	public static SystemMsg valueOf(int id)
	{
		for(SystemMsg m : values())
			if(m.id() == id)
				return m;

		throw new NoSuchElementException("SystemMsg not found: " + id);
	}

	@Override
	public L2GameServerPacket packet(Player player)
	{
		if(_message == null)
			Log.debug("SystemMsg.packet(Player), but message require arguments: " + name(), new Exception());

		return _message;
	}
}
