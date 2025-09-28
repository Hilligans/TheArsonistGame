package dev.hilligans.arsonist.network;

import dev.hilligans.arsonist.Client2D;
import dev.hilligans.arsonist.entity.Flame;
import dev.hilligans.engine.entity.EntityType;
import dev.hilligans.engine.network.engine.ClientNetworkEntity;
import dev.hilligans.engine.network.engine.NetworkEntity;
import dev.hilligans.engine.network.packet.ServerToClientPacketType;
import dev.hilligans.engine.util.IByteArray;
import dev.hilligans.engine2d.client.sprite.Sprite;
import dev.hilligans.engine2d.world.PlayerEntity;

import java.util.List;

public class SUpdatePlayer extends ServerToClientPacketType<Client2D> {

    public static final SUpdatePlayer instance = new SUpdatePlayer();

    public static IByteArray encode(NetworkEntity entity, PlayerEntity playerEntity) {
        IByteArray array = instance.getWriteArray(entity);

        array.writeLong(playerEntity.id);
        playerEntity.encode(array);
        array.writeFloat(playerEntity.health);
        array.writeBoolean(playerEntity.alive);
        array.writeFloat(playerEntity.rot);
        array.writeBoolean(playerEntity.firing);

        return array;
    }


    @Override
    public void decode(ClientNetworkEntity<Client2D> entity, IByteArray data) {
        long ID = data.readLong();


        if(ID == entity.getClient().playerEntity.id) {
            return;
        }

        float x = data.readFloat();
        float y = data.readFloat();
        float z = data.readFloat();

        float health = data.readFloat();
        boolean visisble = data.readBoolean();
        float rot = data.readFloat();
        boolean firing = data.readBoolean();


        PlayerEntity player = entity.getClient().players.computeIfAbsent(ID, (_) -> {
            PlayerEntity playerEntity = new PlayerEntity( entity.getGameInstance().getExcept("engine2D:entity_type", EntityType.class),
                    entity.getGameInstance().getExcept("engine2D:test_sprite", Sprite.class),
                    ID, entity.getGameInstance());

            Flame flame = new Flame(entity.getGameInstance(), playerEntity);
            entity.getClient().getWorld().queueEntities(List.of(playerEntity, flame));

            return playerEntity;
        });

        player.setPosition(x, y, z);
        player.health = health;
        player.alive = visisble;
        player.rot = rot;
        player.firing = firing;
    }
}
