/**
 * IMAS base code for the practical work. 
 * Copyright (C) 2014 DEIM - URV
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cat.urv.imas.onthology;

import cat.urv.imas.agent.AgentType;
import cat.urv.imas.map.BuildingCell;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;
import cat.urv.imas.map.StreetCell;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Current game settings. Cell coordinates are zero based: row and column values
 * goes from [0..n-1], both included.
 * 
 * Use the GenerateGameSettings to build the game.settings configuration file.
 * 
 */
@XmlRootElement(name = "GameSettings")
public class GameSettings implements java.io.Serializable {    

    /* Default values set to all attributes, just in case. */
    /**
     * Seed for random numbers.
     */
    private float seed = 0.0f;
    /**
     * Price for recycling on recycling centers, for plastic, glass and paper. 
     * Therefore, a value "{{1, 2, 3}}" means there will be a single recycling
     * center and it will pay 1 coin for plastic, 2 coins for glass and
     * 3 coins for paper. If there is a 0 coin at some point, it means
     * there is no recycling process for that kind of garbage.
     */
    private int[][] recyclingCenterPrices = {
        {9, 10, 0},
        {10, 0, 9},
        {0, 9, 10},
    };
    /**
     * Total number of simulation steps.
     */
    private int simulationSteps = 100;
    /**
     * City map.
     */
    protected Cell[][] map;
    /**
     * From 0 to 100 (meaning percentage) of probability of having new
     * garbage in the city at every step.
     */
    protected int newGarbageProbability = 10;
    /**
     * If there is new garbage in a certain simulation step, this number
     * represents the maximum number of buildings affected by garbage.
     */
    protected int maxNumberBuildingWithNewGargabe = 5;
    /**
     * For each building with new garbage, this number represents the maximum
     * amount of new garbage that can appear.
     */
    protected int maxAmountOfNewGargabe = 5;
    /**
     * All harvesters will have this capacity of garbage units.
     */
    protected int harvestersCapacity = 6;
    /**
     * In order of appearance of the harvesters, this is the list of garbage
     * types suported by each harvester.
     */
    protected GarbageType[][] allowedGarbageTypePerHarvester;
    /**
     * Computed summary of the position of agents in the city. For each given
     * type of mobile agent, we get the list of their positions.
     */
    protected Map<AgentType, List<Cell>> agentList;
    /**
     * Title to set to the GUI.
     */
    protected String title = "Demo title";
    private int currentSimulationSteps;
    

    public float getSeed() {
        return seed;
    }

    @XmlElement(required = true)
    public void setSeed(float seed) {
        this.seed = seed;
    }

    public int[][] getRecyclingCenterPrices() {
        return recyclingCenterPrices;
    }

    @XmlElement(required = true)
    public void setRecyclingCenterPrices(int[][] prices) {
        this.recyclingCenterPrices = prices;
        int check = 0; // if 7, all garbage types are treated.
        for (int i=0; i < prices.length; i++) {
            for (int j=0; j < 3; j++) {
                if (prices[i][j] != 0) {
                    check |= 1 << j;
                }
            }
        }
        if (check != 7) {
            throw new Error(getClass().getCanonicalName() + " : Not all garbage types are treated in this map.");
        }
    }
    
    public ArrayList<Cell> getAdjacentCells(StreetCell cell) {
        ArrayList<Cell> adjacentCells = new ArrayList<>();

        for (int i = cell.getCol()-1; i <= cell.getCol()+1; i++)
            for (int j = cell.getRow()-1; j <= cell.getRow()+1; j++)
                if((i >= 0 && i < this.getMap()[0].length) && (j >= 0 && j < this.getMap().length)&& (j != cell.getRow() || i != cell.getCol()))
                    adjacentCells.add(this.get(j, i));
        return adjacentCells;
    }

    public int getSimulationSteps() {
        return simulationSteps;
    }

    @XmlElement(required = true)
    public void setSimulationSteps(int simulationSteps) {
        this.simulationSteps = simulationSteps;
    }

    public int getCurrentSimulationSteps() {
        return currentSimulationSteps;
    }

    @XmlElement(required = true)
    public void setCurrentSimulationSteps(int simulationSteps) {
        this.currentSimulationSteps = simulationSteps;
    }    
    
    public String getTitle() {
        return title;
    }

    @XmlElement(required=true)
    public void setTitle(String title) {
        this.title = title;
    }
    
    public int getNewGarbageProbability() {
        return newGarbageProbability;
    }

    @XmlElement(required=true)
    public void setNewGarbageProbability(int newGarbageProbability) {
        this.newGarbageProbability = newGarbageProbability;
    }

    public int getMaxNumberBuildingWithNewGargabe() {
        return maxNumberBuildingWithNewGargabe;
    }

    @XmlElement(required=true)
    public void setMaxNumberBuildingWithNewGargabe(int maxNumberBuildingWithNewGargabe) {
        this.maxNumberBuildingWithNewGargabe = maxNumberBuildingWithNewGargabe;
    }

    public int getMaxAmountOfNewGargabe() {
        return maxAmountOfNewGargabe;
    }

    @XmlElement(required=true)
    public void setMaxAmountOfNewGargabe(int maxAmountOfNewGargabe) {
        this.maxAmountOfNewGargabe = maxAmountOfNewGargabe;
    }

    @XmlTransient
    public GarbageType[][] getAllowedGarbageTypePerHarvester() {
        return allowedGarbageTypePerHarvester;
    }

    public int getHarvestersCapacity() {
        return harvestersCapacity;
    }

    @XmlElement(required=true)
    public void setHarvestersCapacity(int harvestersCapacity) {
        this.harvestersCapacity = harvestersCapacity;
    }
    
    /**
     * Gets the full current city map.
     * @return the current city map.
     */
    @XmlTransient
    public Cell[][] getMap() {
        return map;
    }
    
    public ArrayList<BuildingCell> detectBuildingsWithGarbage(int row, int col) {
        //      find all surrounding cells to (row,col) that are
        //      buildings and have garbage on it.
        //      Use: BuildingCell.detectGarbage() to do so.        
        int rows = map.length, cols = map[0].length;
        ArrayList<BuildingCell> buildingsWithGarbage = new ArrayList<>();

        for (int dy = -1 ; dy <= 1 ; ++dy ) {
            for (int dx = -1 ; dx <= 1 ; ++dx ) {
                int y = row + dy, x = col + dx;
                // check bounderies and filter surrounding cells containing a building                
                if (0 <= y && y < rows && 0 <= x && x < cols && 
                    row != y && col != x &&
                    this.get(y, x).getCellType() == CellType.BUILDING ) {
                    
                    BuildingCell building = (BuildingCell) this.get(y, x);
                    // list garbage that was previously undetected (building 
                    // declared as empty) but after detecting now it is found
                    // TODO: no estoy seguro si esta deberia ser la condicion
                    if (building.getGarbage().isEmpty() &&
                        !building.detectGarbage().isEmpty() ) {
                        buildingsWithGarbage.add(building);
                    }
                }
            }
        }

        return buildingsWithGarbage;
    }
    
    /**
     * Gets the cell given its coordinate.
     * @param row row number (zero based)
     * @param col column number (zero based).
     * @return a city's Cell.
     */
    public Cell get(int row, int col) {
        return map[row][col];
    }

    @XmlTransient
    public Map<AgentType, List<Cell>> getAgentList() {
        return agentList;
    }

    public void setAgentList(Map<AgentType, List<Cell>> agentList) {
        this.agentList = agentList;
    }
    
    public void updateAgentList() {
        Map<AgentType, List<Cell>> newAgentList = new HashMap<>();
        newAgentList.put(AgentType.HARVESTER, new ArrayList<Cell>());
        newAgentList.put(AgentType.SCOUT, new ArrayList<Cell>());
        
        int rows = map.length, cols = map[0].length;
        for (int y = 0 ; y < rows ; ++y) {
            for (int x = 0 ; x < cols ; ++x) {
                Cell cell = this.get(y, x);
                if (cell.getCellType() == CellType.STREET)
                {
                    StreetCell street = (StreetCell) cell;
                    if (street.isThereAnAgent()) {
                        newAgentList.get(street.getAgent().getType()).add(cell);
                        System.out.println("Update: " + cell);
                    }
                }
            }
        }
        
        setAgentList(newAgentList);
    }
    
    @Override
    public String toString() {
        //TODO: show a human readable summary of the game settings.
        return "Game settings";
    }
    
    public String getShortString() {
            //TODO: list of agents
        return "Game settings: agent related string";
    }

    public void setGame() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
