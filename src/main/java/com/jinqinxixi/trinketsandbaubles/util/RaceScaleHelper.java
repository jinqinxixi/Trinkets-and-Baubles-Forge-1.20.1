package com.jinqinxixi.trinketsandbaubles.util;

import net.minecraft.world.entity.LivingEntity;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

/**
 * 种族体型缩放工具类
 * 直接控制模型大小和碰撞箱
 */
public class RaceScaleHelper {

    /**
     * 设置实体的视觉模型和碰撞箱大小
     * @param entity 目标实体
     * @param scale 缩放值(直接值,例如想要3倍大就传3.0f)
     */
    public static void setModelScale(LivingEntity entity, float scale) {
        if (entity == null) return;

        // 直接设置模型和碰撞箱大小
        ScaleTypes.MODEL_WIDTH.getScaleData(entity).setScale(scale);
        ScaleTypes.MODEL_HEIGHT.getScaleData(entity).setScale(scale);
        ScaleTypes.HITBOX_WIDTH.getScaleData(entity).setScale(scale);
        ScaleTypes.HITBOX_HEIGHT.getScaleData(entity).setScale(scale);
        ScaleTypes.EYE_HEIGHT.getScaleData(entity).setScale(scale);
    }

    /**
     * 重置实体的缩放到正常大小
     */
    public static void resetModelScale(LivingEntity entity) {
        setModelScale(entity, 1.0f);
    }

    /**
     * 设置平滑过渡的缩放
     * @param entity 目标实体
     * @param scale 目标缩放值
     * @param ticks 过渡tick数(20 ticks = 1秒)
     */
    public static void setSmoothModelScale(LivingEntity entity, float scale, int ticks) {
        if (entity == null) return;

        // 设置目标缩放值和过渡时间
        ScaleData data;

        data = ScaleTypes.MODEL_WIDTH.getScaleData(entity);
        data.setTargetScale(scale);
        data.setScaleTickDelay(ticks);

        data = ScaleTypes.MODEL_HEIGHT.getScaleData(entity);
        data.setTargetScale(scale);
        data.setScaleTickDelay(ticks);

        data = ScaleTypes.HITBOX_WIDTH.getScaleData(entity);
        data.setTargetScale(scale);
        data.setScaleTickDelay(ticks);

        data = ScaleTypes.HITBOX_HEIGHT.getScaleData(entity);
        data.setTargetScale(scale);
        data.setScaleTickDelay(ticks);

        data = ScaleTypes.EYE_HEIGHT.getScaleData(entity);
        data.setTargetScale(scale);
        data.setScaleTickDelay(ticks);
    }

    /**
     * 获取当前的模型缩放值
     */
    public static float getCurrentModelScale(LivingEntity entity) {
        if (entity == null) return 1.0f;
        return ScaleTypes.MODEL_HEIGHT.getScaleData(entity).getScale();
    }
}