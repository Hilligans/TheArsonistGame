package dev.hilligans.arsonist;

import dev.hilligans.arsonist.network.CCreateEntitiesPacket;
import dev.hilligans.arsonist.network.CUpdatePlayer;
import dev.hilligans.arsonist.network.SCreateEntitiesPacket;
import dev.hilligans.arsonist.network.SUpdatePlayer;
import dev.hilligans.engine.client.graphics.RenderWindow;
import dev.hilligans.engine.client.input.Input;
import dev.hilligans.engine.client.input.RepeatingInput;
import dev.hilligans.engine.client.input.handlers.MouseHandler;
import dev.hilligans.engine.entity.EntityType;
import dev.hilligans.engine.mod.handler.ModClass;
import dev.hilligans.engine.mod.handler.content.CoreExtensionView;
import dev.hilligans.engine.mod.handler.content.ModContainer;
import dev.hilligans.engine.mod.handler.content.RegistryView;
import dev.hilligans.engine.mod.handler.pipeline.InstanceLoaderPipeline;
import dev.hilligans.engine2d.client.sprite.AnimatedSprite;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_R;

public class Arsonist implements ModClass {

    public static final String MOD_ID = "arsonist";

    @Override
    public String getModID() {
        return MOD_ID;
    }

    @Override
    public void registerHooks(InstanceLoaderPipeline<?> pipeline) {
        ModClass.super.registerHooks(pipeline);
    }

    @Override
    public void registerRegistries(RegistryView view) {
        ModClass.super.registerRegistries(view);
    }

    @Override
    public void registerCoreExtensions(CoreExtensionView view) {
        ModClass.super.registerCoreExtensions(view);
        if(view.getGameInstance().getSide().isClient()) {
            view.registerKeybinds(new Input("left_click", "ourcraft:mouse_handler::0") {
                @Override
                public void press(RenderWindow renderWindow, double strength) {
                    if(renderWindow.getClient() instanceof Client2D client2D) {
                        client2D.playerEntity.firing = true;
                    }
                }

                @Override
                public void release(RenderWindow renderWindow, double strength) {
                    if(renderWindow.getClient() instanceof Client2D client2D) {
                        client2D.playerEntity.firing = false;
                    }
                }
            });
            view.registerKeybinds(new Input("rotate_yaw", "ourcraft:mouse_handler::" + MouseHandler.MOUSE_X) {
                @Override
                public void press(RenderWindow window, double strength) {
                    if(window.getClient() instanceof Client2D client2D) {
                        client2D.mouseX = (float) strength;
                        client2D.updateFlameRot();
                    }
                }
            }.onlyWithPipelines("engine2D:pipeline2d"));

            view.registerKeybinds(new Input("rotate_pitch", "ourcraft:mouse_handler::" + MouseHandler.MOUSE_Y) {
                @Override
                public void press(RenderWindow window, double strength) {
                    if(window.getClient() instanceof Client2D client2D) {
                        client2D.mouseY = (float) strength;
                        client2D.updateFlameRot();
                    }
                }
            }.onlyWithPipelines("engine2D:pipeline2d"));

            view.registerKeybinds(new Input("respawn", "ourcraft:key_press_handler::" + GLFW_KEY_R) {
                @Override
                public void press(RenderWindow window, double strength) {
                    if(window.getClient() instanceof Client2D client2D) {
                        if(!client2D.playerEntity.alive) {
                            client2D.respawn();
                        }
                    }
                }
            }.onlyWithPipelines("engine2D:pipeline2d"));

            view.registerApplication(new Client2D());
        }
        view.registerApplication(new ArsonistServer());
    }

    @Override
    public void registerContent(ModContainer container) {
        container.registerEntityType(new EntityType("campfire", MOD_ID));
        container.registerEntityType(new EntityType("flame", MOD_ID));

        if(container.getGameInstance().getSide().isClient()) {
            container.register("engine2D:sprite", new AnimatedSprite("FireSprite", "arsonist/fire1.png", "arsonist/sprites/FireSprite.json"));
            container.register("engine2D:sprite", new RotatingAnimatedSprite("FlameSprite", "arsonist/flame.png", "arsonist/sprites/FlameSprite.json"));
        }

        container.registerPacket("arsonist:play", 0, CCreateEntitiesPacket.instance);
        container.registerPacket("arsonist:play", 1, SCreateEntitiesPacket.instance);
        container.registerPacket("arsonist:play", 2, CUpdatePlayer.instance);
        container.registerPacket("arsonist:play", 3, SUpdatePlayer.instance);
    }
}
