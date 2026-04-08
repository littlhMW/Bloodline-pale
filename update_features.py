import os
import json
import shutil

MOD_ID = "bloodline"
DATA_DIR = f"src/main/resources/data/{MOD_ID}"

# 是否删除现有 worldgen 文件夹（强烈推荐）
DELETE_OLD = True

def write_file(path, content):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    print(f"✅ 写入: {path}")

def clean_old():
    worldgen_path = os.path.join(DATA_DIR, "worldgen")
    if DELETE_OLD and os.path.exists(worldgen_path):
        shutil.rmtree(worldgen_path)
        print(f"🗑️ 已删除旧 worldgen: {worldgen_path}")

def update_biome_features(biome_name, feature_id):
    biome_path = f"{DATA_DIR}/worldgen/biome/{biome_name}.json"
    try:
        with open(biome_path, "r", encoding="utf-8") as f:
            data = json.load(f)
    except FileNotFoundError:
        print(f"⚠️ 未找到群系文件 {biome_path}，请确保已运行过维度生成脚本")
        return

    if "features" not in data:
        data["features"] = [[], [], [], [], [], [], [], [], [], [], []]
    while len(data["features"]) < 11:
        data["features"].append([])

    data["features"][2] = [feature_id]

    with open(biome_path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)
    print(f"🔧 已更新 {biome_name} 的地物为 {feature_id}")

clean_old()

# ---------- 1. 苍白麦原：原版小麦（密集）----------
# 直接使用原版 placed feature ID，无需自定义 configured_feature
update_biome_features("pale_wheat_fields", "minecraft:wheat")

# 但我们不能直接修改原版小麦的放置次数，因此需要创建一个自定义 placed feature 来增加密度
# 这里我们创建一个 placed feature，它引用原版小麦的 configured_feature，并应用高 count
# 首先，确保原版小麦的 configured_feature 存在（它是 minecraft:wheat）
# 然后创建我们的 placed feature
write_file(f"{DATA_DIR}/worldgen/placed_feature/dense_wheat.json", """{
  "feature": "minecraft:wheat",
  "placement": [
    { "type": "minecraft:count", "count": 200 },
    { "type": "minecraft:in_square" },
    { "type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG" }
  ]
}""")

# 更新苍白麦原群系，使用我们自定义的高密度放置
update_biome_features("pale_wheat_fields", f"{MOD_ID}:dense_wheat")

# ---------- 2. 稀疏麦原：原版小麦（稀疏）----------
write_file(f"{DATA_DIR}/worldgen/placed_feature/sparse_wheat.json", """{
  "feature": "minecraft:wheat",
  "placement": [
    { "type": "minecraft:count", "count": 5 },
    { "type": "minecraft:in_square" },
    { "type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG" }
  ]
}""")
update_biome_features("sparse_wheat_plains", f"{MOD_ID}:sparse_wheat")

# ---------- 3. 骨刺森林：原版玄武岩柱 ----------
# 原版玄武岩柱的 configured_feature ID 是 minecraft:basalt_pillar
write_file(f"{DATA_DIR}/worldgen/placed_feature/bone_spike.json", """{
  "feature": "minecraft:basalt_pillar",
  "placement": [
    { "type": "minecraft:count", "count": 8 },
    { "type": "minecraft:in_square" },
    { "type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG" }
  ]
}""")
update_biome_features("bone_spike_forest", f"{MOD_ID}:bone_spike")

# ---------- 4. 枯萎花园：原版凋零玫瑰 ----------
# 凋零玫瑰是简单方块，原版有对应的 placed feature: minecraft:wither_rose
# 但原版的放置次数可能不够，我们同样创建一个自定义 placed feature 增加密度
write_file(f"{DATA_DIR}/worldgen/placed_feature/withered_bush.json", """{
  "feature": "minecraft:wither_rose",
  "placement": [
    { "type": "minecraft:count", "count": 12 },
    { "type": "minecraft:in_square" },
    { "type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG" }
  ]
}""")
update_biome_features("withered_garden", f"{MOD_ID}:withered_bush")

print("\n🎉 终极稳定配置生成完毕！")
print("=" * 50)
print("📌 后续步骤：")
print("1. 运行 ./gradlew build 重新构建模组")
print("2. 删除旧世界，创建新世界进入维度")
print("3. 您将看到极其密集的黄色小麦、黑色玄武岩柱和凋零玫瑰")
print("4. 使用资源包将小麦和玄武岩纹理替换为灰色即可完美呈现“苍白摇篮”")
print("=" * 50)
print("📁 资源包替换指南：")
print("- 替换 assets/minecraft/textures/block/wheat_stage7.png 为您的灰色小麦纹理")
print("- 替换 assets/minecraft/textures/block/basalt_top.png 和 basalt_side.png 为骨刺纹理")
print("- 凋零玫瑰本身是暗色，可保留或替换")
print("=" * 50)