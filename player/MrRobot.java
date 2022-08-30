package player;

import mnkgame.*;
import java.util.Random;
import java.util.ArrayList;
import java.lang.Math.*;

public class MrRobot implements MNKPlayer{

    private Random rand;
    private MNKBoard board;
    private boolean first;
    private MNKGameState RobotWin;
    private MNKGameState HumanWin;
    private int TIMEOUT;
    private long seconds;


    public  MrRobot(){
    }

    public void initPlayer(int m, int n, int k, boolean first, int timeout){

      rand    = new Random(System.currentTimeMillis());
      board = new MNKBoard(m,n,k);
      this.first = first;
      RobotWin = first ? MNKGameState.WINP1 : MNKGameState.WINP2;
		  HumanWin = first ? MNKGameState.WINP2 : MNKGameState.WINP1;
      TIMEOUT = timeout;

    }

    public MNKCell selectCell(MNKCell[] MC, MNKCell[] FC){

      long start = System.currentTimeMillis();

  		if(MC.length > 0) {
  			MNKCell cell = MC[MC.length-1]; // Recover the last move from MC
  			board.markCell(cell.i, cell.j); // Save the last move in the local MNKBoard
  		}

  		// If there is just one possible move, return immediately
  		if(FC.length == 1)
  			return FC[0];

      //se la prima mossa spetta al mio giocatore può essere effettuata randomicamente
      if(RobotWin == MNKGameState.WINP1 && MC.length==0){
  			MNKCell cell = FC[rand.nextInt(FC.length)];
  			board.markCell(cell.i, cell.j);
  			return cell;
  		}

      int score = 0;
      double maxEval = Integer.MIN_VALUE;
      int pos = rand.nextInt(FC.length); 
      MNKCell result = FC[pos]; // random move
      
      for(MNKCell potentialCell : FC) {
        
        // If time is running out, return the randomly selected  cell
              if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(99.0/100.0)) {
          break;
          
        }else{
          
          board.markCell(potentialCell.i, potentialCell.j);	
          
          score = alphabeta(board, true, 5, Integer.MIN_VALUE, Integer.MAX_VALUE, start, TIMEOUT, first);
          
          if(score > maxEval){
            maxEval = score;
            result = potentialCell;
          }
          
          board.unmarkCell();								
        }	
      }
      
      board.markCell(result.i, result.j);		
      return result;

    }

    public String playerName(){
        return "MrRobot";
    }

}
