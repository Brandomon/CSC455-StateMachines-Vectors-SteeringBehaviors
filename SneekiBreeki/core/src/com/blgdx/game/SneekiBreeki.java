package com.blgdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

public class SneekiBreeki extends ApplicationAdapter {
	private OrthographicCamera camera;
	private ShapeRenderer shapeRenderer;
	private Player player;
	private Guard guard;
	private Building building;
	private int playerSize = 10;
	private int buildingSize = 100;
	private int guardBuildingOffset = 50;
	private boolean gameOver = false;
	private SpriteBatch spriteBatch;
	private BitmapFont font;
	private GlyphLayout layout;

	@Override
	public void create () {
		float screenWidth = Gdx.graphics.getWidth();
		float screenHeight = Gdx.graphics.getHeight();

		// Initialize sprite batch
		spriteBatch = new SpriteBatch();

		// Initialize bitmap font
		font = new BitmapFont();
		layout = new GlyphLayout();

		// Initialize orthographic camera
		camera = new OrthographicCamera(screenWidth, screenHeight);
		camera.position.set(screenWidth / 2, screenHeight / 2, 0);
		camera.update();

		shapeRenderer = new ShapeRenderer();

		player = new Player(new Vector2(50, 50));
		building = new Building(new Vector2(screenWidth / 2, screenHeight / 2), buildingSize, buildingSize);
		guard = new Guard(new Vector2(screenWidth / 2 + guardBuildingOffset + playerSize, screenHeight / 2 - guardBuildingOffset - playerSize), player, building);
	}

	@Override
	public void render() {
		if (!gameOver) {
			// Handle input, clear screen, and render game only if game is not over
			handleInput();
			Gdx.gl.glClearColor(0, 0, 0, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

			shapeRenderer.setProjectionMatrix(camera.combined);
			shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

			// Draw building
			building.draw(shapeRenderer);

			// Update guard
			guard.update();

			// Check collision between guard and player
			Circle guardBoundingBox = guard.getBoundingBox();
			Circle playerBoundingBox = player.getBoundingBox();
			if (guardBoundingBox.overlaps(playerBoundingBox)) {
				// Set game over state to true
				gameOver = true;
			}

			// Draw guard
			guard.draw(shapeRenderer);

			// Draw guard's FOV
			guard.drawFOV();

			// Draw player
			player.draw(shapeRenderer);

			shapeRenderer.end();

			// Render guard state at the top of the screen
			spriteBatch.begin();
			font.draw(spriteBatch, "Guard State: " + guard.getGuardState(), 20, Gdx.graphics.getHeight() - 20);
			spriteBatch.end();
		} else {
			// Render game over message
			renderGameOver();
		}
	}

	private void renderGameOver() {
		// Clear the screen
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// Set projection matrix
		spriteBatch.setProjectionMatrix(camera.combined);

		// Begin sprite batch rendering
		spriteBatch.begin();

		// Render game over message
		String gameOverMessageLine1 = "GAME OVER!";
		String gameOverMessageLine2 = "Press R to Replay";
		String gameOverMessageLine3 = "Press ESC to Exit";

		// Calculate layout for each line
		layout.setText(font, gameOverMessageLine1);
		float textWidth1 = layout.width;
		float textHeight1 = layout.height;

		layout.setText(font, gameOverMessageLine2);
		float textWidth2 = layout.width;
		float textHeight2 = layout.height;

		layout.setText(font, gameOverMessageLine3);
		float textWidth3 = layout.width;
		float textHeight3 = layout.height;

		// Calculate position for each line
		float x1 = (Gdx.graphics.getWidth() - textWidth1) / 2;
		float y1 = (Gdx.graphics.getHeight() + textHeight2 + textHeight3) / 2 + textHeight1 / 2; // Center vertically
		float x2 = (Gdx.graphics.getWidth() - textWidth2) / 2;
		float y2 = (Gdx.graphics.getHeight() + textHeight3) / 2 - textHeight2 / 2; // Center vertically
		float x3 = (Gdx.graphics.getWidth() - textWidth3) / 2;
		float y3 = (Gdx.graphics.getHeight() - textHeight3) / 2 - textHeight3 / 2; // Center vertically

		// Draw each line separately
		font.draw(spriteBatch, gameOverMessageLine1, x1, y1);
		font.draw(spriteBatch, gameOverMessageLine2, x2, y2);
		font.draw(spriteBatch, gameOverMessageLine3, x3, y3);

		// End sprite batch rendering
		spriteBatch.end();

		// Handle input for replaying or exiting the game
		if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
			// Reset game
			resetGame();
		} else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
			// Exit game
			Gdx.app.exit();
		}
	}

//	private void handleInput() {
//		float speed = 4;
//		float deltaX = 0;
//		float deltaY = 0;
//
//		// Check for movement input
//		if (Gdx.input.isKeyPressed(Input.Keys.W)) {
//			deltaY += speed;
//		}
//		if (Gdx.input.isKeyPressed(Input.Keys.S)) {
//			deltaY -= speed;
//		}
//		if (Gdx.input.isKeyPressed(Input.Keys.A)) {
//			deltaX -= speed;
//		}
//		if (Gdx.input.isKeyPressed(Input.Keys.D)) {
//			deltaX += speed;
//		}
//
//		// Calculate potential new position
//		float newX = player.getPosition().x + deltaX;
//		float newY = player.getPosition().y + deltaY;
//
//		// Check collision with building
//		if (!building.collidesWithPlayer(newX, newY, playerSize)) {
//			player.move(deltaX, deltaY);
//		} else {
//			// If collision detected, stop movement
//			player.stop();
//		}
//
//		// Apple deceleration if no movement keys are pressed
//		if (deltaX == 0 && deltaY == 0) {
//			player.applyDeceleration();
//		}
//	}

	private void handleInput() {
		float delta = Gdx.graphics.getDeltaTime();
		boolean rotateLeft = Gdx.input.isKeyPressed(Input.Keys.A);
		boolean rotateRight = Gdx.input.isKeyPressed(Input.Keys.D);
		boolean thrust = Gdx.input.isKeyPressed(Input.Keys.W);
		boolean reverse = Gdx.input.isKeyPressed(Input.Keys.S);
		float screenWidth = Gdx.graphics.getWidth();
		float screenHeight = Gdx.graphics.getHeight();

		// Rotate the player based on input
		player.rotate(delta, rotateLeft, rotateRight);

		// Move the player forward or apply deceleration based on input
		player.updateVelocity(delta, thrust, reverse);

		// Calculate potential new position
		float newX = player.getPosition().x + player.getDirection().x * player.getVelocity() * delta;
		float newY = player.getPosition().y + player.getDirection().y * player.getVelocity() * delta;

		// Check collision with building
		if (!building.collidesWithPlayer(newX, newY, playerSize)) {
			player.move(newX, newY, screenWidth, screenHeight);
		} else {
			// If collision detected, stop movement
			player.stop();
		}
	}

	@Override
	public void dispose () {
		shapeRenderer.dispose();
	}

	private void resetGame() {
		// Reset player and guard positions
		player.reset(new Vector2(50, 50));
		guard.reset(new Vector2(Gdx.graphics.getWidth() / 2 + 50 + playerSize, Gdx.graphics.getHeight() / 2 - 50 - playerSize));
		// Reset game over state
		gameOver = false;
	}
}