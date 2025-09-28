package dev.hilligans.arsonist.network;

import dev.hilligans.arsonist.ArsonistServer;
import dev.hilligans.engine.network.engine.NetworkEntity;
import dev.hilligans.engine.network.engine.ServerNetworkEntity;
import dev.hilligans.engine.network.packet.ClientToServerPacketType;
import dev.hilligans.engine.util.IByteArray;
import dev.hilligans.engine2d.world.PlayerEntity;

public class CUpdatePlayer extends ClientToServerPacketType<ArsonistServer> {

    public static final CUpdatePlayer instance = new CUpdatePlayer();


    public static void encode(NetworkEntity entity, PlayerEntity playerEntity) {
        IByteArray array = instance.getWriteArray(entity);

        array.writeLong(playerEntity.id);
        playerEntity.encode(array);
        array.writeFloat(playerEntity.health);
        array.writeBoolean(playerEntity.alive);
        array.writeFloat(playerEntity.rot);
        array.writeBoolean(playerEntity.firing);

        entity.sendPacket(array);
    }

    @Override
    public void decode(ServerNetworkEntity<ArsonistServer> entity, IByteArray data) {
        long ID = data.readLong();

        float x = data.readFloat();
        float y = data.readFloat();
        float z = data.readFloat();

        float health = data.readFloat();
        boolean visible = data.readBoolean();
        float rot = data.readFloat();
        boolean firing = data.readBoolean();

        PlayerEntity player = entity.getServer().players.computeIfAbsent(ID, (_) -> {
            PlayerEntity playerEntity = new PlayerEntity(null, null, ID, entity.getGameInstance());
            return playerEntity;
        });
        player.setPosition(x, y, z);
        player.health = health;
        player.alive = visible;
        player.rot = rot;
        player.firing = firing;
        entity.getServer().send(SUpdatePlayer.encode(entity, player));
    }
}
