package com.habitrpg.android.habitica.models.tasks;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Days extends RealmObject implements Parcelable {

    @PrimaryKey
    private String taskId;
    private boolean m, t, w, th, f, s, su;

    public boolean getT() {
        return t;
    }

    public void setT(boolean t) {
        this.t = t;
    }

    public boolean getW() {
        return w;
    }

    public void setW(boolean w) {
        this.w = w;
    }

    public boolean getTh() {
        return th;
    }

    public void setTh(boolean th) {
        this.th = th;
    }

    public boolean getF() {
        return f;
    }

    public void setF(boolean f) {
        this.f = f;
    }

    public boolean getS() {
        return s;
    }

    public void setS(boolean s) {
        this.s = s;
    }

    public boolean getSu() {
        return su;
    }

    public void setSu(boolean su) {
        this.su = su;
    }

    public boolean getM() {
        return m;
    }

    public void setM(boolean m) {
        this.m = m;
    }

    public boolean getForDay(int day) {
        switch (day) {
            case 2:
                return this.getM();
            case 3:
                return this.getT();
            case 4:
                return this.getW();
            case 5:
                return this.getTh();
            case 6:
                return this.getF();
            case 7:
                return this.getS();
            case 1:
                return this.getSu();
        }
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.taskId);
        dest.writeByte(this.m ? (byte) 1 : (byte) 0);
        dest.writeByte(this.t ? (byte) 1 : (byte) 0);
        dest.writeByte(this.w ? (byte) 1 : (byte) 0);
        dest.writeByte(this.th ? (byte) 1 : (byte) 0);
        dest.writeByte(this.f ? (byte) 1 : (byte) 0);
        dest.writeByte(this.s ? (byte) 1 : (byte) 0);
        dest.writeByte(this.su ? (byte) 1 : (byte) 0);
    }

    protected Days(Parcel in) {
        this.taskId = in.readString();
        this.m = in.readByte() != 0;
        this.t = in.readByte() != 0;
        this.w = in.readByte() != 0;
        this.th = in.readByte() != 0;
        this.f = in.readByte() != 0;
        this.s = in.readByte() != 0;
        this.su = in.readByte() != 0;
    }

    public Days() {
        m = true;
        t = true;
        w = true;
        th = true;
        f = true;
        s = true;
        su = true;
    }

    public static final Parcelable.Creator<Days> CREATOR = new Parcelable.Creator<Days>() {
        @Override
        public Days createFromParcel(Parcel source) {
            return new Days(source);
        }

        @Override
        public Days[] newArray(int size) {
            return new Days[size];
        }
    };

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
}
