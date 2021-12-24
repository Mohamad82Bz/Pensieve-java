package me.mohamad82.pensieve.replaying;

import me.mohamad82.pensieve.nms.hologram.HologramOld;

public class ReplaySettings {

    private final HologramOld hologram;

    private ReplaySettings(HologramOld hologram) {
        this.hologram = hologram;
    }

    public static class Builder {

        private HologramOld hologram = null;

        public Builder withHologram(HologramOld hologram) {
            this.hologram = hologram;
            return this;
        }

        public ReplaySettings build() {
            return new ReplaySettings(hologram);
        }

    }

}
