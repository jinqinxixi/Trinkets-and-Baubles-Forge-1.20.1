package com.jinqinxixi.trinketsandbaubles.client.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;

import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static final KeyMapping DASH_KEY = new KeyMapping(
            "key.trinketsandbaubles.dash",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_ALT,  // 使用左 ALT 键
            "key.categories.trinketsandbaubles"
    );
    // 充能按键
    public static final KeyMapping CHARGE_KEY = new KeyMapping(
            "key.trinketsandbaubles.charge",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "key.categories.trinketsandbaubles"
    );
    // 偏振石开关吸引模式
    public static final KeyMapping ATTRACTION_TOGGLE_KEY = new KeyMapping(
            "key.trinketsandbaubles.attraction_toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Y,
            "key.categories.trinketsandbaubles"
    );

    // 偏振石开关偏转模式
    public static final KeyMapping DEFLECTION_TOGGLE_KEY = new KeyMapping(
            "key.trinketsandbaubles.deflection_toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_U,
            "key.categories.trinketsandbaubles"
    );

    // HUD 位置切换
    public static final KeyMapping MANA_HUD_POSITION_KEY  = new KeyMapping(
            "key.trinketsandbaubles.mana_hud_position",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,  // 使用 H 键作为切换键
            "key.categories.trinketsandbaubles"
    );

    // 添加新的按键绑定
    public static final KeyMapping TOGGLE_DRAGONS_EYE_MODE = new KeyMapping(
            "key.trinketsandbaubles.toggle_dragons_eye_mode",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            "key.categories.trinketsandbaubles"
    );

    public static final KeyMapping TOGGLE_DRAGONS_EYE_VISION = new KeyMapping(
            "key.trinketsandbaubles.toggle_dragons_eye_vision",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            "key.categories.trinketsandbaubles"
    );

    // 龙之效果的夜视切换
    public static final KeyMapping DRAGON_NIGHT_VISION_KEY = new KeyMapping(
            "key.trinketsandbaubles.dragon_night_vision",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_I,
            "key.categories.trinketsandbaubles"
    );

    // 龙之效果的喷火
    public static final KeyMapping DRAGON_BREATH_KEY = new KeyMapping(
            "key.trinketsandbaubles.dragon_breath",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.trinketsandbaubles"
    );
}