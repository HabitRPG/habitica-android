package com.habitrpg.android.habitica.data.local.implementation;

import com.habitrpg.android.habitica.data.local.SocialLocalRepository;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.models.members.Member;
import com.habitrpg.android.habitica.models.social.ChatMessage;
import com.habitrpg.android.habitica.models.social.ChatMessageLike;
import com.habitrpg.android.habitica.models.social.Group;
import com.habitrpg.android.habitica.models.user.User;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.Observable;


public class RealmSocialLocalRepository extends RealmBaseLocalRepository implements SocialLocalRepository {

    public RealmSocialLocalRepository(Realm realm) {
        super(realm);
    }

    @Override
    public Observable<RealmResults<Group>> getGroups(String type) {
        return realm.where(Group.class)
                .equalTo("type", type)
                .findAllAsync()
                .asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public Observable<RealmResults<Group>> getPublicGuilds() {
        return realm.where(Group.class)
                .equalTo("type", "guild")
                .equalTo("privacy", "public")
                .findAllSorted("memberCount", Sort.DESCENDING)
                .asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public Observable<Group> getGroup(String id) {
        return realm.where(Group.class)
                .equalTo("id", id)
                .findAllAsync()
                .asObservable()
                .filter(group -> group.isLoaded() && group.isValid() && !group.isEmpty())
                .map(groups -> groups.first());
    }

    @Override
    public Observable<RealmResults<Group>> getUserGroups() {
        return realm.where(Group.class)
                .equalTo("type", "guild")
                .equalTo("isMember", true)
                .findAllSorted("memberCount", Sort.DESCENDING)
                .asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public Observable<RealmResults<ChatMessage>> getGroupChat(String groupId) {
        return realm.where(ChatMessage.class)
                .equalTo("groupId", groupId)
                .findAllSorted("timestamp", Sort.DESCENDING)
                .asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public void deleteMessage(String id) {
        getMessage(id).first().subscribe(chatMessage -> realm.executeTransaction(realm1 -> chatMessage.deleteFromRealm()), RxErrorHandler.handleEmptyError());
    }

    @Override
    public Observable<RealmResults<Member>> getGroupMembers(String partyId) {
        return realm.where(Member.class)
                .equalTo("party.id", partyId)
                .findAllAsync()
                .asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public void updateRSVPNeeded(User user, boolean newValue) {
        if (user != null && user.getParty() != null && user.getParty().getQuest() != null) {
            realm.executeTransaction(realm1 -> user.getParty().getQuest().RSVPNeeded = newValue);
        }
    }

    @Override
    public void likeMessage(ChatMessage chatMessage, String userId, boolean liked) {
        if (chatMessage.userLikesMessage(userId) == liked) {
            return;
        }
        realm.executeTransaction(realm1 -> {
            if (liked) {
                chatMessage.likes.add(new ChatMessageLike(userId));
            } else {
                for (ChatMessageLike like : chatMessage.likes) {
                    if (userId.equals(like.id)) {
                        like.deleteFromRealm();
                        return;
                    }
                }
            }
        });
    }

    @Override
    public void saveGroupMembers(String groupId, List<Member> members) {
        realm.executeTransaction(realm1 -> realm.insertOrUpdate(members));
        if (groupId != null) {
            List<Member> existingMembers = realm.where(Member.class).equalTo("party.id", groupId).findAll();
            List<Member> membersToRemove = new ArrayList<>();
            for (Member existingMember : existingMembers) {
                boolean isStillMember = false;
                for (Member newMember : members) {
                    if (existingMember.getId() != null && existingMember.getId().equals(newMember.getId())) {
                        isStillMember = true;
                        break;
                    }
                }
                if (!isStillMember) {
                    membersToRemove.add(existingMember);
                }
            }
            realm.executeTransaction(realm1 -> {
                for (Member member : membersToRemove) {
                    member.deleteFromRealm();
                }
            });
        }

    }

    @Override
    public void removeQuest(String partyId) {
        Group party = realm.where(Group.class).equalTo("id", partyId).findFirst();
        if (party != null) {
            realm.executeTransaction(realm1 -> {
                party.quest = null;
            });
        }
    }

    @Override
    public void setQuestActivity(Group party, boolean active) {
        realm.executeTransaction(realm1 -> {
            if (party != null && party.quest != null) {
                party.quest.active = active;
            }
        });
    }


    private Observable<ChatMessage> getMessage(String id) {
        return realm.where(ChatMessage.class).equalTo("id", id)
                .findAllAsync()
                .asObservable()
                .filter(messages -> messages.isLoaded() && messages.isValid() && !messages.isEmpty())
                .map(messages -> messages.first());
    }
}
