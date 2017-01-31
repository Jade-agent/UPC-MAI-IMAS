/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.agent;

import static cat.urv.imas.agent.ImasAgent.OWNER;
import cat.urv.imas.behaviour.scout.ResponseGarbageBehaviour;
import cat.urv.imas.map.BuildingCell;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.StreetCell;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.ArrayList;

/**
 *
 * @author Dario, Angel, Pablo, Emanuel y Daniel
 */
public class ScoutAgent extends ImasAgent{
    
    private StreetCell position;
    /**
     * The Coordinator agent with which interacts sharing game settings every
     * round.
     */
    private AID scoutCoordinatorAgent;
    /**
     * The Coordinator agent with which interacts sharing game settings every
     * round.
     */
    private ArrayList<Cell> adjacentCells;
    /**
     * The Coordinator agent with which interacts sharing game settings every
     * round.
     */
    private ArrayList<BuildingCell> garbageCells;
    
     /**
     * Builds the coordinator agent.
     */
    public ScoutAgent() {
        super(AgentType.SCOUT);
    }

    @Override
    protected void setup() {
        /* ** Very Important Line (VIL) ***************************************/
        this.setEnabledO2ACommunication(true, 1);
        /* ********************************************************************/
        
         // Register the agent to the DF
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType(AgentType.SCOUT.toString());
        sd1.setName(getLocalName());
        sd1.setOwnership(OWNER);
        
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.addServices(sd1);
        dfd.setName(getAID());
        
        try {
            DFService.register(this, dfd);
            log("Registered to the DF");
        } catch (FIPAException e) {
            System.err.println(getLocalName() + " registration with DF unsucceeded. Reason: " + e.getMessage());
            doDelete();
        }
        
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            position = (StreetCell) args[0];
            position.getAgent().setAID(getAID());
            log("At (" + position.getRow() + " , " + position.getCol() + ")");
        } else {
            // Make the agent terminate immediately
            doDelete();
        
        }
        // add behaviours
        // we wait for the initialization of the game
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

        this.addBehaviour(new ResponseGarbageBehaviour(this, mt));
    }

    public ArrayList<Cell> getAdjacentCells() {
        return adjacentCells;
    }

    public void setAdjacentCells(ArrayList<Cell> adjacentCells) {
        this.adjacentCells = adjacentCells;
    }

    public ArrayList<BuildingCell> getGarbageCells() {
        return garbageCells;
    }

    public void setGarbageCells(ArrayList<BuildingCell> garbageCells) {
        this.garbageCells = garbageCells;
    }

    public StreetCell getPosition() {
        return position;
    }

    public void setPosition(StreetCell position) {
        this.position = position;
    }

}
