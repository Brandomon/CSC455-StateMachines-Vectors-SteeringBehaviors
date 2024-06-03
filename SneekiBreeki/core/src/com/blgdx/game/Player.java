package com.blgdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class Player {
    private int playerRadius = 10;
    private Vector2 position;
    private Vector2 direction;
    private float rotation; // Player's rotation in degrees
    private float velocity; // Player's velocity
    private float maxVelocity = 100; // Maximum velocity of the player
    private float accelerationRate = 80; // Acceleration rate of the player
    private float decelerationRate = 100; // Deceleration rate of the player


    public Player(Vector2 position) {
        this.position = position;
        this.direction = new Vector2(1, 0);   // Initial direction (e.g., facing right)
        this.rotation = 0;                          // Initial rotation
        this.velocity = 0;                          // Initial velocity
    }

//    public void move(float x, float y) {
//        // Update direction vector only if there is movement
//        direction.set(x, y).nor();
//
//        // Update acceleration based on input
//        acceleration.set(x, y).nor().scl(maxAcceleration);
//
//        // Update velocity based on acceleration
//        velocity.add(acceleration).limit(maxSpeed);
//
//        // Update position based on velocity
//        position.add(velocity);
//    }

    public void move(float newX, float newY, float screenWidth, float screenHeight) {
        // Ensure newX and newY are within the bounds of the screen
        newX = MathUtils.clamp(newX, 0, screenWidth);
        newY = MathUtils.clamp(newY, 0, screenHeight);

        position.set(newX, newY);
    }

    public void stop() {
        // Reset velocity when player stops
        velocity = 0;
    }

    public void rotate(float delta, boolean rotateLeft, boolean rotateRight) {
        if (rotateLeft) {
            rotation += 180 * delta;
        }
        if (rotateRight) {
            rotation -= 180 * delta;
        }
    }

    public void updateVelocity(float delta, boolean thrust, boolean reverse) {
        // Calculate thrust direction based on player's rotation
        float thrustDirection = rotation;

        // Update velocity based on thrust or reverse
        if (thrust) {
            // Gradually update thrust direction towards the target direction
            direction.set(MathUtils.cosDeg(thrustDirection), MathUtils.sinDeg(thrustDirection));
            velocity += accelerationRate * delta;
        } else if (reverse) {
            velocity -= (decelerationRate + 100f) * delta;
        } else {
            velocity -= decelerationRate * delta;
        }

        // Ensure velocity doesn't go below 0
        velocity = Math.max(velocity, 0);

        // Limit the player's velocity
        velocity = Math.min(velocity, maxVelocity);

        // Update the player's position based on velocity
        position.add(direction.x * velocity * delta, direction.y * velocity * delta);
    }

//    public void stop() {
//        // Reduce velocity gradually to simulate deceleration when no keys are pressed
//        velocity.scl(1 - decelerationRate * Gdx.graphics.getDeltaTime());
//    }
//
//    public void applyDeceleration() {
//        // Apply deceleration to gradually reduce velocity when no movement keys are pressed
//        velocity.scl(1 - decelerationRate * Gdx.graphics.getDeltaTime());
//
//        // Ensure velocity does not become too small
//        if (velocity.len2() < 10f) {
//            velocity.setZero();
//        }
//    }
//
//    public void resetAcceleration() {
//        // Reset acceleration when no movement keys are pressed
//        acceleration.setZero();
//    }

    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getDirection() {
        return direction;
    }

    public float getVelocity() {
        return velocity;
    }

    public void draw(ShapeRenderer shapeRenderer) {
        // Draw player
        shapeRenderer.setColor(1, 1, 1, 1);
        shapeRenderer.circle(position.x, position.y, playerRadius);

        // Draw orientation line
        float lineLength = 20; // Length of the line representing the orientation
        float lineX = position.x + lineLength * MathUtils.cosDeg(rotation);
        float lineY = position.y + lineLength * MathUtils.sinDeg(rotation);
        shapeRenderer.line(position.x, position.y, lineX, lineY);
    }

    public Circle getBoundingBox() {
        return new Circle(position, playerRadius);
    }

    public void reset(Vector2 newPosition) {
        position.set(newPosition);
//        velocity.setZero(); // Reset velocity when player is reset
//        acceleration.setZero(); // Reset acceleration when player is reset
    }
}