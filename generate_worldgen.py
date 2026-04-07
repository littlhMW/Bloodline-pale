import os
import shutil

# ================= 配置区 =================
MOD_ID = "bloodline"
DIM_NAME = "pale_cradle"
DATA_DIR = f"src/main/resources/data/{MOD_ID}"

# 是否先删除现有的 worldgen 文件夹（推荐）
DELETE_OLD = True

# ================= 辅助函数 =================
def write_file(path, content):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    print(f"✅ 写入: {path}")

def clean_old():
    worldgen_path = os.path.join(DATA_DIR, "worldgen")
    if DELETE_OLD and os.path.exists(worldgen_path):
        shutil.rmtree(worldgen_path)
        print(f"🗑️ 已删除旧 worldgen 文件夹: {worldgen_path}")

# ================= 开始生成 =================
clean_old()

# ---------- 1. 维度类型 ----------
dim_type = f"""{{
  "ultrawarm": false,
  "natural": true,
  "coordinate_scale": 1.0,
  "has_skylight": true,
  "has_ceiling": false,
  "ambient_light": 0.5,
  "monster_spawn_light_level": {{
    "type": "minecraft:uniform",
    "min_inclusive": 0,
    "max_inclusive": 7
  }},
  "monster_spawn_block_light_limit": 0,
  "fixed_time": 6000,
  "has_raids": false,
  "piglin_safe": false,
  "respawn_anchor_works": false,
  "bed_works": true,
  "infiniburn": "#minecraft:infiniburn_overworld",
  "effects": "minecraft:overworld",
  "logical_height": 384,
  "height": 384,
  "min_y": -64
}}
"""
write_file(f"{DATA_DIR}/dimension_type/{DIM_NAME}.json", dim_type)

# ---------- 2. 噪声设置（平坦，无洞穴）----------
noise_settings = f"""{{
  "sea_level": -64,
  "disable_mob_generation": false,
  "aquifers_enabled": false,
  "ore_veins_enabled": false,
  "legacy_random_source": false,
  "default_block": {{ "Name": "{MOD_ID}:pale_grass_block" }},
  "default_fluid": {{ "Name": "minecraft:air" }},
  "noise": {{ "min_y": -64, "height": 384, "size_horizontal": 1, "size_vertical": 2 }},
  "noise_router": {{
    "barrier": 0, "fluid_level_floodedness": 0, "fluid_level_spread": 0, "lava": 0,
    "temperature": 0, "vegetation": 0, "continents": 0, "erosion": 0, "depth": 0, "ridges": 0,
    "initial_density_without_jaggedness": {{
      "type": "minecraft:add",
      "argument1": {{ "type": "minecraft:y_clamped_gradient", "from_value": 1, "from_y": -64, "to_value": -1, "to_y": 320 }},
      "argument2": {{ "type": "minecraft:mul", "argument1": 0.1, "argument2": {{ "type": "minecraft:noise", "noise": "minecraft:surface", "xz_scale": 0.2, "y_scale": 0.2 }} }}
    }},
    "final_density": {{
      "type": "minecraft:add",
      "argument1": {{ "type": "minecraft:y_clamped_gradient", "from_value": 1, "from_y": -64, "to_value": -1, "to_y": 320 }},
      "argument2": {{ "type": "minecraft:mul", "argument1": 0.1, "argument2": {{ "type": "minecraft:noise", "noise": "minecraft:surface", "xz_scale": 0.2, "y_scale": 0.2 }} }}
    }},
    "vein_toggle": 0, "vein_ridged": 0, "vein_gap": 0
  }},
  "spawn_target": [],
  "surface_rule": {{
    "type": "minecraft:sequence",
    "sequence": [
      {{
        "type": "minecraft:condition",
        "if_true": {{ "type": "minecraft:vertical_gradient", "random_name": "minecraft:bedrock_floor", "true_at_and_below": {{ "above_bottom": 0 }}, "false_at_and_above": {{ "above_bottom": 5 }} }},
        "then_run": {{ "type": "minecraft:block", "result_state": {{ "Name": "minecraft:bedrock" }} }}
      }},
      {{
        "type": "minecraft:condition",
        "if_true": {{ "type": "minecraft:above_preliminary_surface" }},
        "then_run": {{
          "type": "minecraft:sequence",
          "sequence": [
            {{ "type": "minecraft:condition", "if_true": {{ "type": "minecraft:stone_depth", "add_surface_depth": false, "offset": 0, "secondary_depth_range": 0, "surface_type": "floor" }}, "then_run": {{ "type": "minecraft:block", "result_state": {{ "Name": "{MOD_ID}:pale_grass_block" }} }} }},
            {{ "type": "minecraft:condition", "if_true": {{ "type": "minecraft:stone_depth", "add_surface_depth": true, "offset": 0, "secondary_depth_range": 3, "surface_type": "floor" }}, "then_run": {{ "type": "minecraft:block", "result_state": {{ "Name": "minecraft:dirt" }} }} }},
            {{ "type": "minecraft:block", "result_state": {{ "Name": "minecraft:stone" }} }}
          ]
        }}
      }}
    ]
  }}
}}
"""
write_file(f"{DATA_DIR}/worldgen/noise_settings/{DIM_NAME}.json", noise_settings)

# ---------- 3. 维度入口（使用棋盘格源，便于测试所有群系）----------
dimension = f"""{{
  "type": "{MOD_ID}:{DIM_NAME}",
  "generator": {{
    "type": "minecraft:noise",
    "settings": "{MOD_ID}:{DIM_NAME}",
    "biome_source": {{
      "type": "minecraft:checkerboard",
      "biomes": [
        "{MOD_ID}:pale_wheat_fields",
        "{MOD_ID}:sparse_wheat_plains",
        "{MOD_ID}:bone_spike_forest",
        "{MOD_ID}:withered_garden"
      ],
      "scale": 3
    }}
  }}
}}
"""
write_file(f"{DATA_DIR}/dimension/{DIM_NAME}.json", dimension)

# ---------- 4. 群系定义（四个）----------
def write_biome(name, color_grass, color_foliage, feature_list):
    biome = f"""{{
  "has_precipitation": false,
  "temperature": 0.5,
  "downfall": 0.0,
  "effects": {{
    "sky_color": 11184810,
    "fog_color": 11184810,
    "water_color": 11184810,
    "water_fog_color": 11184810,
    "grass_color": {color_grass},
    "foliage_color": {color_foliage},
    "mood_sound": {{
      "sound": "minecraft:ambient.cave",
      "tick_delay": 6000,
      "block_search_extent": 8,
      "offset": 2.0
    }}
  }},
  "spawners": {{
    "monster": [], "creature": [], "ambient": [], "axolotls": [],
    "underground_water_creature": [], "water_creature": [], "water_ambient": [], "misc": []
  }},
  "spawn_costs": {{}},
  "carvers": {{
    "air": [],
    "liquid": []
  }},
  "features": [
    [],
    [],
    {feature_list},
    [],
    [],
    [],
    [],
    [],
    [],
    [],
    []
  ]
}}"""
    write_file(f"{DATA_DIR}/worldgen/biome/{name}.json", biome)

write_biome("pale_wheat_fields", 8947848, 8947848, f'["{MOD_ID}:pale_wheat_patch"]')
write_biome("sparse_wheat_plains", 8947848, 8947848, f'["{MOD_ID}:sparse_wheat_patch"]')
write_biome("bone_spike_forest", 10526880, 10526880, f'["{MOD_ID}:bone_spike"]')
write_biome("withered_garden", 6579300, 6579300, f'["{MOD_ID}:withered_bush"]')

# ---------- 5. 地物配置 ----------
# 5.1 苍白小麦 - 密集 patch
pale_wheat_dense_cfg = f"""{{
  "type": "minecraft:random_patch",
  "config": {{
    "tries": 64,
    "xz_spread": 7,
    "y_spread": 3,
    "feature": {{
      "type": "minecraft:simple_block",
      "config": {{
        "to_place": {{
          "type": "minecraft:simple_state_provider",
          "state": {{
            "Name": "{MOD_ID}:pale_wheat",
            "Properties": {{ "age": "7" }}
          }}
        }}
      }}
    }}
  }}
}}
"""
write_file(f"{DATA_DIR}/worldgen/configured_feature/pale_wheat_patch.json", pale_wheat_dense_cfg)

pale_wheat_dense_placed = f"""{{
  "feature": "{MOD_ID}:pale_wheat_patch",
  "placement": [
    {{ "type": "minecraft:count", "count": 10 }},
    {{ "type": "minecraft:in_square" }},
    {{ "type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG" }}
  ]
}}
"""
write_file(f"{DATA_DIR}/worldgen/placed_feature/pale_wheat_patch.json", pale_wheat_dense_placed)

# 5.2 苍白小麦 - 稀疏 patch
pale_wheat_sparse_cfg = f"""{{
  "type": "minecraft:random_patch",
  "config": {{
    "tries": 32,
    "xz_spread": 7,
    "y_spread": 3,
    "feature": {{
      "type": "minecraft:simple_block",
      "config": {{
        "to_place": {{
          "type": "minecraft:simple_state_provider",
          "state": {{
            "Name": "{MOD_ID}:pale_wheat",
            "Properties": {{ "age": "7" }}
          }}
        }}
      }}
    }}
  }}
}}
"""
write_file(f"{DATA_DIR}/worldgen/configured_feature/sparse_wheat_patch.json", pale_wheat_sparse_cfg)

pale_wheat_sparse_placed = f"""{{
  "feature": "{MOD_ID}:sparse_wheat_patch",
  "placement": [
    {{ "type": "minecraft:count", "count": 4 }},
    {{ "type": "minecraft:in_square" }},
    {{ "type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG" }}
  ]
}}
"""
write_file(f"{DATA_DIR}/worldgen/placed_feature/sparse_wheat_patch.json", pale_wheat_sparse_placed)

# 5.3 骨刺 (使用 block_column)
bone_spike_cfg = f"""{{
  "type": "minecraft:block_column",
  "config": {{
    "direction": "up",
    "allowed_placement": {{
      "type": "minecraft:matching_blocks",
      "blocks": ["{MOD_ID}:pale_grass_block"]
    }},
    "prioritize_tip": true,
    "layers": [
      {{
        "height": 2,
        "provider": {{
          "type": "minecraft:simple_state_provider",
          "state": {{ "Name": "minecraft:bone_block", "Properties": {{ "axis": "y" }} }}
        }}
      }},
      {{
        "height": 1,
        "provider": {{
          "type": "minecraft:simple_state_provider",
          "state": {{ "Name": "minecraft:bone_block", "Properties": {{ "axis": "y" }} }}
        }}
      }}
    ]
  }}
}}
"""
write_file(f"{DATA_DIR}/worldgen/configured_feature/bone_spike.json", bone_spike_cfg)

bone_spike_placed = f"""{{
  "feature": "{MOD_ID}:bone_spike",
  "placement": [
    {{ "type": "minecraft:count", "count": 3 }},
    {{ "type": "minecraft:in_square" }},
    {{ "type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG" }}
  ]
}}
"""
write_file(f"{DATA_DIR}/worldgen/placed_feature/bone_spike.json", bone_spike_placed)

# 5.4 枯萎灌木 (使用死灌木占位)
withered_bush_cfg = f"""{{
  "type": "minecraft:simple_block",
  "config": {{
    "to_place": {{
      "type": "minecraft:simple_state_provider",
      "state": {{ "Name": "minecraft:dead_bush" }}
    }}
  }}
}}
"""
write_file(f"{DATA_DIR}/worldgen/configured_feature/withered_bush.json", withered_bush_cfg)

withered_bush_placed = f"""{{
  "feature": "{MOD_ID}:withered_bush",
  "placement": [
    {{ "type": "minecraft:count", "count": 8 }},
    {{ "type": "minecraft:in_square" }},
    {{ "type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG" }}
  ]
}}
"""
write_file(f"{DATA_DIR}/worldgen/placed_feature/withered_bush.json", withered_bush_placed)

# ---------- 可选：multi_noise 参数文件（备用）----------
multi_noise_params = f"""{{
  "preset": "minecraft:overworld",
  "biomes": [
    {{ "biome": "{MOD_ID}:pale_wheat_fields",   "parameters": [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0] }},
    {{ "biome": "{MOD_ID}:sparse_wheat_plains", "parameters": [0.0, 0.5, 0.0, 0.0, 0.0, 0.0, 0.0] }},
    {{ "biome": "{MOD_ID}:bone_spike_forest",   "parameters": [0.5, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0] }},
    {{ "biome": "{MOD_ID}:withered_garden",     "parameters": [0.5, 0.5, 0.0, 0.0, 0.0, 0.0, 0.0] }}
  ]
}}
"""
write_file(f"{DATA_DIR}/worldgen/multi_noise_biome_source_parameter_list/{DIM_NAME}_biomes.json", multi_noise_params)

print("\n🎉 worldgen 数据生成完成！")
print("📌 下一步：")
print("1. 重新构建模组: ./gradlew build")
print("2. 删除旧世界，创建新世界")
print("3. 进入维度后使用 F3 查看群系名称，地物应该可见（小麦缺纹理显示为紫黑块）")