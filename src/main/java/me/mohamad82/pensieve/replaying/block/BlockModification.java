package me.mohamad82.pensieve.replaying.block;

import org.bukkit.Location;

public interface BlockModification {

    Location getLocation();

    BlockModificationType getType();

    void apply();

    void rollback();

}
