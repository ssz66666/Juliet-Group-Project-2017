package julietgroupproject;

import java.util.LinkedList;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.scene.Node;

public class Brain {
    // Brain is created when the Alien class is instatiated in the simulation.
    //It has a list of the limbs (Nodes) and joints (HingeJoints) so that they can be used later e.g. by the neural network
    public LinkedList<HingeJoint> joints = new LinkedList<HingeJoint>();
    public LinkedList<Node> nodes = new LinkedList<Node>();
    
}
