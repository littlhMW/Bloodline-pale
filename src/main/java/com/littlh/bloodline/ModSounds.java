package com.littlh.bloodline;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, "bloodline");

    private static final Map<String, Supplier<SoundEvent>> REGISTERED_SOUNDS = new HashMap<>();
    private static final List<String> MUSIC_TRACK_NAMES = new ArrayList<>();

    public static void register(IEventBus modEventBus) {
        loadMusicTracksFromJson();
        SOUND_EVENTS.register(modEventBus);
    }

    private static void loadMusicTracksFromJson() {
        try {
            try (Reader reader = new InputStreamReader(
                    ModSounds.class.getResourceAsStream("/assets/bloodline/music_tracks.json"))) {
                JsonArray array = JsonParser.parseReader(reader).getAsJsonArray();
                for (JsonElement elem : array) {
                    String name = elem.getAsString();
                    MUSIC_TRACK_NAMES.add(name);
                    String eventId = "pale_cradle_music." + name;
                    Supplier<SoundEvent> supplier = SOUND_EVENTS.register(eventId,
                            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.parse("bloodline:" + eventId)));
                    REGISTERED_SOUNDS.put(name, supplier);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load music_tracks.json", e);
        }
    }

    public static List<ResourceLocation> getMusicTrackLocations() {
        List<ResourceLocation> locations = new ArrayList<>();
        for (String name : MUSIC_TRACK_NAMES) {
            locations.add(ResourceLocation.parse("bloodline:pale_cradle_music." + name));
        }
        return locations;
    }

    public static SoundEvent getSoundEventByName(String name) {
        Supplier<SoundEvent> supplier = REGISTERED_SOUNDS.get(name);
        return supplier != null ? supplier.get() : null;
    }
}
