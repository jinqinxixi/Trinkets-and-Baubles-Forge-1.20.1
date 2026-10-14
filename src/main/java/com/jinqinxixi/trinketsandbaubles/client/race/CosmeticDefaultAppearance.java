package com.jinqinxixi.trinketsandbaubles.client.race;

import com.jinqinxixi.trinketsandbaubles.capability.api.IRaceAppearance;

/**
 * 装饰饰品在没有种族可借颜色时用的默认外观：「无种族」的 NONE 种族颜色，默认形状、颜色
 * 原样（option 0）。
 *
 * <p>装饰饰品不是种族，没有自己的 capability。有激活种族时它借那个种族的颜色；没有时就用
 * NONE 种族的默认色——primary 0xA987A4、secondary 0xF9E0F5、aux 0xF9E0F5（即 NONE 的三色），
 * 而不是全白。
 */
public final class CosmeticDefaultAppearance implements IRaceAppearance
{
    public static final CosmeticDefaultAppearance INSTANCE = new CosmeticDefaultAppearance();

    private CosmeticDefaultAppearance()
    {
    }

    @Override
    public boolean showTraits()
    {
        return true;
    }

    @Override
    public void setShowTraits(boolean show)
    {
    }

    @Override
    public int traitVariant()
    {
        return 0;
    }

    @Override
    public void setTraitVariant(int variant)
    {
    }

    @Override
    public int primaryTraitColor()
    {
        return 0xA987A4;
    }

    @Override
    public void setPrimaryTraitColor(int colour)
    {
    }

    @Override
    public int secondaryTraitColor()
    {
        return 0xF9E0F5;
    }

    @Override
    public void setSecondaryTraitColor(int colour)
    {
    }

    @Override
    public int auxTraitColor()
    {
        return 0xF9E0F5;
    }

    @Override
    public void setAuxTraitColor(int colour)
    {
    }

    @Override
    public int auxTraitVariant()
    {
        return 0;
    }

    @Override
    public void setAuxTraitVariant(int variant)
    {
    }

    @Override
    public int traitColorOption()
    {
        return 0;
    }

    @Override
    public void setTraitColorOption(int option)
    {
    }
}