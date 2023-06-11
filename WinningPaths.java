import java.util.*;

class GameState{
  //states to reach from the current state
  List<GameState> children = new ArrayList<GameState>();
  List<String> moves = new ArrayList<String>();
  int[] bins = new int[45];  //Underlying data structure
  boolean queued = false;
  boolean winningState = false;
  int largestBin;    //Index of the largest (non-empty) bin
  int numTerms = 0;
  int numChildren = 0;
  float numOptions = -1.0f;

  //Constructor
  public GameState(int n){
    bins[0] = n;
    largestBin = 0;
    numTerms = n;
  }

  //Overrise constructor
  public GameState(GameState parent, int[] bins){
    numTerms = parent.numTerms;
    largestBin = parent.largestBin;
    this.bins = bins;
  }
  
  //Returns entry at n from the nextStates list
  public void connectToChild(GameState child){
    children.add(child);
    numChildren++;
  }

  public void addMove(String s, int i){
    String move = s.concat(String.valueOf(i));
    moves.add(move);
  }

  //Returns entry at n from the bins array
  public int getEntry(int n){
    return bins[n];
  }
  
  public boolean equals(GameState otherState){
    return Arrays.equals(bins, otherState.getBins());
  }

  public String getMove(int i){
    return moves.get(i);
  }

  public int[] getBins(){
    return bins;
  }
  
  public List<GameState> getChildren(){
    return children;
  }
  
  public GameState getChild(int i){
    return children.get(i);
  }

  public void removeChild(int i){
    children.remove(i);
    numChildren--;
  }
  
  public float getNumOptions(){
    return numOptions;
  }
  
  public void setNumOptions(float n){
    numOptions = n;
  }

  public int getNumChildren(){
    return numChildren;
  }

  public int getLargestBin(){
    return largestBin;
  }

  public void setLargestBin(){
    largestBin++;
  }

  public boolean getWinningState(){
    return winningState;
  }
  
  public void setQueded(){
    queued = true;
  }
  
  public boolean getQueded(){
    return queued;
  }

  public void setWinningState(){
    winningState = true;
  }

  public int getNumTerms(){
    return numTerms;
  }

  public void setNumTerms(){
    numTerms--;
  }

  public void removeMove(int i){
    moves.remove(i);
  }
}//EOF GameState

public class Main{

  static HashMap<Double, GameState> stateMap = new HashMap<>();
  static Queue<GameState> q = new LinkedList<>();
  static List<GameState> stateList = new ArrayList<>();
  static List<Integer> indexList = new ArrayList<>();
  static int[] pathLength = new int[45];
  static int minTerms = 2147483647;
  static int numPaths = 0;

  static Double getKey(int[] bins){
    Double key = 0.0;
    for(int i = 0; i < 45; i++){
      key += bins[i]*Math.sqrt(i+1);
    }
    return key;
  }//EOF getKey
  
  public static int[] split(int n, int[] bins){
    if(n == 1){
      bins[0]++;
    }
    else{
      bins[n-2]++;
    }
    bins[n+1]++;
    bins[n] += -2;
    return bins;
  }//EOF combine

  public static int[] combine(int n, int[] bins){
    if(n == 0){
      bins[0] += -2;
    }
    else{
      bins[n-1]--;
      bins[n]--;
    }
    bins[n+1]++;
    return bins;
  }//EOF combine

  public static void main(String[] args){
    
    //Reads the user input
    Scanner reader = new Scanner(System.in);
    System.out.printf("Please enter the value you wish to check: ");
    int input = reader.nextInt();
    
    //Checks that the input is valid (i.e, a non-negative integer)
    if(input < 0){
      System.out.println("Invalid input");
      System.exit(1);
    }

    GameState firstState = new GameState(input);
    stateList.add(firstState);
    indexList.add(stateList.size()-1);
    q.add(firstState);

    //Creates the map
    while(q.size() != 0){
      GameState currState = q.remove();
      findChildren(currState);
    }
    
    System.out.println("Number of states: "+stateMap.size());
    indexList.add(stateList.size());
    q.clear();

    //Colors the map
    for(int i = indexList.size()-1; i > 0; i--){
      for(int j = indexList.get(i-1); j < indexList.get(i); j++){
        GameState currState = stateList.get(j);
        for(int k = 0; k < currState.getNumChildren(); k++){
          GameState childState = currState.getChild(k);
          if(childState.getWinningState() == false){
            currState.setWinningState();
            k = currState.getNumChildren();
          }
        }
      }
    }
    
    q.add(firstState);
    
    //Removed the gray edges
    while(q.size() != 0){
      GameState currState = q.remove();
      removeGray(currState);
    }

    List<GameState> winningStates = new ArrayList<GameState>();
    List<String> moves = new ArrayList<String>();
    winningStates.add(firstState);
    
    System.out.printf("Enter 2 to see number of paths, 1 to see the states, enter 0 to see the moves: ");
    int response = reader.nextInt();
    
    if(response == 2){
      GameState lastState = stateList.get(indexList.get(indexList.size()-2));
      lastState.setNumOptions(1.0f);
      float answer = pathNums(firstState);
      System.out.println((int)answer);
    }
    else{
      getPaths(winningStates, moves, response);
    }
    
    System.out.println("\nPath length : number of occurences");
    for(int i = 0; i < 45; ++i){
      if(pathLength[i] > 0){
        System.out.println("\t "+i+" : "+pathLength[i]);
      }
    }
    
    reader.close();
    
  }//EOF main

  static void findChildren(GameState parent){
    
    //Split largest first
    for(int i = 44; i > 0; i--){        
      if(parent.getEntry(i) >= 2){
        addChild(parent, 'S', i);
        parent.addMove("S", i);
      }
    }
    
    //Combine largest second
    for(int i = 44; i > 0; i--){
      if(parent.getEntry(i) >= 1 && parent.getEntry(i-1) >= 1){
        addChild(parent, 'C', i);
        parent.addMove("C", i);
      }
    }
    
    //Combine 1's third
    if(parent.getEntry(0) >= 2){
      addChild(parent, 'C', 0);
      parent.addMove("C", 0);
    }

  }//EOF findWinningChildren

  static void addChild(GameState parent, char move, int number){

    int[] childBins = parent.getBins().clone();
    if(move == 'C'){
      childBins = combine(number, childBins);
    }
    else{
      childBins = split(number, childBins);
    }
    
    Double key = getKey(childBins);
    if(childBins[0]==7&&childBins[1]==4&&childBins[2]==5&&childBins[3]==14){
      System.out.println(key);
    }
    GameState child = stateMap.get(key);
      
    if(child == null){
      child = new GameState(parent, childBins);
      stateMap.put(key, child);
      if(move == 'C'){
        child.setNumTerms();
      }
      if(parent.getLargestBin() == number){
        child.setLargestBin();
      }
      stateList.add(child);
      q.add(child);
    }
    
    parent.connectToChild(child);

    if(child.getNumTerms() < minTerms){
      minTerms = child.getNumTerms();
      indexList.add(stateList.size()-1);
    }
  }//EOF addChild

  static void removeGray(GameState currState){
    boolean winner = currState.getWinningState();
    for(int i = 0; i < currState.getNumChildren(); i++){
      GameState child = currState.getChild(i);
      
      boolean childWinner = child.getWinningState();
      if(winner && childWinner){
        currState.removeChild(i);
        currState.removeMove(i);
        i--;
      }
      else if(!child.getQueded()){
        q.add(child);
        child.setQueded();
      }
    }
  }//EOF removeGray

  public static void getPaths(List<GameState> winningStates, List<String> moves, int displayType){
      
    GameState currState = winningStates.get(winningStates.size()-1);
    int numChildren = currState.getNumChildren();
    
    if(currState.equals(stateList.get(indexList.get(indexList.size()-2)))){
      if(displayType == 1){
        displayStates(winningStates, moves);
      }
      else{
        numPaths++;
        System.out.printf("%d.\t", numPaths);
        System.out.println(moves);
        pathLength[moves.size()]++;
      }
    }
    
    for(int i = 0; i < numChildren; i++){
        
      List<GameState> copyStates;
      List<String> copyMoves;
      
      if(numChildren > 1){
        copyStates = new ArrayList<GameState>(winningStates);
        copyMoves = new ArrayList<String>(moves);
      }
      
      else{
        copyStates = winningStates;
        copyMoves = moves;
      }
      
      copyStates.add(currState.getChild(i));
      copyMoves.add(currState.getMove(i));
      getPaths(copyStates, copyMoves, displayType);
    }//EOF for
  }//EOF getPaths

  public static void displayStates(List<GameState> winningStates, List<String> moves){
    numPaths++;
    System.out.printf("\n\n%d.\t", numPaths);
    String s = String.valueOf(numPaths);
    int length = s.length()+1;
    for(int i = 0; i < winningStates.size(); i++){
        
      if(i > 0 && i % 10 == 0){
        System.out.print("\n");
        for(int j = 0; j < length; j++){
          System.out.print(" ");
        }
        System.out.print("\t");
      }
      
      GameState currState = winningStates.get(i);
      //Print all entries of a given GameState
      for(int j = currState.getLargestBin(); j >= 0; j--){
        System.out.printf("%d", currState.getEntry(j));
        if(j>0){
          System.out.printf(",");
        }
      }//EOF inside for
      
      //Prints the next move
      if(i < winningStates.size()-1){
        System.out.printf(" -> ", moves.get(i));
      }
      
    }//EOF outside for
  }//EOF displayStates

  public static void displayMoves(List<String> moves){
    numPaths++;
    String s = String.valueOf(numPaths);
    int length = s.length()+1;
    System.out.printf("\n%d.\t", numPaths);
    for(int i = 0; i < moves.size(); i++){
      if(i > 0 && i % 32 == 0){
        System.out.print("\n");
        for(int j = 0; j < length; j++){
          System.out.print(" ");
        }
        System.out.print("\t");
      }
      if(i % 2 == 0){
        System.out.printf("%S(1)", moves.get(i));
      }
      else{
        System.out.printf("%S(2)", moves.get(i));
      }
    //   System.out.println()
    }//EOF for
    System.out.printf("\n");
  }//EOF displayMoves
  
  public static float pathNums(GameState currState){
    float sum = 0;
    for(int i = 0; i < currState.getNumChildren(); i++){
      GameState child = currState.getChild(i);
      float childOptions = child.getNumOptions();
      if(childOptions < 0){
        childOptions = pathNums(child);
      }
      sum += childOptions;
    }
    if(currState.getNumChildren() > 0){
      currState.setNumOptions(sum); 
    }
    return currState.getNumOptions();
  }
  
}//EOF class WinningPaths
