/**
 *  IMAS base code for the practical work.
 *  Copyright (C) 2014 DEIM - URV
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cat.urv.imas.agent;

import cat.urv.imas.behaviour.system.NewGarbageBehaviour;
import cat.urv.imas.onthology.InitialGameSettings;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.gui.GraphicInterface;
import cat.urv.imas.behaviour.system.RequestResponseBehaviour;
import cat.urv.imas.behaviour.system.SendNewStep;
import cat.urv.imas.map.Cell;
import cat.urv.imas.onthology.MessageContent;
import cat.urv.imas.onthology.MessageWrapper;
import jade.core.*;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.*;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * System agent that controls the GUI and loads initial configuration settings.
 * TODO: You have to decide the onthology and protocol when interacting among
 * the Coordinator agent.
 */
public class SystemAgent extends ImasAgent {

    /**
     * GUI with the map, system agent log and statistics.
     */
    private GraphicInterface gui;
    /**
     * Game settings. At the very beginning, it will contain the loaded initial
     * configuration settings.
     */
    private GameSettings game;
    /**
     * The Coordinator agent with which interacts sharing game settings every
     * round.
     */
    private AID coordinatorAgent;
    private Random random;

    /**
     * Builds the System agent.
     */
    public SystemAgent() {
        super(AgentType.SYSTEM);
    }

    /**
     * A message is shown in the log area of the GUI, as well as in the stantard
     * output.
     *
     * @param log String to show
     */
    @Override
    public void log(String log) {
        if (gui != null) {
            gui.log(getLocalName() + ": " + log + "\n");
        }
        super.log(log);
    }

    /**
     * An error message is shown in the log area of the GUI, as well as in the
     * error output.
     *
     * @param error Error to show
     */
    @Override
    public void errorLog(String error) {
        if (gui != null) {
            gui.log("ERROR: " + getLocalName() + ": " + error + "\n");
        }
        super.errorLog(error);
    }

    /**
     * Gets the game settings.
     *
     * @return game settings.
     */
    public GameSettings getGame() {
        return this.game;
    }

    /**
     * Gets the random object instanciated with seed from the game settings.
     *
     * @return random.
     */    
    public Random getRandom(){
        return this.random;
    }
    
    /**
     * Agent setup method - called when it first come on-line. Configuration of
     * language to use, ontology and initialization of behaviours.
     */
    @Override
    protected void setup() {

        /* ** Very Important Line (VIL) ************************************* */
        this.setEnabledO2ACommunication(true, 1);

        // 1. Register the agent to the DF
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType(AgentType.SYSTEM.toString());
        sd1.setName(getLocalName());
        sd1.setOwnership(OWNER);

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.addServices(sd1);
        dfd.setName(getAID());
        try {
            DFService.register(this, dfd);
            log("Registered to the DF");
        } catch (FIPAException e) {
            System.err.println(getLocalName() + " failed registration to DF [ko]. Reason: " + e.getMessage());
            doDelete();
        }

        // 2. Load game settings.
        this.game = InitialGameSettings.load("game.settings");
        this.game.setCurrentSimulationSteps(1);
        log("Initial configuration settings loaded");
        
        // Instantiate random object with seed from the game settings
        this.random = new Random((long) this.game.getSeed());

        // this behaviour has to be initiated before other agents
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchProtocol(InteractionProtocol.FIPA_REQUEST), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
        this.addBehaviour(new RequestResponseBehaviour(this, mt));

        ContainerController cc = this.getContainerController();

        // 3. Load GUI
        try {
            AgentController agentController = cc.createNewAgent("Coordinator Agent", CoordinatorAgent.class.getName(), new Object[] {game});
            agentController.start();
            
            // search CoordinatorAgent
            // searchAgent is a blocking method, so we will obtain always a correct AID        
            ServiceDescription searchCriterion = new ServiceDescription();
            searchCriterion.setType(AgentType.COORDINATOR.toString());
            this.coordinatorAgent = UtilsAgents.searchAgent(this, searchCriterion);
            
            agentController = cc.createNewAgent("Harvester Coordinator Agent", HarvesterCoordinatorAgent.class.getName(), new Object[] {game});
            agentController.start();
            agentController = cc.createNewAgent("Scout Coordinator Agent", ScoutCoordinatorAgent.class.getName(), new Object[] {game} );
            agentController.start();
            for (Map.Entry<AgentType,List<Cell>> entry : game.getAgentList().entrySet()) {
                String currentKey = entry.getKey().getShortString();
                int i = 1;
                if (currentKey.equals(AgentType.HARVESTER.getShortString())) {
                    for (Cell cell : entry.getValue()) {
                        Object[] params = {cell, this.game.getAllowedGarbageTypePerHarvester()[i - 1], game};
                        agentController = cc.createNewAgent(entry.getKey().getShortString() + i, HarvesterAgent.class.getName(), params);
                        agentController.start();
                        i++;
                    }
                } else if (currentKey.equals(AgentType.SCOUT.getShortString())) {
                    for (Cell cell : entry.getValue()) {
                        Object[] params = {cell, game};
                        agentController = cc.createNewAgent(entry.getKey().getShortString() + i, ScoutAgent.class.getName(), params);
                        agentController.start();
                        i++;
                    }
                } else {
                    // nunca entra
                    log(entry.getValue().toString());
                }
            
            }
            this.gui = new GraphicInterface(game);
            gui.setVisible(true);
            log("GUI loaded");

        // add initial behaviours            
        SequentialBehaviour init = new SequentialBehaviour(this);
        init.addSubBehaviour(new NewGarbageBehaviour(this, 1.0f));
        init.addSubBehaviour(new SendNewStep(this, this.createNewStep()));
        this.addBehaviour(init);
            
        } catch (Exception e) {
            e.printStackTrace();
        }

        // search CoordinatorAgent
//        ServiceDescription searchCriterion = new ServiceDescription();
//        searchCriterion.setType(AgentType.COORDINATOR.toString());
//        this.coordinatorAgent = UtilsAgents.searchAgent(this, searchCriterion);
        // searchAgent is a blocking method, so we will obtain always a correct AID

        // add behaviours
        // we wait for the initialization of the game
//        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchProtocol(InteractionProtocol.FIPA_REQUEST), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
//
//        this.addBehaviour(new RequestResponseBehaviour(this, mt));

        // Setup finished. When the last inform is received, the agent itself will add
        // a behaviour to send/receive actions
        
        
    
    
    
    
    
        //} 
    }

    public void updateGUI() {
        this.gui.updateGame();
    }

    public void showStats (String stats) {
        this.gui.showStatistics(stats);
    }
    
    public ACLMessage createNewStep() {
        ACLMessage newStepRequest = new ACLMessage(ACLMessage.REQUEST);
        newStepRequest.clearAllReceiver();
        newStepRequest.addReceiver(this.coordinatorAgent);
        newStepRequest.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);        
     
        try {
            MessageWrapper wrapper = new MessageWrapper();
            wrapper.setType(MessageContent.NEW_STEP);
            newStepRequest.setContentObject(wrapper);

            log("Start simulation step: " + this.game.getCurrentSimulationSteps());
        } catch (Exception e) {
            e.printStackTrace();
        }
       
        return newStepRequest;
    }    
    
    
}
