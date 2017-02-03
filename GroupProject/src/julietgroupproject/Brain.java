package julietgroupproject;

import com.jme3.bullet.control.RigidBodyControl;
import java.util.ArrayList;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

public class Brain {
    // Brain is created when the Alien class is instatiated in the simulation.
    //It has a list of the limbs (Nodes) and joints (HingeJoints) so that they can be used later e.g. by the neural network
    public ArrayList<HingeJoint> joints = new ArrayList<HingeJoint>();
    public ArrayList<Node> nodes = new ArrayList<Node>();
    
    public void still() {
        for(Node n : nodes){
            n.getControl(RigidBodyControl.class).setAngularVelocity(Vector3f.ZERO);
        }
    }
}
