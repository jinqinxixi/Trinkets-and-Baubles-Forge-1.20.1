package com.jinqinxixi.trinketsandbaubles.util;

/**
 * 触发方式枚举。
 *
 * <p>用于贪婪之眼的龙吼提示（{@code growlActivation}）：SNEAK=潜行时触发、STAND=站立时触发、
 * ALWAYS=始终触发、NEVER=从不触发。
 */
public enum ActivationMethod {
    SNEAK,
    STAND,
    ALWAYS,
    NEVER;

    public boolean isActive(boolean sneaking) {
        return switch (this) {
            case SNEAK -> sneaking;
            case STAND -> !sneaking;
            case ALWAYS -> true;
            case NEVER -> false;
        };
    }
}
