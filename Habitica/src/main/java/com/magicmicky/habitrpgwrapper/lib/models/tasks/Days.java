package com.magicmicky.habitrpgwrapper.lib.models.tasks;

import android.os.Parcel;
import android.os.Parcelable;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by viirus on 06/07/15.
 */
@ModelContainer
@Table(databaseName = HabitDatabase.NAME)
public class Days extends BaseModel implements Parcelable {

    @Column
    @PrimaryKey
    @NotNull
    String task_id;

    @Column
    private boolean m, t, w, th, f, s, su;

    public Days() {
        this.m = false;
        this.t = false;
        this.w = false;
        this.th = false;
        this.f = false;
        this.s = true;
        this.su = true;
    }

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
        dest.writeString(this.task_id);
        dest.writeByte(this.m ? (byte) 1 : (byte) 0);
        dest.writeByte(this.t ? (byte) 1 : (byte) 0);
        dest.writeByte(this.w ? (byte) 1 : (byte) 0);
        dest.writeByte(this.th ? (byte) 1 : (byte) 0);
        dest.writeByte(this.f ? (byte) 1 : (byte) 0);
        dest.writeByte(this.s ? (byte) 1 : (byte) 0);
        dest.writeByte(this.su ? (byte) 1 : (byte) 0);
    }

    protected Days(Parcel in) {
        this.task_id = in.readString();
        this.m = in.readByte() != 0;
        this.t = in.readByte() != 0;
        this.w = in.readByte() != 0;
        this.th = in.readByte() != 0;
        this.f = in.readByte() != 0;
        this.s = in.readByte() != 0;
        this.su = in.readByte() != 0;
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
}
