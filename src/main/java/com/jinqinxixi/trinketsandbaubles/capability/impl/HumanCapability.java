package com.jinqinxixi.trinketsandbaubles.capability.impl;

import com.jinqinxixi.trinketsandbaubles.capability.api.IHumanCapability;
import com.jinqinxixi.trinketsandbaubles.capability.base.AbstractRaceCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;

/**
 * 人类种族能力实现。
 *
 * <p>对应 NONE（无种族）状态：无能力、无属性、体型 1.0、无外观特征，只有一组默认色供装饰饰品
 * 借色和调色界面调色。它不会被玩家主动「变身」成——无种族状态就是它，所以这里 {@code isActive}
 * 保持默认 false，由装饰饰品的渲染和调色界面在「找不到激活种族」时把它当作借色/调色对象。
 *
 * <p>默认色即 NONE 的无参构造值：primary 0xA987A4、secondary 0xF9E0F5、aux 0xF9E0F5。
 */
public class HumanCapability extends AbstractRaceCapability implements IHumanCapability
{
    public HumanCapability(Player player)
    {
        super(player);
        this.scaleFactor = 1.0F;
    }

    @Override
    protected void registerAttributeValues()
    {
        // 人类无属性加成。
    }

    @Override
    public String getRaceId()
    {
        return "human";
    }

    @Override
    public String getRaceName()
    {
        return "HUMAN";
    }

    @Override
    public float getManaBonus()
    {
        return 0.0F;
    }

    @Override
    protected void onTick()
    {
        // 人类无每 tick 效果。
    }

    @Override
    public void onBreakBlock(BlockPos pos, Block block, ServerLevel level)
    {
        // 人类无破坏方块的种族特效。
    }

    @Override
    protected int defaultPrimaryColor()
    {
        return 0xA987A4;
    }

    @Override
    protected int defaultSecondaryColor()
    {
        return 0xF9E0F5;
    }

    @Override
    protected int defaultAuxColor()
    {
        return 0xF9E0F5;
    }
}