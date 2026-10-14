package com.jinqinxixi.trinketsandbaubles.items.baubles;

/**
 * 装饰饰品的 18 种外观特征。
 *
 * <p>三种（HUMAN / DWARF / TITAN）无外观——它们对应「不显示任何特征」，本枚举保留它们是为了序号
 * 固定，否则循环切换会对不上哪一号是哪副打扮。
 */
public enum CosmeticFeature
{
    HUMAN,
    FAIRY,
    DWARF,
    ELF,
    GOBLIN,
    FAELIS,
    TITAN,
    DRAGON,
    TAURUS,
    TAURUS_BELL,
    TAURUS_F,
    TAURUS_F_BELL,
    TAURIAN_BELL,
    SUCCUBUS,
    GENERIC_HORNS,
    GENERIC_HORNS_INVERTED,
    DRAGON_HORNS,
    FAELIS_TAIL;

    public static CosmeticFeature byId(int id)
    {
        CosmeticFeature[] values = values();
        return id >= 0 && id < values.length ? values[id] : HUMAN;
    }
}