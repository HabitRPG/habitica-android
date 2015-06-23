package com.magicmicky.habitrpgwrapper.lib.models;

/**
 * Created by MagicMicky on 16/03/14.
 */
public class Party {
    private String currrent; //id
    private String invitation;
    private String lastMessageSeen;
    private boolean leader;
    private Quest quest;
    private String order;//Order to display ppl

    public Party() {

    }

    public Party(String currrent, String invitation, String lastMessageSeen, boolean leader, Quest quest, String order) {
        this.currrent = currrent;
        this.invitation = invitation;
        this.lastMessageSeen = lastMessageSeen;
        this.leader = leader;
        this.quest = quest;
        this.order = order;
    }

    public String getCurrrent() {
        return currrent;
    }

    public void setCurrrent(String currrent) {
        this.currrent = currrent;
    }

    public String getInvitation() {
        return invitation;
    }

    public void setInvitation(String invitation) {
        this.invitation = invitation;
    }

    public String getLastMessageSeen() {
        return lastMessageSeen;
    }

    public void setLastMessageSeen(String lastMessageSeen) {
        this.lastMessageSeen = lastMessageSeen;
    }

    public boolean isLeader() {
        return leader;
    }

    public void setLeader(boolean leader) {
        this.leader = leader;
    }

    public Quest getQuest() {
        return quest;
    }

    public void setQuest(Quest quest) {
        this.quest = quest;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public class Quest {
        private String key;
        private Progress progress;

        private Quest(String key, Progress progress) {
            this.key = key;
            this.progress = progress;
        }

        public Progress getProgress() {
            return progress;
        }

        public void setProgress(Progress progress) {
            this.progress = progress;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        private class Progress {
            private float down, up;

            private Progress(float down, float up) {
                this.down = down;
                this.up = up;
            }

            public float getDown() {
                return down;
            }

            public void setDown(float down) {
                this.down = down;
            }

            public float getUp() {
                return up;
            }

            public void setUp(float up) {
                this.up = up;
            }
        }
    }
}
