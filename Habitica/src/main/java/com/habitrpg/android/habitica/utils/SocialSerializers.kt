package com.habitrpg.android.habitica.utils

import com.habitrpg.android.habitica.models.inventory.Quest
import com.habitrpg.android.habitica.models.inventory.QuestMember
import com.habitrpg.android.habitica.models.inventory.QuestProgress
import com.habitrpg.android.habitica.models.inventory.QuestProgressCollect
import com.habitrpg.android.habitica.models.inventory.QuestRageStrike
import com.habitrpg.android.habitica.models.invitations.InviteResponse
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.social.ChallengeCategory
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.social.ChatMessageLike
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.social.GroupCategory
import com.habitrpg.android.habitica.models.user.ContributorInfo
import com.habitrpg.android.habitica.models.user.Items
import com.habitrpg.shared.habitica.serialization.DateSerializer
import com.habitrpg.shared.habitica.serialization.JsonTreeSerializer
import com.habitrpg.shared.habitica.serialization.array
import com.habitrpg.shared.habitica.serialization.bool
import com.habitrpg.shared.habitica.serialization.decode
import com.habitrpg.shared.habitica.serialization.double
import com.habitrpg.shared.habitica.serialization.encode
import com.habitrpg.shared.habitica.serialization.int
import com.habitrpg.shared.habitica.serialization.obj
import com.habitrpg.shared.habitica.serialization.primitive
import com.habitrpg.shared.habitica.serialization.string
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

object GroupSerializer : JsonTreeSerializer<Group>("Group") {
    override fun Json.read(element: JsonElement): Group {
        val obj = element as JsonObject
        val group = Group()
        group.id = obj.string("_id") ?: ""
        group.name = obj.string("name")
        group.description = obj.string("description")
        group.summary = obj.string("summary")
        group.leaderMessage = obj.string("leaderMessage")
        obj.string("privacy")?.let { group.privacy = it }
        obj.int("memberCount")?.let { group.memberCount = it }
        obj.double("balance")?.let { group.balance = it }
        group.logo = obj.string("logo")
        obj.string("type")?.let { group.type = it }
        when (val leader = obj["leader"]) {
            is JsonPrimitive -> group.leaderID = leader.takeUnless { it is JsonNull }?.content
            is JsonObject -> {
                group.leaderID = leader.string("_id")
                leader.obj("profile")?.string("name")?.let { group.leaderName = it }
            }
            else -> {}
        }
        obj.obj("managers")?.forEach { (key, value) ->
            if ((value as? JsonPrimitive)?.booleanOrNull == true) group.managers?.add(key)
        }
        obj.obj("quest")?.let { questObject ->
            val quest = decode<Quest>(questObject) ?: return@let
            quest.id = group.id
            // Thin placeholders for the members this payload mentions; SocialRepositoryImpl reconciles them against the full local party roster before persisting.
            questObject.obj("members")?.forEach { (key, value) ->
                quest.participants?.add(
                    Member().apply {
                        id = key
                        participatesInQuest = (value as? JsonPrimitive)?.booleanOrNull
                    },
                )
            }
            questObject.obj("extra")?.obj("worldDmg")?.forEach { (key, value) ->
                quest.addRageStrike(QuestRageStrike(key, value.jsonPrimitive.booleanOrNull ?: false))
            }
            group.quest = quest
        }
        obj.obj("leaderOnly")?.let { leaderOnly ->
            leaderOnly.bool("challenges")?.let { group.leaderOnlyChallenges = it }
            leaderOnly.bool("getGems")?.let { group.leaderOnlyGetGems = it }
        }
        group.tasksOrder = decode(obj["tasksOrder"])
        obj.array("categories")?.forEach { category -> decode<GroupCategory>(category)?.let { group.categories?.add(it) } }
        return group
    }

    override fun Json.write(value: Group): JsonElement =
        buildJsonObject {
            put("name", value.name)
            put("description", value.description)
            put("summary", value.summary)
            put("logo", value.logo)
            put("type", value.type)
            put("leader", value.leaderID)
            putJsonObject("leaderOnly") {
                put("challenges", value.leaderOnlyChallenges)
                put("getGems", value.leaderOnlyGetGems)
            }
        }
}

object ChallengeSerializer : JsonTreeSerializer<Challenge>("Challenge") {
    override fun Json.read(element: JsonElement): Challenge {
        val obj = element as JsonObject
        val challenge = Challenge()
        challenge.id = obj.string("id")
        challenge.name = obj.string("name")
        challenge.shortName = obj.string("shortName") ?: ""
        challenge.description = obj.string("description") ?: ""
        challenge.memberCount = obj.int("memberCount") ?: 0
        obj.int("prize")?.let { challenge.prize = it }
        challenge.official = obj.bool("official") ?: false
        obj.obj("leader")?.let { leader ->
            leader.obj("profile")?.let { profile ->
                challenge.leaderName = profile.string("name")
                challenge.leaderId = leader.string("id") ?: leader.string("_id")
            }
        }
        obj.array("categories")?.forEach { categoryElement ->
            val category = categoryElement as JsonObject
            challenge.categories.add(
                ChallengeCategory().apply {
                    slug = category.string("slug") ?: ""
                    name = category.string("name") ?: ""
                    id = category.string("_id") ?: ""
                },
            )
        }
        challenge.createdAt = decode(obj["createdAt"])
        challenge.updatedAt = decode(obj["updatedAt"])
        obj.string("summary")?.let { challenge.summary = it }
        obj.obj("group")?.let { group ->
            challenge.groupName = group.string("name")
            challenge.groupId = group.string("_id")
        }
        obj.obj("tasksOrder")?.let { tasksOrder ->
            fun ids(key: String) = tasksOrder.array(key)?.joinToString(",") { it.jsonPrimitive.content } ?: ""
            challenge.todoList = ids(Challenge.TASK_ORDER_TODOS)
            challenge.dailyList = ids(Challenge.TASK_ORDER_DAILYS)
            challenge.habitList = ids(Challenge.TASK_ORDER_HABITS)
            challenge.rewardList = ids(Challenge.TASK_ORDER_REWARDS)
        }
        return challenge
    }

    override fun Json.write(value: Challenge): JsonElement =
        buildJsonObject {
            put("id", value.id)
            put("name", value.name)
            put("shortName", value.shortName)
            put("description", value.description)
            put("memberCount", value.memberCount)
            put("prize", value.prize)
            put("official", value.official)
            put("group", value.groupId)
            put("tasksOrder", encode(value.tasksOrder))
        }
}

object ChatMessageSerializer : JsonTreeSerializer<ChatMessage>("ChatMessage") {
    override fun Json.read(element: JsonElement): ChatMessage {
        val obj = element as JsonObject
        val message = ChatMessage()
        message.text = obj.string("text")
        if (obj.containsKey("timestamp")) {
            message.timestamp = DateSerializer.parse(obj.primitive("timestamp")?.content)?.time
        }
        obj.obj("likes")?.let { likes ->
            likes.forEach { (key, value) ->
                if ((value as? JsonPrimitive)?.booleanOrNull == true) message.likes?.add(ChatMessageLike(key))
            }
            message.likeCount = message.likes?.size ?: 0
        }
        obj.int("flagCount")?.let { message.flagCount = it }
        obj.string("uuid")?.let { message.uuid = it }
        message.contributor =
            when (val contributor = obj["contributor"]) {
                is JsonObject -> decode<ContributorInfo>(contributor)
                is JsonPrimitive -> contributor.takeUnless { it is JsonNull }?.let { ContributorInfo().apply { text = it.content } }
                else -> null
            }
        message.backer = decode(obj["backer"])
        obj.string("user")?.let { message.user = it }
        obj.string("username")?.let { message.username = it }
        obj.bool("sent")?.let { message.sent = it }
        message.userStyles = decode(obj["userStyles"])
        obj.string("id")?.let { message.id = it }
        return message
    }
}

object MemberSerializer : JsonTreeSerializer<Member>("Member") {
    override fun Json.read(element: JsonElement): Member {
        val obj = element as JsonObject
        val member = Member()
        member.id = obj.string("_id") ?: throw SerializationException("Member without _id")
        member.flags = decode(obj["flags"])
        member.stats = decode(obj["stats"])
        member.inbox = decode(obj["inbox"])
        member.preferences = decode(obj["preferences"])
        member.profile = decode(obj["profile"])
        member.party = decode(obj["party"])
        member.party?.quest?.id = member.id
        obj.obj("items")?.let { items ->
            member.items =
                decode<Items>(items)?.apply {
                    gear = null
                    special = null
                }
            items.string("currentMount")?.let { member.currentMount = it }
            items.string("currentPet")?.let { member.currentPet = it }
            items.obj("gear")?.let { gear ->
                if (gear.containsKey("costume")) member.costume = decode(gear["costume"])
                if (gear.containsKey("equipped")) member.equipped = decode(gear["equipped"])
            }
        }
        member.contributor = decode(obj["contributor"])
        member.backer = decode(obj["backer"])
        member.authentication = decode(obj["auth"])
        obj.int("loginIncentives")?.let { member.loginIncentives = it }
        return member
    }
}

object QuestSerializer : JsonTreeSerializer<Quest>("Quest") {
    override fun Json.read(element: JsonElement): Quest {
        val obj = element as JsonObject
        val quest = Quest()
        obj.obj("progress")?.let { progressObject ->
            quest.progress =
                QuestProgress().apply {
                    key = quest.key
                    progressObject.double("hp")?.let { hp = it }
                    progressObject.double("rage")?.let { rage = it }
                    progressObject.double("up")?.let { up = it.toFloat() }
                    progressObject.double("down")?.let { down = it.toFloat() }
                    progressObject.int("collectedItems")?.let { collectedItems = it }
                    progressObject.obj("collect")?.forEach { (key, value) ->
                        collect?.add(
                            QuestProgressCollect().apply {
                                this.key = key
                                count = (value as? JsonPrimitive)?.doubleOrNull?.toInt() ?: 0
                            },
                        )
                    }
                }
        }
        quest.key = obj.string("key") ?: return quest
        obj.bool("active")?.let { quest.active = it }
        obj.string("leader")?.let { quest.leader = it }
        if (obj.containsKey("RSVPNeeded")) {
            obj.bool("RSVPNeeded")?.let { quest.rsvpNeeded = it }
        } else {
            quest.rsvpNeededWasSpecified = false
        }
        obj.obj("members")?.forEach { (key, value) ->
            quest.members?.add(
                QuestMember().apply {
                    this.key = key
                    isParticipating = (value as? JsonPrimitive)?.booleanOrNull
                },
            )
        }
        return quest
    }
}

/** Email invites come back as plain strings, user invites as objects. */
object InviteResponseSerializer : JsonTreeSerializer<InviteResponse>("InviteResponse") {
    override fun Json.read(element: JsonElement): InviteResponse =
        when {
            element is JsonPrimitive && element.isString -> InviteResponse.EmailInvite(element.content)
            element is JsonObject ->
                InviteResponse.UserInvite(
                    id = element.string("id") ?: "",
                    name = element.string("name") ?: "",
                    inviter = element.string("inviter") ?: "",
                )
            else -> throw SerializationException("Unexpected InviteResponse: $element")
        }
}
