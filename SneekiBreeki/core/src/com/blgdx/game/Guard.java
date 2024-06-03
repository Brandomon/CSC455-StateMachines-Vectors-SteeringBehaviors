package com.blgdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;

public class Guard {
    private Vector2 position;                                           // Vector signifying the position of the guard
    private Vector2 originalPosition;                                   // Vector signifying the original position of the guard at the corner of the building for RETURN state
    private float startingAngle;                                        // Starting angle of the guard to scan a full 360 degrees within the SEARCH state
    private final int guardRadius = 10;                                 // Size of the circle portraying the guard
    private static final float FOV_ANGLE = 120;                         // Angle of the guard's field of view
    private static final float maxVisibilityDistance = 150;             // Maximum visibility distance of the guard
    private static float scanAngle;                                // Direction of the guard in degrees
    private static final float SCAN_SPEED = 1.5f;                       // Speed the guard scans back and forth within PATROL and SEARCH states
    private boolean scanDirectionRight = true;                          // Boolean signaling change between scanning right and left within PATROL state
    private final int guardSpeed = 90;                                 // Speed of the guard when moving
    private final int buildingCornerAngleOffset = 45;                   // Angle offset to allow guard to scan from the building's top right corner to the bottom left corner from the bottom right corner (270 degrees)
    private final int guardStartingPointWiggleRoom = 2;                 // Allowed room between guard and starting point for RETURN to PATROL state change (Violently changes direction back and forth when set to < 2 when starting point is reached)
    private GuardState guardState;                                      // Guard's state (PATROL, CHASE, SEARCH, RETURN)
    private Player player;                                              // Player object to keep track of coordinates for CHASE state and detect if within FOV
    private Building building;                                          // Reference to the Building instance for collision detection
    private Vector2 velocity;
    private Vector2 acceleration;
    private final float maxSpeed = 90;                                 // Maximum speed of the guard
    private final float maxAcceleration = 80;                          // Maximum acceleration of the guard
    private final float decelerationCircleRadius = 300;                 // Size of radius around starting position to decelerate guard during RETURN state
    private ShapeRenderer shapeRenderer;
    private static final float CHASE_PREDICTION_TIME = 0.2f; // Adjust the prediction time as needed


    public enum GuardState {
        PATROL,
        CHASE,
        SEARCH,
        RETURN
    }

    public Guard(Vector2 buildingCorner, Player player, Building building) {
        this.position = new Vector2(buildingCorner.x, buildingCorner.y);
        this.originalPosition = new Vector2(buildingCorner.x, buildingCorner.y);
        this.player = player;
        this.building = building;
        this.guardState = GuardState.PATROL;
        this.velocity = new Vector2(0, 0);
        this.acceleration = new Vector2(0, 0);
        shapeRenderer = new ShapeRenderer();
    }

    public void drawFOV() {
        // Draw the FOV lines
        float halfFOVAngleRad = 60;
        float leftEdgeX = position.x + (float) (maxVisibilityDistance * Math.cos(MathUtils.degreesToRadians * (scanAngle - halfFOVAngleRad)));
        float leftEdgeY = position.y + (float) (maxVisibilityDistance * Math.sin(MathUtils.degreesToRadians * (scanAngle - halfFOVAngleRad)));
        float rightEdgeX = position.x + (float) (maxVisibilityDistance * Math.cos(MathUtils.degreesToRadians * (scanAngle + halfFOVAngleRad)));
        float rightEdgeY = position.y + (float) (maxVisibilityDistance * Math.sin(MathUtils.degreesToRadians * (scanAngle + halfFOVAngleRad)));

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.GRAY);
        shapeRenderer.line(position.x, position.y, leftEdgeX, leftEdgeY);
        shapeRenderer.line(position.x, position.y, position.x, position.y);
        shapeRenderer.line(position.x, position.y, rightEdgeX, rightEdgeY);
        shapeRenderer.end();
    }

    public void update() {
        switch (guardState) {
            case PATROL:
                // Chase if player is visible
                if (playerIsVisible(player, maxVisibilityDistance)) {
                    guardState = GuardState.CHASE;
                } else {
                    // Update guard's scan angle based on scan direction
                    if (scanDirectionRight) {
                        scanAngle += SCAN_SPEED;
                        // Adjusted the condition to ensure scanning 270 degrees from top right corner to bottom left corner of the building
                        if (scanAngle >= 75 - buildingCornerAngleOffset) {
                            scanAngle = 75 - buildingCornerAngleOffset;
                            scanDirectionRight = false;
                        }
                    } else {
                        scanAngle -= SCAN_SPEED;
                        // Adjusted the condition to ensure scanning 270 degrees from top right corner to bottom left corner of the building
                        if (scanAngle <= -75 - buildingCornerAngleOffset) {
                            scanAngle = -75 - buildingCornerAngleOffset;
                            scanDirectionRight = true;
                        }
                    }
                }
                break;

            case CHASE:
                // Logic to chase the player
                if (!playerIsVisible(player, maxVisibilityDistance)) {
                    // Gradually reduce the guard's speed until it stops
                    float decelerationRate = 100.0f; // Adjust this value to control deceleration speed

                    // Calculate the magnitude of the current velocity
                    float speed = velocity.len();

                    // Check if the guard has effectively stopped
                    if (speed <= 0.1) {
                        guardState = GuardState.SEARCH;
                        break;
                    }

                    // Calculate the deceleration to be applied
                    float deceleration = Math.min(speed, decelerationRate * Gdx.graphics.getDeltaTime());

                    // Update velocity by reducing its magnitude
                    velocity.scl(Math.max(0, (speed - deceleration) / speed));

                    // Update the old X and old Y coordinates
                    float oldX = position.x;
                    float oldY = position.y;

                    // Update position based on the updated velocity
                    position.add(velocity.cpy().scl(Gdx.graphics.getDeltaTime()));

                    // Check for collision with building
                    if (building.collidesWithGuard(this)) {
                        // Handle collision by finding a new position that avoids the building
                        Vector2 guardToBuilding = new Vector2(building.getPosition().x - oldX, building.getPosition().y - oldY).nor();
                        float newX = oldX - guardToBuilding.x * velocity.len() * Gdx.graphics.getDeltaTime();
                        float newY = oldY - guardToBuilding.y * velocity.len() * Gdx.graphics.getDeltaTime();

                        // Update position to the new collision-free position
                        position.set(newX, newY);
                    }
                } else {
                    // Calculate the direction vector from the guard to the player
                    Vector2 directionToPlayer = new Vector2(player.getPosition().x - position.x, player.getPosition().y - position.y).nor();

                    // Estimate the future position of the player using Euler's method
                    Vector2 futurePlayerPosition = player.getPosition().cpy().add(new Vector2(player.getVelocity(), player.getVelocity()).scl(CHASE_PREDICTION_TIME));

                    // Calculate the direction vector from the guard to the future player position
                    Vector2 directionToFuturePlayer = new Vector2(futurePlayerPosition.x - position.x, futurePlayerPosition.y - position.y).nor();

                    // Update the scan angle to face towards the predicted player position
                    scanAngle = directionToFuturePlayer.angleDeg();

                    // Calculate the distance to the player
                    float distanceToPlayer = position.dst(futurePlayerPosition);

                    // Calculate the desired velocity for pursuit
                    Vector2 desiredVelocity = directionToFuturePlayer.scl(maxSpeed);

                    // Calculate the steering force (acceleration) towards the desired velocity
                    Vector2 steering = desiredVelocity.sub(velocity).limit(maxAcceleration);

                    // Apply steering to acceleration
                    acceleration.set(steering);

                    // Update velocity based on acceleration
                    velocity.add(acceleration.cpy().scl(Gdx.graphics.getDeltaTime()));

                    // Limit velocity to maximum speed
                    velocity.limit(maxSpeed);

                    // Move the guard
                    float oldX = position.x;
                    float oldY = position.y;
                    position.add(velocity.cpy().scl(Gdx.graphics.getDeltaTime()));

                    // Check for collision with building
                    if (building.collidesWithGuard(this)) {
                        // Handle collision by finding a new position that avoids the building
                        Vector2 guardToBuilding = new Vector2(building.getPosition().x - oldX, building.getPosition().y - oldY).nor();
                        float newX = oldX - guardToBuilding.x * velocity.len() * Gdx.graphics.getDeltaTime();
                        float newY = oldY - guardToBuilding.y * velocity.len() * Gdx.graphics.getDeltaTime();

                        // Update position to the new collision-free position
                        position.set(newX, newY);
                    }
                }
                break;

            case SEARCH:
                // Reinitialze velocity to 0
                velocity.x = 0;
                velocity.y = 0;

                // Logic to search for the player
                if (playerIsVisible(player, maxVisibilityDistance)) {
                    // Reinitialize startingAngle to 0
                    startingAngle = 0;

                    guardState = GuardState.CHASE;
                } else {
                    // Incrementally scan through the full FOV
                    if (startingAngle < 360) {
                        // Increment the scan angle & startingAngle
                        scanAngle += SCAN_SPEED;
                        startingAngle += SCAN_SPEED;
                        if (playerIsVisible(player, maxVisibilityDistance)) {
                            guardState = GuardState.CHASE;
                        }
                    } else {
                        // Player not found, return to building corner
                        guardState = GuardState.RETURN;
                    }
                }
                break;


            case RETURN:
                // Reinitialize starting angle back to zero
                startingAngle = 0;

                // Calculate the direction vector from the guard's current position to the original starting point (building corner)
                Vector2 directionToOriginalPosition = new Vector2(originalPosition.x - position.x, originalPosition.y - position.y).nor();

                // Update the scan angle to face directly towards the player
                scanAngle = directionToOriginalPosition.angleDeg();

                // Calculate the distance to the original position
                float distanceToOriginalPosition = position.dst(originalPosition);

                // Calculate the desired speed based on the remaining distance
                float desiredSpeed = Math.max(0, (distanceToOriginalPosition / decelerationCircleRadius) * maxSpeed);

                // Calculate the desired velocity for returning to the original position
                Vector2 desiredVelocity = directionToOriginalPosition.scl(desiredSpeed);

                // Calculate the acceleration needed to achieve the desired velocity
                Vector2 acceleration = desiredVelocity.sub(velocity).limit(maxAcceleration);

                // Update velocity based on acceleration
                velocity.add(acceleration.scl(Gdx.graphics.getDeltaTime()));

                // Move the guard towards the original position
                position.add(velocity.cpy().scl(Gdx.graphics.getDeltaTime()));

                // Check for collision with building
                if (building.collidesWithGuard(this)) {
                    // Handle collision by finding a new position that avoids the building
                    Vector2 guardToBuilding = new Vector2(building.getPosition().x - position.x, building.getPosition().y - position.y).nor();
                    float newX = position.x - guardToBuilding.x * velocity.len() * Gdx.graphics.getDeltaTime();
                    float newY = position.y - guardToBuilding.y * velocity.len() * Gdx.graphics.getDeltaTime();

                    // Update position to the new collision-free position
                    position.set(newX, newY);
                }

                // Check for player within FOV
                if (playerIsVisible(player, maxVisibilityDistance)) {
                    guardState = GuardState.CHASE;
                }

                // Check if the guard has reached close to the original position
                if (distanceToOriginalPosition < guardSpeed * Gdx.graphics.getDeltaTime()) {
                    // Set guard position to the original position
                    position.set(originalPosition);

                    // Slow down the guard to stop
                    velocity.setZero();

                    // Change state to PATROL
                    scanAngle = 0;
                    guardState = GuardState.PATROL;
                }
                break;
        }
    }

    public void draw(ShapeRenderer shapeRenderer) {
        // Draw guard
        shapeRenderer.setColor(1, 0, 0, 1);
        shapeRenderer.circle(position.x, position.y, guardRadius);

        // Calculate vertices of FOV triangle
        float halfFOVAngleRad = MathUtils.degreesToRadians * (FOV_ANGLE / 2); // Convert FOV angle to radians and divide by 2

        // Calculate the vertices based on the FOV angle
        float vertex2X = position.x + (float) (maxVisibilityDistance * Math.cos(MathUtils.degreesToRadians * (scanAngle + halfFOVAngleRad)));
        float vertex2Y = position.y + (float) (maxVisibilityDistance * Math.sin(MathUtils.degreesToRadians * (scanAngle + halfFOVAngleRad)));
        float vertex3X = position.x + (float) (maxVisibilityDistance * Math.cos(MathUtils.degreesToRadians * (scanAngle - halfFOVAngleRad)));
        float vertex3Y = position.y + (float) (maxVisibilityDistance * Math.sin(MathUtils.degreesToRadians * (scanAngle - halfFOVAngleRad)));

        // Draw FOV triangle
        shapeRenderer.setColor(Color.GRAY);
        shapeRenderer.triangle(position.x, position.y, vertex2X, vertex2Y, vertex3X, vertex3Y);
    }

    public Circle getBoundingBox() {
        return new Circle(position, guardRadius);
    }

    boolean playerIsVisible(Player player, float maxVisibilityDistance) {
        // Get the position of the player
        Vector2 playerPosition = player.getPosition();

        // Calculate the vector from the guard to the player
        Vector2 directionToPlayer = new Vector2(playerPosition.x - position.x, playerPosition.y - position.y);

        // Calculate the angle between the guard's line of sight and the vector to the player
        float angleToPlayer = directionToPlayer.angleDeg();

        // Adjust angles to be within the range [0, 360) to ensure proper comparison
        float normalizedScanAngle = (scanAngle + 360) % 360;
        float normalizedAngleToPlayer = (angleToPlayer + 360) % 360;

        // Calculate the smallest difference between angles (considering angles as circular values)
        float angleDifference = Math.abs(normalizedScanAngle - normalizedAngleToPlayer);
        float shortestAngle = Math.min(angleDifference, 360 - angleDifference);

        // Check if the shortest angle is within the guard's FOV angle
        if (shortestAngle <= FOV_ANGLE / 2) {
            // Calculate the distance to the player using trigonometry
            float distanceToPlayer = directionToPlayer.len();

            // Calculate the maximum distance based on FOV angle and a fixed length
            float maxDistanceFromFOV = maxVisibilityDistance / MathUtils.sinDeg(FOV_ANGLE / 2);

            // Check if the player is within the maximum visibility distance
            if (distanceToPlayer <= maxDistanceFromFOV) {
                return true; // Player is visible within FOV and distance
            }
        }
        return false; // Player is either outside FOV or beyond maximum visibility distance
    }

    public void reset(Vector2 newPosition) {
        position.set(newPosition);
        originalPosition.set(newPosition);
        guardState = GuardState.PATROL;
    }

    public GuardState getGuardState() {
        return guardState;
    }
}