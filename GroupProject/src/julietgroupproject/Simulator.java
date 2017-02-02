package julietgroupproject;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.ConeJoint;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.bullet.joints.PhysicsJoint;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;

public class Simulator extends SimpleApplication implements ActionListener {

    private BulletAppState bulletAppState = new BulletAppState();
    private Node ragDoll = new Node();
    private Node shoulders;
    private Vector3f upforce = new Vector3f(0, 200, 0);
    private boolean applyForce = false;
    float limbPower = 0.4f;
    float limbTargetVolcity = 3f;
    Brain brainToControl;
    // Used for automatically giving limbs keys
    int[] jointKeys = {
        KeyInput.KEY_T, KeyInput.KEY_Y,
        KeyInput.KEY_U, KeyInput.KEY_I,
        KeyInput.KEY_G, KeyInput.KEY_H,
        KeyInput.KEY_J, KeyInput.KEY_K,
        KeyInput.KEY_V, KeyInput.KEY_B,
        KeyInput.KEY_N, KeyInput.KEY_M};

    @Override
    public void simpleInitApp() {
        // Application start code

        // Setup Physics
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        bulletAppState.setDebugEnabled(true);
        createPhysicsTestWorld(rootNode, assetManager, bulletAppState.getPhysicsSpace());
        createRagDoll();

        // Create an example of an alien (what the editor will do)
        Block rootBlock = new Block(new Vector3f( 0.0f, 0.0f, 0.0f), new Vector3f( 0.0f, 0.0f, 0.0f), 0.8f, 0.5f, 0.7f,"Capsule","Normal");
        Block legLeft   = new Block(new Vector3f(-2.6f, 0.0f, 0.0f), new Vector3f(-1.3f, 0.0f, 0.0f), 1.1f, 0.1f, 0.6f,"Box","Normal");
        Block legRight  = new Block(new Vector3f( 2.6f, 0.0f, 0.0f), new Vector3f( 1.3f, 0.0f, 0.0f), 1.1f, 0.1f, 0.6f,"Box","Normal");
        Block legRight2 = new Block(new Vector3f( 1.9f, 0.0f, 0.0f), new Vector3f( 1.0f, 0.0f, 0.0f), 0.6f, 0.1f, 0.6f,"Box","Normal");
        Block legRight3 = new Block(new Vector3f( 0.0f, 0.0f, 1.6f), new Vector3f( 0.0f, 0.0f, 0.8f), 0.6f, 0.1f, 0.6f,"Box","Xaxis");
        
        rootBlock.addLimb(legRight);
        rootBlock.addLimb(legLeft);
        //legRight.addLimb(legRight3);
        
        Alien alien = new Alien(rootBlock);

        // Create that alien in the simulation, with the Brain interface used to control it.
        Brain brain = initAlien(alien, new Vector3f(0f, 5f, -10f));

        // Control the instantiated alien (what the neural network will do)
        initKeys(brain);
    }

    public void initKeys(Brain brain) {
        // Creates keyboard bindings for the joints with keys in jointKeys, limited by umber of joints or keys specified in jointKeys
        int numberOfJoints = Math.min(brain.joints.size(), jointKeys.length / 2);
        for (int i = 0; i < numberOfJoints; i++) {
            inputManager.addMapping("Alien joint " + ((Integer) i).toString() + " clockwise", new KeyTrigger(jointKeys[2 * i]));
            inputManager.addMapping("Alien joint " + ((Integer) i).toString() + " anticlockwise", new KeyTrigger(jointKeys[2 * i + 1]));
            inputManager.addListener(this, "Alien joint " + ((Integer) i).toString() + " clockwise");
            inputManager.addListener(this, "Alien joint " + ((Integer) i).toString() + " anticlockwise");
        }
        brainToControl = brain;
        inputManager.addMapping("Pull ragdoll up", new MouseButtonTrigger(0));
        inputManager.addListener(this, "Pull ragdoll up");
    }

    public Brain initAlien(Alien alien, Vector3f pos) {

        Brain brain = new Brain();

        Block rootBlock = alien.rootBlock;
        Node rootNode = createLimb(rootBlock.collisionShapeType,rootBlock.width, rootBlock.height, rootBlock.length, pos, true);

        brain.nodes.add(rootNode);

        recursivelyAddBlocks(rootBlock, rootNode, rootBlock, rootNode, brain);

        bulletAppState.getPhysicsSpace().addAll(rootNode);

        return brain;
    }

    public void recursivelyAddBlocks(Block rootBlock, Node rootNode, Block parentBlock, Node parentNode, Brain brain) {
        for (Block b : parentBlock.getConnectedLimbs()) {
            //Node n = createLimb(b.collisionShapeType,b.width, b.height, b.length, parentNode.getLocalTranslation().add(b.getPosition()), true);
            //HingeJoint joint = joinHingeJoint(parentNode, n, parentNode.getLocalTranslation().add(b.getHingePosition()), Vector3f.UNIT_Z, Vector3f.UNIT_Z,b.hingeType);
            Node n = createLimb(b.collisionShapeType,b.width, b.height, b.length, parentNode.getWorldTranslation().add(b.getPosition()), true);
            HingeJoint joint = joinHingeJoint(parentNode, n, parentNode.getWorldTranslation().add(b.getHingePosition()), b.hingeType);
            rootNode.attachChild(n);
            brain.joints.add(joint);
            brain.nodes.add(n);
            recursivelyAddBlocks(rootBlock,rootNode,b,n,brain);
        }
    }

    private Node createLimb(String collisionType, float width, float height, float length,Vector3f location, boolean rotate) {
        int axis = rotate ? PhysicsSpace.AXIS_X : PhysicsSpace.AXIS_Y;
        if (collisionType.equals("Capsule")){
            CapsuleCollisionShape shape = new CapsuleCollisionShape(width, height, axis);
            RigidBodyControl rigidBodyControl = new RigidBodyControl(shape, 1);
            Node node = new Node("Limb");
            node.setLocalTranslation(location);
            node.addControl(rigidBodyControl);
            return node;
        } else {
            BoxCollisionShape shape = new BoxCollisionShape(new Vector3f(width,height,length));
            RigidBodyControl rigidBodyControl = new RigidBodyControl(shape, 1);
            Node node = new Node("Limb");
            node.setLocalTranslation(location);
            node.addControl(rigidBodyControl);
            return node;
        }
    }

    private HingeJoint joinHingeJoint(Node A, Node B, Vector3f connectionPoint, String hingeType) {
        Vector3f pivotA = A.worldToLocal(connectionPoint, new Vector3f());
        Vector3f pivotB = B.worldToLocal(connectionPoint, new Vector3f());
        Vector3f axisA;
        Vector3f axisB;
        if(hingeType.equals("XAxis")){
            axisA = Vector3f.UNIT_X;
            axisB = Vector3f.UNIT_X;
            //joint.setLimit(0f, 0f, 0f,0.2f,0.5f);
        } else {
            axisA = Vector3f.UNIT_Z;
            axisB = Vector3f.UNIT_Z;
        }
        HingeJoint joint = new HingeJoint(A.getControl(RigidBodyControl.class), B.getControl(RigidBodyControl.class), pivotA, pivotB, axisA, axisB);
        
        return joint;
    }

    public void onAction(String string, boolean bln, float tpf) {
        
        // Controls the joints with keys in jointKeys
        if (brainToControl != null) {
            int numberOfJoints = Math.min(brainToControl.joints.size(), jointKeys.length / 2);
            
            for (int i = 0; i < numberOfJoints; i++) {
                if (("Alien joint " + ((Integer) i).toString() + " clockwise").equals(string)) {
                    if (bln) {
                        brainToControl.joints.get(i).enableMotor(true, 1 * limbTargetVolcity, limbPower);
                        System.out.println("G1");
                    } else {
                        brainToControl.joints.get(i).enableMotor(false, 0, 0);
                        System.out.println("G2");
                    }
                }
                if (("Alien joint " + ((Integer) i).toString() + " anticlockwise").equals(string)) {
                    if (bln) {
                        brainToControl.joints.get(i).enableMotor(true, -1 * limbTargetVolcity, limbPower);
                        System.out.println("J1");
                    } else {
                        brainToControl.joints.get(i).enableMotor(false, 0, 0);
                        System.out.println("J2");
                    }
                }
            }
        }
        if ("Pull ragdoll up".equals(string)) {
            if (bln) {
                shoulders.getControl(RigidBodyControl.class).activate();
                applyForce = true;

            } else {
                applyForce = false;
            }
        }
    }

    public Vector3f getPosition(Node node) {
        return node.getControl(RigidBodyControl.class).getPhysicsLocation();
    }

    private void createRagDoll() {
        shoulders  = createLimb("Capsule",0.2f, 1.0f,1, new Vector3f(0.00f, 1.5f, 10), true);
        Node uArmL = createLimb("Capsule",0.2f, 0.5f,1, new Vector3f(-0.75f, 0.8f, 10), false);
        Node uArmR = createLimb("Capsule",0.2f, 0.5f,1, new Vector3f(0.75f, 0.8f, 10), false);
        Node lArmL = createLimb("Capsule",0.2f, 0.5f,1, new Vector3f(-0.75f, -0.2f, 10), false);
        Node lArmR = createLimb("Capsule",0.2f, 0.5f,1, new Vector3f(0.75f, -0.2f, 10), false);
        Node body  = createLimb("Capsule",0.2f, 1.0f,1, new Vector3f(0.00f, 0.5f, 10), false);
        Node hips  = createLimb("Capsule",0.2f, 0.5f,1, new Vector3f(0.00f, -0.5f, 10), true);
        Node uLegL = createLimb("Capsule",0.2f, 0.5f,1, new Vector3f(-0.25f, -1.2f, 10), false);
        Node uLegR = createLimb("Capsule",0.2f, 0.5f,1, new Vector3f(0.25f, -1.2f, 10), false);
        Node lLegL = createLimb("Capsule",0.2f, 0.5f,1, new Vector3f(-0.25f, -2.2f, 10), false);
        Node lLegR = createLimb("Capsule",0.2f, 0.5f,1, new Vector3f(0.25f, -2.2f, 10), false);

        join(body, shoulders, new Vector3f(0f, 1.4f, 10));
        join(body, hips, new Vector3f(0f, -0.5f, 10));

        join(uArmL, shoulders, new Vector3f(-0.75f, 1.4f, 10));
        join(uArmR, shoulders, new Vector3f(0.75f, 1.4f, 10));
        join(uArmL, lArmL, new Vector3f(-0.75f, .4f, 10));
        join(uArmR, lArmR, new Vector3f(0.75f, .4f, 10));

        join(uLegL, hips, new Vector3f(-.25f, -0.5f, 10));
        join(uLegR, hips, new Vector3f(.25f, -0.5f, 10));
        join(uLegL, lLegL, new Vector3f(-.25f, -1.7f, 10));
        join(uLegR, lLegR, new Vector3f(.25f, -1.7f, 10));

        ragDoll.attachChild(shoulders);
        ragDoll.attachChild(body);
        ragDoll.attachChild(hips);
        ragDoll.attachChild(uArmL);
        ragDoll.attachChild(uArmR);
        ragDoll.attachChild(lArmL);
        ragDoll.attachChild(lArmR);
        ragDoll.attachChild(uLegL);
        ragDoll.attachChild(uLegR);
        ragDoll.attachChild(lLegL);
        ragDoll.attachChild(lLegR);

        rootNode.attachChild(ragDoll);
        bulletAppState.getPhysicsSpace().addAll(ragDoll);
    }

    private PhysicsJoint join(Node A, Node B, Vector3f connectionPoint) {
        Vector3f pivotA = A.worldToLocal(connectionPoint, new Vector3f());
        Vector3f pivotB = B.worldToLocal(connectionPoint, new Vector3f());
        ConeJoint joint = new ConeJoint(A.getControl(RigidBodyControl.class), B.getControl(RigidBodyControl.class), pivotA, pivotB);
        joint.setLimit(1f, 1f, 0);
        return joint;
    }

    public static void createPhysicsTestWorld(Node rootNode, AssetManager assetManager, PhysicsSpace space) {
        AmbientLight light = new AmbientLight();
        light.setColor(ColorRGBA.LightGray);
        rootNode.addLight(light);

        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setTexture("ColorMap", assetManager.loadTexture("Interface/Logo/Monkey.jpg"));

        Box floorBox = new Box(140, 0.25f, 140);
        Geometry floorGeometry = new Geometry("Floor", floorBox);
        floorGeometry.setMaterial(material);
        floorGeometry.setLocalTranslation(0, -5, 0);
//        Plane plane = new Plane();
//        plane.setOriginNormal(new Vector3f(0, 0.25f, 0), Vector3f.UNIT_Y);
//        floorGeometry.addControl(new RigidBodyControl(new PlaneCollisionShape(plane), 0));
        floorGeometry.addControl(new RigidBodyControl(0));
        rootNode.attachChild(floorGeometry);
        space.add(floorGeometry);
        /*
        //movable boxes
        for (int i = 0; i < 12; i++) {
            Box box = new Box(0.25f, 0.25f, 0.25f);
            Geometry boxGeometry = new Geometry("Box", box);
            boxGeometry.setMaterial(material);
            boxGeometry.setLocalTranslation(i, 5, -3);
            //RigidBodyControl automatically uses box collision shapes when attached to single geometry with box mesh
            boxGeometry.addControl(new RigidBodyControl(2));
            rootNode.attachChild(boxGeometry);
            space.add(boxGeometry);
        }
        
        //immovable sphere with mesh collision shape
        Sphere sphere = new Sphere(8, 8, 1);
        Geometry sphereGeometry = new Geometry("Sphere", sphere);
        sphereGeometry.setMaterial(material);
        sphereGeometry.setLocalTranslation(4, -4, 2);
        sphereGeometry.addControl(new RigidBodyControl(new MeshCollisionShape(sphere), 0));
        rootNode.attachChild(sphereGeometry);
        space.add(sphereGeometry);
        */
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (applyForce) {
            shoulders.getControl(RigidBodyControl.class).applyForce(upforce, Vector3f.ZERO);
        }
    }

    public static void main(String[] args) {
        Simulator app = new Simulator();
        app.start();
    }
}
