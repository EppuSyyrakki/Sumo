package fi.tuni.tiko;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.physics.box2d.joints.WeldJoint;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;



public class Sumo {
    Texture bodyTexture;
    Texture headTexture;
    Texture legTexture;
    Texture footTexture;
    Sprite bodySprite;
    Sprite headSprite;
    Sprite legSprite;
    Sprite footSprite;
    Body body;
    Body head;
    Body leg;
    Body foot;
    WeldJoint neck;
    RevoluteJoint hip;
    RevoluteJoint knee;

    final static float WORLD_WIDTH  = 12.80f;
    final static float WORLD_HEIGHT = 7.20f;
    final float bodyRadius = 0.8f;
    final float headRadius = 0.3f;
    final float legHeight = 0.5f;
    final float legWidth = 0.3f;
    final float footHeight = 0.5f;
    final float footWidth = footHeight / 2;
    float upperKnee;
    float lowerKnee;
    int player;


    public World create(World world, int player) {
        float bodyX;
        float bodyY = 5f;
        float headX;
        float headY;
        float legX;
        float legY;
        float footX;
        float footY;
        this.player = player;

        if (player == 1) {
            bodyX = WORLD_WIDTH / 2 - 1f;
            headX = bodyX + bodyRadius * 0.75f;
            headY = bodyY + bodyRadius;
            legX = bodyX - legWidth / 2;
            legY = bodyY - bodyRadius - legHeight;
            footX = legX;
            footY = legY - legHeight / 2;
            bodyTexture = new Texture("body_red.png");
            headTexture = new Texture("head_red.png");
            legTexture = new Texture("leg_red.png");
            footTexture = new Texture("foot_red.png");
            upperKnee = 0;
            lowerKnee = -120;
        } else {
            bodyX = WORLD_WIDTH / 2 + 1f;
            headX = bodyX - bodyRadius * 0.75f;
            headY = bodyY + bodyRadius;
            legX = bodyX + legWidth / 2;
            legY = bodyY - bodyRadius - legHeight;
            footX = legX;
            footY = legY - legHeight / 2;
            bodyTexture = new Texture("body_blue.png");
            headTexture = new Texture("head_blue.png");
            legTexture = new Texture("leg_blue.png");
            footTexture = new Texture("foot_blue.png");
            upperKnee = 120;
            lowerKnee = 0;
        }
        body = world.createBody(getDefinitionOfBody(bodyX, bodyY));
        head = world.createBody(getDefinitionOfBody(headX, headY));
        leg = world.createBody(getDefinitionOfBody(legX, legY));
        foot = world.createBody(getDefinitionOfBody(footX, footY));
        body.createFixture(getCircleFixtureDef(bodyRadius));
        head.createFixture(getCircleFixtureDef(headRadius));
        leg.createFixture(getBoxFixtureDef(legWidth, legHeight));
        foot.createFixture(getBoxFixtureDef(footWidth, footHeight));
        neck = (WeldJoint)world.createJoint(getNeckDef(headX,headY));
        hip = (RevoluteJoint)world.createJoint(getHipDef());
        knee = (RevoluteJoint)world.createJoint(getKneeDef());

        if (player == 1) {
            head.setUserData("red");
            // body.setUserData("red");
        } else if (player == 2) {
            head.setUserData("blue");
            // head.setUserData("blue");
        }
        bodySprite = new Sprite(bodyTexture);
        bodySprite.setOriginCenter();
        bodySprite.setSize(bodyRadius * 2, bodyRadius * 2);
        headSprite = new Sprite(headTexture);
        headSprite.setSize(headRadius * 2, headRadius * 2);
        headSprite.setOriginCenter();
        legSprite = new Sprite(legTexture);
        legSprite.setSize(legWidth * 2, legHeight * 2);
        footSprite = new Sprite(footTexture);
        footSprite.setSize(legWidth * 2, legHeight * 2);
        return world;
    }

    private FixtureDef getBoxFixtureDef(float width, float height) {
        FixtureDef myFixtureDef = new FixtureDef();
        myFixtureDef.density = 0.3f;
        myFixtureDef.restitution = 0.3f;
        myFixtureDef.friction = 0.8f;
        PolygonShape legBox = new PolygonShape();
        legBox.setAsBox(width, height);
        myFixtureDef.shape = legBox;
        return myFixtureDef;
    }

    private FixtureDef getCircleFixtureDef(float radius) {
        FixtureDef myFixtureDef = new FixtureDef();
        myFixtureDef.density = 0.2f;
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

    private WeldJointDef getNeckDef(float x, float y) {
        WeldJointDef def = new WeldJointDef();
        def.collideConnected = false;
        def.initialize(body, head, new Vector2(x, y));
        return def;
    }

    private RevoluteJointDef getHipDef() {
        RevoluteJointDef def = new RevoluteJointDef();
        def.bodyA = body;
        def.bodyB = leg;
        def.collideConnected = false;
        def.localAnchorA.set(0f, -bodyRadius);
        def.localAnchorB.set(0f, legHeight / 2);
        def.referenceAngle = 0;
        def.enableLimit = true;
        def.enableMotor = true;
        def.maxMotorTorque = 100f;
        def.lowerAngle = -90f * MathUtils.degreesToRadians;
        def.upperAngle = 90f * MathUtils.degreesToRadians;
    return def;
    }

    private RevoluteJointDef getKneeDef() {
        RevoluteJointDef def = new RevoluteJointDef();
        def.collideConnected = false;
        def.bodyA = leg;
        def.bodyB = foot;
        def.localAnchorA.set(0f, -0.4f);
        def.localAnchorB.set(0, 0.35f);
        def.referenceAngle = 0;
        def.enableLimit = true;
        def.enableMotor = true;
        def.maxMotorTorque = 100f;
        def.upperAngle = upperKnee * MathUtils.degreesToRadians;
        def.lowerAngle = lowerKnee * MathUtils.degreesToRadians;
        return def;
    }

    public void draw(SpriteBatch batch) {
        headSprite.setOriginCenter();
        headSprite.setCenter(head.getPosition().x, head.getPosition().y);
        headSprite.setRotation(head.getTransform().getRotation() * MathUtils.radiansToDegrees);
        headSprite.draw(batch);

        bodySprite.setOriginCenter();
        bodySprite.setCenter(body.getPosition().x, body.getPosition().y);
        bodySprite.setRotation(body.getTransform().getRotation() * MathUtils.radiansToDegrees);
        bodySprite.draw(batch);

        legSprite.setOriginCenter();
        legSprite.setCenter(leg.getPosition().x, leg.getPosition().y);
        legSprite.setRotation(leg.getTransform().getRotation() * MathUtils.radiansToDegrees);
        legSprite.draw(batch);

        footSprite.setOriginCenter();
        footSprite.setCenter(foot.getPosition().x, foot.getPosition().y);
        footSprite.setRotation(foot.getTransform().getRotation() * MathUtils.radiansToDegrees);
        footSprite.draw(batch);
    }

    public void dispose() {
        headTexture.dispose();
        bodyTexture.dispose();
        legTexture.dispose();
        footTexture.dispose();
    }

}
