import os

PACKAGE_PATH = "src/main/java/com/littlh/bloodline"
MOD_ID = "bloodline"

def write_file(path, content):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    print(f"✅ 写入: {path}")

# 创建 ClientMusicBlocker.java 文件
blocker_class = f'''package com.littlh.bloodline;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.lang.reflect.Field;

@EventBusSubscriber(modid = "{MOD_ID}", value = Dist.CLIENT)
public class ClientMusicBlocker {{

    private static boolean isInitialized = false;
    private static SoundEngine soundEngine;
    private static long lastCheckTime = 0;
    private static final long CHECK_INTERVAL_MS = 100; // 每100毫秒检查一次

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {{
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {{
            return;
        }}

        // 只在苍白摇篮维度生效
        if (!minecraft.player.level().dimension().location().toString().equals("{MOD_ID}:pale_cradle")) {{
            isInitialized = false;
            soundEngine = null;
            return;
        }}

        // 获取 SoundEngine
        if (soundEngine == null) {{
            try {{
                Field field = minecraft.getSoundManager().getClass().getDeclaredField("soundEngine");
                field.setAccessible(true);
                soundEngine = (SoundEngine) field.get(minecraft.getSoundManager());
                System.out.println("[Bloodline] Successfully hooked into SoundEngine");
            }} catch (Exception e) {{
                System.err.println("[Bloodline] Failed to get SoundEngine: " + e.getMessage());
                return;
            }}
        }}

        // 限制检查频率，降低性能开销
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCheckTime < CHECK_INTERVAL_MS) {{
            return;
        }}
        lastCheckTime = currentTime;

        // 拦截并停止原版音乐
        try {{
            Field field = soundEngine.getClass().getDeclaredField("instanceBySource");
            field.setAccessible(true);
            Object instanceBySource = field.get(soundEngine);
            
            if (instanceBySource instanceof java.util.Map) {{
                java.util.Map<?, ?> map = (java.util.Map<?, ?>) instanceBySource;
                java.util.List<SoundInstance> toStop = new java.util.ArrayList<>();

                for (Object value : map.values()) {{
                    if (value instanceof SoundInstance) {{
                        SoundInstance instance = (SoundInstance) value;
                        ResourceLocation loc = instance.getLocation();
                        
                        // 只处理音乐 (SoundSource.MUSIC)
                        if (instance.getSource() == net.minecraft.sounds.SoundSource.MUSIC) {{
                            String namespace = loc.getNamespace();
                            // 如果命名空间不是 bloodline，说明是原版或其他模组的音乐
                            if (!namespace.equals("{MOD_ID}")) {{
                                toStop.add(instance);
                            }}
                        }}
                    }}
                }}

                // 停止所有收集到的非模组音乐
                if (!toStop.isEmpty()) {{
                    for (SoundInstance instance : toStop) {{
                        soundEngine.stop(instance);
                        System.out.println("[Bloodline] Blocked vanilla music: " + instance.getLocation());
                    }}
                }}
            }}
        }} catch (Exception e) {{
            System.err.println("[Bloodline] Error blocking vanilla music: " + e.getMessage());
        }}
    }}
}}
'''

write_file(f"{PACKAGE_PATH}/ClientMusicBlocker.java", blocker_class)

print("\n🎉 ClientMusicBlocker.java 已创建！")
print("📌 现在运行 ./gradlew build 重新构建模组，进入维度测试。")
print("📌 如果原版音乐依然播放，可以尝试方案二 (Mixin)。")