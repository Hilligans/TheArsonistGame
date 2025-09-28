package dev.hilligans.engine2d.world;

import dev.hilligans.arsonist.network.CCreateEntitiesPacket;
import dev.hilligans.arsonist.network.CUpdatePlayer;
import dev.hilligans.engine.GameInstance;
import dev.hilligans.engine.network.engine.NetworkEntity;
import dev.hilligans.engine2d.client.sprite.ISpriteEntity;
import dev.hilligans.engine.entity.IEntity;

import java.util.ArrayList;
import java.util.List;

public class World2D {

    public ArrayList<Entity2D> entities = new ArrayList<>();
    public ArrayList<ISpriteEntity> renderableEntities = new ArrayList<>();
    public ArrayList<Entity2D> queuedEntities = new ArrayList<>();
    public ArrayList<Entity2D> queuedEntities1 = new ArrayList<>();


    public GameInstance gameInstance;
    public Scene scene;

    public double lastFrametime;
    public long frameStartTime;

    public NetworkEntity networkEntity;

    public World2D(GameInstance gameInstance, String scene) {
        this.gameInstance = gameInstance;
        this.scene = gameInstance.getExcept(scene, Scene.class);
    }

    public void addEntity(Entity2D entity) {
        entity.setWorld(this);
        this.entities.add(entity);

        if(entity instanceof ISpriteEntity spriteEntity) {
            renderableEntities.addFirst(spriteEntity);
        }
    }

    public void removeEntity(IEntity entity) {
        this.entities.remove(entity);
        if(entity instanceof ISpriteEntity spriteEntity) {
            renderableEntities.remove(spriteEntity);
        }
    }

    public synchronized void queueEntity(Entity2D entity2D) {
        this.queuedEntities.add(entity2D);
    }

    public synchronized void queueEntities(List<Entity2D> entities) {
        queuedEntities1.addAll(entities);
    }

    public Scene getScene() {
        return scene;
    }

    public List<ISpriteEntity> getRenderableEntities() {
        return renderableEntities;
    }

    public synchronized void tick() {
        for(IEntity entity : entities) {
            entity.tick();
        }
        this.entities.removeIf(IEntity::shouldRemove);
        this.renderableEntities.removeIf(IEntity::shouldRemove);

        for(Entity2D entity : queuedEntities) {
            addEntity(entity);
        }
        for(Entity2D entity : queuedEntities1) {
            addEntity(entity);
        }

        if(networkEntity != null) {
            CCreateEntitiesPacket.send(networkEntity, queuedEntities);
        }

        queuedEntities.clear();
        queuedEntities1.clear();
    }
}
