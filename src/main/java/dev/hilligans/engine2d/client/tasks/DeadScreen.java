package dev.hilligans.engine2d.client.tasks;

import dev.hilligans.arsonist.Client2D;
import dev.hilligans.engine.GameInstance;
import dev.hilligans.engine.application.IClientApplication;
import dev.hilligans.engine.client.graphics.RenderTask;
import dev.hilligans.engine.client.graphics.RenderTaskSource;
import dev.hilligans.engine.client.graphics.RenderWindow;
import dev.hilligans.engine.client.graphics.ShaderSource;
import dev.hilligans.engine.client.graphics.api.GraphicsContext;
import dev.hilligans.engine.client.graphics.api.IDefaultEngineImpl;
import dev.hilligans.engine.client.graphics.api.IGraphicsEngine;
import dev.hilligans.engine.client.graphics.api.IMeshBuilder;
import dev.hilligans.engine.client.graphics.resource.MatrixStack;
import dev.hilligans.engine.client.graphics.resource.VertexFormat;
import dev.hilligans.engine.client.graphics.util.StringRenderer;
import dev.hilligans.engine2d.client.Camera2D;

import static dev.hilligans.engine2d.world.PlayerEntity.addRGBA;

public class DeadScreen extends RenderTaskSource {
    public DeadScreen() {
        super("dead_screen");
    }

    @Override
    public RenderTask<?> getDefaultTask() {
        return new RenderTask<>() {
            VertexFormat vertexFormat;
            ShaderSource program;

            @Override
            public void draw(RenderWindow window, GraphicsContext graphicsContext, IGraphicsEngine<?, ?, ?> engine, IClientApplication client, MatrixStack worldStack, MatrixStack screenStack, float delta) {
                if(window.getClient() instanceof Client2D client2D) {
                    IDefaultEngineImpl<?, ?, ?> impl = engine.getDefaultImpl();
                    StringRenderer renderer = engine.getStringRenderer();

                    if(!client2D.playerEntity.alive) {
                        renderer.drawCenteredStringInternal(window, graphicsContext, screenStack, "You Died", window.getWindowWidth() / 2, 100, 1);
                        renderer.drawCenteredStringInternal(window, graphicsContext, screenStack, "Press R to respawn", window.getWindowWidth() / 2, 150, 1);
                    }

                    IMeshBuilder builder = engine.getDefaultImpl().getMeshBuilder(vertexFormat);

                    float minY = (float) (window.getWindowHeight() - 20);
                    float maxY = (float) (window.getWindowHeight() - 0);

                    float minX = (float) (100);
                    float maxX = (float) (window.getWindowWidth() - 100);
                    float maxX2 = minX + (maxX - minX) * client2D.playerEntity.firingCapacity / 100f;

                    addRGBA(builder, minX, minY, maxX, maxY, 0.3f, 0.3f, 0.3f, 1f);
                    addRGBA(builder, minX, minY, maxX2, maxY, 0.1f, 0.1f, 1f, 1f);


                    engine.getDefaultImpl().bindPipeline(graphicsContext, program.program);
                    engine.getDefaultImpl().uploadMatrix(graphicsContext, screenStack, program);
                    engine.getDefaultImpl().drawAndDestroyMesh(graphicsContext, screenStack, builder);
                    engine.getDefaultImpl().bindPipeline(graphicsContext, program.program);

                    if(client2D.playerEntity.overheat) {
                        renderer.drawCenteredStringInternal(window, graphicsContext, screenStack, "Overheat!! Wait to recharge", window.getWindowWidth() / 2, window.getWindowHeight() - 65, 0.8f);
                    }
                }
            }

            @Override
            public void load(GameInstance gameInstance, IGraphicsEngine<?, ?, ?> graphicsEngine, GraphicsContext graphicsContext) {
                program = gameInstance.getExcept("ourcraft:position_color_shader", ShaderSource.class);
                vertexFormat = gameInstance.getExcept("ourcraft:position_color", VertexFormat.class);
            }
        };
    }

    public static void addQuad(IMeshBuilder builder, float x, float y, float maxX, float maxY) {
        int s = builder.getVertexCount();

        float R = 0;
        float G = 0;
        float B = 0;
        float A = 1;

        builder.addVertices(x,    y,    1, R, G, B, A,
                maxX, y,    1, R, G, B, A,
                x,    maxY, 1, R, G, B, A,
                maxX, maxY, 1, R, G, B, A);

        builder.addCounterClockwiseIndices(s);
    }
}
