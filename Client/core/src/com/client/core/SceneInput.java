package com.client.core;

public interface  SceneInput {
    public boolean keyDown(int keycode);
    public boolean keyUp(int keycode);
    public boolean keyTyped(char character);
    public boolean touchDown(int x, int y, int pointer, int button);
    public boolean touchUp(int x, int y, int pointer, int button);
    public boolean touchDragged(int x, int y, int pointer);
    public boolean mouseMoved(int x, int y);
    public boolean scrolled(int amount);
    public boolean clicked(float x, float y);
}