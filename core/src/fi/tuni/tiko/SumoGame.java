package fi.tuni.tiko;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import javax.swing.Box;

public class SumoGame extends ApplicationAdapter {
	SpriteBatch batch;
	Texture redBodyTexture;
	Texture background;
	OrthographicCamera camera;
	World world;
	Body redBody;

	final static float WORLD_WIDTH  = 12.80f;
	final static float WORLD_HEIGHT = 7.20f;
	private float TIME_STEP = 1 / 60f;
	private double accumulator = 0;
	private float r = 0.8f;

	Box2DDebugRenderer debugRenderer;

	Array<Body> bodies = new Array<Body>();

	@Override
	public void create () {
		debugRenderer = new Box2DDebugRenderer();

		batch = new SpriteBatch();
		background = new Texture("dohyo.png");
		redBodyTexture = new Texture("body_red.png");

		world = new World(new Vector2(0, -9.8f), true);

		createGround();

		redBody = world.createBody(getDefinitionOfBody(WORLD_WIDTH / 2, WORLD_HEIGHT / 2));
		redBody.createFixture(getFixtureDefinition(r));
		redBody.setUserData(redBodyTexture);

		camera = new OrthographicCamera();
		camera.setToOrtho(false, WORLD_WIDTH, WORLD_HEIGHT);
	}

	@Override
	public void render () {
		world.getBodies(bodies);

		batch.setProjectionMatrix(camera.combined);

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			redBody.applyLinearImpulse(new Vector2(0f, 5f),
					redBody.getWorldCenter(), true);
		}

		batch.begin();
		batch.draw(background, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
		batch.draw(redBodyTexture, redBody.getPosition().x - r, redBody.getPosition().y - r,
				r * 2, r * 2);
		batch.end();

		debugRenderer.render(world, camera.combined);
		stepPhysics(Gdx.graphics.getDeltaTime());
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		background.dispose();
	}

	private FixtureDef getFixtureDefinition(float radius) {
		FixtureDef myFixtureDef = new FixtureDef();
		myFixtureDef.density = 0.98f;
		myFixtureDef.restitution = 0.3f;
		myFixtureDef.friction = 0.8f;
		CircleShape circle = new CircleShape();
		circle.setRadius(radius);
		myFixtureDef.shape = circle;
		return myFixtureDef;
	}

	private BodyDef getDefinitionOfBody(float x, float y) {
		BodyDef myBodyDef = new BodyDef();
		myBodyDef.type = BodyDef.BodyType.DynamicBody;
		myBodyDef.position.set(x, y);
		return myBodyDef;
	}

	private void createGround() {
		BodyDef groundBodyDef = new BodyDef();
		groundBodyDef.type = BodyDef.BodyType.StaticBody;
		groundBodyDef.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 9);
		Body groundBody = world.createBody(groundBodyDef);
		PolygonShape groundBox = new PolygonShape();
		groundBox.setAsBox(WORLD_WIDTH / 2, WORLD_HEIGHT / 9);
		groundBody.createFixture(groundBox, 0.0f);
	}

	private void stepPhysics(float deltaTime) {
		float frameTime = deltaTime;

		if (deltaTime > 1f / 4f) {
			frameTime = 1f / 4f;
		}

		accumulator += frameTime;

		while (accumulator >= TIME_STEP) {
			world.step(TIME_STEP, 8 ,3);
			accumulator -= TIME_STEP;
		}
	}
}
