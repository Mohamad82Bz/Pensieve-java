package me.mohamad82.pensieve.replaying;

import me.mohamad82.pensieve.nms.hologram.Hologram;

public class ReplaySettings {

    private final Hologram hologram;

    private ReplaySettings(Hologram hologram) {
        this.hologram = hologram;
    }

    public static class Builder {

        private Hologram hologram = null;

        public Builder withHologram(Hologram hologram) {
            this.hologram = hologram;
            return this;
        }

        public ReplaySettings build() {
            return new ReplaySettings(hologram);
        }

    }

}
