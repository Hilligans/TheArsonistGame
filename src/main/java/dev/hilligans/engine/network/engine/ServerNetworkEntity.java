package dev.hilligans.engine.network.engine;

import dev.hilligans.arsonist.ServerPlayerData;
import dev.hilligans.engine.application.IServerApplication;

public interface ServerNetworkEntity<T extends IServerApplication> extends NetworkEntity {


    /**
     * @return the player data belonging to this player
     * @throws IllegalStateException - if the player data hasn't been loaded yet in the networking sequence
     */
    ServerPlayerData getServerPlayerData();

    void setServerPlayerData(ServerPlayerData data);

    T getServer();
}
