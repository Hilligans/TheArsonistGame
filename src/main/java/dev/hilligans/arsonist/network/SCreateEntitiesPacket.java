package dev.hilligans.arsonist.network;

import dev.hilligans.arsonist.Client2D;
import dev.hilligans.arsonist.entity.FireEntity;
import dev.hilligans.engine.entity.EntityType;
import dev.hilligans.engine.entity.IEntity;
import dev.hilligans.engine.network.engine.ClientNetworkEntity;
import dev.hilligans.engine.network.engine.NetworkEntity;
import dev.hilligans.engine.network.packet.ServerToClientPacketType;
import dev.hilligans.engine.util.IByteArray;
import dev.hilligans.engine2d.client.sprite.Sprite;
import dev.hilligans.engine2d.world.Entity2D;

import java.util.ArrayList;
import java.util.List;

import static java.lang.System.exit;

public class SCreateEntitiesPacket extends ServerToClientPacketType<Client2D> {

    public static final SCreateEntitiesPacket instance = new SCreateEntitiesPacket();

    public static IByteArray create(NetworkEntity entity, IByteArray data) {
        IByteArray array = instance.getWriteArray(entity);
        array.write(data);
        return array;
    }

    public static IByteArray create(NetworkEntity entity, List<IEntity> entities) {
        return instance.encode(entity, entities);
    }

    public IByteArray encode(NetworkEntity entity, List<IEntity> entities) {
        IByteArray array = getWriteArray(entity);

        array.writeVarInt(entities.size());

        EntityType lastEntityType = null;
        for(IEntity ent : entities) {
            if(ent.getEntityType() != lastEntityType) {
                array.writeBoolean(true);
                lastEntityType = ent.getEntityType();
                array.writeUTF8(lastEntityType.getIdentifierName());
            } else {
                array.writeBoolean(false);
            }
            ent.encode(array);
        }

        return array;
    }

    @Override
    public void decode(ClientNetworkEntity<Client2D> entity, IByteArray data) {
        int count = data.readVarInt();

        List<Entity2D> addedEntities = new ArrayList<>();
        EntityType flameType = entity.getGameInstance().getExcept("arsonist:campfire", EntityType.class);
        Sprite sprite = entity.getGameInstance().getExcept("arsonist:FireSprite", Sprite.class);

        String entityType = "";
        for (int i = 0; i < count; i++) {
            if (data.readBoolean()) {
                entityType = data.readUTF8();
            }

            if (flameType.getIdentifierName().equals(entityType)) {
                float x = data.readFloat();
                float y = data.readFloat();
                float z = data.readFloat();

                long time = data.readLong();

                addedEntities.add(new FireEntity(flameType, sprite, x, y, time));
            } else {
                System.out.println(entityType);
                exit(1);
            }
        }


        entity.getClient().getWorld().queueEntities(addedEntities);
    }
}
