package dev.hilligans.arsonist.network;

import dev.hilligans.arsonist.ArsonistServer;
import dev.hilligans.arsonist.entity.FireEntity;
import dev.hilligans.engine.entity.EntityType;
import dev.hilligans.engine.entity.IEntity;
import dev.hilligans.engine.network.engine.NetworkEntity;
import dev.hilligans.engine.network.engine.ServerNetworkEntity;
import dev.hilligans.engine.network.packet.ClientToServerPacketType;
import dev.hilligans.engine.util.IByteArray;
import dev.hilligans.engine2d.world.Entity2D;

import java.util.ArrayList;
import java.util.List;

import static java.lang.System.exit;

public class CCreateEntitiesPacket extends ClientToServerPacketType<ArsonistServer> {

    public static final CCreateEntitiesPacket instance = new CCreateEntitiesPacket();

    public static void send(NetworkEntity entity, ArrayList<Entity2D> entities) {
        entity.sendPacket(instance.encode(entity, entities));
    }

    public IByteArray encode(NetworkEntity entity, ArrayList<Entity2D> entities) {
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
    public void decode(ServerNetworkEntity<ArsonistServer> entity, IByteArray data) {
        int count = data.readVarInt();

        List<IEntity> addedEntities = new ArrayList<>();
        EntityType flameType = entity.getGameInstance().getExcept("arsonist:campfire", EntityType.class);

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

                addedEntities.add(new FireEntity(flameType, x, y, time));
            } else {
                System.out.println(entityType);
                exit(1);
            }
        }


        IByteArray newData = SCreateEntitiesPacket.create(entity, addedEntities);
        for(NetworkEntity networkEntity : entity.getServer().networkEntities) {
            if(networkEntity.isAlive() && networkEntity != entity) {
                networkEntity.sendPacket(newData);
            }
        }
    }
}
