import os

MOD_ID = "bloodline"
PACKAGE = "com.littlh.bloodline"
PACKAGE_PATH = PACKAGE.replace(".", "/")
SRC_JAVA = f"src/main/java/{PACKAGE_PATH}"
SRC_RES = "src/main/resources"
DATA = f"{SRC_RES}/data/{MOD_ID}"
ASSETS = f"{SRC_RES}/assets/{MOD_ID}"
DIMENSION_NAME = "pale_cradle"

def write_file(path, content):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    print(f"✅ 已创建/覆盖: {path}")

# ---------- 维度类型 ----------
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
write_file(f"{DATA}/dimension_type/{DIMENSION_NAME}.json", dim_type)

# ---------- 噪声设置 ----------
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
write_file(f"{DATA}/worldgen/noise_settings/{DIMENSION_NAME}.json", noise_settings)

# ---------- 维度入口 ----------
dimension = f"""{{
  "type": "{MOD_ID}:{DIMENSION_NAME}",
  "generator": {{
    "type": "minecraft:noise",
    "settings": "{MOD_ID}:{DIMENSION_NAME}",
    "biome_source": {{ "type": "minecraft:multi_noise", "parameters": "{MOD_ID}:{DIMENSION_NAME}_biomes" }}
  }}
}}
"""
write_file(f"{DATA}/dimension/{DIMENSION_NAME}.json", dimension)

# ---------- 群系参数 ----------
multi_noise = f"""{{
  "preset": "minecraft:overworld",
  "biomes": [
    {{ "biome": "{MOD_ID}:pale_wheat_fields", "parameters": {{ "temperature": 0.5, "humidity": 0.5, "continentalness": 0.5, "erosion": 0.5, "weirdness": 0.0, "depth": 0.0, "offset": 0.0 }} }},
    {{ "biome": "{MOD_ID}:sparse_wheat_plains", "parameters": {{ "temperature": 0.5, "humidity": 0.3, "continentalness": 0.5, "erosion": 0.5, "weirdness": 0.2, "depth": 0.0, "offset": 0.0 }} }},
    {{ "biome": "{MOD_ID}:pale_wheat_fields", "parameters": {{ "temperature": 0.5, "humidity": 0.5, "continentalness": 0.5, "erosion": 0.5, "weirdness": 0.5, "depth": 0.0, "offset": 0.0 }} }},
    {{ "biome": "{MOD_ID}:sparse_wheat_plains", "parameters": {{ "temperature": 0.5, "humidity": 0.3, "continentalness": 0.5, "erosion": 0.5, "weirdness": -0.2, "depth": 0.0, "offset": 0.0 }} }}
  ]
}}
"""
write_file(f"{DATA}/worldgen/multi_noise_biome_source_parameter_list/{DIMENSION_NAME}_biomes.json", multi_noise)

# ---------- 群系定义 ----------
biome_template = """{{
  "has_precipitation": false,
  "temperature": 0.5,
  "downfall": 0.0,
  "effects": {{
    "sky_color": {sky_color},
    "fog_color": {fog_color},
    "water_color": {water_color},
    "water_fog_color": {water_fog_color},
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
  "carvers": [],
  "features": [
    [],
    [],
    {features},
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

pale_features = f'["{MOD_ID}:pale_wheat_patch"]'
sparse_features = f'["{MOD_ID}:sparse_wheat_patch"]'

biome_pale = biome_template.format(
    sky_color=0xAAAAAA, fog_color=0xAAAAAA,
    water_color=0xAAAAAA, water_fog_color=0xAAAAAA,
    grass_color=0x888888, foliage_color=0x888888,
    features=pale_features
)
biome_sparse = biome_template.format(
    sky_color=0xAAAAAA, fog_color=0xAAAAAA,
    water_color=0xAAAAAA, water_fog_color=0xAAAAAA,
    grass_color=0x888888, foliage_color=0x888888,
    features=sparse_features
)
write_file(f"{DATA}/worldgen/biome/pale_wheat_fields.json", biome_pale)
write_file(f"{DATA}/worldgen/biome/sparse_wheat_plains.json", biome_sparse)

# ---------- 地物配置（修正：单株小麦的 placed_feature + configured_feature）----------
# 1. 单株小麦 ConfiguredFeature
single_wheat_cfg = f"""{{
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
"""
write_file(f"{DATA}/worldgen/configured_feature/pale_wheat_single.json", single_wheat_cfg)

# 2. 单株小麦 PlacedFeature（直接放置在表面，但不需要额外放置条件，因为会被 patch 使用）
single_wheat_placed = f"""{{
  "feature": "{MOD_ID}:pale_wheat_single",
  "placement": []
}}
"""
write_file(f"{DATA}/worldgen/placed_feature/pale_wheat_single.json", single_wheat_placed)

# 3. 密集小麦 patch ConfiguredFeature（引用 placed_feature ID）
patch_dense_cfg = f"""{{
  "type": "minecraft:random_patch",
  "config": {{
    "tries": 64,
    "xz_spread": 7,
    "y_spread": 3,
    "feature": "{MOD_ID}:pale_wheat_single"
  }}
}}
"""
write_file(f"{DATA}/worldgen/configured_feature/pale_wheat_patch.json", patch_dense_cfg)

# 4. 稀疏小麦 patch ConfiguredFeature
patch_sparse_cfg = f"""{{
  "type": "minecraft:random_patch",
  "config": {{
    "tries": 32,
    "xz_spread": 7,
    "y_spread": 3,
    "feature": "{MOD_ID}:pale_wheat_single"
  }}
}}
"""
write_file(f"{DATA}/worldgen/configured_feature/sparse_wheat_patch.json", patch_sparse_cfg)

# 5. 密集小麦 PlacedFeature
placed_dense = f"""{{
  "feature": "{MOD_ID}:pale_wheat_patch",
  "placement": [
    {{ "type": "minecraft:count", "count": 10 }},
    {{ "type": "minecraft:in_square" }},
    {{ "type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG" }},
    {{ "type": "minecraft:biome" }}
  ]
}}
"""
write_file(f"{DATA}/worldgen/placed_feature/pale_wheat_patch.json", placed_dense)

# 6. 稀疏小麦 PlacedFeature
placed_sparse = f"""{{
  "feature": "{MOD_ID}:sparse_wheat_patch",
  "placement": [
    {{ "type": "minecraft:count", "count": 4 }},
    {{ "type": "minecraft:in_square" }},
    {{ "type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG" }},
    {{ "type": "minecraft:biome" }}
  ]
}}
"""
write_file(f"{DATA}/worldgen/placed_feature/sparse_wheat_patch.json", placed_sparse)

# ---------- 资源文件（模型、语言等）----------
write_file(f"{ASSETS}/blockstates/pale_grass_block.json", '{"variants": {"": {"model": "bloodline:block/pale_grass_block"}}}')
write_file(f"{ASSETS}/blockstates/pale_wheat.json", '{"variants": {"age=0": {"model": "bloodline:block/pale_wheat_stage0"}, "age=1": {"model": "bloodline:block/pale_wheat_stage1"}, "age=2": {"model": "bloodline:block/pale_wheat_stage2"}, "age=3": {"model": "bloodline:block/pale_wheat_stage3"}, "age=4": {"model": "bloodline:block/pale_wheat_stage4"}, "age=5": {"model": "bloodline:block/pale_wheat_stage5"}, "age=6": {"model": "bloodline:block/pale_wheat_stage6"}, "age=7": {"model": "bloodline:block/pale_wheat_stage7"}}}')
write_file(f"{ASSETS}/models/block/pale_grass_block.json", '{"parent": "minecraft:block/cube_bottom_top","textures": {"bottom": "bloodline:block/pale_grass_block_bottom","top": "bloodline:block/pale_grass_block_top","side": "bloodline:block/pale_grass_block_side"}}')
write_file(f"{ASSETS}/models/item/pale_grass_block.json", '{"parent": "bloodline:block/pale_grass_block"}')
write_file(f"{ASSETS}/models/item/pale_wheat_seeds.json", '{"parent": "minecraft:item/generated","textures": {"layer0": "bloodline:item/pale_wheat_seeds"}}')
for i in range(8):
    write_file(f"{ASSETS}/models/block/pale_wheat_stage{i}.json", f'{{"parent": "minecraft:block/crop","textures": {{"crop": "bloodline:block/pale_wheat_stage{i}"}}}}')
write_file(f"{ASSETS}/lang/en_us.json", f'''{{
  "block.{MOD_ID}.pale_grass_block": "Pale Grass Block",
  "block.{MOD_ID}.pale_wheat": "Pale Wheat",
  "item.{MOD_ID}.pale_wheat_seeds": "Pale Wheat Seeds"
}}''')
write_file(f"{ASSETS}/textures/block/README.txt", "请将纹理 PNG 文件放入此目录。")
write_file(f"{ASSETS}/textures/item/README.txt", "请将纹理 PNG 文件放入此目录。")

print("\n✅ 所有数据文件已修复！现在可以正常创建世界并进入维度。")
print("📌 下一步：添加纹理图片，然后使用命令 /execute in bloodline:pale_cradle run tp @s ~ ~ ~")