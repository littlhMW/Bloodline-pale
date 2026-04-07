import os
import shutil

MOD_ID = "bloodline"
DIM_NAME = "pale_cradle"
DATA_DIR = f"src/main/resources/data/{MOD_ID}"

# 是否删除现有 worldgen（强烈建议）
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

clean_old()

# ---------- 1. 维度类型 ----------
write_file(f"{DATA_DIR}/dimension_type/{DIM_NAME}.json", f"""{{
  "ultrawarm": false,
  "natural": true,
  "coordinate_scale": 1.0,
  "has_skylight": true,
  "has_ceiling": false,
  "ambient_light": 0.5,
  "monster_spawn_light_level": {{ "type": "minecraft:uniform", "min_inclusive": 0, "max_inclusive": 7 }},
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
}}""")

# ---------- 2. 噪声设置 ----------
write_file(f"{DATA_DIR}/worldgen/noise_settings/{DIM_NAME}.json", f"""{{
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
}}""")

# ---------- 3. 维度入口（棋盘格，四个群系交替）----------
write_file(f"{DATA_DIR}/dimension/{DIM_NAME}.json", f"""{{
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
}}""")

# ---------- 4. 群系定义（features 全部为空）----------
def biome_json(name, grass_color, foliage_color):
    return f"""{{
  "has_precipitation": false,
  "temperature": 0.5,
  "downfall": 0.0,
  "effects": {{
    "sky_color": 11184810,
    "fog_color": 11184810,
    "water_color": 11184810,
    "water_fog_color": 11184810,
    "grass_color": {grass_color},
    "foliage_color": {foliage_color},
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
  "carvers": {{ "air": [], "liquid": [] }},
  "features": [[], [], [], [], [], [], [], [], [], [], []]
}}"""

write_file(f"{DATA_DIR}/worldgen/biome/pale_wheat_fields.json", biome_json("pale_wheat_fields", 8947848, 8947848))
write_file(f"{DATA_DIR}/worldgen/biome/sparse_wheat_plains.json", biome_json("sparse_wheat_plains", 8947848, 8947848))
write_file(f"{DATA_DIR}/worldgen/biome/bone_spike_forest.json", biome_json("bone_spike_forest", 10526880, 10526880))
write_file(f"{DATA_DIR}/worldgen/biome/withered_garden.json", biome_json("withered_garden", 6579300, 6579300))

print("\n✅ 最简 worldgen 生成完成！")
print("📌 现在可以安全进入维度，四个群系将交替出现，地表只有苍白草方块。")
print("📌 确认稳定后，我们再逐步添加地物。")