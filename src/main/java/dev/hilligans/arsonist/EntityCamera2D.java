package dev.hilligans.arsonist;

import dev.hilligans.engine.client.graphics.RenderWindow;
import dev.hilligans.engine.client.graphics.api.ICamera;
import dev.hilligans.engine.client.graphics.resource.MatrixStack;
import dev.hilligans.engine.entity.IEntity;
import dev.hilligans.engine2d.client.Camera2D;
import dev.hilligans.engine2d.world.Entity2D;
import org.jetbrains.annotations.NotNull;
import org.joml.*;

import java.lang.Math;

public class EntityCamera2D extends Camera2D {

    IEntity entity = new Entity2D(null);

    public EntityCamera2D(RenderWindow window, float worldWidth, float worldHeight) {
        super(window, worldWidth, worldHeight);
    }

    @Override
    public void setPosition(double x, double y, double z) {
        this.entity.setPosition(x, y, z);
    }

    @Override
    public void move(float x, float y, float z) {
        this.entity.addPosition(x, y, z);
    }

    @Override
    public Vector3d getPosition() {
        return new Vector3d(entity.getX(), entity.getY(), 0);
    }


    @Override
    public void setMotion(float velX, float velY, float velZ) {

    }

    @Override
    public void addMotion(float velX, float velY, float velZ) {

    }

    @Override
    public Vector3f getMotion() {
        return null;
    }

    @Override
    public MatrixStack getMatrixStack(int W, int H, int x, int y) {
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.translate(W - 1 - 2*x, H - 1 - 2*y, 0).scale(W, H, 1).ortho(0, getWindowWidth(), getWindowHeight(),0,-1,20000);

        float scale = getScale();

        int distanceX = (int)getInsetX();
        int distanceY = (int)getInsetY();

        matrix4f.translate(distanceX, distanceY, 0);
        matrix4f.scale(scale);

        return new MatrixStack(matrix4f);
    }

    public float getInsetX() {
        return (getWindowWidth()- worldWidth*getScale())/2;
    }

    public float getInsetY() {
        return (getWindowHeight()- worldHeight*getScale())/2;
    }

    public float getScale() {
        float scaleX = getWindowWidth() / worldWidth;
        float scaleY = getWindowHeight() / worldHeight;

        return Math.min(scaleX, scaleY);
    }

    @Override
    public Matrix4d getView() {
        return null;
    }

    @Override
    public Vector3d getLookVector() {
        return null;
    }

    @Override
    public void savePosition(Vector3d vector3d) {

    }

    @Override
    public Vector3d getSavedPosition() {
        return null;
    }
}
