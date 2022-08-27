
package player;

import mnkgame.*;
import java.util.Random;
import java.util.ArrayList;
import java.lang.Math.*;

public class MrRobot implements MNKPlayer{

    private Random rand;
    private MNKBoard Board;
    private boolean First;
    private MNKGameState RobotWin;
    private MNKGameState HumanWin;
    private int TIMEOUT;
    private long Seconds;


    public  MrRobot(){
    }

    public void initPlayer(int m, int n, int k, boolean First, int timeout){

      rand    = new Random(System.currentTimeMillis());
      Board = new MNKBoard(m,n,k);
      This.First = First;
      RobotWin   = first ? MNKGameState.WINP1 : MNKGameState.WINP2;
		  HumanWin = first ? MNKGameState.WINP2 : MNKGameState.WINP1;
      TIMEOUT = timeout;

    }

    public MNKCell selectCell(MNKCell[] MC, MNKCell[] FC){

      long start = System.currentTimeMillis();

  		if(MC.length > 0) {
  			MNKCell c = MC[MC.length-1]; // Recover the last move from MC
  			B.markCell(c.i,c.j);         // Save the last move in the local MNKBoard
  		}

  		// If there is just one possible move, return immediately
  		if(FC.length == 1)
  			return FC[0];

      //se la prima mossa spetta al mio giocatore può essere effettuata randomicamente
      if(RobotWin == MNKGameState.WINP1 && MC.length==0){
  			MNKCell Cell = FC[rand.nextInt(FC.length)];
  			Board.markCell(Cell.i, Cell.j);
  			return Cell;
  		}

      //chiamata di alphabeta

    }

    public String playerName(){
        return "MrRobot";
    }

}
