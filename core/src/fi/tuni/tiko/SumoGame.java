package fi.tuni.tiko;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.WeldJoint;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.badlogic.gdx.utils.Array;

public class SumoGame extends ApplicationAdapter {
	SpriteBatch batch;
	Texture background;
	OrthographicCamera camera;
	World world;
	Sumo redSumo;
	Sumo blueSumo;
	WeldJoint heads;

	final static float LEG_SPEED = 250f;
	final static float RETRACT_SPEED = 500f;
	final static float EXTEND_SPEED = 800f;
	final static float WORLD_WIDTH  = 12.80f;
	final static float WORLD_HEIGHT = 7.20f;
	private float TIME_STEP = 1 / 60f;
	private double accumulator = 0;
	private boolean gameRunning = false;

	@Override
	public void create () {
		batch = new SpriteBatch();
		background = new Texture("dohyo.png");

		world = new World(new Vector2(0, -9.8f), true);
		world.setContactListener(new ContactListener() {
			@Override
			public void beginContact(Contact contact) {
				Body bodyA = contact.getFixtureA().getBody();
				Body bodyB = contact.getFixtureB().getBody();
				String typeA;
				String typeB;

				if (bodyA.getUserData() != null && bodyB.getUserData() != null) {
					typeA = bodyA.getUserData().toString();
					typeB = bodyB.getUserData().toString();

					if ((typeA == "ground" && typeB == "red")
							|| (typeA == "red" && typeB == "ground")) {
						if (gameRunning) {
							System.out.println("BLUE PLAYER WINS MATCH!");
							gameRunning = false;
							endMatch();
						}
					} else if ((typeA == "ground" && typeB == "blue")
							|| (typeA == "blue" && typeB == "ground")) {
						if (gameRunning) {
							System.out.println("RED PLAYER WINS MATCH!");
							gameRunning = false;
							endMatch();
						}
					}
				}
			}

			@Override
			public void endContact(Contact contact) {

			}

			@Override
			public void preSolve(Contact contact, Manifold oldManifold) {

			}

			@Override
			public void postSolve(Contact contact, ContactImpulse impulse) {

			}
		});
		createGround();

		redSumo = new Sumo();
		blueSumo = new Sumo();
		redSumo.create(world, 1);
		blueSumo.create(world, 2);

		heads = (WeldJoint)world.createJoint(getHeadsDef());

		camera = new OrthographicCamera();
		camera.setToOrtho(false, WORLD_WIDTH, WORLD_HEIGHT);
		gameRunning = true;
	}

	@Override
	public void render () {
		float delta = Gdx.graphics.getDeltaTime();

		batch.setProjectionMatrix(camera.combined);

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		if (gameRunning) {
			getInput(delta);
		}

		batch.begin();
		batch.draw(background, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
		redSumo.draw(batch);
		blueSumo.draw(batch);
		batch.end();

		stepPhysics(delta);
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		background.dispose();
		redSumo.dispose();
		blueSumo.dispose();
		world.dispose();
	}

	public void endMatch() {
		world.clearForces();
		// world.destroyJoint(heads);

	}

	private void getInput(float delta) {
		if (Gdx.input.isKeyPressed(Input.Keys.A)) {
			redSumo.hip.setMotorSpeed(-LEG_SPEED * delta);
		} else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
			redSumo.hip.setMotorSpeed(LEG_SPEED * delta);
		} else {
			redSumo.hip.setMotorSpeed(0);
		}

		if (Gdx.input.isKeyPressed(Input.Keys.S)) {
			redSumo.knee.setMotorSpeed(-RETRACT_SPEED * delta);
		} else if (!Gdx.input.isKeyPressed(Input.Keys.S)) {
			redSumo.knee.setMotorSpeed(EXTEND_SPEED * delta);
		}

		if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
			blueSumo.hip.setMotorSpeed(-LEG_SPEED * delta);
		} else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
			blueSumo.hip.setMotorSpeed(LEG_SPEED * delta);
		} else {
			blueSumo.hip.setMotorSpeed(0);
		}

		if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
			blueSumo.knee.setMotorSpeed(RETRACT_SPEED * delta);
		} else if (!Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
			blueSumo.knee.setMotorSpeed(-EXTEND_SPEED * delta);
		}
	}

	private void createGround() {
		BodyDef groundBodyDef = new BodyDef();
		BodyDef leftBodyDef = new BodyDef();
		BodyDef rightBodyDef = new BodyDef();

		groundBodyDef.type = BodyDef.BodyType.StaticBody;
		leftBodyDef.type = BodyDef.BodyType.StaticBody;
		rightBodyDef.type = BodyDef.BodyType.StaticBody;

		groundBodyDef.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 10);
		leftBodyDef.position.set(-1f, WORLD_HEIGHT / 2);
		rightBodyDef.position.set(WORLD_WIDTH + 1f, WORLD_HEIGHT / 2);

		Body groundBody = world.createBody(groundBodyDef);
		Body leftBody = world.createBody(leftBodyDef);
		Body rightBody = world.createBody(rightBodyDef);

		PolygonShape groundBox = new PolygonShape();
		PolygonShape edgeBox = new PolygonShape();
		groundBox.setAsBox(WORLD_WIDTH, WORLD_HEIGHT / 10);
		edgeBox.setAsBox(1, WORLD_HEIGHT);
		groundBody.createFixture(groundBox, 0.0f);
		leftBody.createFixture(edgeBox, 0.0f);
		rightBody.createFixture(edgeBox, 0.0f);
		groundBody.setUserData("ground");
		leftBody.setUserData("ground");
		rightBody.setUserData("ground");
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

	private WeldJointDef getHeadsDef() {
		WeldJointDef def = new WeldJointDef();
		def.collideConnected = false;
		def.initialize(blueSumo.head, redSumo.head, new Vector2(
				WORLD_WIDTH / 2, 5f));
		return def;
	}
}
