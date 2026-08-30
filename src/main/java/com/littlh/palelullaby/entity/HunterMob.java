package com.littlh.palelullaby.entity;

/** Marker for every blood-hunter mob (both self-written and Iron-based). */
public interface HunterMob {
    HunterRank hunterRank();

    /** 营地随机外观装备：按血猎库在该阶级允许的装备池内随机抽一套。 */
    void equipCampGear();
}
