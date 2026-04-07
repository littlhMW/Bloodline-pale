package com.littlh.bloodline;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod("bloodline")
public class Bloodline {
    public Bloodline(IEventBus modEventBus) {
        BloodlineBlocks.BLOCKS.register(modEventBus);
        BloodlineItems.ITEMS.register(modEventBus);
    }
}
