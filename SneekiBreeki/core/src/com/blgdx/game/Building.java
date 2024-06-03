package com.blgdx.game;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

class Building {
    private Vector2 position;
    private float width;
    private float height;

    public Building(Vector2 position, float width, float height) {
        this.position = position;
        this.width = width;
        this.height = height;
    }

    public void draw(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(0, 0, 1, 1);
        shapeRenderer.rect(position.x - width / 2, position.y - height / 2, width, height);
    }

    // Method to check collision with building and player
    public boolean collidesWithPlayer(float playerX, float playerY, int playerRadius) {
        Rectangle buildingRect = new Rectangle(position.x - width / 2, position.y - height / 2, width, height);
        Rectangle playerRect = new Rectangle(playerX - playerRadius / 2, playerY - playerRadius / 2, playerRadius, playerRadius);
        return buildingRect.overlaps(playerRect);
    }

    // Method to check collision with building and guard
    public boolean collidesWithGuard(Guard guard) {
        return Intersector.overlaps(guard.getBoundingBox(), getBoundingBox());
    }

    // Method to get building bounding box
    public Rectangle getBoundingBox() {
        return new Rectangle(position.x - width / 2, position.y - height / 2, width, height);
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
    public Vector2 getPosition() {
        return position;
    }

}