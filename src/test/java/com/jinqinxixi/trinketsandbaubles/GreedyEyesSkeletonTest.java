package com.jinqinxixi.trinketsandbaubles;

import com.jinqinxixi.trinketsandbaubles.util.ActivationMethod;

/**
 * B 阶段编译/运行冒烟桩（零依赖，纯 JDK main）。
 *
 * <p>只校验纯枚举 {@link ActivationMethod#isActive(boolean)} 的 switch 逻辑（照原版逐字），
 * 不依赖 Minecraft / JUnit。D 阶段可用 {@code javac} 直接运行验证骨架可编译、可执行。
 */
public final class GreedyEyesSkeletonTest {
    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static void main(String[] args) {
        check(ActivationMethod.SNEAK.isActive(true), "SNEAK should be active while sneaking");
        check(!ActivationMethod.SNEAK.isActive(false), "SNEAK should be inactive while standing");
        check(ActivationMethod.STAND.isActive(false), "STAND should be active while standing");
        check(!ActivationMethod.STAND.isActive(true), "STAND should be inactive while sneaking");
        check(ActivationMethod.ALWAYS.isActive(false), "ALWAYS should always be active");
        check(ActivationMethod.ALWAYS.isActive(true), "ALWAYS should always be active");
        check(!ActivationMethod.NEVER.isActive(true), "NEVER should never be active");

        System.out.println("GreedyEyesSkeletonTest passed");
    }

    private GreedyEyesSkeletonTest() {
    }
}
