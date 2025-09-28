package dev.hilligans.arsonist;

import dev.hilligans.arsonist.entity.FireEntity;
import dev.hilligans.arsonist.network.SCreateEntitiesPacket;
import dev.hilligans.arsonist.network.SUpdatePlayer;
import dev.hilligans.engine.GameInstance;
import dev.hilligans.engine.application.IServerApplication;
import dev.hilligans.engine.entity.EntityType;
import dev.hilligans.engine.network.engine.INetworkEngine;
import dev.hilligans.engine.network.engine.NetworkEntity;
import dev.hilligans.engine.network.engine.NetworkSocket;
import dev.hilligans.engine.util.IByteArray;
import dev.hilligans.engine.util.argument.Argument;
import dev.hilligans.engine2d.world.PlayerEntity;
import dev.hilligans.engine2d.world.World2D;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ArsonistServer implements IServerApplication {

    public static Argument<Boolean> server = Argument.existArg("--server");

    public ConcurrentHashMap<Long, PlayerEntity> players = new ConcurrentHashMap<>();

    World2D world;

    public NetworkSocket<?> socket;
    public ConcurrentLinkedQueue<NetworkEntity> networkEntities = new ConcurrentLinkedQueue<>();
    GameInstance gameInstance;

    @Override
    public void postCoreStartApplication(GameInstance gameInstance) {

    }

    public void setup() {
        System.out.println("Starting server");

        INetworkEngine<?, ?> engine = gameInstance.getExcept("ourcraft:nettyEngine", INetworkEngine.class);
        socket = engine.openServer(gameInstance.PROTOCOLS.getExcept("arsonist:play"),this,"25588");
        socket.onConnected((entity -> {
            System.out.println("Entity Connected");
            this.networkEntities.add(entity);
            for(PlayerEntity entity1 : players.values()) {
                entity.sendPacket(SUpdatePlayer.encode(entity, entity1));
            }
        }));

        Thread thread = new Thread(this::connector);
        thread.start();


        while(true) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(!networkEntities.isEmpty()) {
                EntityType flameType = gameInstance.getExcept("arsonist:campfire", EntityType.class);
                send(SCreateEntitiesPacket.create(networkEntities.peek(), List.of(new FireEntity(flameType, 0, 0, System.currentTimeMillis() + 500))));
            }
        }
    }

    public void send(IByteArray array) {
        for(NetworkEntity entity : networkEntities) {
            entity.sendPacket(array);
        }
    }

    public void connector() {
        try {
            socket.connectSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startApplication(GameInstance gameInstance) {
        if(server.get(gameInstance)) {
            this.gameInstance = gameInstance;
            Thread thread = new Thread(this::setup);
            thread.start();
        }
    }

    @Override
    public String getResourceName() {
        return "";
    }

    @Override
    public String getResourceOwner() {
        return "";
    }
}
