import os
import json

MOD_ID = "bloodline"
DIM_NAME = "pale_cradle"
DATA_DIR = f"src/main/resources/data/{MOD_ID}"
ASSETS_DIR = f"src/main/resources/assets/{MOD_ID}"

def write_file(path, content):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    print(f"✅ 写入: {path}")

def write_json(path, data):
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)

# ========== 1. 修复血泊群系：移除 lake_lava，保持红色水体 ==========
biome_blood_path = f"{DATA_DIR}/worldgen/biome/blood_poole.json"
if os.path.exists(biome_blood_path):
    with open(biome_blood_path, "r", encoding="utf-8") as f:
        blood_data = json.load(f)
    # 确保 features 数组长度足够，并清空索引 2（原放置熔岩湖的位置）
    if "features" not in blood_data:
        blood_data["features"] = [[] for _ in range(11)]
    while len(blood_data["features"]) < 11:
        blood_data["features"].append([])
    # 清空可能引起崩溃的索引 2
    blood_data["features"][2] = []
    # 在植被阶段添加稀疏小麦（索引 9）
    blood_data["features"][9] = [f"{MOD_ID}:sparse_wheat_patch"]
    write_json(biome_blood_path, blood_data)
    print("🩸 血泊群系已修正：移除 lake_lava，仅保留红色水体 + 稀疏小麦")
else:
    print("⚠️ 未找到血泊群系文件，请先运行 create_blood_poole_and_biome_distribution.py")

# ========== 2. 确认维度群系分布为您挑选的参数 ==========
dimension_path = f"{DATA_DIR}/dimension/{DIM_NAME}.json"
with open(dimension_path, "r", encoding="utf-8") as f:
    dim_data = json.load(f)

dim_data["generator"]["biome_source"] = {
    "type": "minecraft:multi_noise",
    "biomes": [
        {
            "comment": "苍白麦原：大面积基底",
            "biome": f"{MOD_ID}:pale_wheat_fields",
            "parameters": {
                "temperature": 0,
                "humidity": 0,
                "continentalness": [0.3, 1.0],
                "erosion": [-0.3, 0.3],
                "weirdness": [-0.2, 0.2],
                "depth": 0,
                "offset": 0
            }
        },
        {
            "comment": "稀疏麦原：包围在麦原周围",
            "biome": f"{MOD_ID}:sparse_wheat_plains",
            "parameters": {
                "temperature": 0,
                "humidity": 0,
                "continentalness": [0.1, 0.3],
                "erosion": [-0.3, 0.3],
                "weirdness": [-0.2, 0.2],
                "depth": 0,
                "offset": 0
            }
        },
        {
            "comment": "骨刺森林：条状分布",
            "biome": f"{MOD_ID}:bone_spike_forest",
            "parameters": {
                "temperature": 0,
                "humidity": [-0.5, 0.2],
                "continentalness": [-0.2, 0.5],
                "erosion": 0,
                "weirdness": [0.5, 0.8],
                "depth": 0,
                "offset": 0
            }
        },
        {
            "comment": "血泊：嵌套在骨刺森林中/外围",
            "biome": f"{MOD_ID}:blood_poole",
            "parameters": {
                "temperature": 0,
                "humidity": [0.4, 1.0],
                "continentalness": [-0.2, 0.5],
                "erosion": 0,
                "weirdness": [0.6, 0.75],
                "depth": 0,
                "offset": 0.1
            }
        },
        {
            "comment": "枯萎花园：罕见大块区域",
            "biome": f"{MOD_ID}:withered_garden",
            "parameters": {
                "temperature": 0,
                "humidity": 0,
                "continentalness": [-1.0, -0.5],
                "erosion": [0.6, 1.0],
                "weirdness": 0,
                "depth": 0,
                "offset": 0.2
            }
        }
    ]
}

write_json(dimension_path, dim_data)
print("🌍 群系分布已确认为您挑选的参数")

print("\n🎉 修复完成！")
print("📌 血泊群系不再引用 lake_lava，仅依靠群系水体颜色呈现血红色。")
print("📌 请执行 ./gradlew build，删除旧世界后重新测试。")
